package assignment1.krzysztofoko.s16001089.ui.classroom.components.Assignments

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * A dedicated screen for submitting assignment content and files.
 * Designed for a more "friendly" and theme-integrated experience.
 */
@Composable
fun AssignmentSubmissionScreen(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel, 
                    enabled = !isSubmitting,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                @Suppress("DEPRECATION")
                Text(
                    text = "SUBMIT ASSIGNMENT", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    letterSpacing = 1.2.sp
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))
                
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
                            text = assignment.title, 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), 
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday, 
                                    null, 
                                    modifier = Modifier.size(14.dp), 
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                @Suppress("DEPRECATION")
                                Text(
                                    text = "Due: ${sdf.format(Date(assignment.dueDate))}", 
                                    color = MaterialTheme.colorScheme.primary, 
                                    fontWeight = FontWeight.Bold, 
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Text(
                            text = "Description", 
                            fontWeight = FontWeight.Bold, 
                            style = MaterialTheme.typography.titleSmall, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = assignment.description, 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )
                        
                        Spacer(Modifier.height(32.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = "Your Submission", 
                            fontWeight = FontWeight.Bold, 
                            style = MaterialTheme.typography.titleMedium, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = submissionContent,
                            onValueChange = { submissionContent = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp),
                            placeholder = { 
                                Text(
                                    "Add comments or notes for your tutor...", 
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                ) 
                            },
                            shape = RoundedCornerShape(20.dp),
                            enabled = !isSubmitting,
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, 
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        )
                        
                        Spacer(Modifier.height(24.dp))

                        if (fileName == null) {
                            Surface(
                                onClick = { 
                                    if (!isSubmitting && !isFileAttaching) { 
                                        scope.launch { 
                                            isFileAttaching = true; 
                                            delay(1200); 
                                            fileName = "Assignment1_S16001089_KO.pdf"; 
                                            isFileAttaching = false 
                                        } 
                                    } 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                enabled = !isSubmitting
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp).fillMaxWidth(), 
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isFileAttaching) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp), 
                                            strokeWidth = 2.dp, 
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = if (isFileAttaching) "Attaching..." else "Attach Assignment File", 
                                        color = MaterialTheme.colorScheme.primary, 
                                        fontWeight = FontWeight.Bold, 
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(), 
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                ), 
                                shape = RoundedCornerShape(20.dp), 
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.InsertDriveFile, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileName ?: "file_attached", 
                                            maxLines = 1, 
                                            overflow = TextOverflow.Ellipsis, 
                                            style = MaterialTheme.typography.bodyLarge, 
                                            fontWeight = FontWeight.Bold, 
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        @Suppress("DEPRECATION")
                                        Text(
                                            text = "Ready to submit", 
                                            style = MaterialTheme.typography.labelSmall, 
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    IconButton(onClick = { fileName = null }, enabled = !isSubmitting) { 
                                        Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)) 
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
            
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), 
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { 
                            val finalContent = if (fileName != null) "$submissionContent [File: $fileName]" else submissionContent
                            onSubmit(finalContent) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        enabled = (submissionContent.isNotBlank() || fileName != null) && !isSubmitting && !isFileAttaching,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary, 
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isSubmitting) { 
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("UPLOADING SUBMISSION...", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) 
                        } else { 
                            Icon(Icons.Default.Send, null)
                            Spacer(Modifier.width(12.dp))
                            Text("SUBMIT ASSIGNMENT", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp) 
                        }
                    }
                }
            }
        }

        if (isSubmitting) { 
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(enabled = false) { }) 
        }
    }
}
