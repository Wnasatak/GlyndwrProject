package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.data.UserDao
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    allBooks: List<Book>, // Deprecated in favor of VM state
    onBack: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onViewInvoice: (Book) -> Unit,
    onPlayAudio: (Book) -> Unit,
    currentPlayingBookId: String?,
    isAudioPlaying: Boolean,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(
        repository = BookRepository(AppDatabase.getDatabase(LocalContext.current)),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val localUser by viewModel.localUser.collectAsState()
    val wishlistBooks by viewModel.wishlistBooks.collectAsState()
    val lastViewedBooks by viewModel.lastViewedBooks.collectAsState()
    val commentedBooks by viewModel.commentedBooks.collectAsState()
    val ownedBooks by viewModel.ownedBooks.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val showPaymentPopup by viewModel.showPaymentPopup.collectAsState()
    val bookToRemove by viewModel.bookToRemove.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) viewModel.setSearchVisible(false) }) {
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
                        TopBarSearchAction(isSearchVisible = isSearchVisible) { viewModel.setSearchVisible(true) }
                        IconButton(onClick = { viewModel.setSearchVisible(false); onToggleTheme() }) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
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
                    item { DashboardHeader(name = localUser?.name ?: "Student", photoUrl = localUser?.photoUrl, role = localUser?.role ?: "student", balance = localUser?.balance ?: 0.0, onTopUp = { viewModel.setSearchVisible(false); viewModel.setShowPaymentPopup(true) }) }
                    if (localUser?.role == "admin") { item { AdminQuickActions { viewModel.setSearchVisible(false); navController.navigate("admin_panel") } } }
                    item { SectionHeader("Continue Reading") }
                    if (lastViewedBooks.isNotEmpty()) { item { GrowingLazyRow(lastViewedBooks, icon = Icons.Default.History) { book -> viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") } } } else { item { EmptySectionPlaceholder("No recently viewed items yet.") } }
                    item { SectionHeader("Your Recent Activity") }
                    if (commentedBooks.isNotEmpty()) { item { GrowingLazyRow(commentedBooks, icon = Icons.AutoMirrored.Filled.Comment) { book -> viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") } } } else { item { EmptySectionPlaceholder("No recent reviews.") } }
                    item { SectionHeader("Recently Liked") }
                    if (wishlistBooks.isNotEmpty()) { item { GrowingLazyRow(wishlistBooks, icon = Icons.Default.Favorite) { book -> viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") } } } else { item { EmptySectionPlaceholder("Your favorites list is empty.") } }
                    item { SectionHeader("Your Collection") }

                    if (ownedBooks.isEmpty()) { item { EmptyLibraryPlaceholder(onBrowse = onBack) } } else {
                        items(ownedBooks) { book ->
                            var showMenu by remember { mutableStateOf(false) }
                            BookItemCard(
                                book = book,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                onClick = { viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") },
                                imageOverlay = { if (book.isAudioBook) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { SpinningAudioButton(isPlaying = isAudioPlaying && currentPlayingBookId == book.id, onToggle = { onPlayAudio(book) }, size = 40) } } },
                                topEndContent = {
                                    Box {
                                        IconButton(onClick = { viewModel.setSearchVisible(false); showMenu = true }, modifier = Modifier.size(40.dp).padding(4.dp)) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(24.dp), tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.outline) }
                                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                            if (book.price > 0) { DropdownMenuItem(text = { Text("View Invoice") }, onClick = { showMenu = false; onViewInvoice(book) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) }) }
                                            if (book.mainCategory != "University Gear" && book.price <= 0) {
                                                DropdownMenuItem(text = { Text("Remove from Library", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; viewModel.setBookToRemove(book) }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) })
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
                    isSearchVisible = isSearchVisible, searchQuery = searchQuery, onQueryChange = { viewModel.updateSearchQuery(it) }, onCloseClick = { viewModel.setSearchVisible(false) }, suggestions = suggestions,
                    onSuggestionClick = { book -> viewModel.updateSearchQuery(book.title); viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") },
                    modifier = Modifier.padding(top = padding.calculateTopPadding()).zIndex(10f)
                )
            }
        }

        // --- Centralized Popups ---
        AppPopups.WalletTopUp(
            show = showPaymentPopup,
            user = localUser,
            onDismiss = { viewModel.setShowPaymentPopup(false) },
            onManageProfile = { viewModel.setShowPaymentPopup(false); navController.navigate("profile") },
            onTopUpComplete = { amount ->
                viewModel.topUp(amount) { msg ->
                    viewModel.setShowPaymentPopup(false)
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )

        AppPopups.RemoveFromLibraryConfirmation(
            show = bookToRemove != null,
            bookTitle = bookToRemove?.title ?: "",
            onDismiss = { viewModel.setBookToRemove(null) },
            onConfirm = {
                viewModel.removePurchase(bookToRemove!!) { msg ->
                    viewModel.setBookToRemove(null)
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )
    }
}

class DashboardViewModelFactory(
    private val repository: BookRepository,
    private val userDao: UserDao,
    private val userId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, userDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
