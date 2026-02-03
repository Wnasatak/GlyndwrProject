package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.toBook
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.details.pdf.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.TutorCourseDetailScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Assignments.TutorCourseAssignmentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Modules.TutorCourseModulesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Students.TutorClassStudentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Grades.TutorCourseGradesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.Live.TutorCourseLiveTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorDashboardTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.TutorCoursesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Students.TutorStudentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.TutorMessagesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.TutorChatTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Library.TutorLibraryTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Catalog.TutorBooksTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Catalog.TutorAudioBooksTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorDetailScreen
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage

/**
 * The Central Dashboard for Tutors.
 * Orchestrates My Courses, Student Management, and messaging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorPanelScreen(
    onBack: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayAudio: (Book) -> Unit,
    externalPlayer: Player? = null,
    onStopPlayer: () -> Unit = {},
    currentPlayingBookId: String? = null,
    isAudioPlaying: Boolean = false,
    viewModel: TutorViewModel = viewModel(factory = TutorViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current),
        tutorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val currentSection by viewModel.currentSection.collectAsState()
    val activeBook by viewModel.activeBook.collectAsState()
    val activeAudioBook by viewModel.activeAudioBook.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    
    val isChatOpen = currentSection == TutorSection.CHAT
    val isReaderOpen = currentSection == TutorSection.READ_BOOK
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isReaderOpen) {
            HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!isReaderOpen && currentSection != TutorSection.LISTEN_AUDIOBOOK) {
                    TopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { 
                            if (isChatOpen && selectedStudent != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(
                                        photoUrl = selectedStudent?.photoUrl,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedStudent?.name ?: "User",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            
                                            val roleText = (selectedStudent?.role ?: "user").uppercase()
                                            val tagColor = when(roleText) {
                                                "ADMIN", "TUTOR" -> MaterialTheme.colorScheme.errorContainer
                                                else -> MaterialTheme.colorScheme.primaryContainer
                                            }
                                            val onTagColor = when(roleText) {
                                                "ADMIN", "TUTOR" -> MaterialTheme.colorScheme.onErrorContainer
                                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                                            }

                                            Surface(
                                                color = tagColor,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = roleText,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = onTagColor,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Online",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Animated pulsing logo effect
                                    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
                                    val logoScale by infiniteTransition.animateFloat(
                                        initialValue = 0.85f,
                                        targetValue = 1.15f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1500, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "logoScale"
                                    )

                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .scale(logoScale)
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Color.Transparent)
                                                    ),
                                                    CircleShape
                                                )
                                        )
                                        Surface(
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape,
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        ) {
                                            AsyncImage(
                                                model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                                contentDescription = "University Logo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    
                                    val sectionTitle = when(currentSection) {
                                        TutorSection.LIBRARY -> "Library"
                                        TutorSection.BOOKS -> "Explore Books"
                                        TutorSection.AUDIOBOOKS -> "Explore Audiobooks"
                                        TutorSection.MESSAGES -> "Messages"
                                        TutorSection.STUDENTS -> "Student Directory"
                                        TutorSection.MY_COURSES -> "My Classes"
                                        TutorSection.SELECTED_COURSE -> selectedCourse?.title ?: "Class Details"
                                        TutorSection.COURSE_STUDENTS -> "Class Students"
                                        TutorSection.COURSE_MODULES -> "Class Modules"
                                        TutorSection.COURSE_ASSIGNMENTS -> "Assignments"
                                        TutorSection.COURSE_GRADES -> "Grades"
                                        TutorSection.COURSE_LIVE -> "Live Stream"
                                        TutorSection.DASHBOARD -> "Teacher Dashboard"
                                        TutorSection.TEACHER_DETAIL -> "Teacher Profile"
                                        else -> ""
                                    }
                                    if (sectionTitle.isNotEmpty()) {
                                        Text(
                                            text = " • $sectionTitle",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            if (isChatOpen) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.MESSAGES) }) {
                                    Icon(Icons.Default.ArrowBack, "Back to Messages")
                                }
                            } else if (currentSection == TutorSection.COURSE_STUDENTS || 
                                     currentSection == TutorSection.COURSE_MODULES || 
                                     currentSection == TutorSection.COURSE_ASSIGNMENTS || 
                                     currentSection == TutorSection.COURSE_GRADES || 
                                     currentSection == TutorSection.COURSE_LIVE) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.SELECTED_COURSE) }) {
                                    Icon(Icons.Default.ArrowBack, "Back to Course Detail")
                                }
                            } else if (currentSection == TutorSection.SELECTED_COURSE) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.MY_COURSES) }) {
                                    Icon(Icons.Default.ArrowBack, "Back to My Classes")
                                }
                            } else if (currentSection == TutorSection.TEACHER_DETAIL) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.DASHBOARD) }) {
                                    Icon(Icons.Default.ArrowBack, "Back to Dashboard")
                                }
                            } else if (currentSection != TutorSection.DASHBOARD && 
                                     currentSection != TutorSection.MY_COURSES && 
                                     currentSection != TutorSection.LIBRARY && 
                                     currentSection != TutorSection.STUDENTS && 
                                     currentSection != TutorSection.MESSAGES) {
                                IconButton(onClick = { viewModel.setSection(TutorSection.DASHBOARD) }) {
                                    Icon(Icons.Default.ArrowBack, null)
                                }
                            }
                        },
                        actions = {
                            if (!isChatOpen) {
                                if (currentPlayingBookId != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            if (isAudioPlaying) externalPlayer?.pause() else externalPlayer?.play()
                                        }) {
                                            Icon(
                                                imageVector = if (isAudioPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                                contentDescription = "Toggle Audio",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = onStopPlayer) {
                                            Icon(Icons.Default.Close, "Stop", modifier = Modifier.size(20.dp))
                                        }
                                        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
                                    }
                                }

                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, AppConstants.TITLE_MORE_OPTIONS)
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Teacher Profile") },
                                            onClick = {
                                                showMenu = false
                                                viewModel.setSection(TutorSection.TEACHER_DETAIL)
                                            },
                                            leadingIcon = { Icon(Icons.Default.AccountBox, null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(AppConstants.TITLE_PROFILE_SETTINGS) },
                                            onClick = {
                                                showMenu = false
                                                onNavigateToProfile()
                                            },
                                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (isDarkTheme) AppConstants.TITLE_LIGHT_MODE else AppConstants.TITLE_DARK_MODE) },
                                            onClick = {
                                                showMenu = false
                                                onToggleTheme()
                                            },
                                            leadingIcon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                                        )
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        DropdownMenuItem(
                                            text = { Text(AppConstants.BTN_LOG_OUT, color = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showMenu = false
                                                onLogout()
                                            },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) }
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
                val hideBottomBar = isChatOpen || currentSection == TutorSection.BOOKS ||
                        currentSection == TutorSection.AUDIOBOOKS ||
                        currentSection == TutorSection.READ_BOOK ||
                        currentSection == TutorSection.LISTEN_AUDIOBOOK ||
                        currentSection == TutorSection.SELECTED_COURSE ||
                        currentSection == TutorSection.COURSE_STUDENTS ||
                        currentSection == TutorSection.COURSE_MODULES ||
                        currentSection == TutorSection.COURSE_ASSIGNMENTS ||
                        currentSection == TutorSection.COURSE_GRADES ||
                        currentSection == TutorSection.COURSE_LIVE ||
                        currentSection == TutorSection.TEACHER_DETAIL
                
                if (!hideBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        windowInsets = WindowInsets.navigationBars
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
                            label = "Classes"
                        )
                        TutorNavButton(
                            selected = currentSection == TutorSection.LIBRARY,
                            onClick = { viewModel.setSection(TutorSection.LIBRARY) },
                            icon = Icons.Default.LibraryBooks,
                            label = "Library"
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
            }
        ) { padding ->
            val topPadding = padding.calculateTopPadding()
            val bottomPadding = padding.calculateBottomPadding()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (isReaderOpen) 0.dp else topPadding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isChatOpen) 0.dp else bottomPadding)
                ) {
                    AnimatedContent(
                        targetState = currentSection,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TutorSectionTransition"
                    ) { section ->
                        when (section) {
                            TutorSection.DASHBOARD -> TutorDashboardTab(viewModel, isDarkTheme, onPlayAudio)
                            TutorSection.MY_COURSES -> TutorCoursesTab(viewModel)
                            TutorSection.SELECTED_COURSE -> TutorCourseDetailScreen(viewModel)
                            TutorSection.COURSE_STUDENTS -> TutorClassStudentsTab(viewModel)
                            TutorSection.COURSE_MODULES -> TutorCourseModulesTab(viewModel)
                            TutorSection.COURSE_ASSIGNMENTS -> TutorCourseAssignmentsTab(viewModel)
                            TutorSection.COURSE_GRADES -> TutorCourseGradesTab(viewModel)
                            TutorSection.COURSE_LIVE -> TutorCourseLiveTab(viewModel)
                            TutorSection.STUDENTS -> TutorStudentsTab(viewModel)
                            TutorSection.MESSAGES -> TutorMessagesTab(viewModel)
                            TutorSection.CHAT -> TutorChatTab(viewModel)
                            TutorSection.LIBRARY -> TutorLibraryTab(viewModel, onPlayAudio)
                            TutorSection.BOOKS -> TutorBooksTab(viewModel)
                            TutorSection.AUDIOBOOKS -> TutorAudioBooksTab(viewModel, onPlayAudio)
                            TutorSection.TEACHER_DETAIL -> TutorDetailScreen(viewModel)
                            TutorSection.READ_BOOK -> {
                                activeBook?.let { book ->
                                    PdfReaderScreen(
                                        bookId = book.id,
                                        onBack = { viewModel.setSection(TutorSection.LIBRARY) },
                                        isDarkTheme = isDarkTheme,
                                        onToggleTheme = onToggleTheme
                                    )
                                }
                            }
                            TutorSection.LISTEN_AUDIOBOOK -> {
                                LaunchedEffect(Unit) {
                                    activeAudioBook?.let { ab ->
                                        onPlayAudio(ab.toBook())
                                        viewModel.setSection(TutorSection.LIBRARY)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
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
