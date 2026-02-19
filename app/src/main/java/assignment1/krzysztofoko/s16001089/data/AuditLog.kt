/**
 * AuditLog.kt
 *
 * This file contains the data structures and Data Access Object (DAO) for the application's
 * system logging and auditing system. It tracks both administrative and user-level actions
 * for security and history purposes.
 */

package assignment1.krzysztofoko.s16001089.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Represents a single log entry in the system auditing trail.
 */
@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-incrementing unique ID
    val userId: String, // ID of the user who performed the action
    val userName: String, // Name of the user for quick display
    val action: String, // Description of the action performed (e.g., "Logged In")
    val targetId: String, // ID of the object affected by the action (e.g., a Course ID)
    val details: String, // Additional context or metadata about the event
    val logType: String, // Categorization: "ADMIN" or "USER"
    val timestamp: Long = System.currentTimeMillis() // Exact time the event occurred
)

/**
 * DAO for interacting with the system_logs table.
 * Provides methods for retrieving, inserting, and maintaining logs.
 */
@Dao
interface AuditDao {
    // Retrieves all logs categorized as 'ADMIN', ordered by newest first
    @Query("SELECT * FROM system_logs WHERE logType = 'ADMIN' ORDER BY timestamp DESC")
    fun getAdminLogs(): Flow<List<SystemLog>>

    // Retrieves all logs categorized as 'USER', ordered by newest first
    @Query("SELECT * FROM system_logs WHERE logType = 'USER' ORDER BY timestamp DESC")
    fun getUserLogs(): Flow<List<SystemLog>>

    // Retrieves the entire audit trail across all categories
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SystemLog>>

    /**
     * Standard insertion. Trimming is now moved to a separate maintenance task
     * to prevent UI stutters on every single log entry.
     */
    @Insert
    suspend fun insertLog(log: SystemLog) // Adds a new log entry to the database

    // Keeps only the 100 most recent admin logs to manage storage space
    @Query("DELETE FROM system_logs WHERE logType = 'ADMIN' AND id NOT IN (SELECT id FROM system_logs WHERE logType = 'ADMIN' ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimAdminLogs()

    // Keeps only the 100 most recent user logs to manage storage space
    @Query("DELETE FROM system_logs WHERE logType = 'USER' AND id NOT IN (SELECT id FROM system_logs WHERE logType = 'USER' ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimUserLogs()

    /**
     * MAINTENANCE: Consolidates both trimming operations into one transaction.
     * Should be called periodically or on app startup rather than on every insert.
     */
    @Transaction
    suspend fun performLogMaintenance() {
        trimAdminLogs() // Clean up admin entries
        trimUserLogs()  // Clean up user entries
    }

    // Deletes all logs of a specific type
    @Query("DELETE FROM system_logs WHERE logType = :type")
    suspend fun clearLogsByType(type: String)

    // Wipes the entire logging history
    @Query("DELETE FROM system_logs")
    suspend fun clearAllLogs()
}
