package assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AudioBook
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.toBook
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * TutorDashboardTab provides the primary "Command Center" for course instructors.
 * It synthesizes various data points including student enrollment metrics, active class load,
 * and academic resources into a single responsive view.
 *
 * Key Responsibilities:
 * 1. Performance Overview: Displays real-time stats for assigned classes and total students.
 * 2. Administrative Alerts: Highlights pending student applications requiring approval.
 * 3. Resource Access: Provides a horizontal scrolling library for quick access to books and audiobooks.
 * 4. Quick Actions: Fast-track entry points for creating assignments and starting live streams.
 * 5. Library Management: Integrated Add/Remove logic for the tutor's personal professional catalog.
 */
@Composable
fun TutorDashboardTab(
    viewModel: TutorViewModel,
    isDarkTheme: Boolean,
    onPlayAudio: (Book) -> Unit
) {
    // REACTIVE STATE COLLECTION: Synchronizes UI with database changes
    val currentUser by viewModel.currentUserLocal.collectAsState()
    val assignedCourses by viewModel.assignedCourses.collectAsState()
    val allEnrollments by viewModel.allEnrollments.collectAsState()
    val pendingApps by viewModel.pendingApplications.collectAsState()
    
    // ANALYTICS LOGIC: Derived student count based on the tutor's specific class list
    val studentCount = remember(allEnrollments, assignedCourses) {
        val myCourseIds = assignedCourses.map { it.id }.toSet()
        allEnrollments
            .filter { it.courseId in myCourseIds && (it.status == "APPROVED" || it.status == "ENROLLED") }
            .map { it.userId }
            .distinct()
            .size
    }

    // CATALOG DATA: Pre-fetching institutional resources
    val books by viewModel.allBooks.collectAsState()
    val audioBooks by viewModel.allAudioBooks.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()

    // UI OVERLAY STATE: Manages detail dialogs and confirmation popups
    var detailBook by remember { mutableStateOf<Book?>(null) }
    var detailAudioBook by remember { mutableStateOf<AudioBook?>(null) }

    val scope = rememberCoroutineScope()
    var isAddingToLibrary by remember { mutableStateOf(false) }
    var isRemovingFromLibrary by remember { mutableStateOf(false) }
    
    var bookToConfirmAdd by remember { mutableStateOf<Book?>(null) }
    var bookToConfirmRemove by remember { mutableStateOf<Book?>(null) }
    var audioBookToConfirmAdd by remember { mutableStateOf<AudioBook?>(null) }
    var audioBookToConfirmRemove by remember { mutableStateOf<AudioBook?>(null) }

    val isTablet = isTablet()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1: PERSONALIZED WELCOME ---
        item {
            AdaptiveDashboardSection(maxWidth = AdaptiveWidths.Medium) {
                AdaptiveDashboardCard(
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ) { cardIsTablet ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { viewModel.setSection(TutorSection.TEACHER_DETAIL) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.AccountBox, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        }

                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(photoUrl = currentUser?.photoUrl, modifier = Modifier.size(if (cardIsTablet) 64.dp else 52.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(text = AppConstants.TITLE_WELCOME_BACK, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                val firstName = currentUser?.name?.split(" ")?.firstOrNull() ?: "Tutor"
                                Text(text = "${currentUser?.title ?: ""} $firstName", style = if (cardIsTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: ACADEMIC KPIS ---
        item {
            AdaptiveDashboardSection(maxWidth = AdaptiveWidths.Medium) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Quick view of class load
                    TutorStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Assigned Classes",
                        value = assignedCourses.size.toString(),
                        icon = Icons.Default.School,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setSection(TutorSection.MY_COURSES) }
                    )
                    // Quick view of student reach
                    TutorStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Students",
                        value = studentCount.toString(),
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { viewModel.setSection(TutorSection.STUDENTS) }
                    )
                }
            }
        }

        // --- SECTION 3: ADMINISTRATIVE ALERTS ---
        if (pendingApps > 0) {
            item {
                AdaptiveDashboardSection(maxWidth = AdaptiveWidths.Medium) {
                    AdaptiveDashboardCard(
                        backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationImportant, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(text = "$pendingApps Pending Applications", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text(text = "Students waiting for course approval", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 4: CURATED RESOURCE PREVIEW ---
        item {
            TutorResourceLibrarySection(
                books = books.take(10),
                audioBooks = audioBooks.take(10),
                purchasedIds = purchasedIds,
                onViewAllBooks = { viewModel.setSection(TutorSection.BOOKS) },
                onViewAllAudio = { viewModel.setSection(TutorSection.AUDIOBOOKS) },
                onBookClick = { detailBook = it },
                onAudioClick = { detailAudioBook = it },
                onAddBook = { bookToConfirmAdd = it },
                onRemoveBook = { bookToConfirmRemove = it },
                onAddAudio = { audioBookToConfirmAdd = it },
                onRemoveAudio = { audioBookToConfirmRemove = it }
            )
        }

        // --- SECTION 5: OPERATIONAL QUICK ACTIONS ---
        item {
            AdaptiveDashboardSection(maxWidth = AdaptiveWidths.Medium) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TutorActionCard(title = "Create Assignment", description = "Post a new task for your students to complete.", icon = Icons.Default.Assignment, onClick = { viewModel.setSection(TutorSection.CREATE_ASSIGNMENT) })
                    TutorActionCard(title = "Live Session", description = "Start a new video stream for your active course.", icon = Icons.Default.VideoCall, onClick = { viewModel.setSection(TutorSection.START_LIVE_STREAM) })
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // --- ADMINISTRATIVE OVERLAYS: CONFIRMATION LOGIC ---

    // BOOK LIBRARY ACTIONS
    bookToConfirmAdd?.let { book ->
        AppPopups.AddToLibraryConfirmation(show = true, itemTitle = book.title, category = AppConstants.CAT_BOOKS, onDismiss = { bookToConfirmAdd = null }, onConfirm = { bookToConfirmAdd = null; scope.launch { isAddingToLibrary = true; delay(1200); viewModel.addToLibrary(book.id, AppConstants.CAT_BOOKS); isAddingToLibrary = false } })
    }
    bookToConfirmRemove?.let { book ->
        AppPopups.RemoveFromLibraryConfirmation(show = true, bookTitle = book.title, onDismiss = { bookToConfirmRemove = null }, onConfirm = { bookToConfirmRemove = null; scope.launch { isRemovingFromLibrary = true; delay(800); viewModel.removeFromLibrary(book.id); isRemovingFromLibrary = false } })
    }
    
    // AUDIOBOOK LIBRARY ACTIONS
    audioBookToConfirmAdd?.let { ab ->
        AppPopups.AddToLibraryConfirmation(show = true, itemTitle = ab.title, category = AppConstants.CAT_AUDIOBOOKS, isAudioBook = true, onDismiss = { audioBookToConfirmAdd = null }, onConfirm = { audioBookToConfirmAdd = null; scope.launch { isAddingToLibrary = true; delay(1200); viewModel.addToLibrary(ab.id, AppConstants.CAT_AUDIOBOOKS); isAddingToLibrary = false } })
    }
    audioBookToConfirmRemove?.let { ab ->
        AppPopups.RemoveFromLibraryConfirmation(show = true, bookTitle = ab.title, onDismiss = { audioBookToConfirmRemove = null }, onConfirm = { audioBookToConfirmRemove = null; scope.launch { isRemovingFromLibrary = true; delay(800); viewModel.removeFromLibrary(ab.id); isRemovingFromLibrary = false } })
    }

    // GLOBAL STATUS INDICATORS: Background task feedback
    AppPopups.AddingToLibraryLoading(show = isAddingToLibrary, category = AppConstants.CAT_BOOKS)
    AppPopups.RemovingFromLibraryLoading(show = isRemovingFromLibrary)

    // EXPANDED DETAIL VIEW: Comprehensive metadata overlays
    detailBook?.let { book ->
        TutorResourceDetailDialog(title = book.title, author = book.author, description = book.description, imageUrl = book.imageUrl, category = book.category, isAudio = false, isAdded = purchasedIds.contains(book.id), onAddClick = { bookToConfirmAdd = book }, onRemoveClick = { bookToConfirmRemove = book }, onActionClick = { viewModel.openBook(book) }, onDismiss = { detailBook = null })
    }
    detailAudioBook?.let { ab ->
        TutorResourceDetailDialog(title = ab.title, author = ab.author, description = ab.description, imageUrl = ab.imageUrl, category = ab.category, isAudio = true, isAdded = purchasedIds.contains(ab.id), onAddClick = { audioBookToConfirmAdd = ab }, onRemoveClick = { audioBookToConfirmRemove = ab }, onActionClick = { onPlayAudio(ab.toBook()) }, onDismiss = { detailAudioBook = null })
    }
}

/**
 * A compact statistical visualization card for dashboard metrics.
 */
@Composable
fun TutorStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    AdaptiveDashboardCard(modifier = modifier, onClick = onClick, backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)) { isTablet ->
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(if (isTablet) 44.dp else 36.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(if (isTablet) 24.dp else 20.dp)) }
            }
            Column {
                Text(text = value, fontSize = if (isTablet) 28.sp else 22.sp, fontWeight = FontWeight.Black)
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

/**
 * A standard interactive card for initiating primary administrative tasks.
 */
@Composable
fun TutorActionCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    AdaptiveDashboardCard(onClick = onClick, backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)) { isTablet ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(if (isTablet) 56.dp else 48.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f))
        }
    }
}

/**
 * Centered layout wrapper for dashboard sections to ensure visual balance on large screens.
 */
@Composable
fun AdaptiveDashboardSection(maxWidth: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.adaptiveWidth(maxWidth).padding(horizontal = AdaptiveSpacing.contentPadding())) {
            content()
        }
    }
}

/**
 * Aggregated library section showcasing both books and audiobooks.
 */
@Composable
fun TutorResourceLibrarySection(
    books: List<Book>, audioBooks: List<AudioBook>, purchasedIds: Set<String>, 
    onViewAllBooks: () -> Unit, onViewAllAudio: () -> Unit, 
    onBookClick: (Book) -> Unit, onAudioClick: (AudioBook) -> Unit, 
    onAddBook: (Book) -> Unit, onRemoveBook: (Book) -> Unit, 
    onAddAudio: (AudioBook) -> Unit, onRemoveAudio: (AudioBook) -> Unit
) {
    val hPadding = AdaptiveSpacing.contentPadding()
    Column(modifier = Modifier.fillMaxWidth()) {
        AdaptiveDashboardSection(maxWidth = AdaptiveWidths.Medium) { Text(text = "Resource Library", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 8.dp)) }
        AdaptiveDashboardSection(maxWidth = AdaptiveWidths.Medium) { ResourceHeader(title = "Books", color = MaterialTheme.colorScheme.primary, onExplore = onViewAllBooks) }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(start = hPadding, end = hPadding, bottom = 16.dp, top = 8.dp)) {
            items(books) { book -> TutorResourceItem(title = book.title, imageUrl = book.imageUrl, isAdded = purchasedIds.contains(book.id), isAudio = false, onAddClick = { onAddBook(book) }, onRemoveClick = { onRemoveBook(book) }, onItemClick = { onBookClick(book) }) }
        }
        AdaptiveDashboardSection(maxWidth = AdaptiveWidths.Medium) { ResourceHeader(title = "Audio Books", color = MaterialTheme.colorScheme.secondary, onExplore = onViewAllAudio) }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(start = hPadding, end = hPadding, top = 8.dp)) {
            items(audioBooks) { ab -> TutorResourceItem(title = ab.title, imageUrl = ab.imageUrl, isAdded = purchasedIds.contains(ab.id), isAudio = true, onAddClick = { onAddAudio(ab) }, onRemoveClick = { onRemoveAudio(ab) }, onItemClick = { onAudioClick(ab) }) }
        }
    }
}

