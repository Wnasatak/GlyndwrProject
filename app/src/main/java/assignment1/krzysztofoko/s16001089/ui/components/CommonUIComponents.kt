package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryAddCheck
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage

@Composable
fun ProductHeaderImage(
    book: Book,
    isOwned: Boolean,
    isDarkTheme: Boolean,
    primaryColor: Color
) {
    // Animation for moving violet glow
    val infiniteTransition = rememberInfiniteTransition(label = "shadeAnimation")
    val xPos by infiniteTransition.animateFloat(
        initialValue = -0.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "xPos"
    )
    val yPos by infiniteTransition.animateFloat(
        initialValue = -0.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(18000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "yPos"
    )

    Surface(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (book.imageUrl.isNotEmpty()) {
                AsyncImage(model = book.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            
            // 1. Deep Modern Shade (Top to Bottom)
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.75f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            ))

            // 2. Animated Moving Glow (Soft Primary Glow)
            Box(modifier = Modifier.fillMaxSize().drawBehind {
                val brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(size.width * xPos, size.height * yPos),
                    radius = size.maxDimension * 0.8f
                )
                drawRect(brush)
            })

            // 3. Floating Icon with "Pinky White" Transparent Square
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(110.dp)
                    .background(
                        color = Color(0xFFFCE4EC).copy(alpha = 0.25f), // Transparent Pinky White
                        shape = RoundedCornerShape(28.dp)
                    )
                    .drawBehind {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.6f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        book.isAudioBook -> Icons.Default.Headphones
                        book.mainCategory == "University Courses" -> Icons.Default.School
                        else -> Icons.AutoMirrored.Filled.MenuBook
                    }, 
                    contentDescription = null, 
                    modifier = Modifier.size(64.dp), 
                    tint = Color.White
                )
            }
            
            if (isOwned) {
                // Top Badges
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (book.price > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text(text = "Paid", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else { Spacer(Modifier.width(1.dp)) }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LibraryAddCheck, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(text = if (book.price > 0) "PURCHASED" else "IN LIBRARY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
