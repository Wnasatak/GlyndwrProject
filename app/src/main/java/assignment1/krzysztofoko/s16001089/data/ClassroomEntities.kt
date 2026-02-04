package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classroom_modules")
data class ModuleContent(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val contentType: String, // "VIDEO", "PDF", "QUIZ"
    val contentUrl: String,
    val order: Int
)

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey val id: String,
    val courseId: String,
    val moduleId: String, // Added moduleId to link assignments to modules
    val title: String,
    val description: String,
    val dueDate: Long,
    val status: String = "PENDING", // "PENDING", "SUBMITTED", "GRADED"
    val allowedFileTypes: String = "PDF,DOCX,ZIP" // Comma separated allowed file types
)

@Entity(tableName = "assignment_submissions")
data class AssignmentSubmission(
    @PrimaryKey val id: String,
    val assignmentId: String,
    val userId: String,
    val content: String, // Text response or URL to a file in cloud storage
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "grades")
data class Grade(
    @PrimaryKey val id: String,
    val userId: String,
    val courseId: String,
    val assignmentId: String,
    val score: Double,
    val feedback: String?,
    val gradedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance", primaryKeys = ["userId", "courseId", "date"])
data class Attendance(
    val userId: String,
    val courseId: String,
    val date: Long, // timestamp for the day (midnight)
    val isPresent: Boolean = false
)

@Entity(tableName = "live_sessions")
data class LiveSession(
    @PrimaryKey val id: String,
    val courseId: String,
    val moduleId: String = "",
    val assignmentId: String? = null,
    val title: String = "Live Broadcast",
    val tutorId: String,
    val tutorName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val streamUrl: String,
    val isActive: Boolean = false
)

@Entity(tableName = "classroom_messages")
data class ClassroomMessage(
    @PrimaryKey val id: String,
    val courseId: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "tutor_profiles")
data class TutorProfile(
    @PrimaryKey val id: String,
    val name: String,
    val title: String? = null,
    val email: String,
    val photoUrl: String?,
    val department: String,
    val officeHours: String,
    val bio: String
)
