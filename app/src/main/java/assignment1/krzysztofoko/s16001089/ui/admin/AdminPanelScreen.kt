package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.admin.components.apps.ApplicationDetailScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.apps.ApplicationsTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.catalog.CatalogTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.courses.*
import assignment1.krzysztofoko.s16001089.ui.admin.components.Dashboard.AdminDashboardTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Dashboard.BroadcastAnnouncementDialog
import assignment1.krzysztofoko.s16001089.ui.admin.components.Library.AdminLibraryScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.Profile.AdminDetailScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.UserManagementTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.UsersLogsTab
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.notifications.NotificationScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * AdminPanelScreen.kt
 *
 * Grand administrative hub. Manages navigation and overlays for system-wide operations.
 * Fix: Removed redundant back button on main dashboard and resolved hierarchical navigation.
 */

enum class AdminSection { DASHBOARD, APPLICATIONS, USERS, CATALOG, COURSES, LOGS, LIBRARY, PROFILE, NOTIFICATIONS, BROADCAST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onNavigateToUserDetails: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBookDetails: (String) -> Unit,
    onExploreMore: () -> Unit = {},
    allBooks: List<Book>,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onLogoutClick: () -> Unit = {},
    initialSection: String? = null,
    viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current)
    ))
) {
    // --- DATA STREAMS --- //
    val currentSectionState by viewModel.currentSection.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    // --- INITIAL NAVIGATION --- //
    LaunchedEffect(initialSection) {
        if (initialSection != null) {
            try {
                val target = AdminSection.valueOf(initialSection)
                if (currentSectionState != target) {
                    viewModel.setSection(target)
                }
            } catch (ignored: Exception) { }
        }
    }

    // --- OVERLAY & DIALOG STATES --- //
    var selectedAppForReview by remember { mutableStateOf<AdminApplicationItem?>(null) }
    var selectedCourseForModules by remember { mutableStateOf<Course?>(null) }
    var selectedModuleForTasks by remember { mutableStateOf<ModuleContent?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showThemeSubMenu by remember { mutableStateOf(false) }

    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || (currentTheme == Theme.CUSTOM)
    val isTablet = isTablet()

    // Flag to determine if a detail view is covering the main dashboard.
    val isShowingOverlay = selectedAppForReview != null || selectedCourseForModules != null || selectedModuleForTasks != null

    /**
     * Unified Back Navigation Handler
     * Manages hierarchical dismissal: Tasks -> Modules -> Hub -> Exit
     */
    val handleBackNavigation = {
        when {
            selectedModuleForTasks != null -> selectedModuleForTasks = null
            selectedCourseForModules != null -> selectedCourseForModules = null
            selectedAppForReview != null -> selectedAppForReview = null
            currentSectionState != AdminSection.DASHBOARD -> viewModel.setSection(AdminSection.DASHBOARD)
            else -> onBack()
        }
    }

    // Intercept hardware and gesture back navigation.
    BackHandler(enabled = true, onBack = handleBackNavigation)

    val sectionTitle = when(currentSectionState) {
        AdminSection.DASHBOARD -> "Admin Hub"
        AdminSection.APPLICATIONS -> "Enrolment Hub"
        AdminSection.USERS -> "User Directory"
        AdminSection.CATALOG -> "Product Inventory"
        AdminSection.COURSES -> "Course Catalog"
        AdminSection.LOGS -> "System Logs"
        AdminSection.LIBRARY -> "My Library"
        AdminSection.PROFILE -> "My Profile"
        AdminSection.NOTIFICATIONS -> "Notifications"
        AdminSection.BROADCAST -> "Broadcast Center"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        if (!isShowingOverlay) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sectionTitle, 
                                    fontWeight = FontWeight.Black, 
                                    style = if(sectionTitle == "Product Inventory") MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        navigationIcon = {
                            // Back button only visible when NOT on the main Dashboard hub.
                            if (currentSectionState != AdminSection.DASHBOARD) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = handleBackNavigation) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                    AdaptiveBrandedLogo(
                                        model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                                        contentDescription = "University Logo",
                                        logoSize = 32.dp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            } else {
                                // Just the logo on the main hub.
                                AdaptiveBrandedLogo(
                                    model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                                    contentDescription = "University Logo",
                                    logoSize = 32.dp,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        },
                        actions = {
                            // Contextual 'Add' actions depending on the active system hub.
                            if (currentSectionState == AdminSection.CATALOG) {
                                IconButton(onClick = { showAddProductDialog = true }) { Icon(Icons.Default.Add, "Add Product") }
                            }
                            if (currentSectionState == AdminSection.COURSES) {
                                IconButton(onClick = { showAddCourseDialog = true }) { Icon(Icons.Default.Add, "Add Course") }
                            }
                            if (currentSectionState == AdminSection.USERS) {
                                IconButton(onClick = { showAddUserDialog = true }) { Icon(Icons.Default.Add, "Add User") }
                            }

                            ProNotificationIcon(
                                count = unreadCount,
                                isDarkTheme = isDarkTheme,
                                onClick = { viewModel.setSection(AdminSection.NOTIFICATIONS) }
                            )

                            if (isTablet) {
                                ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, isLoggedIn = true)
                            }

                            Box {
                                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface) {
                                    ProMenuHeader("ADMIN HUB")
                                    DropdownMenuItem(text = { Text("My Profile") }, onClick = { showMenu = false; viewModel.setSection(AdminSection.PROFILE) }, leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = MaterialTheme.colorScheme.primary) })
                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_PROFILE_SETTINGS) }, onClick = { showMenu = false; onNavigateToProfile() }, leadingIcon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary) })
                                    DropdownMenuItem(text = { Text("My Library") }, onClick = { showMenu = false; viewModel.setSection(AdminSection.LIBRARY) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, null, tint = MaterialTheme.colorScheme.primary) })
                                    DropdownMenuItem(text = { Text("Theme Options") }, onClick = { showMenu = false; showThemeSubMenu = true }, leadingIcon = { Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary) })
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    DropdownMenuItem(text = { Text("Sign Off", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }, onClick = { showMenu = false; onLogoutClick() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) })
                                }
                                ThemeSelectionDropdown(expanded = showThemeSubMenu, onDismissRequest = { showThemeSubMenu = false }, onThemeChange = onThemeChange, isLoggedIn = true)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                },
                bottomBar = {
                    // Main System Hub Navigation.
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.height(80.dp)
                    ) {
                        AdminNavButton(selected = currentSectionState == AdminSection.DASHBOARD, onClick = { viewModel.setSection(AdminSection.DASHBOARD) }, icon = Icons.Default.Dashboard, label = "Home")
                        AdminNavButton(selected = currentSectionState == AdminSection.APPLICATIONS, onClick = { viewModel.setSection(AdminSection.APPLICATIONS) }, icon = Icons.AutoMirrored.Filled.Assignment, label = "Apps")
                        AdminNavButton(selected = currentSectionState == AdminSection.USERS, onClick = { viewModel.setSection(AdminSection.USERS) }, icon = Icons.Default.People, label = "Users")
                        AdminNavButton(selected = currentSectionState == AdminSection.COURSES, onClick = { viewModel.setSection(AdminSection.COURSES) }, icon = Icons.Default.School, label = "Courses")
                        AdminNavButton(selected = currentSectionState == AdminSection.CATALOG, onClick = { viewModel.setSection(AdminSection.CATALOG) }, icon = Icons.AutoMirrored.Filled.LibraryBooks, label = "Inventory")
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentSectionState,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "AdminSectionTransition"
                    ) { section ->
                        when (section) {
                            AdminSection.DASHBOARD -> AdminDashboardTab(viewModel, isDarkTheme)
                            AdminSection.APPLICATIONS -> ApplicationsTab(viewModel = viewModel, onReviewApp = { app -> selectedAppForReview = app })
                            AdminSection.USERS -> UserManagementTab(viewModel = viewModel, onNavigateToUserDetails = onNavigateToUserDetails, showAddUserDialog = showAddUserDialog, onAddUserDialogConsumed = { showAddUserDialog = false })
                            AdminSection.COURSES -> CoursesTab(
                                viewModel = viewModel, 
                                isDarkTheme = isDarkTheme, 
                                onCourseSelected = { course: Course -> selectedCourseForModules = course }, 
                                showAddCourseDialog = showAddCourseDialog, 
                                onAddCourseDialogConsumed = { showAddCourseDialog = false }
                            )
                            AdminSection.CATALOG -> CatalogTab(viewModel = viewModel, isDarkTheme = isDarkTheme, showAddProductDialog = showAddProductDialog, onAddProductDialogConsumed = { showAddProductDialog = false }, onNavigateToDetails = onNavigateToBookDetails)
                            AdminSection.LOGS -> UsersLogsTab(viewModel)
                            AdminSection.LIBRARY -> AdminLibraryScreen(allBooks = allBooks, onNavigateToDetails = onNavigateToBookDetails, onExploreMore = onExploreMore, isDarkTheme = isDarkTheme)
                            AdminSection.PROFILE -> AdminDetailScreen(viewModel = viewModel, onNavigateToSettings = onNavigateToProfile)
                            AdminSection.NOTIFICATIONS -> {
                                NotificationScreen(onNavigateToItem = { onNavigateToBookDetails(it) }, onNavigateToInvoice = { _ -> }, onNavigateToMessages = { viewModel.setSection(AdminSection.DASHBOARD) }, onNavigateToUser = { onNavigateToUserDetails(it) }, onViewApplications = { viewModel.setSection(AdminSection.APPLICATIONS) }, onBack = { viewModel.setSection(AdminSection.DASHBOARD) }, isDarkTheme = isDarkTheme)
                            }
                            AdminSection.BROADCAST -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    BroadcastAnnouncementDialog(viewModel = viewModel, onDismiss = { viewModel.setSection(AdminSection.DASHBOARD) }, onSend = { title, message, roles, specificUserId -> viewModel.sendBroadcastToRoleOrUser(title, message, roles, specificUserId) { _ -> viewModel.setSection(AdminSection.DASHBOARD) } })
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Renders immersive full-screen management overlays.
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
                    // Back button in course modules screen should return to the courses tab.
                    CourseModulesScreen(course = selectedCourseForModules!!, viewModel = viewModel, isDarkTheme = isDarkTheme, onBack = { selectedCourseForModules = null }, onModuleSelected = { selectedModuleForTasks = it })
                }

                if (selectedModuleForTasks != null) {
                    // Back button in module tasks overlay should return to the module screen.
                    ModuleTasksOverlay(module = selectedModuleForTasks!!, viewModel = viewModel, isDarkTheme = isDarkTheme, onDismiss = { selectedModuleForTasks = null })
                }
            }
        }

        // Global Processing Feedback Overlay.
        LoadingOverlay(isVisible = isProcessing, label = "Updating System...")
    }
}

/**
 * Branded Navigation Item for the Admin Command Centre.
 */
@Composable
fun RowScope.AdminNavButton(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null, modifier = Modifier.size(24.dp)) },
        label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    )
}
