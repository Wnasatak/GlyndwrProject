package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AdminViewModel(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val bookDao: BookDao,
    private val audioBookDao: AudioBookDao,
    private val gearDao: GearDao,
    private val auditDao: AuditDao,
    private val classroomDao: ClassroomDao
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _currentAdminId = MutableStateFlow(auth.currentUser?.uid ?: "")
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAdminUser: StateFlow<UserLocal?> = _currentAdminId.flatMapLatest { id ->
        if (id.isNotEmpty()) userDao.getUserFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationsCount: StateFlow<Int> = _currentAdminId.flatMapLatest { id ->
        if (id.isNotEmpty()) {
            userDao.getNotificationsForUser(id).map { list -> list.count { !it.isRead } }
        } else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _currentSection = MutableStateFlow(AdminSection.DASHBOARD)
    val currentSection: StateFlow<AdminSection> = _currentSection.asStateFlow()

    fun setSection(section: AdminSection) {
        _currentSection.value = section
    }

    val applications: StateFlow<List<AdminApplicationItem>> = flow {
        userDao.getAllEnrollmentsFlow().collect { list ->
            val mapped = list.map { app ->
                val course = courseDao.getCourseById(app.courseId)
                val requestedCourse = app.requestedCourseId?.let { courseDao.getCourseById(it) }
                val user = userDao.getUserById(app.userId)
                AdminApplicationItem(app, course, user, requestedCourse)
            }.sortedByDescending { it.details.submittedAt }
            emit(mapped)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserLocal>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allAudioBooks: Flow<List<AudioBook>> = audioBookDao.getAllAudioBooks()
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val allGear: Flow<List<Gear>> = gearDao.getAllGear()

    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminLogs: Flow<List<SystemLog>> = auditDao.getAdminLogs()
    val userLogs: Flow<List<SystemLog>> = auditDao.getUserLogs()

    init {
        viewModelScope.launch {
            auditDao.performLogMaintenance()
            addLog("MAINTENANCE", "SYSTEM", "Automated log cleanup performed.", "ADMIN")
        }
    }

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

    fun approveApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            val enrollment = userDao.getEnrollmentById(appId)
            
            // CRITICAL FIX: Always use the userId from the actual enrollment record
            val targetUserId = enrollment?.userId ?: studentId
            val student = userDao.getUserById(targetUserId)

            if (enrollment == null) {
                addLog("ERROR", appId, "Attempted to approve a non-existent enrollment.", "ADMIN")
                return@launch
            }

            if (enrollment.isWithdrawal) {
                // WITHDRAWAL: RESET TO USER
                userDao.deleteEnrollmentById(appId)
                userDao.deletePurchasesForUser(targetUserId) 
                student?.let { 
                    userDao.upsertUser(it.copy(role = "user", discountPercent = 0.0))
                }
                addLog("APPROVED_WITHDRAWAL", appId, "Confirmed withdrawal for $courseTitle. Student role reverted to user and individual discounts cleared.", "ADMIN")
            } else if (enrollment.requestedCourseId != null) {
                val newCourseId = enrollment.requestedCourseId!!
                userDao.updateEnrollmentAfterChange(appId, newCourseId, "APPROVED")
                addLog("APPROVED_CHANGE", appId, "Approved course change to $courseTitle", "ADMIN")
            } else {
                // REGULAR ENROLLMENT: SET TO STUDENT
                userDao.updateEnrollmentStatus(appId, "APPROVED")
                student?.let {
                    userDao.upsertUser(it.copy(role = "student"))
                }
                addLog("APPROVED", appId, "Approved enrollment for $courseTitle. Student role activated.", "ADMIN")
            }
            
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = targetUserId,
                productId = appId.split("_").last(),
                title = "Application Approved!",
                message = "Your request for '$courseTitle' has been approved.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))
        }
    }

    fun rejectApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            val enrollment = userDao.getEnrollmentById(appId)
            val targetUserId = enrollment?.userId ?: studentId
            
            userDao.updateEnrollmentStatus(appId, "REJECTED")
            addLog("REJECTED", appId, "Rejected enrollment for $courseTitle", "ADMIN")
            
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = targetUserId,
                productId = appId.split("_").last(),
                title = "Application Declined",
                message = "Your request for '$courseTitle' was not approved at this time.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))
        }
    }

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

    fun saveRoleDiscount(role: String, discount: Double) {
        viewModelScope.launch {
            userDao.upsertRoleDiscount(RoleDiscount(role, discount))
            addLog("DISCOUNT_UPDATED", role, "Set $role discount to $discount%", "ADMIN")
        }
    }

    fun saveBook(book: Book) {
        viewModelScope.launch { 
            bookDao.upsertBook(book)
            addLog("SAVED", book.id, "Saved book: ${book.title}", "ADMIN")
        }
    }

    fun deleteBook(id: String) {
        viewModelScope.launch { 
            bookDao.deleteBook(id) 
            addLog("DELETED", id, "Removed book", "ADMIN")
        }
    }

    fun saveAudioBook(audioBook: AudioBook) {
        viewModelScope.launch { 
            audioBookDao.upsertAudioBook(audioBook)
            addLog("SAVED", audioBook.id, "Saved audiobook: ${audioBook.title}", "ADMIN")
        }
    }

    fun deleteAudioBook(id: String) {
        viewModelScope.launch { 
            audioBookDao.deleteAudioBook(id) 
            addLog("DELETED", id, "Removed audiobook", "ADMIN")
        }
    }

    fun saveCourse(course: Course) {
        viewModelScope.launch { 
            courseDao.upsertCourse(course) 
            addLog("SAVED", course.id, "Saved course: ${course.title}", "ADMIN")
        }
    }

    fun deleteCourse(id: String) {
        viewModelScope.launch { 
            courseDao.deleteCourse(id) 
            addLog("DELETED", id, "Removed course", "ADMIN")
        }
    }

    fun saveGear(gear: Gear) {
        viewModelScope.launch { 
            gearDao.upsertGear(gear) 
            addLog("SAVED", gear.id, "Saved gear: ${gear.title}", "ADMIN")
        }
    }

    fun deleteGear(id: String) {
        viewModelScope.launch { 
            gearDao.deleteGear(id) 
            addLog("DELETED", id, "Removed gear", "ADMIN")
        }
    }

    fun getModulesForCourse(courseId: String): Flow<List<ModuleContent>> = 
        classroomDao.getModulesForCourse(courseId)

    fun saveModule(module: ModuleContent) {
        viewModelScope.launch {
            classroomDao.upsertModule(module)
            addLog("MODULE_SAVED", module.id, "Saved module: ${module.title}", "ADMIN")
        }
    }

    fun deleteModule(moduleId: String) {
        viewModelScope.launch {
            classroomDao.deleteModule(moduleId)
            addLog("MODULE_DELETED", moduleId, "Deleted module", "ADMIN")
        }
    }

    fun getAssignmentsForModule(moduleId: String): Flow<List<Assignment>> =
        classroomDao.getAssignmentsForModule(moduleId)

    fun saveAssignment(assignment: Assignment) {
        viewModelScope.launch {
            classroomDao.upsertAssignment(assignment)
            addLog("TASK_SAVED", assignment.id, "Saved task: ${assignment.title}", "ADMIN")
        }
    }

    fun deleteAssignment(assignmentId: String) {
        viewModelScope.launch {
            classroomDao.deleteAssignment(assignmentId)
            addLog("TASK_DELETED", assignmentId, "Deleted task", "ADMIN")
        }
    }

    fun sendBroadcastToRoleOrUser(
        title: String, 
        message: String, 
        targetRoles: List<String>, 
        specificUserId: String?, 
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val usersToNotify = if (specificUserId != null) {
                listOfNotNull(userDao.getUserById(specificUserId))
            } else {
                val allUsersList = userDao.getAllUsersFlow().first() 
                allUsersList.filter { user ->
                    targetRoles.any { role -> role.equals(user.role.trim(), ignoreCase = true) }
                }
            }
            
            usersToNotify.forEach { user ->
                userDao.addNotification(NotificationLocal(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    productId = "BROADCAST",
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = "ANNOUNCEMENT"
                ))
            }
            
            val targetDesc = specificUserId ?: targetRoles.joinToString(", ")
            addLog("BROADCAST", targetDesc, "Sent announcement to ${usersToNotify.size} users: $title", "ADMIN")
            onComplete(usersToNotify.size)
        }
    }

    fun clearLogs() {
        viewModelScope.launch { auditDao.clearAllLogs() }
    }
}

data class AdminApplicationItem(
    val details: CourseEnrollmentDetails,
    val course: Course?,
    val student: UserLocal?,
    val requestedCourse: Course? = null
)

class AdminViewModelFactory(
    private val db: AppDatabase
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdminViewModel(
            db.userDao(),
            db.courseDao(),
            db.bookDao(),
            db.audioBookDao(),
            db.gearDao(),
            db.auditDao(),
            db.classroomDao()
        ) as T
    }
}
