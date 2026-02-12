package assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * CreateAssignmentScreen provides a professional, guided authoring environment for course instructors
 * to deploy new academic assessments. It utilizes a three-tier setup wizard to ensure 
 * logical association between courses, curriculum modules, and specific task requirements.
 *
 * Key Architectural Patterns:
 * 1. Sequential Wizard (Stepper): Guarantees data integrity by enforcing a selection order (Course -> Module -> Details).
 * 2. Multi-Part Date/Time Selection: Cascading pickers for precise deadline configuration.
 * 3. Conditional Sub-Dialogs: Allows tutors to create missing curriculum modules on-the-fly.
 * 4. Adaptive Form Input: Real-time validation and reactive state management for complex assignment metadata.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(viewModel: TutorViewModel) {
    // REACTIVE STATE: Monitors the current course load and curriculum registry via the TutorViewModel
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val selectedCourseModules by viewModel.selectedCourseModules.collectAsState()
    val selectedModuleId by viewModel.selectedModuleId.collectAsState()

    // NAVIGATION STATE: Tracks the tutor's progress through the authoring workflow
    var step by remember { mutableIntStateOf(1) }
    var showCreateModuleDialog by remember { mutableStateOf(false) }

    // PRIMARY ASSIGNMENT DATA: Local state for form fields before database persistence
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueDateMillis by remember { mutableLongStateOf(0L) }
    var totalPoints by remember { mutableStateOf("100") }

    // SUBMISSION POLICY: Defines which digital file formats the assignment will accept
    var allowPdf by remember { mutableStateOf(true) }
    var allowDocx by remember { mutableStateOf(true) }
    var allowZip by remember { mutableStateOf(true) }

    // PICKER CONTROL: Manages the visibility and lifecycle of institutional date/time selectors
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    // --- DIALOG: INSTITUTIONAL DATE PICKER ---
    if (showDatePicker) {
        val datePickerColors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            headlineContentColor = MaterialTheme.colorScheme.primary,
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            todayContentColor = MaterialTheme.colorScheme.primary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary,
            dayContentColor = MaterialTheme.colorScheme.onSurface,
            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationContentColor = MaterialTheme.colorScheme.primary
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true // TRANSITION: Cascade to time selection
                }) { Text("Next", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
            },
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 0.dp,
            colors = datePickerColors
        ) {
            DatePicker(
                state = datePickerState,
                colors = datePickerColors
            )
        }
    }

    // --- DIALOG: INSTITUTIONAL TIME PICKER ---
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                datePickerState.selectedDateMillis?.let { cal.timeInMillis = it }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)

                // FINALIZE TIMESTAMP: Combines date and time into a single millisecond value
                dueDateMillis = cal.timeInMillis
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                dueDateText = sdf.format(cal.time)

                showTimePicker = false
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    selectorColor = MaterialTheme.colorScheme.primary,
                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }

    // --- DIALOG: IN-LINE MODULE CREATION ---
    // Use Case: Tutor realizes a new module is needed for this specific assignment
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
                    // PERSISTENCE: Immediately adds module to database and selects it
                    viewModel.upsertModule(newModule)
                    viewModel.selectModule(newModule.id)
                    step = 3 // SKIP TO FINAL STEP
                }
                showCreateModuleDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- SECTION: PROGRESS STEPPER ---
        // Provides visual context of where the tutor is in the authoring lifecycle
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(number = 1, label = "Course", active = step >= 1, completed = step > 1)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 1) MaterialTheme.colorScheme.primary else Color.Gray)
            StepIndicator(number = 2, label = "Module", active = step >= 2, completed = step > 2)
            HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 2) MaterialTheme.colorScheme.primary else Color.Gray)
            StepIndicator(number = 3, label = "Details", active = step >= 3, completed = step > 3)
        }

        // --- SECTION: ADAPTIVE WORKSPACE ---
        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                when (step) {
                    // LAYER 1: Select the target class for the assessment
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
                    
                    // LAYER 2: Associate with a curriculum module
                    2 -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { step = 1 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
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

                        // IN-LINE FLOW: Allows module creation without leaving the assignment wizard
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
                    
                    // LAYER 3: Detailed assessment criteria form
                    3 -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { step = 2 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                            Text("Assignment Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(16.dp))

                        // FORM CONTAINER: High-readability surface with scrolling support
                        Surface(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Assignment Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description / Instructions") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // READ-ONLY DATE FIELD: Triggers the cascading picker flow
                                OutlinedTextField(
                                    value = dueDateText,
                                    onValueChange = { /* Interaction handled via picker triggers */ },
                                    readOnly = true,
                                    label = { Text("Due Date & Time") },
                                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                                    enabled = false,
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

                                OutlinedTextField(
                                    value = totalPoints,
                                    onValueChange = { totalPoints = it },
                                    label = { Text("Total Points") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // POLICY CONFIGURATION: Visual chips for multi-format submission support
                                Text(text = "Allowed Submission Formats", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val chipColors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val chipBorder = FilterChipDefaults.filterChipBorder(enabled = true, selected = true, borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), selectedBorderColor = MaterialTheme.colorScheme.primary)

                                    FilterChip(selected = allowPdf, onClick = { allowPdf = !allowPdf }, label = { Text("PDF") }, colors = chipColors, border = if (allowPdf) null else chipBorder, leadingIcon = if (allowPdf) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null)
                                    FilterChip(selected = allowDocx, onClick = { allowDocx = !allowDocx }, label = { Text("DOCX") }, colors = chipColors, border = if (allowDocx) null else chipBorder, leadingIcon = if (allowDocx) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null)
                                    FilterChip(selected = allowZip, onClick = { allowZip = !allowZip }, label = { Text("ZIP") }, colors = chipColors, border = if (allowZip) null else chipBorder, leadingIcon = if (allowZip) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null)
                                }
                            }
                        }

                        // FINAL COMMIT: Validates input and deploys the assignment to the system
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
                                    // PERSISTENCE: Commits assessment to the database and returns to dashboard
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
    }
}

/**
 * A standard alert dialog wrapper for time selection components.
 */
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel", color = MaterialTheme.colorScheme.primary) } },
        text = { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) { content() } },
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp
    )
}

/**
 * A specialized dialog for quick curriculum expansion during assignment creation.
 */
@Composable
fun CreateModuleDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var moduleTitle by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Create New Module", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter a title for the new module.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = moduleTitle, onValueChange = { moduleTitle = it }, label = { Text("Module Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(moduleTitle) }, enabled = moduleTitle.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Create", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary) } },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp
    )
}

/**
 * Visual step indicator for the creation wizard.
 */
@Composable
fun StepIndicator(number: Int, label: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = CircleShape, color = when { completed -> MaterialTheme.colorScheme.primary; active -> MaterialTheme.colorScheme.primaryContainer; else -> Color.Gray.copy(alpha = 0.2f) }, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                if (completed) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                else Text(text = number.toString(), fontWeight = FontWeight.Bold, color = if (active) MaterialTheme.colorScheme.primary else Color.Gray)
            }
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

/**
 * Adaptive selection card for course and module choosing.
 */
@Composable
fun SelectionCard(title: String, subtitle: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (selected) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            else Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
