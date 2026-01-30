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
 * ViewModel for the Course Details screen and Enrollment Application.
 * 
 * Manages the multi-step enrollment process:
 * 1. Application Submission (Pending Review)
 * 2. Admin Approval (Status changes to Approved)
 * 3. User Payment/Final Enrollment (Status changes to Enrolled)
 */
class CourseDetailViewModel(
    private val courseDao: CourseDao,        
    private val userDao: UserDao,            
    private val courseId: String,            
    val userId: String               
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

    /**
     * Reactively checks if the user is already fully enrolled (paid and approved).
     */
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Application Details Flow:
     * Tracks the status of the academic application (PENDING_REVIEW, APPROVED, REJECTED).
     */
    val applicationDetails: StateFlow<CourseEnrollmentDetails?> = if (userId.isNotEmpty()) {
        userDao.getEnrollmentDetailsFlow(userId, courseId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val enrolledPaidCourseTitle: StateFlow<String?> = if (userId.isNotEmpty()) {
        userDao.getAllPurchasesFlow(userId).map { purchases ->
            val paidCoursePurchase = purchases.find { it.mainCategory == AppConstants.CAT_COURSES && it.totalPricePaid > 0.0 }
            if (paidCoursePurchase != null) {
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

    /**
     * Step 1: Submit Application.
     * Stores the academic details but DOES NOT yet create a purchase/enrollment record.
     */
    fun submitEnrollmentApplication(
        details: CourseEnrollmentDetails,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val currentCourse = _course.value ?: return@launch
            
            // Store Application Details
            userDao.addEnrollmentDetails(details)

            // Create Notification
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = courseId,
                title = "Application Submitted",
                message = "Your application for '${currentCourse.title}' is now under review by university staff.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))

            onComplete()
        }
    }

    /**
     * Step 3: Finalize Enrollment (After Approval).
     * This is called when the user completes payment or free enrollment for an approved course.
     */
    fun finalizeEnrollment(
        isPaid: Boolean = false,
        finalPrice: Double = 0.0,
        orderRef: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val currentCourse = _course.value ?: return@launch
            val user = localUser.value
            val effectiveOrderRef = orderRef ?: OrderUtils.generateOrderReference()

            // 1. Create the final Purchase/Enrollment record
            userDao.addPurchase(PurchaseItem(
                purchaseId = UUID.randomUUID().toString(),
                userId = userId,
                productId = courseId,
                mainCategory = currentCourse.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = if (isPaid) AppConstants.METHOD_UNIVERSITY_ACCOUNT else AppConstants.METHOD_FREE_ENROLLMENT,
                totalPricePaid = finalPrice,
                orderConfirmation = effectiveOrderRef
            ))

            // 2. Officially promote user role to "student"
            if (user != null && user.role != "admin") {
                userDao.upsertUser(user.copy(role = "student"))
            }

            // 3. Update application status if needed (optional, purchase existence usually implies ENROLLED)
            
            // 4. Send Confirmation Notification
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = courseId,
                title = AppConstants.NOTIF_TITLE_COURSE_ENROLLED,
                message = "Welcome to the course! You are now fully enrolled in '${currentCourse.title}'.",
                timestamp = System.currentTimeMillis(),
                type = "PURCHASE"
            ))

            onComplete()
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
