package com.folio.reader.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Each appearance keeps the neutral surfaces and varies the accent (primary). The
// bold per-look colour lives in the launcher icon; in-app it reads as a tasteful tint.
private val DarkOnAccent = Color(0xFF12181C)

private fun darkSchemeFor(primary: Color): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = DarkOnAccent,
    primaryContainer = CharcoalDarkPrimaryContainer,
    onPrimaryContainer = CharcoalDarkOnPrimaryContainer,
    secondary = primary,
    onSecondary = DarkOnAccent,
    secondaryContainer = CharcoalDarkPrimaryContainer,
    onSecondaryContainer = CharcoalDarkOnPrimaryContainer,
    tertiary = primary,
    background = CharcoalDarkBackground,
    onBackground = CharcoalDarkOnSurface,
    surface = CharcoalDarkSurface,
    onSurface = CharcoalDarkOnSurface,
    surfaceVariant = CharcoalDarkSurfaceVariant,
    onSurfaceVariant = CharcoalDarkOnSurfaceVariant,
    outline = CharcoalDarkOutline,
    outlineVariant = CharcoalDarkOutlineVariant,
)

private fun lightSchemeFor(primary: Color): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = CharcoalLightPrimaryContainer,
    onPrimaryContainer = CharcoalLightOnPrimaryContainer,
    secondary = primary,
    onSecondary = Color.White,
    secondaryContainer = CharcoalLightPrimaryContainer,
    onSecondaryContainer = CharcoalLightOnPrimaryContainer,
    tertiary = primary,
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
 * The selectable looks. Each maps to an in-app accent (here) and a launcher icon
 * (res/mipmap-anydpi-v26/ic_launcher_<name> + an activity-alias). CHARCOAL is the default.
 */
enum class Appearance(
    val displayName: String,
    val swatch: Color,
    private val darkPrimary: Color,
    private val lightPrimary: Color,
) {
    CHARCOAL("Charcoal", Color(0xFF5B6169), Color(0xFFC4CBD4), Color(0xFF3C434B)),
    EVERGREEN("Evergreen", Color(0xFF1C6B5A), Color(0xFF5FC9AE), Color(0xFF1C6B5A)),
    SLATE("Slate", Color(0xFF3D5077), Color(0xFF9DB4D4), Color(0xFF3D5077)),
    PAPER("Paper", Color(0xFFB5601F), Color(0xFFE0A85C), Color(0xFFB5601F)),
    OCEAN("Ocean", Color(0xFF1C5A86), Color(0xFF7FB6E6), Color(0xFF1C5A86)),
    PLUM("Plum", Color(0xFF7A3D63), Color(0xFFD6A6C8), Color(0xFF7A3D63)),
    CLAY("Clay", Color(0xFFA24A2C), Color(0xFFE59B7A), Color(0xFFA24A2C)),
    INDIGO("Indigo", Color(0xFF3A4490), Color(0xFFAEB8F0), Color(0xFF3A4490)),
    WINE("Wine", Color(0xFF8A3047), Color(0xFFE0A0B0), Color(0xFF8A3047));

    fun darkScheme(): ColorScheme = darkSchemeFor(darkPrimary)
    fun lightScheme(): ColorScheme = lightSchemeFor(lightPrimary)

    companion object {
        fun fromName(name: String?): Appearance = entries.firstOrNull { it.name == name } ?: CHARCOAL
    }
}
