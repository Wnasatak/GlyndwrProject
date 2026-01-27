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
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.auth.AuthScreen
import assignment1.krzysztofoko.s16001089.ui.home.HomeScreen
import assignment1.krzysztofoko.s16001089.ui.details.book.BookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.audiobook.AudioBookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.gear.GearDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.course.CourseDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardScreen
import assignment1.krzysztofoko.s16001089.ui.profile.ProfileScreen
import assignment1.krzysztofoko.s16001089.ui.splash.SplashScreen
import assignment1.krzysztofoko.s16001089.ui.info.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.notifications.NotificationScreen
import assignment1.krzysztofoko.s16001089.ui.classroom.ClassroomScreen

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    externalPlayer: Player? = null
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
        if (currentRoute != "bookDetails/{bookId}" && mainVm.showPlayer) {
            mainVm.isPlayerMinimized = true
        }
    }

    TopLevelScaffold(
        currentUser = currentUser,
        localUser = localUser,
        currentRoute = currentRoute,
        unreadCount = unreadCount,
        onDashboardClick = { navController.navigate("dashboard") },
        onNotificationsClick = { navController.navigate("notifications") },
        onLogoutClick = { mainVm.showLogoutConfirm = true }
    ) { paddingValues ->
        
        AppNavigationPopups(
            showLogoutConfirm = mainVm.showLogoutConfirm,
            showSignedOutPopup = mainVm.showSignedOutPopup,
            onLogoutConfirm = { mainVm.signOut(navController) },
            onLogoutDismiss = { mainVm.showLogoutConfirm = false },
            onSignedOutDismiss = { mainVm.showSignedOutPopup = false }
        )

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "splash", modifier = Modifier.fillMaxSize()) {
                composable("splash") { 
                    SplashScreen(isLoadingData = isDataLoading, onTimeout = { navController.navigate("home") { popUpTo("splash") { inclusive = true } } }) 
                }
                composable("home") { 
                    HomeScreen(
                        navController = navController, 
                        isLoggedIn = currentUser != null, 
                        isLoading = isDataLoading, 
                        error = loadError, 
                        onRefresh = { mainVm.refreshData() }, 
                        onAboutClick = { navController.navigate("about") }, 
                        isDarkTheme = isDarkTheme, 
                        onToggleTheme = onToggleTheme,
                        onPlayAudio = { mainVm.onPlayAudio(it, externalPlayer) },
                        currentPlayingBookId = mainVm.currentPlayingBook?.id,
                        isAudioPlaying = mainVm.isAudioPlaying
                    )
                }
                composable("auth") { 
                    AuthScreen(
                        onAuthSuccess = { navController.navigate("home") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } }, 
                        onBack = { navController.popBackStack() }, 
                        isDarkTheme = isDarkTheme, 
                        onToggleTheme = onToggleTheme, 
                        snackbarHostState = remember { SnackbarHostState() }
                    ) 
                }
                composable("notifications") {
                    NotificationScreen(
                        onNavigateToItem = { navController.navigate("bookDetails/$it") },
                        onNavigateToInvoice = { navController.navigate("invoice/$it") },
                        onBack = { navController.popBackStack() }, 
                        isDarkTheme = isDarkTheme
                    )
                }
                composable("classroom/{courseId}") { backStackEntry ->
                    val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                    ClassroomScreen(
                        courseId = courseId,
                        onBack = { navController.popBackStack() },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
                composable("bookDetails/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    val selectedBook = allBooks.find { it.id == bookId }
                    
                    when {
                        selectedBook?.isAudioBook == true -> AudioBookDetailScreen(
                            bookId = bookId, user = currentUser, onLoginRequired = { navController.navigate("auth") }, onBack = { navController.popBackStack() },
                            isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onPlayAudio = { mainVm.onPlayAudio(it, externalPlayer) },
                            onNavigateToProfile = { navController.navigate("profile") }, onViewInvoice = { navController.navigate("invoiceCreating/$it") }
                        )
                        selectedBook?.mainCategory == "University Gear" -> GearDetailScreen(
                            navController = navController, gearId = bookId, user = currentUser, onLoginRequired = { navController.navigate("auth") }, onBack = { navController.popBackStack() },
                            isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onNavigateToProfile = { navController.navigate("profile") }, onViewInvoice = { navController.navigate("invoiceCreating/$it") }
                        )
                        selectedBook?.mainCategory == "University Courses" -> CourseDetailScreen(
                            courseId = bookId, user = currentUser, onLoginRequired = { navController.navigate("auth") }, onBack = { navController.popBackStack() },
                            isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onNavigateToProfile = { navController.navigate("profile") }, onViewInvoice = { navController.navigate("invoiceCreating/$it") },
                            onEnterClassroom = { navController.navigate("classroom/$it") }
                        )
                        else -> BookDetailScreen(
                            bookId = bookId, initialBook = selectedBook, user = currentUser, onLoginRequired = { navController.navigate("auth") }, onBack = { navController.popBackStack() },
                            isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onReadBook = { navController.navigate("pdfReader/$it") },
                            onNavigateToProfile = { navController.navigate("profile") }, onViewInvoice = { navController.navigate("invoiceCreating/$it") }
                        )
                    }
                }
                composable("pdfReader/{bookId}") { backStackEntry -> 
                    PdfReaderScreen(bookId = backStackEntry.arguments?.getString("bookId") ?: "", onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) 
                }
                composable("dashboard") { 
                    DashboardScreen(
                        navController = navController, allBooks = allBooks, onBack = { navController.popBackStack() }, onLogout = { mainVm.showLogoutConfirm = true },
                        isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onViewInvoice = { navController.navigate("invoiceCreating/${it.id}") },
                        onPlayAudio = { mainVm.onPlayAudio(it, externalPlayer) }, currentPlayingBookId = mainVm.currentPlayingBook?.id, isAudioPlaying = mainVm.isAudioPlaying
                    )
                }
                composable("profile") { 
                    ProfileScreen(navController = navController, onLogout = { mainVm.showLogoutConfirm = true }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) 
                }
                composable("about") { 
                    AboutScreen(onBack = { navController.popBackStack() }, onDeveloperClick = { navController.navigate("developer") }, onInstructionClick = { navController.navigate("instructions") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) 
                }
                composable("developer") { 
                    DeveloperScreen(onBack = { navController.popBackStack() }, onVersionClick = { navController.navigate("version_info") }, onFutureFeaturesClick = { navController.navigate("future_features") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) 
                }
                composable("instructions") { InstructionScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("version_info") { VersionInfoScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("future_features") { FutureFeaturesScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("invoiceCreating/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    allBooks.find { it.id == bookId }?.let { selectedBook ->
                        InvoiceCreatingScreen(book = selectedBook, onCreationComplete = { navController.navigate("invoice/$bookId") { popUpTo("invoiceCreating/$bookId") { inclusive = true } } }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)
                    }
                }
                composable("invoice/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    allBooks.find { it.id == bookId }?.let { selectedBook ->
                        InvoiceScreen(book = selectedBook, userName = currentUser?.displayName ?: "Student", onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)
                    }
                }
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
