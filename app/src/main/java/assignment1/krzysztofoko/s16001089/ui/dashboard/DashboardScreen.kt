package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * DashboardScreen.kt
 *
 * This file implements the primary management hub for the user. It provides a centralised 
 * overview of their academic profile, financial status (university wallet), course 
 * enrolments, and personal collection. The dashboard is highly adaptive, changing its 
 * functionality and layout based on user roles (Student, Tutor, Admin).
 */

/**
 * DashboardScreen Composable
 *
 * An immersive, role-aware dashboard that serves as the user's primary workstation.
 *
 * Key features:
 * - **Role-Based Orchestration:** Dynamically renders admin and tutor controls only for 
 *   authorised personnel.
 * - **Financial Integration:** Provides a real-time wallet overview with top-up and 
 *   transaction history capabilities.
 * - **Academic Tracking:** Displays application statuses and provides quick-access 
 *   headers for active VLE (Virtual Learning Environment) classrooms.
 * - **Interactive Collections:** Implements a filtered grid of owned items with context-specific 
 *   actions (Listen, Read, View Invoice).
 * - **Adaptive Layout:** Manages grid columns and component sizing for both compact 
 *   mobile devices and large-screen tablets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    allBooks: List<Book>,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Theme) -> Unit,
    onViewInvoice: (Book) -> Unit,
    onPlayAudio: (Book) -> Unit,
    currentPlayingBookId: String?,
    isAudioPlaying: Boolean,
    currentTheme: Theme,
    onOpenThemeBuilder: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(
        repository = BookRepository(AppDatabase.getDatabase(LocalContext.current)),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        classroomDao = AppDatabase.getDatabase(LocalContext.current).classroomDao(),
        auditDao = AppDatabase.getDatabase(LocalContext.current).auditDao(),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- VIEWMODEL STATE OBSERVATION --- //
    val localUser by viewModel.localUser.collectAsState()
    val wishlistBooks by viewModel.wishlistBooks.collectAsState()
    val lastViewedBooks by viewModel.lastViewedBooks.collectAsState()
    val commentedBooks by viewModel.commentedBooks.collectAsState()
    val filteredOwnedBooks by viewModel.filteredOwnedBooks.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()
    val selectedFilter by viewModel.selectedCollectionFilter.collectAsState()
    val walletHistory by viewModel.walletHistory.collectAsState()
    val applicationCount by viewModel.applicationCount.collectAsState()
    val applicationsMap by viewModel.applicationsMap.collectAsState()
    val activeLiveSessions by viewModel.activeLiveSessions.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val showPaymentPopup by viewModel.showPaymentPopup.collectAsState()
    val bookToRemove by viewModel.bookToRemove.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val hasMessages by viewModel.hasMessages.collectAsState()
    val unreadMessagesCount by viewModel.unreadMessagesCount.collectAsState()

    // --- UI INTERACTION STATE --- //
    var selectedBookForPickup by remember { mutableStateOf<Book?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showWalletHistory by remember { mutableStateOf(false) }
    var showClassroomPicker by remember { mutableStateOf(false) }

    // --- RESPONSIVE CONFIGURATION --- //
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600
    val isCompact = screenWidth < 420

    val columns = if (isTablet) 2 else 1
    val gridState = rememberLazyGridState()

    // Pre-calculate course groupings for specialised headers.
    val enrolledPaidCourse = remember(allBooks, purchasedIds) {
        allBooks.find { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }
    }
    val enrolledFreeCourses = remember(allBooks, purchasedIds) {
        allBooks.filter { it.mainCategory == AppConstants.CAT_COURSES && it.price <= 0.0 && purchasedIds.contains(it.id) }
    }
    val enrolledCourses = remember(enrolledPaidCourse, enrolledFreeCourses) {
        listOfNotNull(enrolledPaidCourse) + enrolledFreeCourses
    }

    val isAdmin = localUser?.role == "admin"
    val isTutor = localUser?.role == "teacher" || localUser?.role == "tutor"
    // Show applications link only if the student has pending requests and isn't yet in a paid course.
    val showApplications = applicationCount > 0 && enrolledPaidCourse == null

    /**
     * Calculated scroll target for the collection grid.
     */
    val collectionIndex = remember(showApplications, enrolledPaidCourse, enrolledFreeCourses, isAdmin, isTutor) {
        var count = 1
        if (showApplications) count++
        if (enrolledPaidCourse != null || enrolledFreeCourses.isNotEmpty()) {
            count++
            if (enrolledPaidCourse != null) count++
            count += enrolledFreeCourses.size
        }
        if (isAdmin) count++
        if (isTutor) count++
        count += 6
        count
    }

    // Animation for the notification bell badge.
    val infiniteTransition = rememberInfiniteTransition(label = "bellRing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotation"
    )

    // Collection filter options.
    val filterOptions = listOf(AppConstants.FILTER_ALL, AppConstants.FILTER_BOOKS, AppConstants.FILTER_AUDIOBOOKS, AppConstants.FILTER_GEAR, AppConstants.FILTER_COURSES)
    val filterListState = rememberLazyListState(initialFirstVisibleItemIndex = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % filterOptions.size))

    // Root container with a global search-dismiss handler.
    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) viewModel.setSearchVisible(false) }) {
        // Branded background.
        VerticalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // --- TOP NAVIGATION BAR --- //
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            text = when {
                                isAdmin -> AppConstants.TITLE_ADMIN_HUB
                                isTutor -> AppConstants.TITLE_TUTOR_HUB
                                else -> AppConstants.TITLE_STUDENT_HUB
                            },
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.BTN_BACK) } },
                    actions = {
                        // Compact-aware actions: some are hidden or moved to the menu on small phones.
                        if (!isCompact) {
                            TopBarSearchAction(isSearchVisible = isSearchVisible) { viewModel.setSearchVisible(true) }
                            IconButton(onClick = { navController.navigate(AppConstants.ROUTE_HOME) }) { Icon(Icons.Rounded.Storefront, AppConstants.TITLE_STORE) }
                        }

                        // Message Center Badge.
                        if (hasMessages) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                val chatColor = if (unreadMessagesCount > 0 && isDarkTheme) Color(0xFFFFEB3B) else if (unreadMessagesCount > 0) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurface
                                IconButton(onClick = { navController.navigate(AppConstants.ROUTE_MESSAGES) }, modifier = Modifier.size(36.dp)) {
                                    Icon(imageVector = Icons.AutoMirrored.Rounded.Chat, contentDescription = AppConstants.TITLE_MESSAGES, tint = chatColor, modifier = Modifier.size(22.dp))
                                }
                                if (unreadMessagesCount > 0) {
                                    Surface(color = Color(0xFFE53935), shape = CircleShape, border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface), modifier = Modifier.size(18.dp).offset(x = 4.dp, y = (-2).dp).align(Alignment.TopEnd)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = if (unreadMessagesCount > 9) "!" else unreadMessagesCount.toString(), style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Black, lineHeight = 9.sp), color = Color.White, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }

                        // Notification Center Badge with Ringing Animation.
                        Box(contentAlignment = Alignment.TopEnd) {
                            val bellColor = if (unreadCount > 0 && isDarkTheme) Color(0xFFFFEB3B) else if (unreadCount > 0) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurface
                            IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_NOTIFICATIONS) }, modifier = Modifier.size(36.dp)) {
                                Icon(imageVector = if (unreadCount > 0) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications, contentDescription = AppConstants.TITLE_NOTIFICATIONS, tint = bellColor, modifier = Modifier.size(24.dp).graphicsLayer { if (unreadCount > 0) { rotationZ = rotation } })
                            }
                            if (unreadCount > 0) {
                                Surface(color = Color(0xFFE53935), shape = CircleShape, border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface), modifier = Modifier.size(18.dp).offset(x = 4.dp, y = (-2).dp).align(Alignment.TopEnd)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = if (unreadCount > 9) "!" else unreadCount.toString(), style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Black, lineHeight = 9.sp), color = Color.White, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        if (!isCompact) {
                            ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, onOpenCustomBuilder = onOpenThemeBuilder, isLoggedIn = true)
                        }

                        // Overflow Menu for additional actions and role-specific panels.
                        Box {
                            IconButton(onClick = { showMenu = true }) { Icon(Icons.Rounded.MoreVert, AppConstants.TITLE_MORE_OPTIONS) }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.width(220.dp)) {
                                Text(text = "DASHBOARD OPTIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                                HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                if (isCompact) {
                                    DropdownMenuItem(text = { Text("Search Products", style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; viewModel.setSearchVisible(true) }, leadingIcon = { Icon(Icons.Rounded.Search, null) })
                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_STORE, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_HOME) }, leadingIcon = { Icon(Icons.Rounded.Storefront, null) })
                                    DropdownMenuItem(text = { Text("Appearance", style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; showThemePicker = true }, leadingIcon = { Icon(Icons.Rounded.Palette, null) })
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }

                                if (showApplications) {
                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_MY_APPLICATIONS, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_MY_APPLICATIONS) }, leadingIcon = { Icon(Icons.Rounded.Assignment, null) })
                                }

                                if (enrolledCourses.isNotEmpty()) {
                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_CLASSROOM, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; showClassroomPicker = true }, leadingIcon = { Icon(Icons.Rounded.School, null) })
                                }

                                DropdownMenuItem(text = { Text(AppConstants.TITLE_MESSAGES, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_MESSAGES) }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Chat, null) })

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                if (!isCompact) {
                                    DropdownMenuItem(text = { Text("Appearance", style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; showThemePicker = true }, leadingIcon = { Icon(Icons.Rounded.Palette, null) })
                                }

                                DropdownMenuItem(text = { Text("My Profile", style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_PROFILE) }, leadingIcon = { Icon(Icons.Rounded.AccountCircle, null) })
                                DropdownMenuItem(text = { Text(AppConstants.TITLE_PROFILE_SETTINGS, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_EDIT_PROFILE) }, leadingIcon = { Icon(Icons.Rounded.Settings, null) })
                                DropdownMenuItem(text = { Text("About App", style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_ABOUT) }, leadingIcon = { Icon(Icons.Rounded.Info, null) })

                                if (isAdmin) { DropdownMenuItem(text = { Text(AppConstants.TITLE_ADMIN_PANEL, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) }, leadingIcon = { Icon(Icons.Rounded.AdminPanelSettings, null) }) }
                                if (isTutor) { DropdownMenuItem(text = { Text(AppConstants.TITLE_TUTOR_PANEL, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_TUTOR_PANEL) }, leadingIcon = { Icon(Icons.Rounded.School, null) }) }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                DropdownMenuItem(text = { Text("Sign Off", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; onLogout() }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) })
                            }

                            ThemeSelectionDropdown(expanded = showThemePicker, onDismissRequest = { showThemePicker = false }, onThemeChange = { theme -> onThemeChange(theme) }, onOpenCustomBuilder = { onOpenThemeBuilder() }, isLoggedIn = true, offset = DpOffset(x = (-150).dp, y = 0.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            // --- MAIN SCROLLABLE CONTENT GRID --- //
            Box(modifier = Modifier.fillMaxSize()) {
                AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 300.dp),
                        horizontalArrangement = if (isTablet) Arrangement.spacedBy(16.dp) else Arrangement.Start
                    ) {
                        // Section 1: Welcome Header and Wallet Balance.
                        dashboardHeaderSection(
                            user = localUser, isTablet = isTablet, onProfileClick = { navController.navigate(AppConstants.ROUTE_PROFILE) },
                            onTopUp = { viewModel.setSearchVisible(false); viewModel.setShowPaymentPopup(true) },
                            onViewHistory = { showWalletHistory = true }
                        )
                        // Section 2: Active Academic Applications.
                        applicationsSection(showApplications, isTablet, onClick = { navController.navigate(AppConstants.ROUTE_MY_APPLICATIONS) })
                        // Section 3: Classroom Access.
                        enrolledCoursesSection(enrolledPaidCourse, enrolledFreeCourses, activeLiveSessions, isTablet, onEnterClassroom = { id -> navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$id") })
                        // Section 4: Staff-Only Management Shortcuts.
                        quickActionsSection(isAdmin, isTutor, onAdminClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) }, onTutorClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_TUTOR_PANEL) })
                        // Section 5: Recent History and Activity.
                        activityRowsSection(lastViewedBooks, commentedBooks, wishlistBooks, onBookClick = { book -> viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") })
                        // Section 6: Collection Filtering and the Main Ownership Grid.
                        collectionControlsSection(isTablet = isTablet, selectedFilter = selectedFilter, filterOptions = filterOptions, filterListState = filterListState, infiniteCount = Int.MAX_VALUE, onFilterClick = { filter -> viewModel.setCollectionFilter(filter); scope.launch { gridState.animateScrollToItem(collectionIndex) } })
                        ownedBooksGrid(books = filteredOwnedBooks, purchasedIds = purchasedIds, applicationsMap = applicationsMap, isDarkTheme = isDarkTheme, isAudioPlaying = isAudioPlaying, currentPlayingBookId = currentPlayingBookId, onBookClick = { book -> viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") }, onPlayAudio = onPlayAudio, onViewInvoice = onViewInvoice, onPickupInfo = { selectedBookForPickup = it }, onRemoveFromLibrary = { viewModel.setBookToRemove(it) }, onEnterClassroom = { id -> navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$id") } )
                    }
                }
                // Floating Search Overlay.
                Box(modifier = Modifier.fillMaxWidth().padding(top = paddingValues.calculateTopPadding()), contentAlignment = Alignment.TopCenter) {
                    HomeSearchSection(isSearchVisible = isSearchVisible, searchQuery = searchQuery, recentSearches = searchHistory, onQueryChange = { viewModel.updateSearchQuery(it) }, onClearHistory = { viewModel.clearRecentSearches() }, onCloseClick = { viewModel.setSearchVisible(false) }, suggestions = suggestions, onSuggestionClick = { book -> viewModel.saveSearchQuery(book.title); viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") }, modifier = Modifier.then(if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth()).zIndex(10f))
                }
            }
        }

        // --- SECONDARY DIALOGS AND OVERLAYS --- //

        // Quick-picker for multiple classrooms.
        if (showClassroomPicker) {
            ModalBottomSheet(onDismissRequest = { showClassroomPicker = false }, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(16.dp)); Text(text = "Your Classrooms", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.height(24.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(enrolledCourses) { course ->
                            Surface(onClick = { showClassroomPicker = false; navController.navigate("${AppConstants.ROUTE_CLASSROOM}/${course.id}") }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoStories, null, tint = Color.White) } }
                                    Spacer(Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(course.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(course.author, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Financial and Library confirmation popups.
        if (showWalletHistory) { WalletHistorySheet(transactions = walletHistory, onNavigateToProduct = { id -> navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") }, onViewInvoice = { id, ref -> val route = if (ref != null) "${AppConstants.ROUTE_INVOICE_CREATING}/$id?ref=$ref" else "${AppConstants.ROUTE_INVOICE_CREATING}/$id"; navController.navigate(route) }, onDismiss = { showWalletHistory = false }) }
        if (selectedBookForPickup != null) { PickupInfoDialog(orderConfirmation = selectedBookForPickup?.orderConfirmation, onDismiss = { selectedBookForPickup = null }) }
        AppPopups.WalletTopUp(show = showPaymentPopup, user = localUser, onDismiss = { viewModel.setShowPaymentPopup(false) }, onTopUpComplete = { amount -> viewModel.topUp(amount) { msg -> viewModel.setShowPaymentPopup(false); scope.launch { snackbarHostState.showSnackbar(msg) } } }, onManageProfile = { viewModel.setShowPaymentPopup(false); navController.navigate(AppConstants.ROUTE_EDIT_PROFILE) })
        AppPopups.RemoveFromLibraryConfirmation(show = bookToRemove != null, bookTitle = bookToRemove?.title ?: "", onDismiss = { viewModel.setBookToRemove(null) }, onConfirm = { viewModel.removePurchase(bookToRemove!!) { msg -> viewModel.setBookToRemove(null); scope.launch { snackbarHostState.showSnackbar(msg) } } })
    }
}

/**
 * Factory for injecting DAOs and User Session info into the DashboardViewModel.
 */
class DashboardViewModelFactory(
    private val repository: BookRepository,
    private val userDao: UserDao,
    private val classroomDao: ClassroomDao,
    private val auditDao: AuditDao,
    private val userId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, userDao, classroomDao, auditDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
