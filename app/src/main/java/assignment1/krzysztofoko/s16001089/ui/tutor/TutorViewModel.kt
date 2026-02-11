package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class TutorSection {
    DASHBOARD, MY_COURSES, SELECTED_COURSE, COURSE_MODULES, COURSE_STUDENTS,
    COURSE_ASSIGNMENTS, COURSE_GRADES, COURSE_LIVE, COURSE_ARCHIVED_BROADCASTS,
    STUDENTS, MESSAGES, CHAT, LIBRARY, BOOKS, AUDIOBOOKS, READ_BOOK,
    LISTEN_AUDIOBOOK, TEACHER_DETAIL, CREATE_ASSIGNMENT, START_LIVE_STREAM,
    STUDENT_PROFILE, ABOUT, NOTIFICATIONS, COURSE_ATTENDANCE, INDIVIDUAL_ATTENDANCE_DETAIL
}

data class ConversationPreview(val student: UserLocal, val lastMessage: ClassroomMessage)

/**
 * ViewModel for the Tutor Dashboard. Optimized for safety and performance.
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

    private val _selectedAssignmentId = MutableStateFlow<String?>(null)
    val selectedAssignmentId: StateFlow<String?> = _selectedAssignmentId.asStateFlow()

    private val _activeBook = MutableStateFlow<Book?>(null)
    val activeBook: StateFlow<Book?> = _activeBook.asStateFlow()

    private val _activeAudioBook = MutableStateFlow<AudioBook?>(null)
    val activeAudioBook: StateFlow<AudioBook?> = _activeAudioBook.asStateFlow()

    private val _isLive = MutableStateFlow(false)
    val isLive: StateFlow<Boolean> = _isLive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _attendanceDate = MutableStateFlow(System.currentTimeMillis())
    val attendanceDate: StateFlow<Long> = _attendanceDate.asStateFlow()

    private val _selectedGradesTab = MutableStateFlow(0)
    val selectedGradesTab: StateFlow<Int> = _selectedGradesTab.asStateFlow()

    fun setGradesTab(index: Int) { _selectedGradesTab.value = index }
    fun setAttendanceDate(date: Long) { _attendanceDate.value = date }

    fun setSection(section: TutorSection, student: UserLocal? = null) {
        _currentSection.value = section
        if (student != null) _selectedStudent.value = student
        if (section != TutorSection.READ_BOOK) _activeBook.value = null
    }

    fun selectCourse(courseId: String) {
        _selectedCourseId.value = courseId
        setSection(TutorSection.SELECTED_COURSE)
    }

    fun updateSelectedCourse(courseId: String?) { _selectedCourseId.value = courseId }
    fun selectModule(moduleId: String?) { _selectedModuleId.value = moduleId }
    fun selectAssignment(assignmentId: String?) { _selectedAssignmentId.value = assignmentId }

    fun openBook(book: Book) {
        _activeBook.value = book
        _currentSection.value = TutorSection.READ_BOOK
    }

    fun openAudioBook(ab: AudioBook) {
        _activeAudioBook.value = ab
        setSection(TutorSection.LISTEN_AUDIOBOOK)
    }

    fun findCourseById(courseId: String): Course? {
        return allCourses.value.find { it.id == courseId }
    }

    // --- Optimized Data Streams ---

    val currentUserLocal: StateFlow<UserLocal?> = (if (tutorId.isNotEmpty()) {
        userDao.getUserFlow(tutorId)
    } else {
        flowOf<UserLocal?>(null)
    }).stateIn(viewModelScope, SharingStarted.Lazily, null)

    val tutorProfile: StateFlow<TutorProfile?> = (if (tutorId.isNotEmpty()) {
        classroomDao.getTutorProfileFlow(tutorId)
    } else {
        flowOf<TutorProfile?>(null)
    }).stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allCourses: StateFlow<List<Course>> = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val assignedCourses: StateFlow<List<Course>> = (if (tutorId.isNotEmpty()) {
        combine(courseDao.getAllCourses(), assignedCourseDao.getAssignedCoursesForTutor(tutorId)) { courses, assignments ->
            val assignedIds = assignments.map { it.courseId }.toSet()
            courses.filter { assignedIds.contains(it.id) }
        }
    } else {
        flowOf<List<Course>>(emptyList())
    }).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourse: StateFlow<Course?> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<Course?>(null)
            else flow { emit(courseDao.getCourseById(id)) }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val enrolledStudentsInSelectedCourse: StateFlow<List<UserLocal>> = combine(
        _selectedCourseId, userDao.getAllUsersFlow(), userDao.getAllEnrollmentsFlow()
    ) { courseId, users, enrollments ->
        if (courseId == null) emptyList()
        else {
            val studentIds = enrollments.filter { it.courseId == courseId && (it.status == "APPROVED" || it.status == "ENROLLED") }.map { it.userId }.toSet()
            users.filter { it.id in studentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseGrades: StateFlow<List<Grade>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<List<Grade>>(emptyList())
            else classroomDao.getAllGradesForCourse(id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseAssignments: StateFlow<List<Assignment>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<List<Assignment>>(emptyList())
            else classroomDao.getAssignmentsForCourse(id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseModules: StateFlow<List<ModuleContent>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<List<ModuleContent>>(emptyList())
            else classroomDao.getModulesForCourse(id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allCourseAttendance: StateFlow<List<Attendance>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<List<Attendance>>(emptyList())
            else classroomDao.getAllAttendanceForCourse(id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCourseAttendance: StateFlow<List<Attendance>> = combine(_selectedCourseId, _attendanceDate) { id, date -> id to date }
        .flatMapLatest { (id, date) ->
            if (id == null) flowOf<List<Attendance>>(emptyList())
            else {
                val cal = Calendar.getInstance().apply { timeInMillis = date; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                classroomDao.getAttendanceForCourseAndDate(id, cal.timeInMillis)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recordedAttendanceDates: StateFlow<List<Long>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<List<Long>>(emptyList())
            else classroomDao.getRecordedAttendanceDates(id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allUsers: StateFlow<List<UserLocal>> = userDao.getAllUsersFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allEnrollments: StateFlow<List<CourseEnrollmentDetails>> = userDao.getAllEnrollmentsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingApplications: StateFlow<Int> = userDao.getAllEnrollmentsFlow()
        .map { it.count { app -> app.status == "PENDING_REVIEW" } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val unreadNotificationsCount: StateFlow<Int> = (if (tutorId.isNotEmpty()) {
        userDao.getNotificationsForUser(tutorId).map { list -> list.count { !it.isRead } }
    } else {
        flowOf<Int>(0)
    }).stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val allBooks: StateFlow<List<Book>> = bookDao.getAllBooks().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allAudioBooks: StateFlow<List<AudioBook>> = audioBookDao.getAllAudioBooks().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val purchasedIds: StateFlow<Set<String>> = (if (tutorId.isNotEmpty()) {
        userDao.getPurchaseIds(tutorId).map { it.toSet() }
    } else {
        flowOf<Set<String>>(emptySet())
    }).stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val libraryBooks: StateFlow<List<Book>> = combine(allBooks, purchasedIds) { books, ids ->
        books.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val libraryAudioBooks: StateFlow<List<AudioBook>> = combine(allAudioBooks, purchasedIds) { books, ids ->
        books.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val previousBroadcasts: StateFlow<List<LiveSession>> = _selectedCourseId
        .flatMapLatest { id ->
            if (id == null) flowOf<List<LiveSession>>(emptyList())
            else classroomDao.getPreviousSessionsForCourse(id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedStudentEnrollments: StateFlow<List<CourseEnrollmentDetails>> = _selectedStudent
        .flatMapLatest { student ->
            if (student == null) flowOf<List<CourseEnrollmentDetails>>(emptyList())
            else userDao.getEnrollmentsForUserFlow(student.id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedStudentGrades: StateFlow<List<Grade>> = _selectedStudent
        .flatMapLatest { student ->
            if (student == null) flowOf<List<Grade>>(emptyList())
            else classroomDao.getAllGradesForUser(student.id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedStudentAttendance: StateFlow<List<Attendance>> = _selectedStudent
        .flatMapLatest { student ->
            if (student == null) flowOf<List<Attendance>>(emptyList())
            else classroomDao.getAttendanceForUser(student.id)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<ClassroomMessage>> = _selectedStudent
        .flatMapLatest { student ->
            if (student == null) flowOf<List<ClassroomMessage>>(emptyList())
            else classroomDao.getChatHistory("GENERAL", student.id, tutorId)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentConversations: StateFlow<List<ConversationPreview>> = (if (tutorId.isNotEmpty()) {
        classroomDao.getAllMessagesForUser(tutorId).combine(userDao.getAllUsersFlow()) { messages, users ->
            messages.groupBy { if (it.senderId == tutorId) it.receiverId else it.senderId }
                .mapNotNull { (otherId, msgs) ->
                    val student = users.find { it.id == otherId }
                    if (student != null) ConversationPreview(student, msgs.first()) else null
                }
        }
    } else {
        flowOf<List<ConversationPreview>>(emptyList())
    }).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Actions ---

    private fun addLog(action: String, targetId: String, details: String) {
        if (tutorId.isEmpty()) return
        viewModelScope.launch {
            val user = auth.currentUser
            auditDao.insertLog(SystemLog(userId = tutorId, userName = user?.displayName ?: "Tutor", action = action, targetId = targetId, details = details, logType = "TUTOR"))
        }
    }

    fun toggleAttendance(userId: String, isPresent: Boolean) {
        val courseId = _selectedCourseId.value ?: return
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { timeInMillis = _attendanceDate.value; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            classroomDao.upsertAttendance(Attendance(userId = userId, courseId = courseId, date = cal.timeInMillis, isPresent = isPresent))
            addLog("MARK_ATTENDANCE", userId, "Marked student as ${if (isPresent) "Present" else "Absent"}")
        }
    }

    fun upsertModule(module: ModuleContent) {
        viewModelScope.launch {
            classroomDao.upsertModule(module)
            addLog("UPSERT_MODULE", module.id, "Tutor updated module ${module.title}")
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
            val newGrade = Grade(id = "grade_${userId}_${assignmentId}", userId = userId, courseId = courseId, assignmentId = assignmentId, score = score, feedback = feedback, gradedAt = System.currentTimeMillis())
            classroomDao.upsertGrade(newGrade)
            addLog("UPDATE_GRADE", userId, "Tutor updated grade for student $userId to $score%")
        }
    }

    fun upsertAssignment(assignment: Assignment) {
        viewModelScope.launch {
            classroomDao.upsertAssignment(assignment)
            addLog("UPSERT_ASSIGNMENT", assignment.id, "Tutor updated assignment ${assignment.title}")
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
            addLog("ASSIGN_COURSE", courseId, "Tutor assigned themselves to course $courseId")
        }
    }

    fun unassignCourseFromSelf(courseId: String) {
        viewModelScope.launch {
            assignedCourseDao.unassignCourse(tutorId, courseId)
            addLog("UNASSIGN_COURSE", courseId, "Tutor unassigned themselves from course $courseId")
        }
    }

    fun updateTutorProfile(bio: String, department: String, officeHours: String, title: String? = null) {
        viewModelScope.launch {
            val current = currentUserLocal.value ?: return@launch
            val profile = TutorProfile(id = tutorId, name = current.name, title = title ?: current.title, email = current.email, photoUrl = current.photoUrl, department = department, officeHours = officeHours, bio = bio)
            classroomDao.upsertTutorProfile(profile)
            if (title != null && title != current.title) userDao.upsertUser(current.copy(title = title))
            addLog("UPDATE_TUTOR_PROFILE", tutorId, "Tutor updated professional profile")
        }
    }

    fun addToLibrary(productId: String, category: String) {
        viewModelScope.launch {
            val purchase = PurchaseItem(purchaseId = UUID.randomUUID().toString(), userId = tutorId, productId = productId, mainCategory = category, purchasedAt = System.currentTimeMillis(), paymentMethod = "Tutor Privilege", amountFromWallet = 0.0, amountPaidExternal = 0.0, totalPricePaid = 0.0, quantity = 1, orderConfirmation = "TUTOR-${UUID.randomUUID().toString().take(8).uppercase()}")
            userDao.addPurchase(purchase)
            addLog("TUTOR_ADD_TO_LIBRARY", productId, "Tutor added item to library")
        }
    }

    fun sendMessage(text: String) {
        val student = _selectedStudent.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val message = ClassroomMessage(id = UUID.randomUUID().toString(), courseId = "GENERAL", senderId = tutorId, receiverId = student.id, message = text, timestamp = System.currentTimeMillis())
            classroomDao.sendMessage(message)
            userDao.addNotification(NotificationLocal(id = UUID.randomUUID().toString(), userId = student.id, productId = "MESSAGE", title = "New Message", message = "You have a new message from your tutor.", timestamp = System.currentTimeMillis(), type = "MESSAGE"))
            addLog("SEND_MESSAGE", student.id, "Sent message to ${student.name}")
        }
    }

    fun removeFromLibrary(productId: String) {
        viewModelScope.launch {
            userDao.deletePurchase(tutorId, productId)
            addLog("TUTOR_REMOVE_FROM_LIBRARY", productId, "Tutor removed item from library")
        }
    }

    fun toggleLiveStream(isLive: Boolean) {
        val courseId = _selectedCourseId.value ?: return
        val moduleId = _selectedModuleId.value ?: ""
        val assignmentId = _selectedAssignmentId.value
        viewModelScope.launch {
            _isLive.value = isLive
            if (!isLive) _isPaused.value = false
            val profile = classroomDao.getTutorProfile(tutorId)
            val userLocal = userDao.getUserById(tutorId)
            val tutorName = profile?.name ?: userLocal?.name ?: "Tutor"
            val session = LiveSession(id = if (isLive) "live_${courseId}" else UUID.randomUUID().toString(), courseId = courseId, moduleId = moduleId, assignmentId = assignmentId, title = if (isLive) "Live Broadcast" else "Archived Broadcast", tutorId = tutorId, tutorName = tutorName, startTime = System.currentTimeMillis(), endTime = if (!isLive) System.currentTimeMillis() else null, streamUrl = "https://example.com/stream/${courseId}", isActive = isLive)
            classroomDao.insertLiveSessions(listOf(session))
            addLog(if (isLive) "START_STREAM" else "END_STREAM", courseId, "Tutor ${if (isLive) "started" else "ended"} live broadcast")
        }
    }

    fun setLivePaused(paused: Boolean) {
        _isPaused.value = paused
        addLog("PAUSE_STREAM", _selectedCourseId.value ?: "unknown", "Tutor updated live status")
    }

    fun deletePreviousBroadcast(sessionId: String) {
        viewModelScope.launch {
            classroomDao.deleteSession(sessionId)
            addLog("DELETE_BROADCAST", sessionId, "Tutor deleted archived broadcast")
        }
    }

    fun updateBroadcastTitle(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            classroomDao.updateSessionTitle(sessionId, newTitle)
            addLog("UPDATE_BROADCAST_TITLE", sessionId, "Tutor renamed broadcast")
        }
    }

    fun shareBroadcastWithAll(session: LiveSession) {
        viewModelScope.launch {
            val studentIds = enrolledStudentsInSelectedCourse.value.map { it.id }
            studentIds.forEach { studentId -> userDao.addNotification(NotificationLocal(id = UUID.randomUUID().toString(), userId = studentId, productId = session.id, title = "Broadcast Shared", message = "Tutor shared a replay: ${session.title}", timestamp = System.currentTimeMillis(), type = "BROADCAST")) }
            addLog("SHARE_BROADCAST_ALL", session.id, "Tutor shared replay with all students")
        }
    }

    fun shareBroadcastWithSpecificStudents(session: LiveSession, studentIds: List<String>) {
        viewModelScope.launch {
            studentIds.forEach { studentId -> userDao.addNotification(NotificationLocal(id = UUID.randomUUID().toString(), userId = studentId, productId = session.id, title = "Broadcast Shared", message = "Tutor shared a replay: ${session.title}", timestamp = System.currentTimeMillis(), type = "BROADCAST")) }
            addLog("SHARE_BROADCAST_SPECIFIC", session.id, "Tutor shared replay with specific students")
        }
    }
}

class TutorViewModelFactory(private val db: AppDatabase, private val tutorId: String) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TutorViewModel(db.userDao(), db.courseDao(), db.classroomDao(), db.auditDao(), db.bookDao(), db.audioBookDao(), db.assignedCourseDao(), tutorId) as T
    }
}
