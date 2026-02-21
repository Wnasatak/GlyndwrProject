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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * AdaptiveUtils.kt
 *
 * Centralized design tokens and utility components for the Glyndŵr system.
 * This file handles responsive design logic, ensuring the UI adapts gracefully
 * between mobile and tablet form factors.
 */

/**
 * Adaptive typography system that provides scale-aware TextStyles.
 */
object AdaptiveTypography {
    /** Main display titles for headers. Scaled up for tablets. */
    @Composable
    fun display(): TextStyle = if (isTablet()) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium

    /** Headline style for prominent section titles. */
    @Composable
    fun headline(): TextStyle = if (isTablet()) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium

    /** Sub-titles or section headers within cards. */
    @Composable
    fun sectionHeader(): TextStyle = if (isTablet()) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge

    /** Standard body text for primary content. */
    @Composable
    fun body(): TextStyle = if (isTablet()) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium

    /** Smaller body text or captions for secondary information. */
    @Composable
    fun caption(): TextStyle = if (isTablet()) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall

    /** Label text for UI markers and button text. */
    @Composable
    fun label(): TextStyle = if (isTablet()) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall

    /** Hint or secondary metadata text, optimized for small spaces. */
    @Composable
    fun hint(): TextStyle = if (isTablet()) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
}

/**
 * Centralized spacing tokens to maintain consistent margins and padding across the app.
 */
object AdaptiveSpacing {
    /** Standard large spacing between major UI sections. */
    @Composable
    fun large(): Dp = if (isTablet()) 48.dp else 32.dp

    /** Standard medium spacing for general layout gaps. */
    @Composable
    fun medium(): Dp = if (isTablet()) 32.dp else 24.dp

    /** Small internal spacing for grouping related elements. */
    @Composable
    fun small(): Dp = if (isTablet()) 16.dp else 12.dp

    /** Extra small gaps for tight UI components or tiny margins. */
    @Composable
    fun extraSmall(): Dp = if (isTablet()) 12.dp else 8.dp

    /** Content padding for screen edges to prevent content from touching the bezel. */
    @Composable
    fun contentPadding(): Dp = if (isTablet()) 32.dp else 16.dp

    /** Corner radius for major cards and surfaces. */
    @Composable
    fun cornerRadius(): Dp = if (isTablet()) 32.dp else 24.dp

    /** Corner radius for smaller buttons, pills, and sub-surfaces. */
    @Composable
    fun itemRadius(): Dp = if (isTablet()) 16.dp else 12.dp
    
    /** Padding used specifically inside dialogs for consistent content breathing room. */
    @Composable
    fun dialogPadding(): Dp = if (isTablet()) 32.dp else 24.dp
}

/**
 * Fixed dimension tokens for specific UI elements.
 */
object AdaptiveDimensions {
    val LoadingDialogSize = 200.dp
    val LoadingIndicatorSize = 60.dp
    val LargeIconSize = 64.dp
    val MediumIconSize = 32.dp
    val SmallIconSize = 24.dp
    val StandardButtonHeight = 56.dp
    
    // Avatar tokens
    val LargeAvatar = 64.dp
    val MediumAvatar = 44.dp
    val SmallAvatar = 32.dp
}

/**
 * Maximum width tokens for restricting content width on large screens.
 */
object AdaptiveWidths {
    val Standard = 600.dp
    val Medium = 700.dp
    val Wide = 850.dp
    val HeroImage = 500.dp
    val ActionButton = 400.dp
}

/**
 * Checks if the current screen configuration qualifies as a tablet.
 * @return true if screen width is 600dp or greater.
 */
@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

/**
 * A responsive header for dashboard sections.
 *
 * @param title Primary header text.
 * @param subtitle Secondary descriptive text.
 * @param icon Leading icon for the header.
 * @param modifier Modifier for the container.
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
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
            modifier = Modifier.size(if (isTablet) 56.dp else 44.dp)
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
        Spacer(Modifier.width(if (isTablet) 24.dp else 12.dp))
        Column {
            Text(
                text = title,
                style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * A card component that adapts its padding and shape based on the screen size.
 *
 * @param modifier Modifier for the card.
 * @param onClick Optional click listener.
 * @param backgroundColor Background color of the card.
 * @param content The composable content to be displayed inside the card.
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
 * Displays a branded logo with a subtle pulsing glow animation.
 *
 * @param model The data model for the logo (URL, Resource, etc.).
 * @param contentDescription Accessibility description.
 * @param modifier Modifier for the logo container.
 * @param logoSize The size of the logo.
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
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

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
 * Constrains the width of a hero section on tablets while remaining full-width on mobile.
 */
@Composable
fun Modifier.adaptiveHeroWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.HeroImage) else this.fillMaxWidth()
}

/**
 * Constrains the width of an action button on tablets while remaining full-width on mobile.
 */
@Composable
fun Modifier.adaptiveButtonWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.ActionButton) else this.fillMaxWidth()
}

/**
 * Constrains the width of a component to a maximum value on tablets.
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
 * A top-level container that manages content centering and maximum width for large screens.
 *
 * @param modifier Modifier for the root container.
 * @param maxWidth The maximum width the content should occupy on large screens.
 * @param contentAlignment How the content should be aligned within the container.
 * @param content The screen content.
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
            modifier = Modifier.adaptiveWidth(maxWidth).fillMaxHeight(),
            contentAlignment = contentAlignment
        ) {
            content(isTablet)
        }
    }
}
