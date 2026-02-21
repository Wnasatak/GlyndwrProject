package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.AppConstants
import java.util.Locale

/**
 * ProDesign Object
 *
 * A centralised repository for design tokens used frequently across the application's common components.
 * This ensures that core UI elements like menus and cards maintain a consistent and professional look.
 * Using a centralised object makes it easy to update the visual language of the app globally.
 */
object ProDesign {
    val MenuRadius = 16.dp
    val CardRadius = 24.dp
    val PillRadius = 32.dp
    val StandardPadding = 16.dp
    val CompactPadding = 12.dp
    val IconSize = 20.dp
    val MenuWidth = 220.dp
}

/**
 * LoadingOverlay Composable
 *
 * Provides a high-fidelity modal overlay that blocks interaction during background processing.
 * It features a subtle backdrop and a clean Material3 progress indicator with a branded message.
 *
 * @param isVisible Controls the visibility of the overlay.
 * @param label Text to display below the progress indicator (e.g., "Processing...").
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    label: String = "Processing..."
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.width(200.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * ProWalletPill Composable
 *
 * A compact and clickable UI element designed to display the user's current wallet balance.
 * It is typically placed in a top navigation bar or user profile area for quick access and visibility.
 * The pill format is modern and space-efficient.
 *
 * @param balance The user's account balance as a [Double]. This value is formatted to two decimal places.
 * @param onClick A lambda function to be executed when the user taps on the pill, usually to navigate to a wallet or top-up screen.
 * @param modifier A [Modifier] for custom styling and layout.
 */
