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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.Attendance
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.*
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

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
            Spacer(Modifier.height(12.dp))

            AdaptiveDashboardHeader(
                title = "Class Attendance",
                subtitle = course?.title ?: "Select Course",
                icon = Icons.Default.Groups
            )
            
            Spacer(Modifier.height(20.dp))

            val hasRecordForToday = recordedDates.any { isSameDay(it, selectedDate) }

            Surface(
                onClick = { showCustomCalendar = true },
                color = if (hasRecordForToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
                border = if (hasRecordForToday) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(if (isTablet) 16.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasRecordForToday) Icons.Default.AssignmentTurnedIn else Icons.Default.CalendarToday, 
                            null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(if (isTablet) 24.dp else 18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (hasRecordForToday) "Existing Session Log" else "New Session Date", 
                                style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall, 
                                color = if (hasRecordForToday) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate)),
                                style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Icon(
                        Icons.Default.Edit, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (students.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = "No students are currently enrolled in this class.", 
                        style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
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
    val isTablet = isTablet()
    var calendar by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = currentDate }) }
    var viewingDate by remember { mutableStateOf(currentDate) }
    
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    
    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .adaptiveWidth(AdaptiveWidths.Standard)
        ) {
            Column(modifier = Modifier.padding(AdaptiveSpacing.medium())) {
                Text(
                    text = "Session History", 
                    style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge, 
                    color = Color.Gray
                )
                @Suppress("DEPRECATION")
                Text(
                    text = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(viewingDate)),
                    style = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(20.dp))
                
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
                            Text(
                                text = monthName, 
                                fontWeight = FontWeight.Black, 
                                style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
                            )
                            Row {
                                IconButton(onClick = { calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) { 
                                    Icon(Icons.Default.ChevronLeft, null, modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)) 
                                }
                                IconButton(onClick = { calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) { 
                                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(if (isTablet) 32.dp else 24.dp)) 
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            dayNames.forEach { day ->
                                Text(
                                    text = day, 
                                    modifier = Modifier.weight(1f), 
                                    textAlign = TextAlign.Center, 
                                    style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        val totalSlots = 42
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(if (isTablet) 300.dp else 210.dp),
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
                                            Text(
                                                text = dayNumber.toString(), 
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, 
                                                fontSize = if (isTablet) 18.sp else 13.sp
                                            )
                                            if (hasRecord) {
                                                Box(Modifier.padding(top = 2.dp).size(width = if (isTablet) 16.dp else 10.dp, height = 2.dp).background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary, CircleShape))
                                            }
                                        }
                                    }
                                } else Box(modifier = Modifier.aspectRatio(1f))
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = "Log Preview", 
                    style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold
                )
                
                val presentStudents = remember(viewingDate, allAttendance, allUsers) {
                    allAttendance.filter { isSameDay(it.date, viewingDate) && it.isPresent }
                        .takeLast(if (isTablet) 4 else 2)
                        .mapNotNull { record -> allUsers.find { it.id == record.userId } }
                }
                
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (presentStudents.isEmpty()) {
                        Text(
                            text = "No attendance recorded for this day.", 
                            style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall, 
                            color = Color.Gray
                        )
                    } else {
                        presentStudents.forEach { student ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(if (isTablet) 32.dp else 24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = student.name, 
                                    style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall, 
                                    fontWeight = FontWeight.Medium, 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(if (isTablet) 16.dp else 12.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(if (isTablet) 48.dp else 36.dp)
                    ) { 
                        Text("Close", fontWeight = FontWeight.Medium, fontSize = if (isTablet) 16.sp else 14.sp) 
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { onDateSelected(viewingDate) }, 
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = if (isTablet) 12.dp else 8.dp),
                        modifier = Modifier.height(if (isTablet) 48.dp else 40.dp)
                    ) { 
                        Text("Manage Log", fontWeight = FontWeight.Bold, fontSize = if (isTablet) 15.sp else 13.sp) 
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
    AdaptiveDashboardCard(
        backgroundColor = if (isPresent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) 
                         else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) { isTablet ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(
                photoUrl = student.photoUrl,
                modifier = Modifier.size(if (isTablet) 64.dp else 52.dp)
            )
            Spacer(Modifier.width(if (isTablet) 24.dp else 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayName = buildString {
                    if (!student.title.isNullOrEmpty()) {
                        append(student.title)
                        append(" ")
                    }
                    append(student.name)
                }
                Text(
                    text = displayName, 
                    fontWeight = FontWeight.Bold, 
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = student.email, 
                    style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall, 
                    color = Color.Gray,
                    maxLines = 1
                )
                
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPresent) "PRESENT" else "ABSENT",
                        style = if (isTablet) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isPresent) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    if (isPresent) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(if (isTablet) 18.dp else 14.dp))
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier.size(if (isTablet) 48.dp else 40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                }
                
                Switch(
                    checked = isPresent,
                    onCheckedChange = onTogglePresence,
                    modifier = if (isTablet) Modifier.scale(1.2f) else Modifier,
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
