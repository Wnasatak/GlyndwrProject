package assignment1.krzysztofoko.s16001089.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 * 
 * This class serves as the central state manager for the main landing page.
 * It reactively combines product data from the repository with user-specific 
 * data (wishlist, purchases, history) and UI filter settings to produce
 * a single unified UI state.
 */
class HomeViewModel(
    private val repository: BookRepository, // Repository providing combined access to Books, Gear, Courses
    private val userDao: UserDao,           // DAO for user-specific data like favorites and history
    private val userId: String,              // Unique identifier for the currently logged-in user
    initialCategory: String? = null          // Optional starting category for the home feed
) : ViewModel() {

    // Trigger used to manually refresh data from the repository
    private val _refreshTrigger = MutableStateFlow(0)
    
    /**
     * Internal state for user-driven filters and UI toggles.
     * Separated into a private flow to prevent exposing individual update methods.
     */
    private val _filterSettings = MutableStateFlow(
        FilterSettings(
            mainCategory = initialCategory ?: AppConstants.CAT_ALL,
            subCategory = AppConstants.getDefaultSubcategory(initialCategory ?: AppConstants.CAT_ALL)
        )
    )

    /**
     * The unified UI state for the Home Screen.
     * 
     * Uses 'combine' to merge 7 different data sources:
     * 1. Product catalog (Books, Gear, Courses) via flatMapLatest for refreshing.
     * 2. User's local wishlist IDs.
     * 3. User's local purchase IDs.
     * 4. User's search history.
     * 5. User's wallet transaction history.
     * 6. Active category and search filters.
     * 
     * The result is a high-performance, single-source-of-truth StateFlow.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        // Reactive data fetching: triggers repository reload whenever _refreshTrigger changes
        _refreshTrigger.flatMapLatest { repository.getAllCombinedData(userId) },
        // Conditional flows: only fetch user data if a user is actually logged in
        if (userId.isNotEmpty()) userDao.getWishlistIds(userId) else flowOf(emptyList<String>()),
        if (userId.isNotEmpty()) userDao.getPurchaseIds(userId) else flowOf(emptyList<String>()),
        if (userId.isNotEmpty()) userDao.getRecentSearches(userId) else flowOf(emptyList<String>()),
        if (userId.isNotEmpty()) userDao.getWalletHistory(userId) else flowOf(emptyList<WalletTransaction>()),
        _filterSettings
    ) { array ->
        // Extracting elements from the combined array with safe casting
        @Suppress("UNCHECKED_CAST")
        val books = array[0] as List<Book>?
        @Suppress("UNCHECKED_CAST")
        val wishlist = array[1] as List<String>
        @Suppress("UNCHECKED_CAST")
        val purchased = array[2] as List<String>
        @Suppress("UNCHECKED_CAST")
        val searches = array[3] as List<String>
        @Suppress("UNCHECKED_CAST")
        val wallet = array[4] as List<WalletTransaction>
        @Suppress("UNCHECKED_CAST")
        val filters = array[5] as FilterSettings

        val allBooks = books ?: emptyList()
        
        /**
         * REACTIVE FILTERING LOGIC:
         * Performs in-memory filtering of the master list based on current user selections.
         */
        val filtered = allBooks.filter { book ->
            // 1. Filter by Main Category (e.g. Books vs Gear vs Courses)
            val matchMain = when (filters.mainCategory) {
                AppConstants.CAT_ALL -> true
                AppConstants.CAT_FREE -> book.price == 0.0 // Special logic for "Free" category
                else -> book.mainCategory.equals(filters.mainCategory, ignoreCase = true)
            }
            
            // 2. Filter by Sub-Category (e.g. Technology inside Books)
            val matchSub = if (filters.subCategory.contains("All", ignoreCase = true) || filters.mainCategory == AppConstants.CAT_FREE) true 
                           else book.category.equals(filters.subCategory, ignoreCase = true)
            
            // 3. Filter by Search Query (Real-time title/author match)
            val matchQuery = if (filters.searchQuery.isEmpty()) true 
                             else book.title.contains(filters.searchQuery, ignoreCase = true) || 
                                  book.author.contains(filters.searchQuery, ignoreCase = true) ||
                                  book.category.contains(filters.searchQuery, ignoreCase = true)
            
            matchMain && matchSub && matchQuery
        }

        // Generate instant product suggestions based on search input (min 2 chars)
        val suggestions = if (filters.searchQuery.length < 2) emptyList()
        else allBooks.filter { 
            it.title.contains(filters.searchQuery, ignoreCase = true) || it.author.contains(filters.searchQuery, ignoreCase = true)
        }.take(5)

        // Mapping all combined data into the final immutable HomeUiState object
        HomeUiState(
            allBooks = allBooks,
            filteredBooks = filtered,
            wishlistIds = wishlist.toSet(),
            purchasedIds = purchased.toSet(),
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
            showWalletHistory = filters.showWalletHistory
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    /**
     * Updates the main category filter.
     * Automatically resets the sub-category to its default (e.g. "All Genres") 
     * and clears any active search query for a clean transition.
     */
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

    /**
     * Updates the specific sub-category filter within the current main category.
     */
    fun selectSubCategory(category: String) {
        _filterSettings.update { it.copy(subCategory = category, isSearchVisible = false) }
    }

    /**
     * Real-time search query updater.
     */
    fun updateSearchQuery(query: String) {
        _filterSettings.update { it.copy(searchQuery = query) }
    }

    /**
     * Persists a search query to the database so it appears in the 'Recent' list.
     */
    fun saveSearchQuery(query: String) {
        if (query.isBlank() || userId.isEmpty()) return
        viewModelScope.launch {
            userDao.addSearchQuery(SearchHistoryItem(userId = userId, query = query.trim()))
        }
    }

    /**
     * Deletes the user's search history from the local database.
     */
    fun clearRecentSearches() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            userDao.clearSearchHistory(userId)
        }
    }

    /**
     * Controls the visibility of the full-screen search overlay.
     */
    fun setSearchVisible(visible: Boolean) {
        _filterSettings.update { 
            it.copy(
                isSearchVisible = visible,
                searchQuery = if (!visible) "" else it.searchQuery
            )
        }
    }

    /**
     * Holds a reference to a book currently targeted for library removal.
     */
    fun setBookToRemove(book: Book?) {
        _filterSettings.update { it.copy(bookToRemove = book) }
    }

    /**
     * Toggles the visibility of the wallet history bottom sheet.
     */
    fun setWalletHistoryVisible(visible: Boolean) {
        _filterSettings.update { it.copy(showWalletHistory = visible) }
    }

    /**
     * Adds or removes a book from the user's favorites list.
     * Also updates the 'Recently Viewed' history when adding.
     */
    fun toggleWishlist(book: Book, isLiked: Boolean, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (isLiked) {
                userDao.removeFromWishlist(userId, book.id)
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                userDao.addToHistory(HistoryItem(userId, book.id))
                userDao.addToWishlist(WishlistItem(userId, book.id))
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    /**
     * Permanently removes a purchased digital item from the local library.
     */
    fun removePurchase(book: Book, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.deletePurchase(userId, book.id)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }

    /**
     * Triggers a manual refresh of all combined product data.
     */
    fun refresh() {
        _refreshTrigger.value += 1
    }
}

/**
 * Private helper class to encapsulate local UI settings.
 * Keeps the main uiState flow clean by managing input fields separately.
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
 * Factory class to inject dependencies (Repository, UserDao, UserId) into the HomeViewModel.
 */
class HomeViewModelFactory(
    private val repository: BookRepository,
    private val userDao: UserDao,
    private val userId: String,
    private val initialCategory: String? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, userDao, userId, initialCategory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
