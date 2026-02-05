package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.EnrollmentStatusBadge
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserAcademicTab(
    enrollments: List<CourseEnrollmentDetails>, 
    grades: List<Grade>, 
    allCourses: List<Course>,
    onUpdateStatus: (String, String) -> Unit
) {
    var statusToEdit by remember { mutableStateOf<CourseEnrollmentDetails?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { SectionHeaderDetails("Course Enrollments") }
        if (enrollments.isEmpty()) { 
            item { Text("No active course enrollments.", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp)) } 
        } else {
            items(enrollments) { enrollment ->
                val course = allCourses.find { it.id == enrollment.courseId }
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course?.title ?: "Unknown Course", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                                Text("Course ID: ${enrollment.courseId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            EnrollmentStatusBadge(status = enrollment.status)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        Text(
                            text = "Enrollment Date:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sdf.format(Date(enrollment.submittedAt)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = { statusToEdit = enrollment },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Change Enrollment Status", fontWeight = FontWeight.Bold)
                        }
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
                    shape = RoundedCornerShape(20.dp), 
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    ListItem(
                        headlineContent = { Text("Score: ${grade.score}%", fontWeight = FontWeight.Black, color = if (grade.score >= 40) Color(0xFF4CAF50) else Color.Red) },
                        supportingContent = { Text(grade.feedback ?: "No feedback provided", style = MaterialTheme.typography.bodyMedium) },
                        trailingContent = { Text(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(grade.gradedAt)), style = MaterialTheme.typography.labelSmall) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
        
        item { Spacer(Modifier.height(40.dp)) }
    }

    if (statusToEdit != null) {
        StatusEditDialog(
            currentStatus = statusToEdit!!.status,
            onDismiss = { statusToEdit = null },
            onConfirm = { newStatus ->
                onUpdateStatus(statusToEdit!!.id, newStatus)
                statusToEdit = null
            }
        )
    }
}

@Composable
fun StatusEditDialog(currentStatus: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val statuses = listOf(
        StatusOption("PENDING_REVIEW", "Pending Review", Icons.Default.PendingActions, Color(0xFFFBC02D)),
        StatusOption("ENROLLED", "Enrolled", Icons.Default.School, Color(0xFF673AB7)),
        StatusOption("APPROVED", "Approved", Icons.Default.CheckCircle, Color(0xFF4CAF50)),
        StatusOption("REJECTED", "Rejected", Icons.Default.Cancel, Color(0xFFF44336))
    )
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    val dialogShape = RoundedCornerShape(28.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), dialogShape),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SettingsSuggest, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Update Status", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                Text("Select the new enrollment stage", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                statuses.forEach { option ->
                    val isSelected = selectedStatus == option.id
                    val itemShape = RoundedCornerShape(16.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = option.id },
                        shape = itemShape,
                        color = if (isSelected) option.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) option.color else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isSelected) option.color else Color.Gray.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = option.label,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) option.color else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = option.color, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus) },
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Apply Status Change", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(32.dp)
            ) {
                Text("Cancel", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            }
        },
        shape = dialogShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

data class StatusOption(val id: String, val label: String, val icon: ImageVector, val color: Color)
