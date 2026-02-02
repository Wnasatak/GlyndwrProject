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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorChatTab(
    viewModel: TutorViewModel
) {
    val student by viewModel.selectedStudent.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Modern vertical layout without conflicting scaffolds
    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = student?.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(student?.name ?: "Student", fontWeight = FontWeight.Bold)
                    Text("Online", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                }
            }
        }

        // Messages List - weight(1f) ensures it fills the gap and pushes input bar to bottom
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == viewModel.tutorId
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp, 
                            bottomStart = if (isMe) 16.dp else 4.dp, 
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = msg.message, color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = sdf.format(Date(msg.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                modifier = Alignment.End.let { Modifier.align(it) }
                            )
                        }
                    }
                }
            }
        }

        // Input Bar - Modern Edge-to-Edge alignment
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Space for bottom system bars
                .imePadding(), // Space for keyboard
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { 
                        if (messageText.isNotBlank()) {
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
