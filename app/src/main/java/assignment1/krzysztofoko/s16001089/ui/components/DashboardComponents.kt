package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.WalletTransaction
import assignment1.krzysztofoko.s16001089.ui.theme.CyanAccent
import assignment1.krzysztofoko.s16001089.ui.theme.PinkAccent
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * DashboardComponents.kt
 *
 * A collection of bespoke UI elements designed specifically for the Dashboard.
 * These components focus on providing a high-quality, interactive, and glanceable 
 * overview of the user's account, including their wallet, recent activity, and library.
 */

/**
 * SpinningAudioButton Composable
 *
 * A custom, animated button for controlling audio playback.
 * It features a sweep gradient arc that rotates around the button while audio is playing,
 * providing a dynamic and high-end visual indicator of the playback state.
 *
 * @param isPlaying Boolean flag indicating if the audio is currently playing.
 * @param onToggle Callback to toggle between play and pause states.
 * @param modifier Custom styling for the button container.
 * @param size The diameter of the button in dp.
 */
@Composable
fun SpinningAudioButton(
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    // Animates the rotation speed smoothly when starting or stopping.
    val rotationSpeed by animateFloatAsState(
        targetValue = if (isPlaying) 3000f else 0f, 
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing), 
        label = "rotationSpeed"
    )
    // The continuous rotation animation itself.
    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f, 
        targetValue = 360f, 
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart), 
        label = "baseRotation"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw the rotating gradient arc only when playing or during the transition.
            if (isPlaying || rotationSpeed > 0) {
                rotate(baseRotation) {
                    drawArc(
                        brush = Brush.sweepGradient(colors = listOf(CyanAccent, PinkAccent, CyanAccent)), 
                        startAngle = 0f, 
                        sweepAngle = 360f, 
                        useCenter = false, 
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            } else {
                // Draw a subtle placeholder ring when idle.
                drawCircle(color = Color.Gray.copy(alpha = 0.2f), style = Stroke(width = 1.dp.toPx()))
            }
        }
        // The central interactive button.
        FilledIconButton(
            onClick = onToggle, 
            modifier = Modifier.size((size * 0.85).dp), 
            shape = CircleShape, 
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
        ) {
            AnimatedContent(targetState = isPlaying, label = "iconTransition") { playing ->
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, 
                    contentDescription = if (playing) "Pause" else "Play", 
                    tint = Color.White, 
                    modifier = Modifier.size((size * 0.5).dp)
                )
            }
        }
    }
}

/**
 * DashboardHeader Composable
 *
 * The primary visual anchor of the dashboard. It displays the user's profile information, 
 * their role, and a prominent card showing their current wallet balance with a top-up action.
 *
 * @param name User's display name.
 * @param photoUrl Optional URL for the user's profile picture.
 * @param role The user's role (e.g., Student, Tutor, Admin).
 * @param balance The current account balance.
 * @param onProfileClick Callback to navigate to the full profile screen.
 * @param onTopUp Callback to launch the wallet top-up flow.
 * @param onViewHistory Callback to show the wallet transaction history.
 */
