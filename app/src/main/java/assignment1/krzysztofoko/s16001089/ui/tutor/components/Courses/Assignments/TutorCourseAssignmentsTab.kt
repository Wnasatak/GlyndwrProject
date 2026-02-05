package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Assignments

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
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TutorCourseAssignmentsTab(
    viewModel: TutorViewModel
) {
    val course by viewModel.selectedCourse.collectAsState()
    val assignments by viewModel.selectedCourseAssignments.collectAsState()
    val modules by viewModel.selectedCourseModules.collectAsState()

    var step by remember { mutableIntStateOf(1) } // 1: Select Module, 2: Display Assignments
    var selectedModuleId by remember { mutableStateOf<String?>(null) }
    
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var assignmentToDelete by remember { mutableStateOf<Assignment?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(24.dp))
        
        HeaderSection(
            title = if (step == 1) "Select Module" else "Module Assignments",
            subtitle = course?.title ?: "Manage Assignments",
            icon = if (step == 1) Icons.Default.ViewModule else Icons.Default.Assignment
        )
        
        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AssignmentStepTransition"
        ) { currentStep ->
            when (currentStep) {
                1 -> {
                    if (modules.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No modules found. Please create a module first.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(modules) { module ->
                                ModuleSelectionCard(
                                    title = module.title,
                                    assignmentCount = assignments.count { it.moduleId == module.id },
                                    onClick = {
                                        selectedModuleId = module.id
                                        step = 2
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    val filteredAssignments = assignments.filter { it.moduleId == selectedModuleId }
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { step = 1 }) { Icon(Icons.Default.ArrowBack, null) }
                            Text(
                                text = modules.find { it.id == selectedModuleId }?.title ?: "Assignments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))

                        if (filteredAssignments.isEmpty()) {
                            EmptyAssignmentsState { showCreateDialog = true }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(filteredAssignments) { assignment ->
                                    val module = modules.find { it.id == assignment.moduleId }
                                    AssignmentCard(
                                        assignment = assignment, 
                                        module = module,
                                        onEdit = { editingAssignment = assignment },
                                        onDelete = { assignmentToDelete = assignment }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Floating Action Button logic for Step 2
    if (step == 2) {
        Box(modifier = Modifier.fillMaxSize()) {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Assignment", fontWeight = FontWeight.Bold) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }

    // Create / Edit Dialog
    if (showCreateDialog || editingAssignment != null) {
        AssignmentEditDialog(
            assignment = editingAssignment,
            courseId = course?.id ?: "",
            modules = modules,
            onDismiss = { 
                showCreateDialog = false
                editingAssignment = null
            },
            onSave = { updated ->
                viewModel.upsertAssignment(updated)
                showCreateDialog = false
                editingAssignment = null
            }
        )
    }

    // Delete Confirmation
    if (assignmentToDelete != null) {
        DeleteConfirmationDialog(
            title = assignmentToDelete?.title ?: "",
            onDismiss = { assignmentToDelete = null },
            onConfirm = {
                viewModel.deleteAssignment(assignmentToDelete!!.id)
                assignmentToDelete = null
            }
        )
    }
}

@Composable
fun ModuleSelectionCard(title: String, assignmentCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ViewModule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = "$assignmentCount Assignments", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyAssignmentsState(onAction: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.AssignmentLate,
                null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No assignments yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                "Start by adding a new coursework task for your students to complete.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create First Assignment")
            }
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: Assignment, 
    module: ModuleContent?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM dd, HH:mm", Locale.getDefault())
    val dueDate = dateFormat.format(Date(assignment.dueDate))
    val isOverdue = assignment.dueDate < System.currentTimeMillis()
    
    val timeLeft = remember(assignment.dueDate) {
        val diff = assignment.dueDate - System.currentTimeMillis()
        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff / (1000 * 60 * 60)) % 24
        when {
            diff < 0 -> "Expired"
            days == 0L && hours > 0 -> "$hours hours left"
            days == 0L -> "Due soon"
            days == 1L -> "Due Tomorrow"
            else -> "$days days left"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (isOverdue) BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(0.3f)) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Changed to Top to prevent vertical squeeze
            ) {
                Surface(
                    modifier = Modifier.weight(1f, fill = false),
                    color = (module?.title?.let { Color(0xFF673AB7) } ?: MaterialTheme.colorScheme.secondary).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Layers, 
                            null, 
                            modifier = Modifier.size(12.dp), 
                            tint = (module?.title?.let { Color(0xFF673AB7) } ?: MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = module?.title?.uppercase() ?: "GENERAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = (module?.title?.let { Color(0xFF673AB7) } ?: MaterialTheme.colorScheme.secondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))

                Text(
                    text = timeLeft,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black, // Increased weight for visibility
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp)) // Increased spacing before title
            
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(8.dp)) // Increased spacing after title
            
            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule, 
                        null, 
                        tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = dueDate,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentEditDialog(
    assignment: Assignment?,
    courseId: String,
    modules: List<ModuleContent>,
    onDismiss: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    var title by remember { mutableStateOf(assignment?.title ?: "") }
    var description by remember { mutableStateOf(assignment?.description ?: "") }
    var selectedModuleId by remember { mutableStateOf(assignment?.moduleId ?: modules.firstOrNull()?.id ?: "") }
    var dueDateMillis by remember { mutableStateOf(assignment?.dueDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) }
    
    // File formats state
    val initialFormats = assignment?.allowedFileTypes?.split(",")?.toSet() ?: setOf("PDF", "DOCX", "ZIP")
    var allowPdf by remember { mutableStateOf("PDF" in initialFormats) }
    var allowDocx by remember { mutableStateOf("DOCX" in initialFormats) }
    var allowZip by remember { mutableStateOf("ZIP" in initialFormats) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDateMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = dueDateMillis }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = dueDateMillis }.get(Calendar.MINUTE)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                datePickerState.selectedDateMillis?.let { cal.timeInMillis = it }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                dueDateMillis = cal.timeInMillis
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

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
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (assignment == null) "New Assignment" else "Edit Assignment",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description / Requirements") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Improved Module Selection
                        Text(
                            "Associate Module", 
                            style = MaterialTheme.typography.labelLarge, 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            modules.forEach { mod ->
                                val isSelected = selectedModuleId == mod.id
                                Surface(
                                    onClick = { selectedModuleId = mod.id },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Layers, 
                                            null, 
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = mod.title, 
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        // File Formats
                        Text(
                            "Allowed Formats", 
                            style = MaterialTheme.typography.labelLarge, 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = allowPdf,
                                onClick = { allowPdf = !allowPdf },
                                label = { Text("PDF") },
                                leadingIcon = if (allowPdf) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                            )
                            FilterChip(
                                selected = allowDocx,
                                onClick = { allowDocx = !allowDocx },
                                label = { Text("DOCX") },
                                leadingIcon = if (allowDocx) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                            )
                            FilterChip(
                                selected = allowZip,
                                onClick = { allowZip = !allowZip },
                                label = { Text("ZIP") },
                                leadingIcon = if (allowZip) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                            )
                        }

                        // Date Selection
                        Surface(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Deadline", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(dueDateMillis)),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val formats = mutableListOf<String>()
                                if (allowPdf) formats.add("PDF")
                                if (allowDocx) formats.add("DOCX")
                                if (allowZip) formats.add("ZIP")
                                
                                onSave(Assignment(
                                    id = assignment?.id ?: UUID.randomUUID().toString(),
                                    courseId = courseId,
                                    moduleId = selectedModuleId,
                                    title = title,
                                    description = description,
                                    dueDate = dueDateMillis,
                                    status = assignment?.status ?: "PENDING",
                                    allowedFileTypes = formats.joinToString(",")
                                ))
                            },
                            enabled = title.isNotBlank() && selectedModuleId.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Save Assignment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Remove Assignment") },
        text = { 
            Text("Are you sure you want to permanently delete '$title'? All student submissions and grades for this task will be lost.") 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Delete Assignment") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep Assignment") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
