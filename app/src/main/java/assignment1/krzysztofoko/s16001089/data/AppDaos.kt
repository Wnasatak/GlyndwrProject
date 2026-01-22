package assignment1.krzysztofoko.s16001089.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

const val LOCAL_USER_ID = "local_student_001"

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<Book>)

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): Book?

    @Query("DELETE FROM books")
    suspend fun deleteAll()
}

@Dao
interface AudioBookDao {
    @Query("SELECT * FROM audiobooks")
    fun getAllAudioBooks(): Flow<List<AudioBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audioBooks: List<AudioBook>)

    @Query("DELETE FROM audiobooks")
    suspend fun deleteAll()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}

@Dao
interface GearDao {
    @Query("SELECT * FROM gear")
    fun getAllGear(): Flow<List<Gear>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gear: List<Gear>)

    @Query("DELETE FROM gear")
    suspend fun deleteAll()
}

@Entity(tableName = "users_local")
data class UserLocal(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val address: String? = null,
    val selectedPaymentMethod: String? = null,
    val balance: Double = 0.0,
    val role: String = "student"
)

@Entity(tableName = "wishlist", primaryKeys = ["userId", "productId"])
data class WishlistItem(val userId: String, val productId: String, val addedAt: Long = System.currentTimeMillis())

@Entity(tableName = "purchases", primaryKeys = ["userId", "productId"])
data class PurchaseItem(
    val userId: String,
    val productId: String,
    val purchasedAt: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Unknown",
    val amountFromWallet: Double = 0.0,
    val amountPaidExternal: Double = 0.0
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
    val parentReviewId: Int? = null, // For threaded responses
    val likes: Int = 0,
    val dislikes: Int = 0
)

@Entity(tableName = "review_interactions", primaryKeys = ["reviewId", "userId"])
data class ReviewInteraction(
    val reviewId: Int,
    val userId: String,
    val userName: String,
    val interactionType: String // "LIKE" or "DISLIKE"
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users_local WHERE id = :id")
    fun getUserFlow(id: String): Flow<UserLocal?>

    @Query("SELECT * FROM users_local WHERE id = :id")
    suspend fun getUserById(id: String): UserLocal?

    @Query("SELECT * FROM users_local WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserLocal)

    @Query("DELETE FROM users_local WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("UPDATE reviews SET userId = :newId WHERE userId = :oldId")
    suspend fun migrateReviewOwner(oldId: String, newId: String)

    @Query("UPDATE purchases SET userId = :newId WHERE userId = :oldId")
    suspend fun migratePurchases(oldId: String, newId: String)

    @Query("UPDATE wishlist SET userId = :newId WHERE userId = :oldId")
    suspend fun migrateWishlist(oldId: String, newId: String)

    @Query("UPDATE history SET userId = :newId WHERE userId = :oldId")
    suspend fun migrateHistory(oldId: String, newId: String)

    @Query("UPDATE reviews SET userPhotoUrl = :newPhotoUrl WHERE userId = :userId")
    suspend fun updateReviewAvatars(userId: String, newPhotoUrl: String?)

    @Query("SELECT productId FROM wishlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWishlistIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(item: WishlistItem)

    @Query("DELETE FROM wishlist WHERE userId = :userId AND productId = :productId")
    suspend fun removeFromWishlist(userId: String, productId: String)

    @Query("SELECT productId FROM purchases WHERE userId = :userId ORDER BY purchasedAt DESC")
    fun getPurchaseIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM purchases WHERE userId = :userId AND productId = :productId")
    suspend fun getPurchaseRecord(userId: String, productId: String): PurchaseItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPurchase(item: PurchaseItem)

    @Query("DELETE FROM purchases WHERE userId = :userId AND productId = :productId")
    suspend fun deletePurchase(userId: String, productId: String)

    @Query("SELECT productId FROM history WHERE userId = :userId ORDER BY viewedAt DESC LIMIT 5")
    fun getHistoryIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToHistory(item: HistoryItem)

    @Query("SELECT DISTINCT productId FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCommentedProductIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY timestamp ASC")
    fun getReviewsForProduct(productId: String): Flow<List<ReviewLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReview(review: ReviewLocal)

    @Query("DELETE FROM reviews WHERE reviewId = :reviewId OR parentReviewId = :reviewId")
    suspend fun deleteReview(reviewId: Int)

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
            if (existing.interactionType == "LIKE") {
                decrementLikes(reviewId)
            } else {
                decrementDislikes(reviewId)
            }
            deleteInteraction(reviewId, userId)
            if (existing.interactionType == type) return
        }
        if (type == "LIKE") {
            incrementLikes(reviewId)
        } else {
            incrementDislikes(reviewId)
        }
        insertInteraction(ReviewInteraction(reviewId, userId, userName, type))
    }

    @Query("UPDATE reviews SET likes = CASE WHEN likes > 0 THEN likes - 1 ELSE 0 END WHERE reviewId = :reviewId")
    suspend fun decrementLikes(reviewId: Int)

    @Query("UPDATE reviews SET dislikes = CASE WHEN dislikes > 0 THEN dislikes - 1 ELSE 0 END WHERE reviewId = :reviewId")
    suspend fun decrementDislikes(reviewId: Int)
}
