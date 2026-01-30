package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Gear
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

@Composable
fun GearEditDialog(gear: Gear, onDismiss: () -> Unit, onSave: (Gear) -> Unit) {
    var title by remember { mutableStateOf(gear.title) }
    var price by remember { mutableStateOf(gear.price.toString()) }
    var description by remember { mutableStateOf(gear.description) }
    var imageUrl by remember { mutableStateOf(gear.imageUrl) }
    var category by remember { mutableStateOf(gear.category) }
    var sizes by remember { mutableStateOf(gear.sizes) }
    var colors by remember { mutableStateOf(gear.colors) }
    var stockCount by remember { mutableStateOf(gear.stockCount.toString()) }
    var brand by remember { mutableStateOf(gear.brand) }
    var material by remember { mutableStateOf(gear.material) }
    var sku by remember { mutableStateOf(gear.sku) }
    var originalPrice by remember { mutableStateOf(gear.originalPrice.toString()) }
    var isFeatured by remember { mutableStateOf(gear.isFeatured) }
    var productTags by remember { mutableStateOf(gear.productTags) }
    var secondaryImageUrl by remember { mutableStateOf(gear.secondaryImageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Gear Details", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                AsyncImage(model = formatAssetUrl(imageUrl), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Primary Image URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = secondaryImageUrl, onValueChange = { secondaryImageUrl = it }, label = { Text("Secondary Image URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = originalPrice, onValueChange = { originalPrice = it }, label = { Text("Original Price (for Sale)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stockCount, onValueChange = { stockCount = it }, label = { Text("Stock Count") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sizes, onValueChange = { sizes = it }, label = { Text("Sizes (comma separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = colors, onValueChange = { colors = it }, label = { Text("Colors (comma separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = productTags, onValueChange = { productTags = it }, label = { Text("Tags") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Text("Featured Product")
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(gear.copy(
                    title = title, price = price.toDoubleOrNull() ?: gear.price, 
                    description = description, imageUrl = imageUrl, category = category,
                    sizes = sizes, colors = colors, stockCount = stockCount.toIntOrNull() ?: gear.stockCount,
                    brand = brand, material = material, sku = sku, 
                    originalPrice = originalPrice.toDoubleOrNull() ?: gear.originalPrice,
                    isFeatured = isFeatured, productTags = productTags,
                    secondaryImageUrl = if (secondaryImageUrl.isEmpty()) null else secondaryImageUrl
                )) 
            }) { Text("Save Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
