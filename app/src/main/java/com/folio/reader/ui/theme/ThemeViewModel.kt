package com.folio.reader.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folio.reader.data.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemePrefs(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settings: SettingsStore
) : ViewModel() {

    val prefs: StateFlow<ThemePrefs> =
        combine(settings.themeMode, settings.dynamicColor) { mode, dynamic ->
            ThemePrefs(
                mode = runCatching { ThemeMode.valueOf(mode ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM),
                dynamicColor = dynamic,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemePrefs())

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode.name) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settings.setDynamicColor(enabled) }
    }
}
