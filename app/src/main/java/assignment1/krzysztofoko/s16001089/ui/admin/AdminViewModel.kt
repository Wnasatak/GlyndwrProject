package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the Admin Hub.
 * Manages the review process for academic course applications, user accounts, and the product catalog.
 */
class AdminViewModel(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val bookDao: BookDao,
    private val audioBookDao: AudioBookDao,
    private val gearDao: GearDao
) : ViewModel() {

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

    // --- Actions: Applications ---
    fun approveApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            userDao.updateEnrollmentStatus(appId, "APPROVED")
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = studentId,
                productId = appId.split("_").last(),
                title = "Application Approved!",
                message = "Your application for '$courseTitle' has been approved. You can now complete your enrollment.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))
        }
    }

    fun rejectApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            userDao.updateEnrollmentStatus(appId, "REJECTED")
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = studentId,
                productId = appId.split("_").last(),
                title = "Application Update",
                message = "Unfortunately, your application for '$courseTitle' was not successful at this time.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))
        }
    }

    // --- Actions: User Management ---
    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            userDao.getUserById(userId)?.let { user ->
                userDao.upsertUser(user.copy(role = newRole))
            }
        }
    }

    fun saveUser(user: UserLocal) {
        viewModelScope.launch {
            userDao.upsertUser(user)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userDao.deleteFullUserAccount(userId)
        }
    }

    // --- Actions: Catalog Management ---
    fun saveBook(book: Book) {
        viewModelScope.launch { bookDao.upsertBook(book) }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch { bookDao.deleteBook(id) }
    }

    fun saveAudioBook(audioBook: AudioBook) {
        viewModelScope.launch { audioBookDao.upsertAudioBook(audioBook) }
    }

    fun deleteAudioBook(id: String) {
        viewModelScope.launch { audioBookDao.deleteAudioBook(id) }
    }

    fun saveCourse(course: Course) {
        viewModelScope.launch { courseDao.upsertCourse(course) }
    }

    fun deleteCourse(id: String) {
        viewModelScope.launch { deleteCourse(id) }
    }

    fun saveGear(gear: Gear) {
        viewModelScope.launch { gearDao.upsertGear(gear) }
    }

    fun deleteGear(id: String) {
        viewModelScope.launch { gearDao.deleteGear(id) }
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
            db.gearDao()
        ) as T
    }
}
