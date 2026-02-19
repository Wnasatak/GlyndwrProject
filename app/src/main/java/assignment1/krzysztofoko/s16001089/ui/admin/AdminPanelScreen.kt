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
import assignment1.krzysztofoko.s16001089.ui.admin.components.courses.CourseModulesScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.courses.CoursesTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.courses.ModuleTasksOverlay
import assignment1.krzysztofoko.s16001089.ui.admin.components.dashboard.AdminDashboardTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.dashboard.BroadcastAnnouncementDialog
import assignment1.krzysztofoko.s16001089.ui.admin.components.library.AdminLibraryScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.profile.AdminDetailScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.users.UserManagementTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.users.UsersLogsTab
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.notifications.NotificationScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * AdminPanelScreen.kt
 *
 * Grand administrative command centre. Manages navigation and detail overlays for university staff.
 * Refactored to provide responsive typography for mobile and tablet devices.
 */

/**
 * Enum representing different sections of the Admin Panel.
 */
enum class AdminSection { DASHBOARD, APPLICATIONS, USERS, CATALOG, COURSES, LOGS, LIBRARY, PROFILE, NOTIFICATIONS, BROADCAST }

/**
 * The main screen for administrative tasks.
 * 
 * @param onBack Callback when the user wants to go back.
 * @param onNavigateToUserDetails Callback to navigate to a specific user's details.
 * @param onNavigateToProfile Callback to navigate to the current user's profile settings.
 * @param onNavigateToBookDetails Callback to navigate to a book's detail screen.
 * @param onExploreMore Callback for exploratory actions.
 * @param allBooks List of all books available in the system.
 * @param currentTheme The currently active theme.
 * @param onThemeChange Callback to update the system theme.
 * @param onLogoutClick Callback for signing out.
 * @param initialSection Optional section to display upon entry.
 * @param viewModel The state holder for admin operations.
 */
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
    // --- DATA STREAMS FROM VIEWMODEL --- //
    val currentSectionState by viewModel.currentSection.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    // --- INITIAL NAVIGATION LOGIC --- //
    // If an initial section is provided (e.g., via notification deep link), switch to it.
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

    // --- OVERLAY & DIALOG UI STATES --- //
    var selectedAppForReview by remember { mutableStateOf<AdminApplicationItem?>(null) }
    var selectedCourseForModules by remember { mutableStateOf<Course?>(null) }
    var selectedModuleForTasks by remember { mutableStateOf<ModuleContent?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showThemeSubMenu by remember { mutableStateOf(false) }

    // --- UI CONFIGURATION --- //
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || (currentTheme == Theme.CUSTOM)
    val isTablet = isTablet()

    // Flag to determine if a detail view (overlay) is currently covering the main dashboard/tabs.
    val isShowingOverlay = selectedAppForReview != null || selectedCourseForModules != null || selectedModuleForTasks != null

    /**
     * Unified Back Navigation Handler
     * Manages hierarchical dismissal: Tasks -> Modules -> Main Tab -> Exit Admin Panel
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

    // Intercept hardware back button and gesture navigation.
    BackHandler(enabled = true, onBack = handleBackNavigation)

    // Centralized title management for the TopAppBar based on current section.
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
        // Aesthetic wavy background consistent with the app's visual identity.
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        // Only show the main Scaffold if no detail overlay is active.
        if (!isShowingOverlay) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Adaptive typography: ensure titles are legible but fit on smaller screens.
                                Text(
                                    text = sectionTitle, 
                                    fontWeight = FontWeight.Black, 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = if (isTablet) 20.sp else 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            // Show back arrow if not on the main dashboard; otherwise show university logo.
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
                                AdaptiveBrandedLogo(
                                    model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                                    contentDescription = "University Logo",
                                    logoSize = 32.dp,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        },
                        actions = {
                            // Contextual actions (e.g., "Add" buttons) based on current tab.
                            if (currentSectionState == AdminSection.CATALOG) {
                                IconButton(onClick = { showAddProductDialog = true }) { Icon(Icons.Default.Add, "Add Product") }
                            }
                            if (currentSectionState == AdminSection.COURSES) {
                                IconButton(onClick = { showAddCourseDialog = true }) { Icon(Icons.Default.Add, "Add Course") }
                            }
                            if (currentSectionState == AdminSection.USERS) {
                                IconButton(onClick = { showAddUserDialog = true }) { Icon(Icons.Default.Add, "Add User") }
                            }

                            // Notification badge icon.
                            ProNotificationIcon(
                                count = unreadCount,
                                isDarkTheme = isDarkTheme,
                                onClick = { viewModel.setSection(AdminSection.NOTIFICATIONS) }
                            )

                            // Quick theme toggle for tablet users.
                            if (isTablet) {
                                ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, isLoggedIn = true)
                            }

                            // Overflow menu for profile, settings, and logout.
                            Box {
                                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                                DropdownMenu(
                                    expanded = showMenu, 
                                    onDismissRequest = { showMenu = false }, 
                                    shape = RoundedCornerShape(16.dp), 
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
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
                    // Global navigation bar for switching between primary admin modules.
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
                // MAIN CONTENT AREA: Animates transitions between sections.
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
                                onCourseSelected = { course -> selectedCourseForModules = course }, 
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
                                    BroadcastAnnouncementDialog(
                                        viewModel = viewModel, 
                                        onDismiss = { viewModel.setSection(AdminSection.DASHBOARD) }, 
                                        onSend = { title: String, msg: String, roles: List<String>, id: String? -> 
                                            viewModel.sendBroadcastToRoleOrUser(title, msg, roles, id) { count: Int -> 
                                                viewModel.setSection(AdminSection.DASHBOARD) 
                                            } 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // DETAIL OVERLAYS: These cover the main UI when specific items are being inspected or edited.
            Box(modifier = Modifier.fillMaxSize()) {
                // Application Review Overlay
                if (selectedAppForReview != null) {
                    ApplicationDetailScreen(
                        app = selectedAppForReview!!,
                        onBack = { selectedAppForReview = null },
                        onApprove = { viewModel.approveApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null },
                        onReject = { viewModel.rejectApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null },
                        isDarkTheme = isDarkTheme
                    )
                }

                // Course Modules Overlay
                if (selectedCourseForModules != null && selectedModuleForTasks == null) {
                    CourseModulesScreen(course = selectedCourseForModules!!, viewModel = viewModel, isDarkTheme = isDarkTheme, onBack = { selectedCourseForModules = null }, onModuleSelected = { selectedModuleForTasks = it })
                }

                // Module Tasks Overlay (Nested inside Modules)
                if (selectedModuleForTasks != null) {
                    ModuleTasksOverlay(module = selectedModuleForTasks!!, viewModel = viewModel, isDarkTheme = isDarkTheme, onDismiss = { selectedModuleForTasks = null })
                }
            }
        }

        // Global processing indicator (e.g., during database writes or network calls).
        LoadingOverlay(isVisible = isProcessing, label = "Updating System...")
    }
}

/**
 * Branded Navigation Button for the Admin Command Centre bottom bar.
 * Provides consistent styling and responsive labels.
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
