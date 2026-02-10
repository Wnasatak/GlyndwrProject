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
 * Supported theme presets for the application.
 */
enum class Theme {
    LIGHT, DARK, GRAY, SKY, FOREST, DARK_BLUE, CUSTOM
}

/**
 * CompositionLocal to allow components to access the current app-specific Theme enum.
 */
val LocalAppTheme = staticCompositionLocalOf { Theme.DARK }

/**
 * The main Theme Engine for the Glyndŵr Project.
 * Optimized to handle preset schemes, dynamic OS colors, and live custom themes.
 */
@Composable
fun GlyndwrProjectTheme(
    theme: Theme = Theme.DARK,
    userTheme: UserTheme? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Choose the appropriate color scheme based on user selection and system support
    val colorScheme = when {
        theme == Theme.CUSTOM && userTheme != null -> {
            createCustomColorScheme(userTheme)
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (theme == Theme.DARK || theme == Theme.DARK_BLUE) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        else -> when (theme) {
            Theme.LIGHT -> LightColorScheme
            Theme.DARK -> DarkColorScheme
            Theme.GRAY -> GrayColorScheme
            Theme.SKY -> SkyColorScheme
            Theme.FOREST -> ForestColorScheme
            Theme.DARK_BLUE -> DarkBlueColorScheme
            Theme.CUSTOM -> DarkColorScheme // Fallback
        }
    }

    CompositionLocalProvider(LocalAppTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
