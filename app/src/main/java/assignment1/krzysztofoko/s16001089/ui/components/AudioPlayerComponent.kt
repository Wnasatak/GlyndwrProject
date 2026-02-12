package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sin

/**
 * AudioPlayerComponent is a versatile media player UI that supports both minimized (bar)
 * and maximized (full-screen overlay) states. It integrates with Media3 (ExoPlayer)
 * to provide real-time playback control, progress tracking, and animated visual feedback.
 */
@Composable
fun AudioPlayerComponent(
    book: Book,
    isMinimized: Boolean,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit,
    isDarkTheme: Boolean,
    player: Player?
) {
    // Local state for tracking player progress and status
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    // Lifecycle-aware sync with the Media3 player instance
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                duration = player?.duration?.coerceAtLeast(0L) ?: 0L
            }
            override fun onPlaybackStateChanged(state: Int) {
                duration = player?.duration?.coerceAtLeast(0L) ?: 0L
            }
        }
        player?.addListener(listener)
        // Initial state capture
        isPlaying = player?.isPlaying ?: false
        duration = player?.duration?.coerceAtLeast(0L) ?: 0L

        onDispose {
            player?.removeListener(listener)
        }
    }

    // Coroutine-driven progress update (refreshes every second while playing)
    LaunchedEffect(player, isPlaying) {
        if (isPlaying && player != null) {
            while (isActive) {
                currentPosition = player.currentPosition
                delay(1000)
            }
        }
    }

    // Background animation: Shifting ambient gradient for a dynamic feel
    val infiniteTransition = rememberInfiniteTransition(label = "ambientGradient")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetAnim"
    )

    // Layout structure depends on the minimization state
    Box(modifier = Modifier.padding(if (isMinimized) 0.dp else 16.dp)) {
        if (isMinimized) {
            // VIEW: MINIMIZED (Bottom Bar)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable { onToggleMinimize() },
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                // Animated background gradient
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                ),
                                start = Offset(offsetAnim, 0f),
                                end = Offset(offsetAnim + 400f, 100f)
                            )
                        )
                )

                Column {
                    // Slim progress indicator at the very top of the bar
                    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                    
                    // Main content: Thumbnail, title, and simple controls
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(4.dp)), 
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.labelLarge,
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
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        } else {
            // VIEW: MAXIMIZED (Full-screen Card Style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                // Background Gradient (more pronounced in maximized state)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                ),
                                start = Offset(offsetAnim, offsetAnim),
                                end = Offset(offsetAnim + 500f, offsetAnim + 500f)
                            )
                        )
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Controls to minimize or close
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

                    Spacer(Modifier.height(16.dp))
                    
                    // Large Cover Image with dynamic "shimmer" effect while playing
                    val imageSize = 200.dp
                    Box(
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Diagonal shimmer animation for active playback
                        if (isPlaying) {
                            val diagonalTransition = rememberInfiniteTransition(label = "diagonalShade")
                            val animProgress by diagonalTransition.animateFloat(
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
                                            start = Offset(with(density) { 200.dp.toPx() } * (animProgress - 0.5f) * 2f, with(density) { 200.dp.toPx() } * (animProgress - 0.5f) * 2f),
                                            end = Offset(with(density) { 200.dp.toPx() } * (animProgress + 0.5f) * 2f, with(density) { 200.dp.toPx() } * (animProgress + 0.5f) * 2f)
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Title and Author information
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom animated "Squiggly" progress slider
                    SquigglySlider(
                        progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        isPlaying = isPlaying,
                        onValueChange = { 
                            player?.seekTo((it * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )

                    // Time labels (current vs total)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(currentPosition.toInt()), style = MaterialTheme.typography.labelSmall)
                        Text(text = formatTime(duration.toInt()), style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Playback Controls: Replay, Play/Pause, Fast Forward
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { player?.seekTo(currentPosition - 10000) }) {
                            Icon(Icons.Default.Replay10, null, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        FloatingActionButton(
                            onClick = {
                                if (isPlaying) player?.pause() else player?.play()
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        IconButton(onClick = { player?.seekTo(currentPosition + 30000) }) {
                            Icon(Icons.Default.Forward30, null, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A custom slider component that renders a "squiggly" or wavy path for the active progress.
 * The wave animates while playback is active.
 *
 * @param progress Float between 0 and 1 representing the current playback position.
 * @param isPlaying Boolean to toggle the wave animation.
 * @param onValueChange Lambda triggered when the user scrubs the slider.
 */
@Composable
fun SquigglySlider(
    progress: Float,
    isPlaying: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    
    // Phase animation for the wavy progress path
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
            
            // Draw the background (inactive) track
            drawLine(
                color = trackColor,
                start = Offset(progressX, centerY),
                end = Offset(width, centerY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Generate the wavy path for active progress
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
            
            // Draw the thumb indicator
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = Offset(progressX, if (isPlaying) centerY + waveAmplitude * sin(progressX * waveFrequency + phase) else centerY)
            )
        }
        
        // Invisible slider overlay for touch interaction
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

/**
 * Utility to format millisecond duration into a readable time string (HH:mm:ss or mm:ss).
 */
private fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))
    return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
