package assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import assignment1.krzysztofoko.s16001089.data.LiveSession
import assignment1.krzysztofoko.s16001089.ui.classroom.LiveChatMessage
import assignment1.krzysztofoko.s16001089.ui.components.*

/**
 * LiveStreamView provides the student interface for attending a live lecture.
 * It features a synchronized video player and real-time interactive chat.
 */
@OptIn(UnstableApi::class)
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
    var isBuffering by remember { mutableStateOf(true) }

    // WORKING SOURCE: Matched perfectly with the working Tutor studio
    val workingVideoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // TOP HEADER: Contextual Info
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
                Text(session.tutorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Exit Classroom")
            }
        }

        // VIDEO PLAYER AREA: Created inside scope for instant cleanup
        Card(
            modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                key(workingVideoUrl) {
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            val mediaItem = MediaItem.fromUri(workingVideoUrl)
                            setMediaItem(mediaItem)
                            prepare()
                            repeatMode = Player.REPEAT_MODE_ALL
                            playWhenReady = true
                            
                            addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(state: Int) {
                                    isBuffering = state == Player.STATE_BUFFERING
                                }
                                override fun onPlayerError(error: PlaybackException) {
                                    prepare(); play()
                                }
                            })
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose { 
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                            exoPlayer.release() 
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }
                        },
                        update = { view -> if (view.player != exoPlayer) view.player = exoPlayer },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    if (isBuffering) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                        }
                    }
                }
            }
        }

        // INTERACTIVE CHAT SECTION: Separated for future expansion
        LiveChatComponent(
            chatMessages = chatMessages,
            onSendMessage = onSendMessage,
            listState = listState,
            scope = scope,
            modifier = Modifier.weight(1f)
        )
    }
}
