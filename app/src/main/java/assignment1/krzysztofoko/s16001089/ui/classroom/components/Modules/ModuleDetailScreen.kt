package assignment1.krzysztofoko.s16001089.ui.classroom.components.Modules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
 * A professional, dedicated screen for viewing module details and assignments.
 * Features a glassmorphic design and thematic background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    module: ModuleContent,
    assignments: List<Assignment>,
    onBack: () -> Unit,
    onSubmitAssignment: (Assignment) -> Unit
) {
    val scrollState = rememberScrollState()
    val moduleAssignments = assignments.filter { it.moduleId == module.id }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = true)

        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Custom Header (No statusBarsPadding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                @Suppress("DEPRECATION")
                Text(
                    "MODULE DETAILS", 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                ) 
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Reduced Top Spacer
                Spacer(Modifier.height(4.dp))

                // Main Content Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Module ${module.order}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(24.dp))

                        SectionHeader(title = "About this Module", icon = Icons.Default.Info)
                        Spacer(Modifier.height(12.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            text = module.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )

                        Spacer(Modifier.height(32.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(32.dp))

                        SectionHeader(title = "Learning Materials", icon = Icons.Default.LibraryBooks)
                        Spacer(Modifier.height(16.dp))
                        
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(32.dp))

                        SectionHeader(title = "Module Assignments", icon = Icons.Default.Assignment)
                        Spacer(Modifier.height(16.dp))
                        
                        if (moduleAssignments.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = "No assignments available for this module yet.",
                                    modifier = Modifier.padding(20.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            moduleAssignments.forEach { assignment ->
                                ModuleAssignmentItem(
                                    assignment = assignment,
                                    onSumbitClick = { onSubmitAssignment(assignment) }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, 
            null, 
            modifier = Modifier.size(18.dp), 
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ContentRequirementItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                @Suppress("DEPRECATION")
                Text(
                    description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ModuleAssignmentItem(assignment: Assignment, onSumbitClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val isSubmitted = assignment.status == "SUBMITTED"
    
    Surface(
        color = if (isSubmitted) Color(0xFF4CAF50).copy(alpha = 0.05f) 
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp, 
            color = if (isSubmitted) Color(0xFF4CAF50).copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    assignment.title, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule, 
                        null, 
                        modifier = Modifier.size(12.dp), 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        "Due: ${sdf.format(Date(assignment.dueDate))}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            if (isSubmitted) {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp), tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(6.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            "SUBMITTED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onSumbitClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text(
                        "SUBMIT", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
