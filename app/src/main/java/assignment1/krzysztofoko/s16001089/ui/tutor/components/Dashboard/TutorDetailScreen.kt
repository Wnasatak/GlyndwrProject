package assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

@Composable
fun TutorDetailScreen(
    viewModel: TutorViewModel
) {
    val tutorProfile by viewModel.tutorProfile.collectAsState()
    val userLocal by viewModel.currentUserLocal.collectAsState()
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    
    var isEditing by remember { mutableStateOf(false) }
    var showAssignmentDialog by remember { mutableStateOf(false) }
    
    // Editable state
    var editBio by remember(tutorProfile) { mutableStateOf(tutorProfile?.bio ?: "") }
    var editDept by remember(tutorProfile) { mutableStateOf(tutorProfile?.department ?: "") }
    var editHours by remember(tutorProfile) { mutableStateOf(tutorProfile?.officeHours ?: "") }
    var editTitle by remember(tutorProfile) { mutableStateOf(tutorProfile?.title ?: "") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    photoUrl = userLocal?.photoUrl ?: tutorProfile?.photoUrl,
                    modifier = Modifier.size(120.dp),
                    isLarge = true
                )
                Spacer(Modifier.height(16.dp))
                
                val displayName = buildString {
                    if (!tutorProfile?.title.isNullOrEmpty()) {
                        append(tutorProfile?.title)
                        append(" ")
                    }
                    // Fix: Prioritize userLocal name for identity consistency
                    append(userLocal?.name ?: tutorProfile?.name ?: "Senior Tutor")
                }
                
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isEditing) "Editing Professional Profile" else (tutorProfile?.department ?: "Faculty"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (!isEditing) {
                    Button(
                        onClick = { isEditing = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Professional Details")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done")
                        }
                        Button(
                            onClick = {
                                viewModel.updateTutorProfile(editBio, editDept, editHours, editTitle)
                                isEditing = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Info")
                        }
                    }
                }
            }
        }

        if (isEditing) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("About You", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title (e.g. Prof, Dr)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editDept,
                        onValueChange = { editDept = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editHours,
                        onValueChange = { editHours = it },
                        label = { Text("Office Hours") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Biography") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        } else {
            item {
                TutorInfoCard(
                    icon = Icons.Default.Info,
                    title = "Biography",
                    content = tutorProfile?.bio ?: "No biography available at this time."
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TutorMiniInfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = userLocal?.email ?: tutorProfile?.email ?: "Not listed"
                    )
                    TutorMiniInfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        label = "Office Hours",
                        value = tutorProfile?.officeHours ?: "By appointment"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assigned Classes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Small inline manage button
                    IconButton(
                        onClick = { showAssignmentDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.LibraryAdd, 
                            null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (assignedCourses.isEmpty()) {
                item {
                    Text(
                        "No courses assigned yet. Use the plus button above to link classes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            } else {
                items(assignedCourses) { course ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = course.title, 
                                fontWeight = FontWeight.Medium, 
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showAssignmentDialog) {
        CourseAssignmentDialog(
            viewModel = viewModel,
            onDismiss = { showAssignmentDialog = false }
        )
    }
}

@Composable
fun CourseAssignmentDialog(
    viewModel: TutorViewModel,
    onDismiss: () -> Unit
) {
    val allCourses by viewModel.allCourses.collectAsState()
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val assignedIds = assignedCourses.map { it.id }.toSet()
    val filteredCourses = allCourses.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Manage Assignments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search university courses...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCourses) { course ->
                        val isAssigned = assignedIds.contains(course.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (isAssigned) viewModel.unassignCourseFromSelf(course.id)
                                    else viewModel.assignCourseToSelf(course.id)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAssigned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = if (isAssigned) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(course.department, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                if (isAssigned) {
                                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(Icons.Default.AddCircleOutline, null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun TutorInfoCard(icon: ImageVector, title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TutorMiniInfoCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
