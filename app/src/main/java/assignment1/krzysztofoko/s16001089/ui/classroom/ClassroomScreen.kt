package assignment1.krzysztofoko.s16001089.ui.classroom

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
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

    // Explicitly typing the list to avoid inference errors if constants are temporarily unresolved
    val tabs: List<String> = listOf(
        AppConstants.TAB_MODULES, 
        AppConstants.TAB_ASSIGNMENTS, 
        AppConstants.TAB_PERFORMANCE
    )

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)

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
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Live Session Hub
                LiveBroadcastCard(
                    session = activeSession,
                    onJoinClick = { /* Navigate to Live Player */ }
                )

                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
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
                            0 -> ModulesList(modules)
                            1 -> AssignmentsList(assignments)
                            2 -> PerformanceList(grades, assignments)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModulesList(modules: List<ModuleContent>) {
    if (modules.isEmpty()) {
        ClassroomEmptyState(AppConstants.MSG_NO_MODULES)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modules) { module ->
                ModuleItem(module = module, onClick = { /* Open Content */ })
            }
        }
    }
}

@Composable
fun AssignmentsList(assignments: List<Assignment>) {
    if (assignments.isEmpty()) {
        ClassroomEmptyState(AppConstants.MSG_NO_ASSIGNMENTS)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(assignments) { assignment ->
                AssignmentItem(assignment = assignment, onClick = { /* Submit/View */ })
            }
        }
    }
}

@Composable
fun PerformanceList(grades: List<Grade>, assignments: List<Assignment>) {
    if (grades.isEmpty()) {
        ClassroomEmptyState(AppConstants.MSG_NO_GRADES)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(grades) { grade ->
                val title = assignments.find { it.id == grade.assignmentId }?.title ?: "Assignment"
                GradeItem(grade = grade, assignmentTitle = title)
            }
        }
    }
}

@Composable
fun ClassroomEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.School, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            @Suppress("DEPRECATION")
            Text(message, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}
