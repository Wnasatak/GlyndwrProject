package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import assignment1.krzysztofoko.s16001089.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * Creates and remembers a pair of animated values (scale and alpha) for a "glow" or "pulse" effect.
 * This is commonly used for loading states or highlighting important UI elements.
 *
 * @return A Pair containing the current scale (Float) and alpha (Float) values.
 */
@Composable
fun rememberGlowAnimation(): Pair<Float, Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    
    // Animate scale from 1.0 to 1.4 and back
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    
    // Animate alpha from 0.2 to 0.6 and back
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    return scale to alpha
}

/**
 * A composable that displays the Glyndŵr University logo with a continuous 360-degree rotation.
 * Often used as a branded loading indicator.
 *
 * @param modifier Modifier to be applied to the logo image.
 */
@Composable
fun SpinningLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoSpin")
    
    // Continuous linear rotation from 0 to 360 degrees
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    AsyncImage(
        model = formatAssetUrl("images/media/Glyndwr_University_Logo.png"),
        contentDescription = "Loading...",
        modifier = modifier
            .clip(CircleShape)
            .rotate(rotation),
        contentScale = ContentScale.Fit
    )
}

/**
 * Renders a dynamic, animated background with horizontal waves.
 * The waves adapt their color based on the current application theme.
 *
 * @param isDarkTheme Boolean flag indicating if the dark theme is active.
 * @param animationDuration Duration in milliseconds for one full wave cycle.
 * @param wave1HeightFactor vertical position of the first wave (0.0 to 1.0).
 * @param wave2HeightFactor vertical position of the second wave (0.0 to 1.0).
 * @param wave1Amplitude Peak height of the first wave.
 * @param wave2Amplitude Peak height of the second wave.
 */
@Composable
fun HorizontalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 12000,
    wave1HeightFactor: Float = 0.75f,
    wave2HeightFactor: Float = 0.82f,
    wave1Amplitude: Float = 60f,
    wave2Amplitude: Float = 40f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    // Animate the phase to shift the sine wave horizontally
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = MaterialTheme.colorScheme.background
    val currentTheme = LocalAppTheme.current
    
    // Determine wave color based on the active theme
    val waveColor1 = when(currentTheme) {
        Theme.DARK -> WaveSlateDark1
        Theme.SKY -> WaveSky1
        Theme.FOREST -> WaveForest1
        Theme.GRAY -> WaveGray1
        Theme.DARK_BLUE -> WaveDarkBlue1
        Theme.CUSTOM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        else -> WaveBlueLight1
    }

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        // Draw the solid background color
        drawRect(color = bgColor)

        val width = size.width
        val height = size.height

        // Path for the first (primary) wave
        val path1 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave1HeightFactor + sin(relativeX * 2 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)

        // Path for the second (secondary) wave with different parameters for a more complex look
        val path2 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave2HeightFactor + sin(relativeX * 3 * PI - phase * 0.7f).toFloat() * wave2Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path2, color = waveColor1.copy(alpha = 0.5f))
    }
}

/**
 * Renders a dynamic, animated background with vertical waves.
 * Typically used on side panels or as a decorative element on larger screens.
 *
 * @param isDarkTheme Boolean flag indicating if the dark theme is active.
 * @param animationDuration Duration in milliseconds for one full wave cycle.
 * @param wave1WidthFactor Horizontal position of the first wave.
 * @param wave2WidthFactor Horizontal position of the second wave.
 * @param wave1Amplitude Peak width of the first wave.
 * @param wave2Amplitude Peak width of the second wave.
 */
@Composable
fun VerticalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 10000,
    wave1WidthFactor: Float = 0.6f,
    wave2WidthFactor: Float = 0.75f,
    wave1Amplitude: Float = 100f,
    wave2Amplitude: Float = 60f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    // Animate the phase to shift the sine wave vertically
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = MaterialTheme.colorScheme.background
    val currentTheme = LocalAppTheme.current

    // Determine primary and secondary wave colors based on the active theme
    val waveColor1 = when(currentTheme) {
        Theme.DARK -> WaveSlateDark1
        Theme.SKY -> WaveSky1
        Theme.FOREST -> WaveForest1
        Theme.GRAY -> WaveGray1
        Theme.DARK_BLUE -> WaveDarkBlue1
        Theme.CUSTOM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> WaveBlueLight1
    }
    
    val waveColor2 = when(currentTheme) {
        Theme.DARK -> WaveSlateDark2
        Theme.SKY -> WaveSky2
        Theme.FOREST -> WaveForest2
        Theme.GRAY -> WaveGray2
        Theme.DARK_BLUE -> WaveDarkBlue2
        Theme.CUSTOM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        else -> WaveBlueLight2
    }

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        // Draw the solid background color
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height

        // Path for the first vertical wave
        val path1 = Path().apply {
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) {
                val relativeY = y.toFloat() / height
                val x = width * wave1WidthFactor + sin(relativeY * 1.5 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x, y.toFloat())
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)

        // Path for the second vertical wave
        val path2 = Path().apply {
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) {
                val relativeY = y.toFloat() / height
                val x = width * wave2WidthFactor + sin(relativeY * 2.5 * PI - phase * 0.8f).toFloat() * wave2Amplitude
                lineTo(x, y.toFloat())
            }
            lineTo(width, height)
            close()
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.7f))
    }
}
