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

/**
 * TutorCoursesTab serves as the primary gateway for tutors to access their assigned academic load.
 * It displays a categorized list of courses the tutor is currently instructing,
 * providing immediate access to the detailed management interface for each class.
 */
@Composable
fun TutorCoursesTab(
    viewModel: TutorViewModel
) {
    // REACTIVE STATE: Monitors the list of courses officially assigned to this tutor
    val assignedCourses by viewModel.assignedCourses.collectAsState()

    // ADAPTIVE CONTAINER: Centered width constraint for improved readability on tablets
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = AdaptiveSpacing.contentPadding(),
                vertical = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER SECTION: Institutional context for the current tab
            item {
                AdaptiveDashboardHeader(
                    title = "My Classes",
                    subtitle = "Academic Load Overview",
                    icon = Icons.Default.Class
                )
            }
            
            // EMPTY STATE: Displays if the tutor has no active course assignments
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
                // COURSE LIST: Renders high-impact cards for each assigned class
                items(assignedCourses) { course ->
                    AdaptiveDashboardCard(onClick = { viewModel.selectCourse(course.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Title and Department info with adaptive typography
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
                                
                                // Action Indicator: Branded badge signifying management capability
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
                            // Visual cue for navigation interaction
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

            // Standard bottom spacer to prevent clipping by system navigation elements
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
