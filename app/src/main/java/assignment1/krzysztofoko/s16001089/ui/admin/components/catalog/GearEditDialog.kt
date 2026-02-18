package assignment1.krzysztofoko.s16001089.ui.admin.components.catalog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Gear
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage

/**
 * GearEditDialog.kt
 *
 * This component provides a specialized administrative interface for managing university gear, 
 * apparel, and merchandise. It supports complex product data including stock management, 
 * SKU tracking, and physical attributes like sizes and materials.
 *
 * Key Features:
 * - Dual Asset Support: Handles both primary and secondary product images via native file pickers.
 * - Inventory Tracking: Integrated fields for stock counts, SKU, and branding.
 * - Responsive Grid Layout: Adapts input field arrangements for phone and tablet form factors.
 * - Adaptive Typography: Utilizes project-wide adaptive styling for a consistent visual identity.
 */

/**
 * GearEditDialog Composable
 *
 * The primary modal for administrative gear and merchandise management.
 *
 * @param gear The [Gear] data model representing the item to be edited or created.
 * @param onDismiss Callback invoked when the user cancels or closes the dialog.
 * @param onSave Callback invoked with updated [Gear] data for persistence.
 */
@Composable
fun GearEditDialog(
    gear: Gear, 
    onDismiss: () -> Unit, 
    onSave: (Gear) -> Unit
) {
    // --- MODE & ADAPTIVE LOGIC --- //
    // Detect if we are in 'Create' or 'Edit' mode based on initial data state.
    val isCreateMode = gear.title.isEmpty() && gear.price == 0.0
    val isTablet = isTablet() // Centralized check for tablet UI optimizations.

    // --- STATE MANAGEMENT --- //
    // Local reactive state for all editable gear properties.
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

    // Visibility controls for manual URL input fields (alternative to file picking).
    var showUrlInputForPrimary by remember { mutableStateOf(false) }
    var showUrlInputForSecondary by remember { mutableStateOf(false) }

    // --- NATIVE FILE PICKERS --- //
    // Launcher for selecting the primary product image from device storage.
    val primaryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } }

    // Launcher for selecting an optional secondary (alternate) product image.
    val secondaryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { secondaryImageUrl = it.toString() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.94f),
        title = {
            // --- HEADER: Contextual Icon and Title --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCreateMode) Icons.Default.AddShoppingCart else Icons.Default.Checkroom, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(if (isTablet) 16.dp else 12.dp))
                Text(
                    text = if (isCreateMode) "Add New Gear" else "Edit Gear Details", 
                    style = AdaptiveTypography.headline(), 
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 10.dp), 
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Ensures form accessibility.
            ) {
                // --- SECTION: PRIMARY IMAGE PREVIEW & UPLOAD --- //
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(if (isTablet) 140.dp else 100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                // Live preview of the product's primary visual.
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = "Primary Gear Image", 
                                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(8.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Default icon for items without an image.
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Checkroom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(if (isTablet) 48.dp else 32.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Triggers system picker for primary product image.
                        Button(
                            onClick = { primaryPickerLauncher.launch(arrayOf("image/*")) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.height(if (isTablet) 40.dp else 36.dp).weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Upload", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        
                        // Manual URL input toggle.
                        OutlinedButton(
                            onClick = { showUrlInputForPrimary = !showUrlInputForPrimary },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(if (isTablet) 40.dp else 36.dp).weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Manual", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    
                    if (showUrlInputForPrimary) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = imageUrl, onValueChange = { imageUrl = it },
                            label = { Text("Primary Image URL", style = AdaptiveTypography.label()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = AdaptiveTypography.caption(),
                            singleLine = true
                        )
                    }
                }

                // --- SECTION: BASIC INFO --- //
                // Core product identification.
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Product Title", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption(),
                    singleLine = true
                )
                
                // Adaptive layout for Brand and SKU fields.
                if (isTablet) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Label, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.QrCode, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    }
                } else {
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Label, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.QrCode, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                }

                // --- SECTION: PRICING & STOCK --- //
                // Financial and availability tracking.
                if (isTablet) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = stockCount, onValueChange = { stockCount = it }, label = { Text("Stock", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    }
                } else {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    OutlinedTextField(value = stockCount, onValueChange = { stockCount = it }, label = { Text("Stock", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                }

                // Marketing Description.
                OutlinedTextField(
                    value = description, onValueChange = { description = it }, 
                    label = { Text("Description", style = AdaptiveTypography.label()) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 2, 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // --- SECTION: PRODUCT SPECS --- //
                // Specialized physical attributes sub-form.
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Product Specifications", style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category", style = AdaptiveTypography.hint()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material", style = AdaptiveTypography.hint()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = sizes, onValueChange = { sizes = it }, label = { Text("Sizes", style = AdaptiveTypography.hint()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = colors, onValueChange = { colors = it }, label = { Text("Colors", style = AdaptiveTypography.hint()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = AdaptiveTypography.caption(), singleLine = true)
                    }
                }

                // --- SECTION: SECONDARY ASSETS --- //
                // Management of supplementary product views.
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Secondary Assets", style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            // Secondary system picker.
                            Button(
                                onClick = { secondaryPickerLauncher.launch(arrayOf("image/*")) }, 
                                shape = RoundedCornerShape(8.dp), 
                                modifier = Modifier.weight(1f).height(if (isTablet) 40.dp else 36.dp), 
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Image", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            // Secondary manual URL toggle.
                            OutlinedButton(
                                onClick = { showUrlInputForSecondary = !showUrlInputForSecondary }, 
                                shape = RoundedCornerShape(8.dp), 
                                modifier = Modifier.weight(1f).height(if (isTablet) 40.dp else 36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Manual", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (showUrlInputForSecondary) {
                            OutlinedTextField(value = secondaryImageUrl, onValueChange = { secondaryImageUrl = it }, label = { Text("Secondary Image URL", style = AdaptiveTypography.hint()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = AdaptiveTypography.caption(), singleLine = true)
                        }
                    }
                }

                // Featured Store Listing Flag.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Featured in Store", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            // --- ACTION: PERSIST CHANGES --- //
            Button(
                onClick = { 
                    // Maps local form state back to the [Gear] data model.
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
                enabled = title.isNotBlank() && price.isNotBlank(), // Requirement validation.
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(if (isTablet) 50.dp else 44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(if (isCreateMode) "Add Gear" else "Save Changes", fontWeight = FontWeight.Bold, fontSize = if (isTablet) 16.sp else 14.sp) }
        },
        dismissButton = { 
            // --- ACTION: CANCEL --- //
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { 
                Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = AdaptiveTypography.caption()) 
            } 
        }
    )
}
