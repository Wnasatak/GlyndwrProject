package assignment1.krzysztofoko.s16001089.ui.admin.components.Courses

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog.CatalogDeleteDialog
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import java.util.UUID

/**
 * Optimized Course Modules Screen.
 * Fully adaptive for tablets using AdaptiveScreenContainer.
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderSection(
                        title = "Course Syllabus",
                        subtitle = "Organize modules and learning content.",
                        icon = Icons.Default.LibraryBooks,
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
        ModuleEditDialog(
            module = moduleToEdit!!,
            onDismiss = { moduleToEdit = null },
            onSave = {
                viewModel.saveModule(it)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            dueDate = System.currentTimeMillis() + 604800000 // +1 week
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            )
        }
    ) { padding ->
        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderSection(
                        title = "Tasks & Assignments",
                        subtitle = "Create and manage student work.",
                        icon = Icons.Default.Assignment,
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
        AssignmentEditDialog(
            assignment = taskToEdit!!,
            onDismiss = { taskToEdit = null },
            onSave = {
                viewModel.saveAssignment(it)
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
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
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
