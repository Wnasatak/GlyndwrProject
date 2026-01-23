package assignment1.krzysztofoko.s16001089.ui.components // Package declaration for the components

import androidx.compose.animation.* // Importing animation APIs
import androidx.compose.animation.core.tween // Importing tween animation curve
import androidx.compose.foundation.BorderStroke // Importing border styling
import androidx.compose.foundation.clickable // Importing click interaction
import androidx.compose.foundation.layout.* // Importing layout modifiers
import androidx.compose.foundation.shape.RoundedCornerShape // Importing shape definitions
import androidx.compose.foundation.text.KeyboardActions // Importing keyboard action handling
import androidx.compose.foundation.text.KeyboardOptions // Importing keyboard configuration
import androidx.compose.material.icons.Icons // Importing material icon sets
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Importing mirrored back arrow icon
import androidx.compose.material.icons.filled.Close // Importing close icon
import androidx.compose.material.icons.filled.History // Importing history icon
import androidx.compose.material.icons.filled.Search // Importing search icon
import androidx.compose.material3.* // Importing Material 3 components
import androidx.compose.runtime.* // Importing Compose runtime states
import androidx.compose.ui.Alignment // Importing alignment constants
import androidx.compose.ui.Modifier // Importing UI modifiers
import androidx.compose.ui.graphics.Color // Importing color handling
import androidx.compose.ui.platform.LocalFocusManager // Importing focus management
import androidx.compose.ui.text.font.FontWeight // Importing font weight styles
import androidx.compose.ui.text.input.ImeAction // Importing IME action types
import androidx.compose.ui.text.style.TextOverflow // Importing text overflow handling
import androidx.compose.ui.unit.dp // Importing density-independent pixels
import assignment1.krzysztofoko.s16001089.data.Book // Importing Book data model

@Composable // Marks function as a UI component
fun SearchBarComponent( // Component for the actual search input bar
    query: String, // The current text being searched
    onQueryChange: (String) -> Unit, // Callback when text changes
    onCloseClick: () -> Unit, // Callback to close the search bar
    modifier: Modifier = Modifier // Standard modifier parameter
) {
    val focusManager = LocalFocusManager.current // Handles keyboard and focus state

    Surface( // Background container for the search bar
        modifier = modifier // 
            .fillMaxWidth() // Makes bar take full width
            .height(56.dp) // Sets a fixed height
            .padding(horizontal = 8.dp, vertical = 4.dp), // Adds spacing around the bar
        color = MaterialTheme.colorScheme.surface, // Sets the background color
        shape = RoundedCornerShape(28.dp), // Rounds the corners to a pill shape
        tonalElevation = 8.dp, // Adds depth via color tint
        shadowElevation = 4.dp, // Adds a drop shadow
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) // Adds a subtle border
    ) {
        Row( // Horizontal layout for icons and text field
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), // 
            verticalAlignment = Alignment.CenterVertically // Aligns children in the middle vertically
        ) {
            Icon( // Visual search indicator
                imageVector = Icons.Default.Search, // 
                contentDescription = null, // 
                tint = MaterialTheme.colorScheme.primary // 
            )
            Spacer(modifier = Modifier.width(12.dp)) // Adds horizontal spacing
            TextField( // Input field for user typing
                value = query, // 
                onValueChange = onQueryChange, // 
                modifier = Modifier.weight(1f), // Takes up remaining horizontal space
                placeholder = { Text("Search books, courses...", style = MaterialTheme.typography.bodyMedium) }, // Hint text when empty
                colors = TextFieldDefaults.colors( // Customizes text field appearance
                    focusedContainerColor = Color.Transparent, // 
                    unfocusedContainerColor = Color.Transparent, // 
                    disabledContainerColor = Color.Transparent, // 
                    focusedIndicatorColor = Color.Transparent, // Removes bottom line
                    unfocusedIndicatorColor = Color.Transparent, // 
                ),
                singleLine = true, // Prevents multiple lines
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // Sets keyboard button to "Search"
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }), // Closes keyboard on search click
                textStyle = MaterialTheme.typography.bodyLarge // Sets the font style for input
            )
            
            if (query.isNotEmpty()) { // Only shows clear button if there is text
                TextButton( // Clickable text for clearing
                    onClick = { onQueryChange("") }, // Resets search text
                    contentPadding = PaddingValues(horizontal = 8.dp) // 
                ) {
                    Text("Clear", style = MaterialTheme.typography.labelLarge) // 
                }
            }
            
            IconButton(onClick = onCloseClick) { // Button to dismiss search bar
                Icon( // Visual back arrow
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // 
                    contentDescription = "Close search", // Accessibility label
                    tint = MaterialTheme.colorScheme.primary // 
                )
            }
        }
    }
}

