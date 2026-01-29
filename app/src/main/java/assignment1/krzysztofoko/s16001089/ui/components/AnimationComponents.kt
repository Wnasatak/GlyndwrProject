package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
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
 *
 * The animation uses an infinite transition that oscillates between:
 * - Scale: 1.0f to 1.4f
 * - Alpha: 0.2f to 0.6f
 *
 * This is useful for background glows or highlighting specific UI elements with a breathing effect.
 *
 * @return A [Pair] where the first value is the animated scale (Float) and the second is the animated alpha (Float).
 */
@Composable
fun rememberGlowAnimation(): Pair<Float, Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
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
 * A composable that displays the Glyndwr University logo with a continuous 360-degree rotation.
 *
 * Features:
 * - Infinite rotation using [LinearEasing] for a smooth, constant speed.
 * - Circular clipping applied to the image.
 * - Uses [AsyncImage] to load the logo from assets.
 *
 * @param modifier [Modifier] to be applied to the logo container.
 */
@Composable
fun SpinningLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoSpin")
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
 * Renders a dynamic, animated background with horizontal waves using the Compose Canvas.
 *
 * The component draws a solid background color and overlays two sine-wave paths that move over time.
 * It automatically adapts colors based on the theme (Dark vs Light).
 *
 * @param isDarkTheme Boolean flag to toggle between dark and light theme colors.
 * @param animationDuration Duration in milliseconds for one full phase cycle (speed of wave).
 * @param wave1HeightFactor vertical position of the first wave (0.0 to 1.0 relative to height).
 * @param wave2HeightFactor vertical position of the second wave (0.0 to 1.0 relative to height).
 * @param wave1Amplitude The height (peak-to-peak) of the first wave's oscillations in pixels.
 * @param wave2Amplitude The height (peak-to-peak) of the second wave's oscillations in pixels.
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
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) WaveSlateDarkBg else Color.White
    val waveColor1 = if (isDarkTheme) WaveSlateDark1 else WaveBlueLight1

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        // 1. Draw solid background
        drawRect(color = bgColor)

        val width = size.width
        val height = size.height

        // 2. Calculate and draw the first wave path
        val path1 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                // Sine wave calculation based on horizontal position and animated phase
                val y = height * wave1HeightFactor + sin(relativeX * 2 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)

        // 3. Calculate and draw the second (overlapping) wave path
        val path2 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                // Different frequency (3 * PI) and direction/speed (-phase * 0.7f) for visual variety
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
 * Renders a dynamic, animated background with vertical waves flowing from the right side.
 *
 * Similar to [HorizontalWavyBackground], but the waves are oriented vertically. This is often
 * used for side panels or unique artistic backgrounds.
 *
 * @param isDarkTheme Boolean flag to toggle between dark and light theme colors.
 * @param animationDuration Duration in milliseconds for one full phase cycle.
 * @param wave1WidthFactor horizontal position of the first wave (0.0 to 1.0 relative to width).
 * @param wave2WidthFactor horizontal position of the second wave (0.0 to 1.0 relative to width).
 * @param wave1Amplitude The width of the first wave's oscillations in pixels.
 * @param wave2Amplitude The width of the second wave's oscillations in pixels.
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
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) WaveSlateDarkBg else Color.White
    val waveColor1 = if (isDarkTheme) WaveSlateDark1 else WaveBlueLight1
    val waveColor2 = if (isDarkTheme) WaveSlateDark2 else WaveBlueLight2

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        // 1. Draw solid background
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height

        // 2. Draw first vertical wave path
        val path1 = Path().apply {
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) {
                val relativeY = y.toFloat() / height
                // Sine wave calculation based on vertical position
                val x = width * wave1WidthFactor + sin(relativeY * 1.5 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x, y.toFloat())
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)

        // 3. Draw second vertical wave path
        val path2 = Path().apply {
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) {
                val relativeY = y.toFloat() / height
                // Offset frequency and phase for a natural overlapping look
                val x = width * wave2WidthFactor + sin(relativeY * 2.5 * PI - phase * 0.8f).toFloat() * wave2Amplitude
                lineTo(x, y.toFloat())
            }
            lineTo(width, height)
            close()
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.7f))
    }
}
