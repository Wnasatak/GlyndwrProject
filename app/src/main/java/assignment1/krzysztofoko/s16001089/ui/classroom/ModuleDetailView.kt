package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import java.text.SimpleDateFormat
import java.util.*

/**
 * ModuleDetailView.kt
 *
 * This file implements the detailed view for a specific module within the classroom.
 * It displays module information, requirements, and associated assignments.
 */

/**
 * Main Composable for displaying module details.
 * 
 * @param module The module content to display.
 * @param assignments List of all available assignments to filter from.
 * @param onBack Callback for navigation back.
 * @param onSubmitAssignment Callback for submitting an assignment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailView(
    module: ModuleContent,
    assignments: List<Assignment>,
    onBack: () -> Unit,
    onSubmitAssignment: (Assignment) -> Unit
) {
    // State to manage scrolling in the detail column
    val scrollState = rememberScrollState()
    
    // Filter assignments to only show those belonging to this specific module
    val moduleAssignments = assignments.filter { it.moduleId == module.id }

    Box(modifier = Modifier.fillMaxSize()) {
        // Decorative background component
        VerticalWavyBackground(isDarkTheme = true)

        Scaffold(
            containerColor = Color.Transparent, // Transparent to allow background visibility
            topBar = {
                // App bar with back button and centered title
                CenterAlignedTopAppBar(
                    title = { Text("MODULE DETAILS", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Module identifier (e.g., "Module 1")
                Text(
                    text = "Module ${module.order}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Module Title
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(Modifier.height(24.dp))

                // Module Description Section
                Text(
                    text = "About this Module",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = module.description,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Content Requirements Section
                Text(
                    text = "Requirements & Content",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                
                // Primary content indicator (Video, PDF, or Quiz)
                ContentRequirementItem(
                    icon = when(module.contentType) {
                        "VIDEO" -> Icons.Default.PlayCircle
                        "PDF" -> Icons.Default.Description
                        else -> Icons.Default.Quiz
                    },
                    title = "Primary Content: ${module.contentType}",
                    description = "Access the required ${module.contentType.lowercase()} materials for this module."
                )

                Spacer(Modifier.height(32.dp))

                // Assignments Section
                Text(
                    text = "Module Assignments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Display empty state or list of assignments
                if (moduleAssignments.isEmpty()) {
                    Text(
                        text = "No assignments for this module.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    moduleAssignments.forEach { assignment ->
                        ModuleAssignmentItem(
                            assignment = assignment,
                            onSumbitClick = { onSubmitAssignment(assignment) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable for displaying a content requirement row.
 */
@Composable
fun ContentRequirementItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icon container
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            // Title and description column
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

/**
 * Composable for displaying a single assignment card.
 */
@Composable
fun ModuleAssignmentItem(assignment: Assignment, onSumbitClick: () -> Unit) {
    // Date formatter for due dates
    val sdf = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Assignment info (Title and Due Date)
                Column(modifier = Modifier.weight(1f)) {
                    Text(assignment.title, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Due: ${sdf.format(Date(assignment.dueDate))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                
                // Status indicator or Submit button
                if (assignment.status == "SUBMITTED") {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "SUBMITTED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    Button(
                        onClick = onSumbitClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("SUBMIT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
