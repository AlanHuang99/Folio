package com.folio.reader.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folio.reader.BuildConfig
import com.folio.reader.ui.theme.Appearance
import com.folio.reader.ui.theme.ThemeMode
import com.folio.reader.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val serverUrl by settingsViewModel.serverUrl.collectAsStateWithLifecycle()
    val userName by settingsViewModel.userName.collectAsStateWithLifecycle()
    val theme by themeViewModel.prefs.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("Account")
        ListItem(
            headlineContent = { Text(userName ?: "—") },
            supportingContent = { Text("Signed in") },
            leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
        )
        ListItem(
            headlineContent = { Text(serverUrl ?: "—") },
            supportingContent = { Text("Server") },
            leadingContent = { Icon(Icons.Filled.Dns, contentDescription = null) },
        )

        HorizontalDivider()
        SectionHeader("Appearance")
        SubLabel("Look")
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Appearance.entries.forEach { look ->
                LookSwatch(look, selected = look == theme.appearance) { themeViewModel.setAppearance(look) }
            }
        }
        Spacer(Modifier.height(8.dp))
        SubLabel("Theme")
        ThemeMode.entries.forEach { mode ->
            ListItem(
                headlineContent = { Text(mode.label) },
                leadingContent = {
                    RadioButton(selected = theme.mode == mode, onClick = { themeViewModel.setMode(mode) })
                },
                modifier = Modifier.selectable(
                    selected = theme.mode == mode,
                    onClick = { themeViewModel.setMode(mode) },
                ),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ListItem(
                headlineContent = { Text("Material You") },
                supportingContent = { Text("Use colors from your wallpaper") },
                leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) },
                trailingContent = {
                    Switch(checked = theme.dynamicColor, onCheckedChange = { themeViewModel.setDynamicColor(it) })
                },
            )
        }

        HorizontalDivider()
        SectionHeader("About")
        ListItem(
            headlineContent = { Text("Version") },
            supportingContent = { Text(BuildConfig.VERSION_NAME) },
        )
        ListItem(
            headlineContent = { Text("Folio") },
            supportingContent = { Text("A free, open-source reader for FreshRSS") },
        )

        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Sign out", color = MaterialTheme.colorScheme.error) },
            leadingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            modifier = Modifier.clickable { settingsViewModel.signOut() },
        )
    }
}

@Composable
private fun LookSwatch(look: Appearance, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(60.dp).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(look.swatch)
                .then(
                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            look.displayName,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SubLabel(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}
