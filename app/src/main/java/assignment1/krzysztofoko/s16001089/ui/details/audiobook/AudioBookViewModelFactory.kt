package assignment1.krzysztofoko.s16001089.ui.details.audiobook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AudioBookDao
import assignment1.krzysztofoko.s16001089.data.BookDao
import assignment1.krzysztofoko.s16001089.data.UserDao

class AudioBookViewModelFactory(
    private val bookDao: BookDao,
    private val audioBookDao: AudioBookDao,
    private val userDao: UserDao,
    private val bookId: String,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioBookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioBookViewModel(bookDao, audioBookDao, userDao, bookId, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
