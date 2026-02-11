package assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

@Composable
fun TutorDetailScreen(
    viewModel: TutorViewModel,
    onNavigateToProfile: () -> Unit = {} // Added navigation parameter
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

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Standard) { isTablet ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AdaptiveSpacing.contentPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AdaptiveDashboardHeader(
                    title = "Teacher Profile",
                    subtitle = "Manage your professional university identity",
                    icon = Icons.Default.Badge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                AdaptiveDashboardCard { cardIsTablet ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(
                            photoUrl = userLocal?.photoUrl ?: tutorProfile?.photoUrl,
                            modifier = Modifier.size(if (cardIsTablet) 120.dp else 100.dp),
                            isLarge = true
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        val displayName = buildString {
                            if (!tutorProfile?.title.isNullOrEmpty()) {
                                append(tutorProfile?.title)
                                append(" ")
                            }
                            append(userLocal?.name ?: tutorProfile?.name ?: "Senior Tutor")
                        }
                        
                        Text(
                            text = displayName,
                            style = if (cardIsTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isEditing) "Editing Professional Profile" else (tutorProfile?.department ?: "Faculty"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        
                        if (!isEditing) {
                            Row(
                                modifier = Modifier.adaptiveButtonWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { isEditing = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit Details", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                
                                Spacer(Modifier.width(8.dp))
                                
                                FilledIconButton(
                                    onClick = onNavigateToProfile,
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Settings, "Account Settings")
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.adaptiveButtonWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { isEditing = false },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Done")
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateTutorProfile(editBio, editDept, editHours, editTitle)
                                        isEditing = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }

            if (isEditing) {
                item {
                    AdaptiveDashboardCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Professional Information", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
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
                }
            } else {
                item {
                    TutorAdaptiveInfoCard(
                        icon = Icons.Default.Info,
                        title = "Biography",
                        content = tutorProfile?.bio ?: "No biography available at this time."
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TutorAdaptiveMiniCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = userLocal?.email ?: tutorProfile?.email ?: "Not listed"
                        )
                        TutorAdaptiveMiniCard(
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
                            fontWeight = FontWeight.Black
                        )
                        
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
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                } else {
                    items(assignedCourses) { course ->
                        AdaptiveDashboardCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = course.title, 
                                    fontWeight = FontWeight.Bold, 
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
    }

    if (showAssignmentDialog) {
        CourseAssignmentDialog(
            viewModel = viewModel,
            onDismiss = { showAssignmentDialog = false }
        )
    }
}

@Composable
fun TutorAdaptiveInfoCard(icon: ImageVector, title: String, content: String) {
    AdaptiveDashboardCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TutorAdaptiveMiniCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    AdaptiveDashboardCard(modifier = modifier) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
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
            AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Standard) { isTablet ->
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
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
                                border = if (isAssigned) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
                                         else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
