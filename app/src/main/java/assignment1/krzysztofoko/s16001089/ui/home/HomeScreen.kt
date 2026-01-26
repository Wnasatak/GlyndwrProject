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
@OptIn(ExperimentalMaterial3Api::class) // Using experimental M3 APIs
@Composable // Mark as Composable function
fun HomeScreen( // Home screen component
    navController: NavController, // Navigation controller
    isLoggedIn: Boolean, // User logged in flag
    allBooks: List<Book>, // List of all books
    isLoading: Boolean, // Loading state flag
    error: String?, // Error message
    onRefresh: () -> Unit, // Refresh callback
    onAboutClick: () -> Unit, // About click callback
    isDarkTheme: Boolean, // Dark theme flag
    onToggleTheme: () -> Unit, // Theme toggle callback
    onPlayAudio: (Book) -> Unit, // Audio play callback
    currentPlayingBookId: String?, // Currently playing ID
    isAudioPlaying: Boolean, // Is audio playing flag
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory( // Initialize ViewModel
        repository = BookRepository(AppDatabase.getDatabase(LocalContext.current)), // Pass repository
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(), // Pass DAO
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // Pass user ID
    ))
) {
    // 1. Core Logic & Database Context
    val scope = rememberCoroutineScope() // Coroutine scope for UI tasks
    val snackbarHostState = remember { SnackbarHostState() } // Snackbar state
    
    // UI State from ViewModel
    val selectedMainCategory by viewModel.selectedMainCategory.collectAsState() // Observe main category
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState() // Observe sub category
    val searchQuery by viewModel.searchQuery.collectAsState() // Observe search query
    val isSearchVisible by viewModel.isSearchVisible.collectAsState() // Observe search visibility
    val bookToRemove by viewModel.bookToRemove.collectAsState() // Observe book for removal
    
    val wishlistIds by viewModel.wishlistIds.collectAsState() // Observe wishlist IDs
    val purchasedIds by viewModel.purchasedIds.collectAsState() // Observe purchased IDs
    val filteredBooks by viewModel.filteredBooks.collectAsState() // Observe filtered items
    val suggestions by viewModel.suggestions.collectAsState() // Observe search suggestions

    // Animation for the logo to spin once on entry
    val rotation = remember { Animatable(0f) } // Animation state
    LaunchedEffect(Unit) { // Trigger on launch
        rotation.animateTo( // Run animation
            targetValue = 360f, // Spin 360 degrees
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing) // Duration and easing
        )
    }

    // 3. Category Data Definition
    val mainCategories = listOf("All", "Free", "University Courses", "University Gear", "Books", "Audio Books") // Main categories
    val subCategoriesMap = mapOf( // Sub-category mapping
        "Books" to listOf("All Genres", "Technology", "Cooking", "Fantasy", "Mystery", "Self-Help"),
        "Audio Books" to listOf("All Genres", "Self-Help", "Technology", "Cooking", "Mystery"),
        "University Courses" to listOf("All Departments", "Science", "Business", "Technology")
    )

    // Main Scaffold Layout
    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) viewModel.setSearchVisible(false) }) { // Root container
        VerticalWavyBackground(isDarkTheme = isDarkTheme) // Animated background
        
        Scaffold( // Material scaffold
            containerColor = Color.Transparent, // Transparent background
            snackbarHost = { SnackbarHost(snackbarHostState) }, // Snackbar host
            topBar = { // App bar section
                // Branded Top Navigation Bar
                TopAppBar( // Top app bar
                    windowInsets = WindowInsets(0, 0, 0, 0), // Remove insets
                    title = { // Title content
                        Row(verticalAlignment = Alignment.CenterVertically) { // Row for logo and title
                            AsyncImage(model = "file:///android_asset/images/media/GlyndwrUniversity.jpg", contentDescription = "Logo", modifier = Modifier.size(32.dp).graphicsLayer { rotationZ = rotation.value }.clip(CircleShape), contentScale = ContentScale.Fit) // University logo
                            Spacer(Modifier.width(8.dp)) // Spacer
                            Text(text = "Glyndŵr Store", fontWeight = FontWeight.Black, fontSize = 16.sp) // Store name
                        }
                    },
                    actions = { // Actions in bar
                        TopBarSearchAction(isSearchVisible = isSearchVisible) { viewModel.setSearchVisible(true) } // Search icon
                        IconButton(onClick = { viewModel.setSearchVisible(false); onToggleTheme() }) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme") } // Theme toggle
                        IconButton(onClick = { viewModel.setSearchVisible(false); onAboutClick() }) { Icon(Icons.Default.Info, "About") } // About info
                        if (!isLoggedIn) { // Login logic
                            IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate("auth") }) { Icon(Icons.AutoMirrored.Filled.Login, "Login") } // Login button
                        } else {
                            IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate("dashboard") }) { Icon(Icons.Default.Dashboard, "Dashboard") } // Dashboard button
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)) // Bar styling
                )
            }
        ) { padding -> // Main content area
            Box(modifier = Modifier.fillMaxSize()) { // Container for scrolling content
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) { // Scrolling product list
                    // Promotional Banner Section
                    item { if (!isLoggedIn) PromotionBanner { viewModel.setSearchVisible(false); navController.navigate("auth") } else MemberWelcomeBanner() } // Welcome banner
                    
                    // Filter Bar Section
                    item { MainCategoryFilterBar(categories = mainCategories, selectedCategory = selectedMainCategory) { viewModel.selectMainCategory(it) } } // Main filters
                    item { AnimatedVisibility(visible = subCategoriesMap.containsKey(selectedMainCategory)) { SubCategoryFilterBar(categories = subCategoriesMap[selectedMainCategory] ?: emptyList(), selectedCategory = selectedSubCategory) { viewModel.selectSubCategory(it) } } } // Sub-filters

                    // Loading & Error States
                    if (isLoading) { // Show loading spinner
                        item { Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(16.dp)); Text("Loading Local Data...", style = MaterialTheme.typography.labelSmall) } } }
                    } else if (error != null) { // Show error message
                        item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)); Spacer(Modifier.height(8.dp)); Text("Error: $error", fontWeight = FontWeight.Bold); Button(onClick = onRefresh) { Text("Retry") } } } }
                    } else {
                        // Product List
                        if (filteredBooks.isEmpty()) { // Show "No results"
                            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp).clickable { viewModel.setSearchVisible(false) }, contentAlignment = Alignment.Center) { Text("No results found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray) } }
                        } else {
                            items(filteredBooks) { book -> // Iterate through books
                                val isLiked = wishlistIds.contains(book.id) // Liked check
                                val isPurchased = purchasedIds.contains(book.id) // Purchased check
                                
                                // Interactive Product Card
                                BookItemCard( // Product card UI
                                    book = book, // Current book data
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), // Spacing
                                    onClick = { viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") }, // Click action
                                    imageOverlay = { // Custom image overlay
                                        if (isLoggedIn && book.isAudioBook && isPurchased) { // Play button for audiobooks
                                            // Show Play button for purchased audiobooks
                                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { SpinningAudioButton(isPlaying = isAudioPlaying && currentPlayingBookId == book.id, onToggle = { onPlayAudio(book) }, size = 40) } // Audio playback button
                                        }
                                    },
                                    trailingContent = { // Trailing item actions
                                        if (isLoggedIn) { // Only if logged in
                                            // Like/Wishlist Functionality
                                            IconButton(onClick = { // Wishlist toggle
                                                viewModel.setSearchVisible(false)
                                                viewModel.toggleWishlist(book, isLiked) { msg ->
                                                    scope.launch { snackbarHostState.showSnackbar(msg) } // Show feedback
                                                }
                                            }, modifier = Modifier.size(24.dp)) { Icon(imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (isLiked) MaterialTheme.colorScheme.onSurface else Color.Gray, modifier = Modifier.size(20.dp)) } // Heart icon
                                        }
                                    },
                                    bottomContent = { // Bottom part of card
                                        Spacer(modifier = Modifier.height(12.dp)) // Spacer
                                        Row(verticalAlignment = Alignment.CenterVertically) { // Item status and price
                                            if (isPurchased) { // If user owns item
                                                // Show purchase status and actions (Invoices/Removal)
                                                Row(verticalAlignment = Alignment.CenterVertically) { // Status layout
                                                    if (book.price > 0) { // Paid item status
                                                        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) { // Chip styling
                                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp)); Text(text = "Purchased", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold) } // Purchased text
                                                        }
                                                        Spacer(Modifier.width(8.dp)) // Spacer
                                                        IconButton(onClick = { viewModel.setSearchVisible(false); navController.navigate("invoiceCreating/${book.id}") }, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.ReceiptLong, "Invoice", tint = MaterialTheme.colorScheme.primary) } // Invoice button
                                                    } else { // Free item status
                                                        // Free Item removal logic
                                                        val label = if (book.mainCategory == "University Gear") "Picked Up" else "In Library" // Dynamic label
                                                        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) { // Chip styling
                                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LibraryAddCheck, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp)); Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold) } // Ownership text
                                                        }
                                                        if (book.mainCategory != "University Gear") { // Gear is picked up, others in library
                                                            Spacer(Modifier.width(8.dp)) // Spacer
                                                            IconButton(onClick = { if (isLoggedIn) { viewModel.setSearchVisible(false); viewModel.setBookToRemove(book) } }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error) } // Removal icon
                                                        }
                                                    }
                                                }
                                            } else if (book.price == 0.0) { // Free item price
                                                Text(text = "FREE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50)) // Free label
                                            } else if (isLoggedIn) { // Paid item student discount
                                                // Dynamic Student Discount Calculation
                                                val discountPrice = String.format(Locale.US, "%.2f", book.price * 0.9) // Calculate 10% discount
                                                Text(text = "£${String.format(Locale.US, "%.2f", book.price)}", style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough), color = Color.Gray) // Original price
                                                Spacer(modifier = Modifier.width(8.dp)) // Spacer
                                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text(text = "£$discountPrice", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) } // Discounted price
                                            } else { // Guest price
                                                Text(text = "£${String.format(Locale.US, "%.2f", book.price)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) // Regular price
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) } // Bottom list spacer
                }

                // Global Search Overlay
                HomeSearchSection( // Floating search overlay
                    isSearchVisible = isSearchVisible, searchQuery = searchQuery, onQueryChange = { viewModel.updateSearchQuery(it) }, onCloseClick = { viewModel.setSearchVisible(false) }, suggestions = suggestions, // State and handlers
                    onSuggestionClick = { book -> viewModel.updateSearchQuery(book.title); viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") }, // Suggestion handler
                    modifier = Modifier.padding(top = padding.calculateTopPadding()).zIndex(10f) // Positioning
                )
            }
        }

        // Implementation of the Centralized Library Removal Popup
        AppPopups.RemoveFromLibraryConfirmation( // Removal confirmation dialog
            show = bookToRemove != null, // Visibility flag
            bookTitle = bookToRemove?.title ?: "", // Book title
            onDismiss = { viewModel.setBookToRemove(null) }, // Dismiss handler
            onConfirm = { // Confirmation handler
                viewModel.removePurchase(bookToRemove!!) { msg -> // Remove from library
                    viewModel.setBookToRemove(null) // Reset selection
                    scope.launch { snackbarHostState.showSnackbar(msg) } // Show feedback
                }
            }
        )
    }
}

class HomeViewModelFactory( // Factory for HomeViewModel
    private val repository: BookRepository, // Repo dependency
    private val userDao: UserDao, // DAO dependency
    private val userId: String // ID dependency
) : androidx.lifecycle.ViewModelProvider.Factory { // Implement factory interface
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T { // Create method
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) { // Check class type
            @Suppress("UNCHECKED_CAST") // Safe cast
            return HomeViewModel(repository, userDao, userId) as T // Return instance
        }
        throw IllegalArgumentException("Unknown ViewModel class") // Throw on invalid type
    }
}
