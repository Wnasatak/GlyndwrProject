package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage
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
    onViewInvoice: (Book) -> Unit,
    onPlayAudio: (Book) -> Unit,
    currentPlayingBookId: String?,
    isAudioPlaying: Boolean
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val userId = user?.uid ?: ""
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    val localUser by remember(userId) { if (userId.isNotEmpty()) db.userDao().getUserFlow(userId) else flowOf(null) }.collectAsState(initial = null)
    val wishlistIds by remember(userId) { if (userId.isNotEmpty()) db.userDao().getWishlistIds(userId) else flowOf(emptyList()) }.collectAsState(initial = emptyList())
    val historyIds by remember(userId) { if (userId.isNotEmpty()) db.userDao().getHistoryIds(userId) else flowOf(emptyList()) }.collectAsState(initial = emptyList())
    val commentedIds by remember(userId) { if (userId.isNotEmpty()) db.userDao().getCommentedProductIds(userId) else flowOf(emptyList()) }.collectAsState(initial = emptyList())
    val purchaseIds by remember(userId) { if (userId.isNotEmpty()) db.userDao().getPurchaseIds(userId) else flowOf(emptyList()) }.collectAsState(initial = emptyList())

    val lastViewedBooks = remember(historyIds, allBooks) { historyIds.mapNotNull { id -> allBooks.find { it.id == id } } }
    val wishlistBooks = remember(wishlistIds, allBooks) { wishlistIds.mapNotNull { id -> allBooks.find { it.id == id } } }
    val commentedBooks = remember(commentedIds, allBooks) { commentedIds.mapNotNull { id -> allBooks.find { it.id == id } } }
    val ownedBooks = remember(purchaseIds, allBooks) { purchaseIds.mapNotNull { id -> allBooks.find { it.id == id } } }

    val suggestions = remember(searchQuery, allBooks) { if (searchQuery.length < 2) emptyList() else allBooks.filter { it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true) }.take(5) }

    var showPaymentPopup by remember { mutableStateOf(false) }
    var bookToRemove by remember { mutableStateOf<Book?>(null) }

    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) { isSearchVisible = false; searchQuery = "" } }) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(text = when(localUser?.role) { "admin" -> "Admin Hub"; "teacher" -> "Faculty"; else -> "Student Hub" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        TopBarSearchAction(isSearchVisible = isSearchVisible) { isSearchVisible = true }
                        IconButton(onClick = { isSearchVisible = false; onToggleTheme() }) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                        if (localUser?.role == "admin") { IconButton(onClick = { navController.navigate("admin_panel") }) { Icon(Icons.Default.AdminPanelSettings, "Admin") } }
                        IconButton(onClick = { navController.navigate("profile") }) { Icon(Icons.Default.Settings, "Settings") }
                        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Log Out", tint = MaterialTheme.colorScheme.error) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    item { DashboardHeader(name = localUser?.name ?: user?.displayName ?: "Student", photoUrl = localUser?.photoUrl ?: user?.photoUrl?.toString(), role = localUser?.role ?: "student", balance = localUser?.balance ?: 0.0, onTopUp = { isSearchVisible = false; showPaymentPopup = true }) }
                    if (localUser?.role == "admin") { item { AdminQuickActions { isSearchVisible = false; navController.navigate("admin_panel") } } }
                    item { SectionHeader("Continue Reading") }
                    if (lastViewedBooks.isNotEmpty()) { item { GrowingLazyRow(lastViewedBooks, icon = Icons.Default.History) { book -> isSearchVisible = false; navController.navigate("bookDetails/${book.id}") } } } else { item { EmptySectionPlaceholder("No recently viewed items yet.") } }
                    item { SectionHeader("Your Recent Activity") }
                    if (commentedBooks.isNotEmpty()) { item { GrowingLazyRow(commentedBooks, icon = Icons.AutoMirrored.Filled.Comment) { book -> isSearchVisible = false; navController.navigate("bookDetails/${book.id}") } } } else { item { EmptySectionPlaceholder("No recent reviews.") } }
                    item { SectionHeader("Recently Liked") }
                    if (wishlistBooks.isNotEmpty()) { item { GrowingLazyRow(wishlistBooks, icon = Icons.Default.Favorite) { book -> isSearchVisible = false; navController.navigate("bookDetails/${book.id}") } } } else { item { EmptySectionPlaceholder("Your favorites list is empty.") } }
                    item { SectionHeader("Your Collection") }

                    if (ownedBooks.isEmpty()) { item { EmptyLibraryPlaceholder(onBrowse = onBack) } } else {
                        items(ownedBooks) { book ->
                            var showMenu by remember { mutableStateOf(false) }
                            BookItemCard(
                                book = book,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                onClick = { isSearchVisible = false; navController.navigate("bookDetails/${book.id}") },
                                imageOverlay = { if (book.isAudioBook) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { SpinningAudioButton(isPlaying = isAudioPlaying && currentPlayingBookId == book.id, onToggle = { onPlayAudio(book) }, size = 40) } } },
                                topEndContent = {
                                    Box {
                                        IconButton(onClick = { isSearchVisible = false; showMenu = true }, modifier = Modifier.size(40.dp).padding(4.dp)) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(24.dp), tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.outline) }
                                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                            if (book.price > 0) { DropdownMenuItem(text = { Text("View Invoice") }, onClick = { showMenu = false; onViewInvoice(book) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) }) }
                                            if (book.mainCategory != "University Gear" && book.price <= 0) {
                                                DropdownMenuItem(text = { Text("Remove from Library", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; bookToRemove = book }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) })
                                            }
                                        }
                                    }
                                },
                                bottomContent = {
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        val label = if (book.price > 0) "Purchased" else if (book.mainCategory == "University Gear") "Picked Up" else "In Library"
                                        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                            Text(text = label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(140.dp)) }
                }

                HomeSearchSection(
                    isSearchVisible = isSearchVisible, searchQuery = searchQuery, onQueryChange = { searchQuery = it }, onCloseClick = { isSearchVisible = false; searchQuery = "" }, suggestions = suggestions,
                    onSuggestionClick = { book -> searchQuery = book.title; isSearchVisible = false; navController.navigate("bookDetails/${book.id}") },
                    modifier = Modifier.padding(top = padding.calculateTopPadding()).zIndex(10f)
                )
            }
        }

        // --- Centralized Popups ---
        AppPopups.WalletTopUp(
            show = showPaymentPopup,
            user = localUser,
            onDismiss = { showPaymentPopup = false },
            onManageProfile = { showPaymentPopup = false; navController.navigate("profile") },
            onTopUpComplete = { amount ->
                scope.launch {
                    localUser?.let { db.userDao().upsertUser(it.copy(balance = it.balance + amount)) }
                    snackbarHostState.showSnackbar("£${String.format(Locale.US, "%.2f", amount)} added to your wallet!")
                    showPaymentPopup = false
                }
            }
        )

        AppPopups.RemoveFromLibraryConfirmation(
            show = bookToRemove != null,
            bookTitle = bookToRemove?.title ?: "",
            onDismiss = { bookToRemove = null },
            onConfirm = {
                scope.launch {
                    bookToRemove?.let { db.userDao().deletePurchase(userId, it.id) }
                    bookToRemove = null
                    snackbarHostState.showSnackbar("Removed from library")
                }
            }
        )
    }
}
