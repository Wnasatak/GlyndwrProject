package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SpinningLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    AsyncImage(
        model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
        contentDescription = "Loading...",
        modifier = modifier
            .clip(CircleShape)
            .rotate(rotation),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun HorizontalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 12000,
    wave1HeightFactor: Float = 0.75f,
    wave2HeightFactor: Float = 0.82f,
    wave1Amplitude: Float = 60f,
    wave2Amplitude: Float = 40f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val waveColor1 = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE) 
    val waveColor2 = if (isDarkTheme) Color(0xFF334155) else Color(0xFFBFDBFE) 

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        
        val width = size.width
        val height = size.height
        
        // Horizontal Wave 1
        val path1 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave1HeightFactor + sin(relativeX * 2 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)
        
        // Horizontal Wave 2
        val path2 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave2HeightFactor + sin(relativeX * 3 * PI - phase * 0.7f).toFloat() * wave2Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.8f))
    }
}

@Composable
fun VerticalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 10000,
    wave1WidthFactor: Float = 0.6f,
    wave2WidthFactor: Float = 0.75f,
    wave1Amplitude: Float = 100f,
    wave2Amplitude: Float = 60f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val waveColor1 = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE) 
    val waveColor2 = if (isDarkTheme) Color(0xFF334155) else Color(0xFFBFDBFE) 

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height
        
        // Vertical Wave 1
        val path1 = Path().apply { 
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) { 
                val relativeY = y.toFloat() / height
                val x = width * wave1WidthFactor + sin(relativeY * 1.5 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x, y.toFloat()) 
            }
            lineTo(width, height)
            close() 
        }
        drawPath(path1, color = waveColor1)
        
        // Vertical Wave 2
        val path2 = Path().apply { 
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) { 
                val relativeY = y.toFloat() / height
                val x = width * wave2WidthFactor + sin(relativeY * 2.5 * PI - phase * 0.8f).toFloat() * wave2Amplitude
                lineTo(x, y.toFloat()) 
            }
            lineTo(width, height)
            close() 
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.7f))
    }
}

@Composable
fun InfoCard(
    icon: ImageVector, 
    title: String, 
    content: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
    contentStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    border: BorderStroke? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(text = content, style = contentStyle)
            }
        }
    }
}

@Composable
fun rememberGlowAnimation(): Pair<Float, Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    return scale to alpha
}

@Composable
fun BookItemCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageOverlay: @Composable (BoxScope.() -> Unit)? = null,
    cornerContent: @Composable (BoxScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null,
    statusBadge: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    imageOverlay?.invoke(this)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        trailingContent?.invoke(this)
                    }
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    bottomContent?.invoke(this)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = if (book.isAudioBook) "Audio" else book.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        statusBadge?.invoke(this)
                    }
                }
            }
            
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                cornerContent?.invoke(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(category) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 1.0f)
        )
    )
}

@Composable
fun SelectionOption(
    title: String, 
    icon: ImageVector, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        shape = RoundedCornerShape(12.dp), 
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f), 
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
fun UserAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: Int = 24,
    isLarge: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null,
    overlay: @Composable (BoxScope.() -> Unit)? = null
) {
    val defaultAvatarPath = "file:///android_asset/images/users/avatars/Avatar_defult.png"
    val isUsingDefault = photoUrl.isNullOrEmpty() || photoUrl.contains("Avatar_defult")
    
    val avatarModifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    val infiniteTransition = rememberInfiniteTransition(label = "avatarLoad")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (!photoUrl.isNullOrEmpty()) photoUrl else defaultAvatarPath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = avatarModifier,
                contentScale = if (isUsingDefault) ContentScale.Fit else contentScale,
                loading = { 
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        AsyncImage(
                            model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
                            contentDescription = "Loading...",
                            modifier = Modifier.fillMaxSize().clip(CircleShape).rotate(rotation),
                            contentScale = ContentScale.Fit
                        )
                    } 
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isLarge) Icons.Default.Person else Icons.Default.AccountCircle, 
                            contentDescription = null, 
                            modifier = Modifier.size(iconSize.dp), 
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            overlay?.invoke(this)
        }
    }
}
