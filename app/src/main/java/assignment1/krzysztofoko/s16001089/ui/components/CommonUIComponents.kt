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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import java.util.Locale

/**
 * This component creates the beautiful header image for any product (Book, Audio, or Gear).
 * As a student, think of this as the "Hero Section" of the detail screen.
 * It handles the image loading, the moving gradient animations, and the "Purchased" badges.
 */
@Composable
fun ProductHeaderImage(
    book: Book,
    isOwned: Boolean,
    isDarkTheme: Boolean,
    primaryColor: Color
) {
    // These two values create that "shimmer" or "light sweep" effect moving across the image
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
            // 1. Show the actual product photo from the database
            if (book.imageUrl.isNotEmpty()) {
                AsyncImage(model = book.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            
            // 2. Add a dark gradient at the bottom so white text is always readable
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.75f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            ))

            // 3. Apply the moving "light sweep" animation using the primary theme color
            Box(modifier = Modifier.fillMaxSize().drawBehind {
                val brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(size.width * xPos, size.height * yPos),
                    radius = size.maxDimension * 0.8f
                )
                drawRect(brush)
            })

            // 4. Centered Icon Box: Changes based on whether it's an Audiobook, Course, or normal Book
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(110.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), 
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
            
            // 5. If the student already bought this, show the "PURCHASED" badges in the corners
            if (isOwned) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (book.price > 0) {
                        StatusBadge(text = "Paid", icon = Icons.Default.Paid)
                    } else { Spacer(Modifier.width(1.dp)) }

                    StatusBadge(
                        text = if (book.price > 0) "PURCHASED" else "IN LIBRARY",
                        icon = Icons.Default.LibraryAddCheck
                    )
                }
            }
        }
    }
}

/**
 * This function creates the small "Quick View" popup when you tap on a book in the list.
 * It gives a summary (Photo, Title, Description, and Price) so the student doesn't have to navigate away.
 */
@Composable
fun QuickViewDialog(
    book: Book,
    onDismiss: () -> Unit,
    onReadMore: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            // Main action button to go to the full details page
            Button(
                onClick = { 
                    onDismiss()
                    onReadMore(book.id) 
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Show More Details", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = null, // Custom title is inside the 'text' content below
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large preview image of the item
                AsyncImage(
                    model = book.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Item Title
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                // Author or Narrator name
                Text(
                    text = if (book.isAudioBook) "Narrated by ${book.author}" else "by ${book.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Short description box with a shaded background
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = book.description,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Justify
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bottom row showing the Price and the Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (book.price == 0.0) {
                        Text(text = "FREE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                    } else {
                        // Uses the shared 'formatPrice' function we created earlier
                        Text(text = "£${formatPrice(book.price)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text(book.category) })
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp
    )
}
