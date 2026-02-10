package assignment1.krzysztofoko.s16001089.data

import kotlinx.coroutines.flow.*

/**
 * A high-performance repository for all product-like data.
 * Optimized to provide a single, unified stream of data from multiple tables.
 */
class BookRepository(private val db: AppDatabase) {

    /**
     * Returns a live flow of all combined products.
     * This is the "Source of Truth" for the entire application.
     */
    fun getAllCombinedData(userId: String = ""): Flow<List<Book>> {
        val booksFlow = db.bookDao().getAllBooks()
        val gearFlow = db.gearDao().getAllGear()
        val coursesFlow = db.courseDao().getAllCourses()
        val audioBooksFlow = db.audioBookDao().getAllAudioBooks()
        
        val purchasesFlow = if (userId.isNotEmpty()) {
            db.userDao().getAllPurchasesFlow(userId)
        } else flowOf(emptyList<PurchaseItem>())

        return combine(booksFlow, gearFlow, coursesFlow, audioBooksFlow, purchasesFlow) { books, gear, courses, audioBooks, purchaseRecords ->
            val gearAsBooks = gear.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
            val coursesAsBooks = courses.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
            val audioBooksAsBooks = audioBooks.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
            
            val regularBooks = books.map { b ->
                b.withOrderConf(purchaseRecords.find { it.productId == b.id }?.orderConfirmation)
            }
            
            regularBooks + gearAsBooks + coursesAsBooks + audioBooksAsBooks
        }
    }

    /**
     * High-speed lookup for a single item.
     */
    suspend fun getItemById(id: String, userId: String = ""): Book? {
        return getAllCombinedData(userId).firstOrNull()?.find { it.id == id }
    }
}
