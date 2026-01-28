package assignment1.krzysztofoko.s16001089.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import kotlinx.coroutines.launch

/**
 * Primary Discovery Screen (Home).
 * 
 * This is the central hub for users to explore the store's catalog. It features
 * dynamic category filtering, a persistent search interface, and context-aware 
 * banners based on the user's authentication status.
 * 
 * It also acts as a gateway to academic classrooms for enrolled students and
 * provides immediate access to digital content like audiobooks.
 */
@Composable
fun HomeScreen(
    userId: String,                   // The ID of the currently authenticated user
    initialCategory: String? = null,  // Optional pre-selected category filter from navigation
    navController: NavController,     // Controller used for navigating to details, auth, or dashboard
    isLoggedIn: Boolean,              // Flag indicating the user's login status
    isLoading: Boolean,               // State indicating if the catalog is being fetched
    error: String?,                   // Holds any errors encountered during data loading
    onRefresh: () -> Unit,            // Callback to retry or refresh the catalog data
    onAboutClick: () -> Unit,         // Jumps to the 'About' information section
    isDarkTheme: Boolean,             // The current global UI theme state
    onToggleTheme: () -> Unit,        // Function to flip between Light and Dark modes
    onPlayAudio: (Book) -> Unit,      // Triggers global audio playback for digital items
    currentPlayingBookId: String?,    // ID of the active media item for UI feedback
    isAudioPlaying: Boolean,          // Status of the audio engine
    // Custom ViewModel initialization with required DAOs and logic
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(
        repository = BookRepository(AppDatabase.getDatabase(LocalContext.current)),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        userId = userId,
        initialCategory = initialCategory 
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collecting the complex UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Root container handling gestures to dismiss the search overlay when clicking outside
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, 
                indication = null
            ) { 
                if (uiState.isSearchVisible) viewModel.setSearchVisible(false) 
            }
    ) {
        // Shared animated background component
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Allows the wavy background to be visible
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Customized top navigation bar with branding and quick actions
                HomeTopBar(
                    isSearchVisible = uiState.isSearchVisible,
                    isLoggedIn = isLoggedIn,
                    isDarkTheme = isDarkTheme,
                    onSearchClick = { viewModel.setSearchVisible(true) },
                    onToggleTheme = { viewModel.setSearchVisible(false); onToggleTheme() },
                    onAboutClick = { viewModel.setSearchVisible(false); onAboutClick() },
                    onAuthClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_AUTH) },
                    onDashboardClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_DASHBOARD) }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Primary scrollable area for discovery content
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    
                    // Welcome Area: Promotional banner for guests or greeting for members
                    item { 
                        if (!isLoggedIn) {
                            PromotionBanner { 
                                viewModel.setSearchVisible(false)
                                navController.navigate(AppConstants.ROUTE_AUTH) 
                            } 
                        } else {
                            MemberWelcomeBanner() 
                        }
                    }

                    // ACADEMIC SHORTCUTS: Only visible to logged-in students with active enrollments
                    if (isLoggedIn) {
                        // Find the paid course enrollment if it exists
                        val enrolledPaidCourse = uiState.allBooks.find { 
                            it.mainCategory == AppConstants.CAT_COURSES && uiState.purchasedIds.contains(it.id) && it.price > 0.0
                        }
                        // List all free course enrollments
                        val enrolledFreeCourses = uiState.allBooks.filter { 
                            it.mainCategory == AppConstants.CAT_COURSES && uiState.purchasedIds.contains(it.id) && it.price <= 0.0
                        }

                        // Display prominent course entry points
                        if (enrolledPaidCourse != null) {
                            item {
                                EnrolledCourseHeader(
                                    course = enrolledPaidCourse,
                                    onEnterClassroom = { courseId -> 
                                        navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$courseId") 
                                    }
                                )
                            }
                        }
                        
                        items(enrolledFreeCourses) { freeCourse ->
                            FreeCourseHeader(
                                course = freeCourse,
                                onEnterClassroom = { courseId -> 
                                    navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$courseId") 
                                }
                            )
                        }
                    }
                    
                    // Main Filter: Top-level categories (All, Books, Courses, etc.)
                    item { 
                        MainCategoryFilterBar(
                            categories = AppConstants.MainCategories, 
                            selectedCategory = uiState.selectedMainCategory
                        ) { viewModel.selectMainCategory(it) } 
                    }
                    
                    // Sub-Category Filter: Dynamically appears when a specific main category is picked
                    item { 
                        AnimatedVisibility(visible = AppConstants.SubCategoriesMap.containsKey(uiState.selectedMainCategory)) { 
                            SubCategoryFilterBar(
                                categories = AppConstants.SubCategoriesMap[uiState.selectedMainCategory] ?: emptyList(), 
                                selectedCategory = uiState.selectedSubCategory
                            ) { viewModel.selectSubCategory(it) } 
                        } 
                    }

                    // Conditional UI for Loading and Error states
                    if (isLoading || uiState.isLoading) {
                        item { HomeLoadingState() }
                    } else if (error != null || uiState.error != null) {
                        item { HomeErrorState(error = error ?: uiState.error!!, onRetry = onRefresh) }
                    } else {
                        // Display the filtered product grid
                        if (uiState.filteredBooks.isEmpty()) {
                            item { HomeEmptyState { viewModel.setSearchVisible(false) } }
                        } else {
                            items(uiState.filteredBooks) { book ->
                                // Individual Product Item
                                HomeBookItem(
                                    book = book,
                                    isLoggedIn = isLoggedIn,
                                    isLiked = uiState.wishlistIds.contains(book.id),
                                    isPurchased = uiState.purchasedIds.contains(book.id),
                                    isAudioPlaying = isAudioPlaying && currentPlayingBookId == book.id,
                                    onItemClick = { 
                                        viewModel.setSearchVisible(false)
                                        navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") 
                                    },
                                    onToggleWishlist = {
                                        viewModel.setSearchVisible(false)
                                        viewModel.toggleWishlist(book, uiState.wishlistIds.contains(book.id)) { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    },
                                    onPlayAudio = { onPlayAudio(book) },
                                    onInvoiceClick = { 
                                        viewModel.setSearchVisible(false)
                                        navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/${book.id}") 
                                    },
                                    onRemoveClick = { 
                                        viewModel.setSearchVisible(false)
                                        viewModel.setBookToRemove(book) 
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }

                // Overlay Search Section: Appears on top of the main list when search is active
                HomeSearchSection(
                    isSearchVisible = uiState.isSearchVisible, 
                    searchQuery = uiState.searchQuery, 
                    recentSearches = uiState.recentSearches,
                    onQueryChange = { viewModel.updateSearchQuery(it) }, 
                    onClearHistory = { viewModel.clearRecentSearches() },
                    onCloseClick = { viewModel.setSearchVisible(false) }, 
                    suggestions = uiState.suggestions,
                    onSuggestionClick = { book -> 
                        viewModel.saveSearchQuery(book.title)
                        viewModel.setSearchVisible(false)
                        navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${book.id}") 
                    },
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding()).zIndex(10f)
                )
            }
        }

        /**
         * Global Popup: Removal Confirmation
         * Ensures users actually want to delete a digital item from their local library.
         */
        AppPopups.RemoveFromLibraryConfirmation(
            show = uiState.bookToRemove != null,
            bookTitle = uiState.bookToRemove?.title ?: "",
            onDismiss = { viewModel.setBookToRemove(null) },
            onConfirm = {
                viewModel.removePurchase(uiState.bookToRemove!!) { msg ->
                    viewModel.setBookToRemove(null)
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )
    }
}
