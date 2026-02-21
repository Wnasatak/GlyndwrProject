package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage

/**
 * ProductComponents.kt
 *
 * This file contains a collection of reusable UI components designed to display product information
 * consistently throughout the application. It covers various layouts, from horizontal sliders
 * and detailed header images to flexible list items and quick-view dialogs.
 */

/**
 * UniversalProductSlider Composable
 *
 * A horizontal scrolling row for showcasing a collection of products.
 * It's ideal for "Featured" or "Recommended" sections where space is limited but
 * multiple items need to be glanceable.
 *
 * @param products The list of `Book` objects to be rendered.
 * @param onProductClick Callback invoked when a specific product card is tapped.
 */
@Composable
fun UniversalProductSlider(
    products: List<Book>,
    onProductClick: (Book) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(ProDesign.StandardPadding)
    ) {
        items(products) { item ->
            Card(
                modifier = Modifier
                    .width(140.dp) // Maintain a uniform card width.
                    .clickable { onProductClick(item) },
                shape = RoundedCornerShape(ProDesign.CompactPadding),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    // Item Thumbnail
                    AsyncImage(
                        model = formatAssetUrl(item.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    // Basic Metadata
                    Column(modifier = Modifier.padding(8.dp)) {
                        @Suppress("DEPRECATION")
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val priceText = if (item.price == 0.0) AppConstants.LABEL_FREE else "£${formatPrice(item.price)}"
                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * ProductHeaderImage Composable
 *
 * A high-end, immersive header for the product details screen.
 * Features an animated background gradient and conditional badges to show ownership status.
 *
 * Key features:
 * - **Dynamic Gradient:** Uses an infinite transition to shift a radial gradient across the image.
 * - **Contextual Styling:** Switches between rounded and rectangular shapes based on product category.
 * - **Ownership Feedback:** Displays status badges if the user owns the item.
 *
 * @param book The product data.
 * @param isOwned Whether the current user has already purchased/enrolled in this item.
 * @param isDarkTheme Flag to adjust border contrast.
 * @param primaryColor The theme's primary colour for the animated effect.
 */
@Composable
fun ProductHeaderImage(
    book: Book,
    isOwned: Boolean,
    isDarkTheme: Boolean,
    primaryColor: Color
) {
    // Shifting animation state for the background glow effect.
    val infiniteTransition = rememberInfiniteTransition(label = "shadeAnimation")
    val xPos by infiniteTransition.animateFloat(
        initialValue = -0.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(12000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "xPos"
    )
    val yPos by infiniteTransition.animateFloat(
        initialValue = -0.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(18000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "yPos"
    )

    // Merchandise (Gear) uses sharp corners for a distinct look.
    val headerShape = if (book.mainCategory == AppConstants.CAT_GEAR) RectangleShape else RoundedCornerShape(28.dp)

    Surface(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        shape = headerShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (book.imageUrl.isNotEmpty()) {
                AsyncImage(model = formatAssetUrl(book.imageUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            
            // Visual enhancements for non-merchandise items.
            if (book.mainCategory != AppConstants.CAT_GEAR) {
                // Background scrim.
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                // Animated glow.
                Box(modifier = Modifier.fillMaxSize().drawBehind {
                    val brush = Brush.radialGradient(colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent), center = Offset(size.width * xPos, size.height * yPos), radius = size.maxDimension * 0.8f)
                    drawRect(brush)
                })
                // Branded central icon indicating item type.
                Box(modifier = Modifier.align(Alignment.Center).size(110.dp).background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(28.dp)).drawBehind {
                    drawRoundRect(color = primaryColor.copy(alpha = 0.6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()), style = Stroke(width = 2.dp.toPx()))
                }, contentAlignment = Alignment.Center) {
                    Icon(imageVector = when { book.isAudioBook -> Icons.Default.Headphones; book.mainCategory == AppConstants.CAT_COURSES -> Icons.Default.School; else -> Icons.AutoMirrored.Filled.MenuBook }, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.3f)))))
            }
            
            // Display ownership status if the user is already enrolled or has purchased the item.
            if (isOwned) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(ProDesign.StandardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (book.price <= 0) {
                        // For free items
                        EnrollmentStatusBadge(status = "PICKED_UP")
                        EnrollmentStatusBadge(status = "IN_LIBRARY")
                    } else {
                        // For paid items
                        EnrollmentStatusBadge(status = "APPROVED") // Displays as "PAID"
                        EnrollmentStatusBadge(status = "IN_LIBRARY")
                    }
                }
            }
        }
    }
}

/**
 * BookItemCard Composable
 *
 * A highly versatile and modular list item card. It provides several "slots" 
 * (trailingContent, bottomContent, etc.) allowing it to be adapted for different sections 
 * of the app without duplicating code.
 *
 * @param book The `Book` object to display.
 * @param modifier Custom styling for the card container.
 * @param onClick Triggered on card tap.
 * @param imageOverlay Custom UI to render on top of the thumbnail.
 */
@Composable
fun BookItemCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageOverlay: @Composable (BoxScope.() -> Unit)? = null,
    cornerContent: @Composable (BoxScope.() -> Unit)? = null,
    topEndContent: @Composable (BoxScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null,
    statusBadge: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                // Thumbnail container with a subtle background and fallback icon.
                Box(modifier = Modifier.size(width = 90.dp, height = 120.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)))), contentAlignment = Alignment.Center) {
                    if (book.imageUrl.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(model = formatAssetUrl(book.imageUrl), contentDescription = book.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.65f)), startY = 0f)))
                        }
                    } else {
                        Icon(imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    }
                    imageOverlay?.invoke(this)
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Content Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        @Suppress("DEPRECATION")
                        Text(text = book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        trailingContent?.invoke(this)
                    }
                    @Suppress("DEPRECATION")
                    Text(text = "by ${book.author}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    bottomContent?.invoke(this)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Categories and dynamic badges.
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = if (book.isAudioBook) "Audio" else book.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        statusBadge?.invoke(this)
                    }
                }
            }
            // Absolute positioning zones for specific actions (e.g., delete buttons or top-level badges).
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { topEndContent?.invoke(this) }
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) { cornerContent?.invoke(this) }
        }
    }
}

/**
 * QuickViewDialog Composable
 *
 * A modal alert dialog providing a condensed summary of an item.
 * It allows users to quickly scan details without committing to a full page navigation.
 *
 * @param book The item to preview.
 * @param onDismiss Close the dialog.
 * @param onReadMore Navigate to the full product detail screen.
 */
@Composable
fun QuickViewDialog(
    book: Book,
    onDismiss: () -> Unit,
    onReadMore: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onDismiss(); onReadMore(book.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                @Suppress("DEPRECATION")
                Text(AppConstants.BTN_SIGN_IN_SHOP, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CLOSE) } },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(model = formatAssetUrl(book.imageUrl), contentDescription = null, modifier = Modifier.size(160.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(20.dp))
                @Suppress("DEPRECATION")
                Text(text = book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                @Suppress("DEPRECATION")
                Text(text = if (book.isAudioBook) "${AppConstants.TEXT_NARRATED_BY} ${book.author}" else "${AppConstants.TEXT_BY} ${book.author}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) { 
                    @Suppress("DEPRECATION") 
                    Text(text = book.description, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp, textAlign = TextAlign.Justify) 
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    val priceText = if (book.price == 0.0) AppConstants.LABEL_FREE else "£${formatPrice(book.price)}"
                    @Suppress("DEPRECATION")
                    Text(text = priceText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}
