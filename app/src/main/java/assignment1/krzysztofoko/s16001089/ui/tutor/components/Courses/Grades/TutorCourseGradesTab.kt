package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Grades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.data.Grade
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TutorCourseGradesTab(
    viewModel: TutorViewModel
) {
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

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Grades & Student Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = course?.title ?: "Class",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Tab Row for filtering
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
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
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (students.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No students enrolled to grade yet.", color = Color.Gray)
            }
        } else {
            // Flatten data into pairs of Student + Assignment for easier filtering
            val allItems = remember(students, assignments, courseGrades) {
                students.flatMap { student ->
                    assignments.map { assignment ->
                        val grade = courseGrades.find { it.userId == student.id && it.assignmentId == assignment.id }
                        Triple(student, assignment, grade)
                    }
                }
            }

            // Filter items based on tab
            val filteredItems = remember(allItems, selectedTabIndex) {
                if (selectedTabIndex == 0) {
                    allItems.filter { it.third == null } // Needs Grading
                } else {
                    allItems.filter { it.third != null } // Graded
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredItems) { (student, assignment, grade) ->
                        val module = modules.find { it.id == assignment.moduleId }
                        GradeItemCard(
                            student = student,
                            assignment = assignment,
                            module = module,
                            grade = grade,
                            onEdit = {
                                editingStudent = student
                                editingAssignment = assignment
                                editingGrade = grade
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
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
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayName = buildString {
                        if (!student.title.isNullOrEmpty()) append("${student.title} ")
                        append(student.name)
                    }
                    Text(displayName, fontWeight = FontWeight.Bold)
                    Text("Student ID: ${student.id.take(8).uppercase()}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                if (grade != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${grade.score.toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PENDING",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))
            
            // Module and Assignment Info
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = module?.title ?: "Unknown Module",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Default.Assignment, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("Latest Feedback", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            
            Text(
                text = grade?.feedback?.let { "\"$it\"" } ?: "No feedback provided yet.",
                style = MaterialTheme.typography.bodySmall,
                color = if (grade != null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                lineHeight = 18.sp
            )
            
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateText = if (grade != null) {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(grade.gradedAt))
                } else {
                    "Not yet graded"
                }
                
                Text(
                    text = "Status: $dateText",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                
                TextButton(onClick = onEdit, contentPadding = PaddingValues(0.dp)) {
                    Text(if (grade == null) "Grade Now" else "Edit Grade", fontSize = 12.sp)
                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
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
                Text("Update Performance", fontWeight = FontWeight.Black)
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
                    label = { Text("Pedagogical Feedback") },
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
