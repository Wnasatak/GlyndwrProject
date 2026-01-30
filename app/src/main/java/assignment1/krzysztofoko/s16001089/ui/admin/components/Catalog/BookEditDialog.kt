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
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

@Composable
fun BookEditDialog(book: Book, onDismiss: () -> Unit, onSave: (Book) -> Unit) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var price by remember { mutableStateOf(book.price.toString()) }
    var description by remember { mutableStateOf(book.description) }
    var imageUrl by remember { mutableStateOf(book.imageUrl) }
    var audioUrl by remember { mutableStateOf(book.audioUrl) }
    var pdfUrl by remember { mutableStateOf(book.pdfUrl) }
    var category by remember { mutableStateOf(book.category) }
    var mainCategory by remember { mutableStateOf(book.mainCategory) }
    var isAudioBook by remember { mutableStateOf(book.isAudioBook) }
    var isInstallmentAvailable by remember { mutableStateOf(book.isInstallmentAvailable) }
    var modulePrice by remember { mutableStateOf(book.modulePrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Book Details", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                AsyncImage(
                    model = formatAssetUrl(imageUrl), 
                    contentDescription = null, 
                    modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp)), 
                    contentScale = ContentScale.Crop
                )
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Genre)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mainCategory, onValueChange = { mainCategory = it }, label = { Text("Main Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL (Asset Path)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = pdfUrl, onValueChange = { pdfUrl = it }, label = { Text("PDF Asset Path") }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAudioBook, onCheckedChange = { isAudioBook = it })
                    Text("Is Audiobook")
                }
                if (isAudioBook) {
                    OutlinedTextField(value = audioUrl, onValueChange = { audioUrl = it }, label = { Text("Audio Asset Path") }, modifier = Modifier.fillMaxWidth())
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isInstallmentAvailable, onCheckedChange = { isInstallmentAvailable = it })
                    Text("Installments Available")
                }
                if (isInstallmentAvailable) {
                    OutlinedTextField(value = modulePrice, onValueChange = { modulePrice = it }, label = { Text("Module Price") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(book.copy(
                    title = title, 
                    author = author, 
                    price = price.toDoubleOrNull() ?: book.price, 
                    description = description, 
                    imageUrl = imageUrl, 
                    audioUrl = audioUrl, 
                    pdfUrl = pdfUrl, 
                    category = category, 
                    mainCategory = mainCategory,
                    isAudioBook = isAudioBook,
                    isInstallmentAvailable = isInstallmentAvailable,
                    modulePrice = modulePrice.toDoubleOrNull() ?: book.modulePrice
                )) 
            }) { Text("Save Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
