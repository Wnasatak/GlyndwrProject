package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ClassroomViewModel(
    private val classroomDao: ClassroomDao,
    private val courseId: String,
    private val userId: String
) : ViewModel() {

    val modules: StateFlow<List<ModuleContent>> = classroomDao.getModulesForCourse(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<Assignment>> = classroomDao.getAssignmentsForCourse(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grades: StateFlow<List<Grade>> = classroomDao.getGradesForCourse(userId, courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<LiveSession?> = classroomDao.getActiveSession(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun submitAssignment(assignmentId: String, content: String) {
        viewModelScope.launch {
            val submission = AssignmentSubmission(
                id = UUID.randomUUID().toString(),
                assignmentId = assignmentId,
                userId = userId,
                content = content
            )
            classroomDao.insertSubmission(submission)
            // Ideally, update assignment status too
        }
    }
}

class ClassroomViewModelFactory(
    private val db: AppDatabase,
    private val courseId: String,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClassroomViewModel(db.classroomDao(), courseId, userId) as T
    }
}
