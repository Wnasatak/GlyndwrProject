package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for the User Dashboard.
 * 
 * Acts as the central data orchestrator for the member-only area. It manages:
 * 1. Reactive Collection: Combines global catalog data with user purchase IDs to show owned items.
 * 2. Activity Tracking: Resolves full product metadata for 'Recently Viewed' and 'Commented' items.
 * 3. Wallet Management: Handles balance top-ups, transaction logging, and top-up invoice generation.
 * 4. Real-time Search: Provides integrated search within the dashboard context.
 */
class DashboardViewModel(
    private val repository: BookRepository, // Repository for resolving product details
    private val userDao: UserDao,           // DAO for user-specific data (History, Purchases, Wallet)
    private val userId: String              // ID of the currently authenticated student
) : ViewModel() {

    // --- Observable UI State Flows ---
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _showPaymentPopup = MutableStateFlow(false)
    val showPaymentPopup: StateFlow<Boolean> = _showPaymentPopup.asStateFlow()

    // Holds a reference to a book targeted for library removal
    private val _bookToRemove = MutableStateFlow<Book?>(null)
    val bookToRemove: StateFlow<Book?> = _bookToRemove.asStateFlow()

    // Current category filter for the 'Your Collection' section
    private val _selectedCollectionFilter = MutableStateFlow("All")
    val selectedCollectionFilter: StateFlow<String> = _selectedCollectionFilter.asStateFlow()

    /**
     * User Profile Flow:
     * Reactively updates when the student's name, role, or balance changes.
     */
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Wallet History Flow:
     * Provides a chronological list of all financial movements (Top-ups and Purchases).
     */
    val walletHistory: StateFlow<List<WalletTransaction>> = if (userId.isNotEmpty()) {
        userDao.getWalletHistory(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Master Product Stream:
     * Collects all available items from the unified repository.
     */
    val allBooks: StateFlow<List<Book>> = repository.getAllCombinedData(userId)
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stream of the user's past search queries
    val recentSearches: StateFlow<List<String>> = if (userId.isNotEmpty()) {
        userDao.getRecentSearches(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Notification Badge Flow:
     * Monitors the unread status of all notifications to update the UI badge count.
     */
    val unreadNotificationsCount: StateFlow<Int> = if (userId.isNotEmpty()) {
        userDao.getNotificationsForUser(userId).map { list ->
            list.count { !it.isRead }
        }
    } else {
        flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Wishlist Resolution:
     * Cross-references local favorite IDs with the master catalog to return full Book objects.
     */
    val wishlistBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getWishlistIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * View History Resolution:
     * Resolves metadata for the most recently viewed items.
     */
    val lastViewedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Social Activity Resolution:
     * Finds full product info for any items the user has commented on.
     */
    val commentedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Core Library Collection:
     * List of all items currently owned by the user.
     */
    val ownedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getPurchaseIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Filtered Library View:
     * Dynamically applies the dashboard category filter (Books, AudioBooks, Gear, Courses) 
     * to the user's owned collection.
     */
    val filteredOwnedBooks: StateFlow<List<Book>> = combine(ownedBooks, _selectedCollectionFilter) { books, filter ->
        when (filter) {
            "Books" -> books.filter { it.mainCategory == AppConstants.CAT_BOOKS && !it.isAudioBook }
            "Audiobooks" -> books.filter { it.isAudioBook }
            "Gear" -> books.filter { it.mainCategory == AppConstants.CAT_GEAR }
            "Courses" -> books.filter { it.mainCategory == AppConstants.CAT_COURSES }
            else -> books
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Instant Search Suggestions:
     * Generates a preview list of matching items as the user types in the search bar.
     */
    val suggestions: StateFlow<List<Book>> = combine(allBooks, _searchQuery) { books, query ->
        if (query.length < 2) emptyList()
        else books.filter { 
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State Handlers ---

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun saveSearchQuery(query: String) {
        if (query.isBlank() || userId.isEmpty()) return
        viewModelScope.launch {
            userDao.addSearchQuery(SearchHistoryItem(userId = userId, query = query.trim()))
        }
    }

    fun clearRecentSearches() {
        if (userId.isEmpty()) return
        viewModelScope.launch { userDao.clearSearchHistory(userId) }
    }

    fun setSearchVisible(visible: Boolean) {
        _isSearchVisible.value = visible
        if (!visible) _searchQuery.value = ""
    }

    fun setShowPaymentPopup(show: Boolean) { _showPaymentPopup.value = show }
    fun setBookToRemove(book: Book?) { _bookToRemove.value = book }
    fun setCollectionFilter(filter: String) { _selectedCollectionFilter.value = filter }

    /**
     * Wallet Top-Up Logic:
     * 1. Updates the student's balance in the 'users_local' table.
     * 2. Logs a new 'TOP_UP' transaction in 'wallet_history'.
     * 3. Generates a formal invoice record specifically for the financial top-up.
     */
    fun topUp(amount: Double, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            localUser.value?.let { user ->
                val formattedAmount = String.format(Locale.US, "%.2f", amount)
                val currentMethod = user.selectedPaymentMethod ?: "External Method"
                val orderRef = OrderUtils.generateOrderReference()
                val invoiceNum = OrderUtils.generateInvoiceNumber()
                
                // 1. Update persistent balance
                userDao.upsertUser(user.copy(balance = user.balance + amount))
                
                // 2. Record historical transaction data
                userDao.addWalletTransaction(WalletTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "TOP_UP",
                    amount = amount,
                    paymentMethod = currentMethod,
                    description = "Wallet Top Up",
                    orderReference = orderRef,
                    productId = AppConstants.ID_TOPUP
                ))

                // 3. Create searchable Invoice record for the top-up
                userDao.addInvoice(Invoice(
                    invoiceNumber = invoiceNum,
                    userId = userId,
                    productId = AppConstants.ID_TOPUP,
                    itemTitle = "Wallet Credit Top-Up",
                    itemCategory = AppConstants.CAT_FINANCE,
                    itemVariant = "Balance Increase",
                    pricePaid = amount,
                    discountApplied = 0.0,
                    quantity = 1,
                    purchasedAt = System.currentTimeMillis(),
                    paymentMethod = currentMethod,
                    orderReference = orderRef,
                    billingName = user.name,
                    billingEmail = user.email,
                    billingAddress = user.address
                ))
                
                onComplete("£$formattedAmount ${AppConstants.MSG_WALLET_TOPUP_SUCCESS}")
            }
        }
    }

    /**
     * Permanently removes a purchased digital item from the student's library.
     */
    fun removePurchase(book: Book, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.deletePurchase(userId, book.id)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
