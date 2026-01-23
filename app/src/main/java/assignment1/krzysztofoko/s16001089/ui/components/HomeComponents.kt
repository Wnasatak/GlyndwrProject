package assignment1.krzysztofoko.s16001089.ui.components // folder where this file lives

import androidx.compose.animation.AnimatedVisibility // for smooth show/hide effects
import androidx.compose.foundation.BorderStroke // for drawing lines around boxes
import androidx.compose.foundation.background // to set colors or gradients behind things
import androidx.compose.foundation.layout.* // for padding, size, and spacing tools
import androidx.compose.foundation.lazy.LazyRow // a horizontal scrollable list
import androidx.compose.foundation.lazy.items // to loop through items in a list
import androidx.compose.foundation.lazy.rememberLazyListState // remembers scroll position
import androidx.compose.foundation.shape.RoundedCornerShape // for making corners round
import androidx.compose.material.icons.Icons // the standard icon library
import androidx.compose.material.icons.automirrored.filled.MenuBook // specific book icon
import androidx.compose.material.icons.filled.* // imports all other filled icons
import androidx.compose.material3.* // the main UI component library
import androidx.compose.runtime.* // for managing app "state" (memory)
import androidx.compose.ui.Alignment // for centering or aligning things
import androidx.compose.ui.Modifier // the main tool to change UI look/behavior
import androidx.compose.ui.graphics.Brush // for creating color gradients
import androidx.compose.ui.graphics.Color // for picking colors
import androidx.compose.ui.graphics.vector.ImageVector // for handling icon shapes
import androidx.compose.ui.text.font.FontWeight // for bold or thin text
import androidx.compose.ui.text.style.TextAlign // for centering text
import androidx.compose.ui.unit.dp // unit for measuring sizes
import androidx.compose.ui.unit.sp // unit for measuring text size
import kotlinx.coroutines.delay
import kotlin.math.abs // for calculating absolute numbers

@Composable // tells Android this is a UI piece
fun PromotionBanner(onRegisterClick: () -> Unit) { // banner for users not logged in
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) { // the outer card box
        Box(modifier = Modifier.background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))).padding(24.dp)) { // gradient background
            Column(modifier = Modifier.fillMaxWidth()) { // stacks text vertically
                Text("University Store", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold) // big main title
                Text("Exclusive 10% student discount applied locally.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f)) // subtext description
                Spacer(modifier = Modifier.height(20.dp)) // empty space before button
                Button(onClick = onRegisterClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Get Started", fontWeight = FontWeight.Bold) } // login/register button
            }
        }
    }
}

@Composable // UI piece for logged in students
fun MemberWelcomeBanner() { // the friendly box showing student status
    Surface( // a container that can have a border
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), // size and spacing
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), // light blue-ish background
        shape = RoundedCornerShape(16.dp), // round the corners
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) // thin subtle border
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { // horizontal layout
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) // "sparkle" icon
            Spacer(modifier = Modifier.width(12.dp)) // space after icon
            Column { // text one above the other
                Text(text = "Logged in as Student", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer) // bold main status
                Text(text = "10% discount activated! Enjoy your perks ✨", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)) // friendly discount text
            }
        }
    }
}

@Composable // horizontal bar for categories like "Books", "Gear"
fun MainCategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    // To create an infinite scroll, we use a very large number for item count
    // and map the index to our actual category list
    val infiniteCategories = Int.MAX_VALUE
    val startPosition = infiniteCategories / 2 - (infiniteCategories / 2 % categories.size)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startPosition)
    
    // Auto-scroll logic could be added here if needed, but the user asked for continuous spin while scrolling
    
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(infiniteCategories) { index ->
            val categoryIndex = index % categories.size
            val category = categories[categoryIndex]
            
            val scale by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo
                    val itemInfo = visibleItemsInfo.find { it.index == index }
                    
                    if (itemInfo != null) {
                        val center = layoutInfo.viewportEndOffset / 2
                        val itemCenter = itemInfo.offset + (itemInfo.size / 2)
                        val distanceFromCenter = abs(center - itemCenter).toFloat()
                        val normalizedDistance = (distanceFromCenter / center).coerceIn(0f, 1f)
                        1.25f - (normalizedDistance * 0.4f)
                    } else {
                        0.85f
                    }
                }
            }

            CategorySquareButton(
                label = category,
                icon = getMainCategoryIcon(category),
                isSelected = selectedCategory == category,
                scale = scale,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable // bar for sub-categories like "Technology", "Fantasy"
fun SubCategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { // smaller horizontal list
        items(categories) { category -> // loop through sub-categories
            CategoryChip(category = category, isSelected = selectedCategory == category, onCategorySelected = onCategorySelected) // pill-shaped button
        }
    }
}

private fun getMainCategoryIcon(category: String): ImageVector { // helper to pick icons
    return when (category) { // "when" is like a "switch" statement
        "All" -> Icons.Default.GridView // grid icon
        "Free" -> Icons.Default.Redeem // gift icon
        "University Courses" -> Icons.Default.School // cap icon
        "University Gear" -> Icons.Default.Checkroom // hanger icon
        "Books" -> Icons.AutoMirrored.Filled.MenuBook // book icon
        "Audio Books" -> Icons.Default.Headphones // headphones icon
        else -> Icons.Default.Category // generic category icon
    }
}
