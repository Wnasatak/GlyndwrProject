package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.theme.*
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardViewModel
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardViewModelFactory
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.ui.profile.components.ThemeBuilderDialog
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.admin.AdminSection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * TopLevelScaffold acts as the primary layout wrapper for the application, providing
 * consistent navigation (TopBar, BottomBar, NavRail) and theme management.
 *
 * @param currentUser The currently authenticated Firebase user.
 * @param localUser User data stored locally (role, balance, etc.).
 * @param userTheme Custom theme configuration for the user.
 * @param currentRoute The current navigation destination.
 * @param currentTutorSection Active section within the Tutor Hub.
 * @param currentAdminSection Active section within the Admin Hub.
 * @param unreadCount Number of unread notifications.
 * @param onDashboardClick Callback when Dashboard/Admin Hub is clicked.
 * @param onHomeClick Callback when Home is clicked.
 * @param onProfileClick Callback when Profile is clicked.
 * @param onWalletClick Callback when Wallet pill is clicked.
 * @param onNotificationsClick Callback when Notifications icon is clicked.
 * @param onMyApplicationsClick Callback to view applications.
 * @param onMessagesClick Callback to view messages/chat.
 * @param onLogoutClick Callback to initiate sign out.
 * @param onThemeChange Callback when a new theme is selected.
 * @param currentTheme The active theme enum.
 * @param windowSizeClass Adaptive layout information (Compact vs. Expanded).
 * @param onClassroomClick Callback to navigate to a specific classroom.
 * @param showThemeBuilder Whether the custom theme builder dialog is visible.
 * @param onOpenThemeBuilder Controls the visibility of the theme builder.
 * @param onLiveThemeUpdate Real-time theme updates during editing.
 * @param hideBars If true, top and bottom bars are completely hidden.
 * @param content The main screen content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopLevelScaffold(
    currentUser: FirebaseUser?,
    localUser: UserLocal?,
    userTheme: UserTheme?,
    currentRoute: String?,
    currentTutorSection: TutorSection?,
    currentAdminSection: AdminSection? = null,
    unreadCount: Int,
    onDashboardClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMyApplicationsClick: () -> Unit,
    onMyInvoicesClick: () -> Unit = {},
    onMessagesClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLogoutConfirm: () -> Unit = {},
    onSignedOutDismiss: () -> Unit = {},
    onThemeChange: (Theme) -> Unit = {},
    currentTheme: Theme = Theme.DARK,
    windowSizeClass: WindowSizeClass? = null,
    onClassroomClick: (String) -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onLiveSessionClick: () -> Unit = {},
    onNewAssignmentClick: () -> Unit = {},
    onLogsClick: () -> Unit = {},
    onBroadcastClick: () -> Unit = {},
    showThemeBuilder: Boolean = false,
    onOpenThemeBuilder: (Boolean) -> Unit = {},
    onLiveThemeUpdate: (UserTheme) -> Unit = {},
    onAboutClick: () -> Unit = {},
    hideBars: Boolean = false,
    bottomContent: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val userId = currentUser?.uid ?: ""
    // Use Navigation Rail for tablet/desktop (Expanded) widths
    val useNavRail = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    // Re-initialize state when the user changes
    key(userId) {
        var showMoreMenu by remember { mutableStateOf(false) }
        var showThemePicker by remember { mutableStateOf(false) }
        var showClassroomPicker by remember { mutableStateOf(false) }

        // Determine user permissions based on role
        val isAdmin = localUser?.role?.lowercase(Locale.ROOT) == "admin"
        val isTutor = localUser?.role?.lowercase(Locale.ROOT) in listOf("teacher", "tutor")
        
        // Check if the current theme is considered 'dark' for UI contrast adjustments
        val isDarkTheme = when(currentTheme) {
            Theme.DARK, Theme.DARK_BLUE -> true
            Theme.CUSTOM -> userTheme?.customIsDark ?: true
            else -> false
        }

        // Helper to update theme settings in the local database
        val onToggleRequest: (Boolean, Theme) -> Unit = { enabled, targetTheme ->
            scope.launch {
                val currentT = userTheme
                if (currentT != null) {
                    db.userThemeDao().upsertTheme(currentT.copy(isCustomThemeEnabled = enabled))
                } else if (userId.isNotEmpty()) {
                    db.userThemeDao().upsertTheme(UserTheme(userId = userId, isCustomThemeEnabled = enabled))
                }
                onThemeChange(targetTheme)
            }
        }

        // Fetch user-specific data like enrolled courses and applications
        val dashboardViewModel: DashboardViewModel = viewModel(
            key = "dashboard_vm_$userId",
            factory = DashboardViewModelFactory(
                context = context,
                repository = BookRepository(db),
                userDao = db.userDao(),
                classroomDao = db.classroomDao(),
                auditDao = db.auditDao(),
                userId = userId
            )
        )

        val applicationCount by dashboardViewModel.applicationCount.collectAsState()
        val filteredOwnedBooks by dashboardViewModel.filteredOwnedBooks.collectAsState()
        val purchasedIds by dashboardViewModel.purchasedIds.collectAsState()

        // Filter and categorize courses user has access to
        val enrolledPaidCourse = filteredOwnedBooks.find { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }
        val enrolledFreeCourses = filteredOwnedBooks.filter { it.mainCategory == AppConstants.CAT_COURSES && it.price <= 0.0 && purchasedIds.contains(it.id) }
        val enrolledCourses = listOfNotNull(enrolledPaidCourse) + enrolledFreeCourses

        val hasActiveEnrolledCourse = filteredOwnedBooks.any { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }
        val showApplications = applicationCount > 0 && !hasActiveEnrolledCourse
        val hasCourses = enrolledCourses.isNotEmpty()

        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val railColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // Left-side Navigation Rail (Expanded screens only)
            if (!hideBars && useNavRail && currentUser != null && currentRoute != null && currentRoute != AppConstants.ROUTE_SPLASH && currentRoute != AppConstants.ROUTE_AUTH) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Box(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                            UserAvatar(
                                photoUrl = localUser?.photoUrl ?: currentUser.photoUrl?.toString(),
                                modifier = Modifier.size(44.dp).clickable { onProfileClick() }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationRailItem(
                        selected = currentRoute == AppConstants.ROUTE_HOME || currentRoute?.startsWith("${AppConstants.ROUTE_HOME}?") == true, 
                        onClick = onHomeClick, 
                        icon = { Icon(Icons.Default.Home, null) }, 
                        label = { Text("Home") },
                        colors = railColors
                    )
                    NavigationRailItem(
                        selected = (currentRoute?.startsWith(AppConstants.ROUTE_DASHBOARD) == true) || (currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_PANEL) == true && currentAdminSection == AdminSection.DASHBOARD) || currentTutorSection == TutorSection.DASHBOARD, 
                        onClick = onDashboardClick, 
                        icon = { Icon(imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Dashboard, contentDescription = null) }, 
                        label = { Text(if (isAdmin) "Admin" else "Dashboard") },
                        colors = railColors
                    )

                    // Role-specific rail items
                    if (isTutor) {
                        NavigationRailItem(selected = currentTutorSection == TutorSection.MESSAGES, onClick = onMessagesClick, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) }, label = { Text("Messages") }, colors = railColors)
                        NavigationRailItem(selected = currentTutorSection == TutorSection.LIBRARY, onClick = onLibraryClick, icon = { Icon(Icons.Default.LibraryBooks, null) }, label = { Text("Library") }, colors = railColors)
                        NavigationRailItem(selected = currentTutorSection == TutorSection.COURSE_LIVE, onClick = onLiveSessionClick, icon = { Icon(Icons.Default.LiveTv, null) }, label = { Text("Stream") }, colors = railColors)
                        NavigationRailItem(selected = currentTutorSection == TutorSection.CREATE_ASSIGNMENT, onClick = onNewAssignmentClick, icon = { Icon(Icons.Default.NoteAdd, null) }, label = { Text("New Task") }, colors = railColors)
                    } else if (!isAdmin) {
                        if (hasCourses) {
                            Box {
                                NavigationRailItem(selected = currentRoute?.contains(AppConstants.ROUTE_CLASSROOM) == true, onClick = { showClassroomPicker = true }, icon = { Icon(Icons.Default.School, null) }, label = { Text("Classroom") }, colors = railColors)
                                // Classroom picker dropdown for student role
                                DropdownMenu(
                                    expanded = showClassroomPicker, 
                                    onDismissRequest = { showClassroomPicker = false }, 
                                    offset = androidx.compose.ui.unit.DpOffset(x = 80.dp, y = (-56).dp), 
                                    modifier = Modifier.width(280.dp).padding(vertical = 8.dp),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { 
                                        Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); 
                                        Spacer(Modifier.width(12.dp)); 
                                        Text("Select Classroom", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) 
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    enrolledCourses.forEach { course -> 
                                        DropdownMenuItem(
                                            text = { Text(course.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface) }, 
                                            onClick = { showClassroomPicker = false; onClassroomClick(course.id) }, 
                                            leadingIcon = { 
                                                Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) { 
                                                    Box(contentAlignment = Alignment.Center) { 
                                                        Icon(Icons.Default.AutoStories, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) 
                                                    } 
                                                } 
                                            }, 
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) 
                                    }
                                }
                            }
                        }
                        if (showApplications) { NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_MY_APPLICATIONS, onClick = onMyApplicationsClick, icon = { Icon(Icons.Default.Assignment, null) }, label = { Text("Apps") }, colors = railColors) }
                        NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_MESSAGES, onClick = onMessagesClick, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) }, label = { Text("Chat") }, colors = railColors)
                    } else {
                        // Admin-specific rail items
                        NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_MESSAGES, onClick = onMessagesClick, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) }, label = { Text("Chat") }, colors = railColors)
                        NavigationRailItem(selected = currentAdminSection == AdminSection.LIBRARY, onClick = onLibraryClick, icon = { Icon(Icons.Default.LibraryBooks, null) }, label = { Text("Library") }, colors = railColors)
                        NavigationRailItem(selected = currentAdminSection == AdminSection.LOGS, onClick = onLogsClick, icon = { Icon(Icons.Default.Security, null) }, label = { Text("Logs") }, colors = railColors)
                        NavigationRailItem(selected = currentAdminSection == AdminSection.BROADCAST, onClick = onBroadcastClick, icon = { Icon(Icons.Default.Campaign, null) }, label = { Text("Broadcast") }, colors = railColors)
                    }
                    Spacer(Modifier.weight(1f))
                    NavigationRailItem(selected = false, onClick = onLogoutClick, icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) }, label = { Text("Logout") }, colors = railColors)
                }
            }

            // Main Scaffold for standard Top/Bottom bars and content
            Scaffold(
                topBar = {
                    val isHome = currentRoute == AppConstants.ROUTE_HOME || currentRoute?.startsWith("${AppConstants.ROUTE_HOME}?") == true
                    
                    // Logic to hide/show TopBar depending on current route and user role
                    val shouldShowTopBar = if (currentUser == null || hideBars) {
                        false
                    } else if (isAdmin || isTutor) {
                        isHome // Hide top bar in Admin/Tutor hubs, show only on Home
                    } else {
                        val isHiddenRoute = currentRoute == AppConstants.ROUTE_SPLASH || 
                                            currentRoute == AppConstants.ROUTE_AUTH || 
                                            currentRoute?.startsWith(AppConstants.ROUTE_DASHBOARD) == true || 
                                            currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_PANEL) == true || 
                                            currentRoute?.startsWith(AppConstants.ROUTE_TUTOR_PANEL) == true || 
                                            currentRoute == AppConstants.ROUTE_PROFILE || 
                                            currentRoute == AppConstants.ROUTE_NOTIFICATIONS || 
                                            currentRoute == AppConstants.ROUTE_MESSAGES || 
                                            currentRoute?.startsWith(AppConstants.ROUTE_CLASSROOM) == true || 
                                            currentRoute == AppConstants.ROUTE_MY_APPLICATIONS
                        !isHiddenRoute
                    }

                    if (shouldShowTopBar) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                // User Greeting and Profile access
                                val firstName = localUser?.name?.split(" ")?.firstOrNull() ?: currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                                    if (isAdmin) onProfileClick() else onDashboardClick()
                                }.padding(4.dp)) {
                                    if (!useNavRail) { 
                                        UserAvatar(photoUrl = localUser?.photoUrl ?: currentUser?.photoUrl?.toString(), modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.width(12.dp)) 
                                    }
                                    @Suppress("DEPRECATION")
                                    Text(text = "Hi, $firstName", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }

                                // Right-side actions: Wallet, Notifications, and Overflow Menu
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isAdmin) {
                                        ProWalletPill(balance = localUser?.balance ?: 0.0, onClick = onWalletClick)
                                        Spacer(Modifier.width(8.dp))
                                    }

                                    ProNotificationIcon(count = unreadCount, isDarkTheme = isDarkTheme, onClick = onNotificationsClick)
                                    Spacer(Modifier.width(4.dp))

                                    if (!useNavRail) {
                                        Box {
                                            IconButton(onClick = { showMoreMenu = true }, modifier = Modifier.size(32.dp)) { 
                                                Icon(Icons.Default.MoreVert, "More", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp)) 
                                            }
                                            // Main App Options Menu
                                            DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.width(220.dp)) {
                                                ProMenuHeader("APP OPTIONS")
                                                if (isAdmin) {
                                                    DropdownMenuItem(text = { Text("Admin Hub") }, onClick = { showMoreMenu = false; onDashboardClick() }, leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) })
                                                } else {
                                                    if (showApplications) { DropdownMenuItem(text = { Text(AppConstants.TITLE_MY_APPLICATIONS) }, onClick = { showMoreMenu = false; onMyApplicationsClick() }, leadingIcon = { Icon(Icons.Default.Assignment, null) }) }
                                                    else if (hasCourses) { DropdownMenuItem(text = { Text(AppConstants.TITLE_CLASSROOM) }, onClick = { showMoreMenu = false; showClassroomPicker = true }, leadingIcon = { Icon(Icons.Default.School, null) }) }
                                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_MESSAGES) }, onClick = { showMoreMenu = false; onMessagesClick() }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Chat, null) })
                                                }
                                                DropdownMenuItem(text = { Text(AppConstants.TITLE_PROFILE_SETTINGS) }, onClick = { showMoreMenu = false; onProfileClick() }, leadingIcon = { Icon(Icons.Default.Settings, null) })
                                                DropdownMenuItem(text = { Text("Appearance") }, onClick = { showMoreMenu = false; showThemePicker = true }, leadingIcon = { Icon(Icons.Default.Palette, null) })
                                                DropdownMenuItem(text = { Text("About App") }, onClick = { showMoreMenu = false; onAboutClick() }, leadingIcon = { Icon(Icons.Default.Info, null) })
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                DropdownMenuItem(text = { Text("Sign Off", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }, onClick = { showMoreMenu = false; onLogoutClick() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) })
                                            }
                                            ThemeSelectionDropdown(expanded = showThemePicker, onDismissRequest = { showThemePicker = false }, onThemeChange = { theme -> onToggleRequest(theme == Theme.CUSTOM, theme) }, onOpenCustomBuilder = { onOpenThemeBuilder(true) }, isLoggedIn = currentUser != null, offset = DpOffset(x = (-150).dp, y = 0.dp))
                                        }
                                    } else {
                                        ThemeToggleButton(currentTheme = currentTheme, onThemeChange = { theme -> if (theme == Theme.CUSTOM) onOpenThemeBuilder(true) else onToggleRequest(false, theme) }, onOpenCustomBuilder = { onOpenThemeBuilder(true) }, isLoggedIn = currentUser != null)
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    // Standard Bottom Navigation for Mobile (Compact) widths
                    if (!hideBars && currentUser != null && !useNavRail && currentRoute != AppConstants.ROUTE_SPLASH) {
                        val isHome = currentRoute == AppConstants.ROUTE_HOME || currentRoute?.startsWith("${AppConstants.ROUTE_HOME}?") == true
                        val isAdminHub = currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_PANEL) == true || currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_USER_DETAILS) == true
                        val isTutorHub = currentRoute?.startsWith(AppConstants.ROUTE_TUTOR_PANEL) == true
                        
                        // Hide global bottom bar when viewing Admin/Tutor internal panels
                        if (!isAdminHub && !isTutorHub) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = isHome, 
                                    onClick = onHomeClick, 
                                    icon = { Icon(Icons.Default.Home, null) }, 
                                    label = { Text("Home") },
                                    colors = navColors
                                )
                                NavigationBarItem(
                                    selected = (currentRoute?.startsWith(AppConstants.ROUTE_DASHBOARD) == true) || (currentRoute?.startsWith(AppConstants.ROUTE_ADMIN_PANEL) == true && currentAdminSection == AdminSection.DASHBOARD) || currentTutorSection == TutorSection.DASHBOARD, 
                                    onClick = onDashboardClick, 
                                    icon = { Icon(imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Dashboard, contentDescription = null) }, 
                                    label = { Text(if (isAdmin) "Admin" else "Dashboard") },
                                    colors = navColors
                                )
                                NavigationBarItem(
                                    selected = currentRoute == AppConstants.ROUTE_NOTIFICATIONS || currentAdminSection == AdminSection.NOTIFICATIONS, 
                                    onClick = onNotificationsClick, 
                                    icon = { 
                                        BadgedBox(badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }) {
                                            Icon(Icons.Default.Notifications, null) 
                                        }
                                    }, 
                                    label = { Text("Alerts") },
                                    colors = navColors
                                )
                                NavigationBarItem(
                                    selected = currentRoute == AppConstants.ROUTE_PROFILE || currentTutorSection == TutorSection.TEACHER_DETAIL || currentAdminSection == AdminSection.PROFILE, 
                                    onClick = onProfileClick, 
                                    icon = { Icon(Icons.Default.Person, null) }, 
                                    label = { Text("Profile") },
                                    colors = navColors
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    // Custom Theme Builder Dialog - only accessible when logged in
                    if (showThemeBuilder && userId.isNotEmpty()) {
                        ThemeBuilderDialog(
                            show = showThemeBuilder,
                            onDismiss = { onOpenThemeBuilder(false) },
                            onSave = {
                                scope.launch {
                                    val finalTheme = userTheme ?: UserTheme(userId = userId)
                                    db.userThemeDao().upsertTheme(finalTheme.copy(isCustomThemeEnabled = true))
                                    onOpenThemeBuilder(false)
                                    onThemeChange(Theme.CUSTOM)
                                }
                            },
                            onReset = {
                                val resetTheme = UserTheme(userId = userId, isCustomThemeEnabled = true, customPrimary = 0xFF38BDF8, customOnPrimary = 0xFFFFFFFF, customPrimaryContainer = 0xFF0369A1, customOnPrimaryContainer = 0xFFE0F2FE, customSecondary = 0xFF0EA5E9, customOnSecondary = 0xFFFFFFFF, customSecondaryContainer = 0xFF0C4A6E, customOnSecondaryContainer = 0xFFE0F2FE, customTertiary = 0xFF818CF8, customOnTertiary = 0xFFFFFFFF, customTertiaryContainer = 0xFF312E81, customOnTertiaryContainer = 0xFFE0E7FF, customBackground = 0xFF020617, customOnBackground = 0xFFF8FAFC, customSurface = 0xFF0F172A, customOnSurface = 0xFFF8FAFC, customIsDark = true)
                                onLiveThemeUpdate(resetTheme)
                            },
                            isDark = userTheme?.customIsDark ?: true,
                            onIsDarkChange = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customIsDark = it)) },
                            primary = userTheme?.customPrimary ?: 0xFFBB86FC, onPrimary = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customPrimary = it)) },
                            onPrimaryVal = userTheme?.customOnPrimary ?: 0xFF000000, onOnPrimary = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customOnPrimary = it)) },
                            primaryContainer = userTheme?.customPrimaryContainer ?: 0xFF3700B3, onPrimaryContainer = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customPrimaryContainer = it)) },
                            onPrimaryContainerVal = userTheme?.customOnPrimaryContainer ?: 0xFFFFFFFF, onOnPrimaryContainer = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customOnPrimaryContainer = it)) },
                            secondary = userTheme?.customSecondary ?: 0xFF03DAC6, onSecondary = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customSecondary = it)) },
                            onSecondaryVal = userTheme?.customOnSecondary ?: 0xFF000000, onOnSecondary = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customOnSecondary = it)) },
                            secondaryContainer = userTheme?.customSecondaryContainer ?: 0xFF018786, onSecondaryContainer = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customSecondaryContainer = it)) },
                            onSecondaryContainerVal = userTheme?.customOnSecondaryContainer ?: 0xFFFFFFFF, onOnSecondaryContainer = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customOnSecondaryContainer = it)) },
                            tertiary = userTheme?.customTertiary ?: 0xFF7D5260, onTertiary = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customTertiary = it)) },
                            onTertiaryVal = userTheme?.customOnTertiary ?: 0xFFFFFFFF, onOnTertiary = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customOnTertiary = it)) },
                            tertiaryContainer = userTheme?.customTertiaryContainer ?: 0xFF1E293B, onTertiaryContainer = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customTertiaryContainer = it)) },
                            onTertiaryContainerVal = userTheme?.customOnTertiaryContainer ?: 0xFFFFFFFF, onOnTertiaryContainer = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customOnTertiaryContainer = it)) },
                            background = userTheme?.customBackground ?: 0xFF020617, onBackground = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customBackground = it)) },
                            onBackgroundVal = userTheme?.customOnBackground ?: 0xFFF1F5F9, onOnBackground = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customBackground = it)) },
                            surface = userTheme?.customSurface ?: 0xFF0F172A, onSurface = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customSurface = it)) },
                            onOnSurface = { onLiveThemeUpdate((userTheme ?: UserTheme(userId = userId)).copy(customOnSurface = it)) },
                            onSurfaceVal = userTheme?.customOnSurface ?: 0xFFF1F5F9
                        )
                    }
                }
            ) { paddingValues ->
                // Render the main content provided by the caller
                Box(modifier = Modifier.fillMaxSize()) {
                    content(paddingValues)
                    
                    // Optional overlay content (e.g., Snackbars, specific bottom-aligned components)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        bottomContent()
                    }
                }
            }
        }
    }
}
