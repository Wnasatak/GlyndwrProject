package assignment1.krzysztofoko.s16001089.ui.messages

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
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

/**
 * Main Screen for Messaging. 
 * Handles switching between the conversation list and the active chat interface.
 * Implements a dual-pane layout for tablets where the contact list is pinned to the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit, // Callback to exit messaging
    isDarkTheme: Boolean, // Theme state
    viewModel: MessagesViewModel = viewModel(factory = MessagesViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    // Collect state from ViewModel
    val selectedUser by viewModel.selectedConversationUser.collectAsState() // Current active chat partner
    val conversations by viewModel.recentConversations.collectAsState() // List of active threads
    val allUsers by viewModel.allUsers.collectAsState() // Total user database for searching
    val searchQuery by viewModel.searchQuery.collectAsState() // Active search text
    val messages by viewModel.chatMessages.collectAsState() // List of messages in current chat
    
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // Logged-in user ID
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) } // Time formatting utility

    // Determine current user permissions
    val myUser = allUsers.find { it.id == currentUserId } // Find the current user in the list
    val isAdmin = myUser?.role?.lowercase() == "admin" // Check if the user is an administrator
    val isTutor = myUser?.role?.lowercase() in listOf("teacher", "tutor") // Check if the user is a teacher/tutor

    val contentColor = MaterialTheme.colorScheme.onSurface // Base text/icon color from theme

    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic animated background
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Column(modifier = Modifier.fillMaxSize()) {
            // Dynamic Top App Bar: Shows "Messages" or the details of the user being chatted with
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (selectedUser != null) {
                            // User-specific header info (Avatar + Name + Role)
                            UserAvatar(photoUrl = selectedUser?.photoUrl, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val displayName = buildString {
                                        if (!selectedUser?.title.isNullOrEmpty()) {
                                            append(selectedUser?.title); append(" ")
                                        }
                                        append(selectedUser?.name ?: "")
                                    }
                                    Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = contentColor)
                                    Spacer(Modifier.width(8.dp))
                                    RoleTag(role = selectedUser?.role) // Role badge (e.g., TEACHER)
                                }
                                // Optional course context label
                                val sharedCourse by viewModel.getSharedCourse(selectedUser?.id ?: "").collectAsState(initial = "")
                                if (sharedCourse.isNotEmpty()) {
                                    Text(
                                        text = sharedCourse,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = contentColor.copy(alpha = 0.6f),
                                        fontSize = 9.sp,
                                        modifier = Modifier.offset(y = (-3).dp)
                                    )
                                }
                            }
                        } else {
                            // Default page title
                            @Suppress("DEPRECATION")
                            Text(text = AppConstants.TITLE_MESSAGES, fontWeight = FontWeight.Black, color = contentColor)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedUser != null) viewModel.selectConversation(null) // Back to list if in chat
                        else onBack() // Exit messaging if in list
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = contentColor) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )

            // Adaptive content area
            AdaptiveScreenContainer(
                maxWidth = if (selectedUser == null) AdaptiveWidths.Wide else 2400.dp 
            ) { isTablet ->
                if (isTablet && selectedUser != null) {
                    // TABLET CHAT LAYOUT: Split interface (Chat on left, Contacts on right)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.weight(1f)) {
                            // Left Side: Active conversation thread
                            Box(modifier = Modifier.weight(1f)) {
                                ChatInterface(messages, currentUserId, sdf, isDarkTheme)
                            }
                            
                            // Right Side: Glassmorphic side panel for other conversations
                            Card(
                                modifier = Modifier
                                    .width(320.dp) 
                                    .fillMaxHeight()
                                    .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp), 
                                shape = RoundedCornerShape(32.dp), 
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                ConversationListView(
                                    conversations, allUsers, searchQuery, 
                                    { viewModel.updateSearchQuery(it) }, 
                                    { viewModel.selectConversation(it) }, 
                                    sdf, currentUserId, viewModel, isDarkTheme, isAdmin, isTutor,
                                    showSearch = false, // Hide search inside side pane
                                    isSidePane = true
                                )
                            }
                        }
                        // Bottom Area: Message composer bar lifted for professional floating look
                        MessageInputBar(viewModel, contentColor, isDarkTheme, extraBottomPadding = 32.dp)
                    }
                } else {
                    // MOBILE/LIST LAYOUT: Single-pane stack
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (selectedUser == null) {
                                // Default state: Conversation list with search
                                ConversationListView(
                                    conversations, allUsers, searchQuery, 
                                    { viewModel.updateSearchQuery(it) }, 
                                    { viewModel.selectConversation(it) }, 
                                    sdf, currentUserId, viewModel, isDarkTheme, isAdmin, isTutor,
                                    showSearch = true,
                                    isSidePane = false
                                )
                            } else {
                                // Active chat state (Mobile only)
                                ChatInterface(messages, currentUserId, sdf, isDarkTheme)
                            }
                        }
                        if (selectedUser != null) {
                            // Bottom Area: Standard composer
                            MessageInputBar(viewModel, contentColor, isDarkTheme)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Handles the display of the message history list.
 * Bubbles align based on whether the message is sent or received.
 */
