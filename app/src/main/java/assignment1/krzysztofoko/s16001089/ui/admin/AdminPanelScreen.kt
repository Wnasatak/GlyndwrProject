package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.admin.components.Apps.ApplicationDetailScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.Apps.ApplicationsTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog.CatalogTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Courses.CourseModulesScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.Courses.CoursesTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Courses.ModuleTasksOverlay
import assignment1.krzysztofoko.s16001089.ui.admin.components.Dashboard.AdminDashboardTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.UserManagementTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.UsersLogsTab
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import coil.compose.AsyncImage

enum class AdminSection { DASHBOARD, APPLICATIONS, USERS, CATALOG, COURSES, LOGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onNavigateToUserDetails: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current)
    ))
) {
    val currentSection by viewModel.currentSection.collectAsState()
    var selectedAppForReview by remember { mutableStateOf<AdminApplicationItem?>(null) }
    var selectedCourseForModules by remember { mutableStateOf<Course?>(null) }
    var selectedModuleForTasks by remember { mutableStateOf<ModuleContent?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    val isShowingOverlay = selectedAppForReview != null || selectedCourseForModules != null || selectedModuleForTasks != null

    val sectionTitle = when(currentSection) {
        AdminSection.DASHBOARD -> "Admin Hub"
        AdminSection.APPLICATIONS -> "Enrolment Hub"
        AdminSection.USERS -> "User Directory"
        AdminSection.CATALOG -> "Product Inventory"
        AdminSection.COURSES -> "Course Catalog"
        AdminSection.LOGS -> "System Logs"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        // Only show main Scaffold if no overlay is active
        if (!isShowingOverlay) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { 
                            Row(modifier = Modifier.padding(start = 12.dp)) {
                                Text(text = sectionTitle, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                            }
                        },
                        navigationIcon = {
                            val infiniteTransition = rememberInfiniteTransition("logo_pulse")
                            val animatedShadow by infiniteTransition.animateValue(
                                initialValue = 8.dp,
                                targetValue = 24.dp,
                                typeConverter = Dp.VectorConverter,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "shadow"
                            )

                            AsyncImage(
                                model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                contentDescription = "University Logo",
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(36.dp)
                                    .shadow(
                                        elevation = animatedShadow,
                                        shape = CircleShape,
                                        ambientColor = MaterialTheme.colorScheme.primary,
                                        spotColor = MaterialTheme.colorScheme.primary
                                    )
                                    .clip(CircleShape)
                            )
                        },
                        actions = {
                            if (currentSection == AdminSection.CATALOG) {
                                IconButton(onClick = { showAddProductDialog = true }) {
                                    Icon(Icons.Default.Add, "Add Product")
                                }
                            }
                            if (currentSection == AdminSection.COURSES) {
                                IconButton(onClick = { showAddCourseDialog = true }) {
                                    Icon(Icons.Default.Add, "Add Course")
                                }
                            }
                            if (currentSection == AdminSection.USERS) {
                                IconButton(onClick = { showAddUserDialog = true }) {
                                    Icon(Icons.Default.Add, "Add User")
                                }
                            }
                            IconButton(onClick = onToggleTheme) { 
                                Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) 
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        AdminNavButton(selected = currentSection == AdminSection.DASHBOARD, onClick = { viewModel.setSection(AdminSection.DASHBOARD) }, icon = Icons.Default.Dashboard, label = "Home")
                        AdminNavButton(selected = currentSection == AdminSection.APPLICATIONS, onClick = { viewModel.setSection(AdminSection.APPLICATIONS) }, icon = Icons.AutoMirrored.Filled.Assignment, label = "Apps")
                        AdminNavButton(selected = currentSection == AdminSection.USERS, onClick = { viewModel.setSection(AdminSection.USERS) }, icon = Icons.Default.People, label = "Users")
                        AdminNavButton(selected = currentSection == AdminSection.COURSES, onClick = { viewModel.setSection(AdminSection.COURSES) }, icon = Icons.Default.School, label = "Courses")
                        AdminNavButton(selected = currentSection == AdminSection.CATALOG, onClick = { viewModel.setSection(AdminSection.CATALOG) }, icon = Icons.AutoMirrored.Filled.LibraryBooks, label = "Catalog")
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentSection,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "AdminSectionTransition"
                    ) { section ->
                        when (section) {
                            AdminSection.DASHBOARD -> AdminDashboardTab(viewModel, isDarkTheme)
                            AdminSection.APPLICATIONS -> ApplicationsTab(viewModel = viewModel, onReviewApp = { selectedAppForReview = it })
                            AdminSection.USERS -> UserManagementTab(
                                viewModel = viewModel, 
                                onNavigateToUserDetails = onNavigateToUserDetails,
                                showAddUserDialog = showAddUserDialog,
                                onAddUserDialogConsumed = { showAddUserDialog = false }
                            )
                            AdminSection.COURSES -> CoursesTab(
                                viewModel = viewModel, 
                                isDarkTheme = isDarkTheme, 
                                onCourseSelected = { selectedCourseForModules = it },
                                showAddCourseDialog = showAddCourseDialog,
                                onAddCourseDialogConsumed = { showAddCourseDialog = false }
                            )
                            AdminSection.CATALOG -> CatalogTab(
                                viewModel = viewModel, 
                                isDarkTheme = isDarkTheme,
                                showAddProductDialog = showAddProductDialog,
                                onAddProductDialogConsumed = { showAddProductDialog = false }
                            )
                            AdminSection.LOGS -> UsersLogsTab(viewModel)
                        }
                    }
                }
            }
        } else {
            // Render only the active overlay
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedAppForReview != null) {
                    ApplicationDetailScreen(
                        app = selectedAppForReview!!,
                        onBack = { selectedAppForReview = null },
                        onApprove = { viewModel.approveApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null },
                        onReject = { viewModel.rejectApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null },
                        isDarkTheme = isDarkTheme
                    )
                }

                if (selectedCourseForModules != null && selectedModuleForTasks == null) {
                    CourseModulesScreen(
                        course = selectedCourseForModules!!,
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onBack = { selectedCourseForModules = null },
                        onModuleSelected = { selectedModuleForTasks = it }
                    )
                }

                if (selectedModuleForTasks != null) {
                    ModuleTasksOverlay(
                        module = selectedModuleForTasks!!,
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onDismiss = { selectedModuleForTasks = null }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.AdminNavButton(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}
