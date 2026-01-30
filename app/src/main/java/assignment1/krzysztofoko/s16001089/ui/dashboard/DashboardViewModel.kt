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
 */
class DashboardViewModel(
    private val repository: BookRepository, 
    private val userDao: UserDao,           
    private val userId: String              
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

    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val walletHistory: StateFlow<List<WalletTransaction>> = if (userId.isNotEmpty()) {
        userDao.getWalletHistory(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBooks: StateFlow<List<Book>> = repository.getAllCombinedData(userId)
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<String>> = if (userId.isNotEmpty()) {
        userDao.getRecentSearches(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = if (userId.isNotEmpty()) {
        userDao.getNotificationsForUser(userId).map { list ->
            list.count { !it.isRead }
        }
    } else {
        flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val wishlistBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getWishlistIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastViewedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commentedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Purchased IDs Flow:
     * Exposes the set of product IDs the user has actually paid for or fully enrolled in.
     */
    val purchasedIds: StateFlow<Set<String>> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.toSet() }
    } else {
        flowOf(emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /**
     * Core Library Collection:
     * Combined list of owned items AND active applications.
     */
    val ownedBooks: StateFlow<List<Book>> = combine(
        allBooks, 
        userDao.getPurchaseIds(userId),
        userDao.getAllEnrollmentsFlow()
    ) { books, purchasedIds, enrollments ->
        val userEnrollments = enrollments.filter { it.userId == userId }
        
        books.filter { book ->
            purchasedIds.contains(book.id) || 
            userEnrollments.any { it.courseId == book.id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
     * User Applications Map:
     * Provides a quick lookup for application status by courseId.
     */
    val applicationsMap: StateFlow<Map<String, String>> = if (userId.isNotEmpty()) {
        userDao.getAllEnrollmentsFlow().map { list ->
            list.filter { it.userId == userId }.associate { it.courseId to it.status }
        }
    } else {
        flowOf(emptyMap())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Application Count Flow:
     * Tracks if the user has any applications that are NOT YET PAID/ENROLLED.
     * Once a course is purchased, it is no longer considered an active "Application" in this context.
     */
    val applicationCount: StateFlow<Int> = combine(
        userDao.getAllEnrollmentsFlow(),
        purchasedIds
    ) { enrollments, purchased ->
        enrollments.count { it.userId == userId && !purchased.contains(it.courseId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    fun topUp(amount: Double, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            localUser.value?.let { user ->
                val formattedAmount = String.format(Locale.US, "%.2f", amount)
                val currentMethod = user.selectedPaymentMethod ?: "External Method"
                val orderRef = OrderUtils.generateOrderReference()
                val invoiceNum = OrderUtils.generateInvoiceNumber()
                val timestamp = System.currentTimeMillis()
                
                userDao.upsertUser(user.copy(balance = user.balance + amount))
                
                userDao.addWalletTransaction(WalletTransaction(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "TOP_UP",
                    amount = amount,
                    timestamp = timestamp,
                    paymentMethod = currentMethod,
                    description = "Wallet Top Up",
                    orderReference = orderRef,
                    productId = AppConstants.ID_TOPUP
                ))

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
                    purchasedAt = timestamp,
                    paymentMethod = currentMethod,
                    orderReference = orderRef,
                    billingName = user.name,
                    billingEmail = user.email,
                    billingAddress = user.address
                ))

                userDao.addNotification(NotificationLocal(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    productId = AppConstants.ID_TOPUP,
                    title = "Wallet Top-Up Successful",
                    message = "Successfully added £$formattedAmount to your university wallet. Reference: $orderRef",
                    timestamp = timestamp,
                    type = "FINANCE"
                ))
                
                onComplete("£$formattedAmount ${AppConstants.MSG_WALLET_TOPUP_SUCCESS}")
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