@Composable
fun ChatInterface(
    messages: List<assignment1.krzysztofoko.s16001089.data.ClassroomMessage>, 
    currentUserId: String, // ID of the logged-in user to identify "my" messages
    sdf: SimpleDateFormat, // Formatter for message timestamps
    isDarkTheme: Boolean // Theme status for border logic
) {
    val listState = rememberLazyListState() // State to control scroll position
    // Keeps the user scrolled to the newest message automatically
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) { listState.animateScrollToItem(messages.size - 1) } }
    
    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(messages) { msg ->
            val isMe = msg.senderId == currentUserId // Determine if the message was sent by me
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                // Message bubble container
                Surface(
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, // Bubble color based on sender
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp, 
                        bottomStart = if (isMe) 16.dp else 4.dp, // Tail position for "them"
                        bottomEnd = if (isMe) 4.dp else 16.dp // Tail position for "me"
                    ),
                    modifier = Modifier.widthIn(max = 480.dp), // Limit bubble width on large screens
                    border = if (!isMe && isDarkTheme) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null // Subtle border for dark theme visibility
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), // Bubble internal padding
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // The actual message text
                        @Suppress("DEPRECATION")
                        Text(
                            text = msg.message, // The message content
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, // Contrast color
                            modifier = Modifier.weight(1f, fill = false), // Allow text to take space but not push timestamp off
                            style = MaterialTheme.typography.bodyMedium // Consistent font style
                        )
                        // Tiny timestamp inside the bubble
                        Text(
                            text = sdf.format(Date(msg.timestamp)), // Formatted time
                            style = MaterialTheme.typography.labelSmall, // Small utility font
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else Color.Gray, // Secondary text color
                            fontSize = 10.sp // Specific tiny size
                        )
                    }
                }
            }
        }
    }
}

/**
 * Message composition bar containing the text field and send button.
 */
@Composable
fun MessageInputBar(
    viewModel: MessagesViewModel, // ViewModel to handle send logic
    contentColor: Color, // Base color for text
    isDarkTheme: Boolean, // Theme state
    extraBottomPadding: androidx.compose.ui.unit.Dp = 12.dp // Customizable padding for different screen layouts
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth() // Fill horizontal space
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) // Semi-transparent background
            .padding(bottom = extraBottomPadding, top = 8.dp) // Layout specific padding
    ) {
        Surface(
            modifier = Modifier.widthIn(max = AdaptiveWidths.Wide).fillMaxWidth().padding(horizontal = 16.dp), // Adaptive width constraint
            color = Color.Transparent // Surface provides tonal elevation without background color
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.ime), // Handles keyboard overlap dynamically
                verticalAlignment = Alignment.CenterVertically
            ) {
                var messageText by remember { mutableStateOf("") } // Local state for the typed text
                // Modern rounded text input
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it }, // Update state on typing
                    modifier = Modifier.weight(1f), // Take available space
                    placeholder = { Text("Type a message...", color = contentColor.copy(alpha = 0.4f)) }, // Prompt text
                    shape = RoundedCornerShape(32.dp), // Pill-shaped input
                    textStyle = TextStyle(color = contentColor), // Typed text style
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), // Transparent-ish default
                        focusedContainerColor = MaterialTheme.colorScheme.surface, // Solid color when focused
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Subtle border
                        focusedBorderColor = MaterialTheme.colorScheme.primary // Highlighted border
                    )
                )
                Spacer(Modifier.width(12.dp)) // Gap between input and button
                // Primary send action button
                FloatingActionButton(
                    onClick = { 
                        if (messageText.isNotBlank()) { 
                            viewModel.sendMessage(messageText) // Trigger send logic in VM
                            messageText = "" // Clear input field
                        } 
                    },
                    shape = CircleShape, // Circular button
                    containerColor = MaterialTheme.colorScheme.primary, // Themed background
                    contentColor = MaterialTheme.colorScheme.onPrimary, // Contrast icon color
                    modifier = Modifier.size(48.dp), // Standard size
                    elevation = FloatingActionButtonDefaults.elevation(0.dp) // Flat design
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp)) // Send icon
                }
            }
        }
    }
}

/**
 * Renders the primary list of conversations and searchable contacts.
 * Features complex filtering based on user roles (Admin vs Tutor vs Student).
 */
