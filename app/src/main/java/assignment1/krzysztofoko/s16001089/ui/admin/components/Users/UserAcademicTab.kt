package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserAcademicTab(installments: List<CourseInstallment>, grades: List<Grade>, allBooks: List<Book>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { SectionHeaderDetails("Course Installments") }
        if (installments.isEmpty()) { 
            item { Text("No active plans.", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp)) } 
        } else {
            items(installments) { inst ->
                val course = allBooks.find { it.id == inst.courseId }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(course?.title ?: "Course", fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { inst.modulesPaid.toFloat() / inst.totalModules }, 
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).height(8.dp).clip(CircleShape)
                        )
                        Text("Progress: ${inst.modulesPaid} / ${inst.totalModules} Modules Paid", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item { SectionHeaderDetails("Grades & Tutor Feedback") }
        if (grades.isEmpty()) { 
            item { Text("No grades available.", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp)) } 
        } else {
            items(grades) { grade ->
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp), 
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                ) {
                    ListItem(
                        headlineContent = { Text("Score: ${grade.score}%", fontWeight = FontWeight.Black, color = if (grade.score >= 40) Color(0xFF4CAF50) else Color.Red) },
                        supportingContent = { Text(grade.feedback ?: "No feedback provided") },
                        trailingContent = { Text(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(grade.gradedAt))) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
