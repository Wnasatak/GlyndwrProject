package assignment1.krzysztofoko.s16001089.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * ViewModel for the Profile Screen.
 * Enhanced to support decoupled custom theme customization and institutional requests.
 * Manages all user-related data, activity history, and account settings.
 */
class ProfileViewModel(
    private val userDao: UserDao,           // Data Access Object for user-related database tables
    private val userThemeDao: UserThemeDao, // Data Access Object for theme persistence
    private val auditDao: AuditDao,         // Data Access Object for system logs/auditing
    private val courseDao: CourseDao,       // Data Access Object for general course information
    private val classroomDao: ClassroomDao, // Data Access Object for grades and chat interactions
    private val bookRepository: BookRepository, // Repository for fetching products with owner context
    private val userId: String              // Current authenticated user ID from Firebase
) : ViewModel() {

    // Firebase Authentication instance for session and profile updates
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    /** Reactive stream of the current user's local database profile. */
    val localUser: StateFlow<UserLocal?> = userDao.getUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Reactive stream of the user's persistent theme configuration. */
    val userTheme: StateFlow<UserTheme?> = userThemeDao.getThemeFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Student Activity Data Flows (Reactive streams from database) ---

    /** Master catalog of all items (Books, Courses, Gear) with user-specific ownership flags. */
    val allBooks = bookRepository.getAllCombinedData(userId)
        .map { it ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** List of all financial invoices generated for this user. */
    val invoices = userDao.getInvoicesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Historical list of recent search queries performed by the user. */
    val searchHistory = userDao.getRecentSearches(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Collection of all reviews and ratings submitted by the user. */
    val allReviews = userDao.getReviewsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Real-time stream of the user's active academic course enrollments. */
    val courseEnrollments = userDao.getEnrollmentsForUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Audit log of past course enrollments and status changes. */
    val enrollmentHistory = userDao.getEnrollmentHistoryForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Academic marks/grades associated with the user's course progress. */
    val userGrades = classroomDao.getAllGradesForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Chronological list of wallet transactions (top-ups and product payments). */
    val transactions = userDao.getWalletHistory(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Combined flow identifying full product details for items in the user's view history. */
    val browseHistory = combine(allBooks, userDao.getHistoryIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Combined flow linking wishlist metadata with full product information. */
    val wishlist = combine(allBooks, userDao.getWishlistItems(userId)) { books, wishItems ->
        wishItems.mapNotNull { wish ->
            books.find { it.id == wish.productId }?.let { book ->
                wish to book
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** List of products that the user has previously commented on. */
    val commentedBooks = combine(allBooks, userDao.getCommentedProductIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Full details of all courses and books currently owned by the user. */
    val purchasedBooks = combine(allBooks, userDao.getPurchaseIds(userId)) { books, ids ->
        ids.mapNotNull { id -> books.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Stream of all institutional courses for reference/lookup. */
    val allCourses = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI State Variables (Mutable states for editing) ---

    var isUploading by mutableStateOf(false) // Tracks background upload operations (e.g., avatar)

    // Draft fields for the profile editor form
    var title by mutableStateOf("") // Professional title (e.g., Mr, Dr)
    var firstName by mutableStateOf("") // User's first name
    var surname by mutableStateOf("") // User's family name
    var phoneNumber by mutableStateOf("") // User's contact number
    var selectedPaymentMethod by mutableStateOf("") // User's default billing preference
    var selectedAddress by mutableStateOf("") // User's primary home/billing address

    // --- Custom Theme States (Live states for the theme builder tool) ---
    var isCustomThemeEnabled by mutableStateOf(false) // Toggles between preset and custom palettes
    var customPrimary by mutableLongStateOf(0xFFBB86FC) // Hex color code for the primary accent
    var customOnPrimary by mutableLongStateOf(0xFF000000) // Hex color code for text on primary accent
    var customPrimaryContainer by mutableLongStateOf(0xFF3700B3) // Container variant for primary color
    var customOnPrimaryContainer by mutableLongStateOf(0xFFFFFFFF) // Text on primary container
    
    var customSecondary by mutableLongStateOf(0xFF03DAC6) // Hex color code for secondary UI elements
    var customOnSecondary by mutableLongStateOf(0xFF000000) // Text on secondary color
    var customSecondaryContainer by mutableLongStateOf(0xFF018786) // Secondary container color
    var customOnSecondaryContainer by mutableLongStateOf(0xFFFFFFFF) // Text on secondary container
    
    var customTertiary by mutableLongStateOf(0xFF03DAC6) // Hex color code for tertiary highlights
    var customOnTertiary by mutableLongStateOf(0xFF000000) // Text on tertiary color
    var customTertiaryContainer by mutableLongStateOf(0xFF018786) // Tertiary container color
    var customOnTertiaryContainer by mutableLongStateOf(0xFFFFFFFF) // Text on tertiary container
    
    var customBackground by mutableLongStateOf(0xFF0F172A) // Hex color code for application background
    var customOnBackground by mutableLongStateOf(0xFFF8FAFC) // Text color on background
    var customSurface by mutableLongStateOf(0xFF1E293B) // Hex color code for cards and surfaces
    var customOnSurface by mutableLongStateOf(0xFFF8FAFC) // Text color on surfaces
    var customIsDark by mutableStateOf(true) // Flag indicating if the custom theme is "Dark" style

    /**
     * Internal utility to record user actions into the system audit trail.
     * @param action The operation performed (e.g., EDITED, LOGOUT)
     * @param details Descriptive text about the change
     */
    private fun addLog(action: String, details: String) {
        viewModelScope.launch {
            val name = localUser.value?.name ?: "Student"
            auditDao.insertLog(SystemLog(
                userId = userId,
                userName = name,
                action = action,
                targetId = "PROFILE",
                details = details,
                logType = "USER"
            ))
        }
    }

    /**
     * Populates the mutable UI fields from the database models.
     * Logic: Prevents overwriting user edits by checking if fields are currently empty.
     */
    fun initFields(user: UserLocal, theme: UserTheme?) {
        if (firstName.isEmpty() && surname.isEmpty()) {
            title = user.title ?: ""
            val names = user.name.split(" ") // Split full name for individual field editing
            firstName = names.getOrNull(0) ?: ""
            surname = names.getOrNull(1) ?: ""
            phoneNumber = user.phoneNumber ?: ""
            selectedPaymentMethod = user.selectedPaymentMethod ?: AppConstants.METHOD_UNIVERSITY_ACCOUNT
            selectedAddress = user.address ?: AppConstants.MSG_NO_ADDRESS_YET

            // Populate theme builder states from saved theme data
            theme?.let { t ->
                isCustomThemeEnabled = t.isCustomThemeEnabled
                customPrimary = t.customPrimary ?: 0xFFBB86FC
                customOnPrimary = t.customOnPrimary ?: 0xFF000000
                customPrimaryContainer = t.customPrimaryContainer ?: 0xFF3700B3
                customOnPrimaryContainer = t.customOnPrimaryContainer ?: 0xFFFFFFFF
                customSecondary = t.customSecondary ?: 0xFF03DAC6
                customOnSecondary = t.customOnSecondary ?: 0xFF000000
                customSecondaryContainer = t.customSecondaryContainer ?: 0xFF018786
                customOnSecondaryContainer = t.customOnSecondaryContainer ?: 0xFFFFFFFF
                customTertiary = t.customTertiary ?: 0xFF03DAC6
                customOnTertiary = t.customOnTertiary ?: 0xFF000000
                customTertiaryContainer = t.customTertiaryContainer ?: 0xFF018786
                customOnTertiaryContainer = t.customOnTertiaryContainer ?: 0xFFFFFFFF
                customBackground = t.customBackground ?: 0xFF0F172A
                customOnBackground = t.customOnBackground ?: 0xFFF8FAFC
                customSurface = t.customSurface ?: 0xFF1E293B
                customOnSurface = t.customOnSurface ?: 0xFFF8FAFC
                customIsDark = t.customIsDark
            }
        }
    }

    /**
     * Saves the current draft profile and theme information.
     * Logic: Synchronizes changes first with Firebase Auth, then with the local Room database.
     */
    fun updateProfile(onComplete: (String) -> Unit) {
        isUploading = true // Show visual progress
        val fullName = "$firstName $surname".trim() // Re-assemble name for sync
        
        // 1. Update Firebase Auth display name metadata
        user?.updateProfile(userProfileChangeRequest { displayName = fullName })?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 2. If Firebase succeeds, perform local database persistence
                viewModelScope.launch {
                    localUser.value?.let { u ->
                        // Persist personal account changes
                        userDao.upsertUser(u.copy(
                            name = fullName,
                            title = title.ifEmpty { null },
                            address = selectedAddress,
                            phoneNumber = phoneNumber,
                            selectedPaymentMethod = selectedPaymentMethod
                        ))

                        // Persist visual identity changes
                        userThemeDao.upsertTheme(UserTheme(
                            userId = userId,
                            isCustomThemeEnabled = isCustomThemeEnabled,
                            customPrimary = customPrimary,
                            customOnPrimary = customOnPrimary,
                            customPrimaryContainer = customPrimaryContainer,
                            customOnPrimaryContainer = customOnPrimaryContainer,
                            customSecondary = customSecondary,
                            customOnSecondary = customOnSecondary,
                            customSecondaryContainer = customSecondaryContainer,
                            customOnSecondaryContainer = customOnSecondaryContainer,
                            customTertiary = customTertiary,
                            customOnTertiary = customOnTertiary,
                            customTertiaryContainer = customTertiaryContainer,
                            customOnTertiaryContainer = customOnTertiaryContainer,
                            customBackground = customBackground,
                            customOnBackground = customOnBackground,
                            customSurface = customSurface,
                            customOnSurface = customOnSurface,
                            customIsDark = customIsDark
                        ))
                        addLog("EDITED", "User updated their profile.") // Record the audit log
                    }
                    isUploading = false // Hide progress
                    onComplete(AppConstants.MSG_PROFILE_UPDATE_SUCCESS) // Notify UI of success
                }
            } else {
                isUploading = false
                onComplete(AppConstants.MSG_PROFILE_UPDATE_FAILED) // Notify UI of failure
            }
        }
    }

    /**
     * Process for updating the user avatar image.
     * Logic: Copies image file to persistent local storage, updates user record, 
     * and synchronizes reviews so the user's image is updated on past comments.
     */
    fun uploadAvatar(context: Context, uri: Uri, onComplete: (String) -> Unit) {
        isUploading = true // Show progress indicator
        viewModelScope.launch {
            try {
                // 1. Open the content stream for the selected image from the gallery
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open stream")
                // 2. Create a permanent file in the app's private files directory
                val avatarFile = File(context.filesDir, "avatar_${userId}_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(avatarFile)
                // 3. Copy image data to the local file
                inputStream.copyTo(outputStream)
                inputStream.close(); outputStream.close()

                val localFileUri = Uri.fromFile(avatarFile).toString() // Generate local file URI
                localUser.value?.let { u ->
                    // 4. Update the user profile with the new image path
                    userDao.upsertUser(u.copy(photoUrl = localFileUri))
                    // 5. Update review metadata so old comments show the new photo
                    userDao.updateReviewAvatars(u.id, localFileUri)
                    addLog("EDITED", "User updated their profile avatar.") // Audit log
                }

                // 6. Sync the new photo URI with Firebase Auth profile
                user?.updateProfile(userProfileChangeRequest { photoUri = Uri.parse(localFileUri) })
                isUploading = false
                onComplete(AppConstants.MSG_AVATAR_UPDATE_SUCCESS) // Success feedback
            } catch (e: Exception) {
                isUploading = false
                onComplete("${AppConstants.MSG_AVATAR_UPDATE_FAILED}: ${e.message}") // Error feedback
            }
        }
    }

    /**
     * Terminates the current session and records the logout event.
     */
    fun signOut(localUser: UserLocal?) {
        addLog("USER_LOGOUT", "User signed out from profile settings.") // Log event
        auth.signOut() // Close Firebase session
    }

    // --- Institutional Requests & Admin Notifications (Academic Logic) ---

    /**
     * Formally submits a request to withdraw from an academic enrollment.
     * Logic: Updates the local enrollment status and triggers notifications for all administrators.
     */
    fun submitResignationRequest(enrollment: CourseEnrollmentDetails, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val studentName = localUser.value?.name ?: "Student"
            val courseName = allCourses.value.find { it.id == enrollment.courseId }?.title ?: "Unknown Course"

            // 1. Mark the local enrollment as pending withdrawal review
            userDao.requestWithdrawal(enrollment.id, "PENDING_REVIEW")

            // 2. Log the academic event for auditing
            addLog("RESIGNATION_REQUESTED", "Student requested to withdraw from $courseName.")

            // 3. Alert all administrators via in-app notifications
            userDao.getAllUsersFlow().first().filter { it.role == "admin" }.forEach { admin ->
                userDao.addNotification(NotificationLocal(
                    id = UUID.randomUUID().toString(),
                    userId = admin.id, // Recipient
                    productId = userId, // Subject student ID
                    title = "Withdrawal Request",
                    message = "$studentName has applied to withdraw from $courseName.",
                    type = "ACADEMIC_REQUEST" // Special icon category
                ))
            }
            onComplete("Withdrawal request submitted to administration.")
        }
    }

    /**
     * Formally submits a request to transfer from one course to another.
     * Logic: Records the intended target course and alerts the administration for review.
     */
    fun submitCourseChangeRequest(enrollment: CourseEnrollmentDetails, newCourseId: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val studentName = localUser.value?.name ?: "Student"
            val oldCourseName = allCourses.value.find { it.id == enrollment.courseId }?.title ?: "Unknown Course"
            val newCourseName = allCourses.value.find { it.id == newCourseId }?.title ?: "Unknown Course"

            // 1. Mark enrollment as pending review and record the requested target course ID
            userDao.requestCourseChange(enrollment.id, "PENDING_REVIEW", newCourseId)

            // 2. Log the request event
            addLog("COURSE_CHANGE_REQUESTED", "Student requested to change from $oldCourseName to $newCourseName.")

            // 3. Notify all system administrators
            userDao.getAllUsersFlow().first().filter { it.role == "admin" }.forEach { admin ->
                userDao.addNotification(NotificationLocal(
                    id = UUID.randomUUID().toString(),
                    userId = admin.id,
                    productId = userId, // Subject student ID
                    title = "Course Change Request",
                    message = "$studentName wants to change from $oldCourseName to $newCourseName.",
                    type = "ACADEMIC_REQUEST"
                ))
            }
            onComplete("Course change request submitted for $newCourseName.")
        }
    }
}

/**
 * Factory class providing necessary dependencies to instantiate the ProfileViewModel.
 */
class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val userThemeDao: UserThemeDao,
    private val auditDao: AuditDao,
    private val courseDao: CourseDao,
    private val classroomDao: ClassroomDao,
    private val bookRepository: BookRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        // Validation and creation of the VM with required DAOs
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userDao, userThemeDao, auditDao, courseDao, classroomDao, bookRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
