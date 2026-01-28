package assignment1.krzysztofoko.s16001089.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.data.NotificationLocal
import assignment1.krzysztofoko.s16001089.data.UserDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user notifications.
 * 
 * This class handles the logic for fetching, updating, and deleting notifications
 * from the local Room database. It also provides helper functions to link notifications
 * back to their associated products (Books, Courses, etc.).
 */
class NotificationViewModel(
    private val userDao: UserDao,        // Data Access Object for notification-related DB operations
    private val repository: BookRepository, // Repository to fetch detailed product metadata
    private val userId: String           // The ID of the currently logged-in user
) : ViewModel() {

    /**
     * Reactive stream of notifications for the current user.
     * 
     * Uses stateIn to convert the cold Flow from Room into a hot StateFlow that survives
     * configuration changes. It maintains the 5-second buffer (WhileSubscribed) to 
     * optimize resources during rapid navigation.
     */
    val notifications: StateFlow<List<NotificationLocal>> = userDao.getNotificationsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Fetches detailed information about a product linked to a notification.
     * Used to display product images and metadata in the notification details sheet.
     */
    suspend fun getRelatedBook(productId: String): Book? {
        return repository.getItemById(productId, userId)
    }

    /**
     * Updates the status of a notification to 'Read' in the database.
     * This will automatically update the UI count badge through the reactive notifications flow.
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            userDao.markAsRead(notificationId)
        }
    }

    /**
     * Deletes a single specific notification from the database.
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            userDao.deleteNotification(notificationId)
        }
    }

    /**
     * Performs a batch deletion of all notifications associated with the current user.
     */
    fun clearAll() {
        viewModelScope.launch {
            userDao.clearNotifications(userId)
        }
    }

    /**
     * Handles the cascading removal of a product from the user's library.
     * 
     * When a user removes a digital item:
     * 1. The purchase record is deleted.
     * 2. Any corresponding notifications for that product are also cleaned up to keep the inbox relevant.
     */
    fun removePurchase(productId: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            // 1. Remove the item from the local 'purchases' table
            userDao.deletePurchase(userId, productId)
            // 2. Automatically remove any alerts related to this product
            userDao.deleteNotificationByProduct(userId, productId)
            // 3. Callback to the UI to show a success message
            onComplete(AppConstants.MSG_REMOVED_LIBRARY)
        }
    }
}

/**
 * Factory class to create instances of NotificationViewModel.
 * Provides manual dependency injection for the Room DAO and the Shared Repository.
 */
class NotificationViewModelFactory(
    private val db: AppDatabase,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Injects the UserDao and a fresh instance of BookRepository
            return NotificationViewModel(db.userDao(), BookRepository(db), userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
