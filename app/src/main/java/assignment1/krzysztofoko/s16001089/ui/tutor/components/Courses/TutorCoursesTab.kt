package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Class
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

@Composable
fun TutorCoursesTab(
    viewModel: TutorViewModel
) {
    val assignedCourses by viewModel.assignedCourses.collectAsState()

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = AdaptiveSpacing.contentPadding(),
                vertical = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AdaptiveDashboardHeader(
                    title = "My Classes",
                    subtitle = "Academic Load Overview",
                    icon = Icons.Default.Class
                )
            }
            
            if (assignedCourses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.School, 
                                null, 
                                modifier = Modifier.size(if (isTablet) 80.dp else 64.dp), 
                                tint = Color.Gray.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("No assigned courses", color = Color.Gray)
                            Text(
                                "You haven't been assigned to any courses yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(assignedCourses) { course ->
                    AdaptiveDashboardCard(onClick = { viewModel.selectCourse(course.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course.title, 
                                    fontWeight = FontWeight.Black, 
                                    style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = course.department, 
                                    style = if (isTablet) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium, 
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "MANAGE CLASS",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight, 
                                null, 
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
