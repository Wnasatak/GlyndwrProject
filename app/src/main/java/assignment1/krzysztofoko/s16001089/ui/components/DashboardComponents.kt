package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import java.util.*

@Composable
fun SpinningAudioButton(
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    
    // Smooth rotation speed transition
    val rotationSpeed by animateFloatAsState(
        targetValue = if (isPlaying) 3000f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "rotationSpeed"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (rotationSpeed > 0) (3000 * (3000 / rotationSpeed.coerceAtLeast(1f))).toInt() else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Simpler rotation for better smoothness
    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "baseRotation"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size.dp)) {
        // Spinning Frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isPlaying || rotationSpeed > 0) {
                rotate(baseRotation) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF00BCD4), // Cyan
                                Color(0xFFE91E63), // Pink
                                Color(0xFF00BCD4)  // Cyan
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            } else {
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // Play/Pause Button with Smooth Icon Transition
        FilledIconButton(
            onClick = onToggle,
            modifier = Modifier.size((size * 0.85).dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f) togetherWith
                    fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
                },
                label = "iconTransition"
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size((size * 0.5).dp)
                )
            }
        }
    }
}

@Composable
fun DashboardHeader(
    name: String, 
    photoUrl: String?, 
    role: String, 
    balance: Double, 
    onTopUp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp), 
        shape = RoundedCornerShape(32.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(photoUrl = photoUrl, modifier = Modifier.size(64.dp), isLarge = true)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Welcome, $name", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
                        Text(text = role.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Account Balance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("£${String.format(Locale.US, "%.2f", balance)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = onTopUp, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Top Up")
                }
            }
        }
    }
}

@Composable
fun GrowingLazyRow(
    books: List<Book>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onBookClick: (Book) -> Unit
) {
    if (books.isEmpty()) return
    
    val listState = rememberLazyListState()
    
    val itemsToShow = remember(books) { List(100) { books }.flatten() }
    val initialIndex = itemsToShow.size / 2 - (itemsToShow.size / 2 % books.size)

    LaunchedEffect(books) {
        listState.scrollToItem(initialIndex)
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp), 
        horizontalArrangement = Arrangement.spacedBy(12.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(itemsToShow) { index, book ->
            Box(
                modifier = Modifier.graphicsLayer {
                    val layoutInfo = listState.layoutInfo
                    val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                    
                    if (itemInfo != null) {
                        val viewportWidth = layoutInfo.viewportEndOffset.toFloat()
                        val itemOffset = itemInfo.offset.toFloat()
                        val itemSize = itemInfo.size.toFloat()
                        
                        val centerOffset = (viewportWidth - itemSize) / 2
                        val distanceFromCenter = kotlin.math.abs(itemOffset - centerOffset)
                        
                        val scale = (1.1f - (distanceFromCenter / (viewportWidth / 2)) * 0.3f).coerceIn(0.8f, 1.1f)

                        scaleX = scale
                        scaleY = scale
                        
                        shadowElevation = (16f * scale).coerceAtLeast(2f)
                        shape = RoundedCornerShape(20.dp)
                        clip = false
                        
                    } else {
                        scaleX = 0.8f
                        scaleY = 0.8f
                    }
                }
            ) {
                WishlistMiniCard(
                    book = book, 
                    icon = icon, 
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                ) { 
                    onBookClick(book) 
                }
            }
        }
    }
}

@Composable
fun AdminQuickActions(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Text("Admin Controls: Manage Catalog & Users", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.primary)
}

@Composable
fun WishlistMiniCard(
    book: Book, 
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Favorite, 
    color: Color, 
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(210.dp) 
            .clickable { onClick() }, 
        shape = RoundedCornerShape(24.dp), 
        colors = CardDefaults.cardColors(containerColor = color), 
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp) 
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ), 
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).align(Alignment.CenterStart).background(Color.Black.copy(alpha = 0.05f)))
                
                if (book.imageUrl.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f)
                                        ),
                                        startY = 0.6f
                                    )
                                )
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (book.isAudioBook) Icons.Default.Headphones else if (book.mainCategory == "University Courses") Icons.Default.School else Icons.AutoMirrored.Filled.MenuBook, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
                Icon(icon, null, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(24.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = book.title, 
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.ExtraBold, 
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = book.author, 
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                    color = Color.Gray, 
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun EmptySectionPlaceholder(text: String) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = Color.Transparent, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))) {
        Text(text = text, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun EmptyLibraryPlaceholder(onBrowse: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.LibraryBooks, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Your library is empty", fontWeight = FontWeight.Bold)
        Text("Get books, courses or gear to see them here.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBrowse, shape = RoundedCornerShape(12.dp)) { Text("Explore Store") }
    }
}
