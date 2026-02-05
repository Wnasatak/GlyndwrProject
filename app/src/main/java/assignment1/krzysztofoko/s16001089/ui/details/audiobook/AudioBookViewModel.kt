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
 */
class AudioBookViewModel(
    private val bookDao: BookDao,           
    private val audioBookDao: AudioBookDao, 
    private val userDao: UserDao,           
    private val bookId: String,             
    private val userId: String              
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(bookId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(bookId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            _loading.value = true
            var fetchedBook = bookDao.getBookById(bookId)
            if (fetchedBook == null) {
                val ab = audioBookDao.getAudioBookById(bookId)
                if (ab != null) {
                    fetchedBook = ab.toBook() 
                }
            }
            _book.value = fetchedBook
            if (userId.isNotEmpty() && fetchedBook != null) {
                userDao.addToHistory(HistoryItem(userId, bookId))
            }
            _loading.value = false
        }
    }

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

    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentBook = _book.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference()
            val purchaseId = UUID.randomUUID().toString()
            val user = userDao.getUserById(userId)

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

            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = bookId,
                title = AppConstants.NOTIF_TITLE_AUDIOBOOK_PICKED_UP,
                message = "'${currentBook.title}' is now available in your collection.",
                timestamp = System.currentTimeMillis(),
                type = "PICKUP"
            ))

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

    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, bookId)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
