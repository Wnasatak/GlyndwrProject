package assignment1.krzysztofoko.s16001089.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        VerticalWavyBackground(isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE)
        
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
                ) { screenIsTablet ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = if (screenIsTablet) Arrangement.spacedBy(16.dp) else Arrangement.Start
                    ) {
                        if (screenIsTablet) {
                            item(span = { GridItemSpan(this.maxLineSpan) }) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        item(span = { GridItemSpan(this.maxLineSpan) }) { 
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = if (screenIsTablet) Modifier.widthIn(max = 550.dp).padding(vertical = 8.dp) else Modifier.fillMaxWidth()) {
                                    if (!isLoggedIn) {
                                        PromotionBanner(currentTheme) { 
                                            viewModel.setSearchVisible(false)
                                            navController.navigate(AppConstants.ROUTE_AUTH) 
                                        } 
                                    } else {
                                        MemberWelcomeBanner(user = uiState.localUser, theme = currentTheme) 
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
                                        Box(modifier = if (screenIsTablet) Modifier.widthIn(max = 550.dp) else Modifier.fillMaxWidth()) {
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
                                    Box(modifier = if (screenIsTablet) Modifier.widthIn(max = 550.dp) else Modifier.fillMaxWidth()) {
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

@Composable
fun PromotionBanner(theme: Theme, onGetStarted: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(brush = getBannerBrush(theme))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Glyndŵr Store",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                @Suppress("DEPRECATION")
                Text(
                    text = "Enrol now to unlock exclusive group-wide discounts across our entire catalog.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onGetStarted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Started", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MemberWelcomeBanner(user: UserLocal?, theme: Theme) {
    val firstName = user?.name?.split(" ")?.firstOrNull() ?: "Member"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(ProDesign.StandardPadding),
        tonalElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .background(brush = getBannerBrush(theme))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(photoUrl = user?.photoUrl, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    @Suppress("DEPRECATION")
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = firstName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    IconButton(onClick = { /* Could go to profile */ }) {
                        Icon(Icons.Default.AccountBox, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun getBannerBrush(theme: Theme): Brush {
    val colors = when (theme) {
        Theme.SKY -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
        )
        Theme.FOREST -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
        )
        Theme.DARK_BLUE -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
        Theme.DARK -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
            Color.Black.copy(alpha = 0.2f)
        )
        else -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
        )
    }
    return Brush.linearGradient(
        colors = colors,
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
    )
}
