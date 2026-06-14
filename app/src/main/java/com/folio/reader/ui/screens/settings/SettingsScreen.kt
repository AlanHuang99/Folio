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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folio.reader.BuildConfig
import com.folio.reader.data.Account
import com.folio.reader.ui.theme.Appearance
import com.folio.reader.ui.theme.ThemeMode
import com.folio.reader.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(
    onAddAccount: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val accounts by settingsViewModel.accounts.collectAsStateWithLifecycle()
    val activeAccount by settingsViewModel.activeAccount.collectAsStateWithLifecycle()
    val theme by themeViewModel.prefs.collectAsStateWithLifecycle()
    var removeTarget by remember { mutableStateOf<Account?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader("Accounts")
        accounts.forEach { account ->
            val isActive = account.id == activeAccount?.id
            ListItem(
                headlineContent = { Text(account.userName) },
                supportingContent = { Text(account.serverUrl) },
                leadingContent = {
                    RadioButton(
                        selected = isActive,
                        onClick = { settingsViewModel.switchAccount(account.id) },
                    )
                },
                trailingContent = {
                    IconButton(onClick = { removeTarget = account }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Remove account",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                modifier = Modifier.selectable(
                    selected = isActive,
                    onClick = { settingsViewModel.switchAccount(account.id) },
                ),
            )
        }
        ListItem(
            headlineContent = { Text("Add account") },
            leadingContent = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onAddAccount),
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

        if (activeAccount != null) {
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Sign out", color = MaterialTheme.colorScheme.error) },
                supportingContent = { activeAccount?.let { Text(it.userName) } },
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

    removeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("Remove account?") },
            text = { Text("Remove \"${target.userName}\" on ${target.serverUrl}? You can add it again later.") },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.removeAccount(target.id)
                    removeTarget = null
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { removeTarget = null }) { Text("Cancel") } },
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
