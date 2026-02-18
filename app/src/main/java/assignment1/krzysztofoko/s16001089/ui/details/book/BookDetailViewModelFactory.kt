package assignment1.krzysztofoko.s16001089.ui.details.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AuditDao
import assignment1.krzysztofoko.s16001089.data.BookDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * BookDetailViewModelFactory.kt
 *
 * This factory class is responsible for the instantiation of the [BookDetailViewModel].
 * Since the ViewModel requires specific dependencies (DAOs and IDs) passed through its 
 * constructor, we must provide this custom factory to the Android Lifecycle system 
 * to handle the manual injection.
 */
class BookDetailViewModelFactory(
    private val bookDao: BookDao,   // DAO for accessing general book and product data.
    private val userDao: UserDao,   // DAO for user-centric data like wishlists and purchases.
    private val auditDao: AuditDao, // DAO for recording user actions in the system audit log.
    private val bookId: String,     // The unique identifier for the book being viewed.
    private val userId: String      // The unique identifier for the currently logged-in user.
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the requested ViewModel class.
     * 
     * This method is invoked by the ViewModelProvider and acts as the entry point for 
     * constructing our complex ViewModel with all its required parameters.
     * 
     * @param T The type of the ViewModel.
     * @param modelClass The class of the ViewModel to be created.
     * @return A newly created [BookDetailViewModel] cast to the requested type [T].
     * @throws IllegalArgumentException if the provided [modelClass] is not [BookDetailViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested class matches our specific ViewModel type.
        if (modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {
            // Suppress the unchecked cast as we've just confirmed the target class type.
            @Suppress("UNCHECKED_CAST")
            // Construct and return the ViewModel, injecting all DAOs and identifiers.
            return BookDetailViewModel(bookDao, userDao, auditDao, bookId, userId) as T
        }
        // If this factory is mistakenly asked to create a different class, throw a proper error.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
