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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.auth.AuthScreen
import assignment1.krzysztofoko.s16001089.ui.home.HomeScreen
import assignment1.krzysztofoko.s16001089.ui.details.BookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardScreen
import assignment1.krzysztofoko.s16001089.ui.profile.ProfileScreen
import assignment1.krzysztofoko.s16001089.ui.splash.SplashScreen
import assignment1.krzysztofoko.s16001089.ui.info.*
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.components.InvoiceCreatingScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // FETCH DATA LOCALLY USING FIXED ID
    val localUser by db.userDao().getUserFlow(LOCAL_USER_ID).collectAsState(initial = null)

    var allBooks by remember { mutableStateOf(listOf<Book>()) }
    var isDataLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var showPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerProgress by remember { mutableFloatStateOf(0f) }
    var currentPlayingBook by remember { mutableStateOf<Book?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // FETCH FROM LOCAL DB WITH AUTO-SEEDING
    LaunchedEffect(refreshTrigger) {
        isDataLoading = true
        loadError = null
        try {
            val booksFlow = db.bookDao().getAllBooks()
            val gearFlow = db.gearDao().getAllGear()
            val coursesFlow = db.courseDao().getAllCourses()
            val audioBooksFlow = db.audioBookDao().getAllAudioBooks()

            combine(booksFlow, gearFlow, coursesFlow, audioBooksFlow) { books, gear, courses, audioBooks ->
                if (books.isEmpty() && gear.isEmpty() && courses.isEmpty() && audioBooks.isEmpty()) {
                    null // Database is empty
                } else {
                    val gearAsBooks = gear.map { g ->
                        Book(id = g.id, title = g.title, price = g.price, description = g.description, imageUrl = g.imageUrl, category = g.category, mainCategory = "University Gear", author = "Wrexham University")
                    }
                    val coursesAsBooks = courses.map { c ->
                        Book(id = c.id, title = c.title, price = c.price, description = c.description, imageUrl = c.imageUrl, category = c.category, mainCategory = "University Courses", author = c.department, isInstallmentAvailable = c.isInstallmentAvailable, modulePrice = c.modulePrice)
                    }
                    val audioBooksAsBooks = audioBooks.map { ab ->
                        Book(id = ab.id, title = ab.title, price = ab.price, description = ab.description, imageUrl = ab.imageUrl, category = ab.category, mainCategory = "Audio Books", author = ab.author, isAudioBook = true, audioUrl = ab.audioUrl)
                    }
                    books + gearAsBooks + coursesAsBooks + audioBooksAsBooks
                }
            }.collect { combined ->
                if (combined == null) {
                    Log.d("AppNavigation", "Database empty, starting seeder...")
                    seedDatabase(db)
                    refreshTrigger++ // Trigger a reload after seeding
                } else {
                    allBooks = combined
                    isDataLoading = false
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e("AppNavigation", "DATABASE ERROR", e)
            loadError = "Offline Database Error"
            isDataLoading = false
        }
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Link Firebase user to our local data entry
            val existing = db.userDao().getUserById(LOCAL_USER_ID)
            if (existing == null) {
                db.userDao().upsertUser(UserLocal(
                    id = LOCAL_USER_ID,
                    name = user.displayName ?: "Student",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString(),
                    balance = 1000.0 // Starting balance for new installs
                ))
            } else {
                // Keep local data (balance/address) but update name/photo from Firebase
                db.userDao().upsertUser(existing.copy(
                    name = user.displayName ?: existing.name,
                    email = user.email ?: existing.email,
                    photoUrl = user.photoUrl?.toString() ?: existing.photoUrl
                ))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentUser != null && currentRoute != "dashboard" && currentRoute != "profile" && currentRoute != "pdfReader/{bookId}" && currentRoute != "invoice/{bookId}" && currentRoute != "invoiceCreating/{bookId}") {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        val firstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(photoUrl = currentUser?.photoUrl?.toString(), modifier = Modifier.size(36.dp), onClick = { navController.navigate("dashboard") })
                            Spacer(Modifier.width(12.dp)); Text("Hi, $firstName", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Balance Indicator
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { navController.navigate("dashboard") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "£${String.format(Locale.US, "%.2f", localUser?.balance ?: 0.0)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(Modifier.width(8.dp))
                            
                            // Sign Out Icon Only
                            IconButton(onClick = { showLogoutConfirm = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
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
                confirmButton = { Button(onClick = { showLogoutConfirm = false; auth.signOut(); navController.navigate("home") { popUpTo(0) } }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Log Off", fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") { SplashScreen(onTimeout = { navController.navigate("home") { popUpTo("splash") { inclusive = true } } }) }
                composable("home") { HomeScreen(navController = navController, isLoggedIn = currentUser != null, allBooks = allBooks, isLoading = isDataLoading, error = loadError, onRefresh = { refreshTrigger++ }, onAboutClick = { navController.navigate("about") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("auth") { AuthScreen(onAuthSuccess = { navController.navigate("home") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("bookDetails/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    BookDetailScreen(bookId = bookId, initialBook = allBooks.find { it.id == bookId }, user = currentUser, onLoginRequired = { navController.navigate("auth") }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onPlayAudio = { currentPlayingBook = it; showPlayer = true; isPlaying = true }, onReadBook = { navController.navigate("pdfReader/$it") }, onNavigateToProfile = { navController.navigate("profile") }, onViewInvoice = { navController.navigate("invoiceCreating/$it") } )
                }
                composable("pdfReader/{bookId}") { backStackEntry -> PdfReaderScreen(bookId = backStackEntry.arguments?.getString("bookId") ?: "", onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("dashboard") { DashboardScreen(navController = navController, allBooks = allBooks, onBack = { navController.popBackStack() }, onLogout = { showLogoutConfirm = true }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onViewInvoice = { navController.navigate("invoiceCreating/${it.id}") }) }
                composable("profile") { ProfileScreen(navController = navController, onBack = { navController.popBackStack() }, onLogout = { showLogoutConfirm = true }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("about") { AboutScreen(onBack = { navController.popBackStack() }, onDeveloperClick = { navController.navigate("developer") }, onInstructionClick = { navController.navigate("instructions") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("developer") { DeveloperScreen(onBack = { navController.popBackStack() }, onVersionClick = { navController.navigate("version_info") }, onFutureFeaturesClick = { navController.navigate("future_features") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("instructions") { InstructionScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("version_info") { VersionInfoScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("future_features") { FutureFeaturesScreen(onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("invoiceCreating/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    val selectedBook = allBooks.find { it.id == bookId }
                    if (selectedBook != null) { InvoiceCreatingScreen(book = selectedBook, onCreationComplete = { navController.navigate("invoice/$bookId") { popUpTo("invoiceCreating/$bookId") { inclusive = true } } }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                }
                composable("invoice/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    val selectedBook = allBooks.find { it.id == bookId }
                    if (selectedBook != null) { InvoiceScreen(book = selectedBook, userName = currentUser?.displayName ?: "Student", onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                }
            }
        }
    }
}
