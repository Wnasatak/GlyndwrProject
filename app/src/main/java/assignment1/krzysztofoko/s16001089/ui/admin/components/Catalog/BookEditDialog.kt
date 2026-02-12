package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

/**
 * BookEditDialog provides a comprehensive administrative interface for adding or modifying
 * academic resources (books, etc.) in the catalog.
 */
@Composable
fun BookEditDialog(
    book: Book, 
    onDismiss: () -> Unit, 
    onSave: (Book) -> Unit
) {
    // Mode Detection
    val isCreateMode = book.title.isEmpty() && book.author.isEmpty() && book.price == 0.0

    // Local state management for form fields
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var price by remember { mutableStateOf(if (isCreateMode) "" else book.price.toString()) }
    var description by remember { mutableStateOf(book.description) }
    var imageUrl by remember { mutableStateOf(book.imageUrl) }
    var audioUrl by remember { mutableStateOf(book.audioUrl) }
    var pdfUrl by remember { mutableStateOf(book.pdfUrl) }
    var category by remember { mutableStateOf(book.category) }
    var mainCategory by remember { mutableStateOf(book.mainCategory) }
    var isAudioBook by remember { mutableStateOf(book.isAudioBook) }
    var isInstallmentAvailable by remember { mutableStateOf(book.isInstallmentAvailable) }
    var modulePrice by remember { mutableStateOf(if (isCreateMode) "" else book.modulePrice.toString()) }

    // UI visibility states
    var showUrlInputForImage by remember { mutableStateOf(false) }
    var showUrlInputForPdf by remember { mutableStateOf(false) }

    // NATIVE FILE PICKERS
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { pdfUrl = it.toString() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCreateMode) Icons.Default.AddBusiness else Icons.Default.Edit, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                @Suppress("DEPRECATION")
                Text(
                    text = if (isCreateMode) "Create New Book" else "Edit Book Details", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp), 
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // IMAGE PREVIEW & UPLOAD
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(140.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(12.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // THEMED PLACEHOLDER: Shown when no image is present
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = if (isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Upload Image", fontSize = 12.sp)
                        }
                        
                        OutlinedButton(
                            onClick = { showUrlInputForImage = !showUrlInputForImage },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Manual Path", fontSize = 12.sp)
                        }
                    }
                    
                    if (showUrlInputForImage || imageUrl.startsWith("content://")) {
                        OutlinedTextField(
                            value = imageUrl, onValueChange = { imageUrl = it },
                            label = { Text("Image Asset Path / URI") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            trailingIcon = { if (imageUrl.isNotEmpty()) IconButton(onClick = { imageUrl = "" }) { Icon(Icons.Default.Clear, null) } }
                        )
                    }
                }

                // BASIC INFO
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Book Title") }, leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(20.dp)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author / Narrator") }, leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) })
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Genre") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) })
                }

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // DIGITAL ASSETS
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Digital Resources", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Upload PDF", fontSize = 12.sp)
                                }
                                
                                OutlinedButton(
                                    onClick = { showUrlInputForPdf = !showUrlInputForPdf },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    @Suppress("DEPRECATION")
                                    Text("Manual Path", fontSize = 12.sp)
                                }
                            }
                            
                            if (showUrlInputForPdf || pdfUrl.startsWith("content://")) {
                                OutlinedTextField(value = pdfUrl, onValueChange = { pdfUrl = it }, label = { Text("Internal PDF Path / URI") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), textStyle = MaterialTheme.typography.bodySmall, trailingIcon = { if (pdfUrl.isNotEmpty()) IconButton(onClick = { pdfUrl = "" }) { Icon(Icons.Default.Clear, null) } } )
                            }
                        }

                        // AUDIOBOOK LOGIC
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isAudioBook, onCheckedChange = { isAudioBook = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                            @Suppress("DEPRECATION")
                            Text("Include Audiobook Support", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        if (isAudioBook) {
                            OutlinedTextField(value = audioUrl, onValueChange = { audioUrl = it }, label = { Text("Audio Stream URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Headphones, null, modifier = Modifier.size(18.dp)) })
                        }
                    }
                }

                // MODULAR PAYMENT LOGIC
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isInstallmentAvailable, onCheckedChange = { isInstallmentAvailable = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary))
                            @Suppress("DEPRECATION")
                            Text("Enable Modular Payment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        if (isInstallmentAvailable) {
                            OutlinedTextField(value = modulePrice, onValueChange = { modulePrice = it }, label = { Text("Price per Module (£)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(18.dp)) })
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
                        title = title, author = author, price = price.toDoubleOrNull() ?: 0.0,
                        description = description, imageUrl = imageUrl, audioUrl = audioUrl, 
                        pdfUrl = pdfUrl, category = category, mainCategory = mainCategory,
                        isAudioBook = isAudioBook, isInstallmentAvailable = isInstallmentAvailable,
                        modulePrice = modulePrice.toDoubleOrNull() ?: 0.0
                    )) 
                },
                enabled = title.isNotBlank() && author.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(if (isCreateMode) "Create Book" else "Save Changes", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { 
            @Suppress("DEPRECATION")
            TextButton(onClick = onDismiss) { 
                Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) 
            } 
        }
    )
}
