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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Main Entry Point for the Student Store Application.
 * This screen manages data filtering, search, and user-item interactions.
 */
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

    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (isSearchVisible) viewModel.setSearchVisible(false) }) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HomeTopBar(
                    isSearchVisible = isSearchVisible,
                    isLoggedIn = isLoggedIn,
                    isDarkTheme = isDarkTheme,
                    onSearchClick = { viewModel.setSearchVisible(true) },
                    onToggleTheme = { viewModel.setSearchVisible(false); onToggleTheme() },
                    onAboutClick = { viewModel.setSearchVisible(false); onAboutClick() },
                    onAuthClick = { viewModel.setSearchVisible(false); navController.navigate("auth") },
                    onDashboardClick = { viewModel.setSearchVisible(false); navController.navigate("dashboard") }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    item { if (!isLoggedIn) PromotionBanner { viewModel.setSearchVisible(false); navController.navigate("auth") } else MemberWelcomeBanner() }
                    item { MainCategoryFilterBar(categories = AppConstants.MainCategories, selectedCategory = selectedMainCategory) { viewModel.selectMainCategory(it) } }
                    item { AnimatedVisibility(visible = AppConstants.SubCategoriesMap.containsKey(selectedMainCategory)) { SubCategoryFilterBar(categories = AppConstants.SubCategoriesMap[selectedMainCategory] ?: emptyList(), selectedCategory = selectedSubCategory) { viewModel.selectSubCategory(it) } } }

                    if (isLoading) {
                        item { HomeLoadingState() }
                    } else if (error != null) {
                        item { HomeErrorState(error = error, onRetry = onRefresh) }
                    } else {
                        if (filteredBooks.isEmpty()) {
                            item { HomeEmptyState { viewModel.setSearchVisible(false) } }
                        } else {
                            items(filteredBooks) { book ->
                                HomeBookItem(
                                    book = book,
                                    isLoggedIn = isLoggedIn,
                                    isLiked = wishlistIds.contains(book.id),
                                    isPurchased = purchasedIds.contains(book.id),
                                    isAudioPlaying = isAudioPlaying && currentPlayingBookId == book.id,
                                    onItemClick = { viewModel.setSearchVisible(false); navController.navigate("bookDetails/${book.id}") },
                                    onToggleWishlist = {
                                        viewModel.setSearchVisible(false)
                                        viewModel.toggleWishlist(book, wishlistIds.contains(book.id)) { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    },
                                    onPlayAudio = { onPlayAudio(book) },
                                    onInvoiceClick = { viewModel.setSearchVisible(false); navController.navigate("invoiceCreating/${book.id}") },
                                    onRemoveClick = { viewModel.setSearchVisible(false); viewModel.setBookToRemove(book) }
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
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding()).zIndex(10f)
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
