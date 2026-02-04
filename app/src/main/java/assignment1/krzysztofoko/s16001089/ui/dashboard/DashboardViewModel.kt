package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Enhanced ViewModel for the User Dashboard.
 * Manages comprehensive student activity including library, history, invoices, and academic progress.
 */
class DashboardViewModel(
    private val repository: BookRepository, 
    private val userDao: UserDao,
    private val classroomDao: ClassroomDao,
    private val auditDao: AuditDao,
    private val userId: String              
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- Navigation & UI State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _showPaymentPopup = MutableStateFlow(false)
    val showPaymentPopup: StateFlow<Boolean> = _showPaymentPopup.asStateFlow()

    private val _bookToRemove = MutableStateFlow<Book?>(null)
    val bookToRemove: StateFlow<Book?> = _bookToRemove.asStateFlow()

    private val _selectedCollectionFilter = MutableStateFlow("All")
    val selectedCollectionFilter: StateFlow<String> = _selectedCollectionFilter.asStateFlow()

    // --- User Profile & Finance ---
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

    val invoices: StateFlow<List<Invoice>> = if (userId.isNotEmpty()) {
        userDao.getInvoicesForUser(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Catalog & Library ---
    val allBooks: StateFlow<List<Book>> = repository.getAllCombinedData(userId)
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedIds: StateFlow<Set<String>> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.toSet() }
    } else {
        flowOf(emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val filteredOwnedBooks: StateFlow<List<Book>> = combine(
        allBooks, 
        userDao.getPurchaseIds(userId),
        userDao.getAllEnrollmentsFlow(),
        _selectedCollectionFilter
    ) { books, purchasedIds, enrollments, filter ->
        val userEnrollments = enrollments.filter { it.userId == userId }
        val owned = books.filter { book ->
            purchasedIds.contains(book.id) || userEnrollments.any { it.courseId == book.id }
        }
        
        when (filter) {
            "Books" -> owned.filter { it.mainCategory == AppConstants.CAT_BOOKS && !it.isAudioBook }
            "Audiobooks" -> owned.filter { it.isAudioBook }
            "Gear" -> owned.filter { it.mainCategory == AppConstants.CAT_GEAR }
            "Courses" -> owned.filter { it.mainCategory == AppConstants.CAT_COURSES }
            else -> owned
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Live Sessions ---
    val activeLiveSessions: StateFlow<List<LiveSession>> = classroomDao.getAllActiveSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Activity & Engagement ---
    val wishlistBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getWishlistIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastViewedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commentedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userReviews: StateFlow<List<ReviewLocal>> = if (userId.isNotEmpty()) {
        userDao.getReviewsForUser(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory: StateFlow<List<String>> = if (userId.isNotEmpty()) {
        userDao.getRecentSearches(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Academic Status ---
    val courseInstallments: StateFlow<List<CourseInstallment>> = if (userId.isNotEmpty()) {
        userDao.getCourseInstallmentsForUser(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userGrades: StateFlow<List<Grade>> = if (userId.isNotEmpty()) {
        classroomDao.getAllGradesForUser(userId)
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

    val hasMessages: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        classroomDao.getAllMessagesForUser(userId).map { it.isNotEmpty() }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val unreadMessagesCount: StateFlow<Int> = if (userId.isNotEmpty()) {
        classroomDao.getAllMessagesForUser(userId).map { list ->
            list.count { it.receiverId == userId && !it.isRead }
        }
    } else {
        flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val applicationsMap: StateFlow<Map<String, String>> = if (userId.isNotEmpty()) {
        userDao.getAllEnrollmentsFlow().map { list ->
            list.filter { it.userId == userId }.associate { it.courseId to it.status }
        }
    } else {
        flowOf(emptyMap())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    // --- Logging Helper ---
    private fun addLog(action: String, targetId: String, details: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            auditDao.insertLog(SystemLog(
                userId = userId,
                userName = user?.displayName ?: "Student",
                action = action,
                targetId = targetId,
                details = details,
                logType = "USER"
            ))
        }
    }

    // --- State Handlers ---

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun saveSearchQuery(query: String) {
        if (query.isBlank() || userId.isEmpty()) return
        viewModelScope.launch {
            userDao.addSearchQuery(SearchHistoryItem(userId = userId, query = query.trim()))
            addLog("SEARCH", "none", "Searched for: $query")
        }
    }

    fun clearRecentSearches() {
        if (userId.isEmpty()) return
        viewModelScope.launch { 
            userDao.clearSearchHistory(userId)
            addLog("CLEAR_HISTORY", "none", "Cleared search history")
        }
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
                
                addLog("WALLET_TOPUP", AppConstants.ID_TOPUP, "Topped up wallet by £$formattedAmount")
                onComplete("£$formattedAmount ${AppConstants.MSG_WALLET_TOPUP_SUCCESS}")
            }
        }
    }

    fun removePurchase(book: Book, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.deletePurchase(userId, book.id)
            addLog("REMOVE_FROM_LIBRARY", book.id, "Removed ${book.title} from library")
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }

    fun deleteReview(reviewId: Int) {
        viewModelScope.launch {
            userDao.deleteReview(reviewId)
            addLog("DELETE_REVIEW", reviewId.toString(), "User deleted their own review")
        }
    }
}
