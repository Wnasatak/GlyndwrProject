package assignment1.krzysztofoko.s16001089.ui.details.book

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for the Book Details screen.
 * 
 * This class orchestrates the data flow for a specific book, including its metadata,
 * ownership status (purchased vs not), wishlist status, and collaborative reviews.
 * It handles high-level business logic such as processing free/paid purchases,
 * sending email confirmations, and managing the user's viewing history.
 */
class BookDetailViewModel(
    private val bookDao: BookDao,        // DAO for core book product data
    private val userDao: UserDao,        // DAO for user-specific data (Wishlist, History, Purchases)
    private val bookId: String,          // Unique ID of the book being viewed
    private val userId: String           // ID of the current authenticated student
) : ViewModel() {

    // --- Core Reactive Data Streams ---

    // State holding the current book metadata (resolved from Room)
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    // Flag to track initial data resolution for the UI loading spinner
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /**
     * Local User Flow:
     * Provides real-time updates of the user's profile (balance, name, etc.).
     * Subscribed only while the UI is active (5s timeout).
     */
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Ownership Status Flow:
     * Checks if the current bookId exists in the user's purchase list.
     */
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Wishlist Status Flow:
     * Checks if the current bookId is in the user's local favorites.
     */
    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Reviews Flow:
     * Fetches all community reviews for this specific product ID.
     */
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(bookId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically fetch data and record viewing history on startup
        loadBook()
    }

    /**
     * Internal function to fetch product metadata and update the user's 'Recently Viewed' history.
     */
    private fun loadBook() {
        viewModelScope.launch {
            _loading.value = true
            val fetchedBook = bookDao.getBookById(bookId)
            _book.value = fetchedBook
            
            // Record this view in the student's local history for personalized suggestions
            if (userId.isNotEmpty() && fetchedBook != null) {
                userDao.addToHistory(HistoryItem(userId, bookId))
            }
            _loading.value = false
        }
    }

    /**
     * Logic to add or remove the book from the student's local favorites list.
     */
    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            if (inWishlist.value) {
                // Remove from favorites
                userDao.removeFromWishlist(userId, bookId)
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                // Add to favorites
                userDao.addToHistory(HistoryItem(userId, bookId))
                userDao.addToWishlist(WishlistItem(userId, bookId))
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    /**
     * Workflow for collecting a free digital book.
     * 
     * Process:
     * 1. Generates a local order reference and unique purchase ID.
     * 2. Inserts a new purchase record into the local database.
     * 3. Triggers a system notification for the user.
     * 4. Dispatches an HTML email confirmation with item metadata.
     */
    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentBook = _book.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference()
            val purchaseId = UUID.randomUUID().toString()
            
            val user = userDao.getUserById(userId)

            // Persist the free 'transaction' in the local database
            userDao.addPurchase(PurchaseItem(
                purchaseId = purchaseId,
                userId = userId, 
                productId = bookId, 
                mainCategory = currentBook.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = AppConstants.METHOD_FREE_LIBRARY,
                amountFromWallet = 0.0,
                amountPaidExternal = 0.0,
                totalPricePaid = 0.0,
                quantity = 1,
                orderConfirmation = orderConf
            ))

            // Add a persistent system alert to the user's notification center
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = AppConstants.NOTIF_TITLE_BOOK_PICKED_UP,
                productId = bookId,
                message = "'${currentBook.title}' has been added to your library collection.",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = AppConstants.NOTIF_TYPE_PICKUP
            ))

            // Dispatch external email confirmation via SMTP service
            if (user != null && user.email.isNotEmpty()) {
                val bookDetails = mapOf(
                    "Title" to currentBook.title,
                    "Author" to currentBook.author,
                    "Category" to currentBook.category,
                    "Format" to "Digital eBook"
                )
                EmailUtils.sendPurchaseConfirmation(
                    context = context,
                    recipientEmail = user.email,
                    userName = user.name,
                    itemTitle = currentBook.title,
                    orderRef = orderConf,
                    price = AppConstants.LABEL_FREE,
                    category = currentBook.mainCategory,
                    details = bookDetails
                )
            }

            onComplete(AppConstants.MSG_ADDED_TO_LIBRARY)
        }
    }

    /**
     * Finalizes a paid purchase transaction.
     * 
     * This function is called after the PaymentFlowDialog has successfully updated the
     * database. Its primary role is to trigger the external email confirmation service
     * with the final pricing and reference details.
     */
    fun handlePurchaseComplete(context: Context?, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserById(userId)
            val currentBook = _book.value ?: return@launch
            
            if (user != null && user.email.isNotEmpty()) {
                val priceStr = "£" + String.format(Locale.US, "%.2f", finalPrice)
                val bookDetails = mapOf(
                    "Title" to currentBook.title,
                    "Author" to currentBook.author,
                    "Category" to currentBook.category,
                    "Format" to "Digital eBook"
                )
                EmailUtils.sendPurchaseConfirmation(
                    context = context,
                    recipientEmail = user.email,
                    userName = user.name,
                    itemTitle = currentBook.title,
                    orderRef = orderRef,
                    price = priceStr,
                    category = currentBook.mainCategory,
                    details = bookDetails
                )
            }

            onComplete(AppConstants.MSG_PURCHASE_SUCCESS)
        }
    }

    /**
     * Permanently removes a product from the user's local collection.
     * Cleans up the purchase record from the Room database.
     */
    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, bookId)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
