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
import assignment1.krzysztofoko.s16001089.ui.components.AppPopups
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TutorDashboardTab(
    viewModel: TutorViewModel,
    isDarkTheme: Boolean,
    onPlayAudio: (Book) -> Unit
) {
    val currentUser by viewModel.currentUserLocal.collectAsState()
    val tutorCourses by viewModel.tutorCourses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val pendingApps by viewModel.pendingApplications.collectAsState()
    
    val books by viewModel.allBooks.collectAsState()
    val audioBooks by viewModel.allAudioBooks.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()

    var detailBook by remember { mutableStateOf<Book?>(null) }
    var detailAudioBook by remember { mutableStateOf<AudioBook?>(null) }

    val scope = rememberCoroutineScope()
    var isAddingToLibrary by remember { mutableStateOf(false) }
    var isRemovingFromLibrary by remember { mutableStateOf(false) }
    
    var bookToConfirmAdd by remember { mutableStateOf<Book?>(null) }
    var bookToConfirmRemove by remember { mutableStateOf<Book?>(null) }
    var audioBookToConfirmAdd by remember { mutableStateOf<AudioBook?>(null) }
    var audioBookToConfirmRemove by remember { mutableStateOf<AudioBook?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val firstName = currentUser?.name?.split(" ")?.firstOrNull() ?: "Tutor"
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        photoUrl = currentUser?.photoUrl,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = AppConstants.TITLE_WELCOME_BACK,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = firstName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TutorStatCard(
                    modifier = Modifier.weight(1f),
                    title = "My Courses",
                    value = tutorCourses.size.toString(),
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.setSection(TutorSection.MY_COURSES) }
                )
                TutorStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Students",
                    value = allStudents.size.toString(),
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { viewModel.setSection(TutorSection.STUDENTS) }
                )
            }
        }

        if (pendingApps > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationImportant, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$pendingApps Pending Applications",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Students waiting for course approval",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // --- CATALOG FOR TUTORS ---
        item {
            Column {
                Text(
                    text = "Resource Library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Books",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { viewModel.setSection(TutorSection.BOOKS) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Explore More", fontSize = 12.sp)
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
                    }
                }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                ) {
                    items(books.take(10)) { book ->
                        val isAdded = purchasedIds.contains(book.id)
                        TutorResourceItem(
                            title = book.title,
                            imageUrl = book.imageUrl,
                            isAdded = isAdded,
                            isAudio = false,
                            onAddClick = { bookToConfirmAdd = book },
                            onRemoveClick = { bookToConfirmRemove = book },
                            onItemClick = { detailBook = book }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Audio Books",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { viewModel.setSection(TutorSection.AUDIOBOOKS) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Explore More", fontSize = 12.sp)
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
                    }
                }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp)
                ) {
                    items(audioBooks.take(10)) { ab ->
                        val isAdded = purchasedIds.contains(ab.id)
                        TutorResourceItem(
                            title = ab.title,
                            imageUrl = ab.imageUrl,
                            isAdded = isAdded,
                            isAudio = true,
                            onAddClick = { audioBookToConfirmAdd = ab },
                            onRemoveClick = { audioBookToConfirmRemove = ab },
                            onItemClick = { detailAudioBook = ab }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            TutorActionCard(
                title = "Create New Assignment",
                description = "Post a new task for your students to complete.",
                icon = Icons.Default.Assignment,
                onClick = { /* TODO */ }
            )
        }

        item {
            TutorActionCard(
                title = "Live Session",
                description = "Start a new video stream for your active course.",
                icon = Icons.Default.VideoCall,
                onClick = { /* TODO */ }
            )
        }
        
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // Confirmation Popups
    bookToConfirmAdd?.let { book ->
        AppPopups.AddToLibraryConfirmation(
            show = true,
            itemTitle = book.title,
            category = AppConstants.CAT_BOOKS,
            onDismiss = { bookToConfirmAdd = null },
            onConfirm = {
                bookToConfirmAdd = null
                scope.launch {
                    isAddingToLibrary = true
                    delay(1200)
                    viewModel.addToLibrary(book.id, AppConstants.CAT_BOOKS)
                    isAddingToLibrary = false
                }
            }
        )
    }
    
    bookToConfirmRemove?.let { book ->
        AppPopups.RemoveFromLibraryConfirmation(
            show = true,
            bookTitle = book.title,
            onDismiss = { bookToConfirmRemove = null },
            onConfirm = {
                bookToConfirmRemove = null
                scope.launch {
                    isRemovingFromLibrary = true
                    delay(800)
                    viewModel.removeFromLibrary(book.id)
                    isRemovingFromLibrary = false
                }
            }
        )
    }

    audioBookToConfirmAdd?.let { ab ->
        AppPopups.AddToLibraryConfirmation(
            show = true,
            itemTitle = ab.title,
            category = AppConstants.CAT_AUDIOBOOKS,
            isAudioBook = true,
            onDismiss = { audioBookToConfirmAdd = null },
            onConfirm = {
                audioBookToConfirmAdd = null
                scope.launch {
                    isAddingToLibrary = true
                    delay(1200)
                    viewModel.addToLibrary(ab.id, AppConstants.CAT_AUDIOBOOKS)
                    isAddingToLibrary = false
                }
            }
        )
    }
    
    audioBookToConfirmRemove?.let { ab ->
        AppPopups.RemoveFromLibraryConfirmation(
            show = true,
            bookTitle = ab.title,
            onDismiss = { audioBookToConfirmRemove = null },
            onConfirm = {
                audioBookToConfirmRemove = null
                scope.launch {
                    isRemovingFromLibrary = true
                    delay(800)
                    viewModel.removeFromLibrary(ab.id)
                    isRemovingFromLibrary = false
                }
            }
        )
    }

    // Feedback Popups
    AppPopups.AddingToLibraryLoading(show = isAddingToLibrary, category = AppConstants.CAT_BOOKS)
    AppPopups.RemovingFromLibraryLoading(show = isRemovingFromLibrary)

    // Detail Popups
    detailBook?.let { book ->
        TutorResourceDetailDialog(
            title = book.title,
            author = book.author,
            description = book.description,
            imageUrl = book.imageUrl,
            category = book.category,
            isAudio = false,
            isAdded = purchasedIds.contains(book.id),
            onAddClick = { bookToConfirmAdd = book },
            onRemoveClick = { bookToConfirmRemove = book },
            onActionClick = { viewModel.openBook(book) },
            onDismiss = { detailBook = null }
        )
    }

    detailAudioBook?.let { ab ->
        TutorResourceDetailDialog(
            title = ab.title,
            author = ab.author,
            description = ab.description,
            imageUrl = ab.imageUrl,
            category = ab.category,
            isAudio = true,
            isAdded = purchasedIds.contains(ab.id),
            onAddClick = { audioBookToConfirmAdd = ab },
            onRemoveClick = { audioBookToConfirmRemove = ab },
            onActionClick = { onPlayAudio(ab.toBook()) },
            onDismiss = { detailAudioBook = null }
        )
    }
}

@Composable
fun TutorResourceDetailDialog(
    title: String,
    author: String,
    description: String,
    imageUrl: String,
    category: String,
    isAudio: Boolean,
    isAdded: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onActionClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAdded) {
                    Button(
                        onClick = { onActionClick(); onDismiss() },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(if (isAudio) Icons.Default.PlayArrow else Icons.Default.LibraryBooks, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isAudio) "Listen" else "Read", fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = { onRemoveClick(); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Remove", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { onAddClick(); onDismiss() },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add to Library", fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(36.dp)) {
                Text("Close")
            }
        },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = formatAssetUrl(imageUrl),
                    contentDescription = title,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isAudio) "Narrated by $author" else "By $author",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(category) },
                    icon = {
                        Icon(
                            if (isAudio) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun TutorResourceItem(
    title: String,
    imageUrl: String,
    isAdded: Boolean,
    isAudio: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = formatAssetUrl(imageUrl),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)))))
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAudio) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                if (isAdded) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp).size(16.dp)
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                @Suppress("DEPRECATION")
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                if (!isAdded) {
                    Button(
                        onClick = onAddClick,
                        modifier = Modifier.fillMaxWidth().height(28.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Add to Library", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(1f).height(28.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
                        ) {
                            Text("In Library", fontSize = 10.sp, color = Color(0xFF4CAF50))
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable { onRemoveClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(110.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = color)
            Column {
                Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TutorActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
