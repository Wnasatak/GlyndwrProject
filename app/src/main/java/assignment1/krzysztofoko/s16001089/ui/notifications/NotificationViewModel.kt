package assignment1.krzysztofoko.s16001089.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.data.NotificationLocal
import assignment1.krzysztofoko.s16001089.data.UserDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val userDao: UserDao,
    private val repository: BookRepository,
    private val userId: String
) : ViewModel() {

    val notifications: StateFlow<List<NotificationLocal>> = userDao.getNotificationsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getRelatedBook(productId: String): Book? {
        return repository.getItemById(productId, userId)
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            userDao.markAsRead(notificationId)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            userDao.clearNotifications(userId)
        }
    }

    fun removePurchase(productId: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            // 1. Remove the item from purchases
            userDao.deletePurchase(userId, productId)
            // 2. Remove the related notification automatically
            userDao.deleteNotificationByProduct(userId, productId)
            onComplete("Removed from library")
        }
    }
}

class NotificationViewModelFactory(
    private val db: AppDatabase,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(db.userDao(), BookRepository(db), userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
