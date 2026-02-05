package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Students

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.Attendance
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TutorClassStudentsTab(
    viewModel: TutorViewModel
) {
    val students by viewModel.enrolledStudentsInSelectedCourse.collectAsState()
    val todayAttendance by viewModel.selectedCourseAttendance.collectAsState()
    val allAttendance by viewModel.allCourseAttendance.collectAsState()
    val course by viewModel.selectedCourse.collectAsState()
    val selectedDate by viewModel.attendanceDate.collectAsState()
    val recordedDates by viewModel.recordedAttendanceDates.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var showCustomCalendar by remember { mutableStateOf(false) }

    if (showCustomCalendar) {
        CustomAttendanceCalendar(
            currentDate = selectedDate,
            recordedDates = recordedDates,
            allAttendance = allAttendance,
            allUsers = allUsers,
            onDateSelected = { 
                viewModel.setAttendanceDate(it)
                showCustomCalendar = false
            },
            onDismiss = { showCustomCalendar = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(24.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Class Attendance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = course?.title ?: "Select Course",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))

        // Date Selector Bar with Record indicator
        val hasRecordForToday = recordedDates.any { isSameDay(it, selectedDate) }

        Surface(
            onClick = { showCustomCalendar = true },
            color = if (hasRecordForToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            border = if (hasRecordForToday) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasRecordForToday) Icons.Default.AssignmentTurnedIn else Icons.Default.CalendarToday, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (hasRecordForToday) "Existing Session Log" else "New Session Date", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = if (hasRecordForToday) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        if (students.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No students are currently enrolled in this class.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(students) { student ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = selectedDate
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val targetDate = cal.timeInMillis
                    
                    val attendance = todayAttendance.find { it.userId == student.id && it.date == targetDate }
                    val isPresent = attendance?.isPresent ?: false

                    StudentAttendanceCard(
                        student = student,
                        isPresent = isPresent,
                        onTogglePresence = { present ->
                            viewModel.toggleAttendance(student.id, present)
                        },
                        onChatClick = { viewModel.setSection(TutorSection.CHAT, student) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomAttendanceCalendar(
    currentDate: Long,
    recordedDates: List<Long>,
    allAttendance: List<Attendance>,
    allUsers: List<UserLocal>,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = currentDate }) }
    var viewingDate by remember { mutableStateOf(currentDate) }
    
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    
    // Day Names
    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Session History", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(
                    SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(viewingDate)),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(20.dp))
                
                // Calendar Grid Area
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(monthName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Row {
                                IconButton(onClick = { calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) { Icon(Icons.Default.ChevronLeft, null) }
                                IconButton(onClick = { calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) { Icon(Icons.Default.ChevronRight, null) }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            dayNames.forEach { day ->
                                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                        
                        val totalSlots = 42
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(210.dp),
                            userScrollEnabled = false
                        ) {
                            items(totalSlots) { index ->
                                val dayNumber = index - firstDayOfWeek + 1
                                if (dayNumber in 1..daysInMonth) {
                                    val slotDate = (calendar.clone() as Calendar).apply { 
                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    
                                    val isSelected = isSameDay(slotDate, viewingDate)
                                    val hasRecord = recordedDates.any { isSameDay(it, slotDate) }
                                    
                                    Box(
                                        modifier = Modifier.aspectRatio(1f).padding(2.dp).clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { viewingDate = slotDate },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = dayNumber.toString(), color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                            if (hasRecord) {
                                                Box(Modifier.padding(top = 2.dp).size(width = 10.dp, height = 2.dp).background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary, CircleShape))
                                            }
                                        }
                                    }
                                } else Box(modifier = Modifier.aspectRatio(1f))
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // RECENT RECORDS PREVIEW
                Text("Log Preview", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                
                val presentStudents = remember(viewingDate, allAttendance, allUsers) {
                    allAttendance.filter { isSameDay(it.date, viewingDate) && it.isPresent }
                        .takeLast(2)
                        .mapNotNull { record -> allUsers.find { it.id == record.userId } }
                }
                
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (presentStudents.isEmpty()) {
                        Text("No attendance recorded for this day.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        presentStudents.forEach { student ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(student.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Close", fontWeight = FontWeight.Medium) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onDateSelected(viewingDate) }, 
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) { 
                        Text("Manage Log", fontWeight = FontWeight.Bold, fontSize = 13.sp) 
                    }
                }
            }
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && 
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun StudentAttendanceCard(
    student: UserLocal,
    isPresent: Boolean,
    onTogglePresence: (Boolean) -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPresent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) 
                             else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        border = if (isPresent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                photoUrl = student.photoUrl,
                modifier = Modifier.size(52.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayName = buildString {
                    if (!student.title.isNullOrEmpty()) {
                        append(student.title)
                        append(" ")
                    }
                    append(student.name)
                }
                Text(displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = student.email, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray,
                    maxLines = 1
                )
                
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPresent) "PRESENT" else "ABSENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isPresent) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    if (isPresent) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onChatClick) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = MaterialTheme.colorScheme.primary)
                }
                
                Switch(
                    checked = isPresent,
                    onCheckedChange = onTogglePresence,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}
