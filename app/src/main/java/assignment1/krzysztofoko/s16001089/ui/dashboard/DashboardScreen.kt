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
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.BookItemCard
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
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
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    
    var userRole by remember { mutableStateOf("student") }
    var ownedBooks by remember { mutableStateOf<List<Book>>(listOf()) }
    var wishlistBooks by remember { mutableStateOf<List<Book>>(listOf()) }
    var commentedBooks by remember { mutableStateOf<List<Book>>(listOf()) }
    var lastViewedBooks by remember { mutableStateOf<List<Book>>(listOf()) }
    
    var loadingLibrary by remember { mutableStateOf(true) }
    var userBalance by remember { mutableDoubleStateOf(0.0) }
    var selectedPaymentMethod by remember { mutableStateOf("University Account") }
    var showPaymentPopup by remember { mutableStateOf(false) }

    DisposableEffect(user, allBooks) {
        if (user == null) return@DisposableEffect onDispose {}
        
        val userRef = db.collection("users").document(user.uid)
        
        val userListener = userRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                userBalance = snapshot.getDouble("balance") ?: 0.0
                selectedPaymentMethod = snapshot.getString("selectedPaymentMethod") ?: "University Account"
                userRole = snapshot.getString("role") ?: "student"
            }
        }

        val historyListener = userRef.collection("history")
            .addSnapshotListener { snapshot, _ ->
                val docs = snapshot?.documents ?: emptyList()
                val sortedIds = docs.sortedByDescending { it.getLong("viewedAt") ?: 0L }.take(5).map { it.id }
                if (allBooks.isNotEmpty()) {
                    lastViewedBooks = sortedIds.mapNotNull { id -> allBooks.find { it.id == id } }
                }
            }

        val wishlistListener = userRef.collection("wishlist")
            .addSnapshotListener { snapshot, _ ->
                val docs = snapshot?.documents ?: emptyList()
                val sortedIds = docs.sortedByDescending { it.getLong("addedAt") ?: 0L }.take(5).map { it.id }
                if (allBooks.isNotEmpty()) {
                    wishlistBooks = sortedIds.mapNotNull { id -> allBooks.find { it.id == id } }
                }
            }

        val commentListener = userRef.collection("comments")
            .addSnapshotListener { snapshot, _ ->
                val docs = snapshot?.documents ?: emptyList()
                val sortedIds = docs.sortedByDescending { it.getLong("commentedAt") ?: 0L }.take(5).map { it.id }
                if (allBooks.isNotEmpty()) {
                    commentedBooks = sortedIds.mapNotNull { id -> allBooks.find { it.id == id } }
                }
            }

        val purchasesListener = userRef.collection("purchases")
            .addSnapshotListener { snapshot, _ ->
                val docs = snapshot?.documents ?: emptyList()
                ownedBooks = docs.mapNotNull { doc ->
                    allBooks.find { it.id == doc.id } ?: if (doc.contains("title")) {
                        Book(id = doc.id, title = doc.getString("title") ?: "Unknown", author = doc.getString("author") ?: "Unknown", category = doc.getString("category") ?: "General", isAudioBook = doc.getBoolean("audioBook") ?: false, price = doc.getDouble("pricePaid") ?: 0.0)
                    } else null
                }.distinctBy { it.id }
                loadingLibrary = false
            }

        onDispose {
            userListener.remove()
            historyListener.remove()
            wishlistListener.remove()
            commentListener.remove()
            purchasesListener.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = when(userRole) {
                                "admin" -> "Admin Dashboard"
                                "teacher" -> "Faculty Portal"
                                else -> "Student Hub"
                            }, 
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        ) 
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                        if (userRole == "admin") {
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
                    DashboardHeader(user = user, role = userRole, balance = userBalance) { showPaymentPopup = true }
                }

                if (userRole == "admin") {
                    item { AdminQuickActions { navController.navigate("admin_panel") } }
                }

                // --- Section 1: CONTINUE READING ---
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
                    item { EmptySectionPlaceholder("No recently viewed items yet. Open any item to track your history!") }
                }

                // --- Section 2: RECENT ACTIVITY (COMMENTED) ---
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
                    item { EmptySectionPlaceholder("No recent comments. Share your thoughts on any item to see them here.") }
                }

                // --- Section 3: RECENTLY LIKED ---
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
                    item { EmptySectionPlaceholder("Your favorites list is empty. Tap the heart icon on any item!") }
                }

                // --- Section 4: PURCHASED ITEMS (LIBRARY) ---
                item { SectionHeader("Your Purchased Items") }

                if (loadingLibrary) {
                    item { Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                } else if (ownedBooks.isEmpty()) {
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
                                        DropdownMenuItem(
                                            text = { Text("View Invoice") },
                                            onClick = {
                                                showMenu = false
                                                onViewInvoice(book)
                                            },
                                            leadingIcon = { Icon(Icons.Default.ReceiptLong, null) }
                                        )
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
            Dialog(onDismissRequest = { showPaymentPopup = false }) {
                Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Payment Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showPaymentPopup = false; navController.navigate("profile") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Manage in Profile") }
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Active Method", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(16.dp))
                                Text(selectedPaymentMethod, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(onClick = { showPaymentPopup = false }) { Text("Close") }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title, 
        style = MaterialTheme.typography.titleMedium, 
        fontWeight = FontWeight.Bold, 
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun EmptySectionPlaceholder(text: String) {
    Text(
        text = text, 
        modifier = Modifier.padding(horizontal = 24.dp), 
        style = MaterialTheme.typography.bodySmall, 
        color = Color.Gray,
        lineHeight = 18.sp
    )
}

@Composable
fun DashboardHeader(user: com.google.firebase.auth.FirebaseUser?, role: String, balance: Double, onBalanceClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f), Color.Transparent))).padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(photoUrl = user?.photoUrl?.toString(), modifier = Modifier.size(70.dp), isLarge = true)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Welcome, ${user?.displayName?.split(" ")?.firstOrNull() ?: "Member"}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                    Text(text = role.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Surface(onClick = onBalanceClick, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.End) {
                    Text("Credits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("£${String.format(Locale.US, "%.2f", balance)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun AdminQuickActions(onAdminClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Privileged Access", fontWeight = FontWeight.Bold)
                Text("You have permissions to manage the store catalog.", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onAdminClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Manage") }
        }
    }
}

@Composable
fun WishlistMiniCard(
    book: Book, 
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Favorite,
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(150.dp).clickable(onClick = onClick), 
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = if (book.isAudioBook) Icons.Default.Headphones else icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            Text(book.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(book.author, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun EmptyLibraryPlaceholder(onBrowse: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.LibraryBooks, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Text("Your library is empty", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)
        TextButton(onClick = onBrowse) { Text("Browse the Store") }
    }
}
