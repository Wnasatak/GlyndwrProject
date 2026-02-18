package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.*

/**
 * Branded banner showing an active live session at the top of the classroom modules.
 */
@Composable
fun LiveSessionMiniBanner(session: LiveSession, onJoinClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onJoinClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Red.copy(alpha = alpha), CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            @Suppress("DEPRECATION")
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIVE: ${session.tutorName}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.Red
                )
                Text(
                    text = "Lecture in progress. Tap to join now!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(
                imageVector = Icons.Default.Podcasts,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Large interactive card displaying the status of the current or next live lecture.
 */
@Composable
fun LiveBroadcastCard(session: LiveSession?, onJoinClick: () -> Unit) {
    val isLive = session?.isActive == true
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = if (isLive) 2.dp else 1.dp,
            color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLive) 8.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(if (isLive) pulseScale else 1f)
                            .background(if (isLive) Color.Red else Color.Gray, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        text = if (isLive) "LIVE NOW" else "NEXT SESSION",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isLive) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isLive) {
                    Surface(color = Color.Red, shape = RoundedCornerShape(8.dp)) {
                        @Suppress("DEPRECATION")
                        Text("JOIN", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = session?.tutorName?.let { "Lecture with $it" } ?: "No active sessions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = if (isLive) "Tutor is currently broadcasting live. Click below to enter the interactive classroom."
                       else "Check your schedule for the next broadcast update.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isLive) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Podcasts, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Enter Live Classroom", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * System notification banner for course-wide announcements.
 */
@Composable
fun BroadcastBanner(message: String?, onDismiss: () -> Unit) {
    AnimatedVisibility(visible = message != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        if (message != null) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary), elevation = CardDefaults.cardElevation(8.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, null, tint = Color.White)
                    Spacer(Modifier.width(16.dp))
                    @Suppress("DEPRECATION")
                    Text(text = message, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f)) }
                }
            }
        }
    }
}
