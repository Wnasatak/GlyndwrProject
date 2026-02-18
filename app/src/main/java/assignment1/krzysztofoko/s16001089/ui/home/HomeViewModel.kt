package assignment1.krzysztofoko.s16001089.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * HomeViewModel.kt
 *
 * This ViewModel serves as the brain of the Home screen. It leverages Kotlin Coroutines 
 * and Flows to provide a highly reactive and unified UI state. It handles complex 
 * multi-stream data combination, sophisticated catalogue filtering, search orchestration, 
 * and persistent user interactions like wishlist management and library cleanup.
 */
class HomeViewModel(
    private val repository: BookRepository, 
    private val userDao: UserDao,
    classroomDao: ClassroomDao, 
    private val auditDao: AuditDao,
    private val userId: String,              
    initialCategory: String? = null          
) : ViewModel() {

    // Triggers a complete re-fetch of the combined repository data.
    private val _refreshTrigger = MutableStateFlow(0)
    
    // Internal state for all filtering and UI visibility settings.
    private val _filterSettings = MutableStateFlow(
        FilterSettings(
            mainCategory = initialCategory ?: AppConstants.CAT_ALL,
            subCategory = AppConstants.getDefaultSubcategory(initialCategory ?: AppConstants.CAT_ALL)
        )
    )

    /**
     * Primary UI State Stream:
     * Combines 10 different data sources into a single, cohesive HomeUiState.
     * This uses 'flatMapLatest' and 'combine' to ensure the UI is always in sync with the database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        _refreshTrigger.flatMapLatest { repository.getAllCombinedData(userId) }, // Books and Prices
        if (userId.isNotEmpty()) userDao.getWishlistIds(userId) else flowOf(emptyList()), // User's Likes
        if (userId.isNotEmpty()) userDao.getPurchaseIds(userId) else flowOf(emptyList()), // User's Library
        if (userId.isNotEmpty()) userDao.getRecentSearches(userId) else flowOf(emptyList()), // History
        if (userId.isNotEmpty()) userDao.getWalletHistory(userId) else flowOf(emptyList()), // Money
        _filterSettings, // User's current filters
        if (userId.isNotEmpty()) userDao.getUserFlow(userId) else flowOf(null), // Profile Details
        if (userId.isNotEmpty()) userDao.getAllEnrollmentsFlow() else flowOf(emptyList()), // Course Applications
        classroomDao.getAllActiveSessions(), // Live VLE Status
        if (userId.isNotEmpty()) userDao.getNotificationsForUser(userId) else flowOf(emptyList()) // Alerts
    ) { array ->
        // Extraction and Casting of individual stream results.
        @Suppress("UNCHECKED_CAST") val books = array[0] as List<Book>?
        @Suppress("UNCHECKED_CAST") val wishlist = array[1] as List<String>
        @Suppress("UNCHECKED_CAST") val purchased = array[2] as List<String>
        @Suppress("UNCHECKED_CAST") val searches = array[3] as List<String>
        @Suppress("UNCHECKED_CAST") val wallet = array[4] as List<WalletTransaction>
        @Suppress("UNCHECKED_CAST") val filters = array[5] as FilterSettings
        @Suppress("UNCHECKED_CAST") val localUser = array[6] as UserLocal?
        @Suppress("UNCHECKED_CAST") val enrollments = array[7] as List<CourseEnrollmentDetails>
        @Suppress("UNCHECKED_CAST") val activeSessions = array[8] as List<LiveSession>
        @Suppress("UNCHECKED_CAST") val notifications = array[9] as List<NotificationLocal>

        val allBooks = books ?: emptyList()
        // Map course IDs to status strings for quick badge lookup.
        val appMap = enrollments.filter { it.userId == userId }.associate { it.courseId to it.status }
        
        // Filter live sessions to only show those for which the student is enrolled.
        val userSessions = activeSessions.filter { session ->
            purchased.contains(session.courseId)
        }

        val isAdminOrTutor = localUser?.role == "admin" || localUser?.role == "teacher" || localUser?.role == "tutor"

        // --- FILTERING LOGIC --- //
        val filtered = allBooks.filter { book ->
            // Staff members don't see academic courses in the general discovery feed.
            if (isAdminOrTutor && book.mainCategory == AppConstants.CAT_COURSES) return@filter false

            // Match based on the primary category (e.g., Books, Gear, Free).
            val matchMain = when (filters.mainCategory) {
                AppConstants.CAT_ALL -> true
                AppConstants.CAT_FREE -> book.price == 0.0 
                else -> book.mainCategory.equals(filters.mainCategory, ignoreCase = true)
            }
            // Match based on sub-genre.
            val matchSub = if (filters.subCategory.contains("All", ignoreCase = true) || filters.mainCategory == AppConstants.CAT_FREE) true 
                           else book.category.equals(filters.subCategory, ignoreCase = true)
            // Match based on text search (Title, Author, or Genre).
            val matchQuery = if (filters.searchQuery.isEmpty()) true 
                             else book.title.contains(filters.searchQuery, ignoreCase = true) || 
                                  book.author.contains(filters.searchQuery, ignoreCase = true) ||
                                  book.category.contains(filters.searchQuery, ignoreCase = true)
            matchMain && matchSub && matchQuery
        }

        // Live search suggestions limited to 5 results.
        val suggestions = if (filters.searchQuery.length < 2) emptyList()
        else allBooks.filter { 
            val isCourse = it.mainCategory == AppConstants.CAT_COURSES
            if (isAdminOrTutor && isCourse) return@filter false
            
            it.title.contains(filters.searchQuery, ignoreCase = true) || it.author.contains(filters.searchQuery, ignoreCase = true)
        }.take(5)

        // Compile all individual pieces of data into the final UI state object.
        HomeUiState(
            allBooks = allBooks,
            filteredBooks = filtered,
            wishlistIds = wishlist.toSet(),
            purchasedIds = purchased.toSet(),
            unreadNotificationsCount = notifications.count { !it.isRead },
            applicationsMap = appMap,
            selectedMainCategory = filters.mainCategory,
            selectedSubCategory = filters.subCategory,
            searchQuery = filters.searchQuery,
            isSearchVisible = filters.isSearchVisible,
            suggestions = suggestions,
            recentSearches = searches,
            isLoading = false,
            error = null,
            bookToRemove = filters.bookToRemove,
            walletHistory = wallet,
            showWalletHistory = filters.showWalletHistory,
            localUser = localUser,
            activeLiveSessions = userSessions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    /**
     * Internal helper to record user actions in the system audit log.
     */
    private fun addLog(action: String, targetId: String, details: String) {
        viewModelScope.launch {
            val userName = uiState.value.localUser?.name ?: "User"
            auditDao.insertLog(SystemLog(
                userId = userId,
                userName = userName,
                action = action,
                targetId = targetId,
                details = details,
                logType = "USER"
            ))
        }
    }

    // --- UI ACTION HANDLERS --- //

    fun selectMainCategory(category: String) {
        _filterSettings.update { 
            it.copy(
                mainCategory = category,
                subCategory = AppConstants.getDefaultSubcategory(category),
                isSearchVisible = false,
                searchQuery = ""
            )
        }
    }

    fun selectSubCategory(category: String) {
        _filterSettings.update { it.copy(subCategory = category, isSearchVisible = false) }
    }

    fun updateSearchQuery(query: String) {
        _filterSettings.update { it.copy(searchQuery = query) }
    }

    /**
     * Persists a successful search query to the user's history and audit logs.
     */
    fun saveSearchQuery(query: String) {
        if (query.isBlank() || userId.isEmpty()) return
        viewModelScope.launch {
            userDao.addSearchQuery(SearchHistoryItem(userId = userId, query = query.trim()))
            addLog("SEARCH", "none", "User searched for: $query")
        }
    }

    fun clearRecentSearches() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            userDao.clearSearchHistory(userId) // Wipe from DB.
            addLog("CLEAR_HISTORY", "none", "User cleared their search history.")
        }
    }

    fun setSearchVisible(visible: Boolean) {
        _filterSettings.update { 
            it.copy(
                isSearchVisible = visible,
                searchQuery = if (!visible) "" else it.searchQuery // Clear text when closing.
            )
        }
    }

    fun setBookToRemove(book: Book?) {
        _filterSettings.update { it.copy(bookToRemove = book) }
    }

    /**
     * Toggles an item's status in the user's personal wishlist.
     */
    fun toggleWishlist(book: Book, isLiked: Boolean, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (isLiked) {
                userDao.removeFromWishlist(userId, book.id) // Remove if already liked.
                addLog("WISHLIST_REMOVE", book.id, "Removed from favorites: ${book.title}")
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                // Ensure it's in history before adding to wishlist.
                userDao.addToHistory(HistoryItem(userId, book.id))
                userDao.addToWishlist(WishlistItem(userId, book.id)) // Add new record.
                addLog("WISHLIST_ADD", book.id, "Added to favorites: ${book.title}")
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    /**
     * Removes a record from the user's library.
     */
    fun removePurchase(book: Book, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.deletePurchase(userId, book.id) // Remove record.
            addLog("LIBRARY_REMOVE", book.id, "Removed from library: ${book.title}")
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}

/**
 * Internal private data class for managing filtering states before they are combined.
 */
private data class FilterSettings(
    val mainCategory: String = AppConstants.CAT_ALL,
    val subCategory: String = "All Genres",
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val bookToRemove: Book? = null,
    val showWalletHistory: Boolean = false
)

/**
 * Factory class for constructing the HomeViewModel with its multiple DAOs and identifying parameters.
 */
class HomeViewModelFactory(
    private val repository: BookRepository,
    private val userDao: UserDao,
    private val classroomDao: ClassroomDao,
    private val auditDao: AuditDao,
    private val userId: String,
    private val initialCategory: String? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, userDao, classroomDao, auditDao, userId, initialCategory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
