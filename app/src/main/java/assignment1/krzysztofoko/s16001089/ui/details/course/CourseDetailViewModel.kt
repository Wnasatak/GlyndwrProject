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

/**
 * ViewModel for the Course Details screen.
 * 
 * Manages the data and business logic for university course enrollment. This includes
 * checking existing enrollments, handling free/scholarship enrollments, processing 
 * paid enrollments with email confirmations, and managing course-specific reviews.
 */
class CourseDetailViewModel(
    private val courseDao: CourseDao,        // DAO for core academic course data
    private val userDao: UserDao,            // DAO for user session data (Enrollments, Wishlist, Notifications)
    private val courseId: String,            // Unique ID of the course being viewed
    private val userId: String               // ID of the current authenticated student
) : ViewModel() {

    // --- Core Reactive Data Streams ---

    // State holding the course metadata resolved from the database
    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    // Flag tracking the initial data loading state for the UI spinner
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /**
     * Local User Profile Flow:
     * Provides real-time profile updates (name, email, balance).
     */
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Enrollment Status Flow:
     * Reactively checks if the user is already enrolled in this specific course.
     */
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Enrollment Conflict Check:
     * Students are limited to one paid course enrollment at a time.
     * This flow finds any existing paid course enrollment and resolves its title
     * to inform the user why enrollment might be locked.
     */
    val enrolledPaidCourseTitle: StateFlow<String?> = if (userId.isNotEmpty()) {
        userDao.getAllPurchasesFlow(userId).map { purchases ->
            // Search for any course purchase with a price > 0
            val paidCoursePurchase = purchases.find { it.mainCategory == AppConstants.CAT_COURSES && it.totalPricePaid > 0.0 }
            if (paidCoursePurchase != null) {
                // Return the title of that course
                courseDao.getCourseById(paidCoursePurchase.productId)?.title
            } else null
        }
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Wishlist Status Flow:
     * Tracks if the student has marked this course as a favorite.
     */
    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Academic Reviews Flow:
     * Collects all student feedback posted for this specific course.
     */
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically resolve course data and update history on init
        loadCourse()
    }

    /**
     * Data Resolution Logic:
     * Fetches metadata from courseDao and records the visit in the student's viewing history.
     */
    private fun loadCourse() {
        viewModelScope.launch {
            _loading.value = true
            val fetchedCourse = courseDao.getCourseById(courseId)
            _course.value = fetchedCourse
            
            // Log interaction for personal history
            if (userId.isNotEmpty() && fetchedCourse != null) {
                userDao.addToHistory(HistoryItem(userId, courseId))
            }
            _loading.value = false
        }
    }

    /**
     * Toggles the course's favorite status.
     * Also updates the student's history when adding to favorites.
     */
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

    /**
     * Workflow for free academic enrollment (e.g. Scholarship or Trial courses).
     * 
     * Process:
     * 1. Records enrollment in local DB.
     * 2. Triggers a system notification.
     * 3. Dispatches course-specific HTML email confirmation.
     */
    fun addFreePurchase(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            val currentCourse = _course.value ?: return@launch
            val orderConf = OrderUtils.generateOrderReference()
            val purchaseId = UUID.randomUUID().toString()
            val user = localUser.value

            // Persist enrollment transaction
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

            // Create notification alert
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

            // Dispatch external confirmation via SMTP
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

    /**
     * Finalizes a paid course enrollment.
     * 
     * Called after the UI Payment flow completes. Triggers the official email
     * confirmation service with transaction details.
     */
    fun handlePurchaseComplete(context: Context?, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val user = localUser.value
            val currentCourse = _course.value ?: return@launch
            
            // SMTP Dispatch logic
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

    /**
     * Unenrolls the student from the course.
     * Deletes the record from the persistent local database.
     */
    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, courseId)
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
