package assignment1.krzysztofoko.s16001089.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ConversationPreview(
    val otherUser: UserLocal,
    val lastMessage: ClassroomMessage
)

class MessagesViewModel(
    private val userDao: UserDao,
    private val classroomDao: ClassroomDao,
    private val userId: String
) : ViewModel() {

    private val _selectedConversationUser = MutableStateFlow<UserLocal?>(null)
    val selectedConversationUser: StateFlow<UserLocal?> = _selectedConversationUser.asStateFlow()

    val recentConversations: StateFlow<List<ConversationPreview>> = classroomDao.getAllMessagesForUser(userId)
        .combine(userDao.getAllUsersFlow()) { messages, users ->
            messages.groupBy { if (it.senderId == userId) it.receiverId else it.senderId }
                .mapNotNull { (otherId, msgs) ->
                    val otherUser = users.find { it.id == otherId }
                    if (otherUser != null) {
                        ConversationPreview(otherUser, msgs.first())
                    } else null
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<ClassroomMessage>> = _selectedConversationUser
        .flatMapLatest { otherUser ->
            if (otherUser == null) flowOf(emptyList())
            else classroomDao.getChatHistory("GENERAL", userId, otherUser.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectConversation(user: UserLocal?) {
        _selectedConversationUser.value = user
    }

    fun sendMessage(text: String) {
        val otherUser = _selectedConversationUser.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val message = ClassroomMessage(
                id = UUID.randomUUID().toString(),
                courseId = "GENERAL",
                senderId = userId,
                receiverId = otherUser.id,
                message = text,
                timestamp = System.currentTimeMillis()
            )
            classroomDao.sendMessage(message)
        }
    }
}

class MessagesViewModelFactory(
    private val db: AppDatabase,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessagesViewModel(db.userDao(), db.classroomDao(), userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
