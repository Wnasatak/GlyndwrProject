package assignment1.krzysztofoko.s16001089.ui.admin.components.Courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth

/**
 * AssignmentEditDialog provides a professionally styled administrative interface for adding
 * or modifying course tasks and assignments.
 * Now features a fully themed, high-fidelity dropdown for status selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentEditDialog(
    assignment: Assignment,
    onDismiss: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    // Detect mode: Create vs Edit
    val isCreateMode = assignment.title.isBlank()

    // Local state management for form fields
    var title by remember { mutableStateOf(assignment.title) }
    var description by remember { mutableStateOf(assignment.description) }
    var status by remember { mutableStateOf(assignment.status) }
    var allowedFileTypes by remember { mutableStateOf(assignment.allowedFileTypes) }

    // Dropdown state for Status
    var statusExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("PENDING", "SUBMITTED", "GRADED")

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard),
        content = {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // HEADER with institutional branding
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCreateMode) Icons.Default.PostAdd else Icons.Default.Assignment, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            text = if (isCreateMode) "Create New Task" else "Edit Task Details", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // FORM FIELDS
                    OutlinedTextField(
                        value = title, 
                        onValueChange = { title = it }, 
                        label = { Text("Task Title") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description, 
                        onValueChange = { description = it }, 
                        label = { Text("Requirements / Instructions") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // FULLY THEMED DROPDOWN
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Task Status") },
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Info, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(20.dp),
                                    tint = if (statusExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                focusedTrailingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Menu styling aligned with dialog surface
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            statusOptions.forEach { option ->
                                val isSelected = option == status
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = option, 
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        ) 
                                    },
                                    onClick = {
                                        status = option
                                        statusExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    modifier = Modifier.background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) 
                                        else Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = allowedFileTypes, 
                        onValueChange = { allowedFileTypes = it }, 
                        label = { Text("Allowed Extensions (PDF,DOCX,ZIP)") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    // ACTION BAR
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.End, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        @Suppress("DEPRECATION")
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = if (isCreateMode) "Cancel" else "Discard", 
                                color = MaterialTheme.colorScheme.primary, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onSave(assignment.copy(
                                    title = title,
                                    description = description,
                                    status = status,
                                    allowedFileTypes = allowedFileTypes
                                ))
                            },
                            enabled = title.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { 
                            @Suppress("DEPRECATION")
                            Text(if (isCreateMode) "Create Task" else "Save Changes", fontWeight = FontWeight.Bold) 
                        }
                    }
                }
            }
        }
    )
}
