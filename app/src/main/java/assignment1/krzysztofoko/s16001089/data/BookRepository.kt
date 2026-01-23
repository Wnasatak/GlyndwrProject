package assignment1.krzysztofoko.s16001089.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BookRepository(private val db: AppDatabase) {

    fun getAllCombinedData(): Flow<List<Book>?> {
        val booksFlow = db.bookDao().getAllBooks()
        val gearFlow = db.gearDao().getAllGear()
        val coursesFlow = db.courseDao().getAllCourses()
        val audioBooksFlow = db.audioBookDao().getAllAudioBooks()

        return combine(booksFlow, gearFlow, coursesFlow, audioBooksFlow) { books, gear, courses, audioBooks ->
            if (books.isEmpty() && gear.isEmpty() && courses.isEmpty() && audioBooks.isEmpty()) {
                null
            } else {
                val gearAsBooks = gear.map { g -> 
                    Book(id = g.id, title = g.title, price = g.price, description = g.description, 
                        imageUrl = g.imageUrl, category = g.category, mainCategory = "University Gear", 
                        author = "Wrexham University") 
                }
                val coursesAsBooks = courses.map { c -> 
                    Book(id = c.id, title = c.title, price = c.price, description = c.description, 
                        imageUrl = c.imageUrl, category = c.category, mainCategory = "University Courses", 
                        author = c.department, isInstallmentAvailable = c.isInstallmentAvailable, 
                        modulePrice = c.modulePrice) 
                }
                val audioBooksAsBooks = audioBooks.map { ab -> 
                    Book(id = ab.id, title = ab.title, price = ab.price, description = ab.description, 
                        imageUrl = ab.imageUrl, category = ab.category, mainCategory = "Audio Books", 
                        author = ab.author, isAudioBook = true, audioUrl = ab.audioUrl) 
                }
                books + gearAsBooks + coursesAsBooks + audioBooksAsBooks
            }
        }
    }
}
