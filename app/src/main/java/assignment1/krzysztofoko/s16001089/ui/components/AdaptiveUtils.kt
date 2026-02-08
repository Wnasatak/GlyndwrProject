package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Standard width constraints for different types of screens on tablets.
 */
object AdaptiveWidths {
    val Standard = 600.dp   // For Auth, Profile, Small forms
    val Medium = 700.dp     // For Detail screens (Books, Courses)
    val Wide = 850.dp       // For Grid screens (Home, Dashboard)
    
    // Internal component widths
    val HeroImage = 500.dp
    val ActionButton = 400.dp
    
    // Dashboard specific squeeze
    val DashboardCompact = 480.dp
    val DashboardUltraCompact = 420.dp // New narrower width for dashboard list items
}

/**
 * Standard adaptive spacing and dimensions.
 */
object AdaptiveSpacing {
    @Composable
    fun medium(): Dp = if (isTablet()) 32.dp else 24.dp
    
    @Composable
    fun contentPadding(): Dp = if (isTablet()) 32.dp else 20.dp
    
    @Composable
    fun cornerRadius(): Dp = if (isTablet()) 32.dp else 24.dp
}

/**
 * Global utility to check if the current screen is a tablet.
 */
@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

/**
 * Branded University Logo with a centered soft shade/glow effect behind it.
 * This effect is consistent across both phone and tablet devices.
 */
@Composable
fun AdaptiveBrandedLogo(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp
) {
    val infiniteTransition = rememberInfiniteTransition("logo_pulse")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f, 
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = modifier.size(logoSize),
        contentAlignment = Alignment.Center
    ) {
        // Centered radial gradient shade behind the logo for all devices
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.minDimension / 1.4f) * glowScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFBB86FC).copy(alpha = 0.4f * glowAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

/**
 * Applies a centered "Hero" width (e.g., for product images) on tablets.
 */
@Composable
fun Modifier.adaptiveHeroWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.HeroImage) else this.fillMaxWidth()
}

/**
 * Applies a centered "Action" width (e.g., for big buttons) on tablets.
 */
@Composable
fun Modifier.adaptiveButtonWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.ActionButton) else this.fillMaxWidth()
}

/**
 * A shared modifier that applies tablet-specific width constraints.
 */
@Composable
fun Modifier.adaptiveWidth(maxWidth: Dp = AdaptiveWidths.Standard): Modifier {
    return if (isTablet()) {
        this.widthIn(max = maxWidth).fillMaxHeight()
    } else {
        this.fillMaxWidth()
    }
}

/**
 * A reusable wrapper that centers content on tablets and caps its width.
 */
@Composable
fun AdaptiveScreenContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = AdaptiveWidths.Standard, 
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.(isTablet: Boolean) -> Unit
) {
    val isTablet = isTablet()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
    ) {
        Box(
            modifier = Modifier.adaptiveWidth(maxWidth),
            contentAlignment = contentAlignment
        ) {
            content(isTablet)
        }
    }
}
