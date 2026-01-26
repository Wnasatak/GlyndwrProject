package assignment1.krzysztofoko.s16001089.ui.home // Package declaration

import androidx.compose.animation.* // Animation support
import androidx.compose.animation.core.* // Core animation utilities
import androidx.compose.foundation.BorderStroke // Border styling
import androidx.compose.foundation.background // Background modification
import androidx.compose.foundation.clickable // Click interaction
import androidx.compose.foundation.interaction.MutableInteractionSource // Interaction source
import androidx.compose.foundation.layout.* // Layout components
import androidx.compose.foundation.lazy.LazyColumn // Vertical scrolling list
import androidx.compose.foundation.lazy.items // List item mapping
import androidx.compose.foundation.shape.CircleShape // Circular shape
import androidx.compose.foundation.shape.RoundedCornerShape // Rounded rectangle shape
import androidx.compose.material.icons.Icons // Material Icons
import androidx.compose.material.icons.automirrored.filled.Login // Login icon
import androidx.compose.material.icons.automirrored.filled.ReceiptLong // Receipt icon
import androidx.compose.material.icons.filled.* // All filled icons
import androidx.compose.material3.* // Material 3 components
import androidx.compose.runtime.* // State management
import androidx.compose.ui.Alignment // Alignment options
import androidx.compose.ui.Modifier // UI modifiers
import androidx.compose.ui.draw.clip // Clipping modifier
import androidx.compose.ui.graphics.Color // Color class
import androidx.compose.ui.graphics.graphicsLayer // Graphics transformations
import androidx.compose.ui.layout.ContentScale // Image scaling
import androidx.compose.ui.platform.LocalContext // Context access
import androidx.compose.ui.text.font.FontWeight // Font weight styles
import androidx.compose.ui.text.style.TextDecoration // Text decoration
import androidx.compose.ui.unit.dp // Density pixels
import androidx.compose.ui.unit.sp // Scalable pixels
import androidx.compose.ui.zIndex // Z-index ordering
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel integration
import androidx.navigation.NavController // Navigation controller
import assignment1.krzysztofoko.s16001089.data.* // Data models
import assignment1.krzysztofoko.s16001089.ui.components.* // Custom components
import coil.compose.AsyncImage // Image loading library
import com.google.firebase.auth.FirebaseAuth // Firebase auth
import kotlinx.coroutines.launch // Coroutine launching
import java.util.Locale // Locale support

