package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * AdaptiveUtils.kt
 *
 * This utility file provides a centralized set of constants and composables to handle
 * responsive design across different form factors (Phone vs. Tablet). It ensures a
 * consistent user experience by capping widths and adjusting spacing dynamically.
 */

/**
 * Defines standard width constraints for various UI elements when displayed on larger screens.
 * These values help prevent layouts from stretching too thin on tablets.
 */
object AdaptiveWidths {
    val Standard = 600.dp   // Default container width for most screens
    val Medium = 700.dp     // Slightly wider container for content-heavy pages
    val Wide = 850.dp       // Maximum width for very wide layouts

    val HeroImage = 500.dp  // Ideal width for featured product images
    val ActionButton = 400.dp // Maximum width for primary call-to-action buttons

    val DashboardCompact = 480.dp // used in future
    val DashboardUltraCompact = 420.dp
}

/**
 * Provides dynamic spacing and dimension values that automatically scale based on the device type.
 */
object AdaptiveSpacing {
    /** Returns 32dp for tablets and 24dp for phones. */
    @Composable
    fun medium(): Dp = if (isTablet()) 32.dp else 24.dp

    /** Returns standard outer padding: 32dp for tablets, 20dp for phones. */
    @Composable
    fun contentPadding(): Dp = if (isTablet()) 32.dp else 20.dp

    /** Returns standard corner radius: 32dp for tablets, 24dp for phones. */
    @Composable
    fun cornerRadius(): Dp = if (isTablet()) 32.dp else 24.dp
}

/**
 * A global utility to determine if the device should be treated as a tablet.
 * Based on the Material Design 600dp breakpoint.
 */
@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

/**
 * A standardized header component for Dashboard-style pages.
 * It automatically scales icons, spacing, and typography for mobile or tablet views.
 *
 * @param title The primary heading text.
 * @param subtitle The secondary descriptive text.
 * @param icon The ImageVector icon to display next to the text.
 */
@Composable
fun AdaptiveDashboardHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val isTablet = isTablet()
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container: Scales based on device type
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
            modifier = Modifier.size(if (isTablet) 56.dp else 48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)
                )
            }
        }
        Spacer(Modifier.width(if (isTablet) 24.dp else 16.dp))
        // Text Container: Adjusts typography styles
        Column {
            Text(
                text = title,
                style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * A responsive card component for dashboard items.
 * It manages adaptive corner radii, internal padding, and optional click behavior.
 *
 * @param modifier Custom modifier for the card.
 * @param onClick Optional lambda for click interaction.
 * @param backgroundColor Overridable surface color.
 * @param content Slot for the card's body, providing the 'isTablet' flag for internal logic.
 */
@Composable
fun AdaptiveDashboardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    content: @Composable ColumnScope.(isTablet: Boolean) -> Unit
) {
    val isTablet = isTablet()
    val shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius())

    // Apply clickable modifier only if a callback is provided to maintain clean UI state
    val cardModifier = if (onClick != null) {
        modifier.fillMaxWidth().clip(shape).clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), 
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(if (isTablet) 24.dp else 16.dp)
        ) {
            content(isTablet)
        }
    }
}

/**
 * A high-end branded logo component featuring an animated "glow" backflash.
 * The glow effect uses infinite transitions to pulsate and scale, creating a premium feel.
 *
 * @param model The image source (URL, Resource, etc.) for the Coil AsyncImage.
 * @param contentDescription Accessibility text.
 * @param logoSize The diameter of the circular logo.
 */
@Composable
fun AdaptiveBrandedLogo(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp
) {
    val infiniteTransition = rememberInfiniteTransition("logo_pulse")

    // Pumping alpha animation for the glow
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Scaling animation for the glow expansion
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.size(logoSize),
        contentAlignment = Alignment.Center
    ) {
        // BACKFLASH: Dynamic Canvas drawing for the radial gradient glow effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.minDimension / 1.3f) * glowScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.6f * glowAlpha),
                        secondaryColor.copy(alpha = 0.2f * glowAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // The actual logo image, clipped to a circle with a subtle primary border
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(1.dp, primaryColor.copy(alpha = 0.2f), CircleShape)
        )
    }
}

/**
 * MODIFIER: Caps the width of hero elements on tablets to prevent them from becoming too large.
 */
@Composable
fun Modifier.adaptiveHeroWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.HeroImage) else this.fillMaxWidth()
}

/**
 * MODIFIER: Caps button width on tablets for better visual balance in large layouts.
 */
@Composable
fun Modifier.adaptiveButtonWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.ActionButton) else this.fillMaxWidth()
}

/**
 * MODIFIER: Applies a custom or standard max width constraint only when running on a tablet.
 */
@Composable
fun Modifier.adaptiveWidth(maxWidth: Dp = AdaptiveWidths.Standard): Modifier {
    return if (isTablet()) {
        this.widthIn(max = maxWidth)
    } else {
        this.fillMaxWidth()
    }
}

/**
 * SCREEN CONTAINER: A top-level wrapper that centers content and applies width constraints on tablets.
 * This is the primary tool for creating responsive screens that look good on both mobile and large devices.
 *
 * @param maxWidth The maximum width the content should take on tablets.
 * @param contentAlignment Alignment of the content within the container.
 * @param content Slot for the screen content.
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
        // Center content on tablets, align to start on phones
        contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
    ) {
        Box(
            modifier = Modifier.adaptiveWidth(maxWidth).fillMaxHeight(),
            contentAlignment = contentAlignment
        ) {
            content(isTablet)
        }
    }
}
