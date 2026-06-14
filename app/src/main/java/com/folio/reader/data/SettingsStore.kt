package com.folio.reader.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Non-credential app preferences (theme, appearance). Accounts/credentials live
// separately in AccountStore's "auth" datastore.
private val Context.prefsDataStore by preferencesDataStore(name = "settings")

/** Persists non-credential UI preferences (theme mode, appearance, launcher icon). */
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.prefsDataStore

    val themeMode: Flow<String?> = prefs.data.map { it[KEY_THEME_MODE] }
    val dynamicColor: Flow<Boolean> = prefs.data.map { it[KEY_DYNAMIC_COLOR] ?: false }
    val appearance: Flow<String?> = prefs.data.map { it[KEY_APPEARANCE] }
    // The appearance whose launcher icon is currently enabled (to avoid redundant switches).
    val appliedIcon: Flow<String?> = prefs.data.map { it[KEY_APPLIED_ICON] }

    // Names of categories collapsed (folded) in the Feeds screen.
    val collapsedCategories: Flow<Set<String>> = prefs.data.map { it[KEY_COLLAPSED] ?: emptySet() }

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

    suspend fun toggleCategoryCollapsed(name: String) {
        prefs.edit {
            val current = it[KEY_COLLAPSED] ?: emptySet()
            it[KEY_COLLAPSED] = if (name in current) current - name else current + name
        }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_APPEARANCE = stringPreferencesKey("appearance")
        private val KEY_APPLIED_ICON = stringPreferencesKey("applied_icon")
        private val KEY_COLLAPSED = stringSetPreferencesKey("collapsed_categories")
    }
}
