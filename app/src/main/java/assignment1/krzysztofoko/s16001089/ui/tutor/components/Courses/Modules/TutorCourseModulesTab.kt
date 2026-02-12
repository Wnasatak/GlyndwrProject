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
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import java.util.*

/**
 * TutorCourseModulesTab provides a comprehensive management interface for course curriculum.
 * It allows tutors to organize learning materials into sequential modules, including 
 * support for video lectures, PDF documents, and knowledge-check quizzes.
 *
 * Key Features:
 * - Sequential module management with custom display ordering.
 * - Dynamic content type classification (Video, Reading, Quiz).
 * - Full CRUD support (Create, Read, Update, Delete) via specialized dialogs.
 * - Adaptive layout optimized for high information density on tablets.
 */
@Composable
fun TutorCourseModulesTab(
    viewModel: TutorViewModel
) {
    // REACTIVE DATA STREAMS: Observes the course context and its associated modules
    val course by viewModel.selectedCourse.collectAsState()
    val modules by viewModel.selectedCourseModules.collectAsState()

    // UI STATE: Manages editing context, creation triggers, and deletion safety checks
    var editingModule by remember { mutableStateOf<ModuleContent?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var moduleToDelete by remember { mutableStateOf<ModuleContent?>(null) }

    // ADAPTIVE CONTAINER: Ensures proper centering and width-capping on tablets
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) { isTablet ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
                Spacer(Modifier.height(12.dp))
                
                // HEADER: Displays the course title and management context using institutional branding
                AdaptiveDashboardHeader(
                    title = "Course Modules",
                    subtitle = course?.title ?: "Curriculum Management",
                    icon = Icons.Default.ViewModule
                )
                
                Spacer(Modifier.height(20.dp))

                // CONTENT DISPATCHER: Renders either the empty state or the chronological module list
                if (modules.isEmpty()) {
                    EmptyModulesState { showCreateDialog = true }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp) // Prevents FAB occlusion
                    ) {
                        items(modules.sortedBy { it.order }) { module ->
                            ModuleItemCard(
                                module = module,
                                onEdit = { editingModule = module },
                                onDelete = { moduleToDelete = module }
                            )
                        }
                    }
                }
            }

            // PRIMARY ACTION: FAB for adding new curriculum content
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Module", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AdaptiveSpacing.contentPadding())
                    .padding(bottom = 16.dp)
            )
        }
    }

    // DIALOG: Unified creation and editing interface for module data
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
                // PERSISTENCE: Commits the module change to the Room database via ViewModel
                viewModel.upsertModule(updated)
                showCreateDialog = false
                editingModule = null
            }
        )
    }

    // SAFETY DIALOG: Confirmation before destructive deletion of academic content
    if (moduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { moduleToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Module", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${moduleToDelete?.title}'? This will disconnect all materials linked to this module.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteModule(moduleToDelete!!.id)
                        moduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { moduleToDelete = null }) { 
                    Text("Cancel", color = MaterialTheme.colorScheme.primary) 
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

/**
 * Placeholder UI displayed when a course has no curriculum modules.
 * Encourages the tutor to begin building the syllabus.
 */
@Composable
fun EmptyModulesState(onAction: () -> Unit) {
    val isTablet = isTablet()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.PostAdd, 
                null, 
                modifier = Modifier.size(if (isTablet) 120.dp else 80.dp), 
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Your syllabus is empty", 
                style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction, 
                shape = RoundedCornerShape(12.dp),
                modifier = if (isTablet) Modifier.adaptiveButtonWidth() else Modifier
            ) {
                Text("Create First Module")
            }
        }
    }
}

/**
 * A responsive card component representing a single learning module.
 * Features content-type specific iconography and management actions.
 */
