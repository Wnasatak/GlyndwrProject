package assignment1.krzysztofoko.s16001089.ui.details.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class BookDetailViewModel(
    private val bookDao: BookDao,
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
            val fetchedBook = bookDao.getBookById(bookId)
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
                onComplete("Removed from favorites")
            } else {
                userDao.addToWishlist(WishlistItem(userId, bookId))
                onComplete("Added to favorites!")
            }
        }
    }

    fun addFreePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentBook = _book.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference()
            val invoiceNum = OrderUtils.generateInvoiceNumber()
            val purchaseId = UUID.randomUUID().toString()
            val user = localUser.value

            // Save purchase record
            userDao.addPurchase(PurchaseItem(
                purchaseId = purchaseId,
                userId = userId, 
                productId = bookId, 
                mainCategory = currentBook.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = "Free Library",
                amountFromWallet = 0.0,
                amountPaidExternal = 0.0,
                totalPricePaid = 0.0,
                quantity = 1,
                orderConfirmation = orderConf
            ))

            // Create professional invoice
            userDao.addInvoice(Invoice(
                invoiceNumber = invoiceNum,
                userId = userId,
                productId = bookId,
                itemTitle = currentBook.title,
                itemCategory = currentBook.mainCategory,
                itemVariant = null,
                pricePaid = 0.0,
                discountApplied = 0.0,
                quantity = 1,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = "Free Library",
                orderReference = orderConf,
                billingName = user?.name ?: "Student",
                billingEmail = user?.email ?: "",
                billingAddress = user?.address
            ))

            // Trigger internal notification
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Item Added",
                productId = bookId,
                message = "'${currentBook.title}' has been added to your library.",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = "PURCHASE"
            ))

            onComplete("Added to your library! Ref: $orderConf")
        }
    }

    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, bookId)
            onComplete("Removed from library")
        }
    }
}
