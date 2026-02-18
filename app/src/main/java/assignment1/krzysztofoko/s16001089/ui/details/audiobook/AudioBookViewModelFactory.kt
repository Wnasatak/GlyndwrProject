package assignment1.krzysztofoko.s16001089.ui.details.audiobook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AudioBookDao
import assignment1.krzysztofoko.s16001089.data.BookDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * AudioBookViewModelFactory.kt
 *
 * This factory class is responsible for the instantiation of the [AudioBookViewModel].
 * ViewModels that require non-empty constructors (dependencies like DAOs and IDs)
 * cannot be created by the default lifecycle provider and must use a custom factory
 * to manually inject these requirements.
 */
class AudioBookViewModelFactory(
    private val bookDao: BookDao,           // DAO for accessing the general books table.
    private val audioBookDao: AudioBookDao, // DAO for accessing specialised audiobook data.
    private val userDao: UserDao,           // DAO for user-centric data (wishlist, history, etc.).
    private val bookId: String,             // ID of the specific book being viewed.
    private val userId: String              // ID of the currently logged-in user.
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given ViewModel class.
     * 
     * This method acts as a bridge between the Android Lifecycle system and the 
     * [AudioBookViewModel]'s constructor, ensuring all necessary DAOs and IDs 
     * are correctly provided.
     * 
     * @param T The type of the ViewModel.
     * @param modelClass The class of the ViewModel to be created.
     * @return A newly created [AudioBookViewModel] cast to the requested type [T].
     * @throws IllegalArgumentException if the provided [modelClass] is not [AudioBookViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Confirm that the requested class is indeed our audiobook ViewModel.
        if (modelClass.isAssignableFrom(AudioBookViewModel::class.java)) {
            // Suppress the unchecked cast as we've just verified the class type.
            @Suppress("UNCHECKED_CAST")
            // Construct the ViewModel, manually injecting all its dependencies.
            return AudioBookViewModel(bookDao, audioBookDao, userDao, bookId, userId) as T
        }
        // If the factory is mistakenly used for another class, throw an informative exception.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
