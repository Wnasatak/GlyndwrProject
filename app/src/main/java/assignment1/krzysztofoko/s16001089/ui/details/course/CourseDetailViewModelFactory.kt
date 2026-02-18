package assignment1.krzysztofoko.s16001089.ui.details.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.CourseDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * CourseDetailViewModelFactory.kt
 *
 * This factory class is a crucial part of the Dependency Injection pattern within the app.
 * Its sole responsibility is the instantiation of the [CourseDetailViewModel].
 * 
 * Why do we need this?
 * Under the hood, the Android Lifecycle library uses a default 'no-argument' constructor 
 * to create ViewModels. However, our [CourseDetailViewModel] requires several essential 
 * dependencies (DAOs and unique identifiers) to function correctly. This custom factory 
 * intercepts the creation process and manually 'injects' these dependencies.
 *
 * @property courseDao The Data Access Object used to fetch specific academic course metadata.
 * @property userDao The Data Access Object providing access to user-specific records like enrolments and wishlists.
 * @property courseId The unique string identifier for the specific course the user is currently viewing.
 * @property userId The unique identifier for the currently authenticated student session.
 */
class CourseDetailViewModelFactory(
    private val courseDao: CourseDao,
    private val userDao: UserDao,
    private val courseId: String,
    private val userId: String
) : ViewModelProvider.Factory {

    /**
     * Instantiates the requested ViewModel type.
     * 
     * This method is triggered by the [ViewModelProvider] when a ViewModel is requested 
     * in a Composable. It serves as a gatekeeper, ensuring the requested class is of 
     * the correct type before constructing it with the required parameters.
     * 
     * @param T The type of the ViewModel, which must extend [ViewModel].
     * @param modelClass The [Class] of the ViewModel that the system is trying to create.
     * @return A fully constructed instance of [CourseDetailViewModel], cast to type [T].
     * @throws IllegalArgumentException if the system attempts to use this factory for a 
     *         ViewModel class other than [CourseDetailViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // First, we perform a type check to ensure the factory is being used correctly.
        if (modelClass.isAssignableFrom(CourseDetailViewModel::class.java)) {
            // We suppress the unchecked cast warning because the isAssignableFrom check 
            // guarantees that the instance is indeed a subclass of T.
            @Suppress("UNCHECKED_CAST")
            // Finally, we manually invoke the constructor, passing in all our injected DAOs and IDs.
            return CourseDetailViewModel(courseDao, userDao, courseId, userId) as T
        }
        
        // If the developer accidentally links this factory to the wrong screen, 
        // we throw a descriptive error to help them find the bug quickly.
        throw IllegalArgumentException("Error: CourseDetailViewModelFactory cannot instantiate ${modelClass.name}. It is strictly for CourseDetailViewModel.")
    }
}
