package assignment1.krzysztofoko.s16001089.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import coil.compose.AsyncImage

/**
 * Visual components for the SplashScreen.
 * 
 * This file contains specialized UI elements like the animated moving background,
 * the multi-layered breathing logo, and the loading status footer.
 */

/**
 * Renders an animated background using a linear gradient overlay on top of a campus image.
 * 
 * Logic:
 * - Base Layer: Static image of the Glyndŵr University campus.
 * - Overlay Layer: A 'swimming' linear gradient that shifts its start/end offsets based 
 *   on the animation progress (0.0 to 1.0) passed from the parent screen.
 */
@Composable
fun AnimatedSplashBackground(progress: Float) {
    // Definining blue tones for the 'water' or 'sky' movement effect
    val waterOverlay1 = Color(0xFF1E88E5).copy(alpha = 0.6f)
    val waterOverlay2 = Color(0xFF1565C0).copy(alpha = 0.7f)
    val waterOverlay3 = Color(0xFF0D47A1).copy(alpha = 0.8f)

    Box(modifier = Modifier.fillMaxSize()) {
        // High-resolution campus background from local assets
        AsyncImage(
            model = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.CenterEnd
        )

        // Custom drawing layer for the moving gradient effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // Linear brush that creates a seamless looping movement across the screen
                    val brush = Brush.linearGradient(
                        colors = listOf(waterOverlay1, waterOverlay2, waterOverlay3, waterOverlay2, waterOverlay1),
                        start = Offset(
                            x = -width + (width * 2 * progress),
                            y = -height + (height * 2 * progress)
                        ),
                        end = Offset(
                            x = width * 2 * progress,
                            y = height * 2 * progress
                        )
                    )
                    drawRect(brush)
                }
        )
    }
}

/**
 * Renders the primary animated logo with its associated visual effects.
 * 
 * Layers:
 * 1. Radial Glow: A color-shifting (rainbow) glow that pulses behind the logo.
 * 2. Branding Logo: The official institution logo tinted with shifting hues.
 */
@Composable
fun AnimatedSplashLogo(
    scale: Float,         // Initial pop-in scale
    alpha: Float,         // Initial fade-in alpha
    pulseScale: Float,    // Subtle continuous breathing scale
    shadeAlpha: Float,    // Background ambient light fade state
    rainbowColor: Color   // Current hue in the cycling color animation
) {
    Box(contentAlignment = Alignment.Center) {
        // OUTER GLOW: Draws a pulsing radial gradient circle using the current rainbow color
        Box(
            modifier = Modifier
                .size(350.dp)
                .scale(scale * pulseScale * 1.2f)
                .alpha(alpha * shadeAlpha)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(
                                rainbowColor.copy(alpha = 0.8f),
                                rainbowColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2
                        )
                    )
                }
        )

        // INSTITUTION LOGO: Uses a ColorFilter with Modulate blend mode to apply the shifting rainbow hue
        AsyncImage(
            model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
            contentDescription = "Glyndŵr University Logo",
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .scale(scale * pulseScale)
                .alpha(alpha),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(rainbowColor, BlendMode.Modulate)
        )
    }
}

/**
 * Footer component displaying data synchronization status and version info.
 * 
 * Provides visual feedback to the user while the app connects to the 
 * local database and seeds initial content.
 */
@Composable
fun SplashFooter(
    isLoadingData: Boolean, // State tracking database readiness
    alpha: Float            // Global entry fade status
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Text: Switches from 'Synchronizing' to 'Complete ✓'
        Text(
            text = if (isLoadingData) AppConstants.MSG_SYNC_DATABASE else AppConstants.MSG_SYNC_COMPLETE,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .alpha(alpha)
        )

        // White loading spinner synced with splash fade-in
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 4.dp,
            modifier = Modifier
                .size(40.dp)
                .alpha(alpha)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Static version label
        Text(
            text = "Version ${AppConstants.VERSION_NAME}",
            modifier = Modifier.alpha(alpha * 0.9f),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
