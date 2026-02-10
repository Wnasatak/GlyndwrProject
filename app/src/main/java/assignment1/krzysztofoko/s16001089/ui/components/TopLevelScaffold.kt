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
import androidx.compose.material.icons.rounded.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * A shared Scaffold wrapper that provides the branded University TopBar.
 * Heavily optimized using centralized CommonComponents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopLevelScaffold(
    currentUser: FirebaseUser?,
    localUser: UserLocal?,
    userTheme: UserTheme?, 
    currentRoute: String?,
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
    showThemeBuilder: Boolean = false,
    onOpenThemeBuilder: (Boolean) -> Unit = {},
    onLiveThemeUpdate: (UserTheme) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val userId = currentUser?.uid ?: ""
    val useNavRail = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    key(userId) {
        var showMoreMenu by remember { mutableStateOf(false) }
        var showThemePicker by remember { mutableStateOf(false) }
        var showClassroomPicker by remember { mutableStateOf(false) }
        
        val isAdmin = localUser?.role?.lowercase(Locale.ROOT) == "admin"
        val isTutor = localUser?.role?.lowercase(Locale.ROOT) in listOf("teacher", "tutor")
        val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM

        // Helper to update the live preview state
        val updatePreview = { updated: UserTheme ->
            onLiveThemeUpdate(updated)
        }

        // Helper to update theme in database
        val updateThemeEnabled = { enabled: Boolean, targetTheme: Theme ->
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

        val dashboardViewModel: DashboardViewModel = viewModel(
            key = "dashboard_vm_$userId",
            factory = DashboardViewModelFactory(
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

        val enrolledPaidCourse = filteredOwnedBooks.find { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }
        val enrolledFreeCourses = filteredOwnedBooks.filter { it.mainCategory == AppConstants.CAT_COURSES && it.price <= 0.0 && purchasedIds.contains(it.id) }
        val enrolledCourses = listOfNotNull(enrolledPaidCourse) + enrolledFreeCourses

        val hasActiveEnrolledCourse = filteredOwnedBooks.any { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }
        val showApplications = applicationCount > 0 && !hasActiveEnrolledCourse
        val hasCourses = enrolledCourses.isNotEmpty()

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavRail && currentUser != null && currentRoute != null && currentRoute != AppConstants.ROUTE_SPLASH && currentRoute != AppConstants.ROUTE_AUTH) {
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
                    NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_HOME, onClick = onHomeClick, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                    NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_DASHBOARD || currentRoute == AppConstants.ROUTE_ADMIN_PANEL || currentRoute?.startsWith(AppConstants.ROUTE_TUTOR_PANEL) == true, onClick = onDashboardClick, icon = { Icon(imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Dashboard, contentDescription = null) }, label = { Text(if (isAdmin) "Admin" else "Dashboard") })

                    if (isTutor) {
                        NavigationRailItem(selected = currentRoute?.contains("section=MESSAGES") == true, onClick = onMessagesClick, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) }, label = { Text("Messages") })
                        NavigationRailItem(selected = currentRoute?.contains("section=LIBRARY") == true, onClick = onLibraryClick, icon = { Icon(Icons.Default.LibraryBooks, null) }, label = { Text("Library") })
                        NavigationRailItem(selected = currentRoute?.contains("section=COURSE_LIVE") == true, onClick = onLiveSessionClick, icon = { Icon(Icons.Default.LiveTv, null) }, label = { Text("Stream") })
                        NavigationRailItem(selected = currentRoute?.contains("section=CREATE_ASSIGNMENT") == true, onClick = onNewAssignmentClick, icon = { Icon(Icons.Default.NoteAdd, null) }, label = { Text("New Task") })
                    } else if (!isAdmin) {
                        if (hasCourses) {
                            Box {
                                NavigationRailItem(selected = currentRoute?.contains(AppConstants.ROUTE_CLASSROOM) == true, onClick = { showClassroomPicker = true }, icon = { Icon(Icons.Default.School, null) }, label = { Text("Classroom") })
                                DropdownMenu(expanded = showClassroomPicker, onDismissRequest = { showClassroomPicker = false }, offset = androidx.compose.ui.unit.DpOffset(x = 80.dp, y = 0.dp), modifier = Modifier.width(280.dp).padding(vertical = 8.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text("Select Classroom", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    enrolledCourses.forEach { course -> DropdownMenuItem(text = { Text(course.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }, onClick = { showClassroomPicker = false; onClassroomClick(course.id) }, leadingIcon = { Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoStories, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) } } }, modifier = Modifier.padding(vertical = 4.dp)) }
                                }
                            }
                        }
                        if (showApplications) { NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_MY_APPLICATIONS, onClick = onMyApplicationsClick, icon = { Icon(Icons.Default.Assignment, null) }, label = { Text("Apps") }) }
                        NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_MESSAGES, onClick = onMessagesClick, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) }, label = { Text("Chat") })
                    } else {
                        NavigationRailItem(selected = currentRoute == AppConstants.ROUTE_MESSAGES, onClick = onMessagesClick, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) }, label = { Text("Chat") })
                    }
                    Spacer(Modifier.weight(1f))
                    NavigationRailItem(selected = false, onClick = onLogoutClick, icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) }, label = { Text("Logout") })
                }
            }

            Scaffold(
                topBar = {
                    if (currentUser != null && currentRoute != null && !isAdmin && currentRoute != AppConstants.ROUTE_SPLASH && currentRoute != AppConstants.ROUTE_AUTH && currentRoute != AppConstants.ROUTE_DASHBOARD && currentRoute != AppConstants.ROUTE_ADMIN_PANEL && !currentRoute.startsWith(AppConstants.ROUTE_TUTOR_PANEL) && currentRoute != AppConstants.ROUTE_PROFILE && currentRoute != AppConstants.ROUTE_NOTIFICATIONS && currentRoute != AppConstants.ROUTE_ABOUT && currentRoute != AppConstants.ROUTE_DEVELOPER && currentRoute != AppConstants.ROUTE_INSTRUCTIONS && currentRoute != AppConstants.ROUTE_VERSION_INFO && currentRoute != AppConstants.ROUTE_FUTURE_FEATURES && !currentRoute.contains(AppConstants.ROUTE_PDF_READER) && !currentRoute.contains(AppConstants.ROUTE_INVOICE) && !currentRoute.contains(AppConstants.ROUTE_INVOICE_CREATING)) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                val firstName = localUser?.name?.split(" ")?.firstOrNull() ?: currentUser.displayName?.split(" ")?.firstOrNull() ?: "User"
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onDashboardClick() }.padding(4.dp)) {
                                    if (!useNavRail) { UserAvatar(photoUrl = localUser?.photoUrl ?: currentUser.photoUrl?.toString(), modifier = Modifier.size(36.dp)); Spacer(Modifier.width(12.dp)) }
                                    Text(text = "Hi, $firstName", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isAdmin) {
                                        // Centralized Wallet Action
                                        ProWalletPill(balance = localUser?.balance ?: 0.0, onClick = onWalletClick)
                                        Spacer(Modifier.width(8.dp))
                                    }

                                    // Centralized Notification Action
                                    ProNotificationIcon(count = unreadCount, isDarkTheme = isDarkTheme, onClick = onNotificationsClick)

                                    Spacer(Modifier.width(4.dp))
                                    
                                    if (!useNavRail) {
                                        Box {
                                            IconButton(onClick = { showMoreMenu = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.MoreVert, "More", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp)) }
                                            DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.width(220.dp)) {
                                                ProMenuHeader("APP OPTIONS")
                                                if (isAdmin) {
                                                    DropdownMenuItem(text = { Text("Admin Hub") }, onClick = { showMoreMenu = false; onDashboardClick() }, leadingIcon = { Icon(Icons.Rounded.AdminPanelSettings, null) })
                                                } else {
                                                    if (showApplications) { DropdownMenuItem(text = { Text(AppConstants.TITLE_MY_APPLICATIONS) }, onClick = { showMoreMenu = false; onMyApplicationsClick() }, leadingIcon = { Icon(Icons.Rounded.Assignment, null) }) }
                                                    else if (hasCourses) { DropdownMenuItem(text = { Text(AppConstants.TITLE_CLASSROOM) }, onClick = { showMoreMenu = false; showClassroomPicker = true }, leadingIcon = { Icon(Icons.Rounded.School, null) }) }
                                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_MESSAGES) }, onClick = { showMoreMenu = false; onMessagesClick() }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Chat, null) })
                                                }
                                                DropdownMenuItem(text = { Text(AppConstants.TITLE_PROFILE_SETTINGS) }, onClick = { showMoreMenu = false; onProfileClick() }, leadingIcon = { Icon(Icons.Rounded.Settings, null) })
                                                DropdownMenuItem(text = { Text("Appearance") }, onClick = { showMoreMenu = false; showThemePicker = true }, leadingIcon = { Icon(Icons.Rounded.Palette, null) })
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                DropdownMenuItem(text = { Text("Sign Off", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }, onClick = { showMoreMenu = false; onLogoutClick() }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) })
                                            }
                                            ThemeSelectionDropdown(expanded = showThemePicker, onDismissRequest = { showThemePicker = false }, onThemeChange = { theme -> updateThemeEnabled(theme == Theme.CUSTOM, theme) }, onOpenCustomBuilder = { onOpenThemeBuilder(true) }, isLoggedIn = currentUser != null, offset = DpOffset(x = (-150).dp, y = 0.dp))
                                        }
                                    } else {
                                        ThemeToggleButton(currentTheme = currentTheme, onThemeChange = { theme -> if (theme == Theme.CUSTOM) onOpenThemeBuilder(true) else updateThemeEnabled(false, theme) }, onOpenCustomBuilder = { onOpenThemeBuilder(true) }, isLoggedIn = currentUser != null)
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                content = content
            )
        }

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
                updatePreview(resetTheme)
            },
            isDark = userTheme?.customIsDark ?: true,
            onIsDarkChange = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customIsDark = it)) },
            primary = userTheme?.customPrimary ?: 0xFFBB86FC, onPrimary = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customPrimary = it)) },
            onPrimaryVal = userTheme?.customOnPrimary ?: 0xFF000000, onOnPrimary = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnPrimary = it)) },
            primaryContainer = userTheme?.customPrimaryContainer ?: 0xFF3700B3, onPrimaryContainer = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customPrimaryContainer = it)) },
            onPrimaryContainerVal = userTheme?.customOnPrimaryContainer ?: 0xFFFFFFFF, onOnPrimaryContainer = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnPrimaryContainer = it)) },
            secondary = userTheme?.customSecondary ?: 0xFF03DAC6, onSecondary = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customSecondary = it)) },
            onSecondaryVal = userTheme?.customOnSecondary ?: 0xFF000000, onOnSecondary = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnSecondary = it)) },
            secondaryContainer = userTheme?.customSecondaryContainer ?: 0xFF018786, onSecondaryContainer = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customSecondaryContainer = it)) },
            onSecondaryContainerVal = userTheme?.customOnSecondaryContainer ?: 0xFFFFFFFF, onOnSecondaryContainer = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnSecondaryContainer = it)) },
            tertiary = userTheme?.customTertiary ?: 0xFF7D5260, onTertiary = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customTertiary = it)) },
            onTertiaryVal = userTheme?.customOnTertiary ?: 0xFFFFFFFF, onOnTertiary = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnTertiary = it)) },
            tertiaryContainer = userTheme?.customTertiaryContainer ?: 0xFF1E293B, onTertiaryContainer = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customTertiaryContainer = it)) },
            onTertiaryContainerVal = userTheme?.customOnTertiaryContainer ?: 0xFFFFFFFF, onOnTertiaryContainer = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnTertiaryContainer = it)) },
            background = userTheme?.customBackground ?: 0xFF020617, onBackground = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customBackground = it)) },
            onBackgroundVal = userTheme?.customOnBackground ?: 0xFFF1F5F9, onOnBackground = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnBackground = it)) },
            surface = userTheme?.customSurface ?: 0xFF0F172A, onSurface = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customSurface = it)) },
            onOnSurface = { updatePreview((userTheme ?: UserTheme(userId = userId)).copy(customOnSurface = it)) },
            onSurfaceVal = userTheme?.customOnSurface ?: 0xFFF1F5F9
        )

        if (showClassroomPicker && !useNavRail) {
            ModalBottomSheet(onDismissRequest = { showClassroomPicker = false }, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(16.dp)); Text(text = "Your Classrooms", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.height(24.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { items(enrolledCourses) { course -> Surface(onClick = { showClassroomPicker = false; onClassroomClick(course.id) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) { Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoStories, null, tint = Color.White) } } ; Spacer(Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(course.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(course.author, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }; Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) } } } }
                }
            }
        }
    }
}

private fun String.capitalizeWord(): String = this.lowercase().replaceFirstChar { it.uppercase() }
