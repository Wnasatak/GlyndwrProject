package assignment1.krzysztofoko.s16001089.ui.tutor.components.Students

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.RoleTag
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorStudentsTab(
    viewModel: TutorViewModel
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val allEnrollments by viewModel.allEnrollments.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    var searchTxt by remember { mutableStateOf("") }

    val myCourseIds = assignedCourses.map { it.id }.toSet()

    // Get all students in the tutor's courses (Approved or Enrolled)
    val myStudents = remember(allUsers, allEnrollments, myCourseIds) {
        val myStudentIds = allEnrollments
            .filter { it.courseId in myCourseIds && (it.status == "APPROVED" || it.status == "ENROLLED") }
            .map { it.userId }
            .toSet()
        allUsers.filter { it.id in myStudentIds }
    }

    val (myResults, otherResults) = remember(allUsers, myStudents, searchTxt) {
        if (searchTxt.isBlank()) {
            Pair(myStudents, emptyList())
        } else {
            val searchResults = allUsers.filter {
                (it.role == "student" || it.role == "user") &&
                        (it.name.contains(searchTxt, ignoreCase = true) || it.email.contains(searchTxt, ignoreCase = true))
            }
            searchResults.partition { it in myStudents }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
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
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

            if (myResults.isEmpty() && otherResults.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No students found matching your search.", color = Color.Gray)
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

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

@Composable
fun StudentItemCard(
    student: assignment1.krzysztofoko.s16001089.data.UserLocal,
    viewModel: TutorViewModel,
    allEnrollments: List<assignment1.krzysztofoko.s16001089.data.CourseEnrollmentDetails>,
    allCourses: List<assignment1.krzysztofoko.s16001089.data.Course>,
    myCourseIds: Set<String>
) {
    val displayCourses = remember(allEnrollments, allCourses, myCourseIds) {
        allEnrollments
            .filter { it.userId == student.id && (it.status == "APPROVED" || it.status == "ENROLLED") && it.courseId in myCourseIds }
            .mapNotNull { enrollment -> allCourses.find { it.id == enrollment.courseId }?.title }
            .joinToString(", ")
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { viewModel.setSection(TutorSection.STUDENT_PROFILE, student) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                photoUrl = student.photoUrl,
                modifier = Modifier.size(54.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
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

                Text(
                    text = student.name,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(student.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

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
            IconButton(onClick = { viewModel.setSection(TutorSection.CHAT, student) }) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
