package assignment1.krzysztofoko.s16001089.ui.tutor

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import assignment1.krzysztofoko.s16001089.ui.details.pdf.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorDashboardTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses.TutorCoursesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Students.TutorStudentsTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.TutorMessagesTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages.TutorChatTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Library.TutorLibraryTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Catalog.TutorBooksTab
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Catalog.TutorAudioBooksTab
import com.google.firebase.auth.FirebaseAuth

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
                if (!isReaderOpen && !isChatOpen && currentSection != TutorSection.LISTEN_AUDIOBOOK) {
                    TopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { 
                            Text(
                                text = when(currentSection) {
                                    TutorSection.LIBRARY -> "Resource Library"
                                    TutorSection.BOOKS -> "Digital Books"
                                    TutorSection.AUDIOBOOKS -> "Audio Books"
                                    else -> AppConstants.TITLE_TUTOR_PANEL
                                }, 
                                fontWeight = FontWeight.Black, 
                                style = MaterialTheme.typography.titleLarge
                            ) 
                        },
                        navigationIcon = {
                            if (currentSection != TutorSection.DASHBOARD && 
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
                            // Integrated Mini Audio Controls in Navbar
                            if (currentPlayingBookId != null) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                }
            },
            bottomBar = {
                // Hide bottom navigation when chat or specialized sub-sections are open
                val hideBottomBar = isChatOpen || currentSection == TutorSection.BOOKS || 
                                   currentSection == TutorSection.AUDIOBOOKS || 
                                   currentSection == TutorSection.READ_BOOK || 
                                   currentSection == TutorSection.LISTEN_AUDIOBOOK
                
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
                            label = "Courses"
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
            val contentPadding = if (isReaderOpen) PaddingValues(0.dp) else padding
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding())
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentSection,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TutorSectionTransition"
                    ) { section ->
                        when (section) {
                            TutorSection.DASHBOARD -> TutorDashboardTab(viewModel, isDarkTheme, onPlayAudio)
                            TutorSection.MY_COURSES -> TutorCoursesTab(viewModel)
                            TutorSection.STUDENTS -> TutorStudentsTab(viewModel)
                            TutorSection.MESSAGES -> TutorMessagesTab(viewModel)
                            TutorSection.CHAT -> TutorChatTab(viewModel)
                            TutorSection.LIBRARY -> TutorLibraryTab(viewModel, onPlayAudio)
                            TutorSection.BOOKS -> TutorBooksTab(viewModel)
                            TutorSection.AUDIOBOOKS -> TutorAudioBooksTab(viewModel, onPlayAudio)
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
                                // Transition immediately to global player and return to library
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
