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
 * Centralized design tokens for the Glyndŵr Pro system.
 * Handles responsive typography, spacing, and standard component dimensions.
 */

object AdaptiveTypography {
    /** Main display titles for headers. */
    @Composable
    fun display(): TextStyle = if (isTablet()) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium

    /** Headline style for prominent section titles. */
    @Composable
    fun headline(): TextStyle = if (isTablet()) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium

    /** Sub-titles or section headers within cards. */
    @Composable
    fun sectionHeader(): TextStyle = if (isTablet()) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge

    /** Standard body text. */
    @Composable
    fun body(): TextStyle = if (isTablet()) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium

    /** Smaller body text or captions. */
    @Composable
    fun caption(): TextStyle = if (isTablet()) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall

    /** Label text for UI markers. */
    @Composable
    fun label(): TextStyle = if (isTablet()) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall

    /** Hint or secondary metadata text. */
    @Composable
    fun hint(): TextStyle = if (isTablet()) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
}

object AdaptiveSpacing {
    /** Standard large spacing. */
    @Composable
    fun large(): Dp = if (isTablet()) 48.dp else 32.dp

    /** Standard medium spacing. */
    @Composable
    fun medium(): Dp = if (isTablet()) 32.dp else 24.dp

    /** Small internal spacing. */
    @Composable
    fun small(): Dp = if (isTablet()) 16.dp else 12.dp

    /** Extra small gaps for tight UI. */
    @Composable
    fun extraSmall(): Dp = if (isTablet()) 12.dp else 8.dp

    /** Content padding for screen edges. */
    @Composable
    fun contentPadding(): Dp = if (isTablet()) 32.dp else 16.dp

    /** Corner radius for major cards. */
    @Composable
    fun cornerRadius(): Dp = if (isTablet()) 32.dp else 24.dp

    /** Corner radius for smaller buttons, pills, and sub-surfaces. */
    @Composable
    fun itemRadius(): Dp = if (isTablet()) 16.dp else 12.dp
    
    /** Padding used specifically inside dialogs. */
    @Composable
    fun dialogPadding(): Dp = if (isTablet()) 32.dp else 24.dp
}

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

object AdaptiveWidths {
    val Standard = 600.dp
    val Medium = 700.dp
    val Wide = 850.dp
    val HeroImage = 500.dp
    val ActionButton = 400.dp
}

@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

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

@Composable
fun Modifier.adaptiveHeroWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.HeroImage) else this.fillMaxWidth()
}

@Composable
fun Modifier.adaptiveButtonWidth(): Modifier {
    return if (isTablet()) this.widthIn(max = AdaptiveWidths.ActionButton) else this.fillMaxWidth()
}

@Composable
fun Modifier.adaptiveWidth(maxWidth: Dp = AdaptiveWidths.Standard): Modifier {
    return if (isTablet()) {
        this.widthIn(max = maxWidth)
    } else {
        this.fillMaxWidth()
    }
}

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
