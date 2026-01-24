package assignment1.krzysztofoko.s16001089.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.auth.AuthScreen
import assignment1.krzysztofoko.s16001089.ui.home.HomeScreen
import assignment1.krzysztofoko.s16001089.ui.details.BookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.AudioBookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.GearDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardScreen
import assignment1.krzysztofoko.s16001089.ui.profile.ProfileScreen
import assignment1.krzysztofoko.s16001089.ui.splash.SplashScreen
import assignment1.krzysztofoko.s16001089.ui.info.*
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.components.InvoiceCreatingScreen
import assignment1.krzysztofoko.s16001089.ui.components.AudioPlayerComponent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    externalPlayer: Player? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = AppDatabase.getDatabase(context)
    val repository = remember { BookRepository(db) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val localUser by remember(currentUser) {
        if (currentUser != null) db.userDao().getUserFlow(currentUser!!.uid)
        else flowOf(null)
    }.collectAsState(initial = null)

    var allBooks by remember { mutableStateOf(listOf<Book>()) }
    var isDataLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // PLAYER STATE
    var currentPlayingBook by remember { mutableStateOf<Book?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var showPlayer by remember { mutableStateOf(false) }
    var isPlayerMinimized by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Synchronize local state with externalPlayer
    LaunchedEffect(externalPlayer) {
        externalPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isAudioPlaying = isPlaying
            }
        })
    }

    // Auto-minimize player when navigating away from details
    LaunchedEffect(currentRoute) {
        if (currentRoute != "bookDetails/{bookId}" && showPlayer) {
            isPlayerMinimized = true
        }
    }

    LaunchedEffect(refreshTrigger) {
        isDataLoading = true
        loadError = null
        try {
            seedGearOnly(db)
            repository.getAllCombinedData().collect { combined ->
                allBooks = combined ?: emptyList()
                isDataLoading = false
            }
        } catch (e: Exception) {
            loadError = "Offline Database Error"
            isDataLoading = false
        }
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentUser != null && 
                currentRoute != null && 
                currentRoute != "splash" && 
                currentRoute != "auth" &&
                currentRoute != "dashboard" && 
                currentRoute != "profile" && 
                currentRoute != "pdfReader/{bookId}" && 
                currentRoute != "invoice/{bookId}" && 
                currentRoute != "invoiceCreating/{bookId}") {
                
                Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        val firstName = localUser?.name?.split(" ")?.firstOrNull() ?: currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { navController.navigate("dashboard") }.padding(4.dp)
                        ) {
                            UserAvatar(photoUrl = localUser?.photoUrl ?: currentUser?.photoUrl?.toString(), modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(text = "Hi, $firstName", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp), modifier = Modifier.clickable { navController.navigate("dashboard") }) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = "£${String.format(Locale.US, "%.2f", localUser?.balance ?: 0.0)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { showLogoutConfirm = true }) { Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp)) }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                title = { Text("Log Off") },
                text = { Text("Are you sure you want to log off?") },
                confirmButton = { Button(onClick = { 
                    showLogoutConfirm = false
                    auth.signOut()
                    navController.navigate("home") { popUpTo(0) }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Log Off", fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = "splash", modifier = Modifier.fillMaxSize()) {
                composable("splash") { 
                    SplashScreen(isLoadingData = isDataLoading, onTimeout = { navController.navigate("home") { popUpTo("splash") { inclusive = true } } }) 
                }
                composable("home") { 
                    Box(Modifier.padding(paddingValues)) {
                        HomeScreen(
                            navController = navController, 
                            isLoggedIn = currentUser != null, 
                            allBooks = allBooks, 
                            isLoading = isDataLoading, 
                            error = loadError, 
                            onRefresh = { refreshTrigger++ }, 
                            onAboutClick = { navController.navigate("about") }, 
                            isDarkTheme = isDarkTheme, 
                            onToggleTheme = onToggleTheme,
                            onPlayAudio = { book ->
                                if (currentPlayingBook?.id == book.id) {
                                    if (externalPlayer?.isPlaying == true) externalPlayer.pause() else externalPlayer?.play()
                                } else {
                                    currentPlayingBook = book
                                    showPlayer = true
                                    externalPlayer?.let { player ->
                                        val mediaItem = MediaItem.Builder()
                                            .setUri("asset:///${book.audioUrl}")
                                            .setMediaMetadata(
                                                MediaMetadata.Builder()
                                                    .setTitle(book.title)
                                                    .setArtist(book.author)
                                                    .build()
                                            )
                                            .build()
                                        player.setMediaItem(mediaItem)
                                        player.prepare()
                                        player.play()
                                    }
                                }
                            },
                            currentPlayingBookId = currentPlayingBook?.id,
                            isAudioPlaying = isAudioPlaying
                        )
                    }
                }
                composable("auth") { Box(Modifier.padding(paddingValues)) { AuthScreen(onAuthSuccess = { navController.navigate("home") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, snackbarHostState = snackbarHostState) } }
                composable("bookDetails/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    val selectedBook = allBooks.find { it.id == bookId }
                    
                    Box(Modifier.padding(paddingValues)) {
                        if (selectedBook?.isAudioBook == true) {
                            AudioBookDetailScreen(
                                bookId = bookId,
                                initialBook = selectedBook,
                                user = currentUser,
                                onLoginRequired = { navController.navigate("auth") },
                                onBack = { navController.popBackStack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = onToggleTheme,
                                onPlayAudio = { book ->
                                    currentPlayingBook = book
                                    showPlayer = true
                                    isPlayerMinimized = false 
                                    externalPlayer?.let { player ->
                                        val mediaItem = MediaItem.Builder()
                                            .setUri("asset:///${book.audioUrl}")
                                            .setMediaMetadata(
                                                MediaMetadata.Builder()
                                                    .setTitle(book.title)
                                                    .setArtist(book.author)
                                                    .build()
                                            )
                                            .build()
                                        player.setMediaItem(mediaItem)
                                        player.prepare()
                                        player.play()
                                    }
                                },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onViewInvoice = { navController.navigate("invoiceCreating/$it") }
                            )
                        } else if (selectedBook?.mainCategory == "University Gear") {
                            GearDetailScreen(
                                gearId = bookId,
                                user = currentUser,
                                onLoginRequired = { navController.navigate("auth") },
                                onBack = { navController.popBackStack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = onToggleTheme,
                                onNavigateToProfile = { navController.navigate("profile") },
                                onViewInvoice = { navController.navigate("invoiceCreating/$it") }
                            )
                        } else {
                            BookDetailScreen(
                                bookId = bookId, 
                                initialBook = selectedBook, 
                                user = currentUser, 
                                onLoginRequired = { navController.navigate("auth") }, 
                                onBack = { navController.popBackStack() }, 
                                isDarkTheme = isDarkTheme, 
                                onToggleTheme = onToggleTheme, 
                                onReadBook = { navController.navigate("pdfReader/$it") }, 
                                onNavigateToProfile = { navController.navigate("profile") }, 
                                onViewInvoice = { navController.navigate("invoiceCreating/$it") } 
                            )
                        }
                    }
                }
                composable("pdfReader/{bookId}") { backStackEntry -> Box(Modifier.padding(paddingValues)) { PdfReaderScreen(bookId = backStackEntry.arguments?.getString("bookId") ?: "", onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) } }
                composable("dashboard") { 
                    Box(Modifier.padding(paddingValues)) {
                        DashboardScreen(
                            navController = navController, 
                            allBooks = allBooks, 
                            onBack = { navController.popBackStack() }, 
                            onLogout = { showLogoutConfirm = true }, 
                            isDarkTheme = isDarkTheme, 
                            onToggleTheme = onToggleTheme, 
                            onViewInvoice = { navController.navigate("invoiceCreating/${it.id}") },
                            onPlayAudio = { book ->
                                if (currentPlayingBook?.id == book.id) {
                                    if (externalPlayer?.isPlaying == true) externalPlayer.pause() else externalPlayer?.play()
                                } else {
                                    currentPlayingBook = book
                                    showPlayer = true
                                    externalPlayer?.let { player ->
                                        val mediaItem = MediaItem.Builder()
                                            .setUri("asset:///${book.audioUrl}")
                                            .setMediaMetadata(
                                                MediaMetadata.Builder()
                                                    .setTitle(book.title)
                                                    .setArtist(book.author)
                                                    .build()
                                            )
                                            .build()
                                        player.setMediaItem(mediaItem)
                                        player.prepare()
                                        player.play()
                                    }
                                }
                            },
                            currentPlayingBookId = currentPlayingBook?.id,
                            isAudioPlaying = isAudioPlaying
                        )
                    }
                }
                composable("profile") { Box(Modifier.padding(paddingValues)) { ProfileScreen(navController = navController, onBack = { navController.popBackStack() }, onLogout = { showLogoutConfirm = true }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) } }
                composable("about") { Box(Modifier.padding(paddingValues)) { AboutScreen(onBack = { navController.popBackStack() }, onDeveloperClick = { navController.navigate("developer") }, onInstructionClick = { navController.navigate("instructions") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) } }
                composable("developer") { Box(Modifier.padding(paddingValues)) { DeveloperScreen(onBack = { navController.popBackStack() }, onVersionClick = { navController.navigate("version_info") }, onFutureFeaturesClick = { navController.navigate("future_features") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) } }
                composable("instructions") { Box(Modifier.padding(paddingValues)) { InstructionScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) } }
                composable("version_info") { Box(Modifier.padding(paddingValues)) { VersionInfoScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) } }
                composable("future_features") { Box(Modifier.padding(paddingValues)) { FutureFeaturesScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) } }
                composable("invoiceCreating/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    val selectedBook = allBooks.find { it.id == bookId }
                    Box(Modifier.padding(paddingValues)) {
                        if (selectedBook != null) { InvoiceCreatingScreen(book = selectedBook, onCreationComplete = { navController.navigate("invoice/$bookId") { popUpTo("invoiceCreating/$bookId") { inclusive = true } } }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                    }
                }
                composable("invoice/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    val selectedBook = allBooks.find { it.id == bookId }
                    Box(Modifier.padding(paddingValues)) {
                        if (selectedBook != null) { InvoiceScreen(book = selectedBook, userName = currentUser?.displayName ?: "Student", onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                    }
                }
            }

            // Audio Player Overlay with Minimize logic
            if (showPlayer && currentPlayingBook != null) {
                if (!isPlayerMinimized) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isPlayerMinimized = true }
                    )
                }
                
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    currentPlayingBook?.let { book ->
                        AudioPlayerComponent(
                            book = book,
                            isMinimized = isPlayerMinimized,
                            onToggleMinimize = { isPlayerMinimized = !isPlayerMinimized },
                            onClose = { 
                                showPlayer = false 
                                externalPlayer?.stop()
                                currentPlayingBook = null
                            },
                            isDarkTheme = isDarkTheme,
                            player = externalPlayer
                        )
                    }
                }
            }
        }
    }
}
