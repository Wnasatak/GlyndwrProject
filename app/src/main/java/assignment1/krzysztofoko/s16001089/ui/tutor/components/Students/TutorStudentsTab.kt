package assignment1.krzysztofoko.s16001089.ui.tutor.components.Students

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

    // Logic:
    // 1. Get IDs of courses assigned to THIS tutor.
    // 2. Find all student IDs enrolled in those courses.
    // 3. Separate list into "My Students" and "Other Students" (if searching).
    
    val myCourseIds = assignedCourses.map { it.id }.toSet()
    val myStudentIds = remember(allEnrollments, myCourseIds) {
        allEnrollments
            .filter { it.courseId in myCourseIds && it.status == "APPROVED" }
            .map { it.userId }
            .toSet()
    }

    val studentList = remember(allUsers, searchTxt, myStudentIds) {
        val filtered = allUsers.filter { (it.role == "student" || it.role == "user") }
        
        if (searchTxt.isEmpty()) {
            // Default view: ONLY my students
            filtered.filter { it.id in myStudentIds }
        } else {
            // Search view: Show matching students from everywhere
            filtered.filter { 
                it.name.contains(searchTxt, ignoreCase = true) || 
                it.email.contains(searchTxt, ignoreCase = true) 
            }
        }
    }

    // Split for visual separation if searching
    val (myResults, otherResults) = remember(studentList, myStudentIds, searchTxt) {
        if (searchTxt.isEmpty()) {
            Pair(studentList, emptyList())
        } else {
            studentList.partition { it.id in myStudentIds }
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
                    StudentItemCard(student, viewModel, allEnrollments, allCourses)
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
                    StudentItemCard(student, viewModel, allEnrollments, allCourses)
                }
            }
            
            if (studentList.isEmpty()) {
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
    allCourses: List<assignment1.krzysztofoko.s16001089.data.Course>
) {
    val studentCourses = remember(allEnrollments, allCourses) {
        allEnrollments
            .filter { it.userId == student.id && it.status == "APPROVED" }
            .mapNotNull { enrollment -> allCourses.find { it.id == enrollment.courseId }?.title }
            .joinToString(", ")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                photoUrl = student.photoUrl,
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayName = buildString {
                        if (!student.title.isNullOrEmpty()) {
                            append(student.title)
                            append(" ")
                        }
                        append(student.name)
                    }
                    Text(displayName, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    RoleTag(student.role)
                }
                Text(student.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                if (studentCourses.isNotEmpty()) {
                    Text(
                        text = studentCourses,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
            IconButton(onClick = { viewModel.setSection(TutorSection.CHAT, student) }) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
