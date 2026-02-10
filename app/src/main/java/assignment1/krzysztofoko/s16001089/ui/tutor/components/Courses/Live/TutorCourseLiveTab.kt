package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Live

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorCourseLiveTab(
    viewModel: TutorViewModel
) {
    val course by viewModel.selectedCourse.collectAsState()
    val isLive by viewModel.isLive.collectAsState()
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    val selectedCourseModules by viewModel.selectedCourseModules.collectAsState()
    val selectedModuleId by viewModel.selectedModuleId.collectAsState()
    val assignments by viewModel.selectedCourseAssignments.collectAsState()
    val selectedAssignmentId by viewModel.selectedAssignmentId.collectAsState()
    val previousBroadcasts by viewModel.previousBroadcasts.collectAsState()

    var step by remember(course) { mutableIntStateOf(if (course == null) 1 else 2) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLive) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Progress Stepper - Full Width Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveStepIndicator(number = 1, label = "Course", active = step >= 1, completed = step > 1)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 1) MaterialTheme.colorScheme.primary else Color.Gray)
                    LiveStepIndicator(number = 2, label = "Module", active = step >= 2, completed = step > 2)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 2) MaterialTheme.colorScheme.primary else Color.Gray)
                    LiveStepIndicator(number = 3, label = "Ready", active = step >= 3, completed = step > 3)
                }

                // Adaptive Content Area
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .adaptiveWidth(AdaptiveWidths.Wide)
                            .padding(horizontal = 16.dp)
                    ) {
                        when (step) {
                            1 -> {
                                Text("Select Course for Live Stream", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(16.dp))
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(assignedCourses) { c ->
                                        LiveSelectionCard(
                                            title = c.title,
                                            subtitle = c.department,
                                            icon = Icons.Default.School,
                                            selected = course?.id == c.id,
                                            onClick = {
                                                viewModel.updateSelectedCourse(c.id)
                                                step = 2
                                            }
                                        )
                                    }
                                }
                            }
                            2 -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (course != null && assignedCourses.size > 1) {
                                        IconButton(onClick = { step = 1 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                                    }
                                    Text("Select Module topic", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                course?.let {
                                    Text(
                                        text = "Course: ${it.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = if (assignedCourses.size > 1) 48.dp else 0.dp)
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                if (selectedCourseModules.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                                            Spacer(Modifier.height(16.dp))
                                            Text("No modules found. Please create one to start a live session.", color = Color.Gray)
                                        }
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(selectedCourseModules) { module ->
                                            LiveSelectionCard(
                                                title = module.title,
                                                subtitle = "Topic: ${module.contentType}",
                                                icon = Icons.Default.ViewModule,
                                                selected = selectedModuleId == module.id,
                                                onClick = {
                                                    viewModel.selectModule(module.id)
                                                    step = 3
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            3 -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { step = 2 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                                    Text("Final Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }

                                Spacer(Modifier.height(24.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Podcasts, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(12.dp))
                                        Text(text = "Link to Assignment (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "The replay will be attached to this assignment.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        
                                        Spacer(Modifier.height(24.dp))
                                        
                                        // Compact Dropdown Selector
                                        var expanded by remember { mutableStateOf(false) }
                                        val filteredAssignments = assignments.filter { it.moduleId == selectedModuleId }
                                        val selectedTitle = if (selectedAssignmentId == null) "None (General Session)" 
                                                           else filteredAssignments.find { it.id == selectedAssignmentId }?.title ?: "Select Assignment"

                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedTextField(
                                                value = selectedTitle,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Choose Assignment", fontSize = 12.sp) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                textStyle = MaterialTheme.typography.bodyMedium
                                            )

                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("None (General Session)", style = MaterialTheme.typography.bodyMedium) },
                                                    onClick = {
                                                        viewModel.selectAssignment(null)
                                                        expanded = false
                                                    },
                                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                                )
                                                filteredAssignments.forEach { assignment ->
                                                    DropdownMenuItem(
                                                        text = { Text(assignment.title, style = MaterialTheme.typography.bodyMedium) },
                                                        onClick = {
                                                            viewModel.selectAssignment(assignment.id)
                                                            expanded = false
                                                        },
                                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(32.dp))

                                        Button(
                                            onClick = {
                                                viewModel.toggleLiveStream(true)
                                            },
                                            modifier = Modifier.fillMaxWidth().height(56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                                        ) {
                                            Icon(Icons.Default.VideoCall, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Start Broadcasting Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Archive Entry Point
                        Spacer(Modifier.height(32.dp))
                        OutlinedButton(
                            onClick = { viewModel.setSection(TutorSection.COURSE_ARCHIVED_BROADCASTS) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.History, null)
                            Spacer(Modifier.width(12.dp))
                            Text("View Session Archive (${previousBroadcasts.size})", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        } else {
            BroadcastStudio(
                viewModel = viewModel,
                courseTitle = course?.title
            )
        }
    }
}

@Composable
fun LiveStepIndicator(number: Int, label: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = when {
                completed -> MaterialTheme.colorScheme.primary
                active -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Gray.copy(alpha = 0.2f)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (completed) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = number.toString(),
                        fontWeight = FontWeight.Bold,
                        color = if (active) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
        @Suppress("DEPRECATION")
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun LiveSelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                @Suppress("DEPRECATION")
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (selected) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}
