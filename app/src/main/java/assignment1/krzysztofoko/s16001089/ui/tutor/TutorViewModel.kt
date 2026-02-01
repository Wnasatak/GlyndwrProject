package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TutorSection { DASHBOARD, MY_COURSES, STUDENTS, MESSAGES }

/**
 * ViewModel for the Tutor Dashboard.
 */
class TutorViewModel(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val classroomDao: ClassroomDao,
    private val auditDao: AuditDao,
    private val tutorId: String
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- Navigation State ---
    private val _currentSection = MutableStateFlow(TutorSection.DASHBOARD)
    val currentSection: StateFlow<TutorSection> = _currentSection.asStateFlow()

    fun setSection(section: TutorSection) {
        _currentSection.value = section
    }

    // --- Data Streams ---
    
    // For now, fetching all courses, but in a real app, we'd filter by tutorId
    val tutorCourses: StateFlow<List<Course>> = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<UserLocal>> = userDao.getAllUsersFlow()
        .map { users -> users.filter { it.role == "student" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApplications: StateFlow<Int> = userDao.getAllEnrollmentsFlow()
        .map { it.filter { app -> app.status == "PENDING_REVIEW" }.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun addLog(action: String, targetId: String, details: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            auditDao.insertLog(SystemLog(
                userId = user?.uid ?: "unknown",
                userName = user?.displayName ?: "Tutor",
                action = action,
                targetId = targetId,
                details = details,
                logType = "TUTOR"
            ))
        }
    }

    // --- Placeholder Actions for future functions ---
    fun updateCourseContent(courseId: String, content: String) {
        // Implementation for later
        addLog("UPDATE_CONTENT", courseId, "Updated course content")
    }
}

class TutorViewModelFactory(
    private val db: AppDatabase,
    private val tutorId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TutorViewModel(
                db.userDao(),
                db.courseDao(),
                db.classroomDao(),
                db.auditDao(),
                tutorId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
