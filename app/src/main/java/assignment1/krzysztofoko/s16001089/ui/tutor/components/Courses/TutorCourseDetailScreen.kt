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

@Composable
fun TutorCourseDetailScreen(
    viewModel: TutorViewModel
) {
    val course by viewModel.selectedCourse.collectAsState()
    
    if (course == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

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
                    title = "Course Management",
                    subtitle = course?.title ?: "",
                    icon = Icons.Default.SettingsSuggest
                )
            }

            item {
                Text(
                    text = "Academic Control",
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CourseOptionCard(
                        title = "Course Modules",
                        description = "Manage syllabus, upload PDF materials and videos.",
                        icon = Icons.Default.LibraryBooks,
                        color = Color(0xFF2196F3),
                        onClick = { viewModel.setSection(TutorSection.COURSE_MODULES) }
                    )
                    CourseOptionCard(
                        title = "Attendance Registry",
                        description = "Mark student attendance and track session records.",
                        icon = Icons.Default.FactCheck,
                        color = Color(0xFF009688),
                        onClick = { viewModel.setSection(TutorSection.COURSE_ATTENDANCE) }
                    )
                    CourseOptionCard(
                        title = "Class Students",
                        description = "View and manage students enrolled in this course.",
                        icon = Icons.Default.People,
                        color = Color(0xFF4CAF50),
                        onClick = { viewModel.setSection(TutorSection.COURSE_STUDENTS) }
                    )
                    CourseOptionCard(
                        title = "Assignments",
                        description = "Create, edit and review course assignments.",
                        icon = Icons.Default.Assignment,
                        color = Color(0xFFFF9800),
                        onClick = { viewModel.setSection(TutorSection.COURSE_ASSIGNMENTS) }
                    )
                    CourseOptionCard(
                        title = "Grades & Feedback",
                        description = "Grade submissions and provide student feedback.",
                        icon = Icons.Default.Grade,
                        color = Color(0xFF9C27B0),
                        onClick = { viewModel.setSection(TutorSection.COURSE_GRADES) }
                    )
                    CourseOptionCard(
                        title = "Start Live Stream",
                        description = "Go live for an online interactive lesson.",
                        icon = Icons.Default.VideoCall,
                        color = Color(0xFFF44336),
                        onClick = { viewModel.setSection(TutorSection.COURSE_LIVE) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

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
            Icon(
                Icons.Default.ChevronRight, 
                null, 
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
            )
        }
    }
}
