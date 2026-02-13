package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Live

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LiveChatMessage(val sender: String, val text: String, val isTeacher: Boolean = false)

@Composable
fun BroadcastStudio(
    viewModel: TutorViewModel,
    courseTitle: String?
) {
    val isLive by viewModel.isLive.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isMicOn by remember { mutableStateOf(true) }
    var isCamOn by remember { mutableStateOf(true) }
    var isChatVisible by remember { mutableStateOf(true) }
    var isFullScreen by remember { mutableStateOf(false) }

    var viewerCount by remember { mutableIntStateOf(0) }
    var chatMessages by remember { mutableStateOf<List<LiveChatMessage>>(emptyList()) }
    var teacherMsg by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val isTablet = isTablet()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isLive, isPaused) {
        if (isLive) {
            if (isPaused) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
                viewerCount = 11
                if (chatMessages.isEmpty()) {
                    chatMessages = listOf(LiveChatMessage("System", "Broadcast started successfully."))
                }

                while (isLive && !isPaused) {
                    delay((4000..8000).random().toLong())
                    viewerCount += (-1..3).random()
                    if (viewerCount < 0) viewerCount = 0

                    val students = listOf("Alice", "Mark", "Sarah", "John", "Emma")
                    val texts = listOf("Hello!", "Interesting topic", "Can you repeat that?", "Clear stream!", "Wrexham rules! 🎓")

                    chatMessages = chatMessages + LiveChatMessage(students.random(), texts.random())
                    scope.launch {
                        if (chatMessages.isNotEmpty()) {
                            listState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }
                }
            }
        } else {
            viewerCount = 0
            isRecording = false
            exoPlayer.pause()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Menu / Header
        AnimatedVisibility(visible = !isFullScreen) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(AdaptiveSpacing.contentPadding()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Broadcast Studio", 
                        style = AdaptiveTypography.headline(), 
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        courseTitle ?: "Select Course", 
                        style = AdaptiveTypography.caption(), 
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecording) {
                        BlinkingRecBadge()
                        Spacer(Modifier.width(12.dp))
                    }
                    if (isLive) LiveBadge(viewerCount)
                }
            }
        }

        // Adaptive Content Area
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = if (isFullScreen) Modifier.fillMaxSize()
                else Modifier.adaptiveWidth(AdaptiveWidths.Wide).padding(horizontal = AdaptiveSpacing.contentPadding())
            ) {
                val videoModifier = if (isFullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(if (isTablet) 360.dp else 220.dp)
                Card(
                    modifier = videoModifier,
                    shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isLive && isCamOn && !isPaused) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = false
                                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PauseCircle else if (!isCamOn) Icons.Default.VideocamOff else Icons.Default.Podcasts,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(if (isTablet) 64.dp else 48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (isPaused) "Broadcast Paused" else if (!isCamOn) "Camera Muted" else "Not Broadcasting",
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = AdaptiveTypography.label()
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            IconButton(
                                onClick = { isFullScreen = !isFullScreen },
                                modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(0.4f), CircleShape).size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }

                            if (isLive && isMicOn && !isPaused) {
                                SimulatedAudioVisualizer(Modifier.align(Alignment.BottomStart))
                            }

                            if (isFullScreen && isLive) {
                                Row(Modifier.align(Alignment.TopStart)) {
                                    LiveBadge(viewerCount)
                                    if (isRecording) {
                                        Spacer(Modifier.width(8.dp))
                                        BlinkingRecBadge()
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isFullScreen) {
                    Spacer(Modifier.height(AdaptiveSpacing.contentPadding()))

                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isChatVisible) {
                            Card(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ChatBubble, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Live Chat", fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                                    }

                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(chatMessages) { msg ->
                                            ChatBubble(msg)
                                        }
                                    }

                                    if (isLive) {
                                        OutlinedTextField(
                                            value = teacherMsg,
                                            onValueChange = { teacherMsg = it },
                                            placeholder = { Text("Say something...", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            trailingIcon = {
                                                IconButton(onClick = {
                                                    if (teacherMsg.isNotBlank()) {
                                                        chatMessages = chatMessages + LiveChatMessage("You (Tutor)", teacherMsg, true)
                                                        teacherMsg = ""
                                                        scope.launch { listState.animateScrollToItem(chatMessages.size - 1) }
                                                    }
                                                }) {
                                                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                }
                                            },
                                            singleLine = true,
                                            textStyle = AdaptiveTypography.caption(),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                            keyboardActions = KeyboardActions(onSend = {
                                                if (teacherMsg.isNotBlank()) {
                                                    chatMessages = chatMessages + LiveChatMessage("You (Tutor)", teacherMsg, true)
                                                    teacherMsg = ""
                                                    scope.launch { listState.animateScrollToItem(chatMessages.size - 1) }
                                                }
                                            })
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.width(if (isTablet) 64.dp else 56.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ControlIcon(
                                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                isActive = isPaused,
                                label = if (isPaused) "Resume" else "Pause",
                                activeColor = Color(0xFFFFC107),
                                onClick = { viewModel.setLivePaused(!isPaused) }
                            )
                            ControlIcon(icon = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff, isActive = isMicOn, label = "Mic", onClick = { isMicOn = !isMicOn })
                            ControlIcon(icon = if (isCamOn) Icons.Default.Videocam else Icons.Default.VideocamOff, isActive = isCamOn, label = "Cam", onClick = { isCamOn = !isCamOn })
                            ControlIcon(icon = Icons.Default.FiberManualRecord, isActive = isRecording, label = "Rec", activeColor = Color.Red, onClick = { if(isLive) isRecording = !isRecording })
                            ControlIcon(icon = Icons.Default.Chat, isActive = isChatVisible, label = "Chat", onClick = { isChatVisible = !isChatVisible })
                        }
                    }
                }
            }
        }

        // Adaptive Footer Button
        AnimatedVisibility(visible = !isFullScreen) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.adaptiveWidth(AdaptiveWidths.Wide).padding(AdaptiveSpacing.contentPadding())) {
                    Button(
                        onClick = {
                            viewModel.toggleLiveStream(!isLive)
                        },
                        modifier = Modifier.fillMaxWidth().height(if (isTablet) 56.dp else 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLive) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(if (isLive) Icons.Default.Stop else Icons.Default.Podcasts, null, modifier = Modifier.size(if (isTablet) 24.dp else 20.dp))
                        Spacer(Modifier.width(12.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            if (isLive) "End Broadcast" else "Go Live Now", 
                            fontWeight = FontWeight.Bold, 
                            style = AdaptiveTypography.sectionHeader(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveBadge(viewers: Int) {
    Surface(
        color = Color(0xFFE53935),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("LIVE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            Spacer(Modifier.width(8.dp))
            Text("$viewers", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun SimulatedAudioVisualizer(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio")

    Row(modifier = modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        repeat(5) { i ->
            val height by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = (12..28).random().toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + (i * 100), easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$i"
            )
            Box(Modifier.width(4.dp).height(height.dp).background(Color(0xFF00E676), CircleShape))
        }
    }
}

@Composable
fun ControlIcon(icon: ImageVector, label: String, isActive: Boolean, activeColor: Color? = null, onClick: () -> Unit) {
    val isTablet = isTablet()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(if (isTablet) 48.dp else 40.dp)
                .clickable { onClick() },
            shape = CircleShape,
            color = if (isActive) (activeColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isActive) (activeColor ?: MaterialTheme.colorScheme.primary) else Color.Gray.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) (activeColor ?: MaterialTheme.colorScheme.primary) else Color.Gray,
                    modifier = Modifier.size(if (isTablet) 22.dp else 18.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = AdaptiveTypography.hint(),
            color = if (isActive) (activeColor ?: MaterialTheme.colorScheme.primary) else Color.Gray,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BlinkingRecBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "rec")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )
    Surface(
        color = Color.Black.copy(0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(Color.Red, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("REC", color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        }
    }
}

@Composable
fun ChatBubble(msg: LiveChatMessage) {
    Column {
        Text(
            text = msg.sender,
            fontWeight = FontWeight.Bold,
            style = AdaptiveTypography.hint(),
            color = if (msg.isTeacher) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )
        Box(
            modifier = Modifier
                .background(
                    if (msg.isTeacher) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.6f),
                    RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                msg.text, 
                style = AdaptiveTypography.caption(), 
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
