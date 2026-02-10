package assignment1.krzysztofoko.s16001089.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import assignment1.krzysztofoko.s16001089.data.UserTheme

/**
 * Extension to determine if a color is "bright" or "dark".
 * Useful for determining contrasting text colors.
 */
fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}

/**
 * Generates a full Material 3 ColorScheme based on user-defined colors from the database.
 * Includes intelligent fallbacks for missing (null) values.
 */
fun createCustomColorScheme(userTheme: UserTheme): ColorScheme {
    val primary = userTheme.customPrimary?.let { Color(it) } ?: DarkPrimary
    val onPrimary = userTheme.customOnPrimary?.let { Color(it) } ?: (if (primary.isLight()) Color.Black else Color.White)
    
    val primaryContainer = userTheme.customPrimaryContainer?.let { Color(it) } ?: primary.copy(alpha = 0.3f)
    val onPrimaryContainer = userTheme.customOnPrimaryContainer?.let { Color(it) } ?: (if (primaryContainer.isLight()) Color.Black else Color.White)
    
    val secondary = userTheme.customSecondary?.let { Color(it) } ?: DarkSecondary
    val onSecondary = userTheme.customOnSecondary?.let { Color(it) } ?: (if (secondary.isLight()) Color.Black else Color.White)
    
    val secondaryContainer = userTheme.customSecondaryContainer?.let { Color(it) } ?: secondary.copy(alpha = 0.3f)
    val onSecondaryContainer = userTheme.customOnSecondaryContainer?.let { Color(it) } ?: (if (secondaryContainer.isLight()) Color.Black else Color.White)

    val tertiary = userTheme.customTertiary?.let { Color(it) } ?: DarkTertiary
    val onTertiary = userTheme.customOnTertiary?.let { Color(it) } ?: (if (tertiary.isLight()) Color.Black else Color.White)
    
    val tertiaryContainer = userTheme.customTertiaryContainer?.let { Color(it) } ?: tertiary.copy(alpha = 0.3f)
    val onTertiaryContainer = userTheme.customOnTertiaryContainer?.let { Color(it) } ?: (if (tertiaryContainer.isLight()) Color.Black else Color.White)
    
    val background = userTheme.customBackground?.let { Color(it) } ?: DarkBg
    val onBackground = userTheme.customOnBackground?.let { Color(it) } ?: (if (background.isLight()) Color.Black else Color.White)
    
    val surface = userTheme.customSurface?.let { Color(it) } ?: DarkSurface
    val onSurface = userTheme.customOnSurface?.let { Color(it) } ?: (if (surface.isLight()) Color.Black else Color.White)
    
    return if (userTheme.customIsDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface
        )
    }
}
