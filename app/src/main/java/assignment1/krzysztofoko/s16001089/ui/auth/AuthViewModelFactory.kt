package assignment1.krzysztofoko.s16001089.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AppDatabase

/**
 * Factory for creating `AuthViewModel` instances.
 *
 * This factory is responsible for providing an `AuthViewModel` with its necessary
 * dependencies, in this case, the `AppDatabase`. This approach allows for
 * dependency injection into the ViewModel, making it more testable and modular.
 *
 * @property db The application's Room database instance.
 */
class AuthViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given `ViewModel` class.
     *
     * @param T The type of the ViewModel.
     * @param modelClass A `Class` whose instance is requested.
     * @return A newly created `ViewModel`.
     * @throws IllegalArgumentException if `modelClass` is not a subclass of `AuthViewModel`.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel is of type AuthViewModel.
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // If it is, create and return an instance of AuthViewModel, passing the database.
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(db) as T
        }
        // If the requested ViewModel is of another type, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
