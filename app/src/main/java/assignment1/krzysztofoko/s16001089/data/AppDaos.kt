package assignment1.krzysztofoko.s16001089.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * AppDaos.kt
 *
 * This file serves as the core persistence layer definition for the Room database.
 * It contains all Entity definitions (tables) and Data Access Objects (DAOs) required
 * to manage local data across the application's different modules (Staff, Student, Tutor).
 *
 * Key Areas Covered:
 * 1. Inventory Management (Books, Audiobooks, Courses, Gear)
 * 2. User & Personalisation (Local User Profiles, Themes)
 * 3. Commerce & Finance (Purchases, Invoices, Wallet Transactions)
 * 4. Academic Workflows (Enrolments, Assignments, History)
 * 5. Social & Feedback (Reviews, Interactions)
 */

const val LOCAL_USER_ID = "local_student_001"

/**
 * Entity representing a link between a Tutor and a Course.
 */
@Entity(tableName = "assigned_courses", primaryKeys = ["tutorId", "courseId"])
data class AssignedCourse(
    val tutorId: String,
    val courseId: String,
    val assignedAt: Long = System.currentTimeMillis()
)

/**
 * DAO for managing tutor assignments.
 */
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

/**
 * DAO for managing the local academic book catalogue.
 */
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

/**
 * DAO for managing audio-based learning materials.
 */
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

/**
 * DAO for managing university courses and curriculum metadata.
 */
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

/**
 * DAO for managing official university merchandise and gear.
 * Includes inventory control logic.
 */
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

    /**
     * Atomically reduces stock count for an item, ensuring it doesn't drop below zero.
     */
    @Query("UPDATE gear SET stockCount = CASE WHEN stockCount >= :quantity THEN stockCount - :quantity ELSE 0 END WHERE id = :id")
    suspend fun reduceStock(id: String, quantity: Int)

    @Query("DELETE FROM gear WHERE id = :id")
    suspend fun deleteGear(id: String)

    @Query("DELETE FROM gear")
    suspend fun deleteAll()
}

/**
 * Local representation of a university member.
 * Stores role-based permissions and financial balance.
 */
@Entity(tableName = "users_local")
data class UserLocal(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val title: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val selectedPaymentMethod: String? = null,
    val balance: Double = 0.0,
    val role: String = "user",
    val discountPercent: Double = 0.0
)

/**
 * Entity for storing complex UI theme preferences and custom colour definitions.
 */
@Entity(tableName = "user_themes")
data class UserTheme(
    @PrimaryKey val userId: String,
    val isCustomThemeEnabled: Boolean = false,
    val lastSelectedTheme: String = "DARK",
    val customPrimary: Long? = null,
    val customOnPrimary: Long? = null,
    val customPrimaryContainer: Long? = null,
    val customOnPrimaryContainer: Long? = null,
    val customSecondary: Long? = null,
    val customOnSecondary: Long? = null,
    val customSecondaryContainer: Long? = null,
    val customOnSecondaryContainer: Long? = null,
    val customTertiary: Long? = null,
    val customOnTertiary: Long? = null,
    val customTertiaryContainer: Long? = null,
    val customOnTertiaryContainer: Long? = null,
    val customBackground: Long? = null,
    val customOnBackground: Long? = null,
    val customSurface: Long? = null,
    val customOnSurface: Long? = null,
    val customIsDark: Boolean = true
)

/**
 * DAO for managing user interface customisations.
 */
@Dao
interface UserThemeDao {
    @Query("SELECT * FROM user_themes WHERE userId = :userId")
    fun getThemeFlow(userId: String): Flow<UserTheme?>

    @Query("SELECT * FROM user_themes WHERE userId = :userId")
    suspend fun getThemeById(userId: String): UserTheme?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTheme(theme: UserTheme)

    @Query("DELETE FROM user_themes WHERE userId = :userId")
    suspend fun deleteTheme(userId: String)
}

/**
 * Local record for wishlist tracking.
 */
@Entity(tableName = "wishlist", primaryKeys = ["userId", "productId"])
data class WishlistItem(val userId: String, val productId: String, val addedAt: Long = System.currentTimeMillis())

/**
 * Audit record for a successful user purchase.
 */
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

/**
 * Formal financial record generated after a commerce transaction.
 */
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

