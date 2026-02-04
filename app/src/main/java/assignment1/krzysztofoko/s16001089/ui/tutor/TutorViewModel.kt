package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class TutorSection { 
    DASHBOARD, 
    MY_COURSES, 
    SELECTED_COURSE, 
    COURSE_MODULES,
    COURSE_STUDENTS,
    COURSE_ASSIGNMENTS,
    COURSE_GRADES,
    COURSE_LIVE,
    STUDENTS, 
    MESSAGES, 
    CHAT, 
    LIBRARY, 
    BOOKS, 
    AUDIOBOOKS, 
    READ_BOOK, 
    LISTEN_AUDIOBOOK, 
    TEACHER_DETAIL,
    CREATE_ASSIGNMENT,
    START_LIVE_STREAM
}

data class ConversationPreview(
    val student: UserLocal,
    val lastMessage: ClassroomMessage
)

/**
 * ViewModel for the Tutor Dashboard.
 */
class TutorViewModel(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val classroomDao: ClassroomDao,
    private val auditDao: AuditDao,
    private val bookDao: BookDao,
    private val audioBookDao: AudioBookDao,
    private val assignedCourseDao: AssignedCourseDao,
    val tutorId: String
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- Navigation State ---
    private val _currentSection = MutableStateFlow(TutorSection.DASHBOARD)
    val currentSection: StateFlow<TutorSection> = _currentSection.asStateFlow()

    private val _selectedStudent = MutableStateFlow<UserLocal?>(null)
    val selectedStudent: StateFlow<UserLocal?> = _selectedStudent.asStateFlow()

    private val _selectedCourseId = MutableStateFlow<String?>(null)
    val selectedCourseId: StateFlow<String?> = _selectedCourseId.asStateFlow()

    private val _selectedModuleId = MutableStateFlow<String?>(null)
    val selectedModuleId: StateFlow<String?> = _selectedModuleId.asStateFlow()

    private val _activeBook = MutableStateFlow<Book?>(null)
    val activeBook: StateFlow<Book?> = _activeBook.asStateFlow()

    private val _activeAudioBook = MutableStateFlow<AudioBook?>(null)
    val activeAudioBook: StateFlow<AudioBook?> = _activeAudioBook.asStateFlow()

    private val _isLive = MutableStateFlow(false)
    val isLive: StateFlow<Boolean> = _isLive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    // Attendance Date State
    private val _attendanceDate = MutableStateFlow(System.currentTimeMillis())
    val attendanceDate: StateFlow<Long> = _attendanceDate.asStateFlow()

    fun setAttendanceDate(date: Long) {
        _attendanceDate.value = date
    }

    fun setSection(section: TutorSection, student: UserLocal? = null) {
        _currentSection.value = section
        if (student != null) {
            _selectedStudent.value = student
        }
    }

    fun selectCourse(courseId: String) {
        _selectedCourseId.value = courseId
        _currentSection.value = TutorSection.SELECTED_COURSE
    }
    
    fun updateSelectedCourse(courseId: String?) {
        _selectedCourseId.value = courseId
    }

    fun selectModule(moduleId: String?) {
        _selectedModuleId.value = moduleId
    }

    fun openBook(book: Book) {
        _activeBook.value = book
        _currentSection.value = TutorSection.READ_BOOK
    }

    fun findCourseById(courseId: String): Course? {
        return allCourses.value.find { it.id == courseId }
    }

    fun openAudioBook(ab: AudioBook) {
        _activeAudioBook.value = ab
        _currentSection.value = TutorSection.LISTEN_AUDIOBOOK
    }

    // --- Data Streams ---
    
    val currentUserLocal: StateFlow<UserLocal?> = userDao.getUserFlow(tutorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tutorProfile: StateFlow<TutorProfile?> = classroomDao.getTutorProfileFlow(tutorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCourses: StateFlow<List<Course>> = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignedCourses: StateFlow<List<Course>> = combine(
        courseDao.getAllCourses(),
        assignedCourseDao.getAssignedCoursesForTutor(tutorId)
    ) { courses, assignments ->
        val assignedIds = assignments.map { it.courseId }.toSet()
        courses.filter { assignedIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourse: StateFlow<Course?> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<Course?>(null)
            else flow { emit(courseDao.getCourseById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val enrolledStudentsInSelectedCourse: StateFlow<List<UserLocal>> = combine(
        _selectedCourseId,
        userDao.getAllUsersFlow(),
        userDao.getAllEnrollmentsFlow()
    ) { courseId, users, enrollments ->
        if (courseId == null) emptyList()
        else {
            val studentIds = enrollments
                .filter { it.courseId == courseId && (it.status == "APPROVED" || it.status == "ENROLLED") }
                .map { it.userId }
                .toSet()
            users.filter { it.id in studentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseGrades: StateFlow<List<Grade>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else classroomDao.getAllGradesForCourse(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseAssignments: StateFlow<List<Assignment>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else classroomDao.getAssignmentsForCourse(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseModules: StateFlow<List<ModuleContent>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else classroomDao.getModulesForCourse(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allCourseAttendance: StateFlow<List<Attendance>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else classroomDao.getAllAttendanceForCourse(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseAttendance: StateFlow<List<Attendance>> = combine(_selectedCourseId, _attendanceDate) { id, date ->
        id to date
    }.flatMapLatest { (id, date) ->
        if (id == null) flowOf(emptyList())
        else {
            val cal = Calendar.getInstance()
            cal.timeInMillis = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            classroomDao.getAttendanceForCourseAndDate(id, cal.timeInMillis)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recordedAttendanceDates: StateFlow<List<Long>> = _selectedCourseId
        .flatMapLatest<String?, List<Long>> { id ->
            if (id == null) flowOf(emptyList())
            else classroomDao.getRecordedAttendanceDates(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserLocal>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEnrollments: StateFlow<List<CourseEnrollmentDetails>> = userDao.getAllEnrollmentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApplications: StateFlow<Int> = userDao.getAllEnrollmentsFlow()
        .map { it.filter { app -> app.status == "PENDING_REVIEW" }.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allBooks: StateFlow<List<Book>> = bookDao.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAudioBooks: StateFlow<List<AudioBook>> = audioBookDao.getAllAudioBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedIds: StateFlow<Set<String>> = userDao.getPurchaseIds(tutorId)
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val libraryBooks: StateFlow<List<Book>> = combine(allBooks, purchasedIds) { books, ids ->
        books.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val libraryAudioBooks: StateFlow<List<AudioBook>> = combine(allAudioBooks, purchasedIds) { books, ids ->
        books.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val previousBroadcasts: StateFlow<List<LiveSession>> = _selectedCourseId
        .flatMapLatest<String?, List<LiveSession>> { id ->
            if (id == null) flowOf(emptyList())
            else classroomDao.getPreviousSessionsForCourse(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun toggleAttendance(userId: String, isPresent: Boolean) {
        val courseId = _selectedCourseId.value ?: return
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.timeInMillis = _attendanceDate.value
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val today = cal.timeInMillis

            classroomDao.upsertAttendance(
                Attendance(
                    userId = userId,
                    courseId = courseId,
                    date = today,
                    isPresent = isPresent
                )
            )
            addLog("MARK_ATTENDANCE", userId, "Marked student as ${if (isPresent) "Present" else "Absent"} for $courseId on $today")
        }
    }

    fun upsertModule(module: ModuleContent) {
        viewModelScope.launch {
            classroomDao.upsertModule(module)
            addLog("UPSERT_MODULE", module.id, "Tutor updated/created module ${module.title}")
        }
    }

    fun deleteModule(moduleId: String) {
        viewModelScope.launch {
            classroomDao.deleteModule(moduleId)
            addLog("DELETE_MODULE", moduleId, "Tutor deleted module $moduleId")
        }
    }

    fun updateGrade(userId: String, assignmentId: String, score: Double, feedback: String) {
        val courseId = _selectedCourseId.value ?: return
        viewModelScope.launch {
            val newGrade = Grade(
                id = "grade_${userId}_${assignmentId}",
                userId = userId,
                courseId = courseId,
                assignmentId = assignmentId,
                score = score,
                feedback = feedback,
                gradedAt = System.currentTimeMillis()
            )
            classroomDao.upsertGrade(newGrade)
            addLog("UPDATE_GRADE", userId, "Tutor updated grade for student $userId in assignment $assignmentId to $score%")
        }
    }

    fun upsertAssignment(assignment: Assignment) {
        viewModelScope.launch {
            classroomDao.upsertAssignment(assignment)
            addLog("UPSERT_ASSIGNMENT", assignment.id, "Tutor updated/created assignment ${assignment.title}")
        }
    }

    fun deleteAssignment(assignmentId: String) {
        viewModelScope.launch {
            classroomDao.deleteAssignment(assignmentId)
            addLog("DELETE_ASSIGNMENT", assignmentId, "Tutor deleted assignment $assignmentId")
        }
    }

    fun assignCourseToSelf(courseId: String) {
        viewModelScope.launch {
            assignedCourseDao.assignCourse(AssignedCourse(tutorId, courseId))
            addLog("ASSIGN_COURSE_TO_SELF", courseId, "Tutor assigned themselves to course $courseId")
        }
    }

    fun unassignCourseFromSelf(courseId: String) {
        viewModelScope.launch {
            assignedCourseDao.unassignCourse(tutorId, courseId)
            addLog("UNASSIGN_COURSE_FROM_SELF", courseId, "Tutor unassigned themselves from course $courseId")
        }
    }

    fun updateTutorProfile(bio: String, department: String, officeHours: String, title: String? = null) {
        viewModelScope.launch {
            val current = currentUserLocal.value ?: return@launch
            val profile = TutorProfile(
                id = tutorId,
                name = current.name,
                title = title ?: current.title,
                email = current.email,
                photoUrl = current.photoUrl,
                department = department,
                officeHours = officeHours,
                bio = bio
            )
            classroomDao.upsertTutorProfile(profile)
            
            // Also update the main user record title if changed
            if (title != null && title != current.title) {
                userDao.upsertUser(current.copy(title = title))
            }
            
            addLog("UPDATE_TUTOR_PROFILE", tutorId, "Tutor updated professional profile details")
        }
    }

    fun addToLibrary(productId: String, category: String) {
        viewModelScope.launch {
            val purchase = PurchaseItem(
                purchaseId = UUID.randomUUID().toString(),
                userId = tutorId,
                productId = productId,
                mainCategory = category,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = "Tutor Privilege",
                amountFromWallet = 0.0,
                amountPaidExternal = 0.0,
                totalPricePaid = 0.0,
                quantity = 1,
                orderConfirmation = "TUTOR-${UUID.randomUUID().toString().take(8).uppercase()}"
            )
            userDao.addPurchase(purchase)
            addLog("TUTOR_ADD_TO_LIBRARY", productId, "Tutor added $productId to library for free")
        }
    }

    fun removeFromLibrary(productId: String) {
        viewModelScope.launch {
            userDao.deletePurchase(tutorId, productId)
            addLog("TUTOR_REMOVE_FROM_LIBRARY", productId, "Tutor removed $productId from library")
        }
    }

    // --- Live Stream ---
    
    fun toggleLiveStream(isLive: Boolean) {
        val courseId = _selectedCourseId.value ?: return
        val moduleId = _selectedModuleId.value ?: ""
        viewModelScope.launch {
            _isLive.value = isLive
            if (!isLive) _isPaused.value = false // Ensure it's not paused when ending
            
            // Robust name retrieval
            val profile = classroomDao.getTutorProfile(tutorId)
            val userLocal = userDao.getUserById(tutorId)
            val tutorName = profile?.name ?: userLocal?.name ?: "Tutor"
            
            val session = LiveSession(
                id = if (isLive) "live_${courseId}" else UUID.randomUUID().toString(),
                courseId = courseId,
                moduleId = moduleId,
                title = if (isLive) "Live Broadcast" else "Archived Broadcast",
                tutorId = tutorId,
                tutorName = tutorName,
                startTime = System.currentTimeMillis(),
                endTime = if (!isLive) System.currentTimeMillis() else null,
                streamUrl = "https://example.com/stream/${courseId}",
                isActive = isLive
            )
            classroomDao.insertLiveSessions(listOf(session))
            addLog(if (isLive) "START_STREAM" else "END_STREAM", courseId, "Tutor ${if (isLive) "started" else "ended"} live broadcast for $courseId")
        }
    }

    fun setLivePaused(paused: Boolean) {
        _isPaused.value = paused
        addLog("PAUSE_STREAM", _selectedCourseId.value ?: "unknown", "Tutor ${if (paused) "paused" else "resumed"} live broadcast")
    }

    fun deletePreviousBroadcast(sessionId: String) {
        viewModelScope.launch {
            classroomDao.deleteSession(sessionId)
            addLog("DELETE_PREVIOUS_BROADCAST", sessionId, "Tutor deleted an archived broadcast")
        }
    }

    // --- Messaging ---
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<ClassroomMessage>> = _selectedStudent
        .flatMapLatest { student ->
            if (student == null) flowOf(emptyList())
            else classroomDao.getChatHistory("GENERAL", student.id, tutorId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentConversations: StateFlow<List<ConversationPreview>> = classroomDao.getAllMessagesForUser(tutorId)
        .combine(userDao.getAllUsersFlow()) { messages, users ->
            messages.groupBy { if (it.senderId == tutorId) it.receiverId else it.senderId }
                .mapNotNull { (otherId, msgs) ->
                    val student = users.find { it.id == otherId }
                    if (student != null) {
                        ConversationPreview(student, msgs.first())
                    } else null
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(text: String) {
        val student = _selectedStudent.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val messageId = UUID.randomUUID().toString()
            val message = ClassroomMessage(
                id = messageId,
                courseId = "GENERAL",
                senderId = tutorId,
                receiverId = student.id,
                message = text,
                timestamp = System.currentTimeMillis()
            )
            classroomDao.sendMessage(message)
            
            // Notify Student
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = student.id,
                productId = "MESSAGE",
                title = "New Message from Tutor",
                message = "You have a new message from your tutor: \"${text.take(30)}${if (text.length > 30) "..." else ""}\"",
                timestamp = System.currentTimeMillis(),
                type = "MESSAGE"
            ))

            addLog("SEND_MESSAGE", student.id, "Sent message to ${student.name}")
        }
    }

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
                db.bookDao(),
                db.audioBookDao(),
                db.assignedCourseDao(),
                tutorId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
