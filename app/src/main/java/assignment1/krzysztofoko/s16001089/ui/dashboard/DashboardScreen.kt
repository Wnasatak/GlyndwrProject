package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DashboardScreen acts as the main hub for users, providing access to their profile, 
 * wallet, enrolled courses, and personal library. It adapts its UI based on the 
 * user's role (Student, Tutor, Admin).
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
        context = LocalContext.current,
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
    // Observing various data flows from the ViewModel to update the UI reactively.
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
    val latestAnnouncement by viewModel.latestAnnouncement.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()
    
    // UI visibility flags for educational/informational sections.
    val dailyInsight by viewModel.dailyInsight.collectAsState()
    val showAcademicInsight by viewModel.showAcademicInsight.collectAsState()
    val showCampusNotice by viewModel.showCampusNotice.collectAsState()

    // Search and overlay interaction states.
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val showPaymentPopup by viewModel.showPaymentPopup.collectAsState()
    val bookToRemove by viewModel.bookToRemove.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    // Notification and Messaging indicators.
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val hasMessages by viewModel.hasMessages.collectAsState()
    val unreadMessagesCount by viewModel.unreadMessagesCount.collectAsState()

    // --- UI INTERACTION STATE --- //
    var selectedBookForPickup by remember { mutableStateOf<Book?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showWalletHistory by remember { mutableStateOf(false) }
    var showClassroomPicker by remember { mutableStateOf(false) }

    // Adaptive header title behavior: expands for a few seconds on entry, then collapses.
    var showFullTitle by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(5000L) 
        showFullTitle = false
    }

    // --- RESPONSIVE CONFIGURATION --- //
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600
    val isCompact = screenWidth < 420

    val columns = if (isTablet) 2 else 1
    val gridState = rememberLazyGridState()

    // Filter enrolled items for quick-access sections.
    val enrolledPaidCourse = remember(allBooks, purchasedIds) {
        allBooks.find { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }
    }
    val enrolledFreeCourses = remember(allBooks, purchasedIds) {
        allBooks.filter { it.mainCategory == AppConstants.CAT_COURSES && it.price <= 0.0 && purchasedIds.contains(it.id) }
    }
    val enrolledCourses = remember(enrolledPaidCourse, enrolledFreeCourses) {
        listOfNotNull(enrolledPaidCourse) + enrolledFreeCourses
    }

    // Role-based visibility flags.
    val isAdmin = localUser?.role == "admin"
    val isTutor = localUser?.role == "teacher" || localUser?.role == "tutor"
    val showApplications = applicationCount > 0 && enrolledPaidCourse == null

    val collectionIndex = 8 

    // Animation: Ringing effect for the notification bell icon.
    val infiniteTransition = rememberInfiniteTransition(label = "bellRing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotation"
    )

    // Collection filtering configuration.
    val filterOptions = listOf(AppConstants.FILTER_ALL, AppConstants.FILTER_BOOKS, AppConstants.FILTER_AUDIOBOOKS, AppConstants.FILTER_GEAR, AppConstants.FILTER_COURSES)
    val filterListState = rememberLazyListState(initialFirstVisibleItemIndex = (Int.MAX_VALUE / 2))

    // Root layout container.
    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) viewModel.setSearchVisible(false) }) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // --- TOP NAVIGATION BAR --- //
                val appBarColor = MaterialTheme.colorScheme.surface
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxHeight()) {
                            // Animated Hub title that expands/collapses.
                            AnimatedContent(
                                targetState = showFullTitle,
                                transitionSpec = {
                                    (fadeIn() + expandHorizontally()).togetherWith(fadeOut() + shrinkHorizontally())
                                },
                                label = "titleAnimation"
                            ) { isFull ->
                                if (isFull) {
                                    Surface(
                                        color = appBarColor,
                                        shape = RectangleShape,
                                        modifier = Modifier
                                            .wrapContentWidth(align = Alignment.Start, unbounded = true)
                                            .zIndex(20f)
                                    ) {
                                        @Suppress("DEPRECATION")
                                        Text(
                                            text = if (isAdmin) "Admin Hub" else if (isTutor) "Tutor Hub" else "Student Hub",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(start = 0.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                                        )
                                    }
                                } else {
                                    @Suppress("DEPRECATION")
                                    Text(
                                        text = "Hub",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier
                                            .clickable { showFullTitle = true }
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        if (!showFullTitle) {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.BTN_BACK) }
                        }
                    },
                    actions = {
                        val iconSize = 40.dp
                        TopBarSearchAction(isSearchVisible = isSearchVisible, modifier = Modifier.size(iconSize)) { 
                            viewModel.setSearchVisible(true) 
                        }
                        IconButton(onClick = { navController.navigate(AppConstants.ROUTE_HOME) }, modifier = Modifier.size(iconSize)) { 
                            Icon(Icons.Rounded.Storefront, AppConstants.TITLE_STORE) 
                        }

                        // Message Hub access with unread badge.
                        if (hasMessages) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                val chatColor = if (unreadMessagesCount > 0 && isDarkTheme) Color(0xFFFFEB3B) else if (unreadMessagesCount > 0) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurface
                                IconButton(onClick = { navController.navigate(AppConstants.ROUTE_MESSAGES) }, modifier = Modifier.size(iconSize)) {
                                    Icon(imageVector = Icons.AutoMirrored.Rounded.Chat, contentDescription = AppConstants.TITLE_MESSAGES, tint = chatColor, modifier = Modifier.size(22.dp))
                                }
                                if (unreadMessagesCount > 0) {
                                    Surface(color = Color(0xFFE53935), shape = CircleShape, border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface), modifier = Modifier.size(18.dp).offset(x = 4.dp, y = (-2).dp).align(Alignment.TopEnd)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = if (unreadMessagesCount > 9) "!" else unreadMessagesCount.toString(), style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Black, lineHeight = 8.sp), color = Color.White, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }

                        // Notification Hub access with unread badge and animation.
                        Box(contentAlignment = Alignment.TopEnd) {
                            val bellColor = if (unreadCount > 0 && isDarkTheme) Color(0xFFFFEB3B) else if (unreadCount > 0) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurface
                            IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_NOTIFICATIONS) }, modifier = Modifier.size(iconSize)) {
                                Icon(imageVector = if (unreadCount > 0) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications, contentDescription = AppConstants.TITLE_NOTIFICATIONS, tint = bellColor, modifier = Modifier.size(24.dp).graphicsLayer { if (unreadCount > 0) { rotationZ = rotation } })
                            }
                            if (unreadCount > 0) {
                                Surface(color = Color(0xFFE53935), shape = CircleShape, border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface), modifier = Modifier.size(18.dp).offset(x = 4.dp, y = (-2).dp).align(Alignment.TopEnd)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = if (unreadCount > 9) "!" else unreadCount.toString(), style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Black, lineHeight = 8.sp), color = Color.White, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, onOpenCustomBuilder = onOpenThemeBuilder, isLoggedIn = true, modifier = Modifier.size(iconSize))

                        // Overflow menu for secondary dashboard actions.
                        Box {
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(iconSize)) { Icon(Icons.Rounded.MoreVert, AppConstants.TITLE_MORE_OPTIONS) }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.width(220.dp)) {
                                @Suppress("DEPRECATION")
                                Text(text = "DASHBOARD OPTIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                                HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                DropdownMenuItem(text = { Text("My Profile") }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_PROFILE) }, leadingIcon = { Icon(Icons.Rounded.AccountCircle, null) })
                                if (enrolledCourses.isNotEmpty()) {
                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_CLASSROOM) }, onClick = { showMenu = false; showClassroomPicker = true }, leadingIcon = { Icon(Icons.Rounded.School, null) })
                                }
                                DropdownMenuItem(text = { Text(AppConstants.TITLE_MESSAGES, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_MESSAGES) }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Chat, null) })
                                DropdownMenuItem(text = { Text(AppConstants.TITLE_PROFILE_SETTINGS, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_EDIT_PROFILE) }, leadingIcon = { Icon(Icons.Rounded.Settings, null) })
                                DropdownMenuItem(text = { Text("About App", style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_ABOUT) }, leadingIcon = { Icon(Icons.Rounded.Info, null) })

                                if (isAdmin) { DropdownMenuItem(text = { Text(AppConstants.TITLE_ADMIN_PANEL, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) }, leadingIcon = { Icon(Icons.Rounded.AdminPanelSettings, null) }) }
                                if (isTutor) { DropdownMenuItem(text = { Text(AppConstants.TITLE_TUTOR_PANEL, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; navController.navigate(AppConstants.ROUTE_TUTOR_PANEL) }, leadingIcon = { Icon(Icons.Rounded.School, null) }) }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                DropdownMenuItem(text = { Text("Sign Off", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium) }, onClick = { showMenu = false; onLogout() }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) })
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarColor)
                )
            }
        ) { paddingValues ->
            // --- MAIN SCROLLABLE CONTENT --- //
            Box(modifier = Modifier.fillMaxSize()) {
                AdaptiveScreenContainer(
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                    maxWidth = AdaptiveWidths.Wide
                ) { screenIsTablet ->
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = if (screenIsTablet) Arrangement.spacedBy(16.dp) else Arrangement.Start
                    ) {
                        // Section 1: User Profile & Wallet Header.
                        dashboardHeaderSection(
                            user = localUser, isTablet = screenIsTablet, onProfileClick = { navController.navigate(AppConstants.ROUTE_PROFILE) },
                            onTopUp = { viewModel.setSearchVisible(false); viewModel.setShowPaymentPopup(true) },
                            onViewHistory = { showWalletHistory = true }
                        )

                        // Section 2: Educational Tips (Academic Insights).
                        if (showAcademicInsight) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp), 
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(currentTip.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                @Suppress("DEPRECATION")
                                                Text("APP USAGE TIP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                                            }
                                            Row {
                                                IconButton(onClick = { viewModel.nextAppTip() }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Refresh, "Next Tip")
                                                }
                                                Spacer(Modifier.width(8.dp))
                                                IconButton(onClick = { viewModel.dismissAcademicInsight() }, modifier = Modifier.size(24.dp)) { 
                                                    Icon(Icons.Default.Close, "Dismiss") 
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        @Suppress("DEPRECATION")
                                        Text(text = currentTip.content, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
                                    }
                                }
                            }
                        }

                        // Section 3: Campus Announcements (Broadcast Notices).
                        if (showCampusNotice) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                    LegacyCampusNotice(
                                        title = latestAnnouncement?.title ?: "CAMPUS OFFICIAL NOTICE",
                                        content = latestAnnouncement?.message ?: "Welcome to the Student Hub! Check here for important university updates.",
                                        onAcknowledge = { viewModel.dismissCampusNotice() }
                                    )
                                    IconButton(
                                        onClick = { viewModel.dismissCampusNotice() }, 
                                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                                    ) { 
                                        Icon(Icons.Default.Close, null) 
                                    }
                                }
                            }
                        }

                        // Section 4: Course Enrolments and Academic Progress.
                        applicationsSection(showApplications, screenIsTablet, onClick = { navController.navigate(AppConstants.ROUTE_MY_APPLICATIONS) })
                        enrolledCoursesSection(enrolledPaidCourse, enrolledFreeCourses, activeLiveSessions, screenIsTablet, onEnterClassroom = { id -> navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$id") })
                        quickActionsSection(isAdmin, isTutor, onAdminClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) }, onTutorClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_TUTOR_PANEL) })
                        activityRowsSection(lastViewedBooks, commentedBooks, wishlistBooks, onBookClick = { book -> viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") })
                        
                        // Section 5: Personal Collection & Filtering.
                        collectionControlsSection(isTablet = screenIsTablet, selectedFilter = selectedFilter, filterOptions = filterOptions, filterListState = filterListState, infiniteCount = Int.MAX_VALUE, onFilterClick = { filter -> viewModel.setCollectionFilter(filter); scope.launch { gridState.animateScrollToItem(collectionIndex) } })
                        ownedBooksGrid(books = filteredOwnedBooks, purchasedIds = purchasedIds, applicationsMap = applicationsMap, isDarkTheme = isDarkTheme, isAudioPlaying = isAudioPlaying, currentPlayingBookId = currentPlayingBookId, onBookClick = { book -> viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") }, onPlayAudio = onPlayAudio, onViewInvoice = onViewInvoice, onPickupInfo = { selectedBookForPickup = it }, onRemoveFromLibrary = { viewModel.setBookToRemove(it) }, onEnterClassroom = { id -> navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$id") } )
                        
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
                // Floating Search Overlay.
                Box(modifier = Modifier.fillMaxWidth().padding(top = paddingValues.calculateTopPadding()), contentAlignment = Alignment.TopCenter) {
                    HomeSearchSection(isSearchVisible = isSearchVisible, searchQuery = searchQuery, recentSearches = searchHistory, onQueryChange = { viewModel.updateSearchQuery(it) }, onClearHistory = { viewModel.clearRecentSearches() }, onCloseClick = { viewModel.setSearchVisible(false) }, suggestions = suggestions, onSuggestionClick = { book -> viewModel.saveSearchQuery(book.title); viewModel.setSearchVisible(false); navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") }, modifier = Modifier.then(if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth()).zIndex(10f))
                }
            }
        }

        // --- SECONDARY UI LAYERS (Sheets & Dialogs) --- //

        // Classroom picker sheet.
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

        // Wallet history, Top-up flow, and library removal confirmation.
        if (showWalletHistory) { WalletHistorySheet(transactions = walletHistory, onNavigateToProduct = { id -> navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") }, onViewInvoice = { id, ref -> val route = if (ref != null) "${AppConstants.ROUTE_INVOICE_CREATING}/$id?ref=$ref" else "${AppConstants.ROUTE_INVOICE_CREATING}/$id"; navController.navigate(route) }, onDismiss = { showWalletHistory = false }) }
        if (selectedBookForPickup != null) { PickupInfoDialog(orderConfirmation = selectedBookForPickup?.orderConfirmation, onDismiss = { selectedBookForPickup = null }) }
        AppPopups.WalletTopUp(show = showPaymentPopup, user = localUser, onDismiss = { viewModel.setShowPaymentPopup(false) }, onTopUpComplete = { amount -> viewModel.topUp(amount) { msg -> viewModel.setShowPaymentPopup(false); scope.launch { snackbarHostState.showSnackbar(msg) } } }, onManageProfile = { viewModel.setShowPaymentPopup(false); navController.navigate(AppConstants.ROUTE_EDIT_PROFILE) })
        AppPopups.RemoveFromLibraryConfirmation(show = bookToRemove != null, bookTitle = bookToRemove?.title ?: "", onDismiss = { viewModel.setBookToRemove(null) }, onConfirm = { viewModel.removePurchase(bookToRemove!!) { msg -> viewModel.setBookToRemove(null); scope.launch { snackbarHostState.showSnackbar(msg) } } })
    }
}

private fun getCampusTip(raw: String?): String {
    if (raw == null) return "Connecting to the University Knowledge Hub..."
    return "Campus Instruction: $raw."
}
