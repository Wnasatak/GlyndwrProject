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
 * DashboardViewModel.kt
 *
 * This ViewModel acts as the data orchestrator for the User Dashboard. It handles the 
 * complex logic of managing the user's profile, university wallet, course enrolments, 
 * and personal item collection. 
 * 
 * Key responsibilities:
 * - Providing reactive state streams for all dashboard components.
 * - Managing search and filtering within the user's personal collection.
 * - Coordinating financial transactions like wallet top-ups.
 * - Handling library management actions (e.g., removing items).
 * - Maintaining a comprehensive system audit log for user actions.
 */
class DashboardViewModel(
    private val repository: BookRepository, 
    private val userDao: UserDao,
    private val classroomDao: ClassroomDao,
    private val auditDao: AuditDao,
    private val userId: String              
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance() // Firebase handle for logging user details.

    // --- NAVIGATION & UI STATE --- //
    
    // Holds the raw search query for filtering the collection.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Controls the visibility of the search overlay.
    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    // Toggles the wallet top-up workflow popup.
    private val _showPaymentPopup = MutableStateFlow(false)
    val showPaymentPopup: StateFlow<Boolean> = _showPaymentPopup.asStateFlow()

    // Tracks the specific item the user is currently attempting to remove.
    private val _bookToRemove = MutableStateFlow<Book?>(null)
    val bookToRemove: StateFlow<Book?> = _bookToRemove.asStateFlow()

    // The active filter for the user's personal library (e.g., "All", "Books", "Courses").
    private val _selectedCollectionFilter = MutableStateFlow("All")
    val selectedCollectionFilter: StateFlow<String> = _selectedCollectionFilter.asStateFlow()

    // --- USER PROFILE & FINANCE --- //
    
    // Streams the current user's details, using 'Lazily' to save resources until needed.
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Streams the user's complete financial history.
    val walletHistory: StateFlow<List<WalletTransaction>> = if (userId.isNotEmpty()) {
        userDao.getWalletHistory(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Streams all official digital invoices issued to the user.
    val invoices: StateFlow<List<Invoice>> = if (userId.isNotEmpty()) {
        userDao.getInvoicesForUser(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- CATALOGUE & LIBRARY --- //
    
    // Holds the master list of all available university resources.
    val allBooks: StateFlow<List<Book>> = repository.getAllCombinedData(userId)
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // A set of IDs representing everything the user has acquired.
    val purchasedIds: StateFlow<Set<String>> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.toSet() }
    } else {
        flowOf(emptySet())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    /**
     * Primary Collection Logic:
     * Combines the master catalogue, ownership data, and user filters to 
     * produce the final list of items displayed in the dashboard grid.
     */
    val filteredOwnedBooks: StateFlow<List<Book>> = combine(
        allBooks, 
        userDao.getPurchaseIds(userId),
        userDao.getAllEnrollmentsFlow(),
        _selectedCollectionFilter
    ) { books, purchasedIdsList, enrollments, filter ->
        // 1. Identification: Filter only for this specific student's applications.
        val userEnrollments = enrollments.filter { it.userId == userId }
        val pIds = purchasedIdsList.toSet()
        
        // 2. Ownership Filter: Only include items either purchased or actively applied for.
        val owned = books.filter { book ->
            pIds.contains(book.id) || userEnrollments.any { it.courseId == book.id }
        }
        
        // 3. Category Filter: Narrow down the results based on the UI selection.
        when (filter) {
            "Books" -> owned.filter { it.mainCategory == AppConstants.CAT_BOOKS && !it.isAudioBook }
            "Audiobooks" -> owned.filter { it.isAudioBook }
            "Gear" -> owned.filter { it.mainCategory == AppConstants.CAT_GEAR }
            "Courses" -> owned.filter { it.mainCategory == AppConstants.CAT_COURSES }
            else -> owned // "All" selected.
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- LIVE SESSIONS --- //
    
    // Tracks active classroom sessions for immediate student participation.
    val activeLiveSessions: StateFlow<List<LiveSession>> = classroomDao.getAllActiveSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- ACTIVITY & ENGAGEMENT --- //
    
    // List of items currently in the user's favourites.
    val wishlistBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getWishlistIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Most recent items viewed by the user.
    val lastViewedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Items the user has interacted with socially (posted reviews).
    val commentedBooks: StateFlow<List<Book>> = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Streams all reviews authored by this user.
    val userReviews: StateFlow<List<ReviewLocal>> = if (userId.isNotEmpty()) {
        userDao.getReviewsForUser(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Historical search terms for quick recall in the search UI.
    val searchHistory: StateFlow<List<String>> = if (userId.isNotEmpty()) {
        userDao.getRecentSearches(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- ACADEMIC STATUS --- //
    
    // Tracks payment instalments for multi-part course enrolments.
    val courseInstallments: StateFlow<List<CourseInstallment>> = if (userId.isNotEmpty()) {
        userDao.getCourseInstallmentsForUser(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Historical grades for the student's completed or active modules.
    val userGrades: StateFlow<List<Grade>> = if (userId.isNotEmpty()) {
        classroomDao.getAllGradesForUser(userId)
    } else {
        flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Live unread badge count for system notifications.
    val unreadNotificationsCount: StateFlow<Int> = if (userId.isNotEmpty()) {
        userDao.getNotificationsForUser(userId).map { list ->
            list.count { !it.isRead }
        }
    } else {
        flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Check if the user has any active message threads.
    val hasMessages: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        classroomDao.getAllMessagesForUser(userId).map { it.isNotEmpty() }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Live unread badge count for direct messages.
    val unreadMessagesCount: StateFlow<Int> = if (userId.isNotEmpty()) {
        classroomDao.getAllMessagesForUser(userId).map { list ->
            list.count { it.receiverId == userId && !it.isRead }
        }
    } else {
        flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Maps course IDs to status labels for the dashboard grid badges.
    val applicationsMap: StateFlow<Map<String, String>> = if (userId.isNotEmpty()) {
        userDao.getAllEnrollmentsFlow().map { list ->
            list.filter { it.userId == userId }.associate { it.courseId to it.status }
        }
    } else {
        flowOf(emptyMap())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Total count of pending academic applications.
    val applicationCount: StateFlow<Int> = combine(
        userDao.getAllEnrollmentsFlow(),
        purchasedIds
    ) { enrollments, purchased ->
        enrollments.count { it.userId == userId && !purchased.contains(it.courseId) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Dynamic search results for the dashboard search overlay.
    val suggestions: StateFlow<List<Book>> = combine(allBooks, _searchQuery) { books, query ->
        if (query.length < 2) emptyList() // Only suggest if at least 2 characters entered.
        else books.filter { 
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }.take(5) // Limit to top 5 matches.
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- LOGGING HELPER --- //
    
    /**
     * Internal utility to persist user interactions into the central audit trail.
     */
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

    // --- STATE HANDLERS --- //

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    /**
     * Records a successful search query in the user's history and audit log.
     */
    fun saveSearchQuery(query: String) {
        if (query.isBlank() || userId.isEmpty()) return
        viewModelScope.launch {
            userDao.addSearchQuery(SearchHistoryItem(userId = userId, query = query.trim()))
            addLog("SEARCH", "none", "Searched for: $query")
        }
    }

    /**
     * Wipes the user's local search history records.
     */
    fun clearRecentSearches() {
        if (userId.isEmpty()) return
        viewModelScope.launch { 
            userDao.clearSearchHistory(userId) // Perform DB deletion.
            addLog("CLEAR_HISTORY", "none", "Cleared search history")
        }
    }

    fun setSearchVisible(visible: Boolean) {
        _isSearchVisible.value = visible
        if (!visible) _searchQuery.value = "" // Reset text when overlay closes.
    }

    fun setShowPaymentPopup(show: Boolean) { _showPaymentPopup.value = show }
    fun setBookToRemove(book: Book?) { _bookToRemove.value = book }
    fun setCollectionFilter(filter: String) { _selectedCollectionFilter.value = filter }

    /**
     * Coordinates the complex process of topping up the university wallet.
     * This involves balance updates, transaction records, invoices, and notifications.
     */
    fun topUp(amount: Double, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            localUser.value?.let { user ->
                val formattedAmount = String.format(Locale.US, "%.2f", amount)
                val currentMethod = user.selectedPaymentMethod ?: "External Method"
                val orderRef = OrderUtils.generateOrderReference() // Create unique ref.
                val invoiceNum = OrderUtils.generateInvoiceNumber() // Create invoice ID.
                val timestamp = System.currentTimeMillis()
                
                // 1. Update primary account balance.
                userDao.upsertUser(user.copy(balance = user.balance + amount))
                
                // 2. Log the movements in the financial ledger.
                userDao.addWalletTransaction(WalletTransaction(
                    id = UUID.randomUUID().toString(), userId = userId, type = "TOP_UP",
                    amount = amount, timestamp = timestamp, paymentMethod = currentMethod,
                    description = "Wallet Top Up", orderReference = orderRef, productId = AppConstants.ID_TOPUP
                ))

                // 3. Generate the official digital receipt.
                userDao.addInvoice(Invoice(
                    invoiceNumber = invoiceNum, userId = userId, productId = AppConstants.ID_TOPUP,
                    itemTitle = "Wallet Credit Top-Up", itemCategory = AppConstants.CAT_FINANCE,
                    itemVariant = "Balance Increase", pricePaid = amount, discountApplied = 0.0,
                    quantity = 1, purchasedAt = timestamp, paymentMethod = currentMethod,
                    orderReference = orderRef, billingName = user.name, billingEmail = user.email, billingAddress = user.address
                ))

                // 4. Send a confirmation alert to the user.
                userDao.addNotification(NotificationLocal(
                    id = UUID.randomUUID().toString(), userId = userId, productId = AppConstants.ID_TOPUP,
                    title = "Wallet Top-Up Successful",
                    message = "Successfully added £$formattedAmount to your university wallet. Reference: $orderRef",
                    timestamp = timestamp, type = "FINANCE"
                ))
                
                // 5. Final audit log and UI notification.
                addLog("WALLET_TOPUP", AppConstants.ID_TOPUP, "Topped up wallet by £$formattedAmount")
                onComplete("£$formattedAmount ${AppConstants.MSG_WALLET_TOPUP_SUCCESS}")
            }
        }
    }

    /**
     * Removes an item from the user's library and logs the action.
     */
    fun removePurchase(book: Book, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.deletePurchase(userId, book.id) // Perform DB deletion.
            addLog("REMOVE_FROM_LIBRARY", book.id, "Removed ${book.title} from library")
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }

    /**
     * Deletes a social review authored by the current user.
     */
    fun deleteReview(reviewId: Int) {
        viewModelScope.launch {
            userDao.deleteReview(reviewId) // Perform DB deletion.
            addLog("DELETE_REVIEW", reviewId.toString(), "User deleted their own review")
        }
    }
}
