package assignment1.krzysztofoko.s16001089.ui.tutor.components.Catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorResourceDetailDialog
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorResourceItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TutorBooksTab(viewModel: TutorViewModel) {
    val allBooks by viewModel.allBooks.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var detailBook by remember { mutableStateOf<Book?>(null) }
    
    val scope = rememberCoroutineScope()
    var isAddingToLibrary by remember { mutableStateOf(false) }
    var isRemovingFromLibrary by remember { mutableStateOf(false) }
    
    var bookToConfirmAdd by remember { mutableStateOf<Book?>(null) }
    var bookToConfirmRemove by remember { mutableStateOf<Book?>(null) }

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Search books...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = MaterialTheme.shapes.medium
            )

            val filteredBooks = allBooks.filter { it.title.contains(searchQuery, ignoreCase = true) }

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isTablet) 3 else 2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredBooks) { book ->
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
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
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

    AppPopups.AddingToLibraryLoading(show = isAddingToLibrary, category = AppConstants.CAT_BOOKS)
    AppPopups.RemovingFromLibraryLoading(show = isRemovingFromLibrary)

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
}
