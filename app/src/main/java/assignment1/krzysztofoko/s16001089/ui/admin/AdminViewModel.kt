package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the Admin Hub.
 */
class AdminViewModel(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val bookDao: BookDao,
    private val audioBookDao: AudioBookDao,
    private val gearDao: GearDao,
    private val auditDao: AuditDao
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- Navigation State ---
    private val _currentSection = MutableStateFlow(AdminSection.DASHBOARD)
    val currentSection: StateFlow<AdminSection> = _currentSection.asStateFlow()

    fun setSection(section: AdminSection) {
        _currentSection.value = section
    }

    // --- Course Applications ---
    val applications: StateFlow<List<AdminApplicationItem>> = flow {
        userDao.getAllEnrollmentsFlow().collect { list ->
            val mapped = list.map { app ->
                val course = courseDao.getCourseById(app.courseId)
                val user = userDao.getUserById(app.userId)
                AdminApplicationItem(app, course, user)
            }
            emit(mapped)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- User Management ---
    val allUsers: StateFlow<List<UserLocal>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Catalog Management ---
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allAudioBooks: Flow<List<AudioBook>> = audioBookDao.getAllAudioBooks()
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val allGear: Flow<List<Gear>> = gearDao.getAllGear()

    // --- System Logs ---
    val adminLogs: Flow<List<SystemLog>> = auditDao.getAdminLogs()
    val userLogs: Flow<List<SystemLog>> = auditDao.getUserLogs()

    private fun addLog(action: String, targetId: String, details: String, type: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            auditDao.insertLog(SystemLog(
                userId = user?.uid ?: "unknown",
                userName = user?.displayName ?: "Unknown",
                action = action,
                targetId = targetId,
                details = details,
                logType = type
            ))
        }
    }

    // --- Actions: Applications ---
    fun approveApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            userDao.updateEnrollmentStatus(appId, "APPROVED")
            addLog("APPROVED", appId, "Approved enrollment for $courseTitle", "ADMIN")
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = studentId,
                productId = appId.split("_").last(),
                title = "Application Approved!",
                message = "Your application for '$courseTitle' has been approved.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))
        }
    }

    fun rejectApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            userDao.updateEnrollmentStatus(appId, "REJECTED")
            addLog("REJECTED", appId, "Rejected enrollment for $courseTitle", "ADMIN")
        }
    }

    // --- Actions: User Management ---
    fun saveUser(user: UserLocal) {
        viewModelScope.launch {
            val existing = userDao.getUserById(user.id)
            userDao.upsertUser(user)
            val action = if (existing == null) "CREATED" else "EDITED"
            addLog(action, user.id, "$action user account: ${user.email}", "ADMIN")
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userDao.deleteFullUserAccount(userId)
            addLog("DELETED", userId, "Permanently removed user account", "ADMIN")
        }
    }

    // --- Actions: Catalog Management ---
    fun saveBook(book: Book) {
        viewModelScope.launch { 
            val existing = bookDao.getBookById(book.id)
            bookDao.upsertBook(book)
            val action = if (existing == null) "CREATED" else "EDITED"
            addLog(action, book.id, "$action book: ${book.title}", "ADMIN")
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch { 
            bookDao.deleteBook(id) 
            addLog("DELETED", id, "Removed book from catalog", "ADMIN")
        }
    }

    fun saveAudioBook(audioBook: AudioBook) {
        viewModelScope.launch { 
            val existing = audioBookDao.getAudioBookById(audioBook.id)
            audioBookDao.upsertAudioBook(audioBook)
            val action = if (existing == null) "CREATED" else "EDITED"
            addLog(action, audioBook.id, "$action audiobook: ${audioBook.title}", "ADMIN")
        }
    }

    fun deleteAudioBook(id: String) {
        viewModelScope.launch { 
            audioBookDao.deleteAudioBook(id) 
            addLog("DELETED", id, "Removed audiobook from catalog", "ADMIN")
        }
    }

    fun saveCourse(course: Course) {
        viewModelScope.launch { 
            val existing = courseDao.getCourseById(course.id)
            courseDao.upsertCourse(course) 
            val action = if (existing == null) "CREATED" else "EDITED"
            addLog(action, course.id, "$action course: ${course.title}", "ADMIN")
        }
    }

    fun deleteCourse(id: String) {
        viewModelScope.launch { 
            courseDao.deleteCourse(id) 
            addLog("DELETED", id, "Removed course from catalog", "ADMIN")
        }
    }

    fun saveGear(gear: Gear) {
        viewModelScope.launch { 
            val existing = gearDao.getGearById(gear.id)
            gearDao.upsertGear(gear) 
            val action = if (existing == null) "CREATED" else "EDITED"
            addLog(action, gear.id, "$action gear: ${gear.title}", "ADMIN")
        }
    }

    fun deleteGear(id: String) {
        viewModelScope.launch { 
            gearDao.deleteGear(id) 
            addLog("DELETED", id, "Removed gear item from catalog", "ADMIN")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            auditDao.clearAllLogs()
        }
    }
}

data class AdminApplicationItem(
    val details: CourseEnrollmentDetails,
    val course: Course?,
    val student: UserLocal?
)

class AdminViewModelFactory(
    private val db: AppDatabase
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return AdminViewModel(
            db.userDao(),
            db.courseDao(),
            db.bookDao(),
            db.audioBookDao(),
            db.gearDao(),
            db.auditDao()
        ) as T
    }
}
