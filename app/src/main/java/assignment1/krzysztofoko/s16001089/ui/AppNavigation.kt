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
import assignment1.krzysztofoko.s16001089.ui.details.pdf.PdfReaderScreen
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
 *
 * It acts as the "Main Activity" content equivalent in Jetpack Compose, handling:
 * 1. Navigation routing via NavHost and NavController.
 * 2. Theme persistence (Room DB) and real-time color updates.
 * 3. Global audio player visibility and lifecycle management (Media3).
 * 4. Network connectivity monitoring and reactive UI feedback (Online/Offline status).
 * 5. Role-based access control (RBAC) for navigation targets (Student vs Staff vs Admin).
 */

@Composable
fun AppNavigation(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    externalPlayer: Player? = null,
    windowSizeClass: WindowSizeClass? = null
) {
    // --- INITIALIZATION & DEPENDENCY SETUP --- //
    val context = LocalContext.current
    
    // Create the central NavController that tracks the screen backstack.
    val navController = rememberNavController()
    
    // Database and Repository initialization (Scoped to this Composable's lifecycle)
    val db = AppDatabase.getDatabase(context)
    val repository = remember { BookRepository(db) }
    
    // REQUIREMENT: Advanced System Feedback (8%)
    // Initialize the NetworkMonitor to track real-time internet connectivity.
    // This allows the app to reactively show offline warnings to the user.
    val networkMonitor = remember { NetworkMonitor(context) }

    // Global ViewModel for managing cross-cutting concerns like Auth, Theme, and Connectivity.
    // We use a custom Factory to inject the Room database and Network monitor dependencies.
    // This instance is shared across the entire navigation graph.
    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(repository, db, networkMonitor))

    // --- STATE COLLECTION (Reactive UI) --- //
    // Collecting various UI states from the MainViewModel. Using 'collectAsState' ensures
    // that this composable recomposes whenever the underlying StateFlow emits a new value.
    val currentUser by mainVm.currentUser.collectAsState() // Firebase Auth user (for session management)
    val localUser by mainVm.localUser.collectAsState()     // User data from local Room DB (roles, names, permissions)
    val userThemeFromDb by mainVm.userTheme.collectAsState() // User's persisted theme settings (custom colors etc.)
    val allBooks by mainVm.allBooks.collectAsState()         // Catalog of available books/courses fetched from DB/Cloud
    val isDataLoading by mainVm.isDataLoading.collectAsState() // Global loading state for splash and syncing
    val loadError by mainVm.loadError.collectAsState()         // Global error state for data fetching
    val unreadCount by mainVm.unreadNotificationsCount.collectAsState() // Badge count for notification icons
    val walletHistory by mainVm.walletHistory.collectAsState() // List of transactions for the current user
    
    // Observe network status reactively to show the "Offline" banner.
    val isOnline by mainVm.isOnline.collectAsState()

    // --- NAVIGATION TRACKING --- //
    // Observing the current backstack entry allows the UI to adapt based on which screen is visible.
    // We use 'currentRoute' to decide which items to highlight in the navigation drawer or bottom rail.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- CONTEXT RESOLUTION (For Staff Panels) --- //
    // The Tutor and Admin panels use a single route structure with a 'section' argument.
    // We resolve the active section here so that the TopLevelScaffold can highlight the correct drawer item.
    
    // Extract current Tutor Section from route arguments (e.g., DASHBOARD, LIBRARY, MESSAGES)
    val currentTutorSection = remember(navBackStackEntry) {
        val sectionStr = navBackStackEntry?.arguments?.getString("section")
        if (sectionStr != null) {
            try { TutorSection.valueOf(sectionStr) } catch (_: Exception) { null }
        } else {
            // Default to DASHBOARD if in the Tutor Panel route but no specific section is in the args.
            if (currentRoute?.startsWith(AppConstants.ROUTE_TUTOR_PANEL) == true) TutorSection.DASHBOARD else null
        }
    }

    // Extract current Admin Section from route arguments (e.g., DASHBOARD, LOGS, USERS)
    val currentAdminSection = remember(navBackStackEntry) {
        val sectionStr = navBackStackEntry?.arguments?.getString("section")
        if (sectionStr != null) {
            try { AdminSection.valueOf(sectionStr) } catch (_: Exception) { null }
        } else {
            // Default to DASHBOARD if in the Admin Panel route but no specific section is in the args.
            if (currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_PANEL) == true) AdminSection.DASHBOARD else null
        }
    }

    // --- THEME STATE MANAGEMENT --- //
    var showThemeBuilder by remember { mutableStateOf(false) } // Controls visibility of the Custom Theme Editor sheet
    var liveTheme by remember(userThemeFromDb) { mutableStateOf(userThemeFromDb) } // Holds the temporary "preview" theme during editing
    var isReaderFullScreen by remember { mutableStateOf(false) } // Toggle to hide TopBar/BottomBar when reading a PDF or watching video

    // Determine if the current active theme is a dark variant. This is used for Status Bar icon coloring logic.
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> liveTheme?.customIsDark ?: true
        else -> false
    }

    // --- SYSTEM UI SYNCHRONISATION --- //
    // Automatically update the Android System Status Bar and Navigation Bar colors to match our app's theme.
    // This ensures a seamless "Edge-to-Edge" experience by making the bars transparent and matching content colors.
    LaunchedEffect(isDarkTheme) {
        val activity = context as? androidx.activity.ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (isDarkTheme) androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = if (isDarkTheme) androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
    }

    // --- AUTH & THEME SYNC --- //
    // Logic to handle theme application when a user logs in (restoring their preference from Room DB)
    // or logs out (resetting to defaults and cleaning up active media resources).
    LaunchedEffect(currentUser, userThemeFromDb) {
        val userId = currentUser?.uid
        if (userId != null && userThemeFromDb != null && syncedUserId != userId) {
            // User just logged in: apply their saved theme from the database to the app state.
            val savedThemeName = userThemeFromDb?.lastSelectedTheme
            if (savedThemeName != null) {
                try {
                    onThemeChange(Theme.valueOf(savedThemeName))
                } catch (_: Exception) {
                    // Fallback to CUSTOM if the name doesn't match a standard enum but custom mode is enabled.
                    if (userThemeFromDb?.isCustomThemeEnabled == true) onThemeChange(Theme.CUSTOM)
                }
            }
            syncedUserId = userId // Mark as synced to prevent infinite update loops during recomposition
        } else if (userId == null && syncedUserId != null) {
            // REQUIREMENT: Security & Cleanup.
            // When user logs out, reset theme to default DARK and ENSURE media player stops immediately to protect privacy.
            onThemeChange(Theme.DARK)
            syncedUserId = null
            externalPlayer?.stop()
            externalPlayer?.clearMediaItems()
        }
    }

    // Helper callback to change theme both in-memory (UI) and in the persistent Room storage.
    val handleThemeChange: (Theme) -> Unit = { newTheme ->
        onThemeChange(newTheme)
        mainVm.updateThemePersistence(newTheme)
    }

    // --- MEDIA PLAYER INTEGRATION --- //
    // Sync the Media3 Player's internal state (play/pause status) back to our ViewModel.
    // This keeps the global IntegratedAudioBar buttons in sync with the actual hardware/service state.
    LaunchedEffect(externalPlayer) {
        externalPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                mainVm.syncPlayerState(isPlaying)
            }
        })
    }

    // Context-Aware UI behavior: Automatically minimize the audio bar when navigating away from 
    // the book details screen to keep the main content area focused and less cluttered.
    LaunchedEffect(currentRoute) {
        if (currentRoute != "${AppConstants.ROUTE_BOOK_DETAILS}/{bookId}" && mainVm.showPlayer) {
            mainVm.isPlayerMinimized = true
        }
    }

    // --- PRIMARY UI ORCHESTRATION --- //
    // The root theme provider that applies colors/shapes/typography based on the user's current choice.
    GlyndwrProjectTheme(theme = currentTheme, userTheme = liveTheme) {

        // TopLevelScaffold provides the structural layout (Navigation Drawer, TopBar, Bottom Bars, Rails).
        // It handles common UI elements that persist across different screens.
        TopLevelScaffold(
            currentUser = currentUser,
            localUser = localUser,
            userTheme = liveTheme,
            currentRoute = currentRoute,
            currentTutorSection = currentTutorSection,
            currentAdminSection = currentAdminSection,
            unreadCount = unreadCount,
            hideBars = isReaderFullScreen, // Hide UI chrome when reader is in full-screen mode
            onDashboardClick = {
                // Role-based navigation logic: Determine the correct "Home" dashboard based on user role.
                val target = when (localUser?.role) {
                    "admin" -> AppConstants.ROUTE_ADMIN_PANEL
                    "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL
                    else -> AppConstants.ROUTE_DASHBOARD
                }
                navController.navigate(target)
            },
            onHomeClick = { navController.navigate(AppConstants.ROUTE_HOME) },
            onProfileClick = {
                // Navigate to role-specific profile or detail screens.
                when (localUser?.role?.lowercase()) {
                    "teacher", "tutor" -> navController.navigate("${AppConstants.ROUTE_TUTOR_PANEL}?section=TEACHER_DETAIL")
                    "admin" -> navController.navigate("${AppConstants.ROUTE_ADMIN_PANEL}?section=PROFILE")
                    else -> navController.navigate(AppConstants.ROUTE_PROFILE)
                }
            },
            onWalletClick = { mainVm.showWalletHistory = true },
            onNotificationsClick = {
                // Admin has a specialized notification management interface.
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
                // Real-time update of the custom theme preview.
                liveTheme = themeUpdate
                if (currentTheme != Theme.CUSTOM) onThemeChange(Theme.CUSTOM)
            },
            onAboutClick = { navController.navigate(AppConstants.ROUTE_ABOUT) },
            bottomContent = {
                // --- INTEGRATED AUDIO PLAYER (Mini Bar) --- //
                // Display the mini playback bar above the bottom navigation if a track is active and minimized.
                if (!isReaderFullScreen && mainVm.showPlayer && mainVm.isPlayerMinimized) {
                    val isTutorHub = currentRoute?.startsWith(AppConstants.ROUTE_TUTOR_PANEL) == true
                    val isAdminHub = currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_PANEL) == true
                    val isAdminUserDetails = currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_USER_DETAILS) == true
                    
                    // REQUIREMENT: Professional Layout & Conflict Avoidance. 
                    // Some screens (Staff Hubs) have their own local bottom navigation bars (approx 80dp high).
                    // We apply extra padding here so the audio bar "floats" above them rather than obscuring controls.
                    val bottomNavPadding = if (isTutorHub || isAdminHub || isAdminUserDetails) 80.dp else 0.dp
                    
                    Box(modifier = Modifier.padding(bottom = bottomNavPadding)) {
                        IntegratedAudioBar(
                            currentBook = mainVm.currentPlayingBook,
                            externalPlayer = externalPlayer,
                            onToggleMinimize = { mainVm.isPlayerMinimized = false }, // Maximize on click
                            onClose = { mainVm.stopPlayer(externalPlayer) }         // Stop and hide
                        )
                    }
                }
            }
        ) { paddingValues ->
            // --- GLOBAL POPUPS & OVERLAYS --- //
            // These modal components handle critical interactions like logout confirmation and sign-out feedback.
            AppNavigationPopups(
                showLogoutConfirm = mainVm.showLogoutConfirm,
                showSignedOutPopup = mainVm.showSignedOutPopup,
                onLogoutConfirm = { mainVm.signOut(navController, externalPlayer) },
                onLogoutDismiss = { mainVm.showLogoutConfirm = false },
                onSignedOutDismiss = { mainVm.showSignedOutPopup = false }
            )

            // Main Content Area: Responds to the padding provided by TopLevelScaffold (for TopBars etc.)
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // REQUIREMENT: Advanced System Feedback (8%)
                    // Animated "Offline Mode" banner that slides in when internet connectivity is lost.
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

                    // --- NAVIGATION HOST --- //
                    // Defines the primary navigation routes and screen compositions for the entire app.
                    NavHost(
                        navController = navController, 
                        startDestination = AppConstants.ROUTE_SPLASH, 
                        modifier = Modifier.weight(1f) // Fills the remaining vertical space
                    ) {
                        
                        // Splash Screen: Entry point that handles initial data loading and role-based redirection.
                        composable(AppConstants.ROUTE_SPLASH) {
                            SplashScreen(isLoadingData = isDataLoading, onTimeout = {
                                val targetRoute = when (localUser?.role) {
                                    "admin" -> AppConstants.ROUTE_ADMIN_PANEL
                                    "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL
                                    else -> AppConstants.ROUTE_HOME
                                }
                                // Use inclusive popUpTo to remove Splash from the backstack once redirected.
                                navController.navigate(targetRoute) { popUpTo(AppConstants.ROUTE_SPLASH) { inclusive = true } }
                            })
                        }

                        // Modular Navigation Graphs:
                        // These extension functions group related routes together to keep this central file clean.
                        homeNavGraph(navController, mainVm.currentUser, isDataLoading, loadError, currentTheme, handleThemeChange, { showThemeBuilder = true }, { mainVm.refreshData() }, { mainVm.onPlayAudio(it, externalPlayer) }, mainVm.currentPlayingBook?.id, mainVm.isAudioPlaying)
                        authNavGraph(navController, currentTheme, handleThemeChange)
                        storeNavGraph(navController, mainVm.currentUser, allBooks, currentTheme, handleThemeChange) { mainVm.onPlayAudio(it, externalPlayer) }
                        dashboardNavGraph(navController, mainVm.currentUser, allBooks, currentTheme, handleThemeChange, { showThemeBuilder = true }, { mainVm.onPlayAudio(it, externalPlayer) }, externalPlayer, mainVm.isAudioPlaying, mainVm.currentPlayingBook?.id, mainVm.currentPlayingBook) { mainVm.showLogoutConfirm = true }
                        infoNavGraph(navController, currentTheme, liveTheme, handleThemeChange) { showThemeBuilder = true }
                        invoiceNavGraph(navController, allBooks, currentUser?.displayName ?: AppConstants.TEXT_STUDENT, currentTheme, handleThemeChange)
                        
                        // My Applications Screen: Displays the list of courses the student has enrolled in.
                        composable(AppConstants.ROUTE_MY_APPLICATIONS) {
                            MyApplicationsScreen(onBack = {
                                // Navigate back to the appropriate panel based on role.
                                when (localUser?.role) {
                                    "admin" -> navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) { popUpTo(AppConstants.ROUTE_ADMIN_PANEL) { inclusive = true } }
                                    "teacher", "tutor" -> navController.navigate(AppConstants.ROUTE_TUTOR_PANEL) { popUpTo(AppConstants.ROUTE_TUTOR_PANEL) { inclusive = true } }
                                    else -> navController.popBackStack()
                                }
                            }, onNavigateToCourse = { id -> navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") }, isDarkTheme = isDarkTheme, onToggleTheme = {})
                        }

                        // PDF Reader: Specialized screen for viewing documents. Uses a full-screen callback to hide system bars.
                        composable("${AppConstants.ROUTE_PDF_READER}/{bookId}") { backStackEntry -> 
                            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                            PdfReaderScreen(
                                bookId = bookId,
                                onBack = { navController.popBackStack() },
                                currentTheme = currentTheme,
                                onThemeChange = handleThemeChange,
                                onToggleFullScreen = { isReaderFullScreen = it } // Updates parent state to hide Scaffold bars
                            ) 
                        }
                    }
                }

                // --- MODAL SHEETS (Layered over Content) --- //
                
                // Wallet History: A slide-up sheet showing user transactions (Purchase history).
                if (mainVm.showWalletHistory) {
                    WalletHistorySheet(
                        transactions = walletHistory, 
                        onNavigateToProduct = { id -> 
                            mainVm.showWalletHistory = false
                            navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") 
                        }, 
                        onViewInvoice = { id, ref -> 
                            mainVm.showWalletHistory = false
                            val route = if (ref != null) "${AppConstants.ROUTE_INVOICE_CREATING}/$id?ref=$ref" else "${AppConstants.ROUTE_INVOICE_CREATING}/$id"
                            navController.navigate(route) 
                        }, 
                        onDismiss = { mainVm.showWalletHistory = false }
                    )
                }

                // Maximized Audio Player: A full-screen overlay for detailed control of the current audio track.
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

/**
 * Utility tracker to prevent the Theme synchronization logic from running redundant checks
 * during a single session. It stores the UID of the last successfully synchronized user.
 */
private var syncedUserId: String? = null
