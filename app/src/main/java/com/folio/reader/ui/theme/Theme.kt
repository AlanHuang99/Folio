package com.folio.reader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CharcoalDarkColors = darkColorScheme(
    primary = CharcoalDarkPrimary,
    onPrimary = CharcoalDarkOnPrimary,
    primaryContainer = CharcoalDarkPrimaryContainer,
    onPrimaryContainer = CharcoalDarkOnPrimaryContainer,
    secondary = CharcoalDarkPrimary,
    onSecondary = CharcoalDarkOnPrimary,
    secondaryContainer = CharcoalDarkPrimaryContainer,
    onSecondaryContainer = CharcoalDarkOnPrimaryContainer,
    tertiary = CharcoalDarkPrimary,
    background = CharcoalDarkBackground,
    onBackground = CharcoalDarkOnSurface,
    surface = CharcoalDarkSurface,
    onSurface = CharcoalDarkOnSurface,
    surfaceVariant = CharcoalDarkSurfaceVariant,
    onSurfaceVariant = CharcoalDarkOnSurfaceVariant,
    outline = CharcoalDarkOutline,
    outlineVariant = CharcoalDarkOutlineVariant,
)

private val CharcoalLightColors = lightColorScheme(
    primary = CharcoalLightPrimary,
    onPrimary = CharcoalLightOnPrimary,
    primaryContainer = CharcoalLightPrimaryContainer,
    onPrimaryContainer = CharcoalLightOnPrimaryContainer,
    secondary = CharcoalLightPrimary,
    onSecondary = CharcoalLightOnPrimary,
    secondaryContainer = CharcoalLightPrimaryContainer,
    onSecondaryContainer = CharcoalLightOnPrimaryContainer,
    tertiary = CharcoalLightPrimary,
    background = CharcoalLightBackground,
    onBackground = CharcoalLightOnSurface,
    surface = CharcoalLightSurface,
    onSurface = CharcoalLightOnSurface,
    surfaceVariant = CharcoalLightSurfaceVariant,
    onSurfaceVariant = CharcoalLightOnSurfaceVariant,
    outline = CharcoalLightOutline,
    outlineVariant = CharcoalLightOutlineVariant,
)

/**
 * Folio's theme. Defaults to the brand Charcoal scheme (light/dark by system).
 * Material You (dynamicColor) is wired but off by default; the Phase 6 Appearance
 * picker will let the user choose Charcoal, the other looks, or Material You.
 */
@Composable
fun FolioTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> CharcoalDarkColors
        else -> CharcoalLightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FolioTypography,
        content = content
    )
}
