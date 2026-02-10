package assignment1.krzysztofoko.s16001089.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import kotlinx.coroutines.launch

/**
 * Primary Discovery Screen (Home).
 * Optimized for both phone (list) and tablet (centered grid) using centralized Adaptive utilities.
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
    currentTheme: Theme,             
    onThemeChange: (Theme) -> Unit,        
    onPlayAudio: (Book) -> Unit,      
    currentPlayingBookId: String?,    
    isAudioPlaying: Boolean,          
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(
        repository = BookRepository(AppDatabase.getDatabase(LocalContext.current)),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        classroomDao = AppDatabase.getDatabase(LocalContext.current).classroomDao(),
        auditDao = AppDatabase.getDatabase(LocalContext.current).auditDao(),
        userId = userId,
        initialCategory = initialCategory 
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    
    val isTablet = isTablet()
    val columns = if (isTablet) 2 else 1

    val isAdminOrTutor = uiState.localUser?.role == "admin" || uiState.localUser?.role == "teacher" || uiState.localUser?.role == "tutor"

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
        VerticalWavyBackground(isDarkTheme = currentTheme == Theme.DARK)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HomeTopBar(
                    isSearchVisible = uiState.isSearchVisible,
                    isLoggedIn = isLoggedIn,
                    currentTheme = currentTheme,
                    onSearchClick = { viewModel.setSearchVisible(true) },
                    onThemeChange = { viewModel.setSearchVisible(false); onThemeChange(it) },
                    onAboutClick = { viewModel.setSearchVisible(false); onAboutClick() },
                    onAuthClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_AUTH) },
                    onDashboardClick = { viewModel.setSearchVisible(false); navController.navigate(AppConstants.ROUTE_DASHBOARD) }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                AdaptiveScreenContainer(
                    maxWidth = AdaptiveWidths.Wide
                ) { isTablet ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = if (isTablet) Arrangement.spacedBy(16.dp) else Arrangement.Start
                    ) {
                        if (isTablet) {
                            item(span = { GridItemSpan(this.maxLineSpan) }) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        item(span = { GridItemSpan(this.maxLineSpan) }) { 
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = if (isTablet) Modifier.widthIn(max = 550.dp).padding(vertical = 8.dp) else Modifier.fillMaxWidth()) {
                                    if (!isLoggedIn) {
                                        PromotionBanner { 
                                            viewModel.setSearchVisible(false)
                                            navController.navigate(AppConstants.ROUTE_AUTH) 
                                        } 
                                    } else {
                                        MemberWelcomeBanner(user = uiState.localUser) 
                                    }
                                }
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
                                val isLive = uiState.activeLiveSessions.any { it.courseId == enrolledPaidCourse.id }
                                item(span = { GridItemSpan(this.maxLineSpan) }) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                                        Box(modifier = if (isTablet) Modifier.widthIn(max = 550.dp) else Modifier.fillMaxWidth()) {
                                            EnrolledCourseHeader(
                                                course = enrolledPaidCourse,
                                                isLive = isLive,
                                                onEnterClassroom = { courseId -> 
                                                    navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$courseId") 
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            items(enrolledFreeCourses, span = { GridItemSpan(this.maxLineSpan) }) { freeCourse ->
                                val isLive = uiState.activeLiveSessions.any { it.courseId == freeCourse.id }
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = if (isTablet) Modifier.widthIn(max = 550.dp) else Modifier.fillMaxWidth()) {
                                        FreeCourseHeader(
                                            course = freeCourse,
                                            isLive = isLive,
                                            onEnterClassroom = { courseId -> 
                                                navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$courseId") 
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        item(span = { GridItemSpan(this.maxLineSpan) }) { 
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                                MainCategoryFilterBar(
                                    categories = if (isAdminOrTutor) AppConstants.MainCategories.filter { it != AppConstants.CAT_COURSES } else AppConstants.MainCategories, 
                                    selectedCategory = uiState.selectedMainCategory
                                ) { viewModel.selectMainCategory(it) } 
                            }
                        }
                        
                        item(span = { GridItemSpan(this.maxLineSpan) }) { 
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                                AnimatedVisibility(visible = AppConstants.SubCategoriesMap.containsKey(uiState.selectedMainCategory)) { 
                                    SubCategoryFilterBar(
                                        categories = AppConstants.SubCategoriesMap[uiState.selectedMainCategory] ?: emptyList(), 
                                        selectedCategory = uiState.selectedSubCategory
                                    ) { viewModel.selectSubCategory(it) } 
                                }
                            }
                        }

                        if (isLoading || uiState.isLoading) {
                            item(span = { GridItemSpan(this.maxLineSpan) }) { HomeLoadingState() }
                        } else if (error != null || uiState.error != null) {
                            item(span = { GridItemSpan(this.maxLineSpan) }) { HomeErrorState(error = error ?: uiState.error!!, onRetry = onRefresh) }
                        } else {
                            if (uiState.filteredBooks.isEmpty()) {
                                item(span = { GridItemSpan(this.maxLineSpan) }) { HomeEmptyState { viewModel.setSearchVisible(false) } }
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
                        item(span = { GridItemSpan(this.maxLineSpan) }) { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }

                // Centered search section
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = paddingValues.calculateTopPadding()),
                    contentAlignment = Alignment.TopCenter
                ) {
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
                        modifier = Modifier
                            .then(if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth())
                            .zIndex(10f)
                    )
                }
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
