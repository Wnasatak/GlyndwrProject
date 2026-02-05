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
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
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
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Needs Grading", "Graded")

    var editingStudent by remember { mutableStateOf<UserLocal?>(null) }
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var editingGrade by remember { mutableStateOf<Grade?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(24.dp))
            
            HeaderSection(
                title = "Grades & Performance",
                subtitle = course?.title ?: "Manage Student Grades",
                icon = Icons.Default.Grade
            )

            Spacer(Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (students.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No students enrolled to grade yet.", color = Color.Gray)
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

                val filteredItems = remember(allItems, selectedTabIndex) {
                    if (selectedTabIndex == 0) {
                        allItems.filter { it.third == null } 
                    } else {
                        allItems.filter { it.third != null } 
                    }
                }

                if (filteredItems.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedTabIndex == 0) "Everything is graded! 🎉" else "No graded items found.",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
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
                                        // Dynamic simulation: Try to find actual content from DB
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
private fun HeaderSection(title: String, subtitle: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isLocked) 0.5f else 0.95f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(44.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayName = buildString {
                        if (!student.title.isNullOrEmpty()) append("${student.title} ")
                        append(student.name)
                    }
                    Text(displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("Student ID: ${student.id.take(8).uppercase()}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                Surface(
                    color = if (grade != null) MaterialTheme.colorScheme.primaryContainer else statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (grade != null) "${grade.score.toInt()}%" else statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = if (grade != null) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (grade != null) MaterialTheme.colorScheme.onPrimaryContainer else statusColor
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = module?.title ?: "GENERAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Assignment, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = assignment.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(16.dp))
            Text("TUTOR FEEDBACK", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), letterSpacing = 1.sp)
            Text(text = grade?.feedback?.let { "\"$it\"" } ?: if (isLocked) "Awaiting student submission..." else "No feedback provided yet.", style = MaterialTheme.typography.bodySmall, color = if (grade != null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else Color.Gray, modifier = Modifier.padding(top = 6.dp), lineHeight = 18.sp)
            
            Spacer(Modifier.height(20.dp))
            
            if (assignment.status == "SUBMITTED" || grade != null) {
                Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Download Submission", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val dateText = if (grade != null) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(grade.gradedAt))
                               else if (assignment.status == "SUBMITTED") "Ready to grade"
                               else "Pending submission"
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(6.dp))
                    Text(text = dateText, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
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
                    TextButton(onClick = onEdit, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                        Text(if (grade == null) "Grade Now" else "Edit Grade", fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
                    }
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
    var score by remember { mutableStateOf(grade?.score?.toInt()?.toString() ?: "") }
    var feedback by remember { mutableStateOf(grade?.feedback ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("Assignment Grading", fontWeight = FontWeight.Black)
                Text("${student.name} - ${assignment.title}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Grade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
