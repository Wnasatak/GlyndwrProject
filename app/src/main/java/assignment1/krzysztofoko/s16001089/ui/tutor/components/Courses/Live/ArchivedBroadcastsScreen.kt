package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Live

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import assignment1.krzysztofoko.s16001089.data.LiveSession
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ArchivedBroadcastsScreen(
    viewModel: TutorViewModel
) {
    val course by viewModel.selectedCourse.collectAsState()
    val previousBroadcasts by viewModel.previousBroadcasts.collectAsState()
    val modules by viewModel.selectedCourseModules.collectAsState()
    val assignments by viewModel.selectedCourseAssignments.collectAsState()
    val students by viewModel.enrolledStudentsInSelectedCourse.collectAsState()

    var playingSessionId by remember { mutableStateOf<String?>(null) }
    var renamingSession by remember { mutableStateOf<LiveSession?>(null) }
    var sessionToDelete by remember { mutableStateOf<LiveSession?>(null) }
    var sharingSession by remember { mutableStateOf<LiveSession?>(null) }

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            AdaptiveDashboardHeader(
                title = "Session Archive",
                subtitle = course?.title ?: "All Broadcasts",
                icon = Icons.Default.History
            )

            Spacer(Modifier.height(24.dp))

            if (previousBroadcasts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Podcasts, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        @Suppress("DEPRECATION")
                        Text("No archived broadcasts found.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(previousBroadcasts) { session ->
                        BroadcastArchiveCard(
                            session = session,
                            moduleName = modules.find { it.id == session.moduleId }?.title ?: "General",
                            assignmentName = assignments.find { it.id == session.assignmentId }?.title,
                            isPlaying = playingSessionId == session.id,
                            onDeleteRequest = { sessionToDelete = session },
                            onShareRequest = { sharingSession = session },
                            onRename = { renamingSession = session },
                            onPlayToggle = {
                                playingSessionId = if (playingSessionId == session.id) null else session.id
                            }
                        )
                    }
                }
            }
        }
    }

    if (renamingSession != null) {
        var newTitle by remember { mutableStateOf(renamingSession!!.title) }
        AlertDialog(
            onDismissRequest = { renamingSession = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Rename Broadcast", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Broadcast Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateBroadcastTitle(renamingSession!!.id, newTitle)
                        renamingSession = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { renamingSession = null }) { 
                    Text("Cancel", color = MaterialTheme.colorScheme.primary) 
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (sessionToDelete != null) {
        DeleteBroadcastConfirmationDialog(
            sessionTitle = sessionToDelete!!.title,
            onDismiss = { sessionToDelete = null },
            onConfirm = {
                viewModel.deletePreviousBroadcast(sessionToDelete!!.id)
                sessionToDelete = null
            }
        )
    }

    if (sharingSession != null) {
        ShareBroadcastDialog(
            session = sharingSession!!,
            students = students,
            onDismiss = { sharingSession = null },
            onShareWithAll = {
                viewModel.shareBroadcastWithAll(sharingSession!!)
                sharingSession = null
            },
            onShareWithSpecific = { selectedIds ->
                viewModel.shareBroadcastWithSpecificStudents(sharingSession!!, selectedIds)
                sharingSession = null
            }
        )
    }
}

@Composable
fun ShareBroadcastDialog(
    session: LiveSession,
    students: List<UserLocal>,
    onDismiss: () -> Unit,
    onShareWithAll: () -> Unit,
    onShareWithSpecific: (List<String>) -> Unit
) {
    var selectedStudentIds by remember { mutableStateOf(setOf<String>()) }
    var shareWithAll by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter { it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Share Replay", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                @Suppress("DEPRECATION")
                Text("Select who should receive a notification about this replay.", style = MaterialTheme.typography.bodySmall)
                
                Surface(
                    onClick = { shareWithAll = !shareWithAll },
                    color = if (shareWithAll) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    border = if (shareWithAll) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = shareWithAll, 
                            onCheckedChange = { shareWithAll = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(8.dp))
                        @Suppress("DEPRECATION")
                        Text("Whole Class (${students.size} students)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (!shareWithAll) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search students...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    @Suppress("DEPRECATION")
                    Text("Results (${filteredStudents.size})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredStudents) { student ->
                            val isSelected = selectedStudentIds.contains(student.id)
                            Surface(
                                onClick = {
                                    selectedStudentIds = if (isSelected) selectedStudentIds - student.id else selectedStudentIds + student.id
                                },
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) 
                                        else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) 
                                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(photoUrl = student.photoUrl, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        @Suppress("DEPRECATION")
                                        Text(student.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        @Suppress("DEPRECATION")
                                        Text(student.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            selectedStudentIds = if (it) selectedStudentIds + student.id else selectedStudentIds - student.id
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shareWithAll) onShareWithAll() else onShareWithSpecific(selectedStudentIds.toList())
                },
                enabled = shareWithAll || selectedStudentIds.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Share Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = MaterialTheme.colorScheme.primary) 
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun DeleteBroadcastConfirmationDialog(
    sessionTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmationText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Delete Broadcast", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                @Suppress("DEPRECATION")
                Text(
                    text = "Are you sure you want to permanently delete '$sessionTitle'? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
                @Suppress("DEPRECATION")
                Text(
                    text = "To confirm, please type DELETE below:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    placeholder = { Text("Type DELETE here") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmationText.uppercase() == "DELETE",
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Deletion", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun BroadcastArchiveCard(
    session: LiveSession,
    moduleName: String,
    assignmentName: String?,
    isPlaying: Boolean,
    onDeleteRequest: () -> Unit,
    onShareRequest: () -> Unit,
    onRename: () -> Unit,
    onPlayToggle: () -> Unit
) {
    val isTablet = isTablet()
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(session.startTime))
    val context = LocalContext.current

    val btnHeight = if (isTablet) 44.dp else 34.dp
    val fontSize = if (isTablet) 13.sp else 11.sp
    val iconSize = if (isTablet) 18.dp else 14.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp) 
                             else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            // Player Area
            AnimatedVisibility(visible = isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 300.dp else 200.dp)
                        .background(Color.Black)
                ) {
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            val videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                            val mediaItem = MediaItem.fromUri(videoUrl)
                            setMediaItem(mediaItem)
                            prepare()
                            playWhenReady = true
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose { exoPlayer.release() }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(if (isTablet) 48.dp else 40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VideoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isTablet) 24.dp else 20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        @Suppress("DEPRECATION")
                        Text(
                            text = session.title, 
                            fontWeight = FontWeight.Black, 
                            style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        @Suppress("DEPRECATION")
                        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    
                    Row {
                        IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDeleteRequest, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        ArchiveBadge(icon = Icons.Default.ViewModule, label = moduleName)
                    }
                    if (assignmentName != null) {
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            ArchiveBadge(icon = Icons.Default.Assignment, label = assignmentName, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPlayToggle,
                        modifier = Modifier.weight(1.4f).height(btnHeight),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) MaterialTheme.colorScheme.secondary 
                                             else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, 
                            null, 
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(Modifier.width(6.dp))
                        @Suppress("DEPRECATION")
                        Text(if (isPlaying) "Close" else "Watch Replay", fontSize = fontSize, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onShareRequest,
                        modifier = Modifier.weight(1f).height(btnHeight),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(iconSize))
                        Spacer(Modifier.width(6.dp))
                        @Suppress("DEPRECATION")
                        Text("Share", fontSize = fontSize, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveBadge(icon: ImageVector, label: String, color: Color = MaterialTheme.colorScheme.primary) {
    val isTablet = isTablet()
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(if (isTablet) 14.dp else 12.dp), tint = color)
            Spacer(Modifier.width(4.dp))
            @Suppress("DEPRECATION")
            Text(
                text = label, 
                style = if (isTablet) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), 
                color = color, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
