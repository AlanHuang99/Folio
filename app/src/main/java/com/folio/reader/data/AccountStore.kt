package com.folio.reader.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore file name must match the exclude in res/xml/data_extraction_rules.xml
// (datastore/auth.preferences_pb) so stored credentials never leave the device.
private val Context.authDataStore by preferencesDataStore(name = "auth")

/** Persists the set of signed-in accounts and which one is active. */
@Singleton
class AccountStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ds = context.authDataStore
    private val gson = Gson()

    val accounts: Flow<List<Account>> = ds.data.map { decode(it[KEY_ACCOUNTS]) }

    val activeAccount: Flow<Account?> = ds.data.map { prefs ->
        Accounts.resolveActive(decode(prefs[KEY_ACCOUNTS]), prefs[KEY_ACTIVE_ID])
    }

    /** Migrate a pre-multi-account single session (legacy keys) into the account list. */
    suspend fun migrateIfNeeded() {
        ds.edit { p ->
            if (p[KEY_ACCOUNTS] != null) return@edit
            val url = p[KEY_LEGACY_SERVER]
            val token = p[KEY_LEGACY_TOKEN]
            val name = p[KEY_LEGACY_NAME]
            if (!url.isNullOrBlank() && !token.isNullOrBlank()) {
                val account = Account(Accounts.idFor(url, name ?: url), url, name ?: url, token)
                p[KEY_ACCOUNTS] = gson.toJson(listOf(account))
                p[KEY_ACTIVE_ID] = account.id
            } else {
                p[KEY_ACCOUNTS] = "[]"
            }
            // Legacy keys are no longer read; drop them.
            p.remove(KEY_LEGACY_SERVER)
            p.remove(KEY_LEGACY_TOKEN)
            p.remove(KEY_LEGACY_NAME)
        }
    }

    /** Add or update an account and make it active. */
    suspend fun upsertAndActivate(serverUrl: String, userName: String, token: String) {
        ds.edit { p ->
            val account = Account(Accounts.idFor(serverUrl, userName), serverUrl, userName, token)
            p[KEY_ACCOUNTS] = gson.toJson(Accounts.upsert(decode(p[KEY_ACCOUNTS]), account))
            p[KEY_ACTIVE_ID] = account.id
        }
    }

    suspend fun setActive(id: String) {
        ds.edit { it[KEY_ACTIVE_ID] = id }
    }

    /** Remove an account; if it was active, fall back to the first remaining one. */
    suspend fun remove(id: String) {
        ds.edit { p ->
            val list = Accounts.remove(decode(p[KEY_ACCOUNTS]), id)
            p[KEY_ACCOUNTS] = gson.toJson(list)
            if (p[KEY_ACTIVE_ID] == id) {
                val next = list.firstOrNull()
                if (next != null) p[KEY_ACTIVE_ID] = next.id else p.remove(KEY_ACTIVE_ID)
            }
        }
    }

    private fun decode(json: String?): List<Account> =
        if (json.isNullOrBlank()) emptyList()
        else runCatching { gson.fromJson(json, Array<Account>::class.java)?.toList() ?: emptyList() }
            .getOrDefault(emptyList())

    companion object {
        private val KEY_ACCOUNTS = stringPreferencesKey("accounts")
        private val KEY_ACTIVE_ID = stringPreferencesKey("active_account_id")
        // Legacy single-account keys (pre-0.8.0), read once during migration.
        private val KEY_LEGACY_SERVER = stringPreferencesKey("server_url")
        private val KEY_LEGACY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_LEGACY_NAME = stringPreferencesKey("user_name")
    }
}
