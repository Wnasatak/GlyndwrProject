package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Assignments.AssignmentSubmissionScreen
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Assignments.ClassroomAssignmentsTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Modules.ClassroomModulesTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Modules.ModuleDetailScreen
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Performance.ClassroomPerformanceTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts.ClassroomBroadcastsTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts.BroadcastReplayView
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts.LiveStreamView
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

/**
 * ClassroomScreen: The central hub for student academic activity within a specific course.
 * It provides access to course modules, assignments, performance metrics (grades), and live broadcasts.
 * 
 * Key Features:
 * 1. Targeted Navigation Peeking: The navigation bar expands the label of the active tab for 5 seconds 
 *    on selection to ensure the user knows their location without cluttering the UI permanently.
 * 2. Adaptive Content: Switches between list views and detail views (Modules/Assignments) seamlessly.
 * 3. Live Integration: Displays active stream cards and provides access to interactive live classrooms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    courseId: String,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: ClassroomViewModel = viewModel(factory = ClassroomViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        courseId = courseId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    // Collect reactive state from the ViewModel
    val selectedTab by viewModel.selectedTab.collectAsState()
    val modules by viewModel.modules.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val liveChatMessages by viewModel.liveChatMessages.collectAsState()
    val isLiveViewActive by viewModel.isLiveViewActive.collectAsState()
    val selectedAssignment by viewModel.selectedAssignment.collectAsState()
    val selectedModule by viewModel.selectedModule.collectAsState()
    val selectedBroadcast by viewModel.selectedBroadcast.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val sharedBroadcasts by viewModel.sharedBroadcasts.collectAsState()

    // --- TARGETED PEERING LOGIC ---
    // This state controls the temporary visibility of the selected tab's label.
    var showSelectedLabel by remember { mutableStateOf(true) }

    // This effect is re-triggered every time the user switches tabs (selectedTab changes).
    LaunchedEffect(selectedTab) {
        showSelectedLabel = true     // Immediately show the label
        delay(5000L)                 // Wait for 5 seconds (User preference for readability)
        showSelectedLabel = false    // Hide it to revert back to icon-only mode
    }

    val tabs = listOf(
        AppConstants.TAB_MODULES to Icons.Default.LibraryBooks, 
        AppConstants.TAB_ASSIGNMENTS to Icons.Default.Assignment, 
        AppConstants.TAB_PERFORMANCE to Icons.Default.Grade,
        "Broadcasts" to Icons.Default.Podcasts
    )

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)

        // Navigation & Detail Routing Logic
        if (isLiveViewActive && activeSession != null) {
            LiveStreamView(
                session = activeSession!!,
                chatMessages = liveChatMessages,
                onSendMessage = { msg -> viewModel.sendLiveChatMessage(msg) },
                onClose = { viewModel.leaveLiveSession() }
            )
        } else if (selectedBroadcast != null) {
            BroadcastReplayView(
                session = selectedBroadcast!!,
                onClose = { viewModel.selectBroadcast(null) }
            )
        } else if (selectedAssignment != null) {
            AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) {
                AssignmentSubmissionScreen(
                    assignment = selectedAssignment!!,
                    isSubmitting = isSubmitting,
                    onSubmit = { content: String -> viewModel.submitAssignment(selectedAssignment!!.id, content) },
                    onCancel = { viewModel.selectAssignment(null) }
                )
            }
        } else if (selectedModule != null) {
            AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) {
                ModuleDetailScreen(
                    module = selectedModule!!,
                    assignments = assignments,
                    onBack = { viewModel.selectModule(null) },
                    onSubmitAssignment = { viewModel.selectAssignment(it) }
                )
            }
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    ) {
                        // Header: Displays the screen title and back button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) { 
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") 
                            }
                            Text(
                                AppConstants.TITLE_CLASSROOM, 
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                style = MaterialTheme.typography.titleLarge
                            ) 
                        }
                        
                        // Adaptive Navigation Bar: Features the dynamic "Peek" animation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.widthIn(max = 600.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                        .fillMaxWidth()
                                        .animateContentSize(), // Ensures smooth bar resizing when labels appear/disappear
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    tabs.forEachIndexed { index, (title, icon) ->
                                        val isSelected = selectedTab == index
                                        Box(
                                            modifier = Modifier
                                                // Dynamic weight: Expands the active tab box to make room for text
                                                .weight(if (isSelected && showSelectedLabel) 1.8f else 1f) 
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                                    else Color.Transparent
                                                )
                                                .clickable { 
                                                    if (selectedTab != index) {
                                                        viewModel.selectTab(index) // Switch tab
                                                    } else {
                                                        showSelectedLabel = true // Re-peek if already active
                                                    }
                                                }
                                                .padding(vertical = 10.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                
                                                // ANIMATED LABEL: Expands horizontally and fades in only for the active tab
                                                AnimatedVisibility(
                                                    visible = isSelected && showSelectedLabel,
                                                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                                                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = title,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = FontWeight.Black, 
                                                            color = MaterialTheme.colorScheme.onPrimary,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                AdaptiveScreenContainer(
                    maxWidth = AdaptiveWidths.Wide,
                    modifier = Modifier.padding(padding)
                ) { isTablet ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Live Broadcast/Session Integration
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 0.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = if (isTablet) Modifier.widthIn(max = AdaptiveWidths.Medium) else Modifier.fillMaxWidth()) {
                                LiveBroadcastCard(
                                    session = activeSession,
                                    onJoinClick = { viewModel.enterLiveSession() }
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Cross-fade Tab Transition
                        Box(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = { 
                                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f))
                                        .togetherWith(fadeOut(animationSpec = tween(300))) 
                                },
                                label = "tabTransition"
                            ) { targetTab ->
                                when (targetTab) {
                                    0 -> ClassroomModulesTab(modules, onModuleClick = { viewModel.selectModule(it) })
                                    1 -> ClassroomAssignmentsTab(assignments, onSelect = { viewModel.selectAssignment(it) })
                                    2 -> ClassroomPerformanceTab(grades, assignments)
                                    3 -> ClassroomBroadcastsTab(
                                        sharedBroadcasts = sharedBroadcasts, 
                                        modules = modules, 
                                        assignments = assignments,
                                        onBroadcastClick = { viewModel.selectBroadcast(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
