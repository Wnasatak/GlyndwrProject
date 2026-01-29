package assignment1.krzysztofoko.s16001089.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

/**
 * Root Navigation Controller for the Glyndŵr Project.
 * 
 * This component acts as the central hub for the app's UI architecture. It initializes
 * the global navigation controller, the main application ViewModel, and coordinates
 * the transition between different feature modules (Auth, Home, Store, Dashboard, etc.).
 */
@Composable
fun AppNavigation(
    isDarkTheme: Boolean,           // Current global theme state (Dark/Light)
    onToggleTheme: () -> Unit,      // Callback to flip the theme state
    externalPlayer: Player? = null  // The Media3 player instance provided by MainActivity
) {
    // Standard Context and Database handles
    val context = LocalContext.current
    val navController = rememberNavController()
    val db = AppDatabase.getDatabase(context)
    val repository = remember { BookRepository(db) }
    
    // Global ViewModel initialization with its specific factory for dependency injection
    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(repository, db))

    // UI State observation: collects global data flows into Compose state
    val currentUser by mainVm.currentUser.collectAsState()
    val localUser by mainVm.localUser.collectAsState()
    val allBooks by mainVm.allBooks.collectAsState()
    val isDataLoading by mainVm.isDataLoading.collectAsState()
    val loadError by mainVm.loadError.collectAsState()
    val unreadCount by mainVm.unreadNotificationsCount.collectAsState()
    val walletHistory by mainVm.walletHistory.collectAsState()

    // Navigation State: tracks which screen is currently visible to the user
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    /**
     * Player State Sync: Listens to the Media3 player's internal events.
     * Updates the global ViewModel state whenever playback starts or stops.
     */
    LaunchedEffect(externalPlayer) {
        externalPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                mainVm.syncPlayerState(isPlaying)
            }
        })
    }

    /**
     * Auto-Minimize Logic: Automatically shrinks the audio player bar
     * when the user navigates away from the specific item detail screen.
     */
    LaunchedEffect(currentRoute) {
        if (currentRoute != "${AppConstants.ROUTE_BOOK_DETAILS}/{bookId}" && mainVm.showPlayer) {
            mainVm.isPlayerMinimized = true
        }
    }

    /**
     * TopLevelScaffold: The master layout wrapper.
     * Handles the adaptive Bottom Navigation bar and top-level interactions
     * (Dashboard, Wallet, Notifications, Logout) that persist across screens.
     */
    TopLevelScaffold(
        currentUser = currentUser,
        localUser = localUser,
        currentRoute = currentRoute,
        unreadCount = unreadCount,
        onDashboardClick = { navController.navigate(AppConstants.ROUTE_DASHBOARD) },
        onWalletClick = { mainVm.showWalletHistory = true },
        onNotificationsClick = { navController.navigate(AppConstants.ROUTE_NOTIFICATIONS) },
        onLogoutClick = { mainVm.showLogoutConfirm = true }
    ) { paddingValues ->
        
        /**
         * Global Popups: Centrally managed dialogs for logout confirmations 
         * and success messages.
         */
        AppNavigationPopups(
            showLogoutConfirm = mainVm.showLogoutConfirm,
            showSignedOutPopup = mainVm.showSignedOutPopup,
            onLogoutConfirm = { mainVm.signOut(navController) },
            onLogoutDismiss = { mainVm.showLogoutConfirm = false },
            onSignedOutDismiss = { mainVm.showSignedOutPopup = false }
        )

        // Main Content Area
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            /**
             * NavHost: Defines the routing table for the application.
             * Organizes screens into specialized 'Feature Graphs' for better maintainability.
             */
            NavHost(
                navController = navController, 
                startDestination = AppConstants.ROUTE_SPLASH, 
                modifier = Modifier.fillMaxSize()
            ) {
                // Initial loading screen
                composable(AppConstants.ROUTE_SPLASH) { 
                    SplashScreen(
                        isLoadingData = isDataLoading, 
                        onTimeout = { 
                            navController.navigate(AppConstants.ROUTE_HOME) { 
                                popUpTo(AppConstants.ROUTE_SPLASH) { inclusive = true } 
                            } 
                        }
                    ) 
                }

                // Primary discovery screen
                homeNavGraph(
                    navController = navController,
                    currentUserFlow = mainVm.currentUser,
                    isDataLoading = isDataLoading,
                    loadError = loadError,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onRefresh = { mainVm.refreshData() },
                    onPlayAudio = { mainVm.onPlayAudio(it, externalPlayer) },
                    currentPlayingBookId = mainVm.currentPlayingBook?.id,
                    isAudioPlaying = mainVm.isAudioPlaying 
                )

                // Login, Registration, and 2FA modules
                authNavGraph(navController, isDarkTheme, onToggleTheme)
                
                // Detailed product views (Books, Gear, Courses)
                storeNavGraph(
                    navController = navController,
                    currentUserFlow = mainVm.currentUser,
                    allBooks = allBooks,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onPlayAudio = { mainVm.onPlayAudio(it, externalPlayer) }
                )

                // User collection, statistics, and administrative controls
                dashboardNavGraph(
                    navController = navController,
                    currentUserFlow = mainVm.currentUser,
                    allBooks = allBooks,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onPlayAudio = { mainVm.onPlayAudio(it, externalPlayer) },
                    isAudioPlaying = mainVm.isAudioPlaying,
                    currentPlayingBookId = mainVm.currentPlayingBook?.id,
                    onLogoutClick = { mainVm.showLogoutConfirm = true }
                )

                // Static info screens (About, Developer info, What's New)
                infoNavGraph(navController, isDarkTheme, onToggleTheme)

                // Financial receipt and PDF generation module
                invoiceNavGraph(
                    navController = navController,
                    allBooks = allBooks,
                    currentUserDisplayName = currentUser?.displayName ?: AppConstants.TEXT_STUDENT,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }

            /**
             * Overlay: Wallet History.
             * Displays as a bottom sheet over any current screen when triggered.
             */
            if (mainVm.showWalletHistory) {
                WalletHistorySheet(
                    transactions = walletHistory,
                    onNavigateToProduct = { id -> 
                        mainVm.showWalletHistory = false
                        navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id") 
                    },
                    onViewInvoice = { id, ref ->
                        mainVm.showWalletHistory = false
                        val route = if (ref != null) "${AppConstants.ROUTE_INVOICE_CREATING}/$id?ref=$ref"
                                    else "${AppConstants.ROUTE_INVOICE_CREATING}/$id"
                        navController.navigate(route)
                    },
                    onDismiss = { mainVm.showWalletHistory = false }
                )
            }

            /**
             * Persistent Overlay: Audio Player.
             * Stays visible across the app while media is playing, allowing for 
             * background listening and global control.
             */
            GlobalAudioPlayerOverlay(
                showPlayer = mainVm.showPlayer,
                currentBook = mainVm.currentPlayingBook,
                isMinimized = mainVm.isPlayerMinimized,
                isDarkTheme = isDarkTheme,
                externalPlayer = externalPlayer,
                onToggleMinimize = { mainVm.isPlayerMinimized = !mainVm.isPlayerMinimized },
                onClose = { mainVm.stopPlayer(externalPlayer) },
                onSetMinimized = { mainVm.isPlayerMinimized = it }
            )
        }
    }
}
