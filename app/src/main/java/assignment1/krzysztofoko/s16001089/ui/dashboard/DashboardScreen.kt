package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.LOCAL_USER_ID
import assignment1.krzysztofoko.s16001089.ui.components.BookItemCard
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.components.IntegratedTopUpDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    allBooks: List<Book>,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onViewInvoice: (Book) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // FETCH DATA LOCALLY USING FIXED ID (Matching AppNavigation)
    val localUser by db.userDao().getUserFlow(LOCAL_USER_ID).collectAsState(initial = null)

    val wishlistIds by remember(user) {
        user?.let { db.userDao().getWishlistIds(it.uid) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val historyIds by remember(user) {
        user?.let { db.userDao().getHistoryIds(it.uid) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val commentedIds by remember(user) {
        user?.let { db.userDao().getCommentedProductIds(it.uid) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val purchaseIds by remember(user) {
        user?.let { db.userDao().getPurchaseIds(it.uid) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val lastViewedBooks = remember(historyIds, allBooks) {
        historyIds.mapNotNull { id -> allBooks.find { it.id == id } }
    }
    
    val wishlistBooks = remember(wishlistIds, allBooks) {
        wishlistIds.mapNotNull { id -> allBooks.find { it.id == id } }
    }

    val commentedBooks = remember(commentedIds, allBooks) {
        commentedIds.mapNotNull { id -> allBooks.find { it.id == id } }
    }

    val ownedBooks = remember(purchaseIds, allBooks) {
        purchaseIds.mapNotNull { id -> allBooks.find { it.id == id } }
    }

    var showPaymentPopup by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = when(localUser?.role) {
                                "admin" -> "Admin Dashboard"
                                "teacher" -> "Faculty Portal"
                                else -> "Student Hub"
                            }, 
                            fontWeight = FontWeight.ExtraBold
                        ) 
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                        if (localUser?.role == "admin") {
                            IconButton(onClick = { navController.navigate("admin_panel") }) { Icon(Icons.Default.AdminPanelSettings, "Admin") }
                        }
                        IconButton(onClick = { navController.navigate("profile") }) { Icon(Icons.Default.Settings, "Settings") }
                        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Log Out", tint = MaterialTheme.colorScheme.error) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    DashboardHeader(user = user, role = localUser?.role ?: "student", balance = localUser?.balance ?: 0.0) { showPaymentPopup = true }
                }

                if (localUser?.role == "admin") {
                    item { AdminQuickActions { navController.navigate("admin_panel") } }
                }

                // Section 1: CONTINUE READING
                item { SectionHeader("Continue Reading") }
                if (lastViewedBooks.isNotEmpty()) {
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(lastViewedBooks) { book ->
                                WishlistMiniCard(book, icon = Icons.Default.History, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)) { 
                                    navController.navigate("bookDetails/${book.id}") 
                                }
                            }
                        }
                    }
                } else {
                    item { EmptySectionPlaceholder("No recently viewed items yet.") }
                }

                // Section 2: RECENT ACTIVITY
                item { SectionHeader("Your Recent Activity") }
                if (commentedBooks.isNotEmpty()) {
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(commentedBooks) { book ->
                                WishlistMiniCard(book, icon = Icons.Default.Comment, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)) { 
                                    navController.navigate("bookDetails/${book.id}") 
                                }
                            }
                        }
                    }
                } else {
                    item { EmptySectionPlaceholder("No recent reviews.") }
                }

                // Section 3: RECENTLY LIKED
                item { SectionHeader("Recently Liked") }
                if (wishlistBooks.isNotEmpty()) {
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(wishlistBooks) { book ->
                                WishlistMiniCard(book, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)) { 
                                    navController.navigate("bookDetails/${book.id}") 
                                }
                            }
                        }
                    }
                } else {
                    item { EmptySectionPlaceholder("Your favorites list is empty.") }
                }

                // Section 4: PURCHASED ITEMS (LIBRARY)
                item { SectionHeader("Your Purchased Items") }

                if (ownedBooks.isEmpty()) {
                    item { EmptyLibraryPlaceholder(onBrowse = onBack) }
                } else {
                    items(ownedBooks) { book ->
                        var showMenu by remember { mutableStateOf(false) }
                        BookItemCard(
                            book = book,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onClick = { navController.navigate("bookDetails/${book.id}") },
                            trailingContent = {
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, "Options")
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        if (book.price > 0) {
                                            DropdownMenuItem(
                                                text = { Text("View Invoice") },
                                                onClick = {
                                                    showMenu = false
                                                    onViewInvoice(book)
                                                },
                                                leadingIcon = { Icon(Icons.Default.ReceiptLong, null) }
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                text = { Text("Remove from Library", color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showMenu = false
                                                    if (user != null) {
                                                        scope.launch {
                                                            db.userDao().deletePurchase(user.uid, book.id)
                                                            snackbarHostState.showSnackbar("Removed from library")
                                                        }
                                                    }
                                                },
                                                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(48.dp)) }
            }
        }

        if (showPaymentPopup) {
            IntegratedTopUpDialog(
                user = localUser,
                onDismiss = { showPaymentPopup = false },
                onManageProfile = { 
                    showPaymentPopup = false
                    navController.navigate("profile")
                },
                onTopUpComplete = { amount ->
                    scope.launch {
                        localUser?.let { currentUser ->
                            val updatedUser = currentUser.copy(balance = currentUser.balance + amount)
                            db.userDao().upsertUser(updatedUser)
                            snackbarHostState.showSnackbar("£${String.format(Locale.US, "%.2f", amount)} added to your wallet!")
                            showPaymentPopup = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardHeader(user: com.google.firebase.auth.FirebaseUser?, role: String, balance: Double, onTopUp: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(photoUrl = user?.photoUrl?.toString(), modifier = Modifier.size(64.dp), isLarge = true)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = "Welcome, ${user?.displayName ?: "Student"}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
                        Text(text = role.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Account Balance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("£${String.format(Locale.US, "%.2f", balance)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = onTopUp, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Top Up")
                }
            }
        }
    }
}

@Composable
fun AdminQuickActions(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Text("Admin Controls: Manage Catalog & Users", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.primary)
}

@Composable
fun WishlistMiniCard(book: Book, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Favorite, color: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.width(160.dp).clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = if (book.isAudioBook) Icons.Default.Headphones else if (book.mainCategory == "University Courses") Icons.Default.School else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                Icon(icon, null, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(book.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

@Composable
fun EmptySectionPlaceholder(text: String) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = Color.Transparent, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))) {
        Text(text = text, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun EmptyLibraryPlaceholder(onBrowse: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.LibraryBooks, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Your library is empty", fontWeight = FontWeight.Bold)
        Text("Get books, courses or gear to see them here.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBrowse, shape = RoundedCornerShape(12.dp)) { Text("Explore Store") }
    }
}
