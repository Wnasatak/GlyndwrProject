package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.data.UserDao
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    val filteredOwnedBooks by viewModel.filteredOwnedBooks.collectAsState()
    val selectedFilter by viewModel.selectedCollectionFilter.collectAsState()
    val walletHistory by viewModel.walletHistory.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val showPaymentPopup by viewModel.showPaymentPopup.collectAsState()
    val bookToRemove by viewModel.bookToRemove.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    var selectedBookForPickup by remember { mutableStateOf<Book?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showWalletHistory by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Books", "Audiobooks", "Gear", "Courses")
    
    val infiniteCount = Int.MAX_VALUE
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % filterOptions.size)
    val filterListState = rememberLazyListState(initialFirstVisibleItemIndex = startPosition)

    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) viewModel.setSearchVisible(false) }) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(text = when(localUser?.role) { "admin" -> AppConstants.TITLE_ADMIN_HUB; "teacher" -> AppConstants.TITLE_FACULTY; else -> AppConstants.TITLE_STUDENT_HUB }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        TopBarSearchAction(isSearchVisible = isSearchVisible) { viewModel.setSearchVisible(true) }
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_NOTIFICATIONS) }) { Icon(Icons.Default.Notifications, "Notifications") }
                            if (unreadCount > 0) { Surface(color = MaterialTheme.colorScheme.error, shape = CircleShape, modifier = Modifier.padding(6.dp).size(10.dp), border = BorderStroke(1.dp, Color.White)) {} }
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More Options") }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text(if (isDarkTheme) AppConstants.TITLE_LIGHT_MODE else AppConstants.TITLE_DARK_MODE) }, onClick = { showMenu = false; onToggleTheme() }, leadingIcon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) })
                                DropdownMenuItem(text = { Text(AppConstants.TITLE_PROFILE_SETTINGS) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_PROFILE) }, leadingIcon = { Icon(Icons.Default.Settings, null) })
                                if (localUser?.role == "admin") { DropdownMenuItem(text = { Text(AppConstants.TITLE_ADMIN_PANEL) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) }, leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) }) }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(text = { Text(AppConstants.BTN_LOG_OUT, color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onLogout() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) })
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    item { DashboardHeader(name = localUser?.name ?: "Student", photoUrl = localUser?.photoUrl, role = localUser?.role ?: "student", balance = localUser?.balance ?: 0.0, onTopUp = { viewModel.setSearchVisible(false); viewModel.setShowPaymentPopup(true) }, onViewHistory = { showWalletHistory = true }) }
                    
                    if (localUser?.role == "admin") { item { AdminQuickActions { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) } } }
                    
                    item { SectionHeader(AppConstants.TITLE_CONTINUE_READING) }
                    if (lastViewedBooks.isNotEmpty()) { item { GrowingLazyRow(lastViewedBooks, icon = Icons.Default.History) { book -> viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") } } } else { item { EmptySectionPlaceholder(AppConstants.MSG_NO_RECENTLY_VIEWED) } }
                    
                    item { SectionHeader(AppConstants.TITLE_RECENT_ACTIVITY) }
                    if (commentedBooks.isNotEmpty()) { item { GrowingLazyRow(commentedBooks, icon = Icons.AutoMirrored.Filled.Comment) { book -> viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") } } } else { item { EmptySectionPlaceholder(AppConstants.MSG_NO_RECENT_REVIEWS) } }
                    
                    item { SectionHeader(AppConstants.TITLE_RECENTLY_LIKED) }
                    if (wishlistBooks.isNotEmpty()) { item { GrowingLazyRow(wishlistBooks, icon = Icons.Default.Favorite) { book -> viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") } } } else { item { EmptySectionPlaceholder(AppConstants.MSG_FAVORITES_EMPTY) } }
                    
                    item { 
                        SectionHeader(AppConstants.TITLE_YOUR_COLLECTION)
                        LazyRow(
                            state = filterListState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(infiniteCount) { index ->
                                val filterIndex = index % filterOptions.size
                                val filter = filterOptions[filterIndex]
                                
                                val scale by remember {
                                    derivedStateOf {
                                        val layoutInfo = filterListState.layoutInfo
                                        val visibleItemsInfo = layoutInfo.visibleItemsInfo
                                        val itemInfo = visibleItemsInfo.find { it.index == index }
                                        if (itemInfo != null) {
                                            val center = layoutInfo.viewportEndOffset / 2
                                            val itemCenter = itemInfo.offset + (itemInfo.size / 2)
                                            val dist = abs(center - itemCenter).toFloat()
                                            val normDist = (dist / center).coerceIn(0f, 1f)
                                            1.25f - (normDist * 0.4f)
                                        } else 0.85f
                                    }
                                }

                                CategorySquareButton(
                                    label = filter,
                                    icon = when(filter) {
                                        "Books" -> Icons.AutoMirrored.Filled.MenuBook
                                        "Audiobooks" -> Icons.Default.Headphones
                                        "Gear" -> Icons.Default.Checkroom
                                        "Courses" -> Icons.Default.School
                                        else -> Icons.Default.GridView
                                    },
                                    isSelected = selectedFilter == filter,
                                    scale = scale,
                                    onClick = { viewModel.setCollectionFilter(filter) }
                                )
                            }
                        }
                    }

                    if (filteredOwnedBooks.isEmpty()) { 
                        item { 
                            EmptyLibraryPlaceholder(
                                onBrowse = { 
                                    val targetCategory = if (selectedFilter == "All") AppConstants.CAT_ALL else AppConstants.mapFilterToCategory(selectedFilter)
                                    navController.navigate("${AppConstants.ROUTE_HOME}?category=$targetCategory") 
                                }
                            ) 
                        } 
                    } else {
                        items(filteredOwnedBooks) { book ->
                            var showItemMenu by remember { mutableStateOf(false) }
                            BookItemCard(
                                book = book,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                onClick = { viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") },
                                imageOverlay = { if (book.isAudioBook) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { SpinningAudioButton(isPlaying = isAudioPlaying && currentPlayingBookId == book.id, onToggle = { onPlayAudio(book) }, size = 40) } } },
                                topEndContent = {
                                    Box {
                                        IconButton(onClick = { viewModel.setSearchVisible(false); showItemMenu = true }, modifier = Modifier.size(40.dp).padding(4.dp)) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(24.dp), tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.outline) }
                                        DropdownMenu(expanded = showItemMenu, onDismissRequest = { showItemMenu = false }) {
                                            if (book.mainCategory == AppConstants.CAT_COURSES) { DropdownMenuItem(text = { Text(AppConstants.BTN_ENTER_CLASSROOM) }, onClick = { showItemMenu = false; navController.navigate("${AppConstants.ROUTE_CLASSROOM}/${book.id}") }, leadingIcon = { Icon(Icons.Default.School, null) }) }
                                            if (book.price > 0.0) { DropdownMenuItem(text = { Text(AppConstants.BTN_VIEW_INVOICE) }, onClick = { showItemMenu = false; onViewInvoice(book) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) }) }
                                            if (book.mainCategory == AppConstants.CAT_GEAR) { DropdownMenuItem(text = { Text(AppConstants.BTN_PICKUP_INFO) }, onClick = { showItemMenu = false; selectedBookForPickup = book }, leadingIcon = { Icon(Icons.Default.Info, null) }) }
                                            if (book.mainCategory != AppConstants.CAT_GEAR && book.price <= 0.0) { DropdownMenuItem(text = { Text(AppConstants.MENU_REMOVE_FROM_LIBRARY, color = MaterialTheme.colorScheme.error) }, onClick = { showItemMenu = false; viewModel.setBookToRemove(book) }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }) }
                                        }
                                    }
                                },
                                bottomContent = {
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        val label = AppConstants.getItemStatusLabel(book)
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

                HomeSearchSection(isSearchVisible = isSearchVisible, searchQuery = searchQuery, recentSearches = recentSearches, onQueryChange = { viewModel.updateSearchQuery(it) }, onClearHistory = { viewModel.clearRecentSearches() }, onCloseClick = { viewModel.setSearchVisible(false) }, suggestions = suggestions, onSuggestionClick = { book -> viewModel.saveSearchQuery(book.title); viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") }, modifier = Modifier.padding(top = paddingValues.calculateTopPadding()).zIndex(10f))
            }
        }

        if (showWalletHistory) { WalletHistorySheet(transactions = walletHistory, onNavigateToProduct = { id -> navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") }, onViewInvoice = { id -> navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/$id") }, onDismiss = { showWalletHistory = false }) }
        if (selectedBookForPickup != null) { PickupInfoDialog(orderConfirmation = selectedBookForPickup?.orderConfirmation, onDismiss = { selectedBookForPickup = null }) }
        AppPopups.WalletTopUp(show = showPaymentPopup, user = localUser, onDismiss = { viewModel.setShowPaymentPopup(false) }, onTopUpComplete = { amount -> viewModel.topUp(amount) { msg -> viewModel.setShowPaymentPopup(false); scope.launch { snackbarHostState.showSnackbar(msg) } } }, onManageProfile = { viewModel.setShowPaymentPopup(false); navController.navigate(AppConstants.ROUTE_PROFILE) })
        AppPopups.RemoveFromLibraryConfirmation(show = bookToRemove != null, bookTitle = bookToRemove?.title ?: "", onDismiss = { viewModel.setBookToRemove(null) }, onConfirm = { viewModel.removePurchase(bookToRemove!!) { msg -> viewModel.setBookToRemove(null); scope.launch { snackbarHostState.showSnackbar(msg) } } })
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
