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
 * Primary Discovery Screen (Home).
 */
@Composable
fun HomeScreen(
    userId: String,                   
    initialCategory: String? = null,  
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
        auditDao = AppDatabase.getDatabase(LocalContext.current).auditDao(),
        userId = userId,
        initialCategory = initialCategory 
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

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
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
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
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    
                    item { 
                        if (!isLoggedIn) {
                            PromotionBanner { 
                                viewModel.setSearchVisible(false)
                                navController.navigate(AppConstants.ROUTE_AUTH) 
                            } 
                        } else {
                            MemberWelcomeBanner(role = uiState.localUser?.role ?: "user") 
                        }
                    }

                    if (isLoggedIn) {
                        val enrolledPaidCourse = uiState.allBooks.find { 
                            it.mainCategory == AppConstants.CAT_COURSES && uiState.purchasedIds.contains(it.id) && it.price > 0.0
                        }
                        val enrolledFreeCourses = uiState.allBooks.filter { 
                            it.mainCategory == AppConstants.CAT_COURSES && uiState.purchasedIds.contains(it.id) && it.price <= 0.0
                        }

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
                    
                    item { 
                        MainCategoryFilterBar(
                            categories = AppConstants.MainCategories, 
                            selectedCategory = uiState.selectedMainCategory
                        ) { viewModel.selectMainCategory(it) } 
                    }
                    
                    item { 
                        AnimatedVisibility(visible = AppConstants.SubCategoriesMap.containsKey(uiState.selectedMainCategory)) { 
                            SubCategoryFilterBar(
                                categories = AppConstants.SubCategoriesMap[uiState.selectedMainCategory] ?: emptyList(), 
                                selectedCategory = uiState.selectedSubCategory
                            ) { viewModel.selectSubCategory(it) } 
                        } 
                    }

                    if (isLoading || uiState.isLoading) {
                        item { HomeLoadingState() }
                    } else if (error != null || uiState.error != null) {
                        item { HomeErrorState(error = error ?: uiState.error!!, onRetry = onRefresh) }
                    } else {
                        if (uiState.filteredBooks.isEmpty()) {
                            item { HomeEmptyState { viewModel.setSearchVisible(false) } }
                        } else {
                            items(uiState.filteredBooks) { book ->
                                val appStatus = uiState.applicationsMap[book.id]
                                val isPending = appStatus == "PENDING_REVIEW"

                                HomeBookItem(
                                    book = book,
                                    isLoggedIn = isLoggedIn,
                                    isPendingReview = isPending,
                                    userRole = uiState.localUser?.role,
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
