package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Students

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

/**
 * TutorClassStudentsTab displays a comprehensive list of all students currently enrolled
 * in the selected academic course. It allows tutors to view high-level student information
 * and navigate to individual profiles or initiate direct communication via chat.
 */
@Composable
fun TutorClassStudentsTab(
    viewModel: TutorViewModel
) {
    // REACTIVE DATA: Synchronizes with the course and enrollment streams from the ViewModel
    val students by viewModel.enrolledStudentsInSelectedCourse.collectAsState()
    val course by viewModel.selectedCourse.collectAsState()

    // ADAPTIVE CONTAINER: Centered width constraint for improved readability on tablets and large screens
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
            
            // HEADER: Identifies the class context using institutional branding
            AdaptiveDashboardHeader(
                title = "Class Students",
                subtitle = course?.title ?: "Course Students",
                icon = Icons.Default.Groups
            )
            
            Spacer(Modifier.height(24.dp))

            // ENROLLMENT SUMMARY: Displays total student count in a high-contrast badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Enrolled Students", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer, 
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${students.size} Total",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // CONTENT DISPATCHER: Renders either an empty state placeholder or the student list
            if (students.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No students are currently enrolled in this class.", 
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // STUDENT LIST: Scrollable list of enrolled student cards
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students) { student ->
                        CourseStudentCard(
                            student = student,
                            onClick = {
                                // NAVIGATION: Redirects to the student's detailed profile
                                viewModel.setSection(TutorSection.STUDENT_PROFILE, student)
                            },
                            onChatClick = {
                                // COMMUNICATION: Opens the direct chat interface with the selected student
                                viewModel.setSection(TutorSection.CHAT, student)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A responsive card component representing a single student within the class directory.
 * It displays identity metadata (Avatar, Name, Title, Email) and quick-action triggers.
 *
 * @param student The local student data entity.
 * @param onClick Triggered when the user taps on the card to view the student profile.
 * @param onChatClick Triggered when the user initiates a private conversation.
 */
@Composable
fun CourseStudentCard(
    student: UserLocal,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    AdaptiveDashboardCard(onClick = onClick) { isTablet ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IDENTITY: User Avatar with standard university styling
            UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(48.dp))
            
            Spacer(Modifier.width(16.dp))
            
            // METADATA: Constructing and displaying the student's full name and email
            Column(modifier = Modifier.weight(1f)) {
                val displayName = buildString {
                    if (!student.title.isNullOrEmpty()) {
                        append(student.title)
                        append(" ")
                    }
                    append(student.name)
                }
                Text(
                    text = displayName, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = student.email, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // ACTIONS: Interactive triggers for secondary student management tasks
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Visual cue for primary navigation (Detail Profile)
                Icon(
                    Icons.Default.ChevronRight, 
                    null, 
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
