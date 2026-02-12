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
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth

/**
 * ModuleEditDialog provides a professionally styled administrative interface for adding
 * or modifying course curriculum modules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleEditDialog(
    module: ModuleContent,
    onDismiss: () -> Unit,
    onSave: (ModuleContent) -> Unit
) {
    // Detect mode: Create vs Edit
    val isCreateMode = module.title.isBlank()

    // Local state management for form fields
    var title by remember { mutableStateOf(module.title) }
    var description by remember { mutableStateOf(module.description) }
    var contentType by remember { mutableStateOf(module.contentType) }
    var contentUrl by remember { mutableStateOf(module.contentUrl) }

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
                                    imageVector = if (isCreateMode) Icons.Default.LibraryAdd else Icons.Default.Layers, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            text = if (isCreateMode) "Add Syllabus Module" else "Edit Module Details", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // FORM FIELDS
                    OutlinedTextField(
                        value = title, 
                        onValueChange = { title = it }, 
                        label = { Text("Module Title") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(20.dp)) }
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description, 
                        onValueChange = { description = it }, 
                        label = { Text("Module Summary") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = contentType, 
                        onValueChange = { contentType = it }, 
                        label = { Text("Content Type (e.g. VIDEO, PDF)") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(20.dp)) }
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = contentUrl, 
                        onValueChange = { contentUrl = it }, 
                        label = { Text("Content Resource Path / URL") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Link, null, modifier = Modifier.size(20.dp)) }
                    )

                    Spacer(Modifier.height(32.dp))

                    // ACTION BAR
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.End, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            @Suppress("DEPRECATION")
                            Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val updatedModule = module.copy(
                                    title = title,
                                    description = description,
                                    contentType = contentType,
                                    contentUrl = contentUrl
                                )
                                onSave(updatedModule)
                            },
                            enabled = title.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) { 
                            @Suppress("DEPRECATION")
                            Text(if (isCreateMode) "Create Module" else "Save Changes", fontWeight = FontWeight.Bold) 
                        }
                    }
                }
            }
        }
    )
}
