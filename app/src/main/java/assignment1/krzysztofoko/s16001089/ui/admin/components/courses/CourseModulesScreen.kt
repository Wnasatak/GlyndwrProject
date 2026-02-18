package assignment1.krzysztofoko.s16001089.ui.admin.components.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
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
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.admin.components.catalog.CatalogDeleteDialog
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth
import java.util.UUID

/**
 * CourseModulesScreen.kt
 *
 * This screen provides a centralized hub for managing the educational structure of a course.
 * It allows administrators to organize learning materials into modules and assignments.
 */

/**
 * CourseModulesScreen Composable
 *
 * @param course Parent course.
 * @param viewModel Administrative logic holder.
 * @param isDarkTheme Visual flag.
 * @param onBack Navigation callback.
 * @param onModuleSelected Callback for module selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseModulesScreen(
    course: Course,
    viewModel: AdminViewModel,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onModuleSelected: (ModuleContent) -> Unit
) {
    val modules by viewModel.getModulesForCourse(course.id).collectAsState(initial = emptyList())
    var moduleToEdit by remember { mutableStateOf<ModuleContent?>(null) }
    var moduleToDelete by remember { mutableStateOf<ModuleContent?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                title = { 
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        moduleToEdit = ModuleContent(
                            id = UUID.randomUUID().toString(),
                            courseId = course.id,
                            title = "",
                            description = "",
                            contentType = "VIDEO",
                            contentUrl = "",
                            order = modules.size + 1
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Module")
                    }
                }
            )
        }
    ) { padding ->
        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { _ ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderSection(
                        title = "Course Syllabus",
                        subtitle = "Organize modules and learning content.",
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                        isDarkTheme = isDarkTheme
                    )
                }

                if (modules.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No modules found for this course.", color = Color.Gray)
                        }
                    }
                } else {
                    items(modules) { module ->
                        ModuleItemCard(
                            module = module,
                            onEdit = { moduleToEdit = module },
                            onDelete = { moduleToDelete = module },
                            onClick = { onModuleSelected(module) }
                        )
                    }
                }
                
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }

    if (moduleToEdit != null) {
        LocalModuleEditDialog(
            module = moduleToEdit!!,
            onDismiss = { moduleToEdit = null },
            onSave = { updatedModule ->
                viewModel.saveModule(updatedModule)
                moduleToEdit = null
            }
        )
    }

    if (moduleToDelete != null) {
        CatalogDeleteDialog(
            itemName = "Module",
            onDismiss = { moduleToDelete = null },
            onConfirm = {
                viewModel.deleteModule(moduleToDelete!!.id)
                moduleToDelete = null
            }
        )
    }
}

/**
 * ModuleItemCard Composable
 */
