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
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(viewModel: TutorViewModel) {
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val selectedCourseModules by viewModel.selectedCourseModules.collectAsState()
    val selectedModuleId by viewModel.selectedModuleId.collectAsState()

    var step by remember { mutableIntStateOf(1) }
    var showCreateModuleDialog by remember { mutableStateOf(false) }
    
    // Assignment Form State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueDateMillis by remember { mutableLongStateOf(0L) }
    var totalPoints by remember { mutableStateOf("100") }
    
    // File Type State
    var allowPdf by remember { mutableStateOf(true) }
    var allowDocx by remember { mutableStateOf(true) }
    var allowZip by remember { mutableStateOf(true) }

    // Date & Time Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

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
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                datePickerState.selectedDateMillis?.let { cal.timeInMillis = it }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                
                dueDateMillis = cal.timeInMillis
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                dueDateText = sdf.format(cal.time)
                
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showCreateModuleDialog) {
        CreateModuleDialog(
            onDismiss = { showCreateModuleDialog = false },
            onCreate = { moduleTitle ->
                val courseId = selectedCourse?.id
                if (courseId != null && moduleTitle.isNotBlank()) {
                    val newModule = ModuleContent(
                        id = UUID.randomUUID().toString(),
                        courseId = courseId,
                        title = moduleTitle,
                        description = "Module created during assignment creation.",
                        contentType = "GENERAL",
                        contentUrl = "",
                        order = selectedCourseModules.size
                    )
                    viewModel.upsertModule(newModule)
                    viewModel.selectModule(newModule.id)
                    step = 3
                }
                showCreateModuleDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Progress Stepper
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(number = 1, label = "Course", active = step >= 1, completed = step > 1)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 1) MaterialTheme.colorScheme.primary else Color.Gray)
            StepIndicator(number = 2, label = "Module", active = step >= 2, completed = step > 2)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 2) MaterialTheme.colorScheme.primary else Color.Gray)
            StepIndicator(number = 3, label = "Details", active = step >= 3, completed = step > 3)
        }

        when (step) {
            1 -> {
                Text("Select Course", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(assignedCourses) { course ->
                        SelectionCard(
                            title = course.title,
                            subtitle = course.department,
                            icon = Icons.Default.School,
                            selected = selectedCourse?.id == course.id,
                            onClick = {
                                viewModel.updateSelectedCourse(course.id)
                                step = 2
                            }
                        )
                    }
                }
            }
            2 -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { step = 1 }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Select Module", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Course: ${selectedCourse?.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 48.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedCourseModules.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                                    Spacer(Modifier.height(16.dp))
                                    Text("No modules found for this course.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(selectedCourseModules) { module ->
                            SelectionCard(
                                title = module.title,
                                subtitle = "Module Type: ${module.contentType}",
                                icon = Icons.Default.ViewModule,
                                selected = selectedModuleId == module.id,
                                onClick = {
                                    viewModel.selectModule(module.id)
                                    step = 3
                                }
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { showCreateModuleDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create New Module", fontWeight = FontWeight.Bold)
                }
            }
            3 -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { step = 2 }) { Icon(Icons.Default.ArrowBack, null) }
                    Text("Assignment Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Assignment Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description / Instructions") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = dueDateText,
                            onValueChange = { /* Read-only via picker */ },
                            readOnly = true,
                            label = { Text("Due Date & Time") },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            enabled = false, // Use enabled=false with colors if you want it to look clickable but not typed into
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { 
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarToday, null)
                                }
                            }
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = totalPoints,
                            onValueChange = { totalPoints = it },
                            label = { Text("Total Points") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    item {
                        Text(
                            text = "Allowed Submission Formats",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = allowPdf,
                                onClick = { allowPdf = !allowPdf },
                                label = { Text("PDF") },
                                leadingIcon = if (allowPdf) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = allowDocx,
                                onClick = { allowDocx = !allowDocx },
                                label = { Text("DOCX") },
                                leadingIcon = if (allowDocx) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = allowZip,
                                onClick = { allowZip = !allowZip },
                                label = { Text("ZIP") },
                                leadingIcon = if (allowZip) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (title.isNotBlank() && selectedCourse != null && selectedModuleId != null) {
                            val formats = mutableListOf<String>()
                            if (allowPdf) formats.add("PDF")
                            if (allowDocx) formats.add("DOCX")
                            if (allowZip) formats.add("ZIP")
                            
                            val assignment = Assignment(
                                id = UUID.randomUUID().toString(),
                                courseId = selectedCourse!!.id,
                                moduleId = selectedModuleId!!,
                                title = title,
                                description = description,
                                dueDate = dueDateMillis,
                                status = "PENDING",
                                allowedFileTypes = formats.joinToString(",")
                            )
                            viewModel.upsertAssignment(assignment)
                            viewModel.setSection(TutorSection.DASHBOARD)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create Assignment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        },
        text = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    )
}

@Composable
fun CreateModuleDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var moduleTitle by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Module", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter a title for the new module.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = moduleTitle,
                    onValueChange = { moduleTitle = it },
                    label = { Text("Module Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(moduleTitle) },
                enabled = moduleTitle.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun StepIndicator(number: Int, label: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = when {
                completed -> MaterialTheme.colorScheme.primary
                active -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Gray.copy(alpha = 0.2f)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (completed) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = number.toString(),
                        fontWeight = FontWeight.Bold,
                        color = if (active) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        null, 
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                @Suppress("DEPRECATION")
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (selected) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}
