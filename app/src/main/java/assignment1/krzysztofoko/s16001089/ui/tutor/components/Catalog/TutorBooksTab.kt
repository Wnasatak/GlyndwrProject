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

/**
 * TutorBooksTab provides an interface for tutors to browse the academic book catalog.
 * It allows tutors to search for books, view details, and manage their personal professional library.
 * 
 * Key Features:
 * - Real-time filtering based on search queries.
 * - Integration with the tutor's library (Add/Remove functionality).
 * - Responsive grid layout for consistent display across mobile and tablet devices.
 * - Managed loading states and confirmation dialogs for a polished UX.
 */
@Composable
fun TutorBooksTab(viewModel: TutorViewModel) {
    // REACTIVE DATA: Tracks all available books and the tutor's current ownership status
    val allBooks by viewModel.allBooks.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()
    
    // UI STATE: Manages search input, detail dialog visibility, and loading indicators
    var searchQuery by remember { mutableStateOf("") }
    var detailBook by remember { mutableStateOf<Book?>(null) }
    
    val scope = rememberCoroutineScope()
    var isAddingToLibrary by remember { mutableStateOf(false) }
    var isRemovingFromLibrary by remember { mutableStateOf(false) }
    
    // CONFIRMATION STATE: Tracks which item is currently targeted for a library action
    var bookToConfirmAdd by remember { mutableStateOf<Book?>(null) }
    var bookToConfirmRemove by remember { mutableStateOf<Book?>(null) }

    // ADAPTIVE CONTAINER: Ensures the layout is centered and width-constrained on tablets
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
            
            // SEARCH INTERFACE: Real-time filtering for the academic catalog
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

            // FILTERING LOGIC: Derived state for the grid display
            val filteredBooks = allBooks.filter { it.title.contains(searchQuery, ignoreCase = true) }

            // GRID LAYOUT: Adapts column count (3 for tablet, 2 for mobile) for optimal info density
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
                // Adds bottom spacing to ensure the FAB or navigation doesn't obscure the last items
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // --- DIALOGS: Library Confirmation & Action Orchestration ---

    /**
     * Confirmation popup for adding a resource. 
     * Simulates a secure institutional approval delay before updating the database.
     */
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
                    delay(1200) // Simulated sync delay for visual feedback
                    viewModel.addToLibrary(book.id, AppConstants.CAT_BOOKS)
                    isAddingToLibrary = false
                }
            }
        )
    }
    
    /**
     * Confirmation popup for removing a resource.
     * Use Case: Cleaning up the tutor's personal library of unused academic materials.
     */
    bookToConfirmRemove?.let { book ->
        AppPopups.RemoveFromLibraryConfirmation(
            show = true,
            bookTitle = book.title,
            onDismiss = { bookToConfirmRemove = null },
            onConfirm = {
                bookToConfirmRemove = null
                scope.launch {
                    isRemovingFromLibrary = true
                    delay(800) // Simulated processing delay
                    viewModel.removeFromLibrary(book.id)
                    isRemovingFromLibrary = false
                }
            }
        )
    }

    // SYSTEM OVERLAYS: Global loading indicators for library transactions
    AppPopups.AddingToLibraryLoading(show = isAddingToLibrary, category = AppConstants.CAT_BOOKS)
    AppPopups.RemovingFromLibraryLoading(show = isRemovingFromLibrary)

    /**
     * DETAIL VIEW: Expanded dialog for reviewing book metadata (Author, Description, etc.)
     * and performing primary actions (Read, Add to Library).
     */
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
