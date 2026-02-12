package assignment1.krzysztofoko.s16001089.ui.tutor.components.Students

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Attendance
import assignment1.krzysztofoko.s16001089.data.CourseEnrollmentDetails
import assignment1.krzysztofoko.s16001089.data.Grade
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * TutorStudentProfileScreen provides a centralized 360-degree academic view of a single student.
 * It aggregates data from multiple sources (Enrollments, Grades, Attendance) to provide tutors
 * with a comprehensive picture of student progress and engagement.
 *
 * Key Features:
 * 1. Holistic Identity: High-impact profile header with direct messaging integration.
 * 2. Status Tracking: Detailed overview of course applications and current enrollment states.
 * 3. Behavioral Analytics: Visualized attendance summaries with trend indicators (Progress Rings).
 * 4. Performance History: Chronological list of graded assignments with qualitative feedback.
 * 5. Adaptive Architecture: Optimized for tablet viewing with multi-column card support.
 */
@Composable
fun TutorStudentProfileScreen(viewModel: TutorViewModel) {
    // REACTIVE DATA JOIN: Synchronizes the student's entire academic record from the ViewModel
    val student by viewModel.selectedStudent.collectAsState()
    val enrollments by viewModel.selectedStudentEnrollments.collectAsState()
    val grades by viewModel.selectedStudentGrades.collectAsState()
    val attendanceRecords by viewModel.selectedStudentAttendance.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()

    // Loading State Handling
    if (student == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // ADAPTIVE CONTAINER: Centered width constraint for improved readability on tablets
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- SECTION 1: STUDENT IDENTITY & PRIMARY ACTION ---
            item {
                AdaptiveDashboardCard { cardIsTablet ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(photoUrl = student?.photoUrl, modifier = Modifier.size(if (cardIsTablet) 120.dp else 100.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = student?.name ?: "",
                            style = if (cardIsTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(text = student?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Interaction Point: Initiates a direct tutor-student chat
                        Button(
                            onClick = { viewModel.setSection(TutorSection.CHAT, student) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = if (cardIsTablet) Modifier.adaptiveButtonWidth() else Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Message Student", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- SECTION 2: ENROLLMENT LIFECYCLE ---
            item { SectionTitle(title = "Course Enrollments", icon = Icons.Default.School) }
            
            if (enrollments.isEmpty()) {
                item { Text(text = "No active enrollments found.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(enrollments) { enrollment ->
                    val courseName = allCourses.find { it.id == enrollment.courseId }?.title ?: "Unknown Course"
                    EnrollmentCard(courseName, enrollment)
                }
            }

            // --- SECTION 3: ATTENDANCE ANALYTICS ---
            // Groups individual presence records into a high-level course-by-course summary
            item { SectionTitle(title = "Attendance Analytics", icon = Icons.Default.FactCheck) }

            if (attendanceRecords.isEmpty()) {
                item { Text(text = "Pending recorded sessions.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium) }
            } else {
                val groupedAttendance = attendanceRecords.groupBy { it.courseId }
                items(groupedAttendance.keys.toList()) { courseId ->
                    val courseName = allCourses.find { it.id == courseId }?.title ?: "Unknown Course"
                    val records = groupedAttendance[courseId] ?: emptyList()
                    AttendanceSummaryCard(courseName, records, onClick = {
                        // DRILL-DOWN: Navigates to the detailed daily registry for this specific student
                        viewModel.updateSelectedCourse(courseId)
                        viewModel.setSection(TutorSection.INDIVIDUAL_ATTENDANCE_DETAIL)
                    })
                }
            }

            // --- SECTION 4: ACADEMIC PERFORMANCE HUB ---
            item { SectionTitle(title = "Academic Performance", icon = Icons.Default.Assessment) }

            if (grades.isEmpty()) {
                item { Text(text = "No graded assignments yet.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(grades) { grade ->
                    val courseName = allCourses.find { it.id == grade.courseId }?.title ?: "Unknown Course"
                    GradeCard(courseName, grade)
                }
            }
            
            // Standard bottom spacer for navigation clarity
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/**
 * A sophisticated visualization card for student attendance data.
 * Features a dynamic progress ring that changes color based on the attendance percentage.
 */
@Composable
fun AttendanceSummaryCard(courseName: String, records: List<Attendance>, onClick: () -> Unit) {
    val totalSessions = records.size
    val presentCount = records.count { it.isPresent }
    val percentage = if (totalSessions > 0) (presentCount.toFloat() / totalSessions) * 100 else 0f
    
    AdaptiveDashboardCard(onClick = onClick) { isTablet ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = courseName, fontWeight = FontWeight.Bold, style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
                Text(text = "$presentCount / $totalSessions Sessions Present", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                
                Spacer(Modifier.height(12.dp))
                
                // AUDIT TRAIL: Displays the most recent session dates for quick context
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    val recent = records.sortedByDescending { it.date }.take(3).map { 
                        SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(it.date)) 
                    }
                    Text(text = "Recent: ${recent.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                }
            }
            
            // PROGRESS RING: High-contrast visual metric for attendance
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isTablet) 72.dp else 64.dp)) {
                val color = when {
                    percentage >= 75f -> Color(0xFF4CAF50) // Green: High engagement
                    percentage >= 50f -> Color(0xFFFFC107) // Yellow: At risk
                    else -> Color(0xFFF44336)              // Red: Critical low attendance
                }
                
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f),
                    strokeWidth = 6.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Text(text = "${percentage.toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = color)
            }
        }
    }
}

/** Branded section title with institutional iconography. */
@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Visual card representing a student's status in a specific course (Approved, Pending, Rejected).
 */
@Composable
fun EnrollmentCard(courseName: String, enrollment: CourseEnrollmentDetails) {
    AdaptiveDashboardCard { isTablet ->
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = courseName, fontWeight = FontWeight.Bold, style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
                Text(text = "Applied on: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(enrollment.submittedAt))}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            // STATUS BADGE: Dynamic coloring based on the administrative lifecycle
            Surface(
                color = when(enrollment.status) {
                    "APPROVED" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    "PENDING_REVIEW" -> Color(0xFFFFC107).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = enrollment.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = when(enrollment.status) {
                        "APPROVED" -> Color(0xFF4CAF50)
                        "PENDING_REVIEW" -> Color(0xFFFFC107)
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

/**
 * Performance card displaying an assignment score and the tutor's qualitative remarks.
 */
@Composable
fun GradeCard(courseName: String, grade: Grade) {
    AdaptiveDashboardCard { isTablet ->
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = courseName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(text = "Assignment Grade", fontWeight = FontWeight.Bold, style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium)
                // FEEDBACK SNIPPET: Qualitative comments provided during grading
                if (!grade.feedback.isNullOrBlank()) {
                    Text(text = "\"${grade.feedback}\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }
            // GRADE BADGE: Circular high-contrast score indicator
            Surface(
                color = (if (grade.score >= 40) Color(0xFF4CAF50) else Color.Red).copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(if (isTablet) 64.dp else 56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "${grade.score.toInt()}%", style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (grade.score >= 40) Color(0xFF4CAF50) else Color.Red)
                }
            }
        }
    }
}

/**
 * FUTURE IMPLEMENTATION:
 * 
 * 1. Behavioral Insights: 
 *    - Analytics to track student submission trends over time.
 *    - Required for: Identifying students who may need academic intervention.
 * 
 * 2. Tutor-Private Notes: 
 *    - A secure text area for tutors to record observations that are not visible to the student.
 * 
 * 3. Document Archive: 
 *    - One-click access to all previous assignment submissions by the student.
 */
