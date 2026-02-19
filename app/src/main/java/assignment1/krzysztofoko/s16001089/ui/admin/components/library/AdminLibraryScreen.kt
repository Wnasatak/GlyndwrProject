package assignment1.krzysztofoko.s16001089.ui.admin.components.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.*

/**
 * AdminLibraryScreen.kt
 *
 * This screen displays a collection of academic assets (Books and Audiobooks) that the Admin
 * has acquired or purchased. It acts as a personal repository for educational materials, 
 * separate from the global catalog management.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLibraryScreen(
    allBooks: List<Book>,
    onNavigateToDetails: (String) -> Unit,
    onExploreMore: () -> Unit,
    isDarkTheme: Boolean
) {
    // --- 1. DATA FILTERING (PRE-PROCESSING) ---
    // Extract items that belong in the library.
    // Logic: Item must have an order confirmation (purchased). 
    // We filter to ensure we only show items that are meant for the library (Books/Audiobooks/Courses).
    val libraryItems = remember(allBooks) {
        allBooks.filter { item ->
            item.orderConfirmation != null && 
            (item.mainCategory == AppConstants.CAT_BOOKS || 
             item.mainCategory == AppConstants.CAT_AUDIOBOOKS || 
             item.isAudioBook)
        }
    }

    // --- 2. LOCAL UI STATE ---
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    // --- 3. DYNAMIC FILTERING (REACTIVE) ---
    // Refines the library collection based on user interaction (Search + Tabs).
    val filteredItems = remember(libraryItems, searchQuery, selectedTab) {
        libraryItems.filter { item ->
            // Check if title or author matches the search query (case-insensitive).
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || 
                                item.author.contains(searchQuery, ignoreCase = true)
            
            // Filter by selected tab using the robust 'isAudioBook' flag instead of just category strings.
            val matchesTab = when (selectedTab) {
                0 -> true // "All"
                1 -> !item.isAudioBook // E-Books (not an audiobook)
                else -> item.isAudioBook // Audiobooks
            }
            
            matchesSearch && matchesTab
        }
    }

    // --- 4. UI COMPOSITION ---
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { screenIsTablet ->
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(12.dp))
            
            AdaptiveDashboardHeader(
                title = "My Library",
                subtitle = "Collection of Personal Academic assets",
                icon = Icons.Default.LibraryBooks,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // SEARCH INTERFACE
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search your assets...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )

            // CATEGORY NAVIGATION (TABS)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp),
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All", fontWeight = if(selectedTab == 0) FontWeight.Black else FontWeight.Normal) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Books", fontWeight = if(selectedTab == 1) FontWeight.Black else FontWeight.Normal) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Audiobooks", fontWeight = if(selectedTab == 2) FontWeight.Black else FontWeight.Normal) })
            }

            // ASSET GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (screenIsTablet) 2 else 1),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredItems.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LibraryBooks, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isEmpty()) "Your library is currently empty." else "No matching items found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredItems) { book ->
                        BookItemCard(
                            book = book,
                            onClick = { onNavigateToDetails(book.id) },
                            bottomContent = {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onNavigateToDetails(book.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (book.isAudioBook) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(imageVector = if (book.isAudioBook) Icons.Default.PlayArrow else Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    @Suppress("DEPRECATION")
                                    Text(text = if (book.isAudioBook) "Listen Now" else "Read Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        @Suppress("DEPRECATION")
                        Text(text = "Not finding what you're looking for?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onExploreMore, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 24.dp)) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            @Suppress("DEPRECATION")
                            Text(text = "Search for more", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
