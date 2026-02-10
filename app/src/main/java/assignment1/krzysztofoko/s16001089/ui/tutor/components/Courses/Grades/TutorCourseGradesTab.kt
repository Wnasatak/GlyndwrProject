package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Grades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TutorCourseGradesTab(
    viewModel: TutorViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    
    val course by viewModel.selectedCourse.collectAsState()
    val courseGrades by viewModel.selectedCourseGrades.collectAsState()
    val students by viewModel.enrolledStudentsInSelectedCourse.collectAsState()
    val assignments by viewModel.selectedCourseAssignments.collectAsState()
    val modules by viewModel.selectedCourseModules.collectAsState()
    val selectedGradesTab by viewModel.selectedGradesTab.collectAsState()

    var editingStudent by remember { mutableStateOf<UserLocal?>(null) }
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var editingGrade by remember { mutableStateOf<Grade?>(null) }

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AdaptiveSpacing.contentPadding())
                ) {
                    Spacer(Modifier.height(12.dp))
                    
                    AdaptiveDashboardHeader(
                        title = "Grades & Performance",
                        subtitle = course?.title ?: "Manage Student Grades",
                        icon = Icons.Default.Grade
                    )

                    Spacer(Modifier.height(16.dp))
                }

                if (students.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(AdaptiveSpacing.contentPadding()), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No students enrolled to grade yet.", 
                            style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    val allItems = remember(students, assignments, courseGrades) {
                        students.flatMap { student ->
                            assignments.map { assignment ->
                                val grade = courseGrades.find { it.userId == student.id && it.assignmentId == assignment.id }
                                Triple(student, assignment, grade)
                            }
                        }
                    }

                    val filteredItems = remember(allItems, selectedGradesTab) {
                        if (selectedGradesTab == 0) {
                            allItems.filter { it.third == null } 
                        } else {
                            allItems.filter { it.third != null } 
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(AdaptiveSpacing.contentPadding()), contentAlignment = Alignment.Center) {
                            @Suppress("DEPRECATION")
                            Text(
                                text = if (selectedGradesTab == 0) "Everything is graded! 🎉" else "No graded items found.",
                                style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(
                                start = AdaptiveSpacing.contentPadding(),
                                end = AdaptiveSpacing.contentPadding(),
                                bottom = 100.dp
                            )
                        ) {
                            items(filteredItems) { (student, assignment, grade) ->
                                val module = modules.find { it.id == assignment.moduleId }
                                GradeItemCard(
                                    student = student,
                                    assignment = assignment,
                                    module = module,
                                    grade = grade,
                                    onDownload = {
                                        scope.launch {
                                            val submission = db.classroomDao().getSubmission(assignment.id, student.id).first()
                                            val content = submission?.content ?: "Sample Assignment Content"
                                            snackbarHostState.showSnackbar("Downloading submission: $content")
                                        }
                                    },
                                    onEdit = {
                                        if (assignment.status == "SUBMITTED" || grade != null) {
                                            editingStudent = student
                                            editingAssignment = assignment
                                            editingGrade = grade
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingStudent != null && editingAssignment != null) {
        GradeEditDialog(
            student = editingStudent!!,
            assignment = editingAssignment!!,
            grade = editingGrade,
            onDismiss = { 
                editingStudent = null
                editingAssignment = null
            },
            onSave = { score, feedback ->
                viewModel.updateGrade(editingStudent!!.id, editingAssignment!!.id, score, feedback)
                editingStudent = null
                editingAssignment = null
            }
        )
    }
}

@Composable
fun GradeItemCard(
    student: UserLocal, 
    assignment: Assignment,
    module: ModuleContent?,
    grade: Grade?, 
    onDownload: () -> Unit,
    onEdit: () -> Unit
) {
    val isLocked = assignment.status != "SUBMITTED" && grade == null
    val statusText = when {
        grade != null -> "GRADED"
        assignment.status == "SUBMITTED" -> "SUBMITTED"
        else -> "PENDING"
    }
    val statusColor = when (statusText) {
        "GRADED" -> Color(0xFF4CAF50)
        "SUBMITTED" -> Color(0xFF2196F3)
        else -> Color.Gray
    }

    AdaptiveDashboardCard(
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isLocked) 0.5f else 0.95f)
    ) { isTablet ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(if (isTablet) 56.dp else 44.dp))
            Spacer(Modifier.width(if (isTablet) 16.dp else 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayName = buildString {
                    if (!student.title.isNullOrEmpty()) append("${student.title} ")
                    append(student.name)
                }
                Text(
                    text = displayName, 
                    fontWeight = FontWeight.Bold, 
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Student ID: ${student.id.take(8).uppercase()}", 
                    style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall, 
                    color = Color.Gray
                )
            }
            
            Surface(
                color = if (grade != null) MaterialTheme.colorScheme.primaryContainer else statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (grade != null) "${grade.score.toInt()}%" else statusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (grade != null) MaterialTheme.colorScheme.onPrimaryContainer else statusColor
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isTablet) 18.dp else 14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = module?.title ?: "GENERAL", 
                style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Black, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Icon(Icons.Default.Assignment, null, tint = Color.Gray, modifier = Modifier.size(if (isTablet) 18.dp else 14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = assignment.title, 
                style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "TUTOR FEEDBACK", 
            fontWeight = FontWeight.Black, 
            style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), 
            letterSpacing = 1.sp
        )
        Text(
            text = grade?.feedback?.let { "\"$it\"" } ?: if (isLocked) "Awaiting student submission..." else "No feedback provided yet.", 
            style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall, 
            color = if (grade != null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else Color.Gray, 
            modifier = Modifier.padding(top = 6.dp), 
            lineHeight = if (isTablet) 22.sp else 18.sp
        )
        
        Spacer(Modifier.height(20.dp))
        
        if (assignment.status == "SUBMITTED" || grade != null) {
            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth().height(if (isTablet) 52.dp else 44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(if (isTablet) 22.dp else 18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Download Submission", fontWeight = FontWeight.Bold, fontSize = if (isTablet) 15.sp else 14.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            val dateText = if (grade != null) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(grade.gradedAt))
                           else if (assignment.status == "SUBMITTED") "Ready to grade"
                           else "Pending submission"
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(if (isTablet) 18.dp else 14.dp), tint = Color.Gray)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = dateText, 
                    style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall, 
                    color = Color.Gray, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (isLocked) {
                Surface(color = Color.Gray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(6.dp))
                        Text("Locked", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                TextButton(
                    onClick = onEdit, 
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(if (isTablet) 48.dp else 36.dp)
                ) {
                    Text(if (grade == null) "Grade Now" else "Edit Grade", fontSize = if (isTablet) 15.sp else 13.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(if (isTablet) 22.dp else 18.dp))
                }
            }
        }
    }
}

@Composable
fun GradeEditDialog(
    student: UserLocal,
    assignment: Assignment,
    grade: Grade?,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    val isTablet = isTablet()
    var score by remember { mutableStateOf(grade?.score?.toInt()?.toString() ?: "") }
    var feedback by remember { mutableStateOf(grade?.feedback ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .padding(AdaptiveSpacing.contentPadding())
            .adaptiveWidth(AdaptiveWidths.Standard),
        title = { 
            Column {
                Text(
                    text = "Assignment Grading", 
                    fontWeight = FontWeight.Black,
                    style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${student.name} - ${assignment.title}", 
                    style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = score,
                    onValueChange = { if (it.length <= 3) score = it.filter { c -> c.isDigit() } },
                    label = { Text("Score Percentage (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("0-100") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Tutor Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val finalScore = score.toDoubleOrNull() ?: 0.0
                    onSave(finalScore.coerceIn(0.0, 100.0), feedback) 
                },
                enabled = score.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(if (isTablet) 48.dp else 40.dp)
            ) {
                @Suppress("DEPRECATION")
                Text("Save Grade", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(if (isTablet) 48.dp else 36.dp)
            ) {
                @Suppress("DEPRECATION")
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius())
    )
}
