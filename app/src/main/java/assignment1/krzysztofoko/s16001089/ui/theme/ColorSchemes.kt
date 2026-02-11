package assignment1.krzysztofoko.s16001089.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkTertiary,
    onPrimaryContainer = Color.White,
    secondary = DarkSecondary,
    onSecondary = Color.Black,
    tertiary = DarkTertiary,
    background = DarkBg,
    surface = DarkSurface, // Restored distinct surface color
    surfaceVariant = Color(0xFF334155),
    surfaceTint = Color.Transparent,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f)
)

val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimary.copy(alpha = 0.1f),
    onPrimaryContainer = BluePrimary,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    tertiary = BlueTertiary,
    background = LightBg,
    surface = Color(0xFFE2E8F0), // Clearly visible container color
    surfaceVariant = Color.White,
    surfaceTint = Color.Transparent,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

val GrayColorScheme = lightColorScheme(
    primary = GrayPrimary,
    onPrimary = Color.White,
    primaryContainer = GrayPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = GrayPrimary,
    secondary = GraySecondary,
    tertiary = GrayTertiary,
    background = GrayBg,
    surface = Color(0xFFCBD5E1), // Professional slate containers
    surfaceVariant = Color(0xFFF1F5F9),
    surfaceTint = Color.Transparent,
    onBackground = GrayOnSurface,
    onSurface = GrayOnSurface,
    outline = GrayBorder,
    outlineVariant = GrayBorder.copy(alpha = 0.5f)
)

val SkyColorScheme = lightColorScheme(
    primary = SkyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = SkyPrimary,
    secondary = SkySecondary,
    tertiary = SkyTertiary,
    background = SkyBg,
    surface = Color(0xFFE0F2FE), // Visible light blue containers
    surfaceVariant = Color.White,
    surfaceTint = Color.Transparent,
    onBackground = SkyOnSurface,
    onSurface = SkyOnSurface,
    outline = SkyBorder,
    outlineVariant = SkyBorder.copy(alpha = 0.5f)
)

val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBg,
    surface = Color(0xFFDCFCE7), // Visible light green containers
    surfaceVariant = Color.White,
    surfaceTint = Color.Transparent,
    onBackground = ForestOnSurface,
    onSurface = ForestOnSurface,
    outline = ForestBorder,
    outlineVariant = ForestBorder.copy(alpha = 0.5f)
)

val DarkBlueColorScheme = darkColorScheme(
    primary = DarkBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F172A),
    onPrimaryContainer = DarkBluePrimary,
    secondary = DarkBlueSecondary,
    tertiary = DarkBlueTertiary,
    background = DarkBlueBg,
    surface = DarkBlueSurface, // Deep navy containers
    surfaceVariant = Color(0xFF1E293B),
    surfaceTint = Color.Transparent,
    onBackground = DarkBlueOnSurface,
    onSurface = DarkBlueOnSurface,
    outline = DarkBlueBorder,
    outlineVariant = DarkBlueBorder.copy(alpha = 0.5f)
)
