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
 * AudioBookViewModel.kt
 *
 * This ViewModel acts as the data architect for the Audiobook details screen.
 * It manages the lifecycle of audiobook data, tracks user-specific states like
 * ownership and wishlist status, and coordinates the logic for both free and 
 * paid purchase workflows.
 */
class AudioBookViewModel(
    private val bookDao: BookDao,           
    private val audioBookDao: AudioBookDao, 
    private val userDao: UserDao,           
    private val bookId: String,             
    private val userId: String              
) : ViewModel() {

    // --- UI STATE FLOWS --- //

    // Holds the core book metadata once loaded.
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    // Indicates if a data fetch is currently in progress.
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Streams the current user's data, ensuring UI reacts to balance or role changes.
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Collects all available role-based discounts for real-time price calculations.
    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks if the current user already owns this specific item.
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Tracks if this book is in the user's personal wishlist.
    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Streams all user reviews for this product.
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(bookId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBook() // Kick off data loading immediately on creation.
    }

    /**
     * Attempts to fetch the book data from both generic and specialized audiobook tables.
     * Also records the view in the user's browsing history.
     */
    private fun loadBook() {
        viewModelScope.launch {
            _loading.value = true // Start loading.
            var fetchedBook = bookDao.getBookById(bookId) // Check general books table first.
            if (fetchedBook == null) {
                // If not found, check the dedicated audiobooks table.
                val ab = audioBookDao.getAudioBookById(bookId)
                if (ab != null) {
                    fetchedBook = ab.toBook() // Map specialized model to the generic Book model.
                }
            }
            _book.value = fetchedBook // Update state.
            
            // Add to browsing history if the user is authenticated.
            if (userId.isNotEmpty() && fetchedBook != null) {
                userDao.addToHistory(HistoryItem(userId, bookId))
            }
            _loading.value = false // Loading finished.
        }
    }

    /**
     * Toggles the presence of this book in the user's favorites list.
     */
    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch // Guard against guest users.
            if (inWishlist.value) {
                userDao.removeFromWishlist(userId, bookId) // Remove if already liked.
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                userDao.addToWishlist(WishlistItem(userId, bookId)) // Add to favorites.
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    /**
     * Handles the logic for adding a zero-cost item to the user's digital library.
     * This involves updating the database, creating a notification, and sending a confirmation email.
     */
    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentBook = _book.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference() // Generate unique ref.
            val purchaseId = UUID.randomUUID().toString()
            val user = userDao.getUserById(userId)

            // 1. Record the free purchase in the database.
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

            // 2. Alert the user with an in-app notification.
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = bookId,
                title = AppConstants.NOTIF_TITLE_AUDIOBOOK_PICKED_UP,
                message = "'${currentBook.title}' is now available in your collection.",
                timestamp = System.currentTimeMillis(),
                type = "PICKUP"
            ))

            // 3. Send a formal confirmation email.
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

            onComplete(AppConstants.MSG_ADDED_TO_LIBRARY) // Notify UI of success.
        }
    }

    /**
     * Finalises a paid transaction. The actual payment is handled in the UI flow, 
     * this method handles the post-purchase side effects like sending the receipt.
     */
    fun handlePurchaseComplete(context: Context?, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserById(userId)
            val currentBook = _book.value ?: return@launch
            
            // Send receipt via email.
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
     * Removes an item from the user's library.
     */
    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, bookId) // Wipe record from local DB.
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