@Composable
fun DashboardHeader(
    name: String,
    photoUrl: String?,
    role: String,
    balance: Double,
    onProfileClick: () -> Unit,
    onTopUp: () -> Unit,
    onViewHistory: () -> Unit
) {
    val radius = AdaptiveSpacing.cornerRadius()
    val padding = AdaptiveSpacing.contentPadding()
    val innerPadding = AdaptiveSpacing.medium()
    val avatarSize = AdaptiveDimensions.LargeAvatar

    Card(
        modifier = Modifier.fillMaxWidth().padding(padding),
        shape = RoundedCornerShape(radius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(innerPadding)) {
            // User Identification Area
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfileClick
                )
            ) {
                UserAvatar(
                    photoUrl = photoUrl,
                    modifier = Modifier.size(avatarSize).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape).padding(2.dp),
                    isLarge = true
                )
                Spacer(Modifier.width(innerPadding))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = AppConstants.TEXT_WELCOME, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    // Role Badge
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Text(text = role.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))

            // Financial Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(AppConstants.TEXT_ACCOUNT_BALANCE, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            // Transaction History trigger
                            Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp).clickable { onViewHistory() }.padding(2.dp))
                        }
                        Text(text = "£" + String.format(Locale.US, "%.2f", balance), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }

                    // Direct Top-Up Action
                    Button(
                        onClick = onTopUp,
                        shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        @Suppress("DEPRECATION")
                        Text(AppConstants.BTN_TOP_UP, style = AdaptiveTypography.label(), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

/**
 * WalletHistorySheet Composable
 *
 * A modal bottom sheet that displays a scrollable list of recent wallet transactions.
 * Uses a `LazyColumn` for high performance and provides deep-linking to product details and invoices.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletHistorySheet(transactions: List<WalletTransaction>, onNavigateToProduct: (String) -> Unit, onViewInvoice: (String, String?) -> Unit, onDismiss: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text(text = "Wallet History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 24.dp))
            if (transactions.isEmpty()) { 
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No transactions yet.", color = Color.Gray) } 
            } else { 
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxHeight(0.6f)) { 
                    items(transactions, key = { it.id }) { tx -> 
                        WalletTransactionItem(tx = tx, sdf = sdf, onNavigateToProduct = onNavigateToProduct, onViewInvoice = onViewInvoice, onDismissSheet = onDismiss) 
                    } 
                } 
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { 
                @Suppress("DEPRECATION") 
                Text("Close", style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.Bold) 
            }
        }
    }
}

/**
 * WalletTransactionItem Composable
 *
 * Renders a single transaction row within the history sheet.
 * It intelligently displays different icons and colours based on the transaction type (Top-Up vs Purchase).
 */
@Composable
private fun WalletTransactionItem(tx: WalletTransaction, sdf: SimpleDateFormat, onNavigateToProduct: (String) -> Unit, onViewInvoice: (String, String?) -> Unit, onDismissSheet: () -> Unit) {
    val isTopUp = tx.type == "TOP_UP"
    val isPurchase = tx.type == "PURCHASE"
    var showItemMenu by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        // Icon reflecting transaction direction
        Box(modifier = Modifier.size(40.dp).background(if (isTopUp) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFE91E63).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { 
            Icon(imageVector = if (isTopUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = if (isTopUp) Color(0xFF4CAF50) else Color(0xFFE91E63), modifier = Modifier.size(20.dp)) 
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) { 
            Text(tx.description, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sdf.format(Date(tx.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray) 
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = (if (isTopUp) "+" else "-") + "£${String.format(Locale.US, "%.2f", tx.amount)}", fontWeight = FontWeight.Black, color = if (isTopUp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 8.dp))
            // Context menu for linked items (Products/Invoices)
            if (tx.productId != null) {
                Box {
                    IconButton(onClick = { showItemMenu = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.MoreVert, "Options", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                    DropdownMenu(expanded = showItemMenu, onDismissRequest = { showItemMenu = false }) {
                        if (isPurchase) { DropdownMenuItem(text = { Text("Product Details") }, onClick = { showItemMenu = false; onNavigateToProduct(tx.productId); onDismissSheet() }, leadingIcon = { Icon(Icons.Default.Info, null) }) }
                        DropdownMenuItem(text = { Text("View Invoice") }, onClick = { showItemMenu = false; onViewInvoice(tx.productId, tx.orderReference); onDismissSheet() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) })
                    }
                }
            }
        }
    }
}

/**
 * GrowingLazyRow Composable
 *
 * A highly interactive, horizontally-scrolling list of course or book mini-cards.
 * On mobile devices, it implements a "Cover Flow" effect where items at the centre of the screen
 * are scaled up, creating a visually rich and focused experience. On tablets, it falls back
 * to a standard, clean list layout.
 */
@Composable
fun GrowingLazyRow(books: List<Book>, icon: ImageVector, onBookClick: (Book) -> Unit) {
    if (books.isEmpty()) return
    val isTablet = isTablet()
    val listState = rememberLazyListState()
    
    if (isTablet) { 
        // Standard Row for Tablets
        LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { 
            items(books, key = { it.id }) { book -> 
                WishlistMiniCard(book = book, icon = icon, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)) { onBookClick(book) } 
            } 
        } 
    } else { 
        // "Cover Flow" Row for Mobile
        val itemsToShow = remember(books) { List(100) { books }.flatten() } // Infinite-like scroll mock
        val initialIndex = itemsToShow.size / 2 - (itemsToShow.size / 2 % books.size)
        LaunchedEffect(books) { listState.scrollToItem(initialIndex) }
        
        LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { 
            itemsIndexed(itemsToShow) { index, book -> 
                Box(modifier = Modifier.graphicsLayer { 
                    val layoutInfo = listState.layoutInfo
                    val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                    if (itemInfo != null) { 
                        val viewportWidth = layoutInfo.viewportEndOffset.toFloat()
                        val itemOffset = itemInfo.offset.toFloat()
                        val itemSize = itemInfo.size.toFloat()
                        val centerOffset = (viewportWidth - itemSize) / 2
                        val distanceFromCenter = kotlin.math.abs(itemOffset - centerOffset)
                        // Scale calculation based on distance from viewport centre.
                        val scale = (1.1f - (distanceFromCenter / (viewportWidth / 2)) * 0.3f).coerceIn(0.8f, 1.1f)
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = (16f * scale).coerceAtLeast(2f) 
                    } 
                }) { 
                    WishlistMiniCard(book = book, icon = icon, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)) { onBookClick(book) } 
                } 
            } 
        } 
    } 
}

/**
 * AdminQuickActions Composable
 *
 * A specialized call-to-action button only visible to users with administrative privileges.
 */
@Composable
fun AdminQuickActions(onClick: () -> Unit) { Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)), shape = RoundedCornerShape(AdaptiveSpacing.itemRadius())) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(12.dp)); Text(AppConstants.TEXT_ADMIN_CONTROLS, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error); Spacer(Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.error) } } }

