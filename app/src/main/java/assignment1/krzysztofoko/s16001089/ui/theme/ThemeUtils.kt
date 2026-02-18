package assignment1.krzysztofoko.s16001089.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import assignment1.krzysztofoko.s16001089.data.UserTheme

/**
 * ThemeUtils.kt
 *
 * This utility file provides helper functions for dynamic theme generation and 
 * colour manipulation. It is a core part of the application's personalisation engine,
 * allowing users to create and apply custom colour schemes that persist across sessions.
 */

/**
 * Extension function to determine the perceived brightness (luminance) of a colour.
 * This is used to dynamically select contrasting text colours (Black or White) 
 * for custom-defined background or surface colours.
 * 
 * Logic: Uses standard ITU-R BT.601 coefficients for luminance calculation.
 * @return True if the colour is light (high luminance), False if it is dark.
 */
fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}

/**
 * Generates a full Material 3 ColorScheme based on user-defined parameters from the database.
 * 
 * This function implements a robust fallback mechanism:
 * 1. Primary choice: User's custom colour from the [UserTheme] entity.
 * 2. Secondary choice (if custom is null): Standard system theme defaults (e.g., [DarkPrimary]).
 * 3. Text Contrast: Automatically calculates the best 'on-' colour (Black/White) based 
 *    on the luminance of its parent container if a specific 'on-' colour isn't provided.
 *
 * @param userTheme The persistent theme entity containing custom ARGB colour values.
 * @return A complete [ColorScheme] ready for use in a [MaterialTheme].
 */
fun createCustomColorScheme(userTheme: UserTheme): ColorScheme {
    // RESOLVE PRIMARY: User choice -> Default fallback
    val primary = userTheme.customPrimary?.let { Color(it) } ?: DarkPrimary
    val onPrimary = userTheme.customOnPrimary?.let { Color(it) } ?: (if (primary.isLight()) Color.Black else Color.White)
    
    // RESOLVE CONTAINERS: Use alpha-modified version of the primary if not explicitly defined
    val primaryContainer = userTheme.customPrimaryContainer?.let { Color(it) } ?: primary.copy(alpha = 0.3f)
    val onPrimaryContainer = userTheme.customOnPrimaryContainer?.let { Color(it) } ?: (if (primaryContainer.isLight()) Color.Black else Color.White)
    
    // RESOLVE SECONDARY
    val secondary = userTheme.customSecondary?.let { Color(it) } ?: DarkSecondary
    val onSecondary = userTheme.customOnSecondary?.let { Color(it) } ?: (if (secondary.isLight()) Color.Black else Color.White)
    
    val secondaryContainer = userTheme.customSecondaryContainer?.let { Color(it) } ?: secondary.copy(alpha = 0.3f)
    val onSecondaryContainer = userTheme.customOnSecondaryContainer?.let { Color(it) } ?: (if (secondaryContainer.isLight()) Color.Black else Color.White)

    // RESOLVE TERTIARY
    val tertiary = userTheme.customTertiary?.let { Color(it) } ?: DarkTertiary
    val onTertiary = userTheme.customOnTertiary?.let { Color(it) } ?: (if (tertiary.isLight()) Color.Black else Color.White)
    
    val tertiaryContainer = userTheme.customTertiaryContainer?.let { Color(it) } ?: tertiary.copy(alpha = 0.3f)
    val onTertiaryContainer = userTheme.customOnTertiaryContainer?.let { Color(it) } ?: (if (tertiaryContainer.isLight()) Color.Black else Color.White)
    
    // RESOLVE BACKGROUND & SURFACE
    val background = userTheme.customBackground?.let { Color(it) } ?: DarkBg
    val onBackground = userTheme.customOnBackground?.let { Color(it) } ?: (if (background.isLight()) Color.Black else Color.White)
    
    val surface = userTheme.customSurface?.let { Color(it) } ?: DarkSurface
    val onSurface = userTheme.customOnSurface?.let { Color(it) } ?: (if (surface.isLight()) Color.Black else Color.White)
    
    // Return either a dark or light colour scheme based on user's 'dark mode' toggle
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
