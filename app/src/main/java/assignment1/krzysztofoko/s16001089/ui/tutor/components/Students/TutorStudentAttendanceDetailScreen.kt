package assignment1.krzysztofoko.s16001089.ui.tutor.components.Students

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Attendance
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * TutorStudentAttendanceDetailScreen provides a detailed view of a specific student's 
 * attendance history for a selected course. It allows tutors to review individual 
 * records of presence or absence in a clean, chronological list.
 */
@Composable
fun TutorStudentAttendanceDetailScreen(viewModel: TutorViewModel) {
    // Reactive state collection from the TutorViewModel
    val student by viewModel.selectedStudent.collectAsState()
    val attendanceRecords by viewModel.selectedStudentAttendance.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val selectedCourseId by viewModel.selectedCourseId.collectAsState()

    // Loading/Null state handling: Ensures a student is selected before rendering the detail view
    if (student == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // Resolve the course name for the header and filter records by the selected course
    val courseName = allCourses.find { it.id == selectedCourseId }?.title ?: "Select Course"
    val filteredRecords = attendanceRecords.filter { it.courseId == selectedCourseId }.sortedByDescending { it.date }

    // Adaptive container handles centering and width constraints on larger displays (tablets)
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
            // Header component displaying the student's identity and course context
            AdaptiveDashboardHeader(
                title = "Attendance Detail",
                subtitle = "${student?.name} • $courseName",
                icon = Icons.Default.FactCheck
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Empty state handling: Displays a placeholder if no records exist for the selection
            if (filteredRecords.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No recorded attendance history for this course.", color = Color.Gray)
                }
            } else {
                // Main chronological list of attendance events
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecords) { record ->
                        AttendanceDetailItem(record)
                    }
                }
            }
        }
    }
}

/**
 * A specialized card component for displaying an individual attendance record.
 * Uses dynamic coloring and iconography to clearly distinguish between 'Present' and 'Absent' states.
 *
 * @param record The attendance data including date and status.
 */
@Composable
fun AttendanceDetailItem(record: Attendance) {
    // Determine the status color based on presence
    val statusColor = if (record.isPresent) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        // Soft background tinting for better visual categorization
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp, 
            statusColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Status Indicator (Check or Close icon)
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = statusColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (record.isPresent) Icons.Default.Check else Icons.Default.Close,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Date information formatted for high readability
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date(record.date)),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(record.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            // Textual status label (PRESENT / ABSENT)
            Text(
                text = if (record.isPresent) "PRESENT" else "ABSENT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = statusColor
            )
        }
    }
}
