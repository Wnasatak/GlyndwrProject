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
import assignment1.krzysztofoko.s16001089.data.AudioBook
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorResourceDetailDialog
import assignment1.krzysztofoko.s16001089.ui.tutor.components.Dashboard.TutorResourceItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TutorAudioBooksTab(
    viewModel: TutorViewModel,
    onPlayAudio: (Book) -> Unit
) {
    val allAudioBooks by viewModel.allAudioBooks.collectAsState()
    val purchasedIds by viewModel.purchasedIds.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var detailAudioBook by remember { mutableStateOf<AudioBook?>(null) }
    
    val scope = rememberCoroutineScope()
    var isAddingToLibrary by remember { mutableStateOf(false) }
    var isRemovingFromLibrary by remember { mutableStateOf(false) }
    
    var abToConfirmAdd by remember { mutableStateOf<AudioBook?>(null) }
    var abToConfirmRemove by remember { mutableStateOf<AudioBook?>(null) }

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = AdaptiveSpacing.contentPadding())) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Search audiobooks...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = MaterialTheme.shapes.medium
            )

            val filteredAudio = allAudioBooks.filter { it.title.contains(searchQuery, ignoreCase = true) }

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isTablet) 3 else 2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredAudio) { ab ->
                    val isAdded = purchasedIds.contains(ab.id)
                    TutorResourceItem(
                        title = ab.title,
                        imageUrl = ab.imageUrl,
                        isAdded = isAdded,
                        isAudio = true,
                        onAddClick = { abToConfirmAdd = ab },
                        onRemoveClick = { abToConfirmRemove = ab },
                        onItemClick = { detailAudioBook = ab }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Confirmation Popups
    abToConfirmAdd?.let { ab ->
        AppPopups.AddToLibraryConfirmation(
            show = true,
            itemTitle = ab.title,
            category = AppConstants.CAT_AUDIOBOOKS,
            isAudioBook = true,
            onDismiss = { abToConfirmAdd = null },
            onConfirm = {
                abToConfirmAdd = null
                scope.launch {
                    isAddingToLibrary = true
                    delay(1200)
                    viewModel.addToLibrary(ab.id, AppConstants.CAT_AUDIOBOOKS)
                    isAddingToLibrary = false
                }
            }
        )
    }
    
    abToConfirmRemove?.let { ab ->
        AppPopups.RemoveFromLibraryConfirmation(
            show = true,
            bookTitle = ab.title,
            onDismiss = { abToConfirmRemove = null },
            onConfirm = {
                abToConfirmRemove = null
                scope.launch {
                    isRemovingFromLibrary = true
                    delay(800)
                    viewModel.removeFromLibrary(ab.id)
                    isRemovingFromLibrary = false
                }
            }
        )
    }

    AppPopups.AddingToLibraryLoading(show = isAddingToLibrary, category = AppConstants.CAT_AUDIOBOOKS)
    AppPopups.RemovingFromLibraryLoading(show = isRemovingFromLibrary)

    detailAudioBook?.let { ab ->
        TutorResourceDetailDialog(
            title = ab.title,
            author = ab.author,
            description = ab.description,
            imageUrl = ab.imageUrl,
            category = ab.category,
            isAudio = true,
            isAdded = purchasedIds.contains(ab.id),
            onAddClick = { abToConfirmAdd = ab },
            onRemoveClick = { abToConfirmRemove = ab },
            onActionClick = { 
                // Using the available openAudioBook method from viewModel
                viewModel.openAudioBook(ab) 
            },
            onDismiss = { detailAudioBook = null }
        )
    }
}
