package assignment1.krzysztofoko.s16001089.ui.details.audiobook

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
 * ViewModel for the Audiobook Details screen.
 * 
 * Manages the state and logic for a specific audiobook, including cross-table data fetching
 * (supports items from both 'books' and 'audiobooks' tables), purchase history,
 * wishlist status, and external email notifications.
 */
class AudioBookViewModel(
    private val bookDao: BookDao,           // DAO for products in the 'books' table
    private val audioBookDao: AudioBookDao, // DAO for products in the 'audiobooks' table
    private val userDao: UserDao,           // DAO for user-specific data (History, Wishlist, Notifications)
    private val bookId: String,             // The unique identifier for the audiobook
    private val userId: String              // The currently authenticated user's ID
) : ViewModel() {

    // --- Core Reactive UI State ---

    // Holds the resolved product metadata
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    // Flag to track data resolution for the UI spinner
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /**
     * Local User Profile Flow:
     * Provides real-time updates of the user's local profile data.
     */
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Ownership Check Flow:
     * Reactively checks if the user has purchased this specific audiobook.
     */
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Wishlist Status Flow:
     * Tracks if the item is currently in the user's favorites list.
     */
    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Social Data Flow:
     * Collects all community reviews for this specific product ID.
     */
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(bookId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize by fetching product data and recording the visit
        loadBook()
    }

    /**
     * Cross-table Data Loader:
     * Attempts to find the product ID in the 'books' table first. If missing, 
     * it falls back to the 'audiobooks' table and maps the result to the unified 'Book' model.
     */
    private fun loadBook() {
        viewModelScope.launch {
            _loading.value = true
            // Priority 1: Search main books catalog
            var fetchedBook = bookDao.getBookById(bookId)
            
            // Priority 2: Search specific audiobooks catalog
            if (fetchedBook == null) {
                val ab = audioBookDao.getAudioBookById(bookId)
                if (ab != null) {
                    fetchedBook = ab.toBook() // Mapper call from Mappers.kt
                }
            }

            _book.value = fetchedBook
            
            // Record this interaction in the user's viewing history
            if (userId.isNotEmpty() && fetchedBook != null) {
                userDao.addToHistory(HistoryItem(userId, bookId))
            }
            _loading.value = false
        }
    }

    /**
     * Toggles the favorite status of the audiobook.
     */
    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            if (inWishlist.value) {
                userDao.removeFromWishlist(userId, bookId)
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                userDao.addToWishlist(WishlistItem(userId, bookId))
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    /**
     * Workflow for collecting a free audiobook.
     * 
     * 1. Records the acquisition in the local 'purchases' table.
     * 2. Creates a system notification alert.
     * 3. Sends a stylized HTML confirmation email.
     */
    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentBook = _book.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference()
            val purchaseId = UUID.randomUUID().toString()
            val user = userDao.getUserById(userId)

            // Persist the transaction locally
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

            // Trigger internal system alert
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = bookId,
                title = AppConstants.NOTIF_TITLE_AUDIOBOOK_PICKED_UP,
                message = "'${currentBook.title}' is now available in your collection.",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = "PICKUP"
            ))

            // Dispatch external confirmation via SMTP
            if (user != null && user.email.isNotEmpty()) {
                val bookDetails = mapOf(
                    "Title" to currentBook.title,
                    "Author" to currentBook.author,
                    "Format" to "Digital Audio",
                    "Category" to currentBook.category
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
     * Finalizes a paid transaction and dispatches the confirmation email.
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
                    "Format" to "Digital Audio",
                    "Category" to currentBook.category
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
     * Permanently removes the item record from the user's library.
     */
    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, bookId)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
