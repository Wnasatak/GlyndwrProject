package assignment1.krzysztofoko.s16001089.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AppDatabase

/**
 * Factory to create AuthViewModel with the required AppDatabase dependency.
 */
class AuthViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
