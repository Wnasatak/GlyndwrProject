package assignment1.krzysztofoko.s16001089.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import assignment1.krzysztofoko.s16001089.data.UserTheme

/**
 *
 * This file serves as the primary Theme Configuration for the Glyndŵr Project.
 * It leverages Material Design 3 (M3) to provide a flexible and deeply personalized 
 * user experience, supporting static presets, dynamic Android 12+ system colors, 
 * and a real-time custom theme engine.
 */

/**
 * Enumeration of all supported visual identities within the application.
 * - LIGHT/DARK: Standard institutional contrast modes.
 * - GRAY/SKY/FOREST: Specialized color palettes for accessibility and preference.
 * - DARK_BLUE: High-impact branded identity.
 * - CUSTOM: User-defined color scheme persisted in the local database.
 */
enum class Theme {
    LIGHT, DARK, GRAY, SKY, FOREST, DARK_BLUE, CUSTOM
}

/**
 * CompositionLocal key used to provide the current [Theme] state down the UI tree.
 * This allows child components to reactively adjust their internal logic or custom 
 * drawing based on the active app-wide theme preset.
 */
val LocalAppTheme = staticCompositionLocalOf { Theme.DARK }

/**
 * GlyndwrProjectTheme is the root-level Composable that applies the visual design system.
 * It coordinates the resolution of the final ColorScheme through three distinct paths:
 * 1. User Customization: Real-time ARGB values from the [UserTheme] entity.
 * 2. Dynamic Theming: System-level "Material You" colors on supported Android versions (S+).
 * 3. Static Presets: Hardcoded high-quality color palettes defined in Color.kt.
 *
 * @param theme The active [Theme] preset selected by the user or system.
 * @param userTheme The persistent custom theme data containing specific color overrides.
 * @param dynamicColor Boolean toggle to enable Android 12+ dynamic color extraction.
 * @param content The UI hierarchy to be wrapped in this theme.
 */
@Composable
fun GlyndwrProjectTheme(
    theme: Theme = Theme.DARK,
    userTheme: UserTheme? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // RESOLUTION LOGIC: Determines the final Material 3 ColorScheme
    val colorScheme = when {
        // PATH 1: Custom Theme Engine - High-priority user personalization
        theme == Theme.CUSTOM && userTheme != null -> {
            createCustomColorScheme(userTheme)
        }
        
        // PATH 2: Android 12+ Dynamic Theming (Material You)
        // Automatically extracts accent colors from the user's system wallpaper.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (theme == Theme.DARK || theme == Theme.DARK_BLUE) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        
        // PATH 3: Static Presets - Consistent branded experiences
        else -> when (theme) {
            Theme.LIGHT -> LightColorScheme
            Theme.DARK -> DarkColorScheme
            Theme.GRAY -> GrayColorScheme
            Theme.SKY -> SkyColorScheme
            Theme.FOREST -> ForestColorScheme
            Theme.DARK_BLUE -> DarkBlueColorScheme
            Theme.CUSTOM -> DarkColorScheme // Safety fallback for null userTheme
        }
    }

    // PROVIDER: Injects the Theme enum and MaterialTheme state into the composition
    CompositionLocalProvider(LocalAppTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
