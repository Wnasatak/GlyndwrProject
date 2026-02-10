package assignment1.krzysztofoko.s16001089.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassroomDao {
    // Modules
    @Query("SELECT * FROM classroom_modules WHERE courseId = :courseId ORDER BY `order` ASC")
    fun getModulesForCourse(courseId: String): Flow<List<ModuleContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleContent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModule(module: ModuleContent)

    @Query("DELETE FROM classroom_modules WHERE id = :moduleId")
    suspend fun deleteModule(moduleId: String)

    @Query("DELETE FROM classroom_modules")
    suspend fun deleteAllModules()

    // Assignments (Tasks)
    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY dueDate ASC")
    fun getAssignmentsForCourse(courseId: String): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE moduleId = :moduleId ORDER BY dueDate ASC")
    fun getAssignmentsForModule(moduleId: String): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssignment(assignment: Assignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<Assignment>)

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Query("DELETE FROM assignments WHERE id = :assignmentId")
    suspend fun deleteAssignment(assignmentId: String)

    @Query("DELETE FROM assignments")
    suspend fun deleteAllAssignments()

    // Submissions
    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId AND userId = :userId")
    fun getSubmission(assignmentId: String, userId: String): Flow<AssignmentSubmission?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmission)

    // Grades
    @Query("SELECT * FROM grades WHERE userId = :userId AND courseId = :courseId")
    fun getGradesForCourse(userId: String, courseId: String): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE courseId = :courseId")
    fun getAllGradesForCourse(courseId: String): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE userId = :userId")
    fun getAllGradesForUser(userId: String): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<Grade>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGrade(grade: Grade)

    @Query("DELETE FROM grades")
    suspend fun deleteAllGrades()

    // Attendance
    @Query("SELECT * FROM attendance WHERE courseId = :courseId AND date = :date")
    fun getAttendanceForCourseAndDate(courseId: String, date: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE courseId = :courseId")
    fun getAllAttendanceForCourse(courseId: String): Flow<List<Attendance>>

    @Query("SELECT DISTINCT date FROM attendance WHERE courseId = :courseId")
    fun getRecordedAttendanceDates(courseId: String): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()

    // Live Sessions
    @Query("SELECT * FROM live_sessions WHERE courseId = :courseId AND isActive = 1 LIMIT 1")
    fun getActiveSession(courseId: String): Flow<LiveSession?>

    @Query("SELECT * FROM live_sessions WHERE isActive = 1")
    fun getAllActiveSessions(): Flow<List<LiveSession>>

    @Query("SELECT * FROM live_sessions WHERE courseId = :courseId AND isActive = 0 ORDER BY startTime DESC")
    fun getPreviousSessionsForCourse(courseId: String): Flow<List<LiveSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveSessions(sessions: List<LiveSession>)

    @Query("UPDATE live_sessions SET isActive = :isActive WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, isActive: Boolean)

    @Query("UPDATE live_sessions SET title = :newTitle WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, newTitle: String)

    @Query("DELETE FROM live_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    // Messaging
    @Query("SELECT * FROM classroom_messages WHERE courseId = :courseId AND ((senderId = :userId AND receiverId = :tutorId) OR (senderId = :tutorId AND receiverId = :userId)) ORDER BY timestamp ASC")
    fun getChatHistory(courseId: String, userId: String, tutorId: String): Flow<List<ClassroomMessage>>

    @Query("SELECT * FROM classroom_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllMessagesForUser(userId: String): Flow<List<ClassroomMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sendMessage(message: ClassroomMessage)

    @Query("UPDATE classroom_messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :senderId AND isRead = 0")
    suspend fun markMessagesAsRead(userId: String, senderId: String)

    // Tutor Profile
    @Query("SELECT * FROM tutor_profiles WHERE id = :tutorId")
    suspend fun getTutorProfile(tutorId: String): TutorProfile?

    @Query("SELECT * FROM tutor_profiles WHERE id = :tutorId")
    fun getTutorProfileFlow(tutorId: String): Flow<TutorProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTutorProfile(tutor: TutorProfile)
}
