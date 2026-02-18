package assignment1.krzysztofoko.s16001089.ui

import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

/**
 * AppNavigation.kt
 *
 * This file serves as the grand orchestrator of the entire application's UI. It defines
 * the core navigation graph, manages the global application state (like the current user
 * and active theme), and coordinates the high-level layout using a TopLevelScaffold.
 */

/**
 * AppNavigation Composable
 *
 * The root-level component that brings together navigation, state management, and theme orchestration.
 *
 * Key features:
 * - **Unified Navigation Graph:** Centralises all application routes using a single NavHost.
 * - **Reactive State Management:** Observes the [MainViewModel] to reactively update the UI based on
 *   authentication status, data loading progress, and unread notification counts.
 * - **Dynamic Theme Orchestration:** Synchronises persistent user theme preferences from the database
 *   with the active UI session, supporting Light, Dark, and fully customisable themes.
 * - **Adaptive Role-Based UI:** Intelligent redirection and UI adjustments based on the user's role
 *   (Student, Tutor, Admin).
 * - **Persistent Global Overlays:** Manages high-level UI elements that span multiple screens,
 *   such as the global audio player and wallet history sheet.
 */
@Composable
fun AppNavigation(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    externalPlayer: Player? = null,
    windowSizeClass: WindowSizeClass? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController() // Initialise the primary navigation engine.
    val db = AppDatabase.getDatabase(context)
    val repository = remember { BookRepository(db) }

    // Global ViewModel for managing cross-cutting concerns like Auth and Theme persistence.
    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(repository, db))

    // --- STATE COLLECTION --- //
    val currentUser by mainVm.currentUser.collectAsState()
    val localUser by mainVm.localUser.collectAsState()
    val userThemeFromDb by mainVm.userTheme.collectAsState()
    val allBooks by mainVm.allBooks.collectAsState()
    val isDataLoading by mainVm.isDataLoading.collectAsState()
    val loadError by mainVm.loadError.collectAsState()
    val unreadCount by mainVm.unreadNotificationsCount.collectAsState()
    val walletHistory by mainVm.walletHistory.collectAsState()

    // --- NAVIGATION TRACKING --- //
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- CONTEXT RESOLUTION (For Staff Panels) --- //
    // Determines which sub-section of the staff portals is currently active.
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

    // Calculate if the UI should be dark based on user selection or custom theme parameters.
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> liveTheme?.customIsDark ?: true
        else -> false
    }

    // --- SYSTEM UI SYNCHRONISATION --- //
    // Automatically adjust status and navigation bar colours to match the current theme.
    LaunchedEffect(isDarkTheme) {
        val activity = context as? androidx.activity.ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (isDarkTheme) androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = if (isDarkTheme) androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
    }

    // --- AUTH & THEME SYNC --- //
    // Load the user's saved theme from the database upon successful login.
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
            // Reset to default on logout.
            onThemeChange(Theme.DARK)
            syncedUserId = null
        }
    }

    // Handle theme changes by updating both the session and persistent storage.
    val handleThemeChange: (Theme) -> Unit = { newTheme ->
        onThemeChange(newTheme)
        mainVm.updateThemePersistence(newTheme)
    }

    // --- MEDIA PLAYER INTEGRATION --- //
    // Link the external ExoPlayer state to the global UI state.
    LaunchedEffect(externalPlayer) {
        externalPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                mainVm.syncPlayerState(isPlaying)
            }
        })
    }

    // Minimise the player automatically when navigating away from the details screen.
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

            // NAVIGATION CALLBACKS //
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
                // Persistent mini-player bar.
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
            // --- POPUPS & MODALS --- //
            AppNavigationPopups(
                showLogoutConfirm = mainVm.showLogoutConfirm,
                showSignedOutPopup = mainVm.showSignedOutPopup,
                onLogoutConfirm = { mainVm.signOut(navController) },
                onLogoutDismiss = { mainVm.showLogoutConfirm = false },
                onSignedOutDismiss = { mainVm.showSignedOutPopup = false }
            )

            // --- NAVIGATION HOST: MAIN SCREEN CONTENT --- //
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                NavHost(navController = navController, startDestination = AppConstants.ROUTE_SPLASH, modifier = Modifier.fillMaxSize()) {

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

                    // Grouped Route Definitions.
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

                // --- GLOBAL OVERLAYS --- //

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

// Internal reference for auth state synchronisation.
private var syncedUserId: String? = null
