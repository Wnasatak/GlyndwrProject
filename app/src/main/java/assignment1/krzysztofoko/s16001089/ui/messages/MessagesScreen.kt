package assignment1.krzysztofoko.s16001089.ui.messages

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: MessagesViewModel = viewModel(factory = MessagesViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val selectedUser by viewModel.selectedConversationUser.collectAsState()
    val conversations by viewModel.recentConversations.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Column(modifier = Modifier.fillMaxSize()) {
            // Manual TopAppBar to avoid Scaffold inset conflicts
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedUser != null) {
                            UserAvatar(photoUrl = selectedUser?.photoUrl, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(text = selectedUser?.name ?: AppConstants.TITLE_MESSAGES, fontWeight = FontWeight.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedUser != null) viewModel.selectConversation(null)
                        else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(targetState = selectedUser, label = "ChatTransition") { targetUser ->
                    if (targetUser == null) {
                        ConversationList(conversations = conversations, onConversationClick = { viewModel.selectConversation(it) }, sdf = sdf)
                    } else {
                        ChatInterface(messages = messages, currentUserId = currentUserId, sdf = sdf)
                    }
                }
            }
            
            if (selectedUser != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    MessageInputBar(onSendMessage = { viewModel.sendMessage(it) })
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(onSendMessage: (String) -> Unit) {
    var messageText by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { if (messageText.isNotBlank()) { onSendMessage(messageText); messageText = "" } },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
        ) { Icon(Icons.AutoMirrored.Filled.Send, null) }
    }
}

@Composable
fun ConversationList(conversations: List<ConversationPreview>, onConversationClick: (UserLocal) -> Unit, sdf: SimpleDateFormat) {
    if (conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))
                Text("No messages yet", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(conversations) { conv ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onConversationClick(conv.otherUser) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(photoUrl = conv.otherUser.photoUrl, modifier = Modifier.size(50.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(conv.otherUser.name, fontWeight = FontWeight.Bold)
                                Text(sdf.format(Date(conv.lastMessage.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Text(conv.lastMessage.message, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInterface(messages: List<assignment1.krzysztofoko.s16001089.data.ClassroomMessage>, currentUserId: String, sdf: SimpleDateFormat) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) { listState.animateScrollToItem(messages.size - 1) } }
    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(messages) { msg ->
            val isMe = msg.senderId == currentUserId
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                Surface(
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isMe) 16.dp else 4.dp, bottomEnd = if (isMe) 4.dp else 16.dp),
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = msg.message, color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = sdf.format(Date(msg.timestamp)), style = MaterialTheme.typography.labelSmall, color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray, modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }
    }
}
