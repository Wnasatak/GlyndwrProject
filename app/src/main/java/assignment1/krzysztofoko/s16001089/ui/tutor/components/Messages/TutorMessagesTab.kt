package assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.tutor.ConversationPreview
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TutorMessagesTab(
    viewModel: TutorViewModel
) {
    val conversations by viewModel.recentConversations.collectAsState()
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Messages", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        
        if (conversations.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ChatBubbleOutline, 
                        null, 
                        modifier = Modifier.size(64.dp), 
                        tint = Color.Gray.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No recent conversations", color = Color.Gray)
                    Text(
                        "Messages from students in your courses will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversations) { conv ->
                    ConversationItem(
                        conversation = conv,
                        onClick = { viewModel.setSection(TutorSection.CHAT, conv.student) },
                        timeStr = sdf.format(Date(conv.lastMessage.timestamp))
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationPreview,
    onClick: () -> Unit,
    timeStr: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = conversation.student.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = conversation.lastMessage.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
