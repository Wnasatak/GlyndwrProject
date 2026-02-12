package assignment1.krzysztofoko.s16001089.ui.tutor.components.Students

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.People
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
import assignment1.krzysztofoko.s16001089.ui.messages.RoleTag
import java.util.*

/**
 * TutorStudentsTab is the central directory for instructors to manage and engage with students.
 * It provides a hierarchical view of learners, prioritized by those enrolled in the tutor's 
 * assigned courses, while also allowing a global search across the institutional database.
 *
 * Primary Functional Goals:
 * 1. Targeted Management: Instantly identify and access students in the tutor's direct academic care.
 * 2. Institutional Discovery: Search for any student within the university for administrative or collaborative needs.
 * 3. Integrated Communication: Quick-action triggers for initiating direct student-tutor dialogue.
 * 4. Academic Context: Real-time display of student enrollment status and shared courses.
 */
@Composable
fun TutorStudentsTab(
    viewModel: TutorViewModel
) {
    // REACTIVE DATA STREAMS: Orchestrates multiple database flows to build a comprehensive directory view.
    val allUsers by viewModel.allUsers.collectAsState()
    val allEnrollments by viewModel.allEnrollments.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    
    // UI STATE: Manages user interaction data for the search interface.
    var searchTxt by remember { mutableStateOf("") }

    // PRE-PROCESSING: Identifies the set of unique course IDs assigned to the current tutor.
    val myCourseIds = assignedCourses.map { it.id }.toSet()

    /**
     * PRIMARY STUDENT LOOKUP:
     * Logic: Traverses the enrollment registry to identify students who are officially
     * 'APPROVED' or 'ENROLLED' in courses where the current tutor is an instructor.
     */
    val myStudents = remember(allUsers, allEnrollments, myCourseIds) {
        val myStudentIds = allEnrollments
            .filter { it.courseId in myCourseIds && (it.status == "APPROVED" || it.status == "ENROLLED") }
            .map { it.userId }
            .toSet()
        allUsers.filter { it.id in myStudentIds }
    }

    /**
     * ADVANCED SEARCH & PARTITIONING ENGINE:
     * This logic divides search results into two distinct prioritized categories:
     * 1. 'MyResults': Students the tutor actively teaches who match the search criteria.
     * 2. 'OtherResults': Global students who match the criteria but are not in the tutor's classes.
     * 
     * Filtering Criteria: Matches against 'Name' or 'Email', restricted to 'student' or 'user' roles.
     */
    val (myResults, otherResults) = remember(allUsers, myStudents, searchTxt) {
        if (searchTxt.isBlank()) {
            // Default View: Show all direct students, hide global directory.
            Pair(myStudents, emptyList())
        } else {
            val searchResults = allUsers.filter {
                (it.role == "student" || it.role == "user") &&
                        (it.name.contains(searchTxt, ignoreCase = true) || it.email.contains(searchTxt, ignoreCase = true))
            }
            // PARTITION: Segregates the results to maintain the 'My Students' priority.
            searchResults.partition { it in myStudents }
        }
    }

    // ADAPTIVE CONTAINER: Centered width constraint for improved readability on tablets.
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
            Spacer(Modifier.height(12.dp))
            
            // HEADER: Professional context for the student directory view.
            AdaptiveDashboardHeader(
                title = "Student Directory",
                subtitle = "Manage and connect with learners",
                icon = Icons.Default.People
            )

            // SEARCH INTERFACE: Branded high-readability input for real-time directory filtering.
            OutlinedTextField(
                value = searchTxt,
                onValueChange = { searchTxt = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                placeholder = {
                    Text(
                        text = "Search students across university...",
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // DIRECTORY LIST: Displays results using a prioritized section hierarchy.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp) // Prevents navigation overlap.
            ) {
                // SECTION A: DIRECT STUDENTS (Highest Priority)
                if (myResults.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = if (searchTxt.isEmpty()) "Students in My Classes" else "Matches in My Classes",
                            count = myResults.size,
                            icon = Icons.Default.Group
                        )
                    }
                    items(myResults) { student ->
                        StudentItemCard(student, viewModel, allEnrollments, allCourses, myCourseIds)
                    }
                }

                // SECTION B: GLOBAL DIRECTORY (Secondary Priority)
                if (otherResults.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Global University Search",
                            count = otherResults.size,
                            icon = Icons.Default.Public
                        )
                    }
                    items(otherResults) { student ->
                        StudentItemCard(student, viewModel, allEnrollments, allCourses, myCourseIds)
                    }
                }

                // EMPTY STATE: Feedback when no matching students exist in either registry.
                if (myResults.isEmpty() && otherResults.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No students found matching your search.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

/**
 * A branded sub-header for the student directory sections.
 * Features a dynamic count badge for rapid quantification of results.
 */
@Composable
fun SectionHeader(title: String, count: Int, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        // High-contrast badge for numerical tracking
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * A sophisticated card representing an individual student record.
 * It provides critical metadata and direct communication pathways.
 *
 * Key Display Elements:
 * - Visual Identity: High-impact Avatar.
 * - Institutional Status: RoleTag (e.g. Student, User).
 * - Academic Metadata: Constructive title and full name.
 * - Course Context: Lists specific shared courses for direct tutor students.
 * - Action Trigger: Integrated chat button for immediate support initiation.
 */
@Composable
fun StudentItemCard(
    student: assignment1.krzysztofoko.s16001089.data.UserLocal,
    viewModel: TutorViewModel,
    allEnrollments: List<assignment1.krzysztofoko.s16001089.data.CourseEnrollmentDetails>,
    allCourses: List<assignment1.krzysztofoko.s16001089.data.Course>,
    myCourseIds: Set<String>
) {
    /**
     * CONTEXTUAL COURSE MAPPING:
     * Identifies and formats the shared courses between the student and tutor.
     * This provides the tutor with immediate operational context during the directory lookup.
     */
    val displayCourses = remember(allEnrollments, allCourses, myCourseIds) {
        allEnrollments
            .filter { it.userId == student.id && (it.status == "APPROVED" || it.status == "ENROLLED") && it.courseId in myCourseIds }
            .mapNotNull { enrollment -> allCourses.find { it.id == enrollment.courseId }?.title }
            .joinToString(", ")
    }

    AdaptiveDashboardCard(onClick = { 
        // NAVIGATION: Redirects to the student's full 360-degree academic profile.
        viewModel.setSection(TutorSection.STUDENT_PROFILE, student) 
    }) { isTablet ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            // IDENTITY: Visual branding via University Avatar system.
            UserAvatar(
                photoUrl = student.photoUrl,
                modifier = Modifier.size(if (isTablet) 60.dp else 54.dp)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // METADATA ROW: Displays institutional title and role classification.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!student.title.isNullOrEmpty()) {
                        Text(
                            text = student.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    RoleTag(student.role)
                }

                // NAME & CONTACT: Primary student identifiers with adaptive typography.
                Text(
                    text = student.name,
                    fontWeight = FontWeight.Black,
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(student.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                // ACADEMIC LINKAGE: Visibility restricted to shared institutional courses.
                if (displayCourses.isNotEmpty()) {
                    Text(
                        text = displayCourses,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp),
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 12.sp
                    )
                }
            }
            
            // ACTION: Fast-track trigger for one-on-one student support communication.
            IconButton(onClick = { viewModel.setSection(TutorSection.CHAT, student) }) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat, 
                    null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                )
            }
        }
    }
}
