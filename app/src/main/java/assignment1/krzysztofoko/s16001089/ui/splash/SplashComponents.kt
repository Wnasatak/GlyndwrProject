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
 * Visual components for the SplashScreen.
 */

/**
 * Renders an animated background using a linear gradient overlay on top of a campus image.
 */
@Composable
fun AnimatedSplashBackground(progress: Float) {
    val waterOverlay1 = Color(0xFF1E88E5).copy(alpha = 0.6f)
    val waterOverlay2 = Color(0xFF1565C0).copy(alpha = 0.7f)
    val waterOverlay3 = Color(0xFF0D47A1).copy(alpha = 0.8f)

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.CenterEnd
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val width = size.width
                    val height = size.height

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
 * Renders the primary animated logo.
 * Optimized size for tablets to ensure it fits and remains centered.
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
        
        // Large but safe logo width for tablets
        val logoWidth = if (isTablet) {
            when {
                screenWidth > 1000.dp -> 520.dp 
                screenWidth > 800.dp -> 480.dp 
                else -> 420.dp    
            }.coerceAtMost(screenHeight * 0.4f) 
        } else {
            screenWidth * 0.85f
        }

        // Reduced glow multiplier to prevent pushing other elements
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

        // INSTITUTION LOGO
        AsyncImage(
            model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
            contentDescription = "Glyndŵr University Logo",
            modifier = Modifier
                .width(logoWidth)
                .scale(scale * pulseScale)
                .alpha(alpha),
            contentScale = ContentScale.Fit,
            // Modulate filter for professional integrated look
            colorFilter = ColorFilter.tint(rainbowColor.copy(alpha = 0.8f), BlendMode.Modulate)
        )
    }
}

/**
 * Footer component displaying data synchronization status and version info.
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
        Text(
            text = if (isLoadingData) AppConstants.MSG_SYNC_DATABASE else AppConstants.MSG_SYNC_COMPLETE,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .alpha(alpha)
        )

        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 4.dp,
            modifier = Modifier
                .size(if (isTablet) 28.dp else 40.dp)
                .alpha(alpha)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Version ${AppConstants.VERSION_NAME}",
            modifier = Modifier.alpha(alpha * 0.9f),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
