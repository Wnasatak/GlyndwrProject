package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * AdminViewModel.kt
 *
 * This ViewModel acts as the command centre for the administrative dashboard.
 * It manages the state and logic for administrative tasks including user management,
 * course application processing, catalog maintenance, and system auditing.
 */
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
    
    // Tracks the current administrator's ID for data synchronization.
    private val _currentAdminId = MutableStateFlow(auth.currentUser?.uid ?: "")
    
    // UI state for background processing feedback (e.g., showing a loading overlay).
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // State for temporary status messages (e.g., "Course deleted successfully") shown via snackbars or popups.
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    /**
     * Clears the current status message after it has been displayed.
     */
    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    /**
     * Reactive stream providing the currently authenticated administrator's local user data.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAdminUser: StateFlow<UserLocal?> = _currentAdminId.flatMapLatest { id ->
        if (id.isNotEmpty()) userDao.getUserFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Reactive stream providing the count of unread notifications for the current administrator.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationsCount: StateFlow<Int> = _currentAdminId.flatMapLatest { id ->
        if (id.isNotEmpty()) {
            userDao.getNotificationsForUser(id).map { list -> list.count { !it.isRead } }
        } else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current active section in the Admin Hub (Dashboard, Users, Catalog, etc.).
    private val _currentSection = MutableStateFlow(AdminSection.DASHBOARD)
    val currentSection: StateFlow<AdminSection> = _currentSection.asStateFlow()

    /**
     * Updates the currently active administrative section.
     */
    fun setSection(section: AdminSection) {
        _currentSection.value = section
    }

    /**
     * Reactive stream providing a list of all course applications (enrollments),
     * enriched with related course and user information for display.
     */
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

    /**
     * Reactive stream providing all registered users in the system.
     */
    val allUsers: StateFlow<List<UserLocal>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct data streams for catalog items and configurations.
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allAudioBooks: Flow<List<AudioBook>> = audioBookDao.getAllAudioBooks()
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val allGear: Flow<List<Gear>> = gearDao.getAllGear()

    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit and user activity logs.
    val adminLogs: Flow<List<SystemLog>> = auditDao.getAdminLogs()
    val userLogs: Flow<List<SystemLog>> = auditDao.getUserLogs()

    init {
        // Initial setup: clean up old logs and record system maintenance start.
        viewModelScope.launch {
            auditDao.performLogMaintenance()
            addLog("MAINTENANCE", "SYSTEM", "Automated log cleanup performed.")
        }
    }

    /**
     * Records a system action in the audit logs.
     * @param action The type of action (e.g., "SAVED", "DELETED").
     * @param targetId The ID of the entity affected.
     * @param details Additional textual information about the event.
     */
    private fun addLog(action: String, targetId: String, details: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            auditDao.insertLog(SystemLog(
                userId = user?.uid ?: "unknown",
                userName = user?.displayName ?: "Unknown",
                action = action,
                targetId = targetId,
                details = details,
                logType = "ADMIN"
            ))
        }
    }

    /**
     * Logic for approving a student's course application.
     * Handles normal enrollments, course change requests, and withdrawal requests.
     */
    fun approveApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val enrollment = userDao.getEnrollmentById(appId)
            val targetUserId = enrollment?.userId ?: studentId
            val student = userDao.getUserById(targetUserId)

            if (enrollment == null) {
                addLog("ERROR", appId, "Attempted to approve a non-existent enrolment.")
                _isProcessing.value = false
                return@launch
            }

            if (enrollment.isWithdrawal) {
                // Process Withdrawal: delete enrollment and purchases, reset user role.
                userDao.addEnrollmentHistory(EnrollmentHistory(
                    userId = targetUserId,
                    courseId = enrollment.courseId,
                    status = "WITHDRAWN",
                    timestamp = System.currentTimeMillis()
                ))
                userDao.deleteEnrollmentById(appId)
                userDao.deletePurchasesForUser(targetUserId)
                student?.let { 
                    userDao.upsertUser(it.copy(role = "user", discountPercent = 0.0))
                }
                addLog("APPROVED_WITHDRAWAL", appId, "Confirmed withdrawal for $courseTitle.")
            } else if (enrollment.requestedCourseId != null) {
                // Process Course Change: update to new course ID and record history.
                val newCourseId = enrollment.requestedCourseId
                userDao.addEnrollmentHistory(EnrollmentHistory(
                    userId = targetUserId,
                    courseId = newCourseId, 
                    status = "CHANGED",
                    timestamp = System.currentTimeMillis(),
                    previousCourseId = enrollment.courseId 
                ))
                userDao.updateEnrollmentAfterChange(appId, newCourseId, "APPROVED")
                addLog("APPROVED_CHANGE", appId, "Approved course change to $courseTitle")
            } else {
                // Process standard Application: activate student role and set status.
                userDao.updateEnrollmentStatus(appId, "APPROVED")
                student?.let {
                    userDao.upsertUser(it.copy(role = "student"))
                }
                addLog("APPROVED", appId, "Approved enrolment for $courseTitle. Student role activated.")
            }
            
            // Notify the user of approval.
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = targetUserId,
                productId = appId.split("_").last(),
                title = "Application Approved!",
                message = "Your request for '$courseTitle' has been approved.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Application approved."
        }
    }

    /**
     * Logic for rejecting a course application.
     */
    fun rejectApplication(appId: String, studentId: String, courseTitle: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val enrollment = userDao.getEnrollmentById(appId)
            val targetUserId = enrollment?.userId ?: studentId
            
            userDao.updateEnrollmentStatus(appId, "REJECTED")
            addLog("REJECTED", appId, "Rejected enrolment for $courseTitle")
            
            // Notify the user of rejection.
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = targetUserId,
                productId = appId.split("_").last(),
                title = "Application Declined",
                message = "Your request for '$courseTitle' was not approved at this time.",
                timestamp = System.currentTimeMillis(),
                type = "GENERAL"
            ))
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Application rejected."
        }
    }

    /**
     * Updates or creates a user account record in the local database.
     */
    fun saveUser(user: UserLocal) {
        viewModelScope.launch {
            _isProcessing.value = true
            val existing = userDao.getUserById(user.id)
            userDao.upsertUser(user)
            val action = if (existing == null) "CREATED" else "EDITED"
            addLog(action, user.id, "$action user account: ${user.email}")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "User saved successfully."
        }
    }

    /**
     * Permanently removes a user and all their associated data from the system.
     */
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            userDao.deleteFullUserAccount(userId)
            addLog("DELETED", userId, "Permanently removed user account")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "User deleted successfully."
        }
    }

    /**
     * Configures a global discount percentage for a specific user role (e.g., student).
     */
    fun saveRoleDiscount(role: String, discount: Double) {
        viewModelScope.launch {
            _isProcessing.value = true
            userDao.upsertRoleDiscount(RoleDiscount(role, discount))
            addLog("DISCOUNT_UPDATED", role, "Set $role discount to $discount%")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Discount updated."
        }
    }

    /**
     * Saves or updates a book entry in the catalog.
     */
    fun saveBook(book: Book) {
        viewModelScope.launch { 
            _isProcessing.value = true
            bookDao.upsertBook(book)
            addLog("SAVED", book.id, "Saved book: ${book.title}")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Book saved successfully."
        }
    }

    /**
     * Removes a book from the catalog.
     */
    fun deleteBook(id: String) {
        viewModelScope.launch { 
            _isProcessing.value = true
            bookDao.deleteBook(id)
            addLog("DELETED", id, "Removed book")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Book deleted."
        }
    }

    /**
     * Saves or updates an audiobook entry in the catalog.
     */
    fun saveAudioBook(audioBook: AudioBook) {
        viewModelScope.launch { 
            _isProcessing.value = true
            audioBookDao.upsertAudioBook(audioBook)
            addLog("SAVED", audioBook.id, "Saved audiobook: ${audioBook.title}")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Audiobook saved successfully."
        }
    }

    /**
     * Removes an audiobook from the catalog.
     */
    fun deleteAudioBook(id: String) {
        viewModelScope.launch { 
            _isProcessing.value = true
            audioBookDao.deleteAudioBook(id) 
            addLog("DELETED", id, "Removed audiobook")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Audiobook deleted."
        }
    }

    /**
     * Saves or updates a course entry in the catalog.
     */
    fun saveCourse(course: Course) {
        viewModelScope.launch { 
            _isProcessing.value = true
            courseDao.upsertCourse(course)
            addLog("SAVED", course.id, "Saved course: ${course.title}")
            delay(800)
            _isProcessing.value = false
            _statusMessage.value = "Course saved successfully."
        }
    }

    /**
     * Removes a course from the catalog.
     */
    fun deleteCourse(id: String) {
        viewModelScope.launch { 
            _isProcessing.value = true
            courseDao.deleteCourse(id) 
            addLog("DELETED", id, "Removed course")
            delay(800)
            _isProcessing.value = false
            _statusMessage.value = "Course deleted successfully."
        }
    }

    /**
     * Saves or updates gear/merchandise in the catalog.
     */
    fun saveGear(gear: Gear) {
        viewModelScope.launch { 
            _isProcessing.value = true
            gearDao.upsertGear(gear)
            addLog("SAVED", gear.id, "Saved gear: ${gear.title}")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Merchandise saved successfully."
        }
    }

    /**
     * Removes gear/merchandise from the catalog.
     */
    fun deleteGear(id: String) {
        viewModelScope.launch { 
            _isProcessing.value = true
            gearDao.deleteGear(id) 
            addLog("DELETED", id, "Removed gear")
            delay(500)
            _isProcessing.value = false
            _statusMessage.value = "Item removed."
        }
    }

    // --- CLASSROOM MANAGEMENT --- //

    /**
     * Retrieves modules associated with a specific course.
     */
    fun getModulesForCourse(courseId: String): Flow<List<ModuleContent>> = 
        classroomDao.getModulesForCourse(courseId)

    /**
     * Saves or updates a curriculum module.
     */
    fun saveModule(module: ModuleContent) {
        viewModelScope.launch {
            _isProcessing.value = true
            classroomDao.upsertModule(module)
            addLog("MODULE_SAVED", module.id, "Saved module: ${module.title}")
            delay(600)
            _isProcessing.value = false
            _statusMessage.value = "Module saved."
        }
    }

    /**
     * Deletes a module and its contents.
     */
    fun deleteModule(moduleId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            classroomDao.deleteModule(moduleId) 
            addLog("MODULE_DELETED", moduleId, "Deleted module")
            delay(600)
            _isProcessing.value = false
            _statusMessage.value = "Module deleted."
        }
    }

    /**
     * Retrieves assignments associated with a module.
     */
    fun getAssignmentsForModule(moduleId: String): Flow<List<Assignment>> =
        classroomDao.getAssignmentsForModule(moduleId)

    /**
     * Saves or updates a classroom assignment/task.
     */
    fun saveAssignment(assignment: Assignment) {
        viewModelScope.launch {
            _isProcessing.value = true
            classroomDao.upsertAssignment(assignment)
            addLog("TASK_SAVED", assignment.id, "Saved task: ${assignment.title}")
            delay(600)
            _isProcessing.value = false
            _statusMessage.value = "Assignment saved."
        }
    }

    /**
     * Removes an assignment from a module.
     */
    fun deleteAssignment(assignmentId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            classroomDao.deleteAssignment(assignmentId) 
            addLog("TASK_DELETED", assignmentId, "Deleted task")
            delay(600)
            _isProcessing.value = false
            _statusMessage.value = "Assignment deleted."
        }
    }

    /**
     * Sends a mass notification to users based on their role or to a single target user.
     * @param title The announcement header.
     * @param message The body content of the announcement.
     * @param targetRoles List of roles to receive the message (if not targeting a specific user).
     * @param specificUserId The ID of a single user to notify (overrides roles).
     * @param onComplete Callback invoked with the total number of users notified.
     */
    fun sendBroadcastToRoleOrUser(
        title: String, 
        message: String, 
        targetRoles: List<String>, 
        specificUserId: String?, 
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val usersToNotify = if (specificUserId != null) {
                // Single target.
                listOfNotNull(userDao.getUserById(specificUserId))
            } else {
                // Role-based target.
                val allUsersList = userDao.getAllUsersFlow().first() 
                allUsersList.filter { user ->
                    targetRoles.any { role -> role.equals(user.role.trim(), ignoreCase = true) }
                }
            }
            
            // Dispatch notifications to each target.
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
            addLog("BROADCAST", targetDesc, "Sent announcement to ${usersToNotify.size} users: $title")
            delay(1000)
            _isProcessing.value = false
            _statusMessage.value = "Broadcast sent successfully."
            onComplete(usersToNotify.size)
        }
    }

}

/**
 * Data wrapper for displaying course enrollment applications in the UI.
 * Combines the raw enrollment data with associated user and course objects.
 */
data class AdminApplicationItem(
    val details: CourseEnrollmentDetails,
    val course: Course?,
    val student: UserLocal?,
    val requestedCourse: Course? = null
)

/**
 * Factory for creating [AdminViewModel] with required DAO dependencies.
 */
class AdminViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