@Composable
fun ModuleItemCard(
    module: ModuleContent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdaptiveDashboardCard { isTablet ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Visual Type Indicator: Dynamic icon selection based on content classification
            Surface(
                modifier = Modifier.size(if (isTablet) 48.dp else 40.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(if (isTablet) 12.dp else 10.dp)
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isTablet) 28.dp else 22.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(if (isTablet) 20.dp else 16.dp))
            
            // METADATA: Module Title and Sequential Sequence Number
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Sequence: #${module.order}",
                    style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // MANAGEMENT ACTIONS: Quick-edit and delete triggers
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(if (isTablet) 40.dp else 32.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(if (isTablet) 20.dp else 16.dp))
                }
                FilledTonalIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(if (isTablet) 40.dp else 32.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(if (isTablet) 20.dp else 16.dp))
                }
            }
        }
        
        // Optional description field for module context
        if (module.description.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = module.description,
                style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                lineHeight = if (isTablet) 22.sp else 18.sp
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // CONTENT TYPE BADGE: Visual classification for students and tutors
        Row(verticalAlignment = Alignment.CenterVertically) {
            val typeLabel = when(module.contentType) {
                "VIDEO" -> "Video Lecture"
                "PDF" -> "Reading Material"
                "QUIZ" -> "Knowledge Check"
                else -> "General Content"
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = typeLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = if (isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * A specialized dialog for entering or modifying module metadata.
 * Manages complex state including content type selection and resource URLs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleEditDialog(
    module: ModuleContent?,
    courseId: String,
    nextOrder: Int,
    onDismiss: () -> Unit,
    onSave: (ModuleContent) -> Unit
) {
    val isTablet = isTablet()
    
    // LOCAL FORM STATE: Initialized from the existing module or defaults for a new one
    var title by remember { mutableStateOf(module?.title ?: "") }
    var description by remember { mutableStateOf(module?.description ?: "") }
    var contentType by remember { mutableStateOf(module?.contentType ?: "VIDEO") }
    var contentUrl by remember { mutableStateOf(module?.contentUrl ?: "") }
    var order by remember { mutableStateOf(module?.order?.toString() ?: nextOrder.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(AdaptiveSpacing.contentPadding())
            .adaptiveWidth(AdaptiveWidths.Standard),
        content = {
            Surface(
                shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(AdaptiveSpacing.medium()).verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (module == null) "Add Curriculum Module" else "Edit Module",
                        style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // TITLE INPUT: Required field for curriculum organization
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Module Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // DESCRIPTION INPUT: Qualitative context for the module content
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Module Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(24.dp))
                    
                    // CONTENT TYPE SELECTION: Visual picker for material classification
                    Text("Content Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TypeSelectionIcon(icon = Icons.Default.PlayCircle, label = "VIDEO", isSelected = contentType == "VIDEO") { contentType = "VIDEO" }
                        TypeSelectionIcon(icon = Icons.Default.Description, label = "PDF", isSelected = contentType == "PDF") { contentType = "PDF" }
                        TypeSelectionIcon(icon = Icons.Default.Quiz, label = "QUIZ", isSelected = contentType == "QUIZ") { contentType = "QUIZ" }
                    }

                    Spacer(Modifier.height(24.dp))

                    // RESOURCE URL: External link to the actual learning asset (Stream or Cloud Document)
                    OutlinedTextField(
                        value = contentUrl,
                        onValueChange = { contentUrl = it },
                        label = { Text("Resource URL (Video/PDF link)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Link, null) }
                    )

                    Spacer(Modifier.height(16.dp))

                    // SEQUENTIAL ORDER: Numeric input to control the display sequence in the syllabus
                    OutlinedTextField(
                        value = order,
                        onValueChange = { if (it.all { c -> c.isDigit() }) order = it },
                        label = { Text("Display Order") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(32.dp))
                    
                    // ACTION BAR: Commit or discard changes
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss, modifier = Modifier.height(if (isTablet) 48.dp else 36.dp)) { Text("Cancel") }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                // VALIDATION & EMISSION: Constructs a new ModuleContent object and emits it via onSave
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(if (isTablet) 48.dp else 40.dp)
                        ) {
                            Text("Save Module", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    )
}

/**
 * A branded interactive icon used for content type classification.
 * Features high-contrast selection states and institutional typography.
 */
@Composable
fun TypeSelectionIcon(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val isTablet = isTablet()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(if (isTablet) 56.dp else 48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    null, 
                    tint = if (isSelected) Color.White else Color.Gray,
                    modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                )
            }
        }
        Text(
            text = label, 
            style = if (isTablet) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall, 
            modifier = Modifier.padding(top = 4.dp), 
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}
