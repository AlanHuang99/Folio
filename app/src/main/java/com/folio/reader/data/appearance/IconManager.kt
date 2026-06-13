package com.folio.reader.data.appearance

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.folio.reader.ui.theme.Appearance
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Switches the launcher icon to match the selected [Appearance] using activity-aliases
 * (exactly one enabled at a time). Note: some OEM launchers (notably MIUI/HyperOS) update
 * the icon lazily or show a brief "added" toast; the change always lands, sometimes after
 * the launcher refreshes.
 */
@Singleton
class IconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private fun aliasFor(appearance: Appearance): String =
        "com.folio.reader.Launcher" + appearance.name.lowercase().replaceFirstChar { it.uppercase() }

    fun applyIcon(selected: Appearance) {
        val pm = context.packageManager
        Appearance.entries.forEach { appearance ->
            val state = if (appearance == selected) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            runCatching {
                pm.setComponentEnabledSetting(
                    ComponentName(context, aliasFor(appearance)),
                    state,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