@Composable // 
fun HomeSearchSection( // Higher-level component managing visibility and suggestions
    isSearchVisible: Boolean, // Controls if the search is displayed
    searchQuery: String, // 
    onQueryChange: (String) -> Unit, // 
    onCloseClick: () -> Unit, // 
    suggestions: List<Book>, // List of matching items
    onSuggestionClick: (Book) -> Unit, // Callback when a suggestion is selected
    modifier: Modifier = Modifier // 
) {
    Column(modifier = modifier) { // Vertical layout for bar and results
        AnimatedVisibility( // Animates search bar appearing/disappearing
            visible = isSearchVisible, // 
            enter = expandHorizontally( // Slide/expand animation from the start
                expandFrom = Alignment.Start, // 
                animationSpec = tween(durationMillis = 400) // 400ms duration
            ) + fadeIn(animationSpec = tween(durationMillis = 300)), // Combined with fade
            exit = shrinkHorizontally( // Shrink animation back to start
                shrinkTowards = Alignment.Start, // 
                animationSpec = tween(durationMillis = 400) // 
            ) + fadeOut(animationSpec = tween(durationMillis = 300)) // 
        ) {
            SearchBarComponent( // The input bar itself
                query = searchQuery, // 
                onQueryChange = onQueryChange, // 
                onCloseClick = onCloseClick // 
            )
        }

        AnimatedVisibility( // Animates the suggestions list
            visible = isSearchVisible && suggestions.isNotEmpty(), // Only show if visible and has results
            enter = expandVertically() + fadeIn(), // Drops down vertically
            exit = shrinkVertically() + fadeOut() // 
        ) {
            SearchSuggestionsList( // Displays the list of matching results
                items = suggestions, // 
                onItemClick = onSuggestionClick // 
            ) { book -> // Content for each suggestion row
                Row(verticalAlignment = Alignment.CenterVertically) { // 
                    Icon( // Suggestion/history icon
                        imageVector = Icons.Default.History, // 
                        contentDescription = null, // 
                        tint = MaterialTheme.colorScheme.outline, // 
                        modifier = Modifier.size(18.dp) // 
                    )
                    Spacer(Modifier.width(12.dp)) // 
                    Column { // Vertical info for the book
                        Text( // Book title
                            text = book.title, // 
                            style = MaterialTheme.typography.bodyMedium, // 
                            fontWeight = FontWeight.Bold, // 
                            maxLines = 1, // 
                            overflow = TextOverflow.Ellipsis // Adds "..." if too long
                        )
                        Text( // Book author
                            text = book.author, // 
                            style = MaterialTheme.typography.bodySmall, // 
                            color = MaterialTheme.colorScheme.onSurfaceVariant // 
                        )
                    }
                }
            }
        }
    }
}

@Composable // 
fun <T> SearchSuggestionsList( // Reusable generic list for suggestions
    items: List<T>, // 
    onItemClick: (T) -> Unit, // 
    modifier: Modifier = Modifier, // 
    itemContent: @Composable (T) -> Unit // lambda for rendering items
) {
    Surface( // Background for the list
        modifier = modifier // 
            .fillMaxWidth() // 
            .padding(horizontal = 12.dp) // 
            .animateContentSize(), // Animates size changes automatically
        shape = RoundedCornerShape(16.dp), // Rounds the list corners
        color = MaterialTheme.colorScheme.surface, // 
        tonalElevation = 12.dp, // 
        shadowElevation = 8.dp, // 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) // 
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) { // Vertical stacking of items
            items.forEachIndexed { index, item -> // Loops through suggestions
                Box(modifier = Modifier // Container for single item row
                    .fillMaxWidth() // 
                    .clickable { onItemClick(item) } // Makes row interactive
                    .padding(horizontal = 16.dp, vertical = 12.dp) // 
                ) {
                    itemContent(item) // Renders the provided item UI
                }
                if (index < items.size - 1) { // Adds dividers between items
                    HorizontalDivider( // Visual separator line
                        modifier = Modifier.padding(horizontal = 16.dp), // 
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) // 
                    )
                }
            }
        }
    }
}

@Composable // 
fun TopBarSearchAction( // Component for the search icon in the top bar
    isSearchVisible: Boolean, // 
    onSearchIconClick: () -> Unit // 
) {
    if (!isSearchVisible) { // Only shows the icon if search bar is hidden
        IconButton(onClick = onSearchIconClick) { // 
            Icon( // 
                imageVector = Icons.Default.Search, // 
                contentDescription = "Open Search" // 
            )
        }
    }
}
