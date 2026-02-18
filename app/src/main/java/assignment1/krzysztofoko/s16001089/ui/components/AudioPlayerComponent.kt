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
 * AudioPlayerComponent Composable
 *
 * A sophisticated, state-aware audio player UI that seamlessly transitions between a compact, 
 * minimized bar and an immersive, maximized view. This component is deeply integrated with the
 * Media3 playback library, providing users with a rich and responsive control experience.
 *
 * Key Features:
 * - **Dual-State UI:** Switches between a minimal bar (for background listening) and a full-featured 
 *   maximized player via the `isMinimized` flag.
 * - **Media3 Integration:** Hooks into a `Player` instance to reflect real-time playback state,
 *   including `isPlaying`, `duration`, and `currentPosition`.
 * - **Lifecycle Aware:** Uses `DisposableEffect` to safely add and remove the player listener, preventing memory leaks.
 * - **Live Progress Tracking:** Employs a `LaunchedEffect` to update the current position every second while playing.
 * - **Rich Animations:** Features multiple looping animations, including a dynamic background gradient, a cover art
 *   shimmer effect, and a custom `SquigglySlider` for progress, to create a lively and engaging UI.
 *
 * @param book The `Book` object containing metadata like title, author, and image URL.
 * @param isMinimized A boolean that dictates whether to render the minimized or maximized view.
 * @param onToggleMinimize Callback invoked to switch between the minimized and maximized states.
 * @param onClose Callback to completely dismiss and stop the audio player.
 * @param isDarkTheme Flag to adapt background components (not directly used here but passed to sub-composables).
 * @param player The nullable `Player` instance from Media3 (ExoPlayer) that controls the audio.
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
    // Internal state variables that react to changes from the Media3 Player.
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    // This effect binds the composable's state to the external Player instance.
    // It correctly adds a listener when the player is available and cleans it up when the composable is disposed.
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            // Update UI state when playback starts or stops.
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                duration = player?.duration?.coerceAtLeast(0L) ?: 0L
            }
            // Ensure duration is updated if it changes (e.g., in a live stream or as metadata loads).
            override fun onPlaybackStateChanged(state: Int) {
                duration = player?.duration?.coerceAtLeast(0L) ?: 0L
            }
        }
        player?.addListener(listener)
        // Set initial state immediately upon composition.
        isPlaying = player?.isPlaying ?: false
        duration = player?.duration?.coerceAtLeast(0L) ?: 0L

        // The onDispose block is crucial for preventing leaks.
        onDispose {
            player?.removeListener(listener)
        }
    }

    // This effect creates a coroutine that periodically updates the `currentPosition`.
    // It only runs when the player exists and is actively playing, and restarts if `isPlaying` changes.
    LaunchedEffect(player, isPlaying) {
        if (isPlaying && player != null) {
            while (isActive) { // `isActive` is a check from the CoroutineScope.
                currentPosition = player.currentPosition
                delay(1000) // Poll for new position every second.
            }
        }
    }

    // A subtle, continuous animation for the background gradient to give the UI a dynamic, premium feel.
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

    // The root layout conditionally renders the minimized or maximized view.
    Box(modifier = Modifier.padding(if (isMinimized) 0.dp else 16.dp)) {
        if (isMinimized) {
            // --- MINIMIZED PLAYER BAR ---
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
                // The animated gradient provides a subtle visual texture.
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
                    // A thin progress line gives at-a-glance feedback on playback position.
                    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                    
                    // Bar contents: Cover, Title/Author, Play/Pause, Close.
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
                        // Play/Pause toggle button.
                        IconButton(onClick = {
                            if (isPlaying) player?.pause() else player?.play()
                        }) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        // Close button to dismiss the player entirely.
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        } else {
            // --- MAXIMIZED PLAYER VIEW ---
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
                    // Header with minimize and close controls.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleMinimize) {
                            Icon(Icons.Default.KeyboardArrowDown, "Minimize")
                        }
                        Text("Now Playing", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    // Large cover art with a conditional shimmer effect.
                    val imageSize = 200.dp
                    Box(
                        modifier = Modifier.size(imageSize).clip(RoundedCornerShape(20.dp))
                    ) {
                        AsyncImage(model = book.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        
                        // This shimmering gradient is only rendered when music is actively playing.
                        if (isPlaying) {
                            val diagonalTransition = rememberInfiniteTransition(label = "diagonalShade")
                            val animProgress by diagonalTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(15000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
                                label = "animProgress"
                            )
                            
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.linearGradient(
                                        0.0f to Color.Transparent, 0.45f to Color.Transparent, 0.5f to Color.White.copy(alpha = 0.3f), 0.55f to Color.Transparent, 1.0f to Color.Transparent,
                                        start = Offset(with(density) { 200.dp.toPx() } * (animProgress - 0.5f) * 2f, with(density) { 200.dp.toPx() } * (animProgress - 0.5f) * 2f),
                                        end = Offset(with(density) { 200.dp.toPx() } * (animProgress + 0.5f) * 2f, with(density) { 200.dp.toPx() } * (animProgress + 0.5f) * 2f)
                                    )
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Audiobook metadata.
                    Text(book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(book.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(24.dp))

                    // The custom slider for seeking and progress visualization.
                    SquigglySlider(
                        progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        isPlaying = isPlaying,
                        onValueChange = { newProgress -> 
                            player?.seekTo((newProgress * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )

                    // Time labels showing current position vs total duration.
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = formatTime(currentPosition.toInt()), style = MaterialTheme.typography.labelSmall)
                        Text(text = formatTime(duration.toInt()), style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main playback controls with large central Play/Pause FAB.
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { player?.seekTo(currentPosition - 10000) }) { Icon(Icons.Default.Replay10, null, modifier = Modifier.size(28.dp)) }
                        Spacer(modifier = Modifier.width(32.dp))
                        FloatingActionButton(
                            onClick = { if (isPlaying) player?.pause() else player?.play() },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        IconButton(onClick = { player?.seekTo(currentPosition + 30000) }) { Icon(Icons.Default.Forward30, null, modifier = Modifier.size(28.dp)) }
                    }
                }
            }
        }
    }
}

/**
 * SquigglySlider Composable
 *
 * A highly custom and decorative slider that replaces the standard `Slider` with a `Canvas`-based
 * implementation. It visualizes progress with a wavy line that animates when `isPlaying` is true.
 * A key feature is the invisible `Slider` laid on top, which transparently handles all user touch input
 * (scrubbing) for a robust and simplified implementation.
 *
 * @param progress The current progress, represented as a float from 0.0 to 1.0.
 * @param isPlaying A boolean that controls whether the wave animation is active.
 * @param onValueChange A lambda that reports the new progress value when the user scrubs the slider.
 * @param modifier The modifier to be applied to the slider.
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
    
    // An infinite animation that drives the sine wave's phase, creating the "moving wave" effect.
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Box(modifier = modifier) {
        // The custom drawing layer.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val progressX = width * progress
            
            // 1. Draw the inactive (background) part of the track.
            drawLine(color = trackColor, start = Offset(progressX, centerY), end = Offset(width, centerY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            
            // 2. Draw the active (foreground) part of the track as a path.
            val path = Path()
            path.moveTo(0f, centerY)
            
            val waveAmplitude = if (isPlaying) 6.dp.toPx() else 0f // Wave is flat when paused.
            val waveFrequency = 0.05f
            
            // Generate the points for the squiggly line up to the current progress.
            for (x in 0..progressX.toInt()) {
                val y = if (isPlaying) {
                    centerY + waveAmplitude * sin(x * waveFrequency + phase)
                } else {
                    centerY
                }
                path.lineTo(x.toFloat(), y)
            }
            
            drawPath(path = path, color = primaryColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
            
            // 3. Draw the draggable thumb, which follows the wave.
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = Offset(progressX, if (isPlaying) centerY + waveAmplitude * sin(progressX * waveFrequency + phase) else centerY)
            )
        }
        
        // 4. An invisible, standard Slider is placed on top to handle all touch input.
        // This is much simpler and more robust than handling raw pointer input on the Canvas.
        Slider(
            value = progress,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

/**
 * A simple utility function to convert a duration in milliseconds into a user-friendly
 * time format string (e.g., "01:23" or "01:15:45").
 *
 * @param milliseconds The duration in milliseconds.
 * @return A formatted string representation of the time.
 */
private fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))
    return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
