package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminUserDetailsViewModel(
    private val userId: String,
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val classroomDao: ClassroomDao,
    private val auditDao: AuditDao,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _user = MutableStateFlow<UserLocal?>(null)
    val user = _user.asStateFlow()

    val allBooks = bookRepository.getAllCombinedData(userId)
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices = userDao.getInvoicesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory = userDao.getRecentSearches(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews = userDao.getReviewsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseInstallments = userDao.getCourseInstallmentsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseEnrollments = userDao.getEnrollmentsForUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userGrades = classroomDao.getAllGradesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions = userDao.getWalletHistory(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val browseHistory = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlist = combine(allBooks, userDao.getWishlistItems(userId)) { books, wishItems ->
        wishItems.mapNotNull { wish -> 
            books.find { it.id == wish.productId }?.let { book ->
                wish to book
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commentedBooks = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedBooks = combine(allBooks, userDao.getPurchaseIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            userDao.getUserFlow(userId).collect { _user.value = it }
        }
    }

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

    fun updateUser(updated: UserLocal) {
        viewModelScope.launch {
            userDao.upsertUser(updated)
            addLog("EDITED", updated.id, "Admin updated details for student: ${updated.email}")
        }
    }

    fun updateEnrollmentStatus(enrollmentId: String, newStatus: String) {
        viewModelScope.launch {
            userDao.updateEnrollmentStatus(enrollmentId, newStatus)
            addLog("ENROLLMENT_STATUS_CHANGED", enrollmentId, "Admin updated enrollment status to $newStatus for user $userId")
        }
    }

    fun deleteComment(reviewId: Int) {
        viewModelScope.launch {
            userDao.deleteReview(reviewId)
            addLog("REMOVED", reviewId.toString(), "Admin deleted a comment (Review ID: $reviewId)")
        }
    }

    fun updateReview(review: ReviewLocal) {
        viewModelScope.launch {
            userDao.addReview(review)
            addLog("EDITED", review.reviewId.toString(), "Admin edited a comment for student (Review ID: ${review.reviewId})")
        }
    }
}

class AdminUserDetailsViewModelFactory(
    private val userId: String,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
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
