package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Students

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Attendance
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * TutorCourseAttendanceTab manages the daily attendance registry for an academic course.
 * It allows tutors to select specific dates and mark student presence/absence via an 
 * interactive roll-call interface. All actions are persisted to the local database 
 * through the TutorViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorCourseAttendanceTab(viewModel: TutorViewModel) {
    // REACTIVE STATE: Tracks the active course, enrolled students, and current attendance records
    val course by viewModel.selectedCourse.collectAsState()
    val students by viewModel.enrolledStudentsInSelectedCourse.collectAsState()
    val attendanceRecords by viewModel.selectedCourseAttendance.collectAsState()
    val selectedDate by viewModel.attendanceDate.collectAsState()
    
    // UI STATE: Manages the visibility and configuration of the Material 3 DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    // Loading State: Placeholder while course context is resolved
    if (course == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // ADAPTIVE CONTAINER: Ensures proper centering and width-capping on tablets
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
            
            // HEADER: Professional context for the attendance management view
            AdaptiveDashboardHeader(
                title = "Attendance Registry",
                subtitle = course?.title ?: "",
                icon = Icons.Default.FactCheck
            )
            
            Spacer(Modifier.height(24.dp))
            
            // DATE SELECTION: Card component that adapts its layout based on screen width
            AdaptiveDashboardCard { cardIsTablet ->
                if (cardIsTablet) {
                    // Layout for Tablet: Horizontal alignment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DateDisplayInfo(selectedDate)
                        Button(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            @Suppress("DEPRECATION")
                            Text("Change Date")
                        }
                    }
                } else {
                    // Layout for Mobile: Vertical stacked alignment
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DateDisplayInfo(selectedDate)
                        Spacer(Modifier.height(16.dp))
                        @Suppress("DEPRECATION")
                        Button(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Change Registry Date")
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // SUMMARY BAR: High-level overview of presence for the selected day
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Student Roll Call", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                val presentCount = attendanceRecords.count { it.isPresent }
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "$presentCount / ${students.size} Present",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // ROLL CALL CONTENT: Renders either an empty state or the list of interactive student items
            if (students.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No students enrolled in this course.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students) { student ->
                        // Matches student ID with their attendance status for the current date
                        val record = attendanceRecords.find { it.userId == student.id }
                        AttendanceStudentItem(
                            student = student,
                            isPresent = record?.isPresent ?: false,
                            onToggle = { isPresent -> 
                                // UPDATE LOGIC: Delegates database update to the ViewModel
                                viewModel.toggleAttendance(student.id, isPresent) 
                            }
                        )
                    }
                }
            }
        }
    }

    // DIALOG: Material 3 Date Picker for changing the attendance session date
    if (showDatePicker) {
        val datePickerColors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            headlineContentColor = MaterialTheme.colorScheme.primary,
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            todayContentColor = MaterialTheme.colorScheme.primary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary,
            dayContentColor = MaterialTheme.colorScheme.onSurface,
            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationContentColor = MaterialTheme.colorScheme.primary
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Persists the new date to the ViewModel's state
                        datePickerState.selectedDateMillis?.let { viewModel.setAttendanceDate(it) }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Confirm", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { 
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("Cancel") } 
            },
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 0.dp,
            colors = datePickerColors
        ) {
            DatePicker(
                state = datePickerState,
                colors = datePickerColors
            )
        }
    }
}

/** Helper to display the currently selected session date with institutional formatting. */
@Composable
private fun DateDisplayInfo(selectedDate: Long) {
    Column {
        Text(text = "Selected Session Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            text = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate)),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * A specialized interactive item representing a student in the roll call.
 * Features dual-action triggers for marking presence (Green) or absence (Red).
 * 
 * @param student The student identity data.
 * @param isPresent Current presence status for the active registry date.
 * @param onToggle Callback triggered when a tutor changes the presence status.
 */
@Composable
fun AttendanceStudentItem(
    student: UserLocal,
    isPresent: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        // Visual feedback: Tints the card background based on presence
        colors = CardDefaults.cardColors(
            containerColor = if (isPresent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) 
                            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp, 
            if (isPresent) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User identity avatar
            UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(40.dp))
            
            Spacer(Modifier.width(12.dp))
            
            // Textual student identity information
            @Suppress("DEPRECATION")
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = student.email, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // INTERACTIVE CONTROLS: Quick-toggle buttons for presence management
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ABSENCE TRIGGER (Red)
                IconButton(
                    onClick = { onToggle(false) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (!isPresent) Color.White else Color.Gray,
                        containerColor = if (!isPresent) MaterialTheme.colorScheme.error else Color.Transparent
                    ),
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                }
                
                Spacer(Modifier.width(8.dp))
                
                // PRESENCE TRIGGER (Green)
                IconButton(
                    onClick = { onToggle(true) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isPresent) Color.White else Color.Gray,
                        containerColor = if (isPresent) Color(0xFF4CAF50) else Color.Transparent
                    ),
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
