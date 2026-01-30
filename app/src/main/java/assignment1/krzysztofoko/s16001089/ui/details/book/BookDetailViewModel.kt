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
 * ViewModel for the Book Details screen.
 */
class BookDetailViewModel(
    private val bookDao: BookDao,        
    private val userDao: UserDao,        
    private val auditDao: AuditDao,      
    private val bookId: String,          
    private val userId: String           
) : ViewModel() {

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    private fun loadBook() {
        viewModelScope.launch {
            _loading.value = true
            val fetchedBook = bookDao.getBookById(bookId)
            _book.value = fetchedBook
            if (userId.isNotEmpty() && fetchedBook != null) {
                userDao.addToHistory(HistoryItem(userId, bookId))
                addLog("VIEW_ITEM", "User viewed: ${fetchedBook.title}")
            }
            _loading.value = false
        }
    }

    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            if (inWishlist.value) {
                userDao.removeFromWishlist(userId, bookId)
                addLog("WISHLIST_REMOVE", "Removed item from wishlist")
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                userDao.addToWishlist(WishlistItem(userId, bookId))
                addLog("WISHLIST_ADD", "Added item to wishlist")
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentBook = _book.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference()
            
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

            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = AppConstants.NOTIF_TITLE_BOOK_PICKED_UP,
                productId = bookId,
                message = "'${currentBook.title}' has been added to your library.",
                timestamp = System.currentTimeMillis(),
                type = AppConstants.NOTIF_TYPE_PICKUP
            ))

            addLog("PURCHASE_FREE", "Free pickup: ${currentBook.title}")
            onComplete(AppConstants.MSG_ADDED_TO_LIBRARY)
        }
    }

    fun handlePurchaseComplete(context: Context?, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val currentBook = _book.value ?: return@launch
            addLog("PURCHASE_PAID", "Paid £${String.format(Locale.US, "%.2f", finalPrice)} for: ${currentBook.title}")
            onComplete(AppConstants.MSG_PURCHASE_SUCCESS)
        }
    }

    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, bookId)
            addLog("REMOVE_FROM_LIBRARY", "User removed item from library")
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
