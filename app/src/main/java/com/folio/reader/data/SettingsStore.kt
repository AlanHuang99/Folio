package com.folio.reader.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore file name must match the exclude in res/xml/data_extraction_rules.xml
// (datastore/auth.preferences_pb) so stored credentials never leave the device.
private val Context.authDataStore by preferencesDataStore(name = "auth")

/** Persists the server URL, auth token, and display name. */
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ds = context.authDataStore

    val serverUrl: Flow<String?> = ds.data.map { it[KEY_SERVER_URL] }
    val authToken: Flow<String?> = ds.data.map { it[KEY_AUTH_TOKEN] }
    val userName: Flow<String?> = ds.data.map { it[KEY_USER_NAME] }

    suspend fun saveSession(serverUrl: String, token: String, userName: String) {
        ds.edit {
            it[KEY_SERVER_URL] = serverUrl
            it[KEY_AUTH_TOKEN] = token
            it[KEY_USER_NAME] = userName
        }
    }

    suspend fun clear() {
        ds.edit { it.clear() }
    }

    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
    }
}
