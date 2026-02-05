package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Gear
import assignment1.krzysztofoko.s16001089.ui.theme.StoreFreeGreen
import assignment1.krzysztofoko.s16001089.ui.theme.StoreLowStockOrange
import assignment1.krzysztofoko.s16001089.ui.theme.StoreStockGreen
import coil.compose.AsyncImage
import java.util.Locale
import java.util.UUID

@Composable
fun GearImageGallery(
    images: List<String>,
    selectedImageIndex: Int,
    onImageClick: (Int) -> Unit,
    isFeatured: Boolean,
    title: String
) {
    Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
        AsyncImage(
            model = images.getOrNull(selectedImageIndex) ?: "",
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        if (isFeatured) {
            Surface(
                modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "FEATURED", 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 10.sp
                )
            }
        }

        if (images.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (index == selectedImageIndex) Color.White else Color.White.copy(alpha = 0.5f))
                            .clickable { onImageClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun GearHeaderSection(gear: Gear) {
    Row(verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = gear.brand, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(text = gear.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (gear.originalPrice > gear.price) {
                Text(
                    text = "£${String.format(Locale.US, "%.2f", gear.originalPrice)}",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                    color = Color.Gray
                )
            }
            if (gear.price > 0) {
                Text(text = "£${String.format(Locale.US, "%.2f", gear.price)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            } else {
                Text(text = "FREE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = StoreFreeGreen)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GearTagsSection(tags: String) {
    if (tags.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.split(",").forEach { tag ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "#$tag", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun GearOptionSelectors(
    sizes: String,
    selectedSize: String,
    onSizeSelected: (String) -> Unit,
    colors: String,
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    onColorClick: (String) -> Unit
) {
    Column {
        if (sizes.isNotEmpty() && sizes != "One Size" && sizes != "Default") {
            Text(text = "Size: $selectedSize", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sizes.split(",")) { size ->
                    Surface(
                        modifier = Modifier.size(width = 60.dp, height = 45.dp).clickable { onSizeSelected(size) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedSize == size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (selectedSize == size) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = size, fontWeight = FontWeight.Bold, color = if (selectedSize == size) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (colors.isNotEmpty() && colors != "Default") {
            Text(text = "Color: $selectedColor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(colors.split(",")) { color ->
                    Surface(
                        modifier = Modifier.height(45.dp).padding(horizontal = 4.dp).clickable { 
                            onColorSelected(color)
                            onColorClick(color)
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = if (selectedColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (selectedColor == color) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(text = color, fontWeight = FontWeight.Bold, color = if (selectedColor == color) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GearStockIndicator(
    stockCount: Int,
    quantity: Int,
    isOwned: Boolean,
    isFree: Boolean,
    onQuantityChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = if (stockCount > 5) StoreStockGreen.copy(alpha = 0.1f) else StoreLowStockOrange.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Inventory, null, 
                    modifier = Modifier.size(14.dp), 
                    tint = if (stockCount > 5) StoreStockGreen else StoreLowStockOrange
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (stockCount > 0) "$stockCount in stock" else "Out of stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (stockCount > 5) StoreStockGreen else StoreLowStockOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (!isOwned && stockCount > 0 && !isFree) {
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.RemoveCircleOutline, null)
                }
                Text(text = quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                IconButton(onClick = { if (quantity < stockCount) onQuantityChange(quantity + 1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircleOutline, null)
                }
            }
        }
    }
}

@Composable
fun GearSpecsCard(material: String, sku: String, category: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SpecRow("Material", material)
            SpecRow("SKU", sku)
            SpecRow("Collection", category)
            SpecRow("Pick-up", "Wrexham Student Hub")
        }
    }
}

@Composable
fun GearBottomActionBar(
    isOwned: Boolean,
    price: Double,
    stockCount: Int,
    quantity: Int,
    isLoggedIn: Boolean,
    onViewInvoice: () -> Unit,
    onPickupInfo: () -> Unit,
    onLoginRequired: () -> Unit,
    onCheckout: () -> Unit,
    onFreePickup: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)).padding(16.dp).navigationBarsPadding()) {
        if (isOwned) {
            if (price > 0) {
                Button(onClick = onViewInvoice, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, null)
                    Spacer(Modifier.width(12.dp))
                    Text("View Invoice", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onPickupInfo,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Where to pickup?", fontWeight = FontWeight.Bold)
                }
            }
        } else if (!isLoggedIn) {
            Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Sign In to Shop")
            }
        } else if (stockCount > 0) {
            if (price > 0) {
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Checkout • £${String.format(Locale.US, "%.2f", price)}", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onFreePickup,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StoreFreeGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FrontHand, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Pick it up for FREE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Out of Stock")
            }
        }
    }
}

@Composable
fun SimilarProductsSlider(
    products: List<Gear>,
    onProductClick: (Gear) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { item ->
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onProductClick(item) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "£${String.format(java.util.Locale.US, "%.2f", item.price)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PickupInfoDialog(orderConfirmation: String? = null, onDismiss: () -> Unit) {
    val displayOrderNumber = orderConfirmation ?: remember { UUID.randomUUID().toString().take(8).uppercase() }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
        title = { Text(text = "Pick-up Location", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Please visit the Wrexham Student Hub to collect your item.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Order Confirmation #", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = if (displayOrderNumber.startsWith("WREX-")) displayOrderNumber else "WREX-$displayOrderNumber", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Hub Hours:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text(text = "Mon-Fri: 9:00 AM - 5:00 PM", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Sat-Sun: Closed", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Show this confirmation number and your student ID to the staff at the hub.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Got it!")
            }
        }
    )
}

@Composable
fun FreeOrderConfirmationDialog(
    gearTitle: String,
    onDismiss: () -> Unit
) {
    val randomOrderNumber = remember { UUID.randomUUID().toString().take(8).uppercase() }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StoreFreeGreen, modifier = Modifier.size(48.dp)) },
        title = { Text(text = "Order Confirmed!", fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "You have successfully claimed your:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = gearTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Order Confirmation #", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = "WREX-$randomOrderNumber", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Next Steps:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Go to Wrexham Student Hub\n2. Show your student ID\n3. Collect your items!",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Awesome!")
            }
        }
    )
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
