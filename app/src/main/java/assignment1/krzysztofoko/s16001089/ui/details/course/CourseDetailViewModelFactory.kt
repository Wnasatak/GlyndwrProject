package assignment1.krzysztofoko.s16001089.ui.details.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.CourseDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * Factory class for creating instances of [CourseDetailViewModel].
 * 
 * In Android development, ViewModels cannot have constructor parameters by default 
 * when using the standard provider. This factory acts as a bridge, allowing us 
 * to manually inject the required Data Access Objects (DAOs) and unique 
 * identifiers (courseId, userId) into the ViewModel when the user opens 
 * a specific course detail page.
 */
class CourseDetailViewModelFactory(
    private val courseDao: CourseDao, // Injected DAO for academic course metadata
    private val userDao: UserDao,     // Injected DAO for user-specific data (enrollments, wishlist)
    private val courseId: String,     // The ID of the specific course being viewed
    private val userId: String        // The ID of the current authenticated student session
) : ViewModelProvider.Factory {

    /**
     * Instantiates the requested ViewModel type.
     * 
     * This method performs a type check to ensure it only creates [CourseDetailViewModel].
     * If the check passes, it calls the constructor with all the injected dependencies.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Validate that the requested class is indeed our CourseDetailViewModel
        if (modelClass.isAssignableFrom(CourseDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Manually construct the ViewModel with the parameters provided to the factory
            return CourseDetailViewModel(courseDao, userDao, courseId, userId) as T
        }
        // Safety exception if the factory is used for an incorrect ViewModel type
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
