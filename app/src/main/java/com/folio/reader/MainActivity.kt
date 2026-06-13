package com.folio.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.folio.reader.data.SettingsStore
import com.folio.reader.data.appearance.IconManager
import com.folio.reader.ui.FolioRoot
import com.folio.reader.ui.theme.Appearance
import com.folio.reader.ui.theme.FolioTheme
import com.folio.reader.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var iconManager: IconManager

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themePrefs by themeViewModel.prefs.collectAsStateWithLifecycle()
            FolioTheme(
                appearance = themePrefs.appearance,
                themeMode = themePrefs.mode,
                dynamicColor = themePrefs.dynamicColor,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FolioRoot()
                }
            }
        }
    }

    // Apply the selected look's launcher icon when leaving the foreground. Doing this
    // while foreground finishes the task on some OEM launchers (MIUI/HyperOS).
    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            val desired = settingsStore.appearance.first() ?: return@launch
            if (desired != settingsStore.appliedIcon.first()) {
                iconManager.applyIcon(Appearance.fromName(desired))
                settingsStore.setAppliedIcon(desired)
            }
        }
    }
}
