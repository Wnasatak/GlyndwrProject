package assignment1.krzysztofoko.s16001089.ui.classroom

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiveSessionMiniBanner(session: LiveSession, onJoinClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onJoinClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Red.copy(alpha = alpha), CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIVE: ${session.tutorName}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.Red
                )
                Text(
                    text = "Lecture in progress. Tap to join now!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(
                imageVector = Icons.Default.Podcasts,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LiveBroadcastCard(session: LiveSession?, onJoinClick: () -> Unit) {
    val isLive = session?.isActive == true
    
    // Pulsing animation for the LIVE indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = if (isLive) 2.dp else 1.dp,
            color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLive) 8.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(if (isLive) pulseScale else 1f)
                            .background(if (isLive) Color.Red else Color.Gray, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isLive) "LIVE NOW" else "NEXT SESSION",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isLive) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isLive) {
                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "JOIN",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = session?.tutorName?.let { "Lecture with $it" } ?: "No active sessions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isLive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = if (isLive) "Tutor is currently broadcasting live. Click below to enter the interactive classroom."
                       else "Check your schedule for the next broadcast update.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isLive) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Podcasts, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Enter Live Classroom", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun LiveStreamView(
    session: LiveSession,
    chatMessages: List<LiveChatMessage>,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var userMsg by remember { mutableStateOf("") }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
            play()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color.Red, shape = RoundedCornerShape(4.dp)) {
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(session.tutorName, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null)
            }
        }

        // Video Player
        Card(
            modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Chat Section
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Text("Live Chat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { msg ->
                    StudentChatBubble(msg)
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = userMsg,
                onValueChange = { userMsg = it },
                placeholder = { Text("Ask a question...", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        if (userMsg.isNotBlank()) {
                            onSendMessage(userMsg)
                            userMsg = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userMsg.isNotBlank()) {
                        onSendMessage(userMsg)
                        userMsg = ""
                    }
                })
            )
        }
    }
}

@Composable
fun StudentChatBubble(msg: LiveChatMessage) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "${msg.sender}: ",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (msg.isTeacher) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = msg.text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AssignmentSubmissionView(
    assignment: Assignment,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var submissionContent by remember { mutableStateOf("") }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Default.Close, null)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(text = assignment.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(text = "Due Date: ${sdf.format(Date(assignment.dueDate))}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Text(text = assignment.description, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(text = "Your Submission", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = submissionContent,
            onValueChange = { submissionContent = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            placeholder = { Text("Provide a link to your work or type your response here...") },
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = { onSubmit(submissionContent) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = submissionContent.isNotBlank()
        ) {
            Icon(Icons.Default.FileUpload, null)
            Spacer(Modifier.width(12.dp))
            Text("Upload Assignment", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BroadcastBanner(message: String?, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = message != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        if (message != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, null, tint = Color.White)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}
