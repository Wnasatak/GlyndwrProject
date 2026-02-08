package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage

/**
 * Course-specific shortcut headers for the Dashboard and Home screens.
 */

@Composable
fun EnrolledCourseHeader(
    course: Book,
    isLive: Boolean = false,
    onEnterClassroom: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
    ) {
        Box {
            if (isLive) {
                val liveBadgeTransition = rememberInfiniteTransition(label = "live_badge")
                val liveBadgeAlpha by liveBadgeTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "liveBadgeAlpha"
                )
                Surface(
                    color = Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 0.dp),
                    modifier = Modifier.align(Alignment.TopEnd),
                    border = BorderStroke(0.5.dp, Color.Red.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .graphicsLayer { alpha = liveBadgeAlpha }
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            color = Color.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = formatAssetUrl(course.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                @Suppress("DEPRECATION")
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MY COURSE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onEnterClassroom(course.id) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Default.School, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Enter Classroom", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FreeCourseHeader(
    course: Book,
    isLive: Boolean = false,
    onEnterClassroom: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = formatAssetUrl(course.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                if (isLive) {
                    val pulseTransition = rememberInfiniteTransition(label = "free_pulse")
                    val pulseAlpha by pulseTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.2f,
                        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                        label = "freePulseAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .graphicsLayer { alpha = pulseAlpha }
                            .background(Color.Red, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            @Suppress("DEPRECATION")
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FREE COURSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(
                onClick = { onEnterClassroom(course.id) },
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Enter", style = MaterialTheme.typography.labelMedium)
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}
