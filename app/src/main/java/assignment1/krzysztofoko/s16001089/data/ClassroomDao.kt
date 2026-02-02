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

    // Assignments
    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY dueDate ASC")
    fun getAssignmentsForCourse(courseId: String): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<Assignment>)

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    // Submissions
    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId AND userId = :userId")
    fun getSubmission(assignmentId: String, userId: String): Flow<AssignmentSubmission?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmission)

    // Grades
    @Query("SELECT * FROM grades WHERE userId = :userId AND courseId = :courseId")
    fun getGradesForCourse(userId: String, courseId: String): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE userId = :userId")
    fun getAllGradesForUser(userId: String): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<Grade>)

    // Live Sessions
    @Query("SELECT * FROM live_sessions WHERE courseId = :courseId AND isActive = 1 LIMIT 1")
    fun getActiveSession(courseId: String): Flow<LiveSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveSessions(sessions: List<LiveSession>)

    // Messaging
    @Query("SELECT * FROM classroom_messages WHERE courseId = :courseId AND ((senderId = :userId AND receiverId = :tutorId) OR (senderId = :tutorId AND receiverId = :userId)) ORDER BY timestamp ASC")
    fun getChatHistory(courseId: String, userId: String, tutorId: String): Flow<List<ClassroomMessage>>

    @Query("SELECT * FROM classroom_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllMessagesForUser(userId: String): Flow<List<ClassroomMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sendMessage(message: ClassroomMessage)

    // Tutor Profile
    @Query("SELECT * FROM tutor_profiles WHERE id = :tutorId")
    suspend fun getTutorProfile(tutorId: String): TutorProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTutorProfile(tutor: TutorProfile)
}
