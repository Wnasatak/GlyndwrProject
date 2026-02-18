package assignment1.krzysztofoko.s16001089.ui.admin.components.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage
import java.util.*

/**
 * CatalogShared.kt
 *
 * This file contains reusable UI components specifically designed for the Administrative 
 * Catalog management module. It focuses on providing a consistent visual language for 
 * inventory lists, section categorisation, and destructive action confirmations.
 *
 * Key Design Principles:
 * 1. Visual Consistency: Uses shared shapes, elevations, and color palettes.
 * 2. Information Density: Cards are designed to show critical data (price, title, status) at a glance.
 * 3. Safey: destructive actions are protected by explicit user confirmation steps.
 */

/**
 * CatalogItemCard Composable
 *
 * A highly versatile card used to display individual items in the administrative inventory list.
 * It adapts its layout based on whether it represents a Book, Audiobook, Course, or Gear.
 *
 * @param title The primary name of the catalog item.
 * @param subtitle secondary information (e.g., Author, Department, or Stock count).
 * @param price The monetary value of the item. Displays "FREE" if zero.
 * @param imageUrl The web URL or local path to the item's thumbnail image.
 * @param icon A representative icon for the item category (e.g., School for Courses).
 * @param onEdit Lambda executed when the user clicks the edit (pencil) icon.
 * @param onDelete Lambda executed when the user clicks the delete (trash) icon.
 * @param isDarkTheme Flag to adjust container and border colors for theme compatibility.
 * @param onClick Optional override for the card's tap behavior (e.g., navigating to full details).
 */
@Composable
fun CatalogItemCard(
    title: String,
    subtitle: String,
    price: Double,
    imageUrl: String,
    icon: ImageVector,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDarkTheme: Boolean,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // The card responds to taps by either opening the detail screen or triggering edit mode.
            .clickable { onClick?.invoke() ?: onEdit() },
        shape = RoundedCornerShape(20.dp), // Rounded corners for a modern, approachable feel.
        colors = CardDefaults.cardColors(
            // Adjust transparency in dark mode to allow the wavy background to bleed through slightly.
            containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            else MaterialTheme.colorScheme.surface
        ),
        // subtle border to separate cards in dense lists.
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- IMAGE PREVIEW --- //
            // Loads and crops the product image into a standard square thumbnail.
            AsyncImage(
                model = formatAssetUrl(imageUrl),
                contentDescription = "Preview of $title",
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp)) // Rounds image corners to match card style.
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.width(16.dp)) // horizontal gutter between image and text content.
            
            // --- TEXT CONTENT AREA --- //
            Column(modifier = Modifier.weight(1f)) {
                // Main Title: bold and prominent.
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Subtitle: smaller and greyed out for hierarchy.
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
                
                Spacer(Modifier.height(4.dp))
                
                // --- PRICE & CATEGORY INDICATOR --- //
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small icon denoting the type of product.
                    Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    // Formats price to 2 decimal places with currency symbol.
                    Text(
                        text = if (price > 0) "£${String.format(Locale.US, "%.2f", price)}" else "FREE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // --- ACTION BUTTONS --- //
            Row {
                // Secondary action: Modify the resource.
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit Resource", tint = MaterialTheme.colorScheme.primary)
                }
                // High-visibility action: Remove the resource (styled in error color).
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, "Delete Resource", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * CatalogSectionHeader Composable
 *
 * A stylized header used to mark the beginning of a new product category in the list.
 * Includes a numerical badge to show total items in that category.
 *
 * @param title The text to display (e.g., "Academic Books").
 * @param count The number of items available in this section.
 * @param modifier Custom modifiers for external layout control.
 */
@Composable
fun CatalogSectionHeader(title: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Section Title: Uppercased with increased letter spacing for a 'Table of Contents' look.
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        // Numerical Badge: Displays the count in a rounded primary-colored container.
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * CatalogDeleteDialog Composable
 *
 * A specialized confirmation modal for destructive operations.
 * To prevent accidental deletions, it implements a 'Confirmation Keyword' pattern where the 
 * user must manually type "DELETE" to enable the confirm button.
 *
 * @param itemName The display name of the item to be deleted (e.g., "Physics Textbook").
 * @param onDismiss Handler for when the user clicks Cancel or outside the dialog.
 * @param onConfirm Handler for the final deletion execution.
 */
@Composable
fun CatalogDeleteDialog(itemName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    // Local state to track the user's confirmation input.
    var confirmationText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = { 
            // --- HEADER: Warning Icon and Cautionary Title --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.error, 
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Delete $itemName", 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.error
                ) 
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Explanatory message emphasizing the permanent nature of the action.
                Text(
                    text = "Are you sure you want to permanently remove this $itemName? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Instructions for the safety check.
                Text(
                    text = "To confirm, please type DELETE below:", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                // Confirmation Input Field.
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    placeholder = { Text("DELETE") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    // Field borders reflect the error state of the operation.
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        },
        confirmButton = {
            // Confirmation Button: Only enabled if the user typed the exact safety keyword.
            Button(
                onClick = onConfirm,
                enabled = confirmationText.uppercase() == "DELETE",
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text("Confirm Deletion", fontWeight = FontWeight.Bold) 
            }
        },
        dismissButton = { 
            // Safe exit button.
            TextButton(onClick = onDismiss) { 
                Text(
                    text = "Cancel", 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold
                ) 
            } 
        }
    )
}
