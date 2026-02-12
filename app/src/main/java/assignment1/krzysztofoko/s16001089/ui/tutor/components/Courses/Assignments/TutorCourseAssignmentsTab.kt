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
import assignment1.krzysztofoko.s16001089.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * TutorCourseAssignmentsTab provides a modular administrative interface for creating 
 * and managing academic assessments. It uses a "Step-Based" navigation pattern:
 * 1. Module Selection: Focus on assignments within a specific curriculum topic.
 * 2. Assignment Management: Detailed list of tasks with full CRUD capabilities.
 *
 * Key Features:
 * - Deadline tracking with real-time "time left" calculations.
 * - Multi-format support (PDF, DOCX, ZIP) classification for submissions.
 * - Dynamic linking between assignments and syllabus modules.
 * - Integrated Material 3 Date and Time pickers for deadline configuration.
 */
@Composable
fun TutorCourseAssignmentsTab(
    viewModel: TutorViewModel
) {
    // REACTIVE DATA: Core state streams from the TutorViewModel
    val course by viewModel.selectedCourse.collectAsState()
    val assignments by viewModel.selectedCourseAssignments.collectAsState()
    val modules by viewModel.selectedCourseModules.collectAsState()

    // NAVIGATION STATE: Tracks the current layer of the assignment management hierarchy
    var step by remember { mutableIntStateOf(1) } 
    var selectedModuleId by remember { mutableStateOf<String?>(null) }
    
    // UI OVERLAY STATE: Manages editing context and destructive action safety
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var assignmentToDelete by remember { mutableStateOf<Assignment?>(null) }

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
            Spacer(Modifier.height(12.dp))
            
            // HEADER: Context-aware title and institutional iconography
            AdaptiveDashboardHeader(
                title = if (step == 1) "Select Module" else "Module Assignments",
                subtitle = course?.title ?: "Manage Assignments",
                icon = if (step == 1) Icons.Default.ViewModule else Icons.Default.Assignment
            )
            
            Spacer(Modifier.height(24.dp))

            // STEP DISPATCHER: Transitions between Module Selection and Assignment List
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "AssignmentStepTransition"
            ) { currentStep ->
                when (currentStep) {
                    // LAYER 1: Choose a curriculum module to view its associated tasks
                    1 -> {
                        if (modules.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No modules found. Please create a module first.", 
                                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(), 
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
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
                    
                    // LAYER 2: Management list for assignments within the chosen module
                    2 -> {
                        val filteredAssignments = assignments.filter { it.moduleId == selectedModuleId }
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier.size(if (isTablet) 48.dp else 40.dp)
                                ) { Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)) }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = modules.find { it.id == selectedModuleId }?.title ?: "Assignments",
                                    style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
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
                                    modifier = Modifier.fillMaxSize(),
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

        // PRIMARY ACTION: Create new assignment (Visible only in Layer 2)
        if (step == 2) {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Assignment", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AdaptiveSpacing.contentPadding())
                    .padding(bottom = 16.dp)
            )
        }
    }

    // DIALOG: Institutional interface for defining assessment criteria and deadlines
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
                // PERSISTENCE: Commits changes to the Room database via ViewModel
                viewModel.upsertAssignment(updated)
                showCreateDialog = false
                editingAssignment = null
            }
        )
    }

    // SAFETY DIALOG: Confirms removal of institutional assessment data
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

/**
 * A specialized selection item for curriculum modules.
 * Features an assignment counter for quick oversight.
 */
@Composable
fun ModuleSelectionCard(title: String, assignmentCount: Int, onClick: () -> Unit) {
    AdaptiveDashboardCard(onClick = onClick) { isTablet ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(if (isTablet) 56.dp else 40.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(if (isTablet) 14.dp else 10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ViewModule, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(if (isTablet) 28.dp else 20.dp)
                    )
                }
            }
            Spacer(Modifier.width(if (isTablet) 24.dp else 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold, 
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$assignmentCount Assignments", 
                    style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.ChevronRight, 
                null, 
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
            )
        }
    }
}

/** Placeholder displayed when a module contains no assessments. */
@Composable
fun EmptyAssignmentsState(onAction: () -> Unit) {
    val isTablet = isTablet()
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(if (isTablet) 48.dp else 32.dp)
        ) {
            Icon(
                Icons.Default.AssignmentLate,
                null,
                modifier = Modifier.size(if (isTablet) 120.dp else 80.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No assignments yet",
                style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Start by adding a new coursework task for your students to complete.",
                textAlign = TextAlign.Center,
                style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                modifier = if (isTablet) Modifier.height(52.dp) else Modifier
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create First Assignment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * A detailed card representing a single academic assessment.
 * Features state-aware deadline tracking and management actions.
 */
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
    
    // TEMPORAL LOGIC: Calculates a user-friendly "Time Left" string for the assessment
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

    AdaptiveDashboardCard(
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) { isTablet ->
        Column {
            // CARD HEADER: Branded module link and dynamic deadline badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top 
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
                            modifier = Modifier.size(if (isTablet) 14.dp else 12.dp), 
                            tint = (module?.title?.let { Color(0xFF673AB7) } ?: MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = module?.title?.uppercase() ?: "GENERAL",
                            style = if (isTablet) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
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
                    style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black, 
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp)) 
            
            // CONTENT: Primary title and descriptive assessment criteria
            Text(
                text = assignment.title,
                style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(8.dp)) 
            
            Text(
                text = assignment.description,
                style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (isTablet) 22.sp else 20.sp
            )

            Spacer(Modifier.height(20.dp))
            
            // FOOTER: Specific deadline date and management triggers
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
                        modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = dueDate,
                        style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(if (isTablet) 48.dp else 40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(if (isTablet) 22.dp else 18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(if (isTablet) 48.dp else 40.dp)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(if (isTablet) 22.dp else 18.dp))
                    }
                }
            }
        }
    }
}

