package assignment1.krzysztofoko.s16001089.ui.tutor.components.Library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
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
import assignment1.krzysztofoko.s16001089.ui.components.AppPopups
import assignment1.krzysztofoko.s16001089.ui.components.BookItemCard
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorSection
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorResourceDetailDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorLibraryTab(
    viewModel: TutorViewModel,
    onPlayAudio: (Book) -> Unit
) {
    val books by viewModel.libraryBooks.collectAsState()
    val audioBooks by viewModel.libraryAudioBooks.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val scope = rememberCoroutineScope()
    var isRemovingFromLibrary by remember { mutableStateOf(false) }
    var itemToConfirmRemove by remember { mutableStateOf<Any?>(null) } // Can be Book or AudioBook
    var detailItem by remember { mutableStateOf<Any?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search your library...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = MaterialTheme.shapes.medium
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Books (${books.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Audiobooks (${audioBooks.size})") }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                val filteredBooks = books.filter { it.title.contains(searchQuery, ignoreCase = true) }
                if (filteredBooks.isEmpty()) {
                    item { LibraryEmptyState("No books found in your library.") }
                } else {
                    items(filteredBooks) { book ->
                        LibraryItem(
                            title = book.title,
                            author = book.author,
                            imageUrl = book.imageUrl,
                            isAudio = false,
                            onRemove = { itemToConfirmRemove = book },
                            onAction = { viewModel.openBook(book) }
                        )
                    }
                }
            } else {
                val filteredAudio = audioBooks.filter { it.title.contains(searchQuery, ignoreCase = true) }
                if (filteredAudio.isEmpty()) {
                    item { LibraryEmptyState("No audiobooks found in your library.") }
                } else {
                    items(filteredAudio) { ab ->
                        LibraryItem(
                            title = ab.title,
                            author = ab.author,
                            imageUrl = ab.imageUrl,
                            isAudio = true,
                            onRemove = { itemToConfirmRemove = ab },
                            onAction = { 
                                // Directly play audio when clicking from library
                                onPlayAudio(ab.toBook())
                            }
                        )
                    }
                }
            }
            
            item {
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
                            if (selectedTab == 0) {
                                viewModel.setSection(TutorSection.BOOKS)
                            } else {
                                viewModel.setSection(TutorSection.AUDIOBOOKS)
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
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
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Confirmation Popup
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
                    delay(800)
                    viewModel.removeFromLibrary(id)
                    isRemovingFromLibrary = false
                }
            }
        )
    }

    // Loading Popup
    AppPopups.RemovingFromLibraryLoading(show = isRemovingFromLibrary)

    // Detail Popup
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
            isAdded = true, // It's in the library
            onAddClick = { /* Already added */ },
            onRemoveClick = { itemToConfirmRemove = item },
            onActionClick = {
                if (isAudio) {
                    onPlayAudio((item as AudioBook).toBook())
                } else {
                    viewModel.openBook(item as Book)
                }
            },
            onDismiss = { detailItem = null }
        )
    }
}

@Composable
fun LibraryItem(
    title: String,
    author: String,
    imageUrl: String,
    isAudio: Boolean,
    onRemove: () -> Unit,
    onAction: () -> Unit
) {
    val dummyBook = Book(
        title = title,
        author = author,
        imageUrl = imageUrl,
        isAudioBook = isAudio
    )

    BookItemCard(
        book = dummyBook,
        onClick = onAction,
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
            }
        },
        bottomContent = {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(if (isAudio) Icons.Default.PlayArrow else Icons.Default.LibraryBooks, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isAudio) "Listen Now" else "Read Now")
            }
        }
    )
}

@Composable
fun LibraryEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.LibraryBooks,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
