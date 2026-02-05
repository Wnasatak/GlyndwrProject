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
import assignment1.krzysztofoko.s16001089.data.CourseEnrollmentDetails
import assignment1.krzysztofoko.s16001089.data.Grade
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TutorStudentProfileScreen(viewModel: TutorViewModel) {
    val student by viewModel.selectedStudent.collectAsState()
    val enrollments by viewModel.selectedStudentEnrollments.collectAsState()
    val grades by viewModel.selectedStudentGrades.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()

    if (student == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(photoUrl = student?.photoUrl, modifier = Modifier.size(100.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = student?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = student?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.setSection(TutorSection.CHAT, student) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Message Student")
                        }
                    }
                }
            }
        }

        // Enrollment Status
        item {
            SectionTitle(title = "Course Enrollments", icon = Icons.Default.School)
        }
        
        if (enrollments.isEmpty()) {
            item { Text("No active enrollments found.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp)) }
        } else {
            items(enrollments) { enrollment ->
                val courseName = allCourses.find { it.id == enrollment.courseId }?.title ?: "Unknown Course"
                EnrollmentCard(courseName, enrollment)
            }
        }

        // Academic Performance
        item {
            SectionTitle(title = "Academic Performance", icon = Icons.Default.Assessment)
        }

        if (grades.isEmpty()) {
            item { Text("No graded assignments yet.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp)) }
        } else {
            items(grades) { grade ->
                val courseName = allCourses.find { it.id == grade.courseId }?.title ?: "Unknown Course"
                GradeCard(courseName, grade)
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EnrollmentCard(courseName: String, enrollment: CourseEnrollmentDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = courseName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Applied on: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(enrollment.submittedAt))}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
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

@Composable
fun GradeCard(courseName: String, grade: Grade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = courseName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(text = "Assignment Grade", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                if (!grade.feedback.isNullOrBlank()) {
                    Text(text = "\"${grade.feedback}\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Text(
                text = "${grade.score.toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = if (grade.score >= 40) Color(0xFF4CAF50) else Color.Red
            )
        }
    }
}
