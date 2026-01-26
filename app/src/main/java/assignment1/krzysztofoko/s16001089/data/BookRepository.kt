package assignment1.krzysztofoko.s16001089.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first

class BookRepository(private val db: AppDatabase) {

    fun getAllCombinedData(userId: String = ""): Flow<List<Book>?> {
        val booksFlow = db.bookDao().getAllBooks()
        val gearFlow = db.gearDao().getAllGear()
        val coursesFlow = db.courseDao().getAllCourses()
        val audioBooksFlow = db.audioBookDao().getAllAudioBooks()
        
        val purchasesFlow = if (userId.isNotEmpty()) {
            db.userDao().getAllPurchasesFlow(userId)
        } else flowOf(emptyList<PurchaseItem>())

        return combine(booksFlow, gearFlow, coursesFlow, audioBooksFlow, purchasesFlow) { books, gear, courses, audioBooks, purchaseRecords ->
            if (books.isEmpty() && gear.isEmpty() && courses.isEmpty() && audioBooks.isEmpty()) {
                null
            } else {
                val gearAsBooks = gear.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
                val coursesAsBooks = courses.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
                val audioBooksAsBooks = audioBooks.map { it.toBook(purchaseRecords.find { p -> p.productId == it.id }?.orderConfirmation) }
                
                val regularBooks = books.map { b ->
                    b.withOrderConf(purchaseRecords.find { it.productId == b.id }?.orderConfirmation)
                }
                
                regularBooks + gearAsBooks + coursesAsBooks + audioBooksAsBooks
            }
        }
    }

    suspend fun getItemById(id: String, userId: String = ""): Book? {
        val books = getAllCombinedData(userId).first()
        return books?.find { it.id == id }
    }
}
