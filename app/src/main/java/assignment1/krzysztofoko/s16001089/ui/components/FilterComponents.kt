package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * FilterComponents.kt
 *
 * A collection of bespoke selection components used for categorising and filtering
 * content throughout the application. These components are designed to provide a 
 * professional and interactive experience, helping users narrow down their searches efficiently.
 */

/**
 * CategoryChip Composable
 *
 * A modern, pill-shaped filter chip for selecting content categories.
 * It's built upon the Material 3 `FilterChip` but with customised styling to match the
 * application's aesthetic.
 *
 * @param category The text label for the category.
 * @param isSelected Boolean flag to toggle the chip's selected visual state.
 * @param onCategorySelected Callback invoked when the user taps the chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onCategorySelected: (String) -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = { onCategorySelected(category) },
        label = {
            Text(
                text = category,
                // Colour transitions based on the selection state.
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            selectedContainerColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * CategorySquareButton Composable
 *
 * A more prominent, icon-driven button for top-level category selection.
 * This component is ideal for large grids or carousels where categories need a strong visual identity.
 *
 * Key Features:
 * - **Dynamic Scaling:** Supports a `scale` parameter for use in animations (e.g., in a "Cover Flow" style row).
 * - **Visual Feedback:** Uses distinct elevations, borders, and colour shifts to clearly indicate the selected state.
 * - **Label Control:** Handles multi-line labels with ellipsis to ensure a tidy layout.
 *
 * @param label The category title displayed below the icon.
 * @param icon The ImageVector icon representing the category.
 * @param isSelected Whether this category is currently active.
 * @param scale Float multiplier for the button's size, useful for focus effects.
 * @param onClick Callback triggered on button tap.
 */
@Composable
fun CategorySquareButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    scale: Float = 1f,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            // Apply a scaling transformation for focus animations.
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Surface(
            modifier = Modifier
                .size(60.dp)
                .clickable(
                    // Using a null indication to handle custom selection feedback externally.
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = if (isSelected) 8.dp else 2.dp,
            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        @Suppress("DEPRECATION")
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            lineHeight = 12.sp
        )
    }
}