@Composable
private fun ResourceHeader(title: String, color: Color, onExplore: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
        TextButton(onClick = onExplore, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(30.dp)) {
            Text("Explore More", fontSize = 12.sp); Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * A highly-styled resource card featuring a vertical cover and dual-action library management.
 */
@Composable
fun TutorResourceItem(title: String, imageUrl: String, isAdded: Boolean, isAudio: Boolean, onAddClick: () -> Unit, onRemoveClick: () -> Unit, onItemClick: () -> Unit) {
    val isTablet = isTablet()
    Card(
        modifier = Modifier.width(if (isTablet) 160.dp else 140.dp).clickable { onItemClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(if (isTablet) 180.dp else 160.dp).fillMaxWidth().clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(model = formatAssetUrl(imageUrl), contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)))))
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(imageVector = if (isAudio) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp)) }
                }
                if (isAdded) {
                    Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(bottomEnd = 12.dp), modifier = Modifier.align(Alignment.TopStart)) { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.padding(4.dp).size(16.dp)) }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                if (!isAdded) {
                    Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth().height(32.dp), contentPadding = PaddingValues(0.dp), shape = RoundedCornerShape(10.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Add to Library", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(modifier = Modifier.weight(1f).height(32.dp), shape = RoundedCornerShape(10.dp), color = Color(0xFF4CAF50).copy(alpha = 0.1f), border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))) { Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("In Library", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) } }
                        Surface(onClick = onRemoveClick, modifier = Modifier.size(32.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) } }
                    }
                }
            }
        }
    }
}

