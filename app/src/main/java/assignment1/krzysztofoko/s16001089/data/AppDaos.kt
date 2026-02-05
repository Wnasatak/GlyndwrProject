package assignment1.krzysztofoko.s16001089.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

const val LOCAL_USER_ID = "local_student_001"

@Entity(tableName = "assigned_courses", primaryKeys = ["tutorId", "courseId"])
data class AssignedCourse(
    val tutorId: String,
    val courseId: String,
    val assignedAt: Long = System.currentTimeMillis()
)

@Dao
interface AssignedCourseDao {
    @Query("SELECT * FROM assigned_courses WHERE tutorId = :tutorId")
    fun getAssignedCoursesForTutor(tutorId: String): Flow<List<AssignedCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignCourse(assignedCourse: AssignedCourse)

    @Query("DELETE FROM assigned_courses WHERE tutorId = :tutorId AND courseId = :courseId")
    suspend fun unassignCourse(tutorId: String, courseId: String)
    
    @Query("DELETE FROM assigned_courses")
    suspend fun deleteAll()
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<Book>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBook(book: Book)

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): Book?

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: String)

    @Query("DELETE FROM books")
    suspend fun deleteAll()
}

@Dao
interface AudioBookDao {
    @Query("SELECT * FROM audiobooks")
    fun getAllAudioBooks(): Flow<List<AudioBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audioBooks: List<AudioBook>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAudioBook(audioBook: AudioBook)

    @Query("SELECT * FROM audiobooks WHERE id = :id")
    suspend fun getAudioBookById(id: String): AudioBook?

    @Query("DELETE FROM audiobooks WHERE id = :id")
    suspend fun deleteAudioBook(id: String)

    @Query("DELETE FROM audiobooks")
    suspend fun deleteAll()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourse(course: Course)

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: String): Course?

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: String)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}

@Dao
interface GearDao {
    @Query("SELECT * FROM gear")
    fun getAllGear(): Flow<List<Gear>>

    @Query("SELECT * FROM gear")
    suspend fun getAllGearOnce(): List<Gear>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gear: List<Gear>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGear(gear: Gear)

    @Query("SELECT * FROM gear WHERE id = :id")
    suspend fun getGearById(id: String): Gear?

    @Query("UPDATE gear SET stockCount = CASE WHEN stockCount >= :quantity THEN stockCount - :quantity ELSE 0 END WHERE id = :id")
    suspend fun reduceStock(id: String, quantity: Int)

    @Query("DELETE FROM gear WHERE id = :id")
    suspend fun deleteGear(id: String)

    @Query("DELETE FROM gear")
    suspend fun deleteAll()
}

@Entity(tableName = "users_local")
data class UserLocal(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val title: String? = null, // Added title field
    val address: String? = null,
    val phoneNumber: String? = null,
    val selectedPaymentMethod: String? = null,
    val balance: Double = 0.0,
    val role: String = "student"
)

@Entity(tableName = "wishlist", primaryKeys = ["userId", "productId"])
data class WishlistItem(val userId: String, val productId: String, val addedAt: Long = System.currentTimeMillis())

@Entity(tableName = "purchases")
data class PurchaseItem(
    @PrimaryKey val purchaseId: String, 
    val userId: String,
    val productId: String,
    val mainCategory: String, 
    val purchasedAt: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Unknown",
    val amountFromWallet: Double = 0.0,
    val amountPaidExternal: Double = 0.0,
    val totalPricePaid: Double = 0.0, 
    val quantity: Int = 1,
    val orderConfirmation: String? = null
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val invoiceNumber: String,
    val userId: String,
    val productId: String,
    val itemTitle: String,
    val itemCategory: String,
    val itemVariant: String? = null,
    val pricePaid: Double,
    val discountApplied: Double = 0.0,
    val quantity: Int = 1,
    val purchasedAt: Long = System.currentTimeMillis(),
    val paymentMethod: String,
    val orderReference: String? = null,
    val billingName: String,
    val billingEmail: String,
    val billingAddress: String? = null
)

@Entity(tableName = "notifications")
data class NotificationLocal(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String, 
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "GENERAL"
)

@Entity(tableName = "search_history")
data class SearchHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "course_installments", primaryKeys = ["userId", "courseId"])
data class CourseInstallment(
    val userId: String,
    val courseId: String,
    val modulesPaid: Int = 1,
    val totalModules: Int = 4,
    val isFullyPaid: Boolean = false,
    val lastPaymentDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "history", primaryKeys = ["userId", "productId"])
data class HistoryItem(val userId: String, val productId: String, val viewedAt: Long = System.currentTimeMillis())

@Entity(tableName = "reviews")
data class ReviewLocal(
    @PrimaryKey(autoGenerate = true) val reviewId: Int = 0,
    val productId: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String? = null,
    val comment: String,
    val rating: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val parentReviewId: Int? = null,
    val likes: Int = 0,
    val dislikes: Int = 0
)

