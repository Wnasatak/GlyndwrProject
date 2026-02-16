package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.AudioBook
import assignment1.krzysztofoko.s16001089.data.toBook
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.details.pdf.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.TutorCourseDetailScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Assignments.TutorCourseAssignmentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Modules.TutorCourseModulesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Students.TutorClassStudentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Students.TutorCourseAttendanceTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Grades.TutorCourseGradesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Live.TutorCourseLiveTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Live.ArchivedBroadcastsScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorDashboardTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.TutorCoursesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Students.TutorStudentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.TutorMessagesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.TutorChatTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Library.TutorLibraryTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Catalog.TutorBooksTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Catalog.TutorAudioBooksTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorDetailScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.CreateAssignmentScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Students.TutorStudentProfileScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Students.TutorStudentAttendanceDetailScreen
import assignment1.krzysztofoko.s16001089.ui.notifications.NotificationScreen
import assignment1.krzysztofoko.s16001089.ui.info.AboutScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth

/**
 * TutorPanelScreen is the primary layout orchestrator for the institutional tutoring portal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorPanelScreen(
    onBack: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToInstruction: () -> Unit,
    onOpenThemeBuilder: () -> Unit,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onPlayAudio: (Book) -> Unit,
    externalPlayer: Player? = null,
    onStopPlayer: () -> Unit = {},
    currentPlayingBookId: String? = null,
    isAudioPlaying: Boolean = false,
    initialSection: String? = null,
    viewModel: TutorViewModel = viewModel(factory = TutorViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        tutorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val currentSectionState by viewModel.currentSection.collectAsState()
    
    // PREVENT FLICKER: Resolve the section immediately during the first render
    val resolvedSection = remember(currentSectionState, initialSection) {
        if (initialSection != null && currentSectionState == TutorSection.DASHBOARD) {
            try {
                val target = TutorSection.valueOf(initialSection)
                if (currentSectionState != target) {
                    viewModel.setSection(target)
                }
                target
            } catch (e: Exception) { currentSectionState }
        } else {
            currentSectionState
        }
    }

    val activeBook by viewModel.activeBook.collectAsState()
    val activeAudioBookState = viewModel.activeAudioBook.collectAsState()
    val activeAudioBook by activeAudioBookState
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    val isChatOpen = resolvedSection == TutorSection.CHAT
    val isReaderOpen = resolvedSection == TutorSection.READ_BOOK
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM
    var showMenu by remember { mutableStateOf(false) }

    val infiniteTransitionBell = rememberInfiniteTransition(label = "bellRing")
    val rotation by infiniteTransitionBell.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isReaderOpen && resolvedSection != TutorSection.ABOUT && resolvedSection != TutorSection.NOTIFICATIONS) {
            HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!isReaderOpen && resolvedSection != TutorSection.LISTEN_AUDIOBOOK && resolvedSection != TutorSection.ABOUT && resolvedSection != TutorSection.NOTIFICATIONS) {
                    TopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { 
                            if (isChatOpen && selectedStudent != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(photoUrl = selectedStudent?.photoUrl, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = selectedStudent?.name ?: "User", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Spacer(Modifier.width(8.dp))
                                            val roleText = (selectedStudent?.role ?: "user").uppercase()
                                            Surface(color = if (roleText == "ADMIN" || roleText == "TUTOR") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                                                Text(text = roleText, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = if (roleText == "ADMIN" || roleText == "TUTOR") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(text = "Online", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    AdaptiveBrandedLogo(model = "file:///android_asset/images/media/GlyndwrUniversity.jpg", contentDescription = "University Logo", logoSize = 32.dp)
                                    val sectionTitle = when(resolvedSection) {
                                        TutorSection.LIBRARY -> "Library"
                                        TutorSection.BOOKS -> "Explore Books"
                                        TutorSection.AUDIOBOOKS -> "Explore Audiobooks"
                                        TutorSection.MESSAGES -> "Messages"
                                        TutorSection.STUDENTS -> "Student Directory"
                                        TutorSection.MY_COURSES -> "My Classes"
                                        TutorSection.SELECTED_COURSE -> "Course Management"
                                        TutorSection.COURSE_STUDENTS -> "Class Students"
                                        TutorSection.COURSE_MODULES -> "Class Modules"
                                        TutorSection.COURSE_ASSIGNMENTS -> "Assignments"
                                        TutorSection.COURSE_GRADES -> "Grades"
                                        TutorSection.COURSE_LIVE -> "Live Stream"
                                        TutorSection.COURSE_ARCHIVED_BROADCASTS -> "Session Archive"
                                        TutorSection.COURSE_ATTENDANCE -> "Attendance"
                                        TutorSection.INDIVIDUAL_ATTENDANCE_DETAIL -> "Attendance Detail"
                                        TutorSection.DASHBOARD -> "Teacher Dashboard"
                                        TutorSection.TEACHER_DETAIL -> "Teacher Profile"
                                        TutorSection.CREATE_ASSIGNMENT -> "Create Assignment"
                                        TutorSection.START_LIVE_STREAM -> "Start Live Stream"
                                        TutorSection.STUDENT_PROFILE -> "Student Profile"
                                        else -> ""
                                    }
                                    if (sectionTitle.isNotEmpty()) {
                                        Text(text = " • $sectionTitle", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            if (isChatOpen) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.MESSAGES) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Messages") }
                            } else if (resolvedSection == TutorSection.COURSE_ARCHIVED_BROADCASTS) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.COURSE_LIVE) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                            } else if (resolvedSection in listOf(TutorSection.COURSE_STUDENTS, TutorSection.COURSE_MODULES, TutorSection.COURSE_ASSIGNMENTS, TutorSection.COURSE_GRADES, TutorSection.COURSE_LIVE, TutorSection.COURSE_ATTENDANCE)) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.SELECTED_COURSE) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Course Detail") }
                            } else if (resolvedSection == TutorSection.SELECTED_COURSE) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.MY_COURSES) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to My Classes") }
                            } else if (resolvedSection == TutorSection.TEACHER_DETAIL || resolvedSection == TutorSection.CREATE_ASSIGNMENT || resolvedSection == TutorSection.START_LIVE_STREAM) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.DASHBOARD) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Dashboard") }
                            } else if (resolvedSection == TutorSection.STUDENT_PROFILE) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.STUDENTS) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Student Directory") }
                            } else if (resolvedSection == TutorSection.INDIVIDUAL_ATTENDANCE_DETAIL) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.STUDENT_PROFILE) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Profile") }
                            } else if (resolvedSection == TutorSection.READ_BOOK) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.LIBRARY) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Library") }
                            } else if (resolvedSection !in listOf(TutorSection.DASHBOARD, TutorSection.MY_COURSES, TutorSection.LIBRARY, TutorSection.STUDENTS, TutorSection.MESSAGES)) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.DASHBOARD) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                            }
                        },
                        actions = {
                            if (!isChatOpen) {
                                if (currentPlayingBookId != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { if (isAudioPlaying) externalPlayer?.pause() else externalPlayer?.play() }) {
                                            Icon(imageVector = if (isAudioPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled, contentDescription = "Toggle Audio", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = onStopPlayer) { Icon(Icons.Default.Close, "Stop", modifier = Modifier.size(20.dp)) }
                                        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
                                    }
                                }
                                Box(contentAlignment = Alignment.TopEnd) {
                                    val bellColor = if (unreadCount > 0 && isDarkTheme) Color(0xFFFFEB3B) else if (unreadCount > 0) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurface
                                    IconButton(onClick = { viewModel.setSection(TutorSection.NOTIFICATIONS) }, modifier = Modifier.size(36.dp)) {
                                        Icon(imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications, contentDescription = AppConstants.TITLE_NOTIFICATIONS, tint = bellColor, modifier = Modifier.size(24.dp).graphicsLayer { if (unreadCount > 0) rotationZ = rotation })
                                    }
                                    if (unreadCount > 0) {
                                        Surface(color = Color(0xFFE53935), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface), modifier = Modifier.size(18.dp).offset(x = 4.dp, y = (-2).dp).align(Alignment.TopEnd)) {
                                            Box(contentAlignment = Alignment.Center) { Text(text = if (unreadCount > 9) "!" else unreadCount.toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black, lineHeight = 9.sp), color = Color.White, textAlign = TextAlign.Center) }
                                        }
                                    }
                                }
                                ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, onOpenCustomBuilder = onOpenThemeBuilder, isLoggedIn = true)
                                Box {
                                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, AppConstants.TITLE_MORE_OPTIONS) }
                                    DropdownMenu(
                                        expanded = showMenu, 
                                        onDismissRequest = { showMenu = false },
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.width(220.dp),
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ) {
                                        ProMenuHeader("TEACHER HUB")
                                        DropdownMenuItem(
                                            text = { Text("Teacher Profile") }, 
                                            onClick = { showMenu = false; viewModel.setSection(TutorSection.TEACHER_DETAIL) }, 
                                            leadingIcon = { Icon(Icons.Rounded.AccountBox, null, tint = MaterialTheme.colorScheme.primary) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(AppConstants.TITLE_PROFILE_SETTINGS) }, 
                                            onClick = { showMenu = false; onNavigateToProfile() }, 
                                            leadingIcon = { Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.primary) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("App Info") }, 
                                            onClick = { showMenu = false; viewModel.setSection(TutorSection.ABOUT) }, 
                                            leadingIcon = { Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary) }
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        DropdownMenuItem(
                                            text = { Text("Sign Off", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }, 
                                            onClick = { showMenu = false; onLogout() }, 
                                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) }
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                }
            },
            bottomBar = {
                val hideBottomBar = isChatOpen || resolvedSection == TutorSection.BOOKS || resolvedSection == TutorSection.AUDIOBOOKS || resolvedSection == TutorSection.READ_BOOK || resolvedSection == TutorSection.LISTEN_AUDIOBOOK || resolvedSection == TutorSection.SELECTED_COURSE || resolvedSection == TutorSection.COURSE_STUDENTS || resolvedSection == TutorSection.COURSE_MODULES || resolvedSection == TutorSection.COURSE_ASSIGNMENTS || resolvedSection == TutorSection.COURSE_GRADES || resolvedSection == TutorSection.COURSE_LIVE || resolvedSection == TutorSection.COURSE_ARCHIVED_BROADCASTS || resolvedSection == TutorSection.TEACHER_DETAIL || resolvedSection == TutorSection.CREATE_ASSIGNMENT || resolvedSection == TutorSection.START_LIVE_STREAM || resolvedSection == TutorSection.STUDENT_PROFILE || resolvedSection == TutorSection.NOTIFICATIONS || resolvedSection == TutorSection.ABOUT || resolvedSection == TutorSection.COURSE_ATTENDANCE || resolvedSection == TutorSection.INDIVIDUAL_ATTENDANCE_DETAIL
                if (!hideBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0), 
                        modifier = Modifier.height(80.dp) 
                    ) {
                        TutorNavButton(selected = resolvedSection == TutorSection.DASHBOARD, onClick = { viewModel.setSection(TutorSection.DASHBOARD) }, icon = Icons.Default.Dashboard, label = "Home")
                        TutorNavButton(selected = resolvedSection == TutorSection.MY_COURSES, onClick = { viewModel.setSection(TutorSection.MY_COURSES) }, icon = Icons.Default.School, label = "Classes")
                        TutorNavButton(selected = resolvedSection == TutorSection.LIBRARY, onClick = { viewModel.setSection(TutorSection.LIBRARY) }, icon = Icons.AutoMirrored.Filled.LibraryBooks, label = "Library")
                        TutorNavButton(selected = resolvedSection == TutorSection.STUDENTS, onClick = { viewModel.setSection(TutorSection.STUDENTS) }, icon = Icons.Default.People, label = "Students")
                        TutorNavButton(selected = resolvedSection == TutorSection.MESSAGES, onClick = { viewModel.setSection(TutorSection.MESSAGES) }, icon = Icons.AutoMirrored.Filled.Chat, label = "Messages")
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(top = if (isReaderOpen) 0.dp else padding.calculateTopPadding())) {
                Box(modifier = Modifier.weight(1f).padding(bottom = if (isChatOpen || resolvedSection == TutorSection.ABOUT || resolvedSection == TutorSection.NOTIFICATIONS || resolvedSection == TutorSection.COURSE_ATTENDANCE || resolvedSection == TutorSection.INDIVIDUAL_ATTENDANCE_DETAIL) 0.dp else padding.calculateBottomPadding())) {
                    AnimatedContent(targetState = resolvedSection, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "TutorSectionTransition") { section ->
                        when (section) {
                            TutorSection.DASHBOARD -> TutorDashboardTab(viewModel, isDarkTheme, onPlayAudio)
                            TutorSection.MY_COURSES -> TutorCoursesTab(viewModel)
                            TutorSection.SELECTED_COURSE -> TutorCourseDetailScreen(viewModel)
                            TutorSection.COURSE_STUDENTS -> TutorClassStudentsTab(viewModel)
                            TutorSection.COURSE_MODULES -> TutorCourseModulesTab(viewModel)
                            TutorSection.COURSE_ASSIGNMENTS -> TutorCourseAssignmentsTab(viewModel)
                            TutorSection.COURSE_GRADES -> TutorCourseGradesTab(viewModel)
                            TutorSection.COURSE_LIVE -> TutorCourseLiveTab(viewModel)
                            TutorSection.COURSE_ARCHIVED_BROADCASTS -> ArchivedBroadcastsScreen(viewModel)
                            TutorSection.COURSE_ATTENDANCE -> TutorCourseAttendanceTab(viewModel)
                            TutorSection.INDIVIDUAL_ATTENDANCE_DETAIL -> TutorStudentAttendanceDetailScreen(viewModel)
                            TutorSection.STUDENTS -> TutorStudentsTab(viewModel)
                            TutorSection.MESSAGES -> TutorMessagesTab(viewModel)
                            TutorSection.CHAT -> TutorChatTab(viewModel)
                            TutorSection.LIBRARY -> TutorLibraryTab(viewModel, onPlayAudio)
                            TutorSection.BOOKS -> TutorBooksTab(viewModel)
                            TutorSection.AUDIOBOOKS -> TutorAudioBooksTab(viewModel, onPlayAudio)
                            TutorSection.TEACHER_DETAIL -> TutorDetailScreen(viewModel,  onNavigateToProfile = onNavigateToProfile)
                            TutorSection.CREATE_ASSIGNMENT -> CreateAssignmentScreen(viewModel)
                            TutorSection.START_LIVE_STREAM -> TutorCourseLiveTab(viewModel)
                            TutorSection.STUDENT_PROFILE -> TutorStudentProfileScreen(viewModel)
                            TutorSection.NOTIFICATIONS -> NotificationScreen(onNavigateToItem = {}, onNavigateToInvoice = {}, onNavigateToMessages = { viewModel.setSection(TutorSection.MESSAGES) }, onBack = { viewModel.setSection(TutorSection.DASHBOARD) }, isDarkTheme = isDarkTheme)
                            TutorSection.ABOUT -> AboutScreen(onBack = { viewModel.setSection(TutorSection.DASHBOARD) }, onDeveloperClick = onNavigateToDeveloper, onInstructionClick = onNavigateToInstruction, onOpenThemeBuilder = onOpenThemeBuilder, currentTheme = currentTheme, onThemeChange = onThemeChange)
                            TutorSection.READ_BOOK -> activeBook?.let { book -> PdfReaderScreen(bookId = book.id, onBack = { viewModel.setSection(TutorSection.LIBRARY) }, currentTheme = currentTheme, onThemeChange = onThemeChange) }
                            TutorSection.LISTEN_AUDIOBOOK -> {
                                LaunchedEffect(Unit) { activeAudioBook?.let { ab -> onPlayAudio((ab as AudioBook).toBook()); viewModel.setSection(TutorSection.LIBRARY) } }
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Navigation Bar Item with institutional styling.
 */
@Composable
fun RowScope.TutorNavButton(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(
        selected = selected, 
        onClick = onClick, 
        icon = { Icon(icon, null, modifier = Modifier.size(24.dp)) }, 
        label = { Text(text = label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis) }, 
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
