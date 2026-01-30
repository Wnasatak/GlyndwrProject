package assignment1.krzysztofoko.s16001089.ui.details.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AuditDao
import assignment1.krzysztofoko.s16001089.data.BookDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * Factory class for creating instances of [BookDetailViewModel].
 */
class BookDetailViewModelFactory(
    private val bookDao: BookDao, 
    private val userDao: UserDao, 
    private val auditDao: AuditDao,
    private val bookId: String,   
    private val userId: String    
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookDetailViewModel(bookDao, userDao, auditDao, bookId, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
