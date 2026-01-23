package assignment1.krzysztofoko.s16001089.ui

import android.util.Log
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    var currentPlayingBook by remember { mutableStateOf<Book?>(null) }
    var showPlayer by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(refreshTrigger) {
        isDataLoading = true
        loadError = null
        try {
            repository.getAllCombinedData().collect { combined ->
                allBooks = combined ?: emptyList()
                isDataLoading = false
                Log.d("AppNavigation", "Database sync finished. Items: ${allBooks.size}")
            }
        } catch (e: Exception) {
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
            val existing = db.userDao().getUserById(user.uid)
            if (existing == null) {
                db.userDao().upsertUser(UserLocal(
                    id = user.uid,
                    name = user.displayName ?: "Student",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString(),
                    balance = 1000.0 
                ))
            } else {
                val newPhoto = user.photoUrl?.toString() ?: existing.photoUrl
                db.userDao().upsertUser(existing.copy(
                    name = user.displayName ?: existing.name,
                    email = user.email ?: existing.email,
                    photoUrl = newPhoto
                ))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Only show the custom Navbar if logged in AND NOT on splash or other excluded screens
            if (currentUser != null && 
                currentRoute != null && // Wait for route to be stable
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
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { navController.navigate("dashboard") }
                                .padding(4.dp)
                        ) {
                            UserAvatar(
                                photoUrl = localUser?.photoUrl ?: currentUser?.photoUrl?.toString(), 
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Hi, $firstName", 
                                style = MaterialTheme.typography.labelLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { navController.navigate("dashboard") }
                            ) {
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
                    scope.launch {
                        snackbarHostState.showSnackbar("Successfully logged off")
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Log Off", fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } }
            )
        }

        // We wrap the NavHost in a Box to control padding. 
        // When currentRoute is "splash", we ignore the Scaffold padding so it's truly full-screen.
        Box(modifier = Modifier.fillMaxSize().let { 
            if (currentRoute == "splash") it else it.padding(paddingValues) 
        }) {
            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") { 
                    SplashScreen(
                        isLoadingData = isDataLoading,
                        onTimeout = { navController.navigate("home") { popUpTo("splash") { inclusive = true } } }
                    ) 
                }
                composable("home") { HomeScreen(navController = navController, isLoggedIn = currentUser != null, allBooks = allBooks, isLoading = isDataLoading, error = loadError, onRefresh = { refreshTrigger++ }, onAboutClick = { navController.navigate("about") }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme) }
                composable("auth") { AuthScreen(onAuthSuccess = { navController.navigate("home") { popUpTo(navController.graph.startDestinationId) { inclusive = true } } }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, snackbarHostState = snackbarHostState) }
                composable("bookDetails/{bookId}") { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                    BookDetailScreen(bookId = bookId, initialBook = allBooks.find { it.id == bookId }, user = currentUser, onLoginRequired = { navController.navigate("auth") }, onBack = { navController.popBackStack() }, isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme, onPlayAudio = { currentPlayingBook = it; showPlayer = true }, onReadBook = { navController.navigate("pdfReader/$it") }, onNavigateToProfile = { navController.navigate("profile") }, onViewInvoice = { navController.navigate("invoiceCreating/$it") } )
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
