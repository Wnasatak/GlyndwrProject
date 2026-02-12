package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Live

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * TutorCourseLiveTab acts as the pre-broadcast configuration center for course instructors.
 * It features a guided "Stepper" interface that ensures the tutor selects a course,
 * a specific module topic, and optional assignment linking before going live.
 *
 * Key Features:
 * 1. Progress Stepper: Visual 3-step guide (Course -> Module -> Ready).
 * 2. Dynamic Discovery: Automatically fetches and filters modules and assignments based on the selection.
 * 3. Configuration Memory: Remembers the current step state during UI interaction.
 * 4. Studio Integration: Seamlessly transitions to the 'BroadcastStudio' once configured.
 * 5. Archive Access: Provides a quick navigation link to view previously recorded sessions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorCourseLiveTab(
    viewModel: TutorViewModel
) {
    // REACTIVE DATA: Synchronizes with the tutor's class registry and current session state
    val course by viewModel.selectedCourse.collectAsState()
    val isLive by viewModel.isLive.collectAsState()
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    val selectedCourseModules by viewModel.selectedCourseModules.collectAsState()
    val selectedModuleId by viewModel.selectedModuleId.collectAsState()
    val assignments by viewModel.selectedCourseAssignments.collectAsState()
    val selectedAssignmentId by viewModel.selectedAssignmentId.collectAsState()

    // STEPPER LOGIC: Tracks progress through the live stream setup wizard
    var step by remember { 
        mutableIntStateOf(
            if (viewModel.selectedCourseId.value == null) 1 
            else if (viewModel.selectedModuleId.value == null) 2 
            else 3
        ) 
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLive) {
            // VIEW: CONFIGURATION WIZARD
            Column(modifier = Modifier.fillMaxSize()) {
                
                // STEPPER UI: Horizontal progress indicator with institutional branding
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveStepIndicator(number = 1, label = "Course", active = step >= 1, completed = step > 1)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 1) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
                    LiveStepIndicator(number = 2, label = "Module", active = step >= 2, completed = step > 2)
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 2) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
                    LiveStepIndicator(number = 3, label = "Ready", active = step >= 3, completed = step > 3)
                }

                AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        when (step) {
                            // STEP 1: Identification of the target course
                            1 -> {
                                Text("Select Course for Live Stream", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(16.dp))
                                if (assignedCourses.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                } else {
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
                            }
                            
                            // STEP 2: Selection of the module topic for the session
                            2 -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { step = 1 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                                    Text("Select Module Topic", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Course: ${course?.title ?: "Select Course"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 48.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                if (selectedCourseModules.isEmpty() && course != null) {
                                    Box(Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                                            Spacer(Modifier.height(16.dp))
                                            Text("No modules found for this course.", color = Color.Gray)
                                        }
                                    }
                                } else if (course == null) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
                            
                            // STEP 3: Optional assignment linking and final launch
                            3 -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { step = 2 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                                    Text("Final Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(24.dp))

                                AdaptiveDashboardCard(
                                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(64.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Podcasts, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                            }
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        Text(text = "Link to Assignment (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "The replay will be automatically attached to this assignment.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
                                        
                                        Spacer(Modifier.height(24.dp))
                                        
                                        // ASSIGNMENT DROPDOWN: Filters assignments related to the selected module
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
                                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                                    unfocusedContainerColor = Color.Transparent,
                                                    focusedContainerColor = Color.Transparent
                                                ),
                                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                textStyle = MaterialTheme.typography.bodyMedium
                                            )

                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("None (General Session)", style = MaterialTheme.typography.bodyMedium) },
                                                    onClick = { viewModel.selectAssignment(null); expanded = false },
                                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                                )
                                                filteredAssignments.forEach { assignment ->
                                                    DropdownMenuItem(
                                                        text = { Text(assignment.title, style = MaterialTheme.typography.bodyMedium) },
                                                        onClick = { viewModel.selectAssignment(assignment.id); expanded = false },
                                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(32.dp))

                                        // LAUNCH TRIGGER: Opens the BroadcastStudio
                                        Button(
                                            onClick = { viewModel.toggleLiveStream(true) },
                                            modifier = Modifier.fillMaxWidth().height(56.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                                        ) {
                                            Icon(Icons.Default.VideoCall, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Start Broadcasting Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // SECONDARY NAVIGATION: Link to the session repository
                        Spacer(Modifier.height(32.dp))
                        AdaptiveDashboardCard(
                            onClick = { viewModel.setSection(TutorSection.COURSE_ARCHIVED_BROADCASTS) },
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("View Session Archive", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("Access recorded lessons", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f))
                            }
                        }
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        } else {
            // VIEW: ACTIVE BROADCAST STUDIO
            BroadcastStudio(viewModel = viewModel, courseTitle = course?.title)
        }
    }
}

/**
 * Visual indicator for a single step in the setup wizard.
 * Features different states for Active, Completed, and Inactive.
 */
@Composable
fun LiveStepIndicator(number: Int, label: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (completed) MaterialTheme.colorScheme.primary else if (active) MaterialTheme.colorScheme.primaryContainer else Color.Gray.copy(alpha = 0.2f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (completed) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                else Text(text = number.toString(), fontWeight = FontWeight.Bold, color = if (active) MaterialTheme.colorScheme.primary else Color.Gray)
            }
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

/**
 * A specialized selection card for choosing course entities.
 * Highlights the selection with a primary border and checkmark icon.
 */
@Composable
fun LiveSelectionCard(title: String, subtitle: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (selected) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            else Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
