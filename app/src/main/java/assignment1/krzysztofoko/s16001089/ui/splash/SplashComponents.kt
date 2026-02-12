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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import coil.compose.AsyncImage

/**
 * SplashComponents.kt
 *
 * This file contains the modular UI elements used to build the SplashScreen.
 * Each component is highly optimized for performance (using drawBehind for animations)
 * and responsiveness (supporting dynamic sizing for phone vs. tablet form factors).
 */

/**
 * Renders an animated background layer.
 * It combines a high-quality campus asset with a dynamically shifting linear gradient.
 * 
 * @param progress A float (0..1) representing the current state of the infinite animation loop,
 *                 used to shift the gradient's start and end offsets.
 */
@Composable
fun AnimatedSplashBackground(progress: Float) {
    // Institutional blue color palette for the overlay
    val waterOverlay1 = Color(0xFF1E88E5).copy(alpha = 0.6f)
    val waterOverlay2 = Color(0xFF1565C0).copy(alpha = 0.7f)
    val waterOverlay3 = Color(0xFF0D47A1).copy(alpha = 0.8f)

    Box(modifier = Modifier.fillMaxSize()) {
        // STATIC LAYER: Official University Campus imagery
        AsyncImage(
            model = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.CenterEnd
        )

        // ANIMATED LAYER: Moving gradient overlay to add depth and motion
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // Calculate a shifting linear gradient based on the progress parameter
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
 * Renders the central University logo with a sophisticated multi-layered glow effect.
 * The component uses adaptive logic to ensure the logo is perfectly legible on 
 * devices ranging from small handsets to 12-inch tablets.
 *
 * @param scale The entry transition scale factor.
 * @param alpha The entry transition opacity.
 * @param pulseScale The ongoing "breathing" animation factor.
 * @param shadeAlpha The cinematic fade-out factor for the initial shade.
 * @param rainbowColor The dynamically shifting color used for the logo's ambient glow.
 */
@Composable
fun AnimatedSplashLogo(
    scale: Float,         
    alpha: Float,         
    pulseScale: Float,    
    shadeAlpha: Float,    
    rainbowColor: Color   
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        // RESPONSIVE SIZING: Caps logo width on tablets to maintain visual balance
        val logoWidth = if (isTablet) {
            when {
                screenWidth > 1000.dp -> 520.dp 
                screenWidth > 800.dp -> 480.dp 
                else -> 420.dp    
            }.coerceAtMost(screenHeight * 0.4f) // Ensure it never exceeds 40% of screen height
        } else {
            screenWidth * 0.85f
        }

        // AMBIENT GLOW: A radial gradient that pulses and follows the rainbow color shift
        val glowSize = if (isTablet) logoWidth * 1.3f else 350.dp

        Box(
            modifier = Modifier
                .size(glowSize)
                .scale(scale * pulseScale * (if (isTablet) 1.05f else 1.2f))
                .alpha(alpha * shadeAlpha)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(
                                rainbowColor.copy(alpha = 0.7f),
                                rainbowColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2
                        )
                    )
                }
        )

        // INSTITUTION LOGO: The primary branding asset
        AsyncImage(
            model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
            contentDescription = "Glyndŵr University Logo",
            modifier = Modifier
                .width(logoWidth)
                .scale(scale * pulseScale)
                .alpha(alpha),
            contentScale = ContentScale.Fit,
            // BLEND MODE: Modulate ensures the logo integrates naturally with the shifting rainbow glow
            colorFilter = ColorFilter.tint(rainbowColor.copy(alpha = 0.8f), BlendMode.Modulate)
        )
    }
}

/**
 * A footer component that informs the user about the system's initialization status.
 * It provides clear feedback during long-running data synchronization tasks.
 *
 * @param isLoadingData Boolean flag indicating if backend sync is still in progress.
 * @param alpha Transition alpha for the entire footer section.
 */
@Composable
fun SplashFooter(
    isLoadingData: Boolean, 
    alpha: Float            
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isTablet) 40.dp else 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // DYNAMIC STATUS MESSAGE: Updates reactively based on synchronization state
        Text(
            text = if (isLoadingData) AppConstants.MSG_SYNC_DATABASE else AppConstants.MSG_SYNC_COMPLETE,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .alpha(alpha)
        )

        // PROGRESS INDICATOR: Provides a spinning visual cue during database sync
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 4.dp,
            modifier = Modifier
                .size(if (isTablet) 28.dp else 40.dp)
                .alpha(alpha)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // VERSION INFO: Displays current build information centralized in AppConstants
        Text(
            text = "Version ${AppConstants.VERSION_NAME}",
            modifier = Modifier.alpha(alpha * 0.9f),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