/**
 * TutorQuickActions Composable
 *
 * A specialized call-to-action button only visible to users with tutor privileges.
 */
@Composable
fun TutorQuickActions(onClick: () -> Unit) { Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)), shape = RoundedCornerShape(AdaptiveSpacing.itemRadius())) { Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(12.dp)); Text(AppConstants.TEXT_TUTOR_CONTROLS, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary); Spacer(Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.secondary) } } }

/**
 * SectionHeader Composable
 *
 * Standardised styling for dashboard category headers (e.g., "Recently Viewed", "My Wishlist").
 */
@Composable
fun SectionHeader(title: String) { @Suppress("DEPRECATION") Text(text = title, style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.primary) }

/**
 * WishlistMiniCard Composable
 *
 * A small, compact card for displaying a book or course preview.
 * Includes a gradient-overlay image and a small contextual icon (e.g., a heart for wishlist items).
 */
@Composable
fun WishlistMiniCard(book: Book, icon: ImageVector = Icons.Default.Favorite, color: Color, onClick: () -> Unit) { 
    Card(modifier = Modifier.width(210.dp).clickable { onClick() }, shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()), colors = CardDefaults.cardColors(containerColor = color), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))) { 
        Column { 
            Box(modifier = Modifier.fillMaxWidth().height(145.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.surface))), contentAlignment = Alignment.Center) { 
                if (book.imageUrl.isNotEmpty()) { 
                    Box(modifier = Modifier.fillMaxSize()) { 
                        AsyncImage(model = formatAssetUrl(book.imageUrl), contentDescription = book.title, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = AdaptiveSpacing.itemRadius(), topEnd = AdaptiveSpacing.itemRadius())), contentScale = ContentScale.Crop)
                        // Bottom shadow for text readability
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)), startY = 0.6f))) 
                    } 
                } else { 
                    Icon(imageVector = if (book.isAudioBook) Icons.Default.Headphones else if (book.mainCategory == AppConstants.CAT_COURSES) Icons.Default.School else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) 
                }; 
                Icon(icon, null, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(24.dp), tint = MaterialTheme.colorScheme.primary) 
            }; 
            Column(modifier = Modifier.padding(16.dp)) { 
                Text(text = book.title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp), fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = book.author, style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp), color = Color.Gray, maxLines = 1) 
            } 
        } 
    } 
}

/**
 * Generic placeholder for an empty section.
 */
@Composable
fun EmptySectionPlaceholder(text: String) { Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = Color.Transparent, shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))) { Text(text = text, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.Gray) } }

/**
 * Specialized placeholder for an empty library, encouraging the user to explore the store.
 */
@Composable
fun EmptyLibraryPlaceholder(onBrowse: () -> Unit) { Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.AutoMirrored.Filled.LibraryBooks, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f)); Spacer(Modifier.height(16.dp)); Text(AppConstants.MSG_LIBRARY_EMPTY, fontWeight = FontWeight.Bold); Text(AppConstants.MSG_GET_ITEMS_PROMPT, style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center); Spacer(Modifier.height(24.dp)); Button(onClick = onBrowse, shape = RoundedCornerShape(12.dp)) { Text(AppConstants.BTN_EXPLORE_STORE) } } }
