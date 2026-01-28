package assignment1.krzysztofoko.s16001089.ui.details.audiobook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AudioBookDao
import assignment1.krzysztofoko.s16001089.data.BookDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * Factory class for creating instances of [AudioBookViewModel].
 * 
 * ViewModels with constructor parameters require a custom factory to be instantiated
 * by the Android Lifecycle system. This factory handles the injection of multiple DAOs
 * and specific identifiers required for the audiobook details logic.
 */
class AudioBookViewModelFactory(
    private val bookDao: BookDao,           // DAO for general product information
    private val audioBookDao: AudioBookDao, // DAO specific to audiobook-only data
    private val userDao: UserDao,           // DAO for user session data (purchases, wishlist)
    private val bookId: String,             // Unique ID of the audiobook being loaded
    private val userId: String              // ID of the current authenticated user
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the requested ViewModel class.
     * 
     * @param modelClass The class of the ViewModel to create.
     * @return A constructed instance of [AudioBookViewModel].
     * @throws IllegalArgumentException if the requested class is not [AudioBookViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel matches our specific target class
        if (modelClass.isAssignableFrom(AudioBookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Manually inject all dependencies into the constructor
            return AudioBookViewModel(bookDao, audioBookDao, userDao, bookId, userId) as T
        }
        // Fallback for safety in case of incorrect factory usage
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
