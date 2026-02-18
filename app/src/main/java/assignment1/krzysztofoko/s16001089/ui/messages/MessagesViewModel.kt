package assignment1.krzysztofoko.s16001089.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Data model representing a summary of a chat thread for the list view.
 * Combines the user info with the most recent message exchanged.
 */
data class ConversationPreview(
    val otherUser: UserLocal, // The person you are chatting with // Holds user details like name and avatar
    val lastMessage: ClassroomMessage // The latest message in the thread // Used to show a snippet in the list
)

/**
 * Logic handler for the Messages screen.
 * Manages chat history, contact searching, and message sending.
 */
class MessagesViewModel(
    private val userDao: UserDao, // Data access for user information
    private val classroomDao: ClassroomDao, // Data access for chat messages
    private val assignedCourseDao: AssignedCourseDao, // Data access for course assignments
    private val courseDao: CourseDao, // Data access for general course details
    private val userId: String // Current logged-in user ID // Used to filter messages sent to/from me
) : ViewModel() { // Inherits from ViewModel to survive configuration changes

    // --- STATE FLOWS ---

    // Tracks which user is currently selected for an active chat
    private val _selectedConversationUser = MutableStateFlow<UserLocal?>(null) // Private mutable state
    val selectedConversationUser: StateFlow<UserLocal?> = _selectedConversationUser.asStateFlow() // Public read-only state

    // Tracks the current search query entered by the user
    private val _searchQuery = MutableStateFlow("") // Backing property for search text
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow() // Exposed flow for the UI

    // Exposes all users from the database as a stateful list for searching
    val allUsers: StateFlow<List<UserLocal>> = userDao.getAllUsersFlow() // Streams all users from Room
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) // Converts to StateFlow

    /**
     * Calculates the list of recent conversations.
     * Logic: Gets all messages for the current user, groups them by the "other" participant,
     * and picks the most recent message for each group to create a preview.
     */
    val recentConversations: StateFlow<List<ConversationPreview>> = classroomDao.getAllMessagesForUser(userId) // Get all my messages
        .combine(userDao.getAllUsersFlow()) { messages, users -> // Combine with user data to find names/photos
            messages.groupBy { if (it.senderId == userId) it.receiverId else it.senderId } // Group by the chat partner's ID
                .mapNotNull { (otherId, msgs) -> // Process each group
                    val otherUser = users.find { it.id == otherId } // Match the ID to a User object
                    if (otherUser != null) {
                        ConversationPreview(otherUser, msgs.first()) // Create summary using the newest message
                    } else null // Skip if user not found
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) // Cache for 5 seconds after UI disconnects

    /**
     * Provides the list of messages for the currently selected conversation.
     * Logic: When the selected user changes, it triggers a fetch of the chat history.
     * It also automatically marks unread messages as 'read' upon entering the chat.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<ClassroomMessage>> = _selectedConversationUser // Listen for user selection changes
        .flatMapLatest { otherUser -> // Switch to a new data stream when the user changes
            if (otherUser == null) flowOf(emptyList()) // Return empty list if no one is selected
            else {
                // Background task to mark messages as read
                viewModelScope.launch { // Launch a coroutine for the database update
                    classroomDao.markMessagesAsRead(userId, otherUser.id) // Update 'isRead' status in Room
                }
                // Stream chat history from Room for the specific conversation
                classroomDao.getChatHistory("GENERAL", userId, otherUser.id) // Get messages between us
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) // Maintain state for the UI

    // --- FUNCTIONS ---

    /**
     * Fetches the specific shared course between the student and the tutor.
     * Logic: Compares student's purchased course IDs with the tutor's assigned courses.
     * Returns the title of the first matching course found.
     */
    fun getSharedCourse(otherUserId: String): Flow<String> = flow {
        val purchaseIds = userDao.getPurchaseIds(userId).first() // Get list of courses I bought
        assignedCourseDao.getAssignedCoursesForTutor(otherUserId).collect { assignments -> // Get courses they teach
            val sharedCourse = assignments.find { purchaseIds.contains(it.courseId) } // Find the overlap
            val title = sharedCourse?.let { courseDao.getCourseById(it.courseId)?.title } // Get the course name
            emit(title ?: "") // Send name back to UI
        }
    }

    /**
     * Legacy helper: Fetches all courses a specific tutor is responsible for.
     */
    fun getCourseForTutor(tutorId: String): Flow<String> = flow {
        assignedCourseDao.getAssignedCoursesForTutor(tutorId).collect { assignments -> // Fetch tutor's assignments
            val titles = assignments.mapNotNull { courseDao.getCourseById(it.courseId)?.title } // Map IDs to Titles
            emit(titles.joinToString(", ")) // Combine into a comma-separated string
        }
    }

    /**
     * Selects a user to start/continue a chat with.
     * If user is null, it resets the view to the main conversation list.
     */
    fun selectConversation(user: UserLocal?) {
        _selectedConversationUser.value = user // Update the selected user state
        if (user == null) {
            _searchQuery.value = "" // Clear search when returning to the list // Prevents persistent filters
        }
    }

    /**
     * Updates the global search filter.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query // Update the search text flow
    }

    /**
     * Logic for sending a message.
     * 1. Creates a Room 'ClassroomMessage' object and saves it locally.
     * 2. Automatically creates a 'Notification' for the recipient so they get an alert.
     */
    fun sendMessage(text: String) {
        val otherUser = _selectedConversationUser.value ?: return // Exit if no chat is open
        if (text.isBlank()) return // Exit if message is empty

        viewModelScope.launch { // Coroutine for database operations
            // Create and save the message
            val message = ClassroomMessage(
                id = UUID.randomUUID().toString(), // Generate unique ID
                courseId = "GENERAL", // Tag as a general message
                senderId = userId, // I am the sender
                receiverId = otherUser.id, // They are the receiver
                message = text, // The typed text
                timestamp = System.currentTimeMillis() // Current time
            )
            classroomDao.sendMessage(message) // Save to local database

            // Trigger a system notification for the recipient
            val currentUser = userDao.getUserById(userId) // Get my details for the notification
            val senderName = currentUser?.name ?: "User" // Fallback name

            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(), // Unique notification ID
                userId = otherUser.id, // Recipient's ID
                productId = "MESSAGE", // Type identifier
                title = "New Message from $senderName", // Headline
                message = "You have a new message: \"${text.take(30)}${if (text.length > 30) "..." else ""}\"", // Snippet
                timestamp = System.currentTimeMillis(), // Current time
                type = "MESSAGE" // Category for UI icons
            ))
        }
    }
}

/**
 * Factory class to instantiate the MessagesViewModel with its required dependencies.
 */
class MessagesViewModelFactory(
    private val db: AppDatabase, // Room Database instance // Passed from the UI layer
    private val userId: String   // Current user context // Passed from Firebase Auth
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T { // Factory method
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) { // Type check
            @Suppress("UNCHECKED_CAST")
            return MessagesViewModel(
                db.userDao(), // Provide User DAO
                db.classroomDao(), // Provide Chat DAO
                db.assignedCourseDao(), // Provide Assignments DAO
                db.courseDao(), // Provide Course DAO
                userId // Provide User context
            ) as T // Cast and return
        }
        throw IllegalArgumentException("Unknown ViewModel class") // Error handling for wrong types
    }
}
