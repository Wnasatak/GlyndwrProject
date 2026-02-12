package assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveContent
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * TutorChatTab provides a real-time messaging interface between a tutor and a selected student.
 * It features a reactive message list that automatically stays in sync with the database
 * and a responsive input area that handles keyboard interactions.
 *
 * Pattern: One-on-one instant messaging UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorChatTab(
    viewModel: TutorViewModel
) {
    // REACTIVE DATA: Observes the currently selected student and their chat history from the ViewModel
    val student by viewModel.selectedStudent.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    
    // UI STATE: Manages the active text input and the scrolling state of the chat list
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // UTILITY: Formatter for message timestamps
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    /**
     * AUTO-SCROLL LOGIC: 
     * Ensures that the chat always scrolls to the most recent message whenever the list size changes.
     */
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // ADAPTIVE CONTAINER: Manages layout constraints; isScrollable is false because we use an internal LazyColumn
    AdaptiveContent(isScrollable = false) {
        
        // CHAT HISTORY: Scrollable list of message bubbles
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                // LOGIC: Identifies if the message was sent by the current tutor or the student
                val isMe = msg.senderId == viewModel.tutorId
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // MESSAGE BUBBLE: Branded surface with dynamic tail-shaping based on the sender
                    Surface(
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp, // Tail logic for student messages
                            bottomEnd = if (isMe) 4.dp else 16.dp   // Tail logic for tutor messages
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Text Content: Contrast-aware coloring
                            Text(
                                text = msg.message, 
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f, fill = false),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            // Timestamp: Subtly placed at the bottom-end of the bubble
                            Text(
                                text = sdf.format(Date(msg.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION: INPUT CONTROL PANEL ---
        // Features a persistent blurred background and IME (keyboard) awareness
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime) // UX: Pushes the input field above the virtual keyboard
                    .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MESSAGE INPUT: Primary text entry field
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                
                Spacer(Modifier.width(8.dp))
                
                // SEND TRIGGER: Validates and persists the new message
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            // DATABASE UPDATE: Sends message via ViewModel and clears local state
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                }
            }
        }
    }
}

/**
 * FUTURE IMPLEMENTATION:
 * 
 * 1. Multimedia Attachments: 
 *    - Logic will be added to support document (PDF) and image sharing within the chat.
 *    - Required for: Submitting draft work for tutor feedback.
 * 
 * 2. Message Status Tracking: 
 *    - Real-time 'Delivered' and 'Read' receipts based on local database status flags.
 * 
 * 3. Typing Indicators: 
 *    - Visual feedback when the student or tutor is actively composing a response.
 * 
 * 4. Voice Messaging: 
 *    - Support for sending short audio snippets for quick academic clarifications.
 */