@Composable
fun ConversationListView(
    conversations: List<ConversationPreview>, // List of current active chats
    allUsers: List<UserLocal>, // Total directory of users
    searchQuery: String, // Active search text
    onSearchChange: (String) -> Unit, // Search input callback
    onUserClick: (UserLocal) -> Unit, // User selection callback
    sdf: SimpleDateFormat, // Time formatter for last messages
    currentUserId: String, // ID of local user
    viewModel: MessagesViewModel, // Logic handler
    isDarkTheme: Boolean, // Theme state
    isAdmin: Boolean, // Admin permission flag
    isTutor: Boolean, // Tutor permission flag
    showSearch: Boolean = true, // Toggles search bar visibility
    isSidePane: Boolean = false // Toggles between main list and tablet sidebar styling
) {
    val contentColor = MaterialTheme.colorScheme.onSurface // Base text color

    // Reactive search result calculation - logic defined in a remember block for performance
    val searchResults = remember(conversations, allUsers, searchQuery, isAdmin, isTutor) {
        if (searchQuery.isEmpty()) {
            conversations.map { ConversationItemType.Existing(it) } // Default state: active chats only
        } else {
            val results = mutableListOf<ConversationItemType>()
            // 1. Filter existing chats matching query by name or content
            conversations.filter {
                it.otherUser.name.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.message.contains(searchQuery, ignoreCase = true)
            }.forEach { results.add(ConversationItemType.Existing(it)) }

            // 2. Filter global directory for new contacts based on hierarchy rules
            val existingIds = conversations.map { it.otherUser.id }.toSet() // Avoid duplicates
            val staffRoles = listOf("teacher", "tutor", "admin") // Role grouping
            allUsers.filter {
                val baseFilter = it.id != currentUserId && !existingIds.contains(it.id) && it.name.contains(searchQuery, ignoreCase = true)
                when {
                    isAdmin -> baseFilter // Admin sees all
                    isTutor -> baseFilter && (it.role.lowercase() == "student" || it.role.lowercase() == "admin") // Tutor sees Students/Admin
                    else -> baseFilter && it.role.lowercase() in staffRoles // Student only sees Staff/Admin
                }
            }.forEach { results.add(ConversationItemType.NewUser(it)) }
            results
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Optional search field rendering
        if (showSearch) {
            val searchPlaceholder = when {
                isAdmin -> "Search All Users"
                isTutor -> "Search Students / Admin"
                else -> "Search Teacher / Admin"
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange, // Trigger state update
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                placeholder = { Text(searchPlaceholder, color = contentColor.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = contentColor.copy(alpha = 0.7f)) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Default.Close, null, tint = contentColor) } },
                shape = RoundedCornerShape(16.dp),
                singleLine = true, // Force one line search
                textStyle = TextStyle(color = contentColor),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )
        } else {
            // Margin when search is disabled (tablet mode)
            Spacer(Modifier.height(12.dp))
        }

        // Final list rendering logic
        if (searchResults.isEmpty()) {
            // Empty state placeholder UI
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    @Suppress("DEPRECATION")
                    Text("No results found", color = Color.Gray)
                }
            }
        } else {
            // Scrollable list of chats/contacts using LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = if (isSidePane) 12.dp else 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between list items
            ) {
                items(searchResults) { item ->
                    when (item) {
                        is ConversationItemType.Existing -> {
                            // Active conversation item
                            val sharedCourse by viewModel.getSharedCourse(item.preview.otherUser.id).collectAsState(initial = "")
                            ExistingConversationCard(
                                conv = item.preview, course = sharedCourse, 
                                onClick = { onUserClick(item.preview.otherUser) }, 
                                sdf = sdf, isDarkTheme = isDarkTheme, compact = isSidePane
                            )
                        }
                        is ConversationItemType.NewUser -> {
                            // Potential contact item
                            val sharedCourse by viewModel.getSharedCourse(item.user.id).collectAsState(initial = "")
                            NewUserCard(
                                user = item.user, course = sharedCourse, 
                                onClick = { onUserClick(item.user) }, 
                                isDarkTheme = isDarkTheme, compact = isSidePane
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(32.dp)) } // Bottom list margin
            }
        }
    }
}

/**
 * Union type for items appearing in the conversation list.
 * Helps handle different card UIs for active chats vs. new users.
 */
sealed class ConversationItemType {
    data class Existing(val preview: ConversationPreview) : ConversationItemType()
    data class NewUser(val user: UserLocal) : ConversationItemType()
}

/**
 * Renders a chat summary card for active threads.
 * Includes user avatar, name, last message snippet, and status.
 */
