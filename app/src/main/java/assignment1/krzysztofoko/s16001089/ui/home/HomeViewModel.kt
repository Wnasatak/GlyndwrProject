package assignment1.krzysztofoko.s16001089.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: BookRepository,
    private val userDao: UserDao,
    private val userId: String
) : ViewModel() {

    private val _selectedMainCategory = MutableStateFlow(AppConstants.CAT_ALL)
    val selectedMainCategory: StateFlow<String> = _selectedMainCategory.asStateFlow()

    private val _selectedSubCategory = MutableStateFlow("All Genres")
    val selectedSubCategory: StateFlow<String> = _selectedSubCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _bookToRemove = MutableStateFlow<Book?>(null)
    val bookToRemove: StateFlow<Book?> = _bookToRemove.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allBooks: StateFlow<List<Book>> = _refreshTrigger
        .flatMapLatest { repository.getAllCombinedData(userId) }
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistIds: StateFlow<List<String>> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedIds: StateFlow<List<String>> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent Searches flow
    val recentSearches: StateFlow<List<String>> = if (userId.isNotEmpty()) {
        userDao.getRecentSearches(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredBooks: StateFlow<List<Book>> = combine(
        allBooks, _selectedMainCategory, _selectedSubCategory, _searchQuery
    ) { books, mainCat, subCat, query ->
        books.filter { book ->
            val matchMain = when (mainCat) {
                AppConstants.CAT_ALL -> true
                AppConstants.CAT_FREE -> book.price == 0.0
                else -> book.mainCategory.equals(mainCat, ignoreCase = true)
            }
            val matchSub = if (subCat.contains("All", ignoreCase = true) || mainCat == AppConstants.CAT_FREE) true 
                           else book.category.equals(subCat, ignoreCase = true)
            val matchQuery = if (query.isEmpty()) true 
                             else book.title.contains(query, ignoreCase = true) || 
                                  book.author.contains(query, ignoreCase = true) ||
                                  book.category.contains(query, ignoreCase = true)
            matchMain && matchSub && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<Book>> = combine(allBooks, _searchQuery) { books, query ->
        if (query.length < 2) emptyList()
        else books.filter { 
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMainCategory(category: String) {
        _selectedMainCategory.value = category
        _selectedSubCategory.value = AppConstants.getDefaultSubcategory(category)
        _isSearchVisible.value = false
    }

    fun selectSubCategory(category: String) {
        _selectedSubCategory.value = category
        _isSearchVisible.value = false
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
        _isSearchVisible.value = visible
        if (!visible) _searchQuery.value = ""
    }

    fun setBookToRemove(book: Book?) {
        _bookToRemove.value = book
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
}
