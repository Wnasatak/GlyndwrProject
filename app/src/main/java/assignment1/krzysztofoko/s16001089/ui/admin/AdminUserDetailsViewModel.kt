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
 * Key Responsibilities:
 * - Reactive Data Aggregation: Combines multiple database flows to provide a unified student profile.
 * - Administrative Logic: Handles profile updates, enrolment status changes, and content moderation.
 * - Audit Integration: Records all administrative actions performed on student data.
 */
class AdminUserDetailsViewModel(
    private val userId: String, // The unique identifier of the user being managed.
    private val userDao: UserDao, // Access to user profile, wishlist, and financial records.
    courseDao: CourseDao, // Access to global course catalog for enrolment logic.
    classroomDao: ClassroomDao, // Access to module grades and academic progress.
    private val auditDao: AuditDao, // records administrative interventions for compliance.
    bookRepository: BookRepository // Access to the master product catalogue.
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance() // Handle for identifying the active administrator.
    
    // --- STATE MANAGEMENT ---
    // Internal flow for the student's core profile record.
    private val _user = MutableStateFlow<UserLocal?>(null)
    val user = _user.asStateFlow()

    // --- REACTIVE DATA STREAMS --- //

    /**
     * Master Catalogue Flow: 
     * Provides all products (Books, Courses, Gear) available in the system.
     * Used as a lookup table for cross-referencing user activity IDs.
     */
    val allBooks = bookRepository.getAllCombinedData(userId)
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Financial Records:
     * Stream of all university invoices issued to this specific student.
     */
    val invoices = userDao.getInvoicesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Audit Information:
     * Provides the historical search queries made by the student.
     */
    val searchHistory = userDao.getRecentSearches(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Social Activity:
     * All reviews and comments authored by the student for moderation.
     */
    val allReviews = userDao.getReviewsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Academic Status:
     * Current course applications and active enrolment records.
     */
    val courseEnrollments = userDao.getEnrollmentsForUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Performance Data:
     * Real-time grades and module submissions from the VLE.
     */
    val userGrades = classroomDao.getAllGradesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Ledger History:
     * Detailed chronological log of all deposits and expenditures.
     */
    val transactions = userDao.getWalletHistory(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * DERIVED FLOW: Browse History
     * Resolves a list of product IDs into full Book/Product objects for UI display.
     */
    val browseHistory = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * DERIVED FLOW: Wishlist
     * Combines wishlist timestamps with product data to show exactly what the student desires.
     */
    val wishlist = combine(allBooks, userDao.getWishlistItems(userId)) { books, wishItems ->
        wishItems.mapNotNull { wish -> 
            books.find { it.id == wish.productId }?.let { book ->
                wish to book
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * DERIVED FLOW: Engaged Products
     * Items that the student has actively reviewed or commented on.
     */
    val commentedBooks = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * DERIVED FLOW: Digital Library
     * All products and courses currently successfully purchased by the user.
     */
    val purchasedBooks = combine(allBooks, userDao.getPurchaseIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Course Directory:
     * List of all available university courses for enrolment modification logic.
     */
    val allCourses = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically initiate observation of the student's profile upon ViewModel creation.
        viewModelScope.launch {
            userDao.getUserFlow(userId).collect { _user.value = it }
        }
    }

    /**
     * Internal Auditor:
     * Inserts an entry into the system logs to document administrative changes.
     * @param action The specific action performed (e.g., EDITED, REMOVED).
     * @param targetId The ID of the student or record affected.
     * @param details Human-readable context for the audit trail.
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
     * Account Modification:
     * Persists updates to the student's core record (name, balance, status).
     */
    fun updateUser(updated: UserLocal) {
        viewModelScope.launch {
            userDao.upsertUser(updated) // Atomic update in local DB.
            addLog("EDITED", updated.id, "Admin updated details for student: ${updated.email}")
        }
    }

    /**
     * Academic Regulation:
     * Changes the status of a course application or enrolment record.
     */
    fun updateEnrollmentStatus(enrollmentId: String, newStatus: String) {
        viewModelScope.launch {
            userDao.updateEnrollmentStatus(enrollmentId, newStatus)
            addLog("ENROLMENT_STATUS_CHANGED", enrollmentId, "Admin updated enrolment status to $newStatus for user $userId")
        }
    }

    /**
     * Content Moderation:
     * Removes a student's review or comment if it violates institutional policy.
     */
    fun deleteComment(reviewId: Int) {
        viewModelScope.launch {
            userDao.deleteReview(reviewId)
            addLog("REMOVED", reviewId.toString(), "Admin deleted a comment (Review ID: $reviewId)")
        }
    }

    /**
     * Content Moderation:
     * Edits the existing text of a student's review.
     */
    fun updateReview(review: ReviewLocal) {
        viewModelScope.launch {
            userDao.addReview(review)
            addLog("EDITED", review.reviewId.toString(), "Admin edited a comment for student (Review ID: ${review.reviewId})")
        }
    }
}

/**
 * AdminUserDetailsViewModelFactory.kt
 * 
 * Standard ViewModelProvider Factory for correctly injecting Room DAOs 
 * and specific user IDs into the [AdminUserDetailsViewModel].
 */
class AdminUserDetailsViewModelFactory(
    private val userId: String,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    
    /**
     * Instantiates the ViewModel with all required data layers and Master Repository.
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