/**
 * A sophisticated dialog for modifying assignment parameters.
 * Manages complex state including deadine pickers, module association, and file type filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentEditDialog(
    assignment: Assignment?,
    courseId: String,
    modules: List<ModuleContent>,
    onDismiss: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    val isTablet = isTablet()
    
    // FORM STATE: Initialized from existing data or set to institutional defaults
    var title by remember { mutableStateOf(assignment?.title ?: "") }
    var description by remember { mutableStateOf(assignment?.description ?: "") }
    var selectedModuleId by remember { mutableStateOf(assignment?.moduleId ?: modules.firstOrNull()?.id ?: "") }
    var dueDateMillis by remember { mutableStateOf(assignment?.dueDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) }
    
    // FORMAT LOGIC: Manages the set of allowed file extensions for submissions
    val initialFormats = assignment?.allowedFileTypes?.split(",")?.toSet() ?: setOf("PDF", "DOCX", "ZIP")
    var allowPdf by remember { mutableStateOf("PDF" in initialFormats) }
    var allowDocx by remember { mutableStateOf("DOCX" in initialFormats) }
    var allowZip by remember { mutableStateOf("ZIP" in initialFormats) }

    // PICKER STATE: Manages sequential Date -> Time selection flow
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDateMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = dueDateMillis }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = dueDateMillis }.get(Calendar.MINUTE)
    )

    // DIALOG: Material 3 Date Selection
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
                    showTimePicker = true // Automatically transition to time selection
                }) { Text("Next", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
            },
            colors = datePickerColors,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(28.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = datePickerColors
            )
        }
    }

    // DIALOG: Custom Time Selection wrapper
    if (showTimePicker) {
        assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                datePickerState.selectedDateMillis?.let { cal.timeInMillis = it }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                dueDateMillis = cal.timeInMillis // Finalize the compound timestamp
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

    // MAIN DIALOG WINDOW: Multi-faceted form for assignment data
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(AdaptiveSpacing.contentPadding()).adaptiveWidth(AdaptiveWidths.Standard),
        content = {
            Surface(
                shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(AdaptiveSpacing.medium())) {
                    Text(
                        text = if (assignment == null) "New Assignment" else "Edit Assignment",
                        style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // PRIMARY DATA: Title and Descriptive Requirements
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

                        // MODULE LINKING: Contextual association within the syllabus
                        Text(
                            text = "Associate Module", 
                            style = if (isTablet) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge, 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            modules.forEach { mod ->
                                val isSelected = selectedModuleId == mod.id
                                Surface(
                                    onClick = { selectedModuleId = mod.id },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

                        // FILE TYPE POLICY: Filters the allowed submission formats
                        Text(
                            text = "Allowed Formats", 
                            style = if (isTablet) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge, 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val chipColors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            FilterChip(selected = allowPdf, onClick = { allowPdf = !allowPdf }, label = { Text("PDF") }, colors = chipColors, leadingIcon = if (allowPdf) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null)
                            FilterChip(selected = allowDocx, onClick = { allowDocx = !allowDocx }, label = { Text("DOCX") }, colors = chipColors, leadingIcon = if (allowDocx) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null)
                            FilterChip(selected = allowZip, onClick = { allowZip = !allowZip }, label = { Text("ZIP") }, colors = chipColors, leadingIcon = if (allowZip) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null)
                        }

                        // DEADLINE SUMMARY: Displays the compound result of the pickers
                        Surface(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Deadline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(
                                        SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(dueDateMillis)),
                                        style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isTablet) 28.dp else 24.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    
                    // ACTION BAR: Commits changes to the database
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.height(if (isTablet) 48.dp else 36.dp)) { Text("Cancel", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary) }
                        
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
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = if (isTablet) 12.dp else 8.dp),
                            modifier = Modifier.height(if (isTablet) 48.dp else 40.dp)
                        ) {
                            Text("Save Assignment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    )
}

/**
 * Safety confirmation dialog for removing academic assessments.
 * Warns about the destructive loss of submissions and grades.
 */
@Composable
fun DeleteConfirmationDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Remove Assignment", fontWeight = FontWeight.Bold) },
        text = { 
            Text("Are you sure you want to permanently delete '$title'? All student submissions and grades for this task will be lost.") 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Delete Assignment", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Keep Assignment", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) 
            }
        },
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp
    )
}
