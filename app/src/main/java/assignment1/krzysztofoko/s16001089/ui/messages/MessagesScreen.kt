package assignment1.krzysztofoko.s16001089.ui.messages

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.theme.*
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
    val allUsers by viewModel.allUsers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (selectedUser != null) {
                            UserAvatar(photoUrl = selectedUser?.photoUrl, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val displayName = buildString {
                                        if (!selectedUser?.title.isNullOrEmpty()) {
                                            append(selectedUser?.title)
                                            append(" ")
                                        }
                                        append(selectedUser?.name ?: "")
                                    }
                                    Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDarkTheme) Color.White else Color.Black)
                                    Spacer(Modifier.width(8.dp))
                                    RoleTag(role = selectedUser?.role)
                                }
                                val sharedCourse by viewModel.getSharedCourse(selectedUser?.id ?: "").collectAsState(initial = "")
                                if (sharedCourse.isNotEmpty()) {
                                    Text(
                                        text = sharedCourse, 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Gray, 
                                        fontSize = 9.sp,
                                        modifier = Modifier.offset(y = (-3).dp)
                                    )
                                }
                            }
                        } else {
                            Text(text = AppConstants.TITLE_MESSAGES, fontWeight = FontWeight.Black, color = if (isDarkTheme) Color.White else Color.Black)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedUser != null) viewModel.selectConversation(null)
                        else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDarkTheme) Color.White else Color.Black) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isDarkTheme) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )

            Box(modifier = Modifier.weight(1f)) {
                AdaptiveScreenContainer(
                    maxWidth = if (selectedUser == null) AdaptiveWidths.Medium else AdaptiveWidths.Wide
                ) { isTablet ->
                    AnimatedContent(targetState = selectedUser, label = "ChatTransition") { targetUser ->
                        if (targetUser == null) {
                            ConversationListView(
                                conversations = conversations, 
                                allUsers = allUsers,
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.updateSearchQuery(it) },
                                onUserClick = { viewModel.selectConversation(it) }, 
                                sdf = sdf,
                                currentUserId = currentUserId,
                                viewModel = viewModel,
                                isDarkTheme = isDarkTheme
                            )
                        } else {
                            ChatInterface(messages = messages, currentUserId = currentUserId, sdf = sdf, isDarkTheme = isDarkTheme)
                        }
                    }
                }
            }
            
            if (selectedUser != null) {
                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkTheme) Color.Black.copy(alpha = 0.2f) else Color.Transparent)
                        .padding(bottom = 12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = AdaptiveWidths.Wide)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.ime),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var messageText by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Type a message...", color = if (isDarkTheme) Color.White.copy(alpha = 0.4f) else Color.Gray) },
                                shape = RoundedCornerShape(32.dp),
                                textStyle = TextStyle(color = if (isDarkTheme) Color.White else Color.Black),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.9f),
                                    focusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.White,
                                    unfocusedBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.3f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            FloatingActionButton(
                                onClick = { if (messageText.isNotBlank()) { viewModel.sendMessage(messageText); messageText = "" } },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                modifier = Modifier.size(48.dp),
                                elevation = FloatingActionButtonDefaults.elevation(0.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationListView(
    conversations: List<ConversationPreview>,
    allUsers: List<UserLocal>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onUserClick: (UserLocal) -> Unit,
    sdf: SimpleDateFormat,
    currentUserId: String,
    viewModel: MessagesViewModel,
    isDarkTheme: Boolean
) {
    val searchResults = remember(conversations, allUsers, searchQuery) {
        if (searchQuery.isEmpty()) {
            conversations.map { ConversationItemType.Existing(it) }
        } else {
            val results = mutableListOf<ConversationItemType>()
            
            conversations.filter { 
                it.otherUser.name.contains(searchQuery, ignoreCase = true) || 
                it.lastMessage.message.contains(searchQuery, ignoreCase = true) 
            }.forEach { results.add(ConversationItemType.Existing(it)) }
            
            val existingIds = conversations.map { it.otherUser.id }.toSet()
            allUsers.filter { 
                it.id != currentUserId && 
                !existingIds.contains(it.id) && 
                (it.role == "teacher" || it.role == "tutor" || it.role == "admin") &&
                it.name.contains(searchQuery, ignoreCase = true)
            }.forEach { results.add(ConversationItemType.NewUser(it)) }
            
            results
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            placeholder = { Text("Search Teacher / Admin", color = if (isDarkTheme) Color.White.copy(alpha = 0.4f) else Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Gray) },
            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Default.Close, null, tint = if (isDarkTheme) Color.White else Color.Black) } },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            textStyle = TextStyle(color = if (isDarkTheme) Color.White else Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        )

        if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No results found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searchResults) { item ->
                    when (item) {
                        is ConversationItemType.Existing -> {
                            val sharedCourse by viewModel.getSharedCourse(item.preview.otherUser.id).collectAsState(initial = "")
                            ExistingConversationCard(conv = item.preview, course = sharedCourse, onClick = { onUserClick(item.preview.otherUser) }, sdf = sdf, isDarkTheme = isDarkTheme)
                        }
                        is ConversationItemType.NewUser -> {
                            val sharedCourse by viewModel.getSharedCourse(item.user.id).collectAsState(initial = "")
                            NewUserCard(user = item.user, course = sharedCourse, onClick = { onUserClick(item.user) }, isDarkTheme = isDarkTheme)
                        }
                    }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

sealed class ConversationItemType {
    data class Existing(val preview: ConversationPreview) : ConversationItemType()
    data class NewUser(val user: UserLocal) : ConversationItemType()
}

@Composable
fun ExistingConversationCard(conv: ConversationPreview, course: String, onClick: () -> Unit, sdf: SimpleDateFormat, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DarkSurface else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(photoUrl = conv.otherUser.photoUrl, modifier = Modifier.size(54.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Row 1: Name and Role Tag
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val displayName = buildString {
                        if (!conv.otherUser.title.isNullOrEmpty()) {
                            append(conv.otherUser.title)
                            append(" ")
                        }
                        append(conv.otherUser.name)
                    }
                    Text(
                        text = displayName, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 17.sp, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis, 
                        modifier = Modifier.weight(1f, fill = false),
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Spacer(Modifier.width(8.dp))
                    RoleTag(role = conv.otherUser.role)
                }
                
                // Row 2: Course
                if (course.isNotEmpty()) {
                    Text(
                        text = course, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary, 
                        fontSize = 11.sp, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Row 3: Message and Time
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val messageColor = if (conv.lastMessage.message.contains("Hi, Emma", ignoreCase = true)) {
                         if (isDarkTheme) CyanAccent else VoucherViolet
                    } else if (!conv.lastMessage.isRead && conv.lastMessage.senderId != FirebaseAuth.getInstance().currentUser?.uid) {
                        MaterialTheme.colorScheme.primary 
                    } else {
                        Color.Gray
                    }

                    Text(
                        text = conv.lastMessage.message, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = messageColor, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (!conv.lastMessage.isRead && conv.lastMessage.senderId != FirebaseAuth.getInstance().currentUser?.uid) FontWeight.Black else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = sdf.format(Date(conv.lastMessage.timestamp)), 
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
fun NewUserCard(user: UserLocal, course: String, onClick: () -> Unit, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DarkSurface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(photoUrl = user.photoUrl, modifier = Modifier.size(54.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayName = buildString {
                        if (!user.title.isNullOrEmpty()) {
                            append(user.title)
                            append(" ")
                        }
                        append(user.name)
                    }
                    Text(
                        text = displayName, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 17.sp, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Spacer(Modifier.width(8.dp))
                    RoleTag(role = user.role)
                }
                if (course.isNotEmpty()) {
                    Text(text = course, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                }
                Text("Start a new conversation...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun RoleTag(role: String?) {
    val roleText = when (role?.lowercase()) {
        "admin" -> "ADMIN"
        "teacher", "tutor" -> "TEACHER"
        "student" -> "STUDENT"
        else -> role?.uppercase() ?: "USER"
    }
    
    val tagColor = when (roleText) {
        "ADMIN", "TEACHER" -> Color(0xFF1E88E5)
        "STUDENT" -> Color(0xFF43A047)
        else -> Color.Gray
    }

    Surface(
        color = tagColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, tagColor.copy(alpha = 0.4f))
    ) {
        Text(
            text = roleText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tagColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun ChatInterface(messages: List<assignment1.krzysztofoko.s16001089.data.ClassroomMessage>, currentUserId: String, sdf: SimpleDateFormat, isDarkTheme: Boolean) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) { listState.animateScrollToItem(messages.size - 1) } }
    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(messages) { msg ->
            val isMe = msg.senderId == currentUserId
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                Surface(
                    color = if (isMe) MaterialTheme.colorScheme.primary else if (isDarkTheme) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isMe) 16.dp else 4.dp, bottomEnd = if (isMe) 4.dp else 16.dp),
                    modifier = Modifier.widthIn(max = 480.dp),
                    border = if (!isMe && isDarkTheme) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = msg.message, 
                            color = if (isMe) Color.White else if (isDarkTheme) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.bodyMedium
                        )
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
}
