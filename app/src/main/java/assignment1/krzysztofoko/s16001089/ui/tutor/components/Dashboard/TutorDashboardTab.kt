package assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

@Composable
fun TutorDashboardTab(
    viewModel: TutorViewModel,
    isDarkTheme: Boolean
) {
    val tutorCourses by viewModel.tutorCourses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val pendingApps by viewModel.pendingApplications.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Welcome back, Tutor",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Here's what's happening with your courses.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TutorStatCard(
                    modifier = Modifier.weight(1f),
                    title = "My Courses",
                    value = tutorCourses.size.toString(),
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.setSection(TutorSection.MY_COURSES) }
                )
                TutorStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Students",
                    value = allStudents.size.toString(),
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { viewModel.setSection(TutorSection.STUDENTS) }
                )
            }
        }

        if (pendingApps > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationImportant, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$pendingApps Pending Applications",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Students waiting for course approval",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            TutorActionCard(
                title = "Create New Assignment",
                description = "Post a new task for your students to complete.",
                icon = Icons.Default.Assignment,
                onClick = { /* TODO */ }
            )
        }

        item {
            TutorActionCard(
                title = "Live Session",
                description = "Start a new video stream for your active course.",
                icon = Icons.Default.VideoCall,
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun TutorStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(120.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = color)
            Column {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TutorActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
