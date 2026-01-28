package assignment1.krzysztofoko.s16001089.ui.details.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.BookDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * Factory class for creating instances of [BookDetailViewModel].
 * 
 * Since ViewModels cannot have constructor parameters by default when using the 
 * standard Lifecycle system, this factory is used to inject the required 
 * Data Access Objects (DAOs) and specific item identifiers (bookId, userId) 
 * into the ViewModel at runtime.
 */
class BookDetailViewModelFactory(
    private val bookDao: BookDao, // DAO for accessing core book product data
    private val userDao: UserDao, // DAO for accessing user-specific data (wishlist, purchases)
    private val bookId: String,   // The unique identifier for the book being displayed
    private val userId: String    // The identifier for the current authenticated user session
) : ViewModelProvider.Factory {

    /**
     * Instantiates the [BookDetailViewModel] if the requested class type matches.
     * 
     * @param modelClass The type of ViewModel to create.
     * @return A fresh instance of the requested ViewModel with all dependencies injected.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Manually passing dependencies into the ViewModel constructor
            return BookDetailViewModel(bookDao, userDao, bookId, userId) as T
        }
        // Safety check for incorrect factory usage
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