@Composable
fun ExistingConversationCard(
    conv: ConversationPreview, // Data source
    course: String, // Contextual course info
    onClick: () -> Unit, // Selection action
    sdf: SimpleDateFormat, // Time formatter
    isDarkTheme: Boolean, // Theme state
    compact: Boolean = false // If true, UI shrinks for sidebar mode
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, // Card handles click ripple
        shape = RoundedCornerShape(20.dp), // Consistent rounding
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Themed background
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat design
    ) {
        Row(modifier = Modifier.padding(if (compact) 12.dp else 16.dp), verticalAlignment = Alignment.CenterVertically) {
            // User visual identity
            UserAvatar(photoUrl = conv.otherUser.photoUrl, modifier = Modifier.size(if (compact) 44.dp else 54.dp))
            Spacer(Modifier.width(if (compact) 12.dp else 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Identity row: Name + Badge
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val displayName = buildString {
                        if (!conv.otherUser.title.isNullOrEmpty()) { append(conv.otherUser.title); append(" ") }
                        append(conv.otherUser.name)
                    }
                    Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = if (compact) 14.sp else 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false), color = MaterialTheme.colorScheme.onSurface)
                    if (!compact) { // Hide tag in sidebar to save space
                        Spacer(Modifier.width(8.dp)); RoleTag(role = conv.otherUser.role) 
                    }
                }
                // Optional sub-label for course context
                if (course.isNotEmpty()) {
                    Text(text = course, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = if (compact) 9.sp else 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                }
                // Preview row: Snippet of message + timestamp
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // Logic for unread or branded message colors
                    val messageColor = if (conv.lastMessage.message.contains("Hi, Emma", ignoreCase = true)) { if (isDarkTheme) CyanAccent else VoucherViolet } 
                                       else if (!conv.lastMessage.isRead && conv.lastMessage.senderId != FirebaseAuth.getInstance().currentUser?.uid) { MaterialTheme.colorScheme.primary } 
                                       else { Color.Gray }
                    @Suppress("DEPRECATION")
                    Text(text = conv.lastMessage.message, style = MaterialTheme.typography.bodySmall, color = messageColor, maxLines = 1, overflow = TextOverflow.Ellipsis, 
                         fontWeight = if (!conv.lastMessage.isRead && conv.lastMessage.senderId != FirebaseAuth.getInstance().currentUser?.uid) FontWeight.Black else FontWeight.Normal,
                         modifier = Modifier.weight(1f), fontSize = if (compact) 11.sp else 12.sp)
                    if (!compact) { // Hide time in sidebar to save space
                        Spacer(Modifier.width(8.dp)); Text(text = sdf.format(Date(conv.lastMessage.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp) 
                    }
                }
            }
            if (!compact) { 
                Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)) 
            }
        }
    }
}

/**
 * Renders a contact card for starting a new chat thread.
 * Styled differently to distinguish from active conversations.
 */
@Composable
fun NewUserCard(
    user: UserLocal, // Data source
    course: String, // Contextual course info
    onClick: () -> Unit, // Selection action
    isDarkTheme: Boolean, // Theme state
    compact: Boolean = false // If true, UI shrinks for sidebar mode
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, // Interactive card
        shape = RoundedCornerShape(20.dp), // Round edges
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)), // Subtle primary background
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat
    ) {
        Row(modifier = Modifier.padding(if (compact) 12.dp else 16.dp), verticalAlignment = Alignment.CenterVertically) {
            // User avatar rendering
            UserAvatar(photoUrl = user.photoUrl, modifier = Modifier.size(if (compact) 44.dp else 54.dp))
            Spacer(Modifier.width(if (compact) 12.dp else 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Identity build logic
                val displayName = buildString { if (!user.title.isNullOrEmpty()) { append(user.title); append(" ") }; append(user.name) }
                Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = if (compact) 14.sp else 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                // Optional context row
                if (course.isNotEmpty()) { Text(text = course, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = if (compact) 9.sp else 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) }
                // Prompt text for new chat
                @Suppress("DEPRECATION")
                Text("Start a conversation...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, fontSize = if (compact) 10.sp else 12.sp)
            }
        }
    }
}

/**
 * Visual badge component indicating user role.
 * Color changes dynamically based on the role (ADMIN, TEACHER, STUDENT).
 */
@Composable
fun RoleTag(role: String?) {
    // Map internal role strings to display strings
    val roleText = when (role?.lowercase()) {
        "admin" -> "ADMIN"
        "teacher", "tutor" -> "TEACHER"
        "student" -> "STUDENT"
        else -> role?.uppercase() ?: "USER"
    }
    // Select specific color based on role
    val tagColor = when (roleText) {
        "ADMIN", "TEACHER" -> Color(0xFF1E88E5) // Blue for staff
        "STUDENT" -> Color(0xFF43A047) // Green for students
        else -> Color.Gray // Gray for others
    }
    // Render the badge using a Surface with transparent background and border
    Surface(color = tagColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), border = BorderStroke(0.5.dp, tagColor.copy(alpha = 0.4f))) {
        @Suppress("DEPRECATION")
        Text(text = roleText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = tagColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
