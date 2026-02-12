package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Edit Academic Resource", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp), 
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Preview Card
                Surface(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    AsyncImage(
                        model = formatAssetUrl(imageUrl), 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(12.dp)), 
                        contentScale = ContentScale.Crop
                    )
                }

                // Fields with Icons
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Book Title") }, 
                    leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = author, onValueChange = { author = it }, 
                    label = { Text("Author / Narrator") }, 
                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = price, onValueChange = { price = it }, 
                        label = { Text("Price (£)") }, 
                        leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = category, onValueChange = { category = it }, 
                        label = { Text("Genre") }, 
                        leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description, onValueChange = { description = it }, 
                    label = { Text("Description") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Grouped Logic: Audiobook
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isAudioBook, 
                                onCheckedChange = { isAudioBook = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text("Include Audiobook Support", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        if (isAudioBook) {
                            OutlinedTextField(
                                value = audioUrl, onValueChange = { audioUrl = it }, 
                                label = { Text("Audio Stream URL") }, 
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Headphones, null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }

                // Grouped Logic: Modular Payments
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isInstallmentAvailable, 
                                onCheckedChange = { isInstallmentAvailable = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                            )
                            Text("Enable Modular Payment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        if (isInstallmentAvailable) {
                            OutlinedTextField(
                                value = modulePrice, onValueChange = { modulePrice = it }, 
                                label = { Text("Price per Module (£)") }, 
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onSave(book.copy(
                        title = title, author = author, price = price.toDoubleOrNull() ?: book.price,
                        description = description, imageUrl = imageUrl, audioUrl = audioUrl, 
                        pdfUrl = pdfUrl, category = category, mainCategory = mainCategory,
                        isAudioBook = isAudioBook, isInstallmentAvailable = isInstallmentAvailable,
                        modulePrice = modulePrice.toDoubleOrNull() ?: book.modulePrice
                    )) 
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Save Changes", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) 
            } 
        }
    )
}
