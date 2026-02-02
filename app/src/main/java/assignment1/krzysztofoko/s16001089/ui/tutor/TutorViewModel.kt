package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class TutorSection { DASHBOARD, MY_COURSES, STUDENTS, MESSAGES, CHAT, LIBRARY, BOOKS, AUDIOBOOKS, READ_BOOK, LISTEN_AUDIOBOOK, TEACHER_DETAIL }

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

    private val _activeBook = MutableStateFlow<Book?>(null)
    val activeBook: StateFlow<Book?> = _activeBook.asStateFlow()

    private val _activeAudioBook = MutableStateFlow<AudioBook?>(null)
    val activeAudioBook: StateFlow<AudioBook?> = _activeAudioBook.asStateFlow()

    fun setSection(section: TutorSection, student: UserLocal? = null) {
        _currentSection.value = section
        if (student != null) {
            _selectedStudent.value = student
        }
    }

    fun openBook(book: Book) {
        _activeBook.value = book
        _currentSection.value = TutorSection.READ_BOOK
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

    // --- Actions ---

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