@Composable
fun ProWalletPill(
    balance: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(6.6.dp))
            Text(
                text = "£${String.format(Locale.US, "%.2f", balance)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * ProNotificationIcon Composable
 *
 * A dynamic notification icon that provides rich visual feedback.
 * When there are unread notifications (`count > 0`), it displays a badge and activates a subtle, attention-grabbing
 * ringing animation. The colour also shifts to a more prominent yellow to indicate an active state.
 *
 * @param count The number of unread notifications. Determines the visibility and text of the badge.
 * @param isDarkTheme A boolean to adjust the active colour for better contrast in dark mode.
 * @param onClick The action to perform when the icon is clicked, typically opening the notifications screen.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ProNotificationIcon(
    count: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A looping animation that creates a "ringing" effect by rotating the bell icon back and forth.
    val infiniteTransition = rememberInfiniteTransition(label = "bellRing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotation"
    )

    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        // The bell's colour is state-dependent: yellow when active, default when idle.
        val bellColor = if (count > 0 && isDarkTheme) Color(0xFFFFEB3B)
        else if (count > 0) Color(0xFFFBC02D)
        else MaterialTheme.colorScheme.onPrimaryContainer

        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (count > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = bellColor,
                // The graphicsLayer modifier applies the rotation animation only when there are notifications.
                modifier = Modifier.size(24.dp).graphicsLayer { if (count > 0) { rotationZ = rotation } }
            )
        }
        // The badge is only composed into the UI tree if the count is positive.
        if (count > 0) {
            Surface(
                color = Color(0xFFE53935), // A distinct red for high visibility.
                shape = CircleShape,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.size(18.dp).offset(x = 4.dp, y = (-4).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        // Shows "!" for counts over 9 to maintain a clean look.
                        text = if (count > 9) "!" else count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black, lineHeight = 9.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * ProMessageIcon Composable
 *
 * An icon for accessing user messages or a chat feature. Similar to `ProNotificationIcon`,
 * it displays a badge to indicate the number of unread messages, creating a consistent
 * notification pattern across the app's navigation.
 *
 * @param count The number of unread messages.
 * @param isDarkTheme Flag to adjust the active icon colour for the current theme.
 * @param onClick Lambda to be executed on icon click, usually to navigate to the messages screen.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ProMessageIcon(
    count: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        val chatColor = if (count > 0 && isDarkTheme) Color(0xFFFFEB3B)
        else if (count > 0) Color(0xFFFBC02D)
        else MaterialTheme.colorScheme.onPrimaryContainer

        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Chat,
                contentDescription = "Messages",
                tint = chatColor,
                modifier = Modifier.size(22.dp)
            )
        }
        if (count > 0) {
            Surface(
                color = Color(0xFFE53935),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.size(18.dp).offset(x = 4.dp, y = (-4).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (count > 9) "!" else count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black, lineHeight = 9.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * ProMenuContainer Composable
 *
 * A styled container for building dropdown or overflow menus. It enforces a consistent width, shape,
 * and background colour, serving as a foundational block for creating menus that feel native to the app.
 *
 * @param modifier A modifier to be applied to the container Column.
 * @param content The composable content of the menu, typically `DropdownMenuItem`s or custom rows.
 */
@Composable
fun ProMenuContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .width(ProDesign.MenuWidth)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(ProDesign.MenuRadius)),
        content = content
    )
}

/**
 * ProMenuHeader Composable
 *
 * A header used within a `ProMenuContainer` to create labelled sections in a menu.
 * It displays a capitalised title and a horizontal divider for clear visual separation.
 *
 * @param title The text to be displayed as the section header.
 */
@Composable
fun ProMenuHeader(title: String) {
    @Suppress("DEPRECATION")
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(horizontal = ProDesign.StandardPadding, vertical = ProDesign.CompactPadding)
    )
    HorizontalDivider(
        modifier = Modifier.padding(bottom = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

/**
 * AdaptiveContent Composable
 *
 * A powerful and reusable layout wrapper that intelligently adapts its content to different screen sizes.
 * On larger screens (tablets), it constrains the content to a maximum width for better readability.
 * On smaller screens (phones), it allows the content to fill the width. It also provides optional
 * scrolling behaviour.
 *
 * @param modifier The modifier for this composable.
 * @param maxWidth The maximum width the content should occupy on large screens.
 * @param padding The padding to be applied within the content area.
 * @param isScrollable If true, the content will be placed in a vertically scrolling column.
 * @param content The main body of content to be displayed within this adaptive container.
 */
@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 600.dp,
    padding: PaddingValues = PaddingValues(horizontal = ProDesign.StandardPadding, vertical = ProDesign.StandardPadding),
    isScrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    // Use more generous padding on tablets for a less cramped layout.
    val hPadding = if (isTablet) 32.dp else padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)

    // The outer Box centres the content column on larger screens.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth) // This is the core of the adaptive behaviour.
                .fillMaxHeight()
                .then(if (isScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(start = hPadding, end = hPadding, top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/**
 * InfoCard Composable
 *
 * A generic and versatile card component for displaying a key piece of information, 
 * consisting of a leading icon, a title, and a content string. It's ideal for use in dashboards,
 * settings screens, or any context where a piece of data needs to be highlighted.
 *
 * @param icon The leading icon that gives a visual clue about the content.
 * @param title A short title or label for the information.
 * @param content The main informational text.
 * @param modifier The modifier for the Card.
 * @param containerColor The background colour of the card.
 * @param contentStyle The text style for the main content string.
 * @param border An optional border to apply to the card.
 */
@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
    contentStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    border: BorderStroke? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = border
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(ProDesign.StandardPadding))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(text = content, style = contentStyle)
            }
        }
    }
}

/**
 * EnrollmentStatusBadge Composable
 *
 * A specialised badge that provides a clear, colour-coded, and icon-driven representation of an
 * item's status (e.g., an application's approval state). It maps a raw status string to a
 * visually distinct combination of colour, label, and icon.
 *
 * @param status The raw status string (e.g., \"PENDING_REVIEW\", \"APPROVED\").
 * @param modifier The modifier for this composable.
 */
@Composable
fun EnrollmentStatusBadge(status: String, modifier: Modifier = Modifier) {
    // The `when` block is a state machine that translates a string into a complete visual theme.
    val (color, label, icon) = when (status) {
        "PENDING_REVIEW" -> Triple(Color(0xFFFBC02D), "PENDING", Icons.Default.PendingActions)
        "APPROVED" -> Triple(Color(0xFF4CAF50), "PAID", Icons.Default.CheckCircle)
        "REJECTED" -> Triple(Color(0xFFF44336), "DECLINED", Icons.Default.Error)
        "ENROLLED" -> Triple(Color(0xFF673AB7), "ENROLLED", Icons.Default.School)
        "IN_LIBRARY" -> Triple(Color(0xFF673AB7), "IN LIBRARY", Icons.Default.LibraryBooks)
        "PICKED_UP" -> Triple(Color(0xFF009688), "PICKED UP", Icons.Default.LibraryAddCheck)
        "FREE_COLLECTION" -> Triple(Color(0xFF03A9F4), "FREE COLLECTION", Icons.Default.Redeem)
        else -> Triple(Color.Gray, status, Icons.Default.PendingActions) // Fallback for unknown statuses.
    }

    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.5f)), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(Modifier.width(6.6.dp))
            Text(text = label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * ViewInvoiceButton Composable
 *
 * A prominent button for viewing a financial invoice or receipt. A key feature is that this button
 * will only be composed into the UI if the associated `price` is greater than zero, preventing
 * users from seeing an unnecessary button for free items.
 *
 * @param price The price of the associated item. The button is only shown if this is > 0.
 * @param onClick The action to perform when clicked, typically navigating to an invoice detail screen.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ViewInvoiceButton(price: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Conditional rendering: The button only exists in the composition tree if there is a price.
    if (price > 0) {
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(ProDesign.MenuRadius),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null)
            Spacer(Modifier.width(12.dp))
            Text(AppConstants.BTN_VIEW_INVOICE, fontWeight = FontWeight.Bold)
        }
    }
}
