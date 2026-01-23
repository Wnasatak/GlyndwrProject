package assignment1.krzysztofoko.s16001089.ui.components

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun AudioPlayerComponent(
    book: Book,
    isMinimized: Boolean,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit,
    isDarkTheme: Boolean,
    isPlaying: Boolean,
    onPlayingStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }
    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(book.id) {
        isReady = false
        mediaPlayer?.release()
        if (book.audioUrl.isNotEmpty()) {
            try {
                val player = MediaPlayer()
                val descriptor: AssetFileDescriptor = context.assets.openFd(book.audioUrl)
                player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                player.prepare()
                player.setVolume(1.0f, 1.0f) 
                mediaPlayer = player
                duration = player.duration
                isReady = true
                player.start()
                onPlayingStateChange(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying && mediaPlayer != null) {
            currentPosition = mediaPlayer?.currentPosition ?: 0
            delay(1000)
        }
    }

    // React to external play/pause requests
    LaunchedEffect(isPlaying) {
        mediaPlayer?.let {
            if (isPlaying && !it.isPlaying) {
                it.start()
            } else if (!isPlaying && it.isPlaying) {
                it.pause()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Box(modifier = Modifier.padding(16.dp)) {
        if (isMinimized) {
            // MINI PLAYER
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clickable { onToggleMinimize() },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = {
                        onPlayingStateChange(!isPlaying)
                    }) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
        } else {
            // FULL PLAYER
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleMinimize) {
                            Icon(Icons.Default.KeyboardArrowDown, "Minimize")
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Now Playing",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isReady) {
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { 
                                currentPosition = it.toInt()
                                mediaPlayer?.seekTo(it.toInt())
                            },
                            valueRange = 0f..duration.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formatTime(currentPosition), style = MaterialTheme.typography.labelSmall)
                            Text(text = formatTime(duration), style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls Row - Removed volume logic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { mediaPlayer?.seekTo(currentPosition - 10000) }) {
                            Icon(Icons.Default.Replay10, null, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        FloatingActionButton(
                            onClick = {
                                onPlayingStateChange(!isPlaying)
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        IconButton(onClick = { mediaPlayer?.seekTo(currentPosition + 30000) }) {
                            Icon(Icons.Default.Forward30, null, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))
    return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
