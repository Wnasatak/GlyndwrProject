package assignment1.krzysztofoko.s16001089.ui.classroom.components.Performance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.data.Grade
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Modules.ClassroomEmptyState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClassroomPerformanceTab(grades: List<Grade>, assignments: List<Assignment>) {
    var selectedGrade by remember { mutableStateOf<Grade?>(null) }
    
    if (grades.isEmpty()) {
        ClassroomEmptyState(AppConstants.MSG_NO_GRADES)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(grades) { grade ->
                val title = assignments.find { it.id == grade.assignmentId }?.title ?: "Assignment"
                GradeItem(
                    grade = grade, 
                    assignmentTitle = title,
                    onClick = { selectedGrade = grade }
                )
            }
        }
    }

    selectedGrade?.let { grade ->
        val assignment = assignments.find { it.id == grade.assignmentId }
        GradeDetailDialog(
            grade = grade,
            assignment = assignment,
            onDismiss = { selectedGrade = null }
        )
    }
}

@Composable
fun GradeItem(grade: Grade, assignmentTitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignmentTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (grade.feedback != null) {
                    Text(
                        text = "Feedback: ${grade.feedback}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${grade.score.toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun GradeDetailDialog(grade: Grade, assignment: Assignment?, onDismiss: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = assignment?.title ?: "Grade Details",
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(label = "Score", value = "${grade.score}%")
                DetailRow(label = "Graded On", value = dateFormat.format(Date(grade.gradedAt)))
                
                if (grade.feedback != null) {
                    Column {
                        Text(
                            text = "Feedback",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = grade.feedback,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                assignment?.let {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Assignment Description",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = it.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
    }
}
