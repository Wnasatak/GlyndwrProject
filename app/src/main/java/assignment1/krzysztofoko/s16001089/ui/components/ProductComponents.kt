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
 * This file contains a collection of reusable Composables designed to display product information
 * throughout the application. These components are optimized for consistency and visual appeal,
 * covering various use cases from product sliders to detailed header images and list items.
 */


/**
 * A horizontal scrolling list for showcasing products (e.g., books, courses, gear).
 * Ideal for "Featured" or "Recommended" sections on a home or dashboard screen.
 *
 * @param products The list of `Book` objects to be displayed in the slider.
 * @param onProductClick A lambda function invoked when a user taps on any product in the slider.
 */
@Composable
fun UniversalProductSlider(
    products: List<Book>,
    onProductClick: (Book) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(ProDesign.StandardPadding) // Spacing between items
    ) {
        items(products) { item ->
            Card(
                modifier = Modifier
                    .width(140.dp) // Fixed width for a uniform look
                    .clickable { onProductClick(item) },
                shape = RoundedCornerShape(ProDesign.CompactPadding),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    // Product Image
                    AsyncImage(
                        model = formatAssetUrl(item.imageUrl),
                        contentDescription = null, // Decorative image
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Crop // Ensures the image fills the space
                    )
                    // Product Details (Title & Price)
                    Column(modifier = Modifier.padding(8.dp)) {
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
 * A sophisticated header image for the product details screen. It features a dynamic,
 * animated background and displays ownership status.
 *
 * @param book The `Book` data to render in the header.
 * @param isOwned A boolean indicating if the current user owns this product.
 * @param isDarkTheme A boolean to adjust border colors for the current theme.
 * @param primaryColor The primary color of the current theme, used for the animated gradient.
 */
@Composable
fun ProductHeaderImage(
    book: Book,
    isOwned: Boolean,
    isDarkTheme: Boolean,
    primaryColor: Color
) {
    // Infinite transition for a subtle, shifting background gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "shadeAnimation")
    val xPos by infiniteTransition.animateFloat(
        initialValue = -0.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "xPos"
    )
    val yPos by infiniteTransition.animateFloat(
        initialValue = -0.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(18000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "yPos"
    )

    // Use a rectangular shape for gear, rounded for all other product types
    val headerShape = if (book.mainCategory == AppConstants.CAT_GEAR) RectangleShape else RoundedCornerShape(28.dp)

    Surface(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        shape = headerShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (book.imageUrl.isNotEmpty()) {
                AsyncImage(model = formatAssetUrl(book.imageUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            
            // Overlays for non-gear items to improve text readability and visual flair
            if (book.mainCategory != AppConstants.CAT_GEAR) {
                // Scrim for contrast
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                // Animated radial gradient
                Box(modifier = Modifier.fillMaxSize().drawBehind {
                    val brush = Brush.radialGradient(colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent), center = Offset(size.width * xPos, size.height * yPos), radius = size.maxDimension * 0.8f)
                    drawRect(brush)
                })
                // Central icon indicating product type
                Box(modifier = Modifier.align(Alignment.Center).size(110.dp).background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(28.dp)).drawBehind {
                    drawRoundRect(color = primaryColor.copy(alpha = 0.6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()), style = Stroke(width = 2.dp.toPx()))
                }, contentAlignment = Alignment.Center) {
                    Icon(imageVector = when { book.isAudioBook -> Icons.Default.Headphones; book.mainCategory == AppConstants.CAT_COURSES -> Icons.Default.School; else -> Icons.AutoMirrored.Filled.MenuBook }, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
                }
            } else {
                // Lighter scrim for gear items
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.3f)))))
            }
            
            // Display ownership status badges if the user owns the item
            if (isOwned) {
                Row(modifier = Modifier.fillMaxWidth().padding(ProDesign.StandardPadding), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (book.price > 0) { // Paid items get an "APPROVED" badge
                        EnrollmentStatusBadge(status = "APPROVED") 
                    } else { // Keep layout consistent for free items
                        Spacer(Modifier.width(1.dp)) 
                    }
                    EnrollmentStatusBadge(status = if (book.mainCategory == AppConstants.CAT_COURSES) "ENROLLED" else "APPROVED")
                }
            }
        }
    }
}

/**
 * A highly flexible and customizable card for displaying a book in a list format.
 * It supports various content slots to inject custom UI elements.
 *
 * @param book The `Book` object to display.
 * @param modifier The modifier to be applied to the card.
 * @param onClick Lambda triggered when the card is clicked.
 * @param imageOverlay A composable slot to draw content over the book's image.
 * @param cornerContent A composable slot for content at the bottom-end corner of the card.
 * @param topEndContent A composable slot for content at the top-end corner.
 * @param trailingContent A composable slot for content to the right of the book title.
 * @param bottomContent A composable slot for content below the book's author.
 * @param statusBadge A composable slot for adding status indicators (e.g., "New", "Popular").
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
                // Image container with a fallback icon
                Box(modifier = Modifier.size(width = 90.dp, height = 120.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)))), contentAlignment = Alignment.Center) {
                    if (book.imageUrl.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(model = formatAssetUrl(book.imageUrl), contentDescription = book.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            // Gradient overlay for better text visibility if content is placed on top
                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.65f)), startY = 0f)))
                        }
                    } else {
                        Icon(imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    }
                    // Custom overlay content injection
                    imageOverlay?.invoke(this)
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Text content section
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        @Suppress("DEPRECATION")
                        Text(text = book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        trailingContent?.invoke(this) // Trailing content slot
                    }
                    @Suppress("DEPRECATION")
                    Text(text = "by ${book.author}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    bottomContent?.invoke(this) // Bottom content slot
                    Spacer(modifier = Modifier.height(8.dp))
                    // Category and status badges
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
                        statusBadge?.invoke(this) // Status badge slot
                    }
                }
            }
            // Injecting content into specific alignment zones
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { topEndContent?.invoke(this) }
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) { cornerContent?.invoke(this) }
        }
    }
}

/**
 * A dialog that provides a quick, summarized view of a product.
 * It shows key details and offers navigation to the full product page.
 *
 * @param book The product (`Book`) to display in the dialog.
 * @param onDismiss Lambda function to call when the dialog is dismissed.
 * @param onReadMore Lambda function that navigates to the detailed product view, passing the book's ID.
 */
@Composable
fun QuickViewDialog(
    book: Book,
    onDismiss: () -> Unit,
    onReadMore: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // Confirm button navigates to the full details screen
        confirmButton = {
            Button(onClick = { onDismiss(); onReadMore(book.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                @Suppress("DEPRECATION")
                Text(AppConstants.BTN_SIGN_IN_SHOP, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        },
        // Dismiss button simply closes the dialog
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CLOSE) } },
        title = null, // Custom title layout is used
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                // Product Image
                AsyncImage(model = formatAssetUrl(book.imageUrl), contentDescription = null, modifier = Modifier.size(160.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(20.dp))
                // Title and Author
                @Suppress("DEPRECATION")
                Text(text = book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                @Suppress("DEPRECATION")
                Text(text = if (book.isAudioBook) "${AppConstants.TEXT_NARRATED_BY} ${book.author}" else "${AppConstants.TEXT_BY} ${book.author}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                // Description Snippet
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) { @Suppress("DEPRECATION") Text(text = book.description, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp, textAlign = TextAlign.Justify) }
                Spacer(modifier = Modifier.height(16.dp))
                // Price Display
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    val priceText = if (book.price == 0.0) AppConstants.LABEL_FREE else "£${formatPrice(book.price)}"
                    @Suppress("DEPRECATION")
                    Text(text = priceText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}
