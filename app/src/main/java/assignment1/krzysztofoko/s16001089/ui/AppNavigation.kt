package assignment1.krzysztofoko.s16001089.ui

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

/**
 * Root Navigation Controller for the Glyndŵr Project.
 */
@Composable
fun AppNavigation(
    isDarkTheme: Boolean,           
    onToggleTheme: () -> Unit,      
    externalPlayer: Player? = null,
    windowSizeClass: WindowSizeClass? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val db = AppDatabase.getDatabase(context)
    val repository = remember { BookRepository(db) }
    
    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(repository, db))

    val currentUser by mainVm.currentUser.collectAsState()
    val localUser by mainVm.localUser.collectAsState()
    val allBooks by mainVm.allBooks.collectAsState()
    val isDataLoading by mainVm.isDataLoading.collectAsState()
    val loadError by mainVm.loadError.collectAsState()
    val unreadCount by mainVm.unreadNotificationsCount.collectAsState()
    val walletHistory by mainVm.walletHistory.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

    TopLevelScaffold(
        currentUser = currentUser,
        localUser = localUser,
        currentRoute = currentRoute,
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
        onProfileClick = { navController.navigate(AppConstants.ROUTE_PROFILE) },
        onWalletClick = { mainVm.showWalletHistory = true },
        onNotificationsClick = { navController.navigate(AppConstants.ROUTE_NOTIFICATIONS) },
        onMyApplicationsClick = { navController.navigate(AppConstants.ROUTE_MY_APPLICATIONS) },
        onMessagesClick = { navController.navigate(AppConstants.ROUTE_MESSAGES) },
        onLogoutClick = { mainVm.showLogoutConfirm = true },
        onToggleTheme = onToggleTheme,
        isDarkTheme = isDarkTheme,
        windowSizeClass = windowSizeClass
    ) { paddingValues ->
        
        AppNavigationPopups(
            showLogoutConfirm = mainVm.showLogoutConfirm,
            showSignedOutPopup = mainVm.showSignedOutPopup,
            onLogoutConfirm = { mainVm.signOut(navController) },
            onLogoutDismiss = { mainVm.showLogoutConfirm = false },
            onSignedOutDismiss = { mainVm.showSignedOutPopup = false }
        )

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(
                navController = navController, 
                startDestination = AppConstants.ROUTE_SPLASH, 
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppConstants.ROUTE_SPLASH) { 
                    SplashScreen(
                        isLoadingData = isDataLoading, 
                        onTimeout = { 
                            val targetRoute = when (localUser?.role) {
                                "admin" -> AppConstants.ROUTE_ADMIN_PANEL
                                "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL
                                else -> AppConstants.ROUTE_HOME
                            }
                            navController.navigate(targetRoute) { 
                                popUpTo(AppConstants.ROUTE_SPLASH) { inclusive = true } 
                            } 
                        }
                    ) 
                }

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

                authNavGraph(navController, isDarkTheme, onToggleTheme)
                
                storeNavGraph(
                    navController = navController,
                    currentUserFlow = mainVm.currentUser,
                    allBooks = allBooks,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onPlayAudio = { mainVm.onPlayAudio(it, externalPlayer) }
                )

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

                infoNavGraph(navController, isDarkTheme, onToggleTheme)

                invoiceNavGraph(
                    navController = navController,
                    allBooks = allBooks,
                    currentUserDisplayName = currentUser?.displayName ?: AppConstants.TEXT_STUDENT,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )

                composable(AppConstants.ROUTE_MY_APPLICATIONS) {
                    MyApplicationsScreen(
                        onBack = { 
                            when (localUser?.role) {
                                "admin" -> {
                                    navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) {
                                        popUpTo(AppConstants.ROUTE_ADMIN_PANEL) { inclusive = true }
                                    }
                                }
                                "teacher", "tutor" -> {
                                    navController.navigate(AppConstants.ROUTE_TUTOR_PANEL) {
                                        popUpTo(AppConstants.ROUTE_TUTOR_PANEL) { inclusive = true }
                                    }
                                }
                                else -> navController.popBackStack()
                            }
                        },
                        onNavigateToCourse = { id -> 
                            navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$id")
                        },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
            }

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