@Entity(tableName = "review_interactions", primaryKeys = ["reviewId", "userId"])
data class ReviewInteraction(
    val reviewId: Int,
    val userId: String,
    val userName: String,
    val interactionType: String
)

@Entity(tableName = "wallet_history")
data class WalletTransaction(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // "TOP_UP" or "PURCHASE"
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String,
    val description: String,
    val orderReference: String? = null,
    val productId: String? = null,
    val purchaseId: String? = null
)

@Entity(tableName = "course_enrollment_details")
data class CourseEnrollmentDetails(
    @PrimaryKey val id: String, // userId_courseId
    val userId: String,
    val courseId: String,
    val lastQualification: String,
    val institution: String,
    val graduationYear: String,
    val englishProficiencyLevel: String,
    val dateOfBirth: String,
    val nationality: String,
    val gender: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val motivationalText: String,
    val cvFileName: String? = null,
    val portfolioUrl: String? = null,
    val specialSupportRequirements: String? = null,
    val status: String = "PENDING_REVIEW", 
    val submittedAt: Long = System.currentTimeMillis()
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users_local WHERE id = :id")
    fun getUserFlow(id: String): Flow<UserLocal?>

    @Query("SELECT * FROM users_local WHERE id = :id")
    suspend fun getUserById(id: String): UserLocal?

    @Query("SELECT * FROM users_local ORDER BY name ASC")
    fun getAllUsersFlow(): Flow<List<UserLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserLocal)

    @Query("DELETE FROM users_local WHERE id = :userId")
    suspend fun deleteUserOnly(userId: String)

    @Transaction
    suspend fun deleteFullUserAccount(userId: String) {
        deleteWishlistForUser(userId)
        deletePurchasesForUser(userId)
        deleteNotificationsForUser(userId)
        deleteSearchHistoryForUser(userId)
        deleteCourseInstallmentsForUser(userId)
        deleteHistoryForUser(userId)
        deleteReviewsForUser(userId)
        deleteReviewInteractionsForUser(userId)
        deleteWalletHistoryForUser(userId)
        deleteEnrollmentsForUser(userId)
        deleteUserOnly(userId)
    }

    @Query("DELETE FROM wishlist WHERE userId = :userId")
    suspend fun deleteWishlistForUser(userId: String)

    @Query("DELETE FROM purchases WHERE userId = :userId")
    suspend fun deletePurchasesForUser(userId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteNotificationsForUser(userId: String)

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun deleteSearchHistoryForUser(userId: String)

    @Query("DELETE FROM course_installments WHERE userId = :userId")
    suspend fun deleteCourseInstallmentsForUser(userId: String)

    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun deleteHistoryForUser(userId: String)

    @Query("DELETE FROM reviews WHERE userId = :userId")
    suspend fun deleteReviewsForUser(userId: String)

    @Query("DELETE FROM review_interactions WHERE userId = :userId")
    suspend fun deleteReviewInteractionsForUser(userId: String)

    @Query("DELETE FROM wallet_history WHERE userId = :userId")
    suspend fun deleteWalletHistoryForUser(userId: String)

    @Query("DELETE FROM course_enrollment_details WHERE userId = :userId")
    suspend fun deleteEnrollmentsForUser(userId: String)

    @Query("SELECT productId FROM wishlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWishlistIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM wishlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWishlistItems(userId: String): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(item: WishlistItem)

    @Query("DELETE FROM wishlist WHERE userId = :userId AND productId = :productId")
    suspend fun removeFromWishlist(userId: String, productId: String)

    @Query("SELECT productId FROM purchases WHERE userId = :userId ORDER BY purchasedAt DESC")
    fun getPurchaseIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM purchases WHERE userId = :userId ORDER BY purchasedAt DESC")
    fun getAllPurchasesFlow(userId: String): Flow<List<PurchaseItem>>

    @Query("SELECT * FROM purchases WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getPurchaseRecord(userId: String, productId: String): PurchaseItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPurchase(item: PurchaseItem)

    @Query("DELETE FROM purchases WHERE userId = :userId AND productId = :productId")
    suspend fun deletePurchase(userId: String, productId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotification(notification: NotificationLocal)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationLocal>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearNotifications(userId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId AND productId = :productId")
    suspend fun deleteNotificationByProduct(userId: String, productId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSearchQuery(item: SearchHistoryItem)

    @Query("SELECT `query` FROM search_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(userId: String): Flow<List<String>>

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearSearchHistory(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourseInstallment(installment: CourseInstallment)

    @Query("SELECT * FROM course_installments WHERE userId = :userId AND courseId = :courseId")
    suspend fun getCourseInstallment(userId: String, courseId: String): CourseInstallment?

    @Query("SELECT * FROM course_installments WHERE userId = :userId")
    fun getCourseInstallmentsForUser(userId: String): Flow<List<CourseInstallment>>

    @Query("SELECT productId FROM history WHERE userId = :userId ORDER BY viewedAt DESC LIMIT 5")
    fun getHistoryIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToHistory(item: HistoryItem)

    @Query("SELECT DISTINCT productId FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCommentedProductIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getReviewsForUser(userId: String): Flow<List<ReviewLocal>>

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY timestamp ASC")
    fun getReviewsForProduct(productId: String): Flow<List<ReviewLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReview(review: ReviewLocal)

    @Query("DELETE FROM reviews WHERE reviewId = :reviewId OR parentReviewId = :reviewId")
    suspend fun deleteReview(reviewId: Int)

    @Query("UPDATE reviews SET userPhotoUrl = :newPhotoUrl WHERE userId = :userId")
    suspend fun updateReviewAvatars(userId: String, newPhotoUrl: String?)

    @Query("UPDATE reviews SET likes = likes + 1 WHERE reviewId = :reviewId")
    suspend fun incrementLikes(reviewId: Int)

    @Query("UPDATE reviews SET dislikes = dislikes + 1 WHERE reviewId = :reviewId")
    suspend fun incrementDislikes(reviewId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: ReviewInteraction)

    @Query("DELETE FROM review_interactions WHERE reviewId = :reviewId AND userId = :userId")
    suspend fun deleteInteraction(reviewId: Int, userId: String)

    @Query("SELECT * FROM review_interactions WHERE reviewId = :reviewId AND userId = :userId")
    suspend fun getInteraction(reviewId: Int, userId: String): ReviewInteraction?

    @Query("SELECT * FROM review_interactions WHERE reviewId = :reviewId")
    fun getInteractionsForReview(reviewId: Int): Flow<List<ReviewInteraction>>

    @Transaction
    suspend fun toggleInteraction(reviewId: Int, userId: String, userName: String, type: String) {
        val existing = getInteraction(reviewId, userId)
        if (existing != null) {
            if (existing.interactionType == "LIKE") decrementLikes(reviewId) else decrementDislikes(reviewId)
            deleteInteraction(reviewId, userId)
            if (existing.interactionType == type) return
        }
        if (type == "LIKE") incrementLikes(reviewId) else incrementDislikes(reviewId)
        insertInteraction(ReviewInteraction(reviewId, userId, userName, type))
    }

    @Query("UPDATE reviews SET likes = CASE WHEN likes > 0 THEN likes - 1 ELSE 0 END WHERE reviewId = :reviewId")
    suspend fun decrementLikes(reviewId: Int)

    @Query("UPDATE reviews SET dislikes = CASE WHEN dislikes > 0 THEN dislikes - 1 ELSE 0 END WHERE reviewId = :reviewId")
    suspend fun decrementDislikes(reviewId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addInvoice(invoice: Invoice)

    @Query("SELECT * FROM invoices WHERE userId = :userId ORDER BY purchasedAt DESC")
    fun getInvoicesForUser(userId: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber")
    suspend fun getInvoiceByNumber(invoiceNumber: String): Invoice?

    /**
     * Consolidate Lookup Logic:
     * We try to match by reference first if provided, otherwise fallback to the most recent record.
     */
    @Query("SELECT * FROM invoices WHERE userId = :userId AND (orderReference = :orderRef OR :orderRef IS NULL) AND productId = :productId ORDER BY purchasedAt DESC LIMIT 1")
    suspend fun getInvoiceRecord(userId: String, productId: String, orderRef: String?): Invoice?

    @Query("SELECT * FROM invoices WHERE productId = :productId AND userId = :userId ORDER BY purchasedAt DESC LIMIT 1")
    suspend fun getInvoiceForProduct(userId: String, productId: String): Invoice?

    @Query("SELECT * FROM invoices WHERE userId = :userId AND orderReference = :orderRef LIMIT 1")
    suspend fun getInvoiceByReference(userId: String, orderRef: String): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWalletTransaction(transaction: WalletTransaction)

    @Query("SELECT * FROM wallet_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getWalletHistory(userId: String): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEnrollmentDetails(details: CourseEnrollmentDetails)

    @Query("SELECT * FROM course_enrollment_details WHERE userId = :userId AND courseId = :courseId LIMIT 1")
    suspend fun getEnrollmentDetails(userId: String, courseId: String): CourseEnrollmentDetails?

    @Query("SELECT * FROM course_enrollment_details WHERE userId = :userId AND courseId = :courseId LIMIT 1")
    fun getEnrollmentDetailsFlow(userId: String, courseId: String): Flow<CourseEnrollmentDetails?>

    @Query("SELECT * FROM course_enrollment_details ORDER BY submittedAt DESC")
    fun getAllEnrollmentsFlow(): Flow<List<CourseEnrollmentDetails>>

    @Query("SELECT * FROM course_enrollment_details WHERE userId = :userId")
    fun getEnrollmentsForUserFlow(userId: String): Flow<List<CourseEnrollmentDetails>>

    @Query("UPDATE course_enrollment_details SET status = :status WHERE id = :id")
    suspend fun updateEnrollmentStatus(id: String, status: String)
}
