package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorDashboardTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.TutorCoursesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Students.TutorStudentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.TutorMessagesTab
import com.google.firebase.auth.FirebaseAuth

/**
 * The Central Dashboard for Tutors.
 * Orchestrates My Courses, Student Management, and messaging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorPanelScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: TutorViewModel = viewModel(factory = TutorViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        tutorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val currentSection by viewModel.currentSection.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(text = AppConstants.TITLE_TUTOR_PANEL, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    },
                    actions = { IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    TutorNavButton(
                        selected = currentSection == TutorSection.DASHBOARD,
                        onClick = { viewModel.setSection(TutorSection.DASHBOARD) },
                        icon = Icons.Default.Dashboard,
                        label = "Home"
                    )
                    TutorNavButton(
                        selected = currentSection == TutorSection.MY_COURSES,
                        onClick = { viewModel.setSection(TutorSection.MY_COURSES) },
                        icon = Icons.Default.School,
                        label = "Courses"
                    )
                    TutorNavButton(
                        selected = currentSection == TutorSection.STUDENTS,
                        onClick = { viewModel.setSection(TutorSection.STUDENTS) },
                        icon = Icons.Default.People,
                        label = "Students"
                    )
                    TutorNavButton(
                        selected = currentSection == TutorSection.MESSAGES,
                        onClick = { viewModel.setSection(TutorSection.MESSAGES) },
                        icon = Icons.Default.Chat,
                        label = "Messages"
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = currentSection,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TutorSectionTransition"
                ) { section ->
                    when (section) {
                        TutorSection.DASHBOARD -> TutorDashboardTab(viewModel, isDarkTheme)
                        TutorSection.MY_COURSES -> TutorCoursesTab(viewModel)
                        TutorSection.STUDENTS -> TutorStudentsTab(viewModel)
                        TutorSection.MESSAGES -> TutorMessagesTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TutorNavButton(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}
