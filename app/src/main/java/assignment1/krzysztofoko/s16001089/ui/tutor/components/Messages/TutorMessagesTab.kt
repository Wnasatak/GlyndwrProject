package assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.QuestionAnswer
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
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.data.CourseEnrollmentDetails
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.messages.RoleTag
import assignment1.krzysztofoko.s16001089.ui.tutor.ConversationPreview
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TutorMessagesTab(
    viewModel: TutorViewModel
) {
    val conversations by viewModel.recentConversations.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allEnrollments by viewModel.allEnrollments.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    var searchTxt by remember { mutableStateOf("") }
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val searchResults = remember(conversations, allUsers, searchTxt) {
        if (searchTxt.isEmpty()) {
            conversations.map { SearchResult.Existing(it) }
        } else {
            val results = mutableListOf<SearchResult>()
            conversations.filter { 
                it.student.name.contains(searchTxt, ignoreCase = true) || 
                it.lastMessage.message.contains(searchTxt, ignoreCase = true) 
            }.forEach { results.add(SearchResult.Existing(it)) }
            
            val existingIds = conversations.map { it.student.id }.toSet()
            allUsers.filter { 
                it.id != viewModel.tutorId && 
                !existingIds.contains(it.id) && 
                it.name.contains(searchTxt, ignoreCase = true)
            }.forEach { results.add(SearchResult.New(it)) }
            results
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(24.dp))
        
        HeaderSection(
            title = "Messages",
            subtitle = "Academic Communication",
            icon = Icons.Default.QuestionAnswer
        )
        
        OutlinedTextField(
            value = searchTxt,
            onValueChange = { searchTxt = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            placeholder = { Text("Search users or messages...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        
        if (searchResults.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No results found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(searchResults) { result ->
                    when (result) {
                        is SearchResult.Existing -> {
                            val user = result.preview.student
                            val userCourses = getUserCourses(user.id, allEnrollments, allCourses)
                            ConversationItem(
                                conversation = result.preview,
                                courses = userCourses,
                                onClick = { viewModel.setSection(TutorSection.CHAT, user) },
                                timeStr = sdf.format(Date(result.preview.lastMessage.timestamp))
                            )
                        }
                        is SearchResult.New -> {
                            val userCourses = getUserCourses(result.user.id, allEnrollments, allCourses)
                            NewConversationItem(
                                user = result.user,
                                courses = userCourses,
                                onClick = { viewModel.setSection(TutorSection.CHAT, result.user) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getUserCourses(userId: String, enrollments: List<CourseEnrollmentDetails>, courses: List<Course>): String {
    return enrollments
        .filter { it.userId == userId && (it.status == "APPROVED" || it.status == "ENROLLED") }
        .mapNotNull { env -> courses.find { it.id == env.courseId }?.title }
        .joinToString(", ")
}

sealed class SearchResult {
    data class Existing(val preview: ConversationPreview) : SearchResult()
    data class New(val user: UserLocal) : SearchResult()
}

@Composable
fun ConversationItem(
    conversation: ConversationPreview,
    courses: String,
    onClick: () -> Unit,
    timeStr: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(photoUrl = conversation.student.photoUrl, modifier = Modifier.size(54.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Row 1: Name and Role Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.student.name, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    RoleTag(conversation.student.role)
                }
                
                // Row 2: Courses (if any)
                if (courses.isNotEmpty()) {
                    Text(
                        text = courses,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Row 3: Message Content and Time (Inline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = timeStr, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun NewConversationItem(
    user: UserLocal,
    courses: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(photoUrl = user.photoUrl, modifier = Modifier.size(54.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    RoleTag(user.role)
                }
                
                if (courses.isNotEmpty()) {
                    Text(
                        text = courses,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Start a new conversation...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HeaderSection(title: String, subtitle: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            @Suppress("DEPRECATION")
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            @Suppress("DEPRECATION")
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
