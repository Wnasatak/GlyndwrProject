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
import assignment1.krzysztofoko.s16001089.AppConstants
import java.util.Locale

/**
 * Standard Design Tokens for the Glyndŵr Pro system.
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
 * A professional Wallet Pill for the Navbar.
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
 * A professional Notification Icon with an animated ringing effect and badge.
 */
@Composable
fun ProNotificationIcon(
    count: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bellRing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        val bellColor = if (count > 0 && isDarkTheme) Color(0xFFFFEB3B)
        else if (count > 0) Color(0xFFFBC02D)
        else MaterialTheme.colorScheme.onPrimaryContainer

        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (count > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = bellColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { if (count > 0) { rotationZ = rotation } }
            )
        }
        if (count > 0) {
            Surface(
                color = Color(0xFFE53935),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .size(18.dp)
                    .offset(x = 4.dp, y = (-4).dp)
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
 * A professional Message/Chat Icon with unread badge.
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
                modifier = Modifier
                    .size(18.dp)
                    .offset(x = 4.dp, y = (-4).dp)
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
 * A professional, opaque container for all app-wide overflow menus.
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
 * A professional header for menu sections.
 */
@Composable
fun ProMenuHeader(title: String) {
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
 * A standard adaptive content wrapper.
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
    val hPadding = if (isTablet) 32.dp else padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxHeight()
                .then(if (isScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(start = hPadding, end = hPadding, top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

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

@Composable
fun EnrollmentStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (color, label, icon) = when (status) {
        "PENDING_REVIEW" -> Triple(Color(0xFFFBC02D), "PENDING", Icons.Default.PendingActions)
        "APPROVED" -> Triple(Color(0xFF4CAF50), "APPROVED", Icons.Default.CheckCircle)
        "REJECTED" -> Triple(Color(0xFFF44336), "DECLINED", Icons.Default.Error)
        "ENROLLED" -> Triple(Color(0xFF673AB7), "ENROLLED", Icons.Default.School)
        "PICKED_UP" -> Triple(Color(0xFF009688), "PICKED UP", Icons.Default.LibraryAddCheck)
        "FREE_COLLECTION" -> Triple(Color(0xFF03A9F4), "FREE COLLECTION", Icons.Default.Redeem)
        else -> Triple(Color.Gray, status, Icons.Default.PendingActions)
    }

    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.5f)), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(Modifier.width(6.6.dp))
            Text(text = label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ViewInvoiceButton(price: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
