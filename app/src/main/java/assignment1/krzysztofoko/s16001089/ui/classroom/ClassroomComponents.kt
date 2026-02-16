package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
 * Specialized view for submitting assignment content and files.
 */
@Composable
fun AssignmentSubmissionView(
    assignment: Assignment,
    isSubmitting: Boolean,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var submissionContent by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf<String?>(null) }
    var isFileAttaching by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = true)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel, enabled = !isSubmitting) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                @Suppress("DEPRECATION")
                Text(text = "SUBMIT ASSIGNMENT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
            }
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(16.dp))
                Text(text = assignment.title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(Modifier.height(12.dp))
                
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        @Suppress("DEPRECATION")
                        Text(text = "Due: ${sdf.format(Date(assignment.dueDate))}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                Text(text = "Description", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(text = assignment.description, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
                }
                
                Spacer(Modifier.height(32.dp))
                Text(text = "Your Submission", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = submissionContent,
                    onValueChange = { submissionContent = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    placeholder = { Text("Add comments or notes for your tutor...", color = Color.White.copy(alpha = 0.4f)) },
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSubmitting,
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), cursorColor = MaterialTheme.colorScheme.primary)
                )
                
                Spacer(Modifier.height(24.dp))

                if (fileName == null) {
                    OutlinedCard(
                        onClick = { if (!isSubmitting && !isFileAttaching) { scope.launch { isFileAttaching = true; delay(1200); fileName = "Assignment1_S16001089_KO.pdf"; isFileAttaching = false } } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
                        enabled = !isSubmitting
                    ) {
                        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            if (isFileAttaching) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                            else Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(text = if (isFileAttaching) "Attaching..." else "Attach Assignment File", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = fileName ?: "file_attached", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                @Suppress("DEPRECATION")
                                Text(text = "Ready to submit", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                            }
                            IconButton(onClick = { fileName = null }, enabled = !isSubmitting) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)) }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
            
            Surface(color = Color.Transparent, modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { val finalContent = if (fileName != null) "$submissionContent [File: $fileName]" else submissionContent; onSubmit(finalContent) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = (submissionContent.isNotBlank() || fileName != null) && !isSubmitting && !isFileAttaching,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    if (isSubmitting) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(12.dp)); Text("UPLOADING SUBMISSION...", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
                    else { Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(12.dp)); Text("SUBMIT ASSIGNMENT", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp) }
                }
            }
        }

        if (isSubmitting) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)).clickable(enabled = false) { }) }
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
                    Text(text = message, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f)) }
                }
            }
        }
    }
}
