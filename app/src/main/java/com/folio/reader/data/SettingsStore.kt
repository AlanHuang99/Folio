package com.folio.reader.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
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

// Non-credential app preferences (theme, etc.) — kept separate from credentials.
private val Context.prefsDataStore by preferencesDataStore(name = "settings")

/** Persists the server URL, auth token, and display name. */
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ds = context.authDataStore
    private val prefs = context.prefsDataStore

    val serverUrl: Flow<String?> = ds.data.map { it[KEY_SERVER_URL] }
    val authToken: Flow<String?> = ds.data.map { it[KEY_AUTH_TOKEN] }
    val userName: Flow<String?> = ds.data.map { it[KEY_USER_NAME] }

    val themeMode: Flow<String?> = prefs.data.map { it[KEY_THEME_MODE] }
    val dynamicColor: Flow<Boolean> = prefs.data.map { it[KEY_DYNAMIC_COLOR] ?: false }
    val appearance: Flow<String?> = prefs.data.map { it[KEY_APPEARANCE] }
    // The appearance whose launcher icon is currently enabled (to avoid redundant switches).
    val appliedIcon: Flow<String?> = prefs.data.map { it[KEY_APPLIED_ICON] }

    suspend fun setThemeMode(mode: String) {
        prefs.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        prefs.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAppearance(name: String) {
        prefs.edit { it[KEY_APPEARANCE] = name }
    }

    suspend fun setAppliedIcon(name: String) {
        prefs.edit { it[KEY_APPLIED_ICON] = name }
    }

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
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_APPEARANCE = stringPreferencesKey("appearance")
        private val KEY_APPLIED_ICON = stringPreferencesKey("applied_icon")
    }
}
