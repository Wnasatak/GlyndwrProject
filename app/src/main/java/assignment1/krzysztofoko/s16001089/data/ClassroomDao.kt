/**
 * ClassroomDao.kt
 *
 * This Data Access Object (DAO) interface defines the operations for the Virtual Classroom module.
 * It manages modules, assignments, submissions, grades, attendance, live sessions, and messaging.
 */

package assignment1.krzysztofoko.s16001089.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassroomDao {
    
    // --- Course Modules ---

    // Retrieves all educational modules for a specific course, sorted by their display order
    @Query("SELECT * FROM classroom_modules WHERE courseId = :courseId ORDER BY `order` ASC")
    fun getModulesForCourse(courseId: String): Flow<List<ModuleContent>>

    // Bulk inserts or replaces module content
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleContent>)

    // Inserts or updates a single module
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModule(module: ModuleContent)

    // Removes a specific module by its ID
    @Query("DELETE FROM classroom_modules WHERE id = :moduleId")
    suspend fun deleteModule(moduleId: String)

    // Wipes all modules from the local database
    @Query("DELETE FROM classroom_modules")
    suspend fun deleteAllModules()

    // --- Assignments (Tasks) ---

    // Retrieves all assignments for a course, ordered by the upcoming due date
    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY dueDate ASC")
    fun getAssignmentsForCourse(courseId: String): Flow<List<Assignment>>

    // Retrieves assignments specifically linked to a module
    @Query("SELECT * FROM assignments WHERE moduleId = :moduleId ORDER BY dueDate ASC")
    fun getAssignmentsForModule(moduleId: String): Flow<List<Assignment>>

    // Retrieves every assignment across all courses
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    // Inserts or updates a single assignment
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssignment(assignment: Assignment)

    // Bulk inserts or replaces assignments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<Assignment>)

    // Updates existing assignment details
    @Update
    suspend fun updateAssignment(assignment: Assignment)

    // Deletes a specific assignment
    @Query("DELETE FROM assignments WHERE id = :assignmentId")
    suspend fun deleteAssignment(assignmentId: String)

    // Wipes all assignments
    @Query("DELETE FROM assignments")
    suspend fun deleteAllAssignments()

    // --- Submissions ---

    // Retrieves a specific user's submission for a particular assignment
    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId AND userId = :userId")
    fun getSubmission(assignmentId: String, userId: String): Flow<AssignmentSubmission?>

    // Records or updates a user's work submission
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmission)

    // --- Grades ---

    // Retrieves all grades for a specific user in a specific course
    @Query("SELECT * FROM grades WHERE userId = :userId AND courseId = :courseId")
    fun getGradesForCourse(userId: String, courseId: String): Flow<List<Grade>>

    // Retrieves all grades awarded in a course (Admin/Tutor view)
    @Query("SELECT * FROM grades WHERE courseId = :courseId")
    fun getAllGradesForCourse(courseId: String): Flow<List<Grade>>

    // Retrieves all grades for a specific user across all their courses
    @Query("SELECT * FROM grades WHERE userId = :userId")
    fun getAllGradesForUser(userId: String): Flow<List<Grade>>

    // Bulk inserts or replaces grade records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<Grade>)

    // Inserts or updates a single grade entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGrade(grade: Grade)

    // Wipes all grading records
    @Query("DELETE FROM grades")
    suspend fun deleteAllGrades()

    // --- Attendance ---

    // Retrieves attendance records for a specific course and date
    @Query("SELECT * FROM attendance WHERE courseId = :courseId AND date = :date")
    fun getAttendanceForCourseAndDate(courseId: String, date: Long): Flow<List<Attendance>>

    // Retrieves all attendance logs for a course
    @Query("SELECT * FROM attendance WHERE courseId = :courseId")
    fun getAllAttendanceForCourse(courseId: String): Flow<List<Attendance>>

    // Retrieves the complete attendance history for a specific user
    @Query("SELECT * FROM attendance WHERE userId = :userId")
    fun getAttendanceForUser(userId: String): Flow<List<Attendance>>

    // Retrieves a list of all dates where attendance was recorded for a course
    @Query("SELECT DISTINCT date FROM attendance WHERE courseId = :courseId")
    fun getRecordedAttendanceDates(courseId: String): Flow<List<Long>>

    // Marks or updates a user's attendance status
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(attendance: Attendance)

    // Clears all attendance data
    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()

    // --- Live Sessions ---

    // Retrieves the currently active streaming session for a course
    @Query("SELECT * FROM live_sessions WHERE courseId = :courseId AND isActive = 1 LIMIT 1")
    fun getActiveSession(courseId: String): Flow<LiveSession?>

    // Retrieves all active live sessions across the platform
    @Query("SELECT * FROM live_sessions WHERE isActive = 1")
    fun getAllActiveSessions(): Flow<List<LiveSession>>

    // Retrieves archived/finished sessions for a course, newest first
    @Query("SELECT * FROM live_sessions WHERE courseId = :courseId AND isActive = 0 ORDER BY startTime DESC")
    fun getPreviousSessionsForCourse(courseId: String): Flow<List<LiveSession>>

    // Bulk inserts or replaces session metadata
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveSessions(sessions: List<LiveSession>)

    // Starts or stops a live session (sets isActive flag)
    @Query("UPDATE live_sessions SET isActive = :isActive WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, isActive: Boolean)

    // Updates the display title of a session
    @Query("UPDATE live_sessions SET title = :newTitle WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, newTitle: String)

    // Removes a session record
    @Query("DELETE FROM live_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    // --- Messaging ---

    // Retrieves the private message history between a student and a tutor within a course context
    @Query("SELECT * FROM classroom_messages WHERE courseId = :courseId AND ((senderId = :userId AND receiverId = :tutorId) OR (senderId = :tutorId AND receiverId = :userId)) ORDER BY timestamp ASC")
    fun getChatHistory(courseId: String, userId: String, tutorId: String): Flow<List<ClassroomMessage>>

    // Retrieves all messages where the user is either the sender or the receiver
    @Query("SELECT * FROM classroom_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllMessagesForUser(userId: String): Flow<List<ClassroomMessage>>

    // Persists a new message to the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sendMessage(message: ClassroomMessage)

    // Marks unread messages from a specific sender as read for the current user
    @Query("UPDATE classroom_messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :senderId AND isRead = 0")
    suspend fun markMessagesAsRead(userId: String, senderId: String)

    // --- Tutor Profile ---

    // One-shot retrieval of a tutor's profile by their ID
    @Query("SELECT * FROM tutor_profiles WHERE id = :tutorId")
    suspend fun getTutorProfile(tutorId: String): TutorProfile?

    // Reactive flow of a tutor's profile data
    @Query("SELECT * FROM tutor_profiles WHERE id = :tutorId")
    fun getTutorProfileFlow(tutorId: String): Flow<TutorProfile?>

    // Inserts or updates a tutor's profile information
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTutorProfile(tutor: TutorProfile)
}
