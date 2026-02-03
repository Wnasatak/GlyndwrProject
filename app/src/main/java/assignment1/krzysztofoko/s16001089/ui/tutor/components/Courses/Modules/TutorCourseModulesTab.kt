package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Modules

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.util.*

@Composable
fun TutorCourseModulesTab(
    viewModel: TutorViewModel
) {
    val course by viewModel.selectedCourse.collectAsState()
    val modules by viewModel.selectedCourseModules.collectAsState()

    var editingModule by remember { mutableStateOf<ModuleContent?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var moduleToDelete by remember { mutableStateOf<ModuleContent?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Module", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(24.dp))
            
            HeaderSection(
                title = "Course Syllabus",
                subtitle = course?.title ?: "Manage Content",
                icon = Icons.Default.LibraryBooks
            )
            
            Spacer(Modifier.height(24.dp))

            if (modules.isEmpty()) {
                EmptyModulesState { showCreateDialog = true }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(modules) { module ->
                        ModuleItemCard(
                            module = module,
                            onEdit = { editingModule = module },
                            onDelete = { moduleToDelete = module }
                        )
                    }
                }
            }
        }
    }

    // Create / Edit Dialog
    if (showCreateDialog || editingModule != null) {
        ModuleEditDialog(
            module = editingModule,
            courseId = course?.id ?: "",
            nextOrder = (modules.maxOfOrNull { it.order } ?: 0) + 1,
            onDismiss = { 
                showCreateDialog = false
                editingModule = null
            },
            onSave = { updated ->
                viewModel.upsertModule(updated)
                showCreateDialog = false
                editingModule = null
            }
        )
    }

    // Delete Confirmation
    if (moduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { moduleToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Module") },
            text = { Text("Are you sure you want to delete '${moduleToDelete?.title}'? This will disconnect all materials linked to this module.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteModule(moduleToDelete!!.id)
                        moduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { moduleToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EmptyModulesState(onAction: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PostAdd, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))
            Text("Your syllabus is empty", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAction, shape = RoundedCornerShape(12.dp)) {
                Text("Create First Module")
            }
        }
    }
}

@Composable
fun ModuleItemCard(
    module: ModuleContent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(module.contentType) {
                                "VIDEO" -> Icons.Default.PlayCircle
                                "PDF" -> Icons.Default.Description
                                "QUIZ" -> Icons.Default.Quiz
                                else -> Icons.Default.Article
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Module Order: ${module.order}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val typeLabel = when(module.contentType) {
                    "VIDEO" -> "Video Lecture"
                    "PDF" -> "Reading Material"
                    "QUIZ" -> "Knowledge Check"
                    else -> "General Content"
                }
                SuggestionChip(
                    onClick = {},
                    label = { Text(typeLabel) },
                    colors = SuggestionChipDefaults.suggestionChipColors(labelColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = module.contentUrl.take(25) + "...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleEditDialog(
    module: ModuleContent?,
    courseId: String,
    nextOrder: Int,
    onDismiss: () -> Unit,
    onSave: (ModuleContent) -> Unit
) {
    var title by remember { mutableStateOf(module?.title ?: "") }
    var description by remember { mutableStateOf(module?.description ?: "") }
    var contentType by remember { mutableStateOf(module?.contentType ?: "VIDEO") }
    var contentUrl by remember { mutableStateOf(module?.contentUrl ?: "") }
    var order by remember { mutableStateOf(module?.order?.toString() ?: nextOrder.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).fillMaxWidth(),
        content = {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (module == null) "Add Curriculum Module" else "Edit Module",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Module Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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

                    Spacer(Modifier.height(24.dp))
                    
                    Text("Content Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TypeSelectionIcon(icon = Icons.Default.PlayCircle, label = "VIDEO", isSelected = contentType == "VIDEO") { contentType = "VIDEO" }
                        TypeSelectionIcon(icon = Icons.Default.Description, label = "PDF", isSelected = contentType == "PDF") { contentType = "PDF" }
                        TypeSelectionIcon(icon = Icons.Default.Quiz, label = "QUIZ", isSelected = contentType == "QUIZ") { contentType = "QUIZ" }
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = contentUrl,
                        onValueChange = { contentUrl = it },
                        label = { Text("Resource URL (Video/PDF link)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Link, null) }
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = order,
                        onValueChange = { if (it.all { c -> c.isDigit() }) order = it },
                        label = { Text("Display Order") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(32.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onSave(ModuleContent(
                                    id = module?.id ?: UUID.randomUUID().toString(),
                                    courseId = courseId,
                                    title = title,
                                    description = description,
                                    contentType = contentType,
                                    contentUrl = contentUrl,
                                    order = order.toIntOrNull() ?: nextOrder
                                ))
                            },
                            enabled = title.isNotBlank() && contentUrl.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Module", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun TypeSelectionIcon(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (isSelected) Color.White else Color.Gray)
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp), color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}
