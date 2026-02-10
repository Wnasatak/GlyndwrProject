package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Assignments.ClassroomAssignmentsTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Modules.ClassroomModulesTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Performance.ClassroomPerformanceTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts.ClassroomBroadcastsTab
import assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts.BroadcastReplayView
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    courseId: String,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: ClassroomViewModel = viewModel(factory = ClassroomViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        courseId = courseId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
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

    val tabs = listOf(
        AppConstants.TAB_MODULES, 
        AppConstants.TAB_ASSIGNMENTS, 
        AppConstants.TAB_PERFORMANCE,
        "Broadcasts"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)

        if (isLiveViewActive && activeSession != null) {
            LiveStreamView(
                session = activeSession!!,
                chatMessages = liveChatMessages,
                onSendMessage = { viewModel.sendLiveChatMessage(it) },
                onClose = { viewModel.leaveLiveSession() }
            )
        } else if (selectedBroadcast != null) {
            BroadcastReplayView(
                session = selectedBroadcast!!,
                onClose = { viewModel.selectBroadcast(null) }
            )
        } else if (selectedAssignment != null) {
            AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) {
                AssignmentSubmissionView(
                    assignment = selectedAssignment!!,
                    isSubmitting = isSubmitting,
                    onSubmit = { content -> viewModel.submitAssignment(selectedAssignment!!.id, content) },
                    onCancel = { viewModel.selectAssignment(null) }
                )
            }
        } else if (selectedModule != null) {
            AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Medium) {
                ModuleDetailView(
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
                    CenterAlignedTopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { Text(AppConstants.TITLE_CLASSROOM, fontWeight = FontWeight.Black) },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                        },
                        actions = {
                            IconButton(onClick = onToggleTheme) {
                                Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
                }
            ) { padding ->
                AdaptiveScreenContainer(
                    maxWidth = AdaptiveWidths.Wide,
                    modifier = Modifier.padding(padding)
                ) { isTablet ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Live Session Card - Center constrained on tablets
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Box(modifier = if (isTablet) Modifier.widthIn(max = AdaptiveWidths.Medium) else Modifier.fillMaxWidth()) {
                                LiveBroadcastCard(
                                    session = activeSession,
                                    onJoinClick = { viewModel.enterLiveSession() }
                                )
                            }
                        }

                        // Navigation Tabs
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            edgePadding = 12.dp,
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { viewModel.selectTab(index) },
                                    text = { 
                                        Text(
                                            text = title, 
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        ) 
                                    }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Tab Content
                        Box(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
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
