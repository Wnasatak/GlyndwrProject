package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun AudioPlayerComponent(
    book: Book,
    isMinimized: Boolean,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit,
    isDarkTheme: Boolean,
    player: Player?
) {
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(player) {
        player?.let {
            isPlaying = it.isPlaying
            duration = it.duration.coerceAtLeast(0L)
            
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                    duration = it.duration.coerceAtLeast(0L)
                }
                override fun onPlaybackStateChanged(state: Int) {
                    duration = it.duration.coerceAtLeast(0L)
                }
            }
            it.addListener(listener)
            
            while (true) {
                currentPosition = it.currentPosition
                delay(500)
            }
        }
    }

    Box(modifier = Modifier.padding(16.dp)) {
        if (isMinimized) {
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
                        if (isPlaying) player?.pause() else player?.play()
                    }) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
        } else {
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
                    
                    // IMAGE WITH VERY SLOW DIAGONAL SHADE
                    val imageSize = 180.dp
                    val imageSizePx = with(LocalDensity.current) { imageSize.toPx() }
                    
                    Box(
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        if (isPlaying) {
                            val infiniteTransition = rememberInfiniteTransition(label = "diagonalShade")
                            // Increased duration to 15 seconds for a much slower movement
                            val animProgress by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(15000, easing = LinearOutSlowInEasing), 
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "animProgress"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            0.0f to Color.Transparent,
                                            0.45f to Color.Transparent,
                                            0.5f to Color.White.copy(alpha = 0.3f),
                                            0.55f to Color.Transparent,
                                            1.0f to Color.Transparent,
                                            start = androidx.compose.ui.geometry.Offset(
                                                x = imageSizePx * (animProgress - 0.5f) * 2f,
                                                y = imageSizePx * (animProgress - 0.5f) * 2f
                                            ),
                                            end = androidx.compose.ui.geometry.Offset(
                                                x = imageSizePx * (animProgress + 0.5f) * 2f,
                                                y = imageSizePx * (animProgress + 0.5f) * 2f
                                            )
                                        )
                                    )
                            )
                        }
                    }

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

                    Spacer(modifier = Modifier.height(24.dp))

                    SquigglySlider(
                        progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        isPlaying = isPlaying,
                        onValueChange = { 
                            player?.seekTo((it * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(currentPosition.toInt()), style = MaterialTheme.typography.labelSmall)
                        Text(text = formatTime(duration.toInt()), style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { player?.seekTo(currentPosition - 10000) }) {
                            Icon(Icons.Default.Replay10, null, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        FloatingActionButton(
                            onClick = {
                                if (isPlaying) player?.pause() else player?.play()
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
                        IconButton(onClick = { player?.seekTo(currentPosition + 30000) }) {
                            Icon(Icons.Default.Forward30, null, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SquigglySlider(
    progress: Float,
    isPlaying: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val progressX = width * progress
            
            drawLine(
                color = trackColor,
                start = androidx.compose.ui.geometry.Offset(progressX, centerY),
                end = androidx.compose.ui.geometry.Offset(width, centerY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            val path = Path()
            path.moveTo(0f, centerY)
            
            val waveAmplitude = if (isPlaying) 6.dp.toPx() else 0f
            val waveFrequency = 0.05f
            
            for (x in 0..progressX.toInt()) {
                val y = if (isPlaying) {
                    centerY + waveAmplitude * sin(x * waveFrequency + phase)
                } else {
                    centerY
                }
                path.lineTo(x.toFloat(), y.toFloat())
            }
            
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(progressX, if (isPlaying) centerY + waveAmplitude * sin(progressX * waveFrequency + phase) else centerY)
            )
        }
        
        Slider(
            value = progress,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

private fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))
    return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
