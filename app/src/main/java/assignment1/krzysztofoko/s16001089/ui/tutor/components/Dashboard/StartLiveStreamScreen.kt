package assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.util.*

/**
 * StartLiveStreamScreen provides a guided setup wizard for tutors to initiate a live broadcast.
 * It uses a sequential multi-step interface to ensure all necessary session parameters 
 * (Course and Module) are selected before the stream begins.
 *
 * Pattern: Stepper-based workflow (Course -> Module -> Final Confirmation).
 */
@Composable
fun StartLiveStreamScreen(viewModel: TutorViewModel) {
    // REACTIVE STATE: Synchronizes with the tutor's registry and current selection state
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val selectedCourseModules by viewModel.selectedCourseModules.collectAsState()
    val selectedModuleId by viewModel.selectedModuleId.collectAsState()
    val isLive by viewModel.isLive.collectAsState()

    // NAVIGATION LOGIC: Tracks the current progress through the configuration wizard
    var step by remember { mutableIntStateOf(1) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        // STEPPER UI: A horizontal progress indicator showing the user's position in the setup
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(number = 1, label = "Course", active = step >= 1, completed = step > 1)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 1) MaterialTheme.colorScheme.primary else Color.Gray)
            StepIndicator(number = 2, label = "Module", active = step >= 2, completed = step > 2)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 2) MaterialTheme.colorScheme.primary else Color.Gray)
            StepIndicator(number = 3, label = "Go Live", active = step >= 3, completed = step > 3)
        }

        // STEP DISPATCHER: Renders the appropriate selection interface based on the active step
        when (step) {
            // LAYER 1: Identify the target course for the stream
            1 -> {
                Text("Select Course for Live Stream", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(assignedCourses) { course ->
                        SelectionCard(
                            title = course.title,
                            subtitle = course.department,
                            icon = Icons.Default.School,
                            selected = selectedCourse?.id == course.id,
                            onClick = {
                                // PERSISTENCE: Updates the active course context in the ViewModel
                                viewModel.updateSelectedCourse(course.id)
                                step = 2
                            }
                        )
                    }
                }
            }
            
            // LAYER 2: Selection of the module topic to be covered in the session
            2 -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { step = 1 }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Select Module topic", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Course: ${selectedCourse?.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 48.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                // EMPTY STATE: Ensures a module exists before allowing a broadcast to start
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
                            SelectionCard(
                                title = module.title,
                                subtitle = "Topic: ${module.contentType}",
                                icon = Icons.Default.ViewModule,
                                selected = selectedModuleId == module.id,
                                onClick = {
                                    // PERSISTENCE: Sets the specific module focus for the stream
                                    viewModel.selectModule(module.id)
                                    step = 3
                                }
                            )
                        }
                    }
                }
            }
            
            // LAYER 3: Final confirmation card before initiating the stream
            3 -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { step = 2 }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Ready to Stream", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Podcasts, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text(text = "Live Broadcast", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text(text = "You are about to start a live session for:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Summary of the selected course
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                            Text(text = selectedCourse?.title ?: "", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Summary of the selected topic
                        val module = selectedCourseModules.find { it.id == selectedModuleId }
                        Text(text = "Topic: ${module?.title}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        
                        Spacer(Modifier.height(32.dp))
                        
                        // BROADCAST TRIGGER: Finalizes the session and transitions to the Dashboard
                        Button(
                            onClick = {
                                viewModel.toggleLiveStream(true)
                                viewModel.setSection(TutorSection.DASHBOARD)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)) // High-visibility action color
                        ) {
                            Icon(Icons.Default.VideoCall, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Broadcasting Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
