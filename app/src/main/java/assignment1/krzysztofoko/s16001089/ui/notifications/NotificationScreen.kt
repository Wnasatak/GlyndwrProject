package assignment1.krzysztofoko.s16001089.ui.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
 * Screen displaying the user's notification history with engaging announcement support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateToItem: (String) -> Unit,    
    onNavigateToInvoice: (String) -> Unit, 
    onBack: () -> Unit,                    
    isDarkTheme: Boolean,                  
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val notifications by viewModel.notifications.collectAsState()
    val dismissingIds = remember { mutableStateOf<List<String>>(emptyList()) }
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
                                Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.padding(top = 2.dp)) {
                                    Text("$unreadCount ${AppConstants.LABEL_NEW}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.BTN_BACK) } },
                    actions = {
                        if (notifications.isNotEmpty()) {
                            IconButton(onClick = { 
                                scope.launch {
                                    val ids = notifications.map { it.id }
                                    dismissingIds.value = ids
                                    delay(400); viewModel.clearAll(); dismissingIds.value = emptyList()
                                }
                            }) { Icon(Icons.Default.DeleteSweep, AppConstants.BTN_CLEAR_ALL, tint = MaterialTheme.colorScheme.error) }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                )
            }
        ) { paddingValues ->
            if (notifications.isEmpty()) {
                EmptyNotificationsView(modifier = Modifier.padding(paddingValues))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(notifications, key = { it.id }) { notification ->
                        val isVisible = !dismissingIds.value.contains(notification.id)
                        LaunchedEffect(isVisible) { if (!isVisible) { delay(300); viewModel.deleteNotification(notification.id) } }

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = slideOutHorizontally(targetOffsetX = { -it }) + shrinkVertically() + fadeOut(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NotificationItem(notification = notification, isDarkTheme = isDarkTheme, viewModel = viewModel, onDelete = { dismissingIds.value = dismissingIds.value + notification.id }, onClick = { 
                                    scope.launch {
                                        relatedBook = if (notification.type == "ANNOUNCEMENT" || notification.type == "MESSAGE") null else viewModel.getRelatedBook(notification.productId)
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
            val isAnnouncement = selectedNotification!!.type == "ANNOUNCEMENT"
            val isMessage = selectedNotification!!.type == "MESSAGE"
            
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Styled Icon Header with Radial Glow
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = if (isAnnouncement || isMessage) Brush.radialGradient(listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), Color.Transparent))
                                        else Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), Color.Transparent)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).background(if (isAnnouncement || isMessage) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isMessage -> Icons.AutoMirrored.Filled.Chat
                                    isAnnouncement -> Icons.Default.Campaign
                                    isTopUp -> Icons.Default.AccountBalanceWallet
                                    selectedNotification!!.type == AppConstants.NOTIF_TYPE_PURCHASE -> Icons.Default.ShoppingBag
                                    selectedNotification!!.type == AppConstants.NOTIF_TYPE_PICKUP -> Icons.Default.Storefront
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = selectedNotification!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedNotification!!.message, 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = MaterialTheme.colorScheme.onSurface, 
                            textAlign = TextAlign.Center, 
                            modifier = Modifier.padding(20.dp),
                            lineHeight = 24.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (!isAnnouncement && !isMessage) {
                        Button(onClick = { showSheet = false; if (isTopUp) onNavigateToInvoice(selectedNotification!!.productId) else onNavigateToItem(selectedNotification!!.productId) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                            Icon(imageVector = if (isTopUp) Icons.AutoMirrored.Filled.ReceiptLong else Icons.Default.OpenInNew, contentDescription = null)
                            Spacer(Modifier.width(12.dp)); Text(text = if (isTopUp) AppConstants.BTN_VIEW_INVOICE else AppConstants.BTN_VIEW_PRODUCT_DETAILS, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    if (!isTopUp && !isAnnouncement && !isMessage && relatedBook != null && relatedBook!!.price > 0) {
                        OutlinedButton(onClick = { showSheet = false; onNavigateToInvoice(selectedNotification!!.productId) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null); Spacer(Modifier.width(12.dp)); Text(AppConstants.BTN_VIEW_INVOICE, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    if (relatedBook != null && relatedBook!!.price <= 0 && relatedBook!!.mainCategory != AppConstants.CAT_GEAR && !isTopUp && !isAnnouncement && !isMessage) {
                        OutlinedButton(onClick = { showSheet = false; showRemoveConfirm = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))) {
                            Icon(Icons.Default.DeleteOutline, null); Spacer(Modifier.width(12.dp)); Text(AppConstants.MENU_REMOVE_FROM_LIBRARY, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedButton(
                        onClick = { dismissingIds.value = dismissingIds.value + selectedNotification!!.id; showSheet = false }, 
                        modifier = Modifier.fillMaxWidth().height(56.dp), 
                        shape = RoundedCornerShape(16.dp), 
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), 
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp)); Text(AppConstants.BTN_DELETE_NOTIFICATION, fontWeight = FontWeight.Bold)
                    }
                    
                    TextButton(onClick = { showSheet = false }, modifier = Modifier.padding(top = 8.dp)) { Text(AppConstants.BTN_CLOSE, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }

        AppPopups.RemoveFromLibraryConfirmation(show = showRemoveConfirm, bookTitle = relatedBook?.title ?: "", onDismiss = { showRemoveConfirm = false }, onConfirm = { viewModel.removePurchase(relatedBook!!.id) { msg -> showRemoveConfirm = false; scope.launch { snackbarHostState.showSnackbar(msg) } } })
    }
}

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
    val isAnnouncement = notification.type == "ANNOUNCEMENT"
    val isMessage = notification.type == "MESSAGE"
    
    var relatedItem by remember { mutableStateOf<Book?>(null) }
    LaunchedEffect(notification.productId) {
        if (!isAnnouncement && !isMessage) relatedItem = viewModel.getRelatedBook(notification.productId)
    }

    val (categoryIcon, categoryColor) = when {
        isMessage -> Icons.AutoMirrored.Filled.Chat to MaterialTheme.colorScheme.secondary
        isAnnouncement -> Icons.Default.Campaign to MaterialTheme.colorScheme.secondary
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
    val accentColor = if (isDarkTheme) Color(0xFF311B92).copy(alpha = 0.3f) else Color(0xFFF3E5F5).copy(alpha = 0.8f)
    val announceColor = if (isDarkTheme) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFFE8F5E9).copy(alpha = 0.9f)
    val messageColor = if (isDarkTheme) Color(0xFF0D47A1).copy(alpha = 0.4f) else Color(0xFFE3F2FD).copy(alpha = 0.9f)
    
    val cardColor = when {
        isAnnouncement -> announceColor
        isMessage -> messageColor
        notification.isRead -> baseColor.copy(alpha = 0.8f)
        else -> accentColor
    }
                    
    val borderColor = when {
        (isAnnouncement || isMessage) -> categoryColor.copy(alpha = if (notification.isRead) 0.3f else 0.8f)
        !notification.isRead -> categoryColor.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    }

    // Pulsing animation for unread announcements/messages
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if ((isAnnouncement || isMessage) && !notification.isRead) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = if ((isAnnouncement || isMessage) && !notification.isRead) {
                    Brush.verticalGradient(listOf(categoryColor.copy(alpha = 0.15f), cardColor))
                } else {
                    Brush.verticalGradient(listOf(cardColor, cardColor))
                }
            )
            .border(if ((isAnnouncement || isMessage) && !notification.isRead) 2.dp else 1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                val imagePath = if (isAnnouncement || isMessage || notification.productId == AppConstants.ID_TOPUP) "file:///android_asset/images/media/GlyndwrUniversity.jpg"
                                else formatAssetUrl(relatedItem?.imageUrl ?: "images/media/GlyndwrUniversity.jpg")
                
                AsyncImage(
                    model = imagePath, 
                    contentDescription = null, 
                    modifier = Modifier.size(56.dp).clip(CircleShape).border(1.dp, categoryColor.copy(alpha = 0.3f), CircleShape), 
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .scale(pulseScale)
                        .size(26.dp)
                        .background(categoryColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = categoryIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(
                            text = notification.title, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = if (notification.isRead) FontWeight.Bold else FontWeight.Black, 
                            color = if ((isAnnouncement || isMessage) && !notification.isRead) categoryColor else MaterialTheme.colorScheme.onSurface, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isAnnouncement && !notification.isRead) {
                            Surface(
                                color = categoryColor,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("URGENT", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Default.Close, contentDescription = AppConstants.BTN_DELETE, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(16.dp)) }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp)); Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(120.dp).background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))), CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        }
        Spacer(modifier = Modifier.height(24.dp)); Text(AppConstants.TEXT_ALL_CAUGHT_UP, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp)); Text(AppConstants.MSG_EMPTY_NOTIFICATIONS, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
