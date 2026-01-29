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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val notifications by viewModel.notifications.collectAsState()
    val dismissingIds = remember { mutableStateListOf<String>() }
    val unreadCount = notifications.count { !it.isRead }
    
    var selectedNotification by remember { mutableStateOf<NotificationLocal?>(null) }
    var relatedBook by remember { mutableStateOf<Book?>(null) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(AppConstants.TITLE_NOTIFICATIONS, fontWeight = FontWeight.Black)
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
                        if (notifications.isNotEmpty()) {
                            IconButton(onClick = { 
                                scope.launch {
                                    val idsToClear = notifications.map { it.id }
                                    idsToClear.forEach { id ->
                                        if (!dismissingIds.contains(id)) {
                                            dismissingIds.add(id)
                                            delay(30)
                                        }
                                    }
                                    delay(400)
                                    viewModel.clearAll()
                                    dismissingIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.DeleteSweep, AppConstants.BTN_CLEAR_ALL, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        ) { paddingValues ->
            if (notifications.isEmpty()) {
                EmptyNotificationsView(modifier = Modifier.padding(paddingValues))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        val isVisible = !dismissingIds.contains(notification.id)

                        LaunchedEffect(isVisible) {
                            if (!isVisible) {
                                delay(300)
                                viewModel.deleteNotification(notification.id)
                                dismissingIds.remove(notification.id)
                            }
                        }

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
                                        relatedBook = viewModel.getRelatedBook(notification.productId)
                                        selectedNotification = notification
                                        showSheet = true
                                        viewModel.markAsRead(notification.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showSheet && selectedNotification != null) {
            val isTopUp = selectedNotification!!.productId == AppConstants.ID_TOPUP
            
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
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isTopUp -> Icons.Default.AccountBalanceWallet
                                selectedNotification!!.type == AppConstants.NOTIF_TYPE_PURCHASE -> Icons.Default.ShoppingBag
                                selectedNotification!!.type == AppConstants.NOTIF_TYPE_PICKUP -> Icons.Default.Storefront
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
                    
                    // PRIMARY ACTION: Dynamic based on notification type
                    Button(
                        onClick = { 
                            showSheet = false
                            if (isTopUp) onNavigateToInvoice(selectedNotification!!.productId)
                            else onNavigateToItem(selectedNotification!!.productId)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isTopUp) Icons.AutoMirrored.Filled.ReceiptLong else Icons.Default.OpenInNew, 
                            contentDescription = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isTopUp) AppConstants.BTN_VIEW_INVOICE else AppConstants.BTN_VIEW_PRODUCT_DETAILS, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // SECONDARY ACTION: Show Invoice for standard paid purchases (non-topup)
                    if (!isTopUp && relatedBook != null && relatedBook!!.price > 0) {
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
                    
                    if (relatedBook != null && relatedBook!!.price <= 0 && relatedBook!!.mainCategory != AppConstants.CAT_GEAR && !isTopUp) {
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

                    OutlinedButton(
                        onClick = { 
                            dismissingIds.add(selectedNotification!!.id)
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
 */
@Composable
fun NotificationItem(
    notification: NotificationLocal,
    isDarkTheme: Boolean,
    viewModel: NotificationViewModel,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeStr = sdf.format(Date(notification.timestamp))
    
    var relatedItem by remember { mutableStateOf<Book?>(null) }
    LaunchedEffect(notification.productId) {
        relatedItem = viewModel.getRelatedBook(notification.productId)
    }

    val (categoryIcon, categoryColor) = when {
        notification.productId == AppConstants.ID_TOPUP -> Icons.Default.AccountBalanceWallet to CatCoursesTeal
        relatedItem?.mainCategory == AppConstants.CAT_BOOKS -> Icons.Default.MenuBook to CatBooksBlue
        relatedItem?.mainCategory == AppConstants.CAT_AUDIOBOOKS -> Icons.Default.Headphones to CatAudioBooksPink
        relatedItem?.mainCategory == AppConstants.CAT_COURSES -> Icons.Default.School to CatCoursesTeal
        relatedItem?.mainCategory == AppConstants.CAT_GEAR -> Icons.Default.ShoppingBag to CatGearOrange
        else -> when (notification.type) {
            AppConstants.NOTIF_TYPE_PURCHASE -> Icons.Default.ShoppingBag to SuccessGreen
            AppConstants.NOTIF_TYPE_PICKUP -> Icons.Default.Storefront to InfoBlue
            else -> Icons.Default.Notifications to MaterialTheme.colorScheme.primary
        }
    }

    val baseColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val accentColor = if (isDarkTheme) Color(0xFF311B92) else Color(0xFFF3E5F5)
    
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
            Box(modifier = Modifier.size(56.dp)) {
                val imagePath = if (notification.productId == AppConstants.ID_TOPUP) "file:///android_asset/images/media/GlyndwrUniversity.jpg"
                                else formatAssetUrl(relatedItem?.imageUrl ?: "images/media/GlyndwrUniversity.jpg")
                
                AsyncImage(
                    model = imagePath,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(24.dp).background(categoryColor, CircleShape).padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = categoryIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(text = notification.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
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
 */
@Composable
fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
