/**
 * ClassroomEntities.kt
 *
 * This file defines the Room entities used within the Virtual Classroom module.
 * These entities represent the data structure for course content, assessments,
 * communication, and administrative tracking.
 */

package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an educational module within a course.
 */
@Entity(tableName = "classroom_modules")
data class ModuleContent(
    @PrimaryKey val id: String, // Unique identifier for the module
    val courseId: String,       // ID of the parent course
    val title: String,          // Display title of the module
    val description: String,    // Summary of module content
    val contentType: String,    // Type of content: "VIDEO", "PDF", "QUIZ"
    val contentUrl: String,     // URL or path to the actual educational material
    val order: Int              // Sequence number for display ordering
)

/**
 * Represents an assignment or task given to students.
 */
@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey val id: String,         // Unique identifier for the assignment
    val courseId: String,               // ID of the parent course
    val moduleId: String,               // Link to a specific module (added for structure)
    val title: String,                  // Title of the task
    val description: String,            // Detailed instructions for the student
    val dueDate: Long,                  // Submission deadline timestamp
    val status: String = "PENDING",     // Current state: "PENDING", "SUBMITTED", "GRADED"
    val allowedFileTypes: String = "PDF,DOCX,ZIP" // Constraints on file uploads
)

/**
 * Represents a student's submission for a specific assignment.
 */
@Entity(tableName = "assignment_submissions")
data class AssignmentSubmission(
    @PrimaryKey val id: String,         // Unique identifier for the submission
    val assignmentId: String,           // ID of the target assignment
    val userId: String,                 // ID of the student submitting the work
    val content: String,                // Text response or cloud storage URL to the file
    val submittedAt: Long = System.currentTimeMillis() // Exact time of submission
)

/**
 * Represents the grade and feedback awarded to a student's assignment.
 */
@Entity(tableName = "grades")
data class Grade(
    @PrimaryKey val id: String,         // Unique identifier for the grade record
    val userId: String,                 // ID of the student being graded
    val courseId: String,               // ID of the course context
    val assignmentId: String,           // ID of the graded assignment
    val score: Double,                  // Numeric score or percentage
    val feedback: String?,              // Qualitative feedback from the tutor
    val gradedAt: Long = System.currentTimeMillis() // Time when the grade was finalized
)

/**
 * Tracks daily attendance for students in a particular course.
 */
@Entity(tableName = "attendance", primaryKeys = ["userId", "courseId", "date"])
data class Attendance(
    val userId: String,                 // ID of the student
    val courseId: String,               // ID of the course
    val date: Long,                     // Timestamp normalized to the day (midnight)
    val isPresent: Boolean = false      // Boolean attendance status
)

/**
 * Represents a live video broadcast or session for a course.
 */
@Entity(tableName = "live_sessions")
data class LiveSession(
    @PrimaryKey val id: String,         // Unique identifier for the session
    val courseId: String,               // ID of the associated course
    val moduleId: String = "",          // Optional module link
    val assignmentId: String? = null,   // Optional assignment context
    val title: String = "Live Broadcast", // Session title
    val tutorId: String,                // ID of the hosting tutor
    val tutorName: String,              // Display name of the host
    val startTime: Long,                // Scheduled or actual start time
    val endTime: Long? = null,          // Session conclusion time
    val streamUrl: String,              // RTMP or HLS streaming URL
    val isActive: Boolean = false       // Flag to indicate if the session is currently live
)

/**
 * Represents a private message exchanged between users in the classroom.
 */
@Entity(tableName = "classroom_messages")
data class ClassroomMessage(
    @PrimaryKey val id: String,         // Unique identifier for the message
    val courseId: String,               // Course context for the conversation
    val senderId: String,               // ID of the user who sent the message
    val receiverId: String,             // ID of the recipient
    val message: String,                // The text content of the message
    val timestamp: Long = System.currentTimeMillis(), // Time sent
    val isRead: Boolean = false         // Flag to track read status for notifications
)

/**
 * Stores professional profile information for course tutors.
 */
@Entity(tableName = "tutor_profiles")
data class TutorProfile(
    @PrimaryKey val id: String,         // Unique ID (typically matching user ID)
    val name: String,                   // Full name of the tutor
    val title: String? = null,          // Academic title (e.g., Dr., Prof.)
    val email: String,                  // Professional contact email
    val photoUrl: String?,              // URL to profile picture
    val department: String,             // Academic department
    val officeHours: String,            // Availability description
    val bio: String                     // Professional biography
)
