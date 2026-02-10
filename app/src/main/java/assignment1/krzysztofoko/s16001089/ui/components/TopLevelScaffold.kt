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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.ui.theme.*
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardViewModel
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardViewModelFactory
import assignment1.krzysztofoko.s16001089.data.BookRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

/**
 * A shared Scaffold wrapper that provides the branded University TopBar
 * and consistent layout padding for all main screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopLevelScaffold(
    currentUser: FirebaseUser?,
    localUser: UserLocal?,
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
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    windowSizeClass: WindowSizeClass? = null,
    onClassroomClick: (String) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val userId = currentUser?.uid ?: ""
    val useNavRail = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    // Force a complete internal state reset when user identity changes
    key(userId) {
        var showMoreMenu by remember { mutableStateOf(false) }
        var showClassroomPicker by remember { mutableStateOf(false) }
        
        val isAdmin = localUser?.role?.lowercase(Locale.ROOT) == "admin"
        val isTutor = localUser?.role?.lowercase(Locale.ROOT) in listOf("teacher", "tutor")
        
        val context = LocalContext.current
        val db = AppDatabase.getDatabase(context)

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
        
        val enrolledCourses = remember(enrolledPaidCourse, enrolledFreeCourses) {
            listOfNotNull(enrolledPaidCourse) + enrolledFreeCourses
        }

        val hasActiveEnrolledCourse = filteredOwnedBooks.any { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }
        val showApplications = applicationCount > 0 && !hasActiveEnrolledCourse
        val hasCourses = enrolledCourses.isNotEmpty()

        val infiniteTransition = rememberInfiniteTransition(label = "bellRing")
        val rotation by infiniteTransition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(250, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation"
        )

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
                    NavigationRailItem(
                        selected = currentRoute == AppConstants.ROUTE_HOME,
                        onClick = onHomeClick,
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") }
                    )
                    NavigationRailItem(
                        selected = currentRoute == AppConstants.ROUTE_DASHBOARD || currentRoute == AppConstants.ROUTE_ADMIN_PANEL || currentRoute == AppConstants.ROUTE_TUTOR_PANEL,
                        onClick = onDashboardClick,
                        icon = { 
                            Icon(
                                imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Dashboard, 
                                contentDescription = null 
                            ) 
                        },
                        label = { Text(if (isAdmin) "Admin" else "Dashboard") }
                    )

                    // Classroom item - Only for students
                    if (hasCourses && !isAdmin && !isTutor) {
                        Box {
                            NavigationRailItem(
                                selected = currentRoute?.contains(AppConstants.ROUTE_CLASSROOM) == true,
                                onClick = { showClassroomPicker = true },
                                icon = { Icon(Icons.Default.School, null) },
                                label = { Text("Classroom") }
                            )
                            DropdownMenu(
                                expanded = showClassroomPicker,
                                onDismissRequest = { showClassroomPicker = false },
                                // Offset adjusted to open inline with the Classroom button
                                offset = androidx.compose.ui.unit.DpOffset(x = 80.dp, y = 0.dp),
                                modifier = Modifier.width(280.dp).padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    @Suppress("DEPRECATION")
                                    Text("Select Classroom", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                enrolledCourses.forEach { course ->
                                    DropdownMenuItem(
                                        text = { Text(course.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            showClassroomPicker = false
                                            onClassroomClick(course.id)
                                        },
                                        leadingIcon = { 
                                            Surface(
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            ) {
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

                    if (showApplications && !isAdmin && !isTutor) {
                        NavigationRailItem(
                            selected = currentRoute == AppConstants.ROUTE_MY_APPLICATIONS,
                            onClick = onMyApplicationsClick,
                            icon = { Icon(Icons.Default.Assignment, null) },
                            label = { Text("Apps") }
                        )
                    }
                    
                    NavigationRailItem(
                        selected = currentRoute == AppConstants.ROUTE_MESSAGES,
                        onClick = onMessagesClick,
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) },
                        label = { Text("Chat") }
                    )
                    
                    Spacer(Modifier.weight(1f))

                    NavigationRailItem(
                        selected = false,
                        onClick = onToggleTheme,
                        icon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) },
                        label = { Text("Theme") }
                    )
                    NavigationRailItem(
                        selected = false,
                        onClick = onLogoutClick,
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) },
                        label = { Text("Logout") }
                    )
                }
            }

            Scaffold(
                topBar = {
                    if (currentUser != null && 
                        currentRoute != null && 
                        currentRoute != AppConstants.ROUTE_SPLASH && 
                        currentRoute != AppConstants.ROUTE_AUTH &&
                        currentRoute != AppConstants.ROUTE_DASHBOARD && 
                        currentRoute != AppConstants.ROUTE_ADMIN_PANEL && 
                        currentRoute != AppConstants.ROUTE_TUTOR_PANEL && 
                        currentRoute != AppConstants.ROUTE_PROFILE && 
                        currentRoute != AppConstants.ROUTE_NOTIFICATIONS &&
                        currentRoute != AppConstants.ROUTE_ABOUT &&
                        currentRoute != AppConstants.ROUTE_DEVELOPER &&
                        currentRoute != AppConstants.ROUTE_INSTRUCTIONS &&
                        currentRoute != AppConstants.ROUTE_VERSION_INFO &&
                        currentRoute != AppConstants.ROUTE_FUTURE_FEATURES &&
                        !currentRoute.contains(AppConstants.ROUTE_PDF_READER) && 
                        !currentRoute.contains(AppConstants.ROUTE_INVOICE) && 
                        !currentRoute.contains(AppConstants.ROUTE_INVOICE_CREATING)) {
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val firstName = localUser?.name?.split(" ")?.firstOrNull() ?: currentUser.displayName?.split(" ")?.firstOrNull() ?: "User"
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onDashboardClick() }
                                        .padding(4.dp)
                                ) {
                                    if (!useNavRail) {
                                        UserAvatar(
                                            photoUrl = localUser?.photoUrl ?: currentUser.photoUrl?.toString(),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                    }
                                    Text(
                                        text = "Hi, $firstName",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isAdmin) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.clickable { onWalletClick() }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                                Spacer(Modifier.width(6.6.dp))
                                                val balance = localUser?.balance ?: 0.0
                                                Text(text = "£${String.format(Locale.US, "%.2f", balance)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                    }

                                    Box(contentAlignment = Alignment.TopEnd) {
                                        val bellColor = if (unreadCount > 0 && isDarkTheme) Color(0xFFFFEB3B) 
                                                        else if (unreadCount > 0) Color(0xFFFBC02D)
                                                        else MaterialTheme.colorScheme.onPrimaryContainer
                                        
                                        IconButton(onClick = onNotificationsClick, modifier = Modifier.size(36.dp)) {
                                            Icon(
                                                imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                                contentDescription = "Notifications",
                                                tint = bellColor,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .graphicsLayer { if (unreadCount > 0) { rotationZ = rotation } }
                                            )
                                        }
                                        if (unreadCount > 0) {
                                            Surface(
                                                color = Color(0xFFE53935),
                                                shape = CircleShape,
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primaryContainer),
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .offset(x = 4.dp, y = (-4).dp)
                                                    .align(Alignment.TopEnd)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (unreadCount > 9) "!" else unreadCount.toString(),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black, lineHeight = 9.sp),
                                                        color = Color.White,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.width(4.dp))
                                    
                                    if (!useNavRail) {
                                        Box {
                                            IconButton(onClick = { showMoreMenu = true }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.MoreVert, "More", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                                            }
                                            DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                                if (isAdmin) {
                                                    DropdownMenuItem(text = { Text("Admin Hub") }, onClick = { showMoreMenu = false; onDashboardClick() }, leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) })
                                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                } else {
                                                    if (showApplications) {
                                                        DropdownMenuItem(text = { Text(AppConstants.TITLE_MY_APPLICATIONS) }, onClick = { showMoreMenu = false; onMyApplicationsClick() }, leadingIcon = { Icon(Icons.Default.Assignment, null) })
                                                    } else if (hasCourses) {
                                                        DropdownMenuItem(
                                                            text = { Text(AppConstants.TITLE_CLASSROOM) }, 
                                                            onClick = { 
                                                                showMoreMenu = false
                                                                showClassroomPicker = true 
                                                            }, 
                                                            leadingIcon = { Icon(Icons.Default.School, null) }
                                                        )
                                                    }
                                                    DropdownMenuItem(text = { Text(AppConstants.TITLE_MESSAGES) }, onClick = { showMoreMenu = false; onMessagesClick() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, null) })
                                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                }

                                                DropdownMenuItem(text = { Text(AppConstants.TITLE_PROFILE_SETTINGS) }, onClick = { showMoreMenu = false; onProfileClick() }, leadingIcon = { Icon(Icons.Default.Settings, null) })
                                                DropdownMenuItem(text = { Text(if (isDarkTheme) "Light Mode" else "Dark Mode") }, onClick = { showMoreMenu = false; onToggleTheme() }, leadingIcon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) })
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                DropdownMenuItem(text = { Text(AppConstants.BTN_LOG_OUT, color = MaterialTheme.colorScheme.error) }, onClick = { showMoreMenu = false; onLogoutClick() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) })
                                            }
                                        }
                                    } else {
                                        IconButton(onClick = onProfileClick, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                                        }
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

        // Modal Bottom Sheet for Course Selection on Phones
        if (showClassroomPicker && !useNavRail) {
            ModalBottomSheet(
                onDismissRequest = { showClassroomPicker = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Your Classrooms",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(enrolledCourses) { course ->
                            Surface(
                                onClick = {
                                    showClassroomPicker = false
                                    onClassroomClick(course.id)
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.AutoStories, null, tint = Color.White)
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(course.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(course.author, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.capitalizeWord(): String = this.lowercase().replaceFirstChar { it.uppercase() }
