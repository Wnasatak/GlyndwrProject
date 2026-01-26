package assignment1.krzysztofoko.s16001089.ui.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.NotificationLocal
import assignment1.krzysztofoko.s16001089.ui.components.AppPopups
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    val unreadCount = notifications.count { !it.isRead }
    
    var selectedNotification by remember { mutableStateOf<NotificationLocal?>(null) }
    var relatedBook by remember { mutableStateOf<Book?>(null) }
    val sheetState = rememberModalBottomSheetState()
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
                            Text("Notifications", fontWeight = FontWeight.Black)
                            if (unreadCount > 0) {
                                Text("$unreadCount unread", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    },
                    actions = {
                        if (notifications.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearAll() }) {
                                Icon(Icons.Default.DeleteSweep, "Clear All", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        ) { padding ->
            if (notifications.isEmpty()) {
                EmptyNotificationsView(modifier = Modifier.padding(padding))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
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
                    Text(
                        text = selectedNotification!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = selectedNotification!!.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Button 1: Check it out (Always visible)
                    Button(
                        onClick = { 
                            showSheet = false
                            onNavigateToItem(selectedNotification!!.productId)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Check it out", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Button 2: View Invoice (Only if not free)
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
                            Text("View Invoice", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Button 3: Remove from library (Only if free AND not Gear)
                    if (relatedBook != null && relatedBook!!.price <= 0 && relatedBook!!.mainCategory != "University Gear") {
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
                            Text("Remove from Library", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    TextButton(
                        onClick = { showSheet = false },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Dismiss", color = Color.Gray)
                    }
                }
            }
        }

        // Professional Confirmation Popup
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

@Composable
fun NotificationItem(
    notification: NotificationLocal,
    onClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeStr = sdf.format(Date(notification.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) 
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = if (!notification.isRead) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant 
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(notification.type) {
                        "PURCHASE" -> Icons.Default.ShoppingBag
                        "PICKUP" -> Icons.Default.Storefront
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = if (notification.isRead) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Bold else FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape
                        ) {}
                    }
                }
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Inbox is empty",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            "We'll notify you about your orders and updates here.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 8.dp)
        )
    }
}
