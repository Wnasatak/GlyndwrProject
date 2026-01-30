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
import assignment1.krzysztofoko.s16001089.data.AudioBook
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

@Composable
fun AudioBookEditDialog(audioBook: AudioBook, onDismiss: () -> Unit, onSave: (AudioBook) -> Unit) {
    var title by remember { mutableStateOf(audioBook.title) }
    var author by remember { mutableStateOf(audioBook.author) }
    var price by remember { mutableStateOf(audioBook.price.toString()) }
    var description by remember { mutableStateOf(audioBook.description) }
    var imageUrl by remember { mutableStateOf(audioBook.imageUrl) }
    var audioUrl by remember { mutableStateOf(audioBook.audioUrl) }
    var category by remember { mutableStateOf(audioBook.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Audiobook Details", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                AsyncImage(model = formatAssetUrl(imageUrl), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Narrator/Author") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = audioUrl, onValueChange = { audioUrl = it }, label = { Text("Audio URL") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(audioBook.copy(
                    title = title, author = author, price = price.toDoubleOrNull() ?: audioBook.price, 
                    description = description, imageUrl = imageUrl, audioUrl = audioUrl, category = category
                )) 
            }) { Text("Save Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
