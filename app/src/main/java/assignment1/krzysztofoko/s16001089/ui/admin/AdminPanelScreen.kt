package assignment1.krzysztofoko.s16001089.ui.admin

import android.content.Intent
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
import assignment1.krzysztofoko.s16001089.DigitalIDActivity
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
import java.util.Locale

/**
 * AdminPanelScreen.kt
 *
 * Grand administrative command centre. Manages navigation and detail overlays for university staff.
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
    val context = LocalContext.current
    val currentSectionState by viewModel.currentSection.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val adminUser by viewModel.currentAdminUser.collectAsState()

    LaunchedEffect(initialSection) {
        if (initialSection != null) {
            try {
                val target = AdminSection.valueOf(initialSection)
                if (currentSectionState != target) viewModel.setSection(target)
            } catch (ignored: Exception) { }
        }
    }

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
    val isShowingOverlay = selectedAppForReview != null || selectedCourseForModules != null || selectedModuleForTasks != null

    val handleBackNavigation = {
        when {
            selectedModuleForTasks != null -> selectedModuleForTasks = null
            selectedCourseForModules != null -> selectedCourseForModules = null
            selectedAppForReview != null -> selectedAppForReview = null
            currentSectionState != AdminSection.DASHBOARD -> viewModel.setSection(AdminSection.DASHBOARD)
            else -> onBack()
        }
    }

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
                            Text(
                                text = sectionTitle, 
                                fontWeight = FontWeight.Black, 
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = if (isTablet) 20.sp else 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            if (currentSectionState != AdminSection.DASHBOARD) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = handleBackNavigation) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                                    AdaptiveBrandedLogo(model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"), contentDescription = "University Logo", logoSize = 32.dp, modifier = Modifier.padding(start = 4.dp))
                                }
                            } else {
                                AdaptiveBrandedLogo(model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"), contentDescription = "University Logo", logoSize = 32.dp, modifier = Modifier.padding(start = 16.dp))
                            }
                        },
                        actions = {
                            if (currentSectionState == AdminSection.CATALOG) IconButton(onClick = { showAddProductDialog = true }) { Icon(Icons.Default.Add, "Add Product") }
                            if (currentSectionState == AdminSection.COURSES) IconButton(onClick = { showAddCourseDialog = true }) { Icon(Icons.Default.Add, "Add Course") }
                            if (currentSectionState == AdminSection.USERS) IconButton(onClick = { showAddUserDialog = true }) { Icon(Icons.Default.Add, "Add User") }

                            ProNotificationIcon(count = unreadCount, isDarkTheme = isDarkTheme, onClick = { viewModel.setSection(AdminSection.NOTIFICATIONS) })

                            if (isTablet) ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, isLoggedIn = true)

                            Box {
                                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface) {
                                    ProMenuHeader("ADMIN HUB")
                                    
                                    // REQUIREMENT: Digital Staff ID shortcut added to Admin Menu
                                    DropdownMenuItem(
                                        text = { Text("Digital Staff ID") }, 
                                        onClick = { 
                                            showMenu = false
                                            val intent = Intent(context, DigitalIDActivity::class.java).apply {
                                                putExtra("USER_NAME", adminUser?.name ?: "Administrator")
                                                putExtra("STUDENT_ID", adminUser?.id?.take(8)?.uppercase(Locale.ROOT) ?: "--------")
                                                putExtra("USER_ROLE", "admin")
                                                putExtra("USER_PHOTO", adminUser?.photoUrl)
                                            }
                                            context.startActivity(intent)
                                        }, 
                                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary) }
                                    )

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
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), tonalElevation = 0.dp, windowInsets = WindowInsets(0, 0, 0, 0), modifier = Modifier.height(80.dp)) {
                        AdminNavButton(selected = currentSectionState == AdminSection.DASHBOARD, onClick = { viewModel.setSection(AdminSection.DASHBOARD) }, icon = Icons.Default.Dashboard, label = "Home")
                        AdminNavButton(selected = currentSectionState == AdminSection.APPLICATIONS, onClick = { viewModel.setSection(AdminSection.APPLICATIONS) }, icon = Icons.AutoMirrored.Filled.Assignment, label = "Apps")
                        AdminNavButton(selected = currentSectionState == AdminSection.USERS, onClick = { viewModel.setSection(AdminSection.USERS) }, icon = Icons.Default.People, label = "Users")
                        AdminNavButton(selected = currentSectionState == AdminSection.COURSES, onClick = { viewModel.setSection(AdminSection.COURSES) }, icon = Icons.Default.School, label = "Courses")
                        AdminNavButton(selected = currentSectionState == AdminSection.CATALOG, onClick = { viewModel.setSection(AdminSection.CATALOG) }, icon = Icons.AutoMirrored.Filled.LibraryBooks, label = "Inventory")
                    }
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                    AnimatedContent(targetState = currentSectionState, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "AdminSectionTransition") { section ->
                        when (section) {
                            AdminSection.DASHBOARD -> AdminDashboardTab(viewModel, isDarkTheme)
                            AdminSection.APPLICATIONS -> ApplicationsTab(viewModel = viewModel, onReviewApp = { app -> selectedAppForReview = app })
                            AdminSection.USERS -> UserManagementTab(viewModel = viewModel, onNavigateToUserDetails = onNavigateToUserDetails, showAddUserDialog = showAddUserDialog, onAddUserDialogConsumed = { showAddUserDialog = false })
                            AdminSection.COURSES -> CoursesTab(viewModel = viewModel, isDarkTheme = isDarkTheme, onCourseSelected = { course -> selectedCourseForModules = course }, showAddCourseDialog = showAddCourseDialog, onAddCourseDialogConsumed = { showAddCourseDialog = false })
                            AdminSection.CATALOG -> CatalogTab(viewModel = viewModel, isDarkTheme = isDarkTheme, showAddProductDialog = showAddProductDialog, onAddProductDialogConsumed = { showAddProductDialog = false }, onNavigateToDetails = onNavigateToBookDetails)
                            AdminSection.LOGS -> UsersLogsTab(viewModel)
                            AdminSection.LIBRARY -> AdminLibraryScreen(allBooks = allBooks, onNavigateToDetails = onNavigateToBookDetails, onExploreMore = onExploreMore, isDarkTheme = isDarkTheme)
                            AdminSection.PROFILE -> AdminDetailScreen(viewModel = viewModel, onNavigateToSettings = onNavigateToProfile)
                            AdminSection.NOTIFICATIONS -> NotificationScreen(onNavigateToItem = { onNavigateToBookDetails(it) }, onNavigateToInvoice = { _ -> }, onNavigateToMessages = { viewModel.setSection(AdminSection.DASHBOARD) }, onNavigateToUser = { onNavigateToUserDetails(it) }, onViewApplications = { viewModel.setSection(AdminSection.APPLICATIONS) }, onBack = { viewModel.setSection(AdminSection.DASHBOARD) }, isDarkTheme = isDarkTheme)
                            AdminSection.BROADCAST -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { BroadcastAnnouncementDialog(viewModel = viewModel, onDismiss = { viewModel.setSection(AdminSection.DASHBOARD) }, onSend = { title, msg, roles, id -> viewModel.sendBroadcastToRoleOrUser(title, msg, roles, id) { viewModel.setSection(AdminSection.DASHBOARD) } }) }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedAppForReview != null) ApplicationDetailScreen(app = selectedAppForReview!!, onBack = { selectedAppForReview = null }, onApprove = { viewModel.approveApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null }, onReject = { viewModel.rejectApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null }, isDarkTheme = isDarkTheme)
                if (selectedCourseForModules != null && selectedModuleForTasks == null) CourseModulesScreen(course = selectedCourseForModules!!, viewModel = viewModel, isDarkTheme = isDarkTheme, onBack = { selectedCourseForModules = null }, onModuleSelected = { selectedModuleForTasks = it })
                if (selectedModuleForTasks != null) ModuleTasksOverlay(module = selectedModuleForTasks!!, viewModel = viewModel, isDarkTheme = isDarkTheme, onDismiss = { selectedModuleForTasks = null })
            }
        }
        LoadingOverlay(isVisible = isProcessing, label = "Updating System...")
    }
}

@Composable
fun RowScope.AdminNavButton(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null, modifier = Modifier.size(24.dp)) },
        label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    )
}