/**
 * Entity for tracking in-app alerts and notifications.
 */
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

/**
 * Recent search query tracking for the user search bar.
 */
@Entity(tableName = "search_history")
data class SearchHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Tracks modular payment progress for university courses.
 */
@Entity(tableName = "course_installments", primaryKeys = ["userId", "courseId"])
data class CourseInstallment(
    val userId: String,
    val courseId: String,
    val modulesPaid: Int = 1,
    val totalModules: Int = 4,
    val isFullyPaid: Boolean = false,
    val lastPaymentDate: Long = System.currentTimeMillis()
)

/**
 * Tracking for recently viewed products to power recommendation engines.
 */
@Entity(tableName = "history", primaryKeys = ["userId", "productId"])
data class HistoryItem(val userId: String, val productId: String, val viewedAt: Long = System.currentTimeMillis())

/**
 * Local storage for product reviews and feedback.
 */
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

/**
 * Tracks unique interactions (Likes/Dislikes) to prevent duplicate votes.
 */
@Entity(tableName = "review_interactions", primaryKeys = ["reviewId", "userId"])
data class ReviewInteraction(
    val reviewId: Int,
    val userId: String,
    val userName: String,
    val interactionType: String
)

/**
 * Ledger for the user's digital wallet.
 */
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

/**
 * Extensive data structure for course enrolment applications and review metadata.
 */
@Entity(tableName = "course_enrollment_details")
data class CourseEnrollmentDetails(
    @PrimaryKey val id: String, // userId_courseId
    val userId: String,
    val courseId: String,
    val requestedCourseId: String? = null,
    val isWithdrawal: Boolean = false,
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

/**
 * Log entry for academic enrolment changes (audit trail).
 */
@Entity(tableName = "enrollment_history")
data class EnrollmentHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val courseId: String,
    val status: String, // "WITHDRAWN", "CHANGED"
    val timestamp: Long = System.currentTimeMillis(),
    val previousCourseId: String? = null
)

/**
 * Configuration for global role-based pricing perks.
 */
@Entity(tableName = "global_discounts")
data class RoleDiscount(
    @PrimaryKey val role: String, // "admin", "student", "teacher", "user"
    val discountPercent: Double
)

/**
 * Comprehensive DAO managing user profiles, commerce, social features, and academic status.
 * This is the primary interface for user-centric data operations.
 */
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

    /**
     * Atomically removes all data associated with a specific user (GDPR compliance).
     */
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
        deleteEnrollmentHistoryForUser(userId)
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

    @Query("DELETE FROM enrollment_history WHERE userId = :userId")
    suspend fun deleteEnrollmentHistoryForUser(userId: String)

    @Query("DELETE FROM course_enrollment_details WHERE id = :id")
    suspend fun deleteEnrollmentById(id: String)

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

    /**
     * Logic for toggling a user's reaction to a review.
     * Prevents multiple reactions and manages like/dislike counts automatically.
     */
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

    @Query("UPDATE course_enrollment_details SET status = :status, requestedCourseId = :requestedId, isWithdrawal = 0 WHERE id = :id")
    suspend fun requestCourseChange(id: String, status: String, requestedId: String)

    @Query("UPDATE course_enrollment_details SET status = :status, isWithdrawal = 1, requestedCourseId = NULL WHERE id = :id")
    suspend fun requestWithdrawal(id: String, status: String)

    @Query("SELECT * FROM course_enrollment_details WHERE id = :id")
    suspend fun getEnrollmentById(id: String): CourseEnrollmentDetails?

    @Query("UPDATE course_enrollment_details SET courseId = :newCourseId, requestedCourseId = NULL, status = :status WHERE id = :id")
    suspend fun updateEnrollmentAfterChange(id: String, newCourseId: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEnrollmentHistory(history: EnrollmentHistory)

    @Query("SELECT * FROM enrollment_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getEnrollmentHistoryForUser(userId: String): Flow<List<EnrollmentHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoleDiscount(discount: RoleDiscount)

    @Query("SELECT * FROM global_discounts WHERE role = :role")
    suspend fun getRoleDiscount(role: String): RoleDiscount?

    @Query("SELECT * FROM global_discounts")
    fun getAllRoleDiscounts(): Flow<List<RoleDiscount>>
}
