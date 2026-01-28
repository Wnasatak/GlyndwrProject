package assignment1.krzysztofoko.s16001089.ui.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.NotificationLocal
import assignment1.krzysztofoko.s16001089.ui.components.AppPopups
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import assignment1.krzysztofoko.s16001089.ui.theme.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen displaying the user's notification history.
 * 
 * Provides a chronological list of system alerts, purchase confirmations, and 
 * pick-up reminders. Features include staggered swipe-to-delete animations,
 * unread count badges, and detailed bottom sheets for item-specific actions.
 * 
 * DESIGN PRINCIPLES:
 * - Reactive state: Uses Flow collection to stay in sync with the Room DB.
 * - Staggered Animations: Clears all items one by one for a smooth visual effect.
 * - Contextual UI: Actions change based on whether the notification is for a course, book, or gear.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateToItem: (String) -> Unit,    // Callback to view product details
    onNavigateToInvoice: (String) -> Unit, // Callback to view financial receipt
    onBack: () -> Unit,                    // Navigation return callback
    isDarkTheme: Boolean,                  // Global theme state (true = Dark Mode)
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    // Coroutine scope for launching staggered animations and DB tasks
    val scope = rememberCoroutineScope()
    // Host for displaying temporary feedback messages (e.g. "Item Removed")
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collecting notifications as reactive state from the ViewModel.
    // The underlying Room flow ensures this list updates automatically when items are added or deleted.
    val notifications by viewModel.notifications.collectAsState()
    
    /** 
     * Dismissing IDs State:
     * We use a snapshotStateList to track IDs that are currently in the process of 
     * animating out. This allows us to keep the item in the list while it fades away, 
     * only performing the final DB deletion after the visual transition ends.
     */
    val dismissingIds = remember { mutableStateListOf<String>() }
    
    // Derived state to keep the unread badge in the top bar accurate
    val unreadCount = notifications.count { !it.isRead }
    
    // Holds the currently inspected notification for the BottomSheet
    var selectedNotification by remember { mutableStateOf<NotificationLocal?>(null) }
    // Holds the associated product data fetched when a user clicks a notification
    var relatedBook by remember { mutableStateOf<Book?>(null) }
    
    // Sheet state for the details panel (skips partial expansion for a cleaner full view)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    // Controls visibility of the confirmation popup for deleting an item from the library
    var showRemoveConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Shared background component that renders vertical wave patterns
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Overrides default to show wavy background
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(AppConstants.TITLE_NOTIFICATIONS, fontWeight = FontWeight.Black)
                            // Unread Badge: Shows a highlighted chip if there are new items
                            if (unreadCount > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        "$unreadCount ${AppConstants.LABEL_NEW}", 
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.BTN_BACK) }
                    },
                    actions = {
                        /**
                         * STAGGERED CLEAR LOGIC:
                         * Instead of a sudden list empty, we add IDs to the dismissing state with a delay.
                         * This triggers the individual 'AnimatedVisibility' for each item in sequence.
                         */
                        if (notifications.isNotEmpty()) {
                            IconButton(onClick = { 
                                scope.launch {
                                    val idsToClear = notifications.map { it.id }
                                    idsToClear.forEach { id ->
                                        if (!dismissingIds.contains(id)) {
                                            dismissingIds.add(id)
                                            delay(30) // Creates the "cascade" falling effect
                                        }
                                    }
                                    delay(400) // Wait for the exit animations to complete
                                    viewModel.clearAll() // Perform the batch DB deletion
                                    dismissingIds.clear() // Reset the animation tracker
                                }
                            }) {
                                Icon(Icons.Default.DeleteSweep, AppConstants.BTN_CLEAR_ALL, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) // Frosted glass effect
                    )
                )
            }
        ) { paddingValues ->
            // Branching UI based on whether there is any history to show
            if (notifications.isEmpty()) {
                EmptyNotificationsView(modifier = Modifier.padding(paddingValues))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    /**
                     * Using 'key' in items ensures that Compose tracks individual notification identities.
                     * This is critical for maintaining animations during deletions or list updates.
                     */
                    items(notifications, key = { it.id }) { notification ->
                        // The item is visible if its ID is NOT in the dismissing list
                        val isVisible = !dismissingIds.contains(notification.id)

                        /**
                         * Lifecycle Sync: 
                         * When an item is marked as invisible (isVisible = false), we wait for its 
                         * slide-out animation to finish before removing it from the persistent DB.
                         */
                        LaunchedEffect(isVisible) {
                            if (!isVisible) {
                                delay(300) // Duration matching the exit transition
                                viewModel.deleteNotification(notification.id)
                                dismissingIds.remove(notification.id)
                            }
                        }

                        // Wrapper that handles entry/exit physics
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = slideOutHorizontally(targetOffsetX = { -it }) + shrinkVertically() + fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NotificationItem(
                                notification = notification,
                                isDarkTheme = isDarkTheme,
                                viewModel = viewModel,
                                onDelete = { dismissingIds.add(notification.id) },
                                onClick = { 
                                    scope.launch {
                                        // 1. Fetch metadata for the linked product
                                        relatedBook = viewModel.getRelatedBook(notification.productId)
                                        // 2. Set active notification for the sheet UI
                                        selectedNotification = notification
                                        // 3. Trigger the sheet display
                                        showSheet = true
                                        // 4. Update status in local DB (removes badge dot)
                                        viewModel.markAsRead(notification.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        /**
         * Notification Details Sheet:
         * A modal surface that appears from the bottom. It provides granular actions 
         * based on the content of the notification (e.g. showing 'View Invoice' for purchases).
         */
        if (showSheet && selectedNotification != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // HEADER ICON: Changes based on notification type (Purchase vs Pick-up)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(selectedNotification!!.type) {
                                AppConstants.NOTIF_TYPE_PURCHASE -> Icons.Default.ShoppingBag
                                AppConstants.NOTIF_TYPE_PICKUP -> Icons.Default.Storefront
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = selectedNotification!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = selectedNotification!!.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // NAV ACTION: Jumps to the product page (Book Reader, Course Info, or Gear)
                    Button(
                        onClick = { 
                            showSheet = false
                            onNavigateToItem(selectedNotification!!.productId)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, null)
                        Spacer(Modifier.width(12.dp))
                        Text(AppConstants.BTN_VIEW_PRODUCT_DETAILS, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // CONDITIONAL ACTION: Only show Invoice option if the item was NOT free
                    if (relatedBook != null && relatedBook!!.price > 0) {
                        OutlinedButton(
                            onClick = { 
                                showSheet = false
                                onNavigateToInvoice(selectedNotification!!.productId)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null)
                            Spacer(Modifier.width(12.dp))
                            Text(AppConstants.BTN_VIEW_INVOICE, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    /**
                     * CONDITIONAL ACTION: Special removal logic for free digital items.
                     * Physical Gear (CAT_GEAR) cannot be removed once reserved via this screen.
                     */
                    if (relatedBook != null && relatedBook!!.price <= 0 && relatedBook!!.mainCategory != AppConstants.CAT_GEAR) {
                        OutlinedButton(
                            onClick = { 
                                showSheet = false
                                showRemoveConfirm = true 
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.DeleteOutline, null)
                            Spacer(Modifier.width(12.dp))
                            Text(AppConstants.MENU_REMOVE_FROM_LIBRARY, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // UTILITY ACTION: Deletes the specific notification alert
                    OutlinedButton(
                        onClick = { 
                            dismissingIds.add(selectedNotification!!.id) // Trigger the exit animation
                            showSheet = false 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DeleteSweep, null)
                        Spacer(Modifier.width(12.dp))
                        Text(AppConstants.BTN_DELETE_NOTIFICATION, fontWeight = FontWeight.Bold)
                    }
                    
                    TextButton(
                        onClick = { showSheet = false },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(AppConstants.BTN_CLOSE, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Standardized popup for database-level deletion of items from collection
        AppPopups.RemoveFromLibraryConfirmation(
            show = showRemoveConfirm,
            bookTitle = relatedBook?.title ?: "",
            onDismiss = { showRemoveConfirm = false },
            onConfirm = {
                viewModel.removePurchase(relatedBook!!.id) { msg ->
                    showRemoveConfirm = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )
    }
}

/**
 * Individual list item component representing a single notification.
 * 
 * Logic Highlights:
 * - Dynamic Branding: Resolves icons and colors (Pink for AudioBooks, Teal for Courses, etc.) 
 *   to help users visually identify the source of the notification.
 * - Adaptive Backgrounds: Changes transparency and hue based on 'Read' status and theme.
 * - Badge Overlay: Adds a secondary icon badge to the product image for extra context.
 */
@Composable
fun NotificationItem(
    notification: NotificationLocal,
    isDarkTheme: Boolean,
    viewModel: NotificationViewModel,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    // Format the millisecond timestamp into a human-readable day/time string
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeStr = sdf.format(Date(notification.timestamp))
    
    // Reactive state to hold the associated product info (needed for the avatar and category colors)
    var relatedItem by remember { mutableStateOf<Book?>(null) }
    LaunchedEffect(notification.productId) {
        relatedItem = viewModel.getRelatedBook(notification.productId)
    }

    /**
     * VISUAL RESOLUTION:
     * Logic to determine category-specific Icons and theme Colors.
     * This creates a distinct "color coding" throughout the notification list.
     */
    val (categoryIcon, categoryColor) = when (relatedItem?.mainCategory) {
        AppConstants.CAT_BOOKS -> Icons.Default.MenuBook to CatBooksBlue
        AppConstants.CAT_AUDIOBOOKS -> Icons.Default.Headphones to CatAudioBooksPink
        AppConstants.CAT_COURSES -> Icons.Default.School to CatCoursesTeal
        AppConstants.CAT_GEAR -> Icons.Default.ShoppingBag to CatGearOrange
        else -> when (notification.type) {
            AppConstants.NOTIF_TYPE_PURCHASE -> Icons.Default.ShoppingBag to SuccessGreen
            AppConstants.NOTIF_TYPE_PICKUP -> Icons.Default.Storefront to InfoBlue
            else -> Icons.Default.Notifications to MaterialTheme.colorScheme.primary
        }
    }

    // Color definitions for the card container (Dark vs Light)
    val baseColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val accentColor = if (isDarkTheme) Color(0xFF311B92) else Color(0xFFF3E5F5)
    
    // Logic: Unread items have a stronger tint than read items to grab attention
    val cardColor = if (notification.isRead) baseColor.copy(alpha = 0.8f) else accentColor.copy(alpha = 0.6f)
    val borderColor = if (!notification.isRead) categoryColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // PRODUCT AVATAR WITH OVERLAID CATEGORY BADGE
            Box(modifier = Modifier.size(56.dp)) {
                // Correctly format the imageUrl using formatAssetUrl utility
                val imagePath = formatAssetUrl(relatedItem?.imageUrl ?: "images/media/GlyndwrUniversity.jpg")
                
                AsyncImage(
                    model = imagePath,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                
                // Small corner badge showing the category icon (e.g. Headphones for AudioBooks)
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(24.dp).background(categoryColor, CircleShape).padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = categoryIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Notification Metadata Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        // Visual Hierarchy: Bold for unread, semi-bold for read
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Priority Indicator Dot (only for unread)
                        if (!notification.isRead) {
                            Box(modifier = Modifier.size(10.dp).background(categoryColor, CircleShape))
                            Spacer(Modifier.width(8.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = AppConstants.BTN_DELETE, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Display the main message text with comfortable line height
                Text(text = notification.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Footer: Displays how long ago (or when) the alert was received
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Spacer(Modifier.width(4.dp))
                    Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}

/**
 * Empty state layout shown when the notification table in the DB is empty.
 * Uses a soft gradient background and a bell icon to inform the user that everything is quiet.
 */
@Composable
fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Soft icon background with custom linear gradient
        Box(
            modifier = Modifier.size(120.dp).background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(AppConstants.TEXT_ALL_CAUGHT_UP, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(AppConstants.MSG_EMPTY_NOTIFICATIONS, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
