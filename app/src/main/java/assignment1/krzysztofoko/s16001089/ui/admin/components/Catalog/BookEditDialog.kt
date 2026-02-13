package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage

/**
 * BookEditDialog provides a comprehensive administrative interface for adding or modifying
 * academic resources (books, etc.) in the catalog.
 * Now fully optimized for smartphones using centralized Adaptive typography and spacing.
 */
@Composable
fun BookEditDialog(
    book: Book, 
    onDismiss: () -> Unit, 
    onSave: (Book) -> Unit
) {
    // Mode Detection
    val isCreateMode = book.title.isEmpty() && book.author.isEmpty() && book.price == 0.0
    val isTablet = isTablet()

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
        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.94f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCreateMode) Icons.Default.AddBusiness else Icons.Default.Edit, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(if (isTablet) 16.dp else 12.dp))
                Text(
                    text = if (isCreateMode) "Create Book" else "Edit Book Details", 
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
                    .verticalScroll(rememberScrollState())
            ) {
                // IMAGE PREVIEW & UPLOAD
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(if (isTablet) 140.dp else 100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(8.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = if (isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
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
                        Button(
                            onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.height(if (isTablet) 40.dp else 36.dp).weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Upload", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        
                        OutlinedButton(
                            onClick = { showUrlInputForImage = !showUrlInputForImage },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(if (isTablet) 40.dp else 36.dp).weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Manual", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (showUrlInputForImage) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = imageUrl, onValueChange = { imageUrl = it },
                            label = { Text("Image URL", style = AdaptiveTypography.label()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = AdaptiveTypography.caption(),
                            singleLine = true
                        )
                    }
                }

                // BASIC INFO
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Book Title", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = author, onValueChange = { author = it }, 
                    label = { Text("Author", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption(),
                    singleLine = true
                )

                if (isTablet) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Genre", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    }
                } else {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Genre", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                }

                OutlinedTextField(
                    value = description, onValueChange = { description = it }, 
                    label = { Text("Description", style = AdaptiveTypography.label()) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 2, 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // DIGITAL ASSETS
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Digital Assets", style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(if (isTablet) 40.dp else 36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("PDF", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            
                            OutlinedButton(
                                onClick = { showUrlInputForPdf = !showUrlInputForPdf },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(if (isTablet) 40.dp else 36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Manual", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        if (showUrlInputForPdf) {
                            OutlinedTextField(
                                value = pdfUrl, onValueChange = { pdfUrl = it },
                                label = { Text("PDF URL", style = AdaptiveTypography.hint()) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = AdaptiveTypography.caption(),
                                singleLine = true
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isAudioBook, onCheckedChange = { isAudioBook = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Audiobook Support", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        if (isAudioBook) {
                            OutlinedTextField(value = audioUrl, onValueChange = { audioUrl = it }, label = { Text("Audio URL", style = AdaptiveTypography.hint()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Headphones, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                        }
                    }
                }

                // MODULAR PAYMENT
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isInstallmentAvailable, onCheckedChange = { isInstallmentAvailable = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Modular Payment", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        if (isInstallmentAvailable) {
                            OutlinedTextField(value = modulePrice, onValueChange = { modulePrice = it }, label = { Text("Price/Module (£)", style = AdaptiveTypography.hint()) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
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
                modifier = Modifier.fillMaxWidth().height(if (isTablet) 50.dp else 44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(if (isCreateMode) "Create Book" else "Save Changes", fontWeight = FontWeight.Bold, fontSize = if (isTablet) 16.sp else 14.sp) }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { 
                Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = AdaptiveTypography.caption()) 
            } 
        }
    )
}
