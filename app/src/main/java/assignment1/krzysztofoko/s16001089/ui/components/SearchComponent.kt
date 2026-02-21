package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Book

/**
 * SearchComponent.kt
 *
 * This file provides a comprehensive search subsystem for the application.
 * It includes a stylized search bar, an animated transition for revealing the search UI,
 * and specialized lists for displaying search history and live suggestions.
 */

/**
 * SearchBarComponent Composable
 *
 * A custom-built search bar designed to replace the standard TopAppBar title when searching.
 * It features a clean, rounded appearance with integrated clear and back actions.
 *
 * @param query The current text entered by the user.
 * @param onQueryChange Callback invoked as the user types.
 * @param onCloseClick Callback to dismiss the search mode and return to the normal title.
 * @param modifier Custom styling for the bar container.
 */
@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Used to dismiss the software keyboard programmatically.
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading search icon to reinforce the purpose of the field.
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            // The main input field. Styled to be transparent within the custom Surface.
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search books, courses...", style = MaterialTheme.typography.bodyMedium) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
                // Configure the keyboard to show a "Search" button.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            // Show a "Clear" button only when there is text in the field.
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
            
            // Trailing back button to exit search mode.
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * HomeSearchSection Composable
 *
 * Orchestrates the entire search UI logic on the home screen. 
 * It manages the animated visibility of the search bar and intelligently switches
 * between showing recent history and live results/suggestions.
 *
 * @param isSearchVisible Master flag controlling if search mode is active.
 * @param searchQuery Current input.
 * @param recentSearches List of strings from the user's local search history.
 * @param suggestions List of book/course objects matching the current query.
 */
@Composable
fun HomeSearchSection(
    isSearchVisible: Boolean,
    searchQuery: String,
    recentSearches: List<String>,
    onQueryChange: (String) -> Unit,
    onClearHistory: () -> Unit,
    onCloseClick: () -> Unit,
    suggestions: List<Book>,
    onSuggestionClick: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // The Search Bar uses a horizontal slide animation when appearing.
        AnimatedVisibility(
            visible = isSearchVisible,
            enter = expandHorizontally(expandFrom = Alignment.Start, animationSpec = tween(durationMillis = 400)) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start, animationSpec = tween(durationMillis = 400)) + fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            SearchBarComponent(
                query = searchQuery,
                onQueryChange = onQueryChange,
                onCloseClick = onCloseClick
            )
        }

        // --- SEARCH HISTORY --- //
        // Revealed only when the query is empty and the search mode is active.
        AnimatedVisibility(
            visible = isSearchVisible && searchQuery.isEmpty() && recentSearches.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SearchHistoryList(
                queries = recentSearches,
                onQueryClick = { onQueryChange(it) },
                onClearAll = onClearHistory
            )
        }

        // --- LIVE SUGGESTIONS --- //
        // Revealed as the user types and matches are found.
        AnimatedVisibility(
            visible = isSearchVisible && searchQuery.isNotEmpty() && suggestions.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SearchSuggestionsList(
                items = suggestions,
                onItemClick = onSuggestionClick
            ) { book ->
                // Row content for a single suggestion.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * SearchHistoryList Composable
 *
 * Displays a list of the user's most recent search queries.
 * Features a "Clear All" action to wipe the local history.
 */
@Composable
fun SearchHistoryList(
    queries: List<String>,
    onQueryClick: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            // Header with title and clear action.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Searches", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                TextButton(onClick = onClearAll) {
                    Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear All", style = MaterialTheme.typography.labelSmall)
                }
            }
            // Dynamically render each historical query.
            queries.forEachIndexed { index, query ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQueryClick(query) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(query, style = MaterialTheme.typography.bodyMedium)
                }
                // Visual divider between list items.
                if (index < queries.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

/**
 * SearchSuggestionsList Composable
 *
 * A generic, reusable list for displaying search results or suggestions. 
 * Uses a generic type `T` and a lambda for `itemContent` to allow flexible data binding.
 */
@Composable
fun <T> SearchSuggestionsList(
    items: List<T>,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .animateContentSize(), // Smoothly animate height changes as suggestions list grows/shrinks.
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            items.forEachIndexed { index, item ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    itemContent(item)
                }
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * TopBarSearchAction Composable
 *
 * A standard search icon intended for use in a TopAppBar's actions slot. 
 * It automatically hides itself when the search UI is active.
 */
@Composable
fun TopBarSearchAction(
    isSearchVisible: Boolean,
    modifier: Modifier = Modifier,
    onSearchIconClick: () -> Unit
) {
    if (!isSearchVisible) {
        IconButton(onClick = onSearchIconClick, modifier = modifier) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Open Search"
            )
        }
    }
}
