package assignment1.krzysztofoko.s16001089.ui.details.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.CourseDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * Factory to create CourseDetailViewModel with custom parameters.
 */
class CourseDetailViewModelFactory(
    private val courseDao: CourseDao,
    private val userDao: UserDao,
    private val courseId: String,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CourseDetailViewModel(courseDao, userDao, courseId, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
