package assignment1.krzysztofoko.s16001089.ui.tutor.components.Library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AudioBook
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.toBook
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorResourceDetailDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * TutorLibraryTab provides a dedicated administrative view for managing the tutor's personal
 * collection of academic resources. It separates materials into 'Books' and 'Audiobooks'
 * and provides direct access to educational tools like the PDF Reader and Media Player.
 *
 * Key Features:
 * 1. Categorized Navigation: Dual-tab interface for logical resource segregation.
 * 2. Personal Library Filtering: Real-time search focusing exclusively on owned materials.
 * 3. Library Maintenance: Integrated removal logic with confirmation safety checks.
 * 4. Tool Integration: Orchestrates transitions to specialized viewing/listening components.
 * 5. Adaptive Information Density: Responsive grid that adjusts column count based on screen width.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorLibraryTab(
    viewModel: TutorViewModel,
    onPlayAudio: (Book) -> Unit
) {
    // REACTIVE DATA: Synchronizes with the tutor's persistent 'Owned' resource streams
    val books by viewModel.libraryBooks.collectAsState()
    val audioBooks by viewModel.libraryAudioBooks.collectAsState()

    // UI STATE: Manages the active category, search filtering, and overlay triggers
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    var isRemovingFromLibrary by remember { mutableStateOf(false) }
    var itemToConfirmRemove by remember { mutableStateOf<Any?>(null) } 
    var detailItem by remember { mutableStateOf<Any?>(null) }

    // ADAPTIVE CONTAINER: Centered width constraint for improved readability on tablets
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { screenIsTablet ->
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(12.dp))
            
            // HEADER: Professional context for the personal library view
            AdaptiveDashboardHeader(
                title = "Resource Library",
                subtitle = "Access your educational materials",
                icon = Icons.Default.LibraryBooks,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // SEARCH BAR: Targeted filtering for the personal collection
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search your library...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp)
            )

            // CATEGORY NAVIGATION: High-contrast TabRow for switching resource types
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Books (${books.size})", fontWeight = if(selectedTab == 0) FontWeight.Black else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Audiobooks (${audioBooks.size})", fontWeight = if(selectedTab == 1) FontWeight.Black else FontWeight.Normal) }
                )
            }

            // RESOURCE GRID: Adaptive layout (1 column mobile / 2 columns tablet)
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (screenIsTablet) 2 else 1),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    // RENDER: Academic Books
                    val filteredBooks = books.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    if (filteredBooks.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { LibraryEmptyState("No books found in your library.") }
                    } else {
                        items(filteredBooks) { book ->
                            LibraryItem(
                                title = book.title,
                                author = book.author,
                                imageUrl = book.imageUrl,
                                category = book.category,
                                isAudio = false,
                                onRemove = { itemToConfirmRemove = book },
                                onAction = { 
                                    // TOOL TRIGGER: Opens the integrated PDF Reader
                                    viewModel.openBook(book) 
                                }
                            )
                        }
                    }
                } else {
                    // RENDER: Digital Audiobooks
                    val filteredAudio = audioBooks.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    if (filteredAudio.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { LibraryEmptyState("No audiobooks found in your library.") }
                    } else {
                        items(filteredAudio) { ab ->
                            LibraryItem(
                                title = ab.title,
                                author = ab.author,
                                imageUrl = ab.imageUrl,
                                category = ab.category,
                                isAudio = true,
                                onRemove = { itemToConfirmRemove = ab },
                                onAction = { 
                                    // TOOL TRIGGER: Initiates the global Media Player
                                    onPlayAudio(ab.toBook()) 
                                }
                            )
                        }
                    }
                }

                // FOOTER: External link to explore the wider institutional catalog
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Not found what you looking for?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                // NAVIGATION: Redirects to the appropriate catalog tab
                                if (selectedTab == 0) viewModel.setSection(TutorSection.BOOKS) 
                                else viewModel.setSection(TutorSection.AUDIOBOOKS)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (selectedTab == 0) "Explore More Books" else "Explore More Audiobooks",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Safety spacer for navigation bar
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // --- OVERLAYS: Library Management & Detail Logic ---

    // CONFIRMATION DIALOG: Prevents accidental removal of academic assets
    itemToConfirmRemove?.let { item ->
        val title = if (item is Book) item.title else (item as AudioBook).title
        val id = if (item is Book) item.id else (item as AudioBook).id

        AppPopups.RemoveFromLibraryConfirmation(
            show = true,
            bookTitle = title,
            onDismiss = { itemToConfirmRemove = null },
            onConfirm = {
                itemToConfirmRemove = null
                scope.launch {
                    isRemovingFromLibrary = true
                    delay(800) // Simulated database processing delay
                    viewModel.removeFromLibrary(id)
                    isRemovingFromLibrary = false
                }
            }
        )
    }

    // STATUS FEEDBACK: Global overlay during removal operations
    AppPopups.RemovingFromLibraryLoading(show = isRemovingFromLibrary)

    // DETAIL POPUP: Detailed metadata overview for library items
    detailItem?.let { item ->
        val isAudio = item is AudioBook
        val title = if (item is Book) item.title else (item as AudioBook).title
        val author = if (item is Book) item.author else (item as AudioBook).author
        val desc = if (item is Book) item.description else (item as AudioBook).description
        val img = if (item is Book) item.imageUrl else (item as AudioBook).imageUrl
        val cat = if (item is Book) item.category else (item as AudioBook).category

        TutorResourceDetailDialog(
            title = title,
            author = author,
            description = desc,
            imageUrl = img,
            category = cat,
            isAudio = isAudio,
            isAdded = true, 
            onAddClick = { },
            onRemoveClick = { itemToConfirmRemove = item },
            onActionClick = {
                if (isAudio) onPlayAudio((item as AudioBook).toBook())
                else viewModel.openBook(item as Book)
            },
            onDismiss = { detailItem = null }
        )
    }
}

/**
 * A specialized interactive item card for the personal library.
 * Wraps the standard [BookItemCard] with library-specific actions (Remove, Read/Listen).
 */
@Composable
fun LibraryItem(
    title: String,
    author: String,
    imageUrl: String,
    category: String,
    isAudio: Boolean,
    onRemove: () -> Unit,
    onAction: () -> Unit
) {
    val dummyBook = Book(
        id = "", title = title, author = author, imageUrl = imageUrl, 
        category = category, isAudioBook = isAudio, price = 0.0
    )

    BookItemCard(
        book = dummyBook,
        onClick = onAction,
        trailingContent = {
            // Contextual action for library maintenance
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
            }
        },
        bottomContent = {
            Spacer(modifier = Modifier.height(8.dp))
            // Primary tool trigger button
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(if (isAudio) Icons.Default.PlayArrow else Icons.Default.LibraryBooks, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isAudio) "Listen Now" else "Read Now", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/** Placeholder component for empty library states. */
@Composable
fun LibraryEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LibraryBooks, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        }
    }
}
