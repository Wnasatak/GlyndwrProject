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
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.messages.RoleTag
import assignment1.krzysztofoko.s16001089.ui.tutor.ConversationPreview
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * TutorMessagesTab acts as the primary communications inbox for course instructors.
 * It synthesizes various data sources to provide a categorized view of active conversations 
 * and searchable potential student contacts.
 *
 * Key Features:
 * 1. Hybrid Search Engine: Real-time filtering that identifies both active conversations 
 *    (existing threads) and new contacts (directory lookup).
 * 2. Academic Contextualization: Displays the courses shared between the tutor and the student 
 *    directly within the conversation preview.
 * 3. Smart Temporal Formatting: Dynamically switches between 'Time-only' (Today) and 
 *    'Date-Time' (Historical) formats for message timestamps.
 * 4. Adaptive Directory: Responsive card layout that scales information density for tablets.
 * 5. Role Awareness: Integrates RoleTags to clearly distinguish between student and faculty peers.
 */
@Composable
fun TutorMessagesTab(
    viewModel: TutorViewModel
) {
    // REACTIVE STATE: Synchronizes with active conversations, student directory, and enrollment load
    val conversations by viewModel.recentConversations.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allEnrollments by viewModel.allEnrollments.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    
    // UI STATE: Manages the active search query
    var searchTxt by remember { mutableStateOf("") }
    
    // TEMPORAL UTILITIES: Formatter definitions for the messaging timeline
    val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateSdf = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()) }

    /**
     * UNIFIED SEARCH LOGIC: 
     * Joins active 'ConversationPreview' objects with global 'UserLocal' records.
     * Ensures the directory results do not duplicate existing conversation threads.
     */
    val searchResults = remember(conversations, allUsers, searchTxt) {
        if (searchTxt.isEmpty()) {
            // Default view: Show all active conversation threads
            conversations.map { SearchResult.Existing(it) }
        } else {
            val results = mutableListOf<SearchResult>()
            
            // FILTER 1: Match against active conversation participants or message content
            conversations.filter {
                it.student.name.contains(searchTxt, ignoreCase = true) ||
                        it.lastMessage.message.contains(searchTxt, ignoreCase = true)
            }.forEach { results.add(SearchResult.Existing(it)) }

            // FILTER 2: Match against the student directory for starting new threads
            val existingIds = conversations.map { it.student.id }.toSet()
            allUsers.filter {
                it.id != viewModel.tutorId && // Exclude self
                        !existingIds.contains(it.id) && // Exclude already-active threads
                        it.name.contains(searchTxt, ignoreCase = true)
            }.forEach { results.add(SearchResult.New(it)) }
            
            results
        }
    }

    // ADAPTIVE CONTAINER: Centered width constraint for high readability on tablets
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
            Spacer(Modifier.height(12.dp))

            // HEADER: Professional context for the communication hub
            AdaptiveDashboardHeader(
                title = "Messages",
                subtitle = "Academic Communication",
                icon = Icons.Default.QuestionAnswer
            )

            // SEARCH INTERFACE: Real-time directory and thread filter
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

            // CONTENT DISPATCHER: Renders empty state or the prioritized message list
            if (searchResults.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text("No results found", color = Color.Gray)
                    }
                }
            } else {
                // INBOX LIST: Prioritized list of active and potential conversations
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // UX: Ensures the last item isn't obscured
                ) {
                    items(searchResults) { result ->
                        when (result) {
                            // CASE: Active thread with existing message history
                            is SearchResult.Existing -> {
                                val user = result.preview.student
                                val userCourses = getUserCourses(user.id, allEnrollments, allCourses)
                                
                                // LOGIC: Determine if the message occurred within the last 24 hours
                                val msgDate = Date(result.preview.lastMessage.timestamp)
                                val calendar = Calendar.getInstance()
                                val now = calendar.time
                                calendar.time = msgDate
                                val isToday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(msgDate) == 
                                            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)
                                
                                val timeStr = if (isToday) timeSdf.format(msgDate) else dateSdf.format(msgDate)

                                ConversationItem(
                                    conversation = result.preview,
                                    courses = userCourses,
                                    onClick = { viewModel.setSection(TutorSection.CHAT, user) },
                                    timeStr = timeStr
                                )
                            }
                            
                            // CASE: Directory entry for a new student-tutor contact
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
}

/**
 * UTILITY: Aggregates course titles for a specific student to provide academic context in the inbox.
 */
private fun getUserCourses(userId: String, enrollments: List<CourseEnrollmentDetails>, courses: List<Course>): String {
    return enrollments
        .filter { it.userId == userId && (it.status == "APPROVED" || it.status == "ENROLLED") }
        .mapNotNull { env -> courses.find { it.id == env.courseId }?.title }
        .joinToString(", ")
}

/**
 * SEARCH ABSTRACTION: Distinguishes between active threads and new potential contacts.
 */
sealed class SearchResult {
    data class Existing(val preview: ConversationPreview) : SearchResult()
    data class New(val user: UserLocal) : SearchResult()
}

/**
 * A detailed conversation preview card for active threads.
 * Displays student info, shared courses, latest message snippet, and timestamp.
 */
@Composable
fun ConversationItem(
    conversation: ConversationPreview,
    courses: String,
    onClick: () -> Unit,
    timeStr: String
) {
    AdaptiveDashboardCard(onClick = onClick) { isTablet ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(photoUrl = conversation.student.photoUrl, modifier = Modifier.size(if (isTablet) 60.dp else 54.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Identity Layer: Name and institutional role branding
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = conversation.student.name, fontWeight = FontWeight.Bold, style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    RoleTag(conversation.student.role)
                }

                // Academic Layer: Course metadata for rapid tutor orientation
                if (courses.isNotEmpty()) {
                    Text(text = courses, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Interaction Layer: Latest message content and formatted time
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = conversation.lastMessage.message, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * A simplified contact card for initiating new student-tutor conversations.
 */
@Composable
fun NewConversationItem(
    user: UserLocal,
    courses: String,
    onClick: () -> Unit
) {
    AdaptiveDashboardCard(onClick = onClick) { isTablet ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(photoUrl = user.photoUrl, modifier = Modifier.size(if (isTablet) 60.dp else 54.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = user.name, fontWeight = FontWeight.Bold, style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    RoleTag(user.role)
                }

                if (courses.isNotEmpty()) {
                    Text(text = courses, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // CTA: Clear visual cue for starting a new thread
                Text(text = "Start a new conversation...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}