/**
 * A professional detail dialog for academic resources. 
 * Features integrated cover art previews and contextual action rows.
 */
@Composable
fun TutorResourceDetailDialog(
    title: String, author: String, description: String, imageUrl: String, category: String, 
    isAudio: Boolean, isAdded: Boolean, onAddClick: () -> Unit, onRemoveClick: () -> Unit, 
    onActionClick: () -> Unit = {}, onDismiss: () -> Unit
) {
    val isTablet = isTablet()
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.adaptiveWidth(AdaptiveWidths.Medium),
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isAdded) {
                    Button(onClick = { onActionClick(); onDismiss() }, modifier = Modifier.height(44.dp).weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Icon(if (isAudio) Icons.Default.PlayArrow else Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(if (isAudio) "Listen Now" else "Read Now", fontWeight = FontWeight.Bold) }
                    FilledIconButton(onClick = { onRemoveClick(); onDismiss() }, modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f), contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Default.Delete, null) }
                } else {
                    Button(onClick = { onAddClick(); onDismiss() }, modifier = Modifier.height(44.dp).weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Add to Library", fontWeight = FontWeight.Bold) }
                }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))) { Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        },
        dismissButton = null,
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) { Surface(modifier = Modifier.size(if (isTablet) 240.dp else 180.dp).padding(8.dp), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) { AsyncImage(model = formatAssetUrl(imageUrl), contentDescription = title, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop) } }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
                Text(text = if (isAudio) "Narrated by $author" else "By $author", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) { Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (isAudio) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp)); Text(text = category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }
                Spacer(modifier = Modifier.height(20.dp))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) { Text(text = description, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
