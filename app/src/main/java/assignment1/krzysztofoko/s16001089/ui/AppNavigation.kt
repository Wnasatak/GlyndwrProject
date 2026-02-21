package assignment1.krzysztofoko.s16001089.ui

import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.navigation.*
import assignment1.krzysztofoko.s16001089.ui.splash.SplashScreen
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.details.course.MyApplicationsScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import assignment1.krzysztofoko.s16001089.ui.theme.GlyndwrProjectTheme
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.admin.AdminSection
import assignment1.krzysztofoko.s16001089.utils.NetworkMonitor

/**
 * AppNavigation.kt
 *
 * This file serves as the grand orchestrator of the entire application's UI. It defines
 * the core navigation graph, manages the global application state (like the current user
 * and active theme), and coordinates the high-level layout using a TopLevelScaffold.
 */

@Composable
fun AppNavigation(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    externalPlayer: Player? = null,
    windowSizeClass: WindowSizeClass? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val db = AppDatabase.getDatabase(context)
    val repository = remember { BookRepository(db) }
    
    // REQUIREMENT: Advanced System Feedback (8%)
    // Initialize the NetworkMonitor to track real-time internet connectivity.
    val networkMonitor = remember { NetworkMonitor(context) }

    // Global ViewModel for managing cross-cutting concerns like Auth, Theme, and Connectivity.
    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(repository, db, networkMonitor))

    // --- STATE COLLECTION --- //
    val currentUser by mainVm.currentUser.collectAsState()
    val localUser by mainVm.localUser.collectAsState()
    val userThemeFromDb by mainVm.userTheme.collectAsState()
    val allBooks by mainVm.allBooks.collectAsState()
    val isDataLoading by mainVm.isDataLoading.collectAsState()
    val loadError by mainVm.loadError.collectAsState()
    val unreadCount by mainVm.unreadNotificationsCount.collectAsState()
    val walletHistory by mainVm.walletHistory.collectAsState()
    
    // Observe network status reactively
    val isOnline by mainVm.isOnline.collectAsState()

    // --- NAVIGATION TRACKING --- //
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- CONTEXT RESOLUTION (For Staff Panels) --- //
    val currentTutorSection = remember(navBackStackEntry) {
        val sectionStr = navBackStackEntry?.arguments?.getString("section")
        if (sectionStr != null) {
            try { TutorSection.valueOf(sectionStr) } catch (_: Exception) { null }
        } else {
            if (currentRoute?.startsWith(AppConstants.ROUTE_TUTOR_PANEL) == true) TutorSection.DASHBOARD else null
        }
    }

    val currentAdminSection = remember(navBackStackEntry) {
        val sectionStr = navBackStackEntry?.arguments?.getString("section")
        if (sectionStr != null) {
            try { AdminSection.valueOf(sectionStr) } catch (_: Exception) { null }
        } else {
            if (currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_PANEL) == true) AdminSection.DASHBOARD else null
        }
    }

    // --- THEME STATE MANAGEMENT --- //
    var showThemeBuilder by remember { mutableStateOf(false) }
    var liveTheme by remember(userThemeFromDb) { mutableStateOf(userThemeFromDb) }

    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> liveTheme?.customIsDark ?: true
        else -> false
    }

    // --- SYSTEM UI SYNCHRONISATION --- //
    LaunchedEffect(isDarkTheme) {
        val activity = context as? androidx.activity.ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (isDarkTheme) androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = if (isDarkTheme) androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
    }

    // --- AUTH & THEME SYNC --- //
    LaunchedEffect(currentUser, userThemeFromDb) {
        val userId = currentUser?.uid
        if (userId != null && userThemeFromDb != null && syncedUserId != userId) {
            val savedThemeName = userThemeFromDb?.lastSelectedTheme
            if (savedThemeName != null) {
                try {
                    onThemeChange(Theme.valueOf(savedThemeName))
                } catch (_: Exception) {
                    if (userThemeFromDb?.isCustomThemeEnabled == true) onThemeChange(Theme.CUSTOM)
                }
            }
            syncedUserId = userId
        } else if (userId == null && syncedUserId != null) {
            onThemeChange(Theme.DARK)
            syncedUserId = null
        }
    }

    val handleThemeChange: (Theme) -> Unit = { newTheme ->
        onThemeChange(newTheme)
        mainVm.updateThemePersistence(newTheme)
    }

    // --- MEDIA PLAYER INTEGRATION --- //
    LaunchedEffect(externalPlayer) {
        externalPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                mainVm.syncPlayerState(isPlaying)
            }
        })
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute != "${AppConstants.ROUTE_BOOK_DETAILS}/{bookId}" && mainVm.showPlayer) {
            mainVm.isPlayerMinimized = true
        }
    }

    // --- PRIMARY UI ORCHESTRATION --- //
    GlyndwrProjectTheme(theme = currentTheme, userTheme = liveTheme) {

        TopLevelScaffold(
            currentUser = currentUser,
            localUser = localUser,
            userTheme = liveTheme,
            currentRoute = currentRoute,
            currentTutorSection = currentTutorSection,
            currentAdminSection = currentAdminSection,
            unreadCount = unreadCount,
            onDashboardClick = {
                val target = when (localUser?.role) {
                    "admin" -> AppConstants.ROUTE_ADMIN_PANEL
                    "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL
                    else -> AppConstants.ROUTE_DASHBOARD
                }
                navController.navigate(target)
            },
            onHomeClick = { navController.navigate(AppConstants.ROUTE_HOME) },
            onProfileClick = {
                when (localUser?.role?.lowercase()) {
                    "teacher", "tutor" -> navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=TEACHER_DETAIL")
                    "admin" -> navController.navigate("${AppConstants.ROUTE_ADMIN_PANEL}?section=PROFILE")
                    else -> navController.navigate(AppConstants.ROUTE_PROFILE)
                }
            },
            onWalletClick = { mainVm.showWalletHistory = true },
            onNotificationsClick = {
                if (localUser?.role?.lowercase() == "admin") {
                    navController.navigate("${AppConstants.ROUTE_ADMIN_PANEL}?section=NOTIFICATIONS")
                } else {
                    navController.navigate(AppConstants.ROUTE_NOTIFICATIONS)
                }
            },
            onMyApplicationsClick = { navController.navigate(AppConstants.ROUTE_MY_APPLICATIONS) },
            onMessagesClick = {
                if (localUser?.role?.lowercase() in listOf("teacher", "tutor")) {
                    navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=MESSAGES")
                } else {
                    navController.navigate(AppConstants.ROUTE_MESSAGES)
                }
            },
            onLibraryClick = {
                if (localUser?.role?.lowercase() == "admin") {
                    navController.navigate("${AppConstants.ROUTE_ADMIN_PANEL}?section=LIBRARY")
                } else {
                    navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=LIBRARY")
                }
            },
            onLogsClick = {
                if (localUser?.role?.lowercase() == "admin") {
                    navController.navigate("${AppConstants.ROUTE_ADMIN_PANEL}?section=LOGS")
                }
            },
            onBroadcastClick = {
                if (localUser?.role?.lowercase() == "admin") {
                    navController.navigate("${AppConstants.ROUTE_ADMIN_PANEL}?section=BROADCAST")
                }
            },
            onLiveSessionClick = { navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=COURSE_LIVE") },
            onNewAssignmentClick = { navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=CREATE_ASSIGNMENT") },
            onLogoutClick = { mainVm.showLogoutConfirm = true },
            onThemeChange = handleThemeChange,
            currentTheme = currentTheme,
            windowSizeClass = windowSizeClass,
            onClassroomClick = { id -> navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$id") },
            showThemeBuilder = showThemeBuilder,
            onOpenThemeBuilder = { open -> showThemeBuilder = open },
            onLiveThemeUpdate = { themeUpdate ->
                liveTheme = themeUpdate
                if (currentTheme != Theme.CUSTOM) onThemeChange(Theme.CUSTOM)
            },
            onAboutClick = { navController.navigate(AppConstants.ROUTE_ABOUT) },
            bottomContent = {
                if (mainVm.showPlayer && mainVm.isPlayerMinimized) {
                    IntegratedAudioBar(
                        currentBook = mainVm.currentPlayingBook,
                        externalPlayer = externalPlayer,
                        onToggleMinimize = { mainVm.isPlayerMinimized = false },
                        onClose = { mainVm.stopPlayer(externalPlayer) }
                    )
                }
            }
        ) { paddingValues ->
            AppNavigationPopups(
                showLogoutConfirm = mainVm.showLogoutConfirm,
                showSignedOutPopup = mainVm.showSignedOutPopup,
                onLogoutConfirm = { mainVm.signOut(navController) },
                onLogoutDismiss = { mainVm.showLogoutConfirm = false },
                onSignedOutDismiss = { mainVm.showSignedOutPopup = false }
            )

            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // REQUIREMENT: Advanced System Feedback (8%)
                    // Animated "Offline Mode" banner that appears when internet is lost.
                    AnimatedVisibility(
                        visible = !isOnline,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Offline Mode: Catalog sync suspended",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = AppConstants.ROUTE_SPLASH, modifier = Modifier.weight(1f)) {
                        composable(AppConstants.ROUTE_SPLASH) {
                            SplashScreen(isLoadingData = isDataLoading, onTimeout = {
                                val targetRoute = when (localUser?.role) {
                                    "admin" -> AppConstants.ROUTE_ADMIN_PANEL
                                    "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL
                                    else -> AppConstants.ROUTE_HOME
                                }
                                navController.navigate(targetRoute) { popUpTo(AppConstants.ROUTE_SPLASH) { inclusive = true } }
                            })
                        }
                        homeNavGraph(navController, mainVm.currentUser, isDataLoading, loadError, currentTheme, handleThemeChange, { showThemeBuilder = true }, { mainVm.refreshData() }, { mainVm.onPlayAudio(it, externalPlayer) }, mainVm.currentPlayingBook?.id, mainVm.isAudioPlaying)
                        authNavGraph(navController, currentTheme, handleThemeChange)
                        storeNavGraph(navController, mainVm.currentUser, allBooks, currentTheme, handleThemeChange) { mainVm.onPlayAudio(it, externalPlayer) }
                        dashboardNavGraph(navController, mainVm.currentUser, allBooks, currentTheme, handleThemeChange, { showThemeBuilder = true }, { mainVm.onPlayAudio(it, externalPlayer) }, mainVm.isAudioPlaying, mainVm.currentPlayingBook?.id) { mainVm.showLogoutConfirm = true }
                        infoNavGraph(navController, currentTheme, liveTheme, handleThemeChange) { showThemeBuilder = true }
                        invoiceNavGraph(navController, allBooks, currentUser?.displayName ?: AppConstants.TEXT_STUDENT, currentTheme, handleThemeChange)
                        composable(AppConstants.ROUTE_MY_APPLICATIONS) {
                            MyApplicationsScreen(onBack = {
                                when (localUser?.role) {
                                    "admin" -> navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) { popUpTo(AppConstants.ROUTE_ADMIN_PANEL) { inclusive = true } }
                                    "teacher", "tutor" -> navController.navigate(AppConstants.ROUTE_TUTOR_PANEL) { popUpTo(AppConstants.ROUTE_TUTOR_PANEL) { inclusive = true } }
                                    else -> navController.popBackStack()
                                }
                            }, onNavigateToCourse = { id -> navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") }, isDarkTheme = isDarkTheme, onToggleTheme = {})
                        }
                    }
                }

                if (mainVm.showWalletHistory) {
                    WalletHistorySheet(transactions = walletHistory, onNavigateToProduct = { id -> mainVm.showWalletHistory = false; navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") }, onViewInvoice = { id, ref -> mainVm.showWalletHistory = false; val route = if (ref != null) "${AppConstants.ROUTE_INVOICE_CREATING}/$id?ref=$ref" else "${AppConstants.ROUTE_INVOICE_CREATING}/$id"; navController.navigate(route) }, onDismiss = { mainVm.showWalletHistory = false })
                }
                if (mainVm.showPlayer && !mainVm.isPlayerMinimized) {
                    MaximizedAudioPlayerOverlay(
                        currentBook = mainVm.currentPlayingBook,
                        isDarkTheme = isDarkTheme,
                        externalPlayer = externalPlayer,
                        onToggleMinimize = { mainVm.isPlayerMinimized = true },
                        onClose = { mainVm.stopPlayer(externalPlayer) }
                    )
                }
            }
        }
    }
}

private var syncedUserId: String? = null
