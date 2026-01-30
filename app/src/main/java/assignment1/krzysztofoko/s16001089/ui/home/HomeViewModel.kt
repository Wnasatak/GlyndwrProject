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
 */
class HomeViewModel(
    private val repository: BookRepository, 
    private val userDao: UserDao,           
    private val userId: String,              
    initialCategory: String? = null          
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    
    private val _filterSettings = MutableStateFlow(
        FilterSettings(
            mainCategory = initialCategory ?: AppConstants.CAT_ALL,
            subCategory = AppConstants.getDefaultSubcategory(initialCategory ?: AppConstants.CAT_ALL)
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        _refreshTrigger.flatMapLatest { repository.getAllCombinedData(userId) },
        if (userId.isNotEmpty()) userDao.getWishlistIds(userId) else flowOf(emptyList<String>()),
        if (userId.isNotEmpty()) userDao.getPurchaseIds(userId) else flowOf(emptyList<String>()),
        if (userId.isNotEmpty()) userDao.getRecentSearches(userId) else flowOf(emptyList<String>()),
        if (userId.isNotEmpty()) userDao.getWalletHistory(userId) else flowOf(emptyList<WalletTransaction>()),
        _filterSettings,
        if (userId.isNotEmpty()) userDao.getUserFlow(userId) else flowOf(null) // Added user details
    ) { array ->
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
        @Suppress("UNCHECKED_CAST")
        val localUser = array[6] as UserLocal?

        val allBooks = books ?: emptyList()
        
        val filtered = allBooks.filter { book ->
            val matchMain = when (filters.mainCategory) {
                AppConstants.CAT_ALL -> true
                AppConstants.CAT_FREE -> book.price == 0.0 
                else -> book.mainCategory.equals(filters.mainCategory, ignoreCase = true)
            }
            val matchSub = if (filters.subCategory.contains("All", ignoreCase = true) || filters.mainCategory == AppConstants.CAT_FREE) true 
                           else book.category.equals(filters.subCategory, ignoreCase = true)
            val matchQuery = if (filters.searchQuery.isEmpty()) true 
                             else book.title.contains(filters.searchQuery, ignoreCase = true) || 
                                  book.author.contains(filters.searchQuery, ignoreCase = true) ||
                                  book.category.contains(filters.searchQuery, ignoreCase = true)
            matchMain && matchSub && matchQuery
        }

        val suggestions = if (filters.searchQuery.length < 2) emptyList()
        else allBooks.filter { 
            it.title.contains(filters.searchQuery, ignoreCase = true) || it.author.contains(filters.searchQuery, ignoreCase = true)
        }.take(5)

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
            showWalletHistory = filters.showWalletHistory,
            localUser = localUser // Pass local user to state
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

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

    fun saveSearchQuery(query: String) {
        if (query.isBlank() || userId.isEmpty()) return
        viewModelScope.launch {
            userDao.addSearchQuery(SearchHistoryItem(userId = userId, query = query.trim()))
        }
    }

    fun clearRecentSearches() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            userDao.clearSearchHistory(userId)
        }
    }

    fun setSearchVisible(visible: Boolean) {
        _filterSettings.update { 
            it.copy(
                isSearchVisible = visible,
                searchQuery = if (!visible) "" else it.searchQuery
            )
        }
    }

    fun setBookToRemove(book: Book?) {
        _filterSettings.update { it.copy(bookToRemove = book) }
    }

    fun setWalletHistoryVisible(visible: Boolean) {
        _filterSettings.update { it.copy(showWalletHistory = visible) }
    }

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

    fun removePurchase(book: Book, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.deletePurchase(userId, book.id)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }

    fun refresh() {
        _refreshTrigger.value += 1
    }
}

private data class FilterSettings(
    val mainCategory: String = AppConstants.CAT_ALL,
    val subCategory: String = "All Genres",
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val bookToRemove: Book? = null,
    val showWalletHistory: Boolean = false
)

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