/**
 * Main Entry Point for the Student Store Application.
 * This screen manages data filtering, search, and user-item interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onAboutClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayAudio: (Book) -> Unit,
    currentPlayingBookId: String?,
    isAudioPlaying: Boolean,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(
        repository = BookRepository(AppDatabase.getDatabase(LocalContext.current)),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val selectedMainCategory by viewModel.selectedMainCategory.collectAsState()
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val bookToRemove by viewModel.bookToRemove.collectAsState()
    
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()
    val filteredBooks by viewModel.filteredBooks.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val mainCategories = listOf("All", "Free", "University Courses", "University Gear", "Books", "Audio Books")
    val subCategoriesMap = mapOf(
        "Books" to listOf("All Genres", "Technology", "Cooking", "Fantasy", "Mystery", "Self-Help"),
        "Audio Books" to listOf("All Genres", "Self-Help", "Technology", "Cooking", "Mystery"),
        "University Courses" to listOf("All Departments", "Science", "Business", "Technology")
    )

    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) viewModel.setSearchVisible(false) }) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = "file:///android_asset/images/media/GlyndwrUniversity.jpg", contentDescription = "Logo", modifier = Modifier.size(32.dp).graphicsLayer { rotationZ = rotation.value }.clip(CircleShape), contentScale = ContentScale.Fit)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Glyndŵr Store", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    },
                    actions = {
                        TopBarSearchAction(isSearchVisible = isSearchVisible) { viewModel.setSearchVisible(true) }
                        IconButton(onClick = { viewModel.setSearchVisible(false); onToggleTheme() }) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme") }
                        IconButton(onClick = { viewModel.setSearchVisible(false); onAboutClick() }) { Icon(Icons.Default.Info, "About") }
                        if (!isLoggedIn) {
                            IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate("auth") }) { Icon(Icons.AutoMirrored.Filled.Login, "Login") }
                        } else {
                            IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate("dashboard") }) { Icon(Icons.Default.Dashboard, "Dashboard") }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    item { if (!isLoggedIn) PromotionBanner { viewModel.setSearchVisible(false); navController.navigate("auth") } else MemberWelcomeBanner() }
                    item { MainCategoryFilterBar(categories = mainCategories, selectedCategory = selectedMainCategory) { viewModel.selectMainCategory(it) } }
                    item { AnimatedVisibility(visible = subCategoriesMap.containsKey(selectedMainCategory)) { SubCategoryFilterBar(categories = subCategoriesMap[selectedMainCategory] ?: emptyList(), selectedCategory = selectedSubCategory) { viewModel.selectSubCategory(it) } } }

                    if (isLoading) {
                        item { Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(16.dp)); Text("Loading Data...", style = MaterialTheme.typography.labelSmall) } } }
                    } else if (error != null) {
                        item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)); Spacer(Modifier.height(8.dp)); Text("Error: $error", fontWeight = FontWeight.Bold); Button(onClick = onRefresh) { Text("Retry") } } } }
                    } else {
                        if (filteredBooks.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp).clickable { viewModel.setSearchVisible(false) }, contentAlignment = Alignment.Center) { Text("No results found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray) } }
                        } else {
                            items(filteredBooks) { book ->
                                val isLiked = wishlistIds.contains(book.id)
                                val isPurchased = purchasedIds.contains(book.id)
                                
                                BookItemCard(
                                    book = book,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    onClick = { viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") },
                                    imageOverlay = {
                                        if (isLoggedIn && book.isAudioBook && isPurchased) {
                                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { SpinningAudioButton(isPlaying = isAudioPlaying && currentPlayingBookId == book.id, onToggle = { onPlayAudio(book) }, size = 40) }
                                        }
                                    },
                                    trailingContent = {
                                        if (isLoggedIn) {
                                            IconButton(onClick = {
                                                viewModel.setSearchVisible(false)
                                                viewModel.toggleWishlist(book, isLiked) { msg ->
                                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                                }
                                            }, modifier = Modifier.size(24.dp)) { Icon(imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (isLiked) MaterialTheme.colorScheme.onSurface else Color.Gray, modifier = Modifier.size(20.dp)) }
                                        }
                                    },
                                    bottomContent = {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isPurchased) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (book.price > 0) {
                                                        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp)); Text(text = "Purchased", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold) }
                                                        }
                                                        Spacer(Modifier.width(8.dp))
                                                        IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate("invoiceCreating/${book.id}") }, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.ReceiptLong, "Invoice", tint = MaterialTheme.colorScheme.primary) }
                                                    } else {
                                                        val label = if (book.mainCategory == "University Gear") "Picked Up" else "In Library"
                                                        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LibraryAddCheck, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp)); Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold) }
                                                        }
                                                        if (book.mainCategory != "University Gear") {
                                                            Spacer(Modifier.width(8.dp))
                                                            IconButton(onClick = { if (isLoggedIn) { viewModel.setSearchVisible(false); viewModel.setBookToRemove(book) } }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error) }
                                                        }
                                                    }
                                                }
                                            } else if (book.price == 0.0) {
                                                Text(text = "FREE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                                            } else if (isLoggedIn) {
                                                val discountPrice = "£" + String.format(Locale.US, "%.2f", book.price * 0.9)
                                                Text(text = "£" + String.format(Locale.US, "%.2f", book.price), style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough), color = Color.Gray)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text(text = discountPrice, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                                            } else {
                                                Text(text = "£" + String.format(Locale.US, "%.2f", book.price), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }

                HomeSearchSection(
                    isSearchVisible = isSearchVisible, searchQuery = searchQuery, 
                    recentSearches = recentSearches,
                    onQueryChange = { viewModel.updateSearchQuery(it) }, 
                    onClearHistory = { viewModel.clearRecentSearches() },
                    onCloseClick = { viewModel.setSearchVisible(false) }, suggestions = suggestions,
                    onSuggestionClick = { book -> 
                        viewModel.saveSearchQuery(book.title)
                        viewModel.setSearchVisible(false)
                        navController.navigate("bookDetails/${book.id}") 
                    },
                    modifier = Modifier.padding(top = padding.calculateTopPadding()).zIndex(10f)
                )
            }
        }

        AppPopups.RemoveFromLibraryConfirmation(
            show = bookToRemove != null,
            bookTitle = bookToRemove?.title ?: "",
            onDismiss = { viewModel.setBookToRemove(null) },
            onConfirm = {
                viewModel.removePurchase(bookToRemove!!) { msg ->
                    viewModel.setBookToRemove(null)
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )
    }
}

class HomeViewModelFactory(
    private val repository: BookRepository,
    private val userDao: UserDao,
    private val userId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, userDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
