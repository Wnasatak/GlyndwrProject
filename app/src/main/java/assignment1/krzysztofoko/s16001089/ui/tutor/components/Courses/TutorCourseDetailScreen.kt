package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

/**
 * TutorCourseDetailScreen acts as the centralized management hub for a specific academic course.
 * It provides the instructor with quick access to all administrative and educational tools
 * required to manage the class, including modules, attendance, students, and live broadcasting.
 */
@Composable
fun TutorCourseDetailScreen(
    viewModel: TutorViewModel
) {
    // REACTIVE STATE: Tracks the currently selected course from the ViewModel
    val course by viewModel.selectedCourse.collectAsState()
    
    // Loading State: Displays a progress indicator if the course data hasn't been resolved yet
    if (course == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // ADAPTIVE CONTAINER: Centered width constraint for tablets to prevent layout stretching
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = AdaptiveSpacing.contentPadding(),
                vertical = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER: Displays the course title and management context
            item {
                AdaptiveDashboardHeader(
                    title = "Course Management",
                    subtitle = course?.title ?: "",
                    icon = Icons.Default.SettingsSuggest
                )
            }

            // SECTION TITLE: Categorizes the management options
            item {
                Text(
                    text = "Academic Control",
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // MANAGEMENT OPTIONS: A vertical list of high-impact cards for different administrative tasks
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Educational Content Management
                    CourseOptionCard(
                        title = "Course Modules",
                        description = "Manage syllabus, upload PDF materials and videos.",
                        icon = Icons.Default.LibraryBooks,
                        color = Color(0xFF2196F3),
                        onClick = { viewModel.setSection(TutorSection.COURSE_MODULES) }
                    )
                    // Attendance & Registry
                    CourseOptionCard(
                        title = "Attendance Registry",
                        description = "Mark student attendance and track session records.",
                        icon = Icons.Default.FactCheck,
                        color = Color(0xFF009688),
                        onClick = { viewModel.setSection(TutorSection.COURSE_ATTENDANCE) }
                    )
                    // Enrollment Management
                    CourseOptionCard(
                        title = "Class Students",
                        description = "View and manage students enrolled in this course.",
                        icon = Icons.Default.People,
                        color = Color(0xFF4CAF50),
                        onClick = { viewModel.setSection(TutorSection.COURSE_STUDENTS) }
                    )
                    // Task & Assessment Management
                    CourseOptionCard(
                        title = "Assignments",
                        description = "Create, edit and review course assignments.",
                        icon = Icons.Default.Assignment,
                        color = Color(0xFFFF9800),
                        onClick = { viewModel.setSection(TutorSection.COURSE_ASSIGNMENTS) }
                    )
                    // Academic Performance Tracking
                    CourseOptionCard(
                        title = "Grades & Feedback",
                        description = "Grade submissions and provide student feedback.",
                        icon = Icons.Default.Grade,
                        color = Color(0xFF9C27B0),
                        onClick = { viewModel.setSection(TutorSection.COURSE_GRADES) }
                    )
                    // Synchronous Learning Tool
                    CourseOptionCard(
                        title = "Start Live Stream",
                        description = "Go live for an online interactive lesson.",
                        icon = Icons.Default.VideoCall,
                        color = Color(0xFFF44336),
                        onClick = { viewModel.setSection(TutorSection.COURSE_LIVE) }
                    )
                }
            }
            
            // Standard bottom spacer for navigation clarity
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

/**
 * A specialized navigation card used within the Course Detail screen.
 * It combines descriptive text, thematic iconography, and adaptive typography.
 *
 * @param title The primary action title.
 * @param description A brief summary of what can be managed in this section.
 * @param icon The visual representation of the management task.
 * @param color The accent color used for the icon background and tinting.
 * @param onClick Navigation callback to the specific management section.
 */
@Composable
fun CourseOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    AdaptiveDashboardCard(onClick = onClick) { isTablet ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon Container: Features a themed, low-opacity background for high contrast
            Surface(
                modifier = Modifier.size(if (isTablet) 56.dp else 48.dp),
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        null, 
                        tint = color,
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                }
            }
            Spacer(Modifier.width(if (isTablet) 24.dp else 16.dp))
            
            // Text Content: Adapts typography based on screen factor
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold, 
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description, 
                    style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall, 
                    color = Color.Gray, 
                    lineHeight = if (isTablet) 20.sp else 16.sp
                )
            }
            
            // Visual cue for interactivity
            Icon(
                Icons.Default.ChevronRight, 
                null, 
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
            )
        }
    }
}
