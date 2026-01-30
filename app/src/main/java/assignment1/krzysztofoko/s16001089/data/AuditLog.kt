package assignment1.krzysztofoko.s16001089.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val action: String, 
    val targetId: String, 
    val details: String, 
    val logType: String, // "ADMIN" or "USER"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AuditDao {
    @Query("SELECT * FROM system_logs WHERE logType = 'ADMIN' ORDER BY timestamp DESC")
    fun getAdminLogs(): Flow<List<SystemLog>>

    @Query("SELECT * FROM system_logs WHERE logType = 'USER' ORDER BY timestamp DESC")
    fun getUserLogs(): Flow<List<SystemLog>>

    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SystemLog>>

    @Insert
    suspend fun insertLogInternal(log: SystemLog)

    @Transaction
    suspend fun insertLog(log: SystemLog) {
        insertLogInternal(log)
        trimAdminLogs()
        trimUserLogs()
    }

    @Query("DELETE FROM system_logs WHERE logType = 'ADMIN' AND id NOT IN (SELECT id FROM system_logs WHERE logType = 'ADMIN' ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimAdminLogs()

    @Query("DELETE FROM system_logs WHERE logType = 'USER' AND id NOT IN (SELECT id FROM system_logs WHERE logType = 'USER' ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimUserLogs()

    @Query("DELETE FROM system_logs WHERE logType = :type")
    suspend fun clearLogsByType(type: String)

    @Query("DELETE FROM system_logs")
    suspend fun clearAllLogs()
}
