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
        drawRect(color = bgColor)

        val width = size.width
        val height = size.height

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
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height

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
