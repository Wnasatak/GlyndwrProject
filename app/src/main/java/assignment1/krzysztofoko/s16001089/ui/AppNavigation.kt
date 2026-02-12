package assignment1.krzysztofoko.s16001089.ui

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

/**
 * AppNavigation is the central orchestrator for the application's UI hierarchy and state.
 * It integrates navigation, theme management, media playback control, and user session monitoring.
 *
 * Primary Responsibilities:
 * 1. Routing Management: Using Jetpack Navigation (NavHost) to manage all screen transitions.
 * 2. State Synchronization: Coordinating data between the MainViewModel and the UI.
 * 3. Dynamic Theming: Handling transitions between standard (Dark/Light) and user-defined custom themes.
 * 4. System Integration: Managing edge-to-edge status bars and adaptive window sizing.
 * 5. Global UI Components: Integrating the shared TopLevelScaffold, Audio Player, and App Popups.
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
    
    // Core ViewModel for global app state management
    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(repository, db))

    // REACTIVE STATE: Synchronized with the MainViewModel's data flows
    val currentUser by mainVm.currentUser.collectAsState()
    val localUser by mainVm.localUser.collectAsState()
    val userThemeFromDb by mainVm.userTheme.collectAsState()
    val allBooks by mainVm.allBooks.collectAsState()
    val isDataLoading by mainVm.isDataLoading.collectAsState()
    val loadError by mainVm.loadError.collectAsState()
    val unreadCount by mainVm.unreadNotificationsCount.collectAsState()
    val walletHistory by mainVm.walletHistory.collectAsState()

    // NAVIGATION TRACKING: Identifies the active screen and current portal section
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // CONTEXT RESOLUTION: Determines if the user is in a specific Tutor portal section
    val currentTutorSection = remember(navBackStackEntry) {
        val sectionStr = navBackStackEntry?.arguments?.getString("section")
        if (sectionStr != null) {
            try { TutorSection.valueOf(sectionStr) } catch (e: Exception) { null }
        } else {
            // Default to Dashboard if within the Tutor Panel route
            if (currentRoute?.startsWith(AppConstants.ROUTE_TUTOR_PANEL) == true) TutorSection.DASHBOARD else null
        }
    }
    
    // THEME STATE: Managed locally for real-time preview during customization
    var showThemeBuilder by remember { mutableStateOf(false) }
    var liveTheme by remember(userThemeFromDb) { mutableStateOf(userThemeFromDb) }
    
    // RESOLVE DARK MODE: Derived from the current theme and custom user settings
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> liveTheme?.customIsDark ?: true
        else -> false
    }

    // SYSTEM UI SYNC: Updates the status and navigation bar styles to match the active theme
    LaunchedEffect(isDarkTheme) {
        val activity = context as? ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (isDarkTheme) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = if (isDarkTheme) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
    }

    // AUTH & THEME SYNC: Ensures the user's preferred theme is loaded upon sign-in
    LaunchedEffect(currentUser, userThemeFromDb) {
        val userId = currentUser?.uid
        if (userId != null && userThemeFromDb != null && syncedUserId != userId) {
            val savedThemeName = userThemeFromDb?.lastSelectedTheme
            if (savedThemeName != null) {
                try { onThemeChange(Theme.valueOf(savedThemeName)) } catch (e: Exception) { if (userThemeFromDb?.isCustomThemeEnabled == true) onThemeChange(Theme.CUSTOM) }
            }
            syncedUserId = userId
        } else if (userId == null && syncedUserId != null) {
            // Revert to system default on sign-out
            onThemeChange(Theme.DARK)
            syncedUserId = null
        }
    }

    /** Helper to handle theme changes and persist them to the database. */
    val handleThemeChange: (Theme) -> Unit = { newTheme ->
        onThemeChange(newTheme)
        mainVm.updateThemePersistence(newTheme)
    }

    // MEDIA SYNC: Connects the Media3 player lifecycle to the MainViewModel for state tracking
    LaunchedEffect(externalPlayer) {
        externalPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { mainVm.syncPlayerState(isPlaying) }
        })
    }

    // UI LOGIC: Automatically minimizes the player when navigating away from the details screen
    LaunchedEffect(currentRoute) {
        if (currentRoute != "${AppConstants.ROUTE_BOOK_DETAILS}/{bookId}" && mainVm.showPlayer) {
            mainVm.isPlayerMinimized = true
        }
    }

    // COMPOSITION ROOT: The entire app is wrapped in the Branded Theme and Global Scaffold
    GlyndwrProjectTheme(theme = currentTheme, userTheme = liveTheme) {
        TopLevelScaffold(
            currentUser = currentUser,
            localUser = localUser,
            userTheme = liveTheme,
            currentRoute = currentRoute,
            currentTutorSection = currentTutorSection,
            unreadCount = unreadCount,
            // PORTAL ROUTING: Redirects based on User Role (Admin, Tutor, or Student)
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
                if (localUser?.role?.lowercase() in listOf("teacher", "tutor")) {
                    navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=TEACHER_DETAIL")
                } else {
                    navController.navigate(AppConstants.ROUTE_PROFILE) 
                }
            },
            onWalletClick = { mainVm.showWalletHistory = true },
            onNotificationsClick = { navController.navigate(AppConstants.ROUTE_NOTIFICATIONS) },
            onMyApplicationsClick = { navController.navigate(AppConstants.ROUTE_MY_APPLICATIONS) },
            onMessagesClick = { 
                if (localUser?.role?.lowercase() in listOf("teacher", "tutor")) {
                    navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=MESSAGES")
                } else {
                    navController.navigate(AppConstants.ROUTE_MESSAGES) 
                }
            },
            onLibraryClick = { navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=LIBRARY") },
            onLiveSessionClick = { navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=COURSE_LIVE") },
            onNewAssignmentClick = { navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=CREATE_ASSIGNMENT") },
            onLogoutClick = { mainVm.showLogoutConfirm = true }, 
            onThemeChange = handleThemeChange,
            currentTheme = currentTheme,
            windowSizeClass = windowSizeClass,
            onClassroomClick = { id -> navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$id") },
            showThemeBuilder = showThemeBuilder,
            onOpenThemeBuilder = { showThemeBuilder = it },
            onLiveThemeUpdate = { 
                liveTheme = it 
                if (currentTheme != Theme.CUSTOM) onThemeChange(Theme.CUSTOM)
            },
            onAboutClick = { navController.navigate(AppConstants.ROUTE_ABOUT) },
            // PERSISTENT PLAYER: Minimized audio control bar at the bottom of the scaffold
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
            // GLOBAL POPUPS: Manages high-level dialogs like Logout confirmation
            AppNavigationPopups(
                showLogoutConfirm = mainVm.showLogoutConfirm,
                showSignedOutPopup = mainVm.showSignedOutPopup,
                onLogoutConfirm = { mainVm.signOut(navController) },
                onLogoutDismiss = { mainVm.showLogoutConfirm = false },
                onSignedOutDismiss = { mainVm.showSignedOutPopup = false }
            )

            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // CORE NAVIGATION HOST: Defines all modular navigation graphs
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
                    // Modular Nav Graphs for specific app zones
                    homeNavGraph(navController, mainVm.currentUser, isDataLoading, loadError, currentTheme, handleThemeChange, { showThemeBuilder = true }, { mainVm.refreshData() }, { mainVm.onPlayAudio(it, externalPlayer) }, mainVm.currentPlayingBook?.id, mainVm.isAudioPlaying)
                    authNavGraph(navController, currentTheme, handleThemeChange)
                    storeNavGraph(navController, mainVm.currentUser, allBooks, currentTheme, handleThemeChange, { mainVm.onPlayAudio(it, externalPlayer) })
                    dashboardNavGraph(navController, mainVm.currentUser, allBooks, currentTheme, handleThemeChange, { showThemeBuilder = true }, { mainVm.onPlayAudio(it, externalPlayer) }, mainVm.isAudioPlaying, mainVm.currentPlayingBook?.id, { mainVm.showLogoutConfirm = true })
                    infoNavGraph(navController, currentTheme, handleThemeChange, { showThemeBuilder = true })
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

                // BOTTOM SHEET: Transactional wallet history overlay
                if (mainVm.showWalletHistory) {
                    WalletHistorySheet(transactions = walletHistory, onNavigateToProduct = { id -> mainVm.showWalletHistory = false; navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") }, onViewInvoice = { id, ref -> mainVm.showWalletHistory = false; val route = if (ref != null) "${AppConstants.ROUTE_INVOICE_CREATING}/$id?ref=$ref" else "${AppConstants.ROUTE_INVOICE_CREATING}/$id"; navController.navigate(route) }, onDismiss = { mainVm.showWalletHistory = false })
                }

                // MAXIMIZED PLAYER: Full-screen overlay providing deep media control
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

/** Tracks the currently synced user to manage theme re-loading. */
private var syncedUserId: String? = null
