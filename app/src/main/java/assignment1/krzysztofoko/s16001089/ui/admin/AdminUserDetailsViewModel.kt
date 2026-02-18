package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * AdminUserDetailsViewModel.kt
 *
 * This ViewModel serves as the data architect for the Admin's view of a specific user's details.
 * It aggregates a vast array of student-specific data streams, including browsing history,
 * financial transactions, academic enrolments, and social activity (reviews).
 * 
 * It also provides the administrative logic for updating student profiles, changing
 * enrolment statuses, and moderating user-generated content.
 */
class AdminUserDetailsViewModel(
    private val userId: String, // The ID of the student being managed.
    private val userDao: UserDao, // DAO for user-centric data.
    courseDao: CourseDao, // Injected for enrolment management logic.
    classroomDao: ClassroomDao, // Injected for VLE data access.
    private val auditDao: AuditDao, // DAO for recording admin interventions.
    bookRepository: BookRepository // Repository for master catalogue access.
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance() // Handle for current admin session.
    
    // Internal state for the student's profile data.
    private val _user = MutableStateFlow<UserLocal?>(null)
    val user = _user.asStateFlow()

    // --- AGGREGATED DATA STREAMS --- //

    // Master list of all books/courses for cross-referencing.
    val allBooks = bookRepository.getAllCombinedData(userId)
        .map { it } // Mapping directly as the list is guaranteed non-null.
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All invoices issued to this specific student.
    val invoices = userDao.getInvoicesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // The student's historical search queries for auditing.
    val searchHistory = userDao.getRecentSearches(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Every review and comment posted by this student.
    val allReviews = userDao.getReviewsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current academic application and enrolment records.
    val courseEnrollments = userDao.getEnrollmentsForUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live grade data from the virtual learning environment.
    val userGrades = classroomDao.getAllGradesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Complete transaction history for the student's university wallet.
    val transactions = userDao.getWalletHistory(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived: Recent items the student has browsed.
    val browseHistory = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived: Items currently in the student's wishlist.
    val wishlist = combine(allBooks, userDao.getWishlistItems(userId)) { books, wishItems ->
        wishItems.mapNotNull { wish -> 
            books.find { it.id == wish.productId }?.let { book ->
                wish to book
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived: Store items the student has commented on.
    val commentedBooks = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived: List of all products and courses currently owned by the student.
    val purchasedBooks = combine(allBooks, userDao.getPurchaseIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of all available courses for enrolment management.
    val allCourses = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically start observing the student's profile on initialisation.
        viewModelScope.launch {
            userDao.getUserFlow(userId).collect { _user.value = it }
        }
    }

    /**
     * Internal utility to record administrative interventions into the system audit trail.
     */
    private fun addLog(action: String, targetId: String, details: String) {
        viewModelScope.launch {
            val adminUser = auth.currentUser
            auditDao.insertLog(SystemLog(
                userId = adminUser?.uid ?: "unknown",
                userName = adminUser?.displayName ?: "Admin",
                action = action,
                targetId = targetId,
                details = details,
                logType = "ADMIN"
            ))
        }
    }

    /**
     * Directly updates the student's profile information in the local database.
     */
    fun updateUser(updated: UserLocal) {
        viewModelScope.launch {
            userDao.upsertUser(updated) // Perform atomic database update.
            addLog("EDITED", updated.id, "Admin updated details for student: ${updated.email}")
        }
    }

    /**
     * Modifies the status of a specific academic enrolment (e.g., from PENDING to APPROVED).
     */
    fun updateEnrollmentStatus(enrollmentId: String, newStatus: String) {
        viewModelScope.launch {
            userDao.updateEnrollmentStatus(enrollmentId, newStatus) // Update the record.
            addLog("ENROLMENT_STATUS_CHANGED", enrollmentId, "Admin updated enrolment status to $newStatus for user $userId")
        }
    }

    /**
     * Moderation tool: Permanently removes a student's review or comment.
     */
    fun deleteComment(reviewId: Int) {
        viewModelScope.launch {
            userDao.deleteReview(reviewId) // Wipe the record from the database.
            addLog("REMOVED", reviewId.toString(), "Admin deleted a comment (Review ID: $reviewId)")
        }
    }

    /**
     * Moderation tool: Allows an admin to edit the text of a student's review.
     */
    fun updateReview(review: ReviewLocal) {
        viewModelScope.launch {
            userDao.addReview(review) // Persist the edited review.
            addLog("EDITED", review.reviewId.toString(), "Admin edited a comment for student (Review ID: ${review.reviewId})")
        }
    }
}

/**
 * AdminUserDetailsViewModelFactory.kt
 * 
 * Standard factory for injecting Room DAOs and the target user ID into the ViewModel.
 */
class AdminUserDetailsViewModelFactory(
    private val userId: String,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    
    /**
     * Instantiates the complex ViewModel with all its required dependencies.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdminUserDetailsViewModel(
            userId, 
            db.userDao(), 
            db.courseDao(),
            db.classroomDao(), 
            db.auditDao(),
            BookRepository(db)
        ) as T
    }
}
