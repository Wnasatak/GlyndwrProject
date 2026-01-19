package assignment1.krzysztofoko.s16001089.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.seedDatabase
import assignment1.krzysztofoko.s16001089.ui.auth.AuthScreen
import assignment1.krzysztofoko.s16001089.ui.home.HomeScreen
import assignment1.krzysztofoko.s16001089.ui.details.BookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardScreen
import assignment1.krzysztofoko.s16001089.ui.profile.ProfileScreen
import assignment1.krzysztofoko.s16001089.ui.splash.SplashScreen
import assignment1.krzysztofoko.s16001089.ui.info.AboutScreen
import assignment1.krzysztofoko.s16001089.ui.info.DeveloperScreen
import assignment1.krzysztofoko.s16001089.ui.info.InstructionScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    var allBooks by remember { mutableStateOf(listOf<Book>()) }
    var isDataLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var showPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerProgress by remember { mutableFloatStateOf(0f) }
    var currentPlayingBook by remember { mutableStateOf<Book?>(null) }

    LaunchedEffect(refreshTrigger) {
        isDataLoading = true
        loadError = null
        db.collection("books").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    seedDatabase()
                    refreshTrigger++
                } else {
                    allBooks = result.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
                    isDataLoading = false
                }
            }
            .addOnFailureListener {
                loadError = it.localizedMessage
                isDataLoading = false
            }
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && playerProgress < 1f) {
                delay(1000)
                playerProgress += 0.01f
            }
            if (playerProgress >= 1f) isPlaying = false
        }
    }

    Scaffold(
        topBar = {
            if (currentUser != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val firstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { navController.navigate("profile") }
                        ) {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Logged in as $firstName", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        IconButton(onClick = { auth.signOut(); navController.navigate("home") { popUpTo(0) } }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "Log Out", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = showPlayer && currentPlayingBook != null, enter = slideInVertically(initialOffsetY = { it }), exit = slideOutVertically(targetOffsetY = { it })) {
                currentPlayingBook?.let { book ->
                    Surface(tonalElevation = 12.dp, shadowElevation = 24.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Now Playing", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(book.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                                IconButton(onClick = { showPlayer = false; isPlaying = false }) { Icon(Icons.Default.Close, null) }
                            }
                            Slider(value = playerProgress, onValueChange = { playerProgress = it }, modifier = Modifier.height(32.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { playerProgress = (playerProgress - 0.05f).coerceAtLeast(0f) }) { Icon(Icons.Default.Replay10, null) }
                                Spacer(Modifier.width(16.dp))
                                FilledIconButton(onClick = { isPlaying = !isPlaying }, modifier = Modifier.size(56.dp)) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(32.dp)) }
                                Spacer(Modifier.width(16.dp))
                                IconButton(onClick = { playerProgress = (playerProgress + 0.05f).coerceAtMost(1f) }) { Icon(Icons.Default.Forward10, null) }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen(onTimeout = {
                        navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                    })
                }
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        isLoggedIn = currentUser != null,
                        allBooks = allBooks,
                        isLoading = isDataLoading,
                        error = loadError,
                        onRefresh = { refreshTrigger++ },
                        onAboutClick = { navController.navigate("about") },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
                composable("auth") { AuthScreen(onAuthSuccess = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("bookDetails/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    BookDetailScreen(
                        bookId = bookId, 
                        isLoggedIn = currentUser != null, 
                        onLoginRequired = { navController.navigate("auth") }, 
                        onBack = { navController.popBackStack() }, 
                        isDarkTheme = isDarkTheme, 
                        onToggleTheme = onToggleTheme, 
                        onPlayAudio = { currentPlayingBook = it; showPlayer = true; isPlaying = true },
                        onReadBook = { navController.navigate("pdfReader/$it") }
                    )
                }
                composable("pdfReader/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    PdfReaderScreen(
                        bookId = bookId,
                        onBack = { navController.popBackStack() },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
                composable("dashboard") { DashboardScreen(navController = navController, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("profile") { ProfileScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("about") { 
                    AboutScreen(
                        onBack = { navController.popBackStack() }, 
                        onDeveloperClick = { navController.navigate("developer") }, 
                        onInstructionClick = { navController.navigate("instructions") },
                        isDarkTheme = isDarkTheme, 
                        onToggleTheme = onToggleTheme
                    ) 
                }
                composable("developer") { DeveloperScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("instructions") { InstructionScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
            }
        }
    }
}