@Composable
fun ModuleItemCard(
    module: ModuleContent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = module.order.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = module.contentType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * ModuleTasksOverlay Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleTasksOverlay(
    module: ModuleContent,
    viewModel: AdminViewModel,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    val tasks by viewModel.getAssignmentsForModule(module.id).collectAsState(initial = emptyList())
    var taskToEdit by remember { mutableStateOf<Assignment?>(null) }
    var taskToDelete by remember { mutableStateOf<Assignment?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                title = {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        taskToEdit = Assignment(
                            id = UUID.randomUUID().toString(),
                            courseId = module.courseId,
                            moduleId = module.id,
                            title = "",
                            description = "",
                            dueDate = System.currentTimeMillis() + 604800000
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            )
        }
    ) { padding ->
        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { _ ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderSection(
                        title = "Tasks & Assignments",
                        subtitle = "Create and manage student work.",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        isDarkTheme = isDarkTheme
                    )
                }

                if (tasks.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No tasks found for this module.", color = Color.Gray)
                        }
                    }
                } else {
                    items(tasks) { task ->
                        TaskItemCard(
                            task = task,
                            onEdit = { taskToEdit = task },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
                
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }

    if (taskToEdit != null) {
        LocalAssignmentEditDialog(
            assignment = taskToEdit!!,
            onDismiss = { taskToEdit = null },
            onSave = { updatedAssignment ->
                viewModel.saveAssignment(updatedAssignment)
                taskToEdit = null
            }
        )
    }

    if (taskToDelete != null) {
        CatalogDeleteDialog(
            itemName = "Task",
            onDismiss = { taskToDelete = null },
            onConfirm = {
                viewModel.deleteAssignment(taskToDelete!!.id)
                taskToDelete = null
            }
        )
    }
}

/**
 * TaskItemCard Composable
 */
@Composable
fun TaskItemCard(
    task: Assignment,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Assignment Task",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * HeaderSection Composable
 */
@Composable
private fun HeaderSection(title: String, subtitle: String, icon: ImageVector, isDarkTheme: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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
                fontWeight = FontWeight.Black,
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * LocalModuleEditDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalModuleEditDialog(
    module: ModuleContent,
    onDismiss: () -> Unit,
    onSave: (ModuleContent) -> Unit
) {
    val isCreateMode = module.title.isBlank()
    var title by remember { mutableStateOf(module.title) }
    var description by remember { mutableStateOf(module.description) }
    var contentType by remember { mutableStateOf(module.contentType) }
    var contentUrl by remember { mutableStateOf(module.contentUrl) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(text = if (isCreateMode) "Add Syllabus Module" else "Edit Module Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Module Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Module Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = contentType, onValueChange = { contentType = it }, label = { Text("Content Type") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = contentUrl, onValueChange = { contentUrl = it }, label = { Text("Content URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = { onSave(module.copy(title = title, description = description, contentType = contentType, contentUrl = contentUrl)) }, enabled = title.isNotBlank(), shape = RoundedCornerShape(12.dp)) { Text("Save") }
                }
            }
        }
    }
}

/**
 * LocalAssignmentEditDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalAssignmentEditDialog(
    assignment: Assignment,
    onDismiss: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    val isCreateMode = assignment.title.isBlank()
    var title by remember { mutableStateOf(assignment.title) }
    var description by remember { mutableStateOf(assignment.description) }
    var status by remember { mutableStateOf(assignment.status) }
    var allowedFileTypes by remember { mutableStateOf(assignment.allowedFileTypes) }
    var statusExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("PENDING", "SUBMITTED", "GRADED")

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isCreateMode) "Create New Task" else "Edit Task Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }) {
                    OutlinedTextField(value = status, onValueChange = {}, readOnly = true, label = { Text("Status") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statusOptions.forEach { option -> DropdownMenuItem(text = { Text(text = option) }, onClick = { status = option; statusExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = allowedFileTypes, onValueChange = { allowedFileTypes = it }, label = { Text("Allowed Extensions") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = { onSave(assignment.copy(title = title, description = description, status = status, allowedFileTypes = allowedFileTypes)) }, enabled = title.isNotBlank(), shape = RoundedCornerShape(12.dp)) { Text("Save") }
                }
            }
        }
    }
}

/*
 * --- COMMENTED OUT FUNCTIONS FOR FUTURE ENHANCEMENTS ---
 *
 * Below are placeholder functions for features planned for future iterations.
 */

/*
/**
 * bulkDeleteModules
 * 
 * Allows the administrator to delete multiple modules in a single batch operation.
 */
private fun bulkDeleteModules(moduleIds: List<String>, viewModel: AdminViewModel) {
    // moduleIds.forEach { id -> viewModel.deleteModule(id) }
}
*/

/*
/**
 * exportSyllabusToPdf
 * 
 * Generates a formatted PDF document of the course curriculum for student distribution.
 */
private fun exportSyllabusToPdf(course: Course, modules: List<ModuleContent>) {
    // Logic for PDF generation here using a library like iText or OpenPDF.
}
*/

/*
/**
 * duplicateModule
 * 
 * Creates a carbon copy of an existing module to allow for rapid creation of similar modules.
 */
private fun duplicateModule(module: ModuleContent, viewModel: AdminViewModel) {
    // val newModule = module.copy(id = UUID.randomUUID().toString(), title = "Copy of ${module.title}")
    // viewModel.saveModule(newModule)
}
*/
