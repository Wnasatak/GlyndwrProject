package assignment1.krzysztofoko.s16001089.ui.details.course

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

class CourseDetailViewModel(
    private val courseDao: CourseDao,
    private val userDao: UserDao,
    private val courseId: String,
    private val userId: String
) : ViewModel() {

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Holds the title of the paid course the user is currently enrolled in, if any
    val enrolledPaidCourseTitle: StateFlow<String?> = if (userId.isNotEmpty()) {
        userDao.getAllPurchasesFlow(userId).map { purchases ->
            val paidCoursePurchase = purchases.find { it.mainCategory == AppConstants.CAT_COURSES && it.totalPricePaid > 0.0 }
            if (paidCoursePurchase != null) {
                // Fetch course title from DAO
                courseDao.getCourseById(paidCoursePurchase.productId)?.title
            } else null
        }
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCourse()
    }

    private fun loadCourse() {
        viewModelScope.launch {
            _loading.value = true
            val fetchedCourse = courseDao.getCourseById(courseId)
            _course.value = fetchedCourse
            
            if (userId.isNotEmpty() && fetchedCourse != null) {
                userDao.addToHistory(HistoryItem(userId, courseId))
            }
            _loading.value = false
        }
    }

    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            if (inWishlist.value) {
                userDao.removeFromWishlist(userId, courseId)
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                userDao.addToHistory(HistoryItem(userId, courseId))
                userDao.addToWishlist(WishlistItem(userId, courseId))
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentCourse = _course.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference()
            val purchaseId = UUID.randomUUID().toString()
            val user = localUser.value

            userDao.addPurchase(PurchaseItem(
                purchaseId = purchaseId,
                userId = userId, 
                productId = courseId, 
                mainCategory = currentCourse.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = AppConstants.METHOD_FREE_ENROLLMENT,
                amountFromWallet = 0.0,
                amountPaidExternal = 0.0,
                totalPricePaid = 0.0,
                quantity = 1,
                orderConfirmation = orderConf
            ))

            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = courseId,
                title = AppConstants.NOTIF_TITLE_COURSE_ENROLLED,
                message = "You have successfully enrolled in '${currentCourse.title}'.",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = "PICKUP"
            ))

            if (user != null && user.email.isNotEmpty()) {
                val courseDetails = mapOf(
                    "Course Name" to currentCourse.title,
                    "Department" to currentCourse.department,
                    "Category" to currentCourse.category,
                    "Access Type" to "Full Access (Scholarship)"
                )
                EmailUtils.sendPurchaseConfirmation(
                    context = context,
                    recipientEmail = user.email,
                    userName = user.name,
                    itemTitle = currentCourse.title,
                    orderRef = orderConf,
                    price = AppConstants.LABEL_FREE,
                    category = currentCourse.mainCategory,
                    details = courseDetails
                )
            }

            onComplete("${AppConstants.MSG_ENROLL_FREE_SUCCESS} Ref: $orderConf")
        }
    }

    fun handlePurchaseComplete(context: Context?, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val user = localUser.value
            val currentCourse = _course.value ?: return@launch
            
            if (user != null && user.email.isNotEmpty()) {
                val priceStr = "£" + String.format(Locale.US, "%.2f", finalPrice)
                val courseDetails = mapOf(
                    "Course Name" to currentCourse.title,
                    "Department" to currentCourse.department,
                    "Category" to currentCourse.category,
                    "Access Type" to "Instant Digital Access"
                )
                EmailUtils.sendPurchaseConfirmation(
                    context = context,
                    recipientEmail = user.email,
                    userName = user.name,
                    itemTitle = currentCourse.title,
                    orderRef = orderRef,
                    price = priceStr,
                    category = currentCourse.mainCategory,
                    details = courseDetails
                )
            }

            onComplete(AppConstants.MSG_ENROLL_PAID_SUCCESS)
        }
    }

    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, courseId)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
