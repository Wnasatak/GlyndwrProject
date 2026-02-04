package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class LiveChatMessage(val sender: String, val text: String, val isTeacher: Boolean = false)

class ClassroomViewModel(
    private val classroomDao: ClassroomDao,
    private val courseDao: CourseDao,
    private val courseId: String,
    private val userId: String
) : ViewModel() {

    val course: StateFlow<Course?> = flow { emit(courseDao.getCourseById(courseId)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val modules: StateFlow<List<ModuleContent>> = classroomDao.getModulesForCourse(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine course-level assignments and module-level assignments
    val assignments: StateFlow<List<Assignment>> = combine(
        classroomDao.getAssignmentsForCourse(courseId),
        classroomDao.getAllAssignments(),
        modules
    ) { courseAssignments, allAssignments, currentModules ->
        val moduleIds = currentModules.map { it.id }.toSet()
        val combined = (courseAssignments + allAssignments.filter { it.moduleId in moduleIds }).distinctBy { it.id }
        combined.sortedBy { it.dueDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grades: StateFlow<List<Grade>> = classroomDao.getGradesForCourse(userId, courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // More resilient observation: Get all active sessions and filter in Kotlin
    val activeSession: StateFlow<LiveSession?> = classroomDao.getAllActiveSessions()
        .map { sessions ->
            sessions.find { it.courseId.trim().equals(courseId.trim(), ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _broadcastMessage = MutableStateFlow<String?>(null)
    val broadcastMessage = _broadcastMessage.asStateFlow()

    private val _liveChatMessages = MutableStateFlow<List<LiveChatMessage>>(emptyList())
    val liveChatMessages = _liveChatMessages.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _isLiveViewActive = MutableStateFlow(false)
    val isLiveViewActive = _isLiveViewActive.asStateFlow()

    private val _selectedAssignment = MutableStateFlow<Assignment?>(null)
    val selectedAssignment = _selectedAssignment.asStateFlow()

    init {
        // Observe the active session and simulate broadcasting
        viewModelScope.launch {
            activeSession.collect { session ->
                if (session != null && session.isActive) {
                    if (_liveChatMessages.value.isEmpty()) {
                        _liveChatMessages.value = listOf(LiveChatMessage("System", "Joined the live session with ${session.tutorName}."))
                    }
                    
                    // Simulated teacher messages specifically for this session
                    _broadcastMessage.value = "Tutor ${session.tutorName} is now live! Join to start learning."
                } else {
                    _broadcastMessage.value = null
                    _liveChatMessages.value = emptyList()
                    _isLiveViewActive.value = false
                }
            }
        }
    }

    fun selectAssignment(assignment: Assignment?) {
        _selectedAssignment.value = assignment
    }

    fun enterLiveSession() {
        _isLiveViewActive.value = true
    }

    fun leaveLiveSession() {
        _isLiveViewActive.value = false
    }

    fun sendLiveChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _liveChatMessages.value = _liveChatMessages.value + LiveChatMessage("Me (Student)", text)
        }
    }

    fun dismissBroadcast() {
        _broadcastMessage.value = null
    }

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

            val assignmentToUpdate = assignments.value.find { it.id == assignmentId }
            if (assignmentToUpdate != null) {
                classroomDao.updateAssignment(assignmentToUpdate.copy(status = "SUBMITTED"))
            }
            
            _selectedAssignment.value = null
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
        return ClassroomViewModel(db.classroomDao(), db.courseDao(), courseId, userId) as T
    }
}
