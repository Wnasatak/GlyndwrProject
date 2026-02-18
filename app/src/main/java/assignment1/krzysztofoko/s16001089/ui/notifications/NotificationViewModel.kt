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
 * Handles database operations for alerts, messages, and administrative requests.
 */
class NotificationViewModel(
    private val userDao: UserDao,        // DAO for notification and purchase operations
    private val repository: BookRepository, // Repository for fetching linked product metadata
    private val userId: String           // Contextual ID for the logged-in user
) : ViewModel() {

    /**
     * Continuous reactive stream of notifications for the current user.
     * Logic: Automatically updates the UI whenever the Room database changes.
     * Persistence: Remains active for 5 seconds after UI disappearance to handle rapid rotations.
     */
    val notifications: StateFlow<List<NotificationLocal>> = userDao.getNotificationsForUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Resolves the full Book/Course object associated with a notification.
     * Used to display rich media (images, titles) in the detail view.
     */
    suspend fun getRelatedBook(productId: String): Book? {
        return repository.getItemById(productId, userId)
    }

    /**
     * Updates a single notification's status to 'read' in the local database.
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            userDao.markAsRead(notificationId)
        }
    }

    /**
     * Permanently deletes a single specific notification.
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            userDao.deleteNotification(notificationId)
        }
    }

    /**
     * Bulk deletion of all notifications belonging to the current user.
     */
    fun clearAll() {
        viewModelScope.launch {
            userDao.clearNotifications(userId)
        }
    }

    /**
     * Logic for removing a resource from the user's library and cleaning up its alerts.
     * 1. Deletes the ownership record.
     * 2. Removes any notifications referencing that specific product.
     * 3. Sends a confirmation string back to the UI for Snackbar feedback.
     */
    fun removePurchase(productId: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.deletePurchase(userId, productId) // Remove ownership
            userDao.deleteNotificationByProduct(userId, productId) // Cleanup related UI alerts
            onComplete(AppConstants.MSG_REMOVED_LIBRARY) // Notify the UI
        }
    }
}

/**
 * Factory class providing manual dependency injection for the NotificationViewModel.
 */
class NotificationViewModelFactory(
    private val db: AppDatabase,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            // Provide DAOs and a transient repository instance
            return NotificationViewModel(db.userDao(), BookRepository(db), userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
