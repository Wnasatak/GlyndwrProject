package assignment1.krzysztofoko.s16001089.ui.details.book

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * BookDetailViewModel.kt
 *
 * This ViewModel acts as the data architect for the Book details screen.
 * It manages the lifecycle of book data, tracks user-specific states like
 * ownership and wishlist status, and coordinates the logic for both free and 
 * paid purchase workflows, while also maintaining a comprehensive audit log.
 */
class BookDetailViewModel(
    private val bookDao: BookDao,        
    private val userDao: UserDao,        
    private val auditDao: AuditDao,      
    private val bookId: String,          
    private val userId: String           
) : ViewModel() {

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    // --- UI STATE FLOWS --- //

    // Holds the core book metadata once loaded from the database.
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    // Indicates if an asynchronous data fetch is currently in progress.
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Streams the current user's local data (balance, role, etc.).
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Collects available role-based discounts for dynamic price calculation.
    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks if the user already owns this specific item.
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Tracks if this book is currently in the user's wishlist.
    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Streams all user reviews for this specific book.
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(bookId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBook() // Initialise data loading on creation.
    }

    /**
     * Internal helper to record user actions in the system audit log.
     */
    private fun addLog(action: String, details: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            auditDao.insertLog(SystemLog(
                userId = userId,
                userName = user?.displayName ?: "Student",
                action = action,
                targetId = bookId,
                details = details,
                logType = "USER"
            ))
        }
    }

    /**
     * Fetches the book data and records the visit in the user's history.
     */
    private fun loadBook() {
        viewModelScope.launch {
            _loading.value = true // Start loading indicator.
            val fetchedBook = bookDao.getBookById(bookId) // Fetch from Room.
            _book.value = fetchedBook // Update state flow.
            
            // If authenticated, record the view event.
            if (userId.isNotEmpty() && fetchedBook != null) {
                userDao.addToHistory(HistoryItem(userId, bookId))
                addLog("VIEW_ITEM", "User viewed: ${fetchedBook.title}")
            }
            _loading.value = false // Stop loading indicator.
        }
    }

    /**
     * Toggles the presence of this book in the user's personal favorites.
     */
    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch // Guard for guests.
            if (inWishlist.value) {
                userDao.removeFromWishlist(userId, bookId) // Remove from DB.
                addLog("WISHLIST_REMOVE", "Removed item from wishlist")
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                userDao.addToWishlist(WishlistItem(userId, bookId)) // Add to DB.
                addLog("WISHLIST_ADD", "Added item to wishlist")
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    /**
     * Handles the logic for acquiring a zero-cost item.
     * Records the purchase, notifies the user, and logs the event.
     */
    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentBook = _book.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference() // Generate unique reference.
            
            // 1. Persist the purchase record.
            userDao.addPurchase(PurchaseItem(
                purchaseId = UUID.randomUUID().toString(),
                userId = userId, 
                productId = bookId, 
                mainCategory = currentBook.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = AppConstants.METHOD_FREE_LIBRARY,
                totalPricePaid = 0.0,
                orderConfirmation = orderConf
            ))

            // 2. Trigger an in-app notification.
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = AppConstants.NOTIF_TITLE_BOOK_PICKED_UP,
                productId = bookId,
                message = "'${currentBook.title}' has been added to your library.",
                timestamp = System.currentTimeMillis(),
                type = AppConstants.NOTIF_TYPE_PICKUP
            ))

            // 3. Audit log the free claim.
            addLog("PURCHASE_FREE", "Free pickup: ${currentBook.title}")
            onComplete(AppConstants.MSG_ADDED_TO_LIBRARY)
        }
    }

    /**
     * Finalises a paid transaction. The payment processing is handled in the UI, 
     * this method records the successful result and logs it.
     */
    fun handlePurchaseComplete(context: Context?, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val currentBook = _book.value ?: return@launch
            // Audit log the financial transaction.
            addLog("PURCHASE_PAID", "Paid £${String.format(Locale.US, "%.2f", finalPrice)} for: ${currentBook.title}")
            onComplete(AppConstants.MSG_PURCHASE_SUCCESS)
        }
    }

    /**
     * Removes a previously acquired item from the user's library.
     */
    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, bookId) // Wipe the record.
            addLog("REMOVE_FROM_LIBRARY", "User removed item from library")
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
