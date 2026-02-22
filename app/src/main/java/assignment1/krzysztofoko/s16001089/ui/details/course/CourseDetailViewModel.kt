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
 * CourseDetailViewModel.kt
 *
 * This ViewModel acts as the central logic controller for the Course Details screen.
 * It manages a sophisticated academic enrolment workflow, including:
 * 1. Initial application submission.
 * 2. Real-time tracking of application review status.
 * 3. Finalisation of enrolment through payment or free claims.
 * 4. User role promotion upon successful enrolment.
 */
class CourseDetailViewModel(
    private val courseDao: CourseDao,
    private val userDao: UserDao,
    private val courseId: String,
    val userId: String
) : ViewModel() {

    // --- UI STATE FLOWS --- //

    // Holds the core course metadata loaded from the database.
    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    // Indicates if initial data fetching is in progress.
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Streams current user profile data, including balance and role.
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Collects all role-based discounts for live pricing updates.
    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Reactively checks if the user is already officially enrolled in this course.
     */
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Application Details Flow:
     * Monitors the academic application table for status changes (PENDING, APPROVED, etc.).
     */
    val applicationDetails: StateFlow<CourseEnrollmentDetails?> = if (userId.isNotEmpty()) {
        userDao.getEnrollmentDetailsFlow(userId, courseId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Enrolled Paid Course Logic:
     * Checks if the user is currently enrolled in any other paid course to enforce 
     * the "one paid course at a time" university policy.
     */
    val enrolledPaidCourseTitle: StateFlow<String?> = if (userId.isNotEmpty()) {
        userDao.getAllPurchasesFlow(userId).map { purchases ->
            val paidCoursePurchase =
                purchases.find { it.mainCategory == AppConstants.CAT_COURSES && it.totalPricePaid > 0.0 }
            if (paidCoursePurchase != null) {
                // If a paid course exists, fetch its title for the warning UI.
                courseDao.getCourseById(paidCoursePurchase.productId)?.title
            } else null
        }
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Tracks wishlist status for the current course.
    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Streams all user-submitted reviews for this academic programme.
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCourse() // Initialise the data fetch on ViewModel creation.
    }

    /**
     * Fetches the core course data and records the visit in the user's history.
     */
    private fun loadCourse() {
        viewModelScope.launch {
            _loading.value = true // Start loading indicator.
            val fetchedCourse = courseDao.getCourseById(courseId) // Query Room.
            _course.value = fetchedCourse // Update state.
            
            // Record the browsing history if authenticated.
            if (userId.isNotEmpty() && fetchedCourse != null) {
                userDao.addToHistory(HistoryItem(userId, courseId))
            }
            _loading.value = false // Stop loading indicator.
        }
    }

    /**
     * Toggles the presence of this course in the user's favorites.
     */
    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch // Guard for guest users.
            if (inWishlist.value) {
                userDao.removeFromWishlist(userId, courseId) // Remove record.
                onComplete(AppConstants.MSG_REMOVED_FAVORITES)
            } else {
                // Ensure it's in history before adding to wishlist.
                userDao.addToHistory(HistoryItem(userId, courseId))
                userDao.addToWishlist(WishlistItem(userId, courseId)) // Add record.
                onComplete(AppConstants.MSG_ADDED_FAVORITES)
            }
        }
    }

    /**
     * Workflow Step 1: Submit Enrollment Application.
     * Records official academic details for staff review. No financial transaction occurs yet.
     */
    fun submitEnrollmentApplication(
        details: CourseEnrollmentDetails,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val currentCourse = _course.value ?: return@launch

            // 1. Persist the formal application details.
            userDao.addEnrollmentDetails(details)

            // 2. Create a notification to confirm successful submission.
            userDao.addNotification(
                NotificationLocal(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    productId = courseId,
                    title = "Application Submitted",
                    message = "Your application for '${currentCourse.title}' is now under review by university staff.",
                    timestamp = System.currentTimeMillis(),
                    type = "GENERAL"
                )
            )

            onComplete() // Notify UI to transition.
        }
    }

    /**
     * Workflow Step 3: Finalize Enrollment (Post-Approval).
     * Converts an approved application into an active enrolment.
     */
    fun finalizeEnrollment(
        context: Context?,
        isPaid: Boolean = false,
        finalPrice: Double = 0.0,
        orderRef: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val currentCourse = _course.value ?: return@launch
            val user = localUser.value
            val effectiveOrderRef = orderRef ?: OrderUtils.generateOrderReference()

            // 1. Create the final Purchase/Enrolment record in the library.
            userDao.addPurchase(
                PurchaseItem(
                    purchaseId = UUID.randomUUID().toString(),
                    userId = userId,
                    productId = courseId,
                    mainCategory = currentCourse.mainCategory,
                    purchasedAt = System.currentTimeMillis(),
                    // Distinguish between paid university account charges and free claims.
                    paymentMethod = if (isPaid) AppConstants.METHOD_UNIVERSITY_ACCOUNT else AppConstants.METHOD_FREE_ENROLLMENT,
                    totalPricePaid = finalPrice,
                    orderConfirmation = effectiveOrderRef
                )
            )

            // 2. Officially promote the user's role to "student" to unlock academic perks.
            if (user != null && user.role != "admin") {
                userDao.upsertUser(user.copy(role = "student"))
            }

            // 3. Mark the application status as "ENROLLED" in the database.
            val enrollmentId = "${userId}_${courseId}"
            userDao.updateEnrollmentStatus(enrollmentId, "ENROLLED")

            // 4. Send a formal confirmation email.
            if (user != null && user.email.isNotEmpty()) {
                val priceStr = if (finalPrice <= 0) AppConstants.LABEL_FREE else "£" + String.format(Locale.US, "%.2f", finalPrice)
                val academicDetails = mapOf(
                    "Course Name" to currentCourse.title,
                    "Department" to currentCourse.department,
                    "Academic Status" to "Active Student",
                    "Course ID" to currentCourse.id
                )
                EmailUtils.sendPurchaseConfirmation(
                    context = context,
                    recipientEmail = user.email,
                    userName = user.name,
                    itemTitle = currentCourse.title,
                    orderRef = effectiveOrderRef,
                    price = priceStr,
                    category = currentCourse.mainCategory,
                    details = academicDetails
                )
            }

            // 5. Send a celebratory confirmation notification.
            userDao.addNotification(
                NotificationLocal(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    productId = courseId,
                    title = AppConstants.NOTIF_TITLE_COURSE_ENROLLED,
                    message = "Welcome to the course! You are now fully enrolled in '${currentCourse.title}'.",
                    timestamp = System.currentTimeMillis(),
                    type = "PURCHASE"
                )
            )

            onComplete() // Finalise UI transition.
        }
    }

    /**
     * Removes an enrolment from the user's library.
     * Note: This reverts the application status so the user can re-enrol later if desired.
     */
    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            // Delete the purchase record from the library.
            userDao.deletePurchase(userId, courseId)
            
            // Revert enrolment status back to "APPROVED" to allow for future re-acquisition.
            val enrollmentId = "${userId}_${courseId}"
            userDao.updateEnrollmentStatus(enrollmentId, "APPROVED")
            
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}
