package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Gear
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

/**
 * GearEditDialog provides a comprehensive administrative interface for adding or modifying
 * institutional gear and apparel. Features native system file picking for real image uploading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearEditDialog(
    gear: Gear, 
    onDismiss: () -> Unit, 
    onSave: (Gear) -> Unit
) {
    // Mode Detection: Create vs Edit
    val isCreateMode = gear.title.isEmpty() && gear.price == 0.0

    // Local state management for form fields initialized with existing data
    var title by remember { mutableStateOf(gear.title) }
    var price by remember { mutableStateOf(if (isCreateMode) "" else gear.price.toString()) }
    var description by remember { mutableStateOf(gear.description) }
    var imageUrl by remember { mutableStateOf(gear.imageUrl) }
    var category by remember { mutableStateOf(gear.category) }
    var sizes by remember { mutableStateOf(gear.sizes) }
    var colors by remember { mutableStateOf(gear.colors) }
    var stockCount by remember { mutableStateOf(if (isCreateMode) "" else gear.stockCount.toString()) }
    var brand by remember { mutableStateOf(gear.brand) }
    var material by remember { mutableStateOf(gear.material) }
    var sku by remember { mutableStateOf(gear.sku) }
    var originalPrice by remember { mutableStateOf(if (isCreateMode) "" else gear.originalPrice.toString()) }
    var isFeatured by remember { mutableStateOf(gear.isFeatured) }
    var productTags by remember { mutableStateOf(gear.productTags) }
    var secondaryImageUrl by remember { mutableStateOf(gear.secondaryImageUrl ?: "") }

    // UI visibility states for manual paths
    var showUrlInputForPrimary by remember { mutableStateOf(false) }
    var showUrlInputForSecondary by remember { mutableStateOf(false) }

    // NATIVE FILE PICKERS
    val primaryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } }

    val secondaryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { secondaryImageUrl = it.toString() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp).widthIn(max = 500.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // HEADER section with dynamic iconography
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCreateMode) Icons.Default.AddShoppingCart else Icons.Default.Checkroom, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            text = if (isCreateMode) "Add New Gear" else "Edit Gear Details", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // PRIMARY IMAGE PREVIEW & UPLOAD
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.size(140.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(12.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // THEMED PLACEHOLDER
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Checkroom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { primaryPickerLauncher.launch(arrayOf("image/*")) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Upload Photo", fontSize = 12.sp)
                            }
                            
                            OutlinedButton(onClick = { showUrlInputForPrimary = !showUrlInputForPrimary }, shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Manual Path", fontSize = 12.sp)
                            }
                        }
                        
                        AnimatedVisibility(visible = showUrlInputForPrimary || imageUrl.startsWith("content://")) {
                            OutlinedTextField(
                                value = imageUrl, onValueChange = { imageUrl = it },
                                label = { Text("Primary Image Path / URI") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                trailingIcon = { if (imageUrl.isNotEmpty()) IconButton(onClick = { imageUrl = "" }) { Icon(Icons.Default.Clear, null) } }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // BASIC INFO
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Product Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(20.dp)) })
                    Spacer(Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Label, null, modifier = Modifier.size(18.dp)) })
                        OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.QrCode, null, modifier = Modifier.size(18.dp)) })
                    }

                    Spacer(Modifier.height(16.dp))

                    // PRICING & STOCK
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) })
                        OutlinedTextField(value = stockCount, onValueChange = { stockCount = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp)) })
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))

                    Spacer(Modifier.height(20.dp))

                    // PRODUCT SPECS
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Product Specifications", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(value = sizes, onValueChange = { sizes = it }, label = { Text("Available Sizes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(value = colors, onValueChange = { colors = it }, label = { Text("Available Colors") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // SECONDARY ASSETS
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Secondary Assets", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(onClick = { secondaryPickerLauncher.launch(arrayOf("image/*")) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Add Image", fontSize = 12.sp)
                                }
                                OutlinedButton(onClick = { showUrlInputForSecondary = !showUrlInputForSecondary }, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Manual Path", fontSize = 12.sp)
                                }
                            }
                            AnimatedVisibility(visible = showUrlInputForSecondary || secondaryImageUrl.startsWith("content://")) {
                                OutlinedTextField(value = secondaryImageUrl, onValueChange = { secondaryImageUrl = it }, label = { Text("Secondary Image URI") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = MaterialTheme.typography.bodySmall, trailingIcon = { if (secondaryImageUrl.isNotEmpty()) IconButton(onClick = { secondaryImageUrl = "" }) { Icon(Icons.Default.Clear, null) } })
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                        Text("Featured in University Store", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(32.dp))

                    // ACTION BAR
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) {
                            Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { 
                                onSave(gear.copy(
                                    title = title, price = price.toDoubleOrNull() ?: 0.0, 
                                    description = description, imageUrl = imageUrl, category = category,
                                    sizes = sizes, colors = colors, stockCount = stockCount.toIntOrNull() ?: 0,
                                    brand = brand, material = material, sku = sku, 
                                    originalPrice = originalPrice.toDoubleOrNull() ?: 0.0,
                                    isFeatured = isFeatured, productTags = productTags,
                                    secondaryImageUrl = if (secondaryImageUrl.isEmpty()) null else secondaryImageUrl
                                )) 
                            },
                            enabled = title.isNotBlank() && price.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(if (isCreateMode) "Add Gear" else "Save Changes", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    )
}
