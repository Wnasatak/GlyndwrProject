/**
 * BookRepository.kt
 *
 * This file contains the BookRepository class, which acts as the main data hub for the application.
 * It abstracts the underlying Room database complexity, providing a unified stream of product-like data.
 */

package assignment1.krzysztofoko.s16001089.data

import kotlinx.coroutines.flow.*

/**
 * A high-performance repository for all product-like data (Books, Gear, Courses, AudioBooks).
 * Optimized to provide a single, unified stream of data from multiple database tables.
 */
class BookRepository(private val db: AppDatabase) {

    /**
     * Returns a live flow of all combined products across various entities.
     * This acts as the "Source of Truth" for the entire application, mapping specialized
     * entities (like Gear or Courses) into a common 'Book' object for UI consistency.
     *
     * @param userId If provided, also retrieves and attaches purchase confirmation details.
     */
    fun getAllCombinedData(userId: String = ""): Flow<List<Book>> {
        // Retrieve individual flows from the respective DAOs
        val booksFlow = db.bookDao().getAllBooks() // Regular books
        val gearFlow = db.gearDao().getAllGear()   // Merchandise/Gear
        val coursesFlow = db.courseDao().getAllCourses() // Educational courses
        val audioBooksFlow = db.audioBookDao().getAllAudioBooks() // Digital audiobooks
        
        // Conditional flow based on whether a user is logged in
        val purchasesFlow = if (userId.isNotEmpty()) {
            db.userDao().getAllPurchasesFlow(userId) // User-specific purchase history
        } else flowOf(emptyList<PurchaseItem>()) // Empty flow if no user context

        // Combine all five data streams into one unified list of Books
        return combine(booksFlow, gearFlow, coursesFlow, audioBooksFlow, purchasesFlow) { books, gear, courses, audioBooks, purchaseRecords ->
            // Convert specialized entities to the common 'Book' model, attaching purchase confirmations
            val gearAsBooks = gear.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
            val coursesAsBooks = courses.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
            val audioBooksAsBooks = audioBooks.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
            
            // Map regular books, also attaching order confirmations if they've been purchased
            val regularBooks = books.map { b ->
                b.withOrderConf(purchaseRecords.find { it.productId == b.id }?.orderConfirmation)
            }
            
            // Return the flattened, combined list of all items
            regularBooks + gearAsBooks + coursesAsBooks + audioBooksAsBooks
        }
    }

    /**
     * High-speed lookup for a single item by its unique identifier.
     * Extracts the first emitted list from the combined flow and finds the matching item.
     *
     * @param id The unique ID of the item to find.
     * @param userId Context for checking if the item has been purchased by the current user.
     */
    suspend fun getItemById(id: String, userId: String = ""): Book? {
        return getAllCombinedData(userId).firstOrNull()?.find { it.id == id }
    }
}
