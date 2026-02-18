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
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage

/**
 * BookEditDialog.kt
 *
 * This component provides a comprehensive administrative interface for adding or modifying
 * academic resources (books, etc.) in the catalog.
 */

@Composable
fun BookEditDialog(
    book: Book, 
    onDismiss: () -> Unit, 
    onSave: (Book) -> Unit
) {
    // Determine if we are creating a new entry or editing an existing one by checking if fields are empty
    val isCreateMode = book.title.isEmpty() && book.author.isEmpty() && book.price == 0.0
    val isTablet = isTablet() // Responsive check for layout adjustments

    // --- STATE MANAGEMENT --- //
    // Local state for all editable book fields, initialised with current book data
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

    // Toggle visibility for manual URL entry fields if file picker isn't preferred
    var showUrlInputForImage by remember { mutableStateOf(false) }
    var showUrlInputForPdf by remember { mutableStateOf(false) }

    // --- NATIVE FILE LAUNCHERS --- //
    // Launcher for selecting cover images from the device gallery/storage
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } } // Convert URI to string for state

    // Launcher for selecting PDF documents from the device storage
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { pdfUrl = it.toString() } } // Store selected document path

    AlertDialog(
        onDismissRequest = onDismiss, // Handle clicks outside the dialog
        containerColor = MaterialTheme.colorScheme.surface, // Background color
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()), // Rounded corners from theme
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.94f),
        title = {
            // --- HEADER: Dialog Icon and Title --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), // Light primary tint
                    shape = CircleShape,
                    modifier = Modifier.size(AdaptiveDimensions.MediumAvatar)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCreateMode) Icons.Default.AddBusiness else Icons.Default.Edit, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(AdaptiveDimensions.SmallIconSize)
                        )
                    }
                }
                Spacer(Modifier.width(AdaptiveSpacing.small())) // Horizontal spacing
                Text(
                    text = if (isCreateMode) "Create Book" else "Edit Book Details", 
                    style = AdaptiveTypography.headline(), 
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small()), 
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Allow scrolling if content is too tall
            ) {
                // --- SECTION: IMAGE PREVIEW & ASSET UPLOAD --- //
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(if (isTablet) 140.dp else 100.dp),
                            shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                // Display preview of the selected image URL or URI
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(8.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Default placeholder icon based on book type
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = if (isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(AdaptiveDimensions.MediumIconSize)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(AdaptiveSpacing.small())) // Vertical gap
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Launch gallery to pick an image file
                        Button(
                            onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(40.dp).weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Upload", style = AdaptiveTypography.hint(), fontWeight = FontWeight.Bold)
                        }
                        
                        // Toggle manual URL input field
                        OutlinedButton(
                            onClick = { showUrlInputForImage = !showUrlInputForImage },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp).weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Manual", style = AdaptiveTypography.hint())
                        }
                    }

                    if (showUrlInputForImage) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = imageUrl, onValueChange = { imageUrl = it },
                            label = { Text("Image URL", style = AdaptiveTypography.label()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = AdaptiveTypography.body(),
                            singleLine = true
                        )
                    }
                }

                // --- SECTION: CORE BOOK DATA --- //
                // Title Input
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Book Title", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(AdaptiveDimensions.SmallIconSize)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.body(),
                    singleLine = true
                )
                
                // Author Input
                OutlinedTextField(
                    value = author, onValueChange = { author = it }, 
                    label = { Text("Author", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(AdaptiveDimensions.SmallIconSize)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.body(),
                    singleLine = true
                )

                // Row for Price and Genre
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = price, onValueChange = { price = it }, 
                        label = { Text("Price", style = AdaptiveTypography.label()) }, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(12.dp), 
                        leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) }, 
                        textStyle = AdaptiveTypography.body(), 
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = category, onValueChange = { category = it }, 
                        label = { Text("Genre", style = AdaptiveTypography.label()) }, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(12.dp), 
                        leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) }, 
                        textStyle = AdaptiveTypography.body(), 
                        singleLine = true
                    )
                }

                // Description Multi-line Input
                OutlinedTextField(
                    value = description, onValueChange = { description = it }, 
                    label = { Text("Description", style = AdaptiveTypography.label()) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 3, 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.body()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // --- SECTION: DIGITAL ASSETS (PDF & AUDIO) --- //
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Digital Assets", style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            // Launch PDF picker to select academic document
                            Button(
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("PDF", style = AdaptiveTypography.hint(), fontWeight = FontWeight.Bold)
                            }
                            
                            // Toggle manual link entry for PDF path
                            OutlinedButton(
                                onClick = { showUrlInputForPdf = !showUrlInputForPdf },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Manual", style = AdaptiveTypography.hint())
                            }
                        }

                        // Flag to indicate if this book includes an audio version
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isAudioBook, onCheckedChange = { isAudioBook = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(8.dp))
                            Text("Audiobook Support", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            // --- ACTION: PERSIST DATA --- //
            Button(
                onClick = { 
                    // Update the book data model with all local form states and trigger save
                    onSave(book.copy(
                        title = title, author = author, price = price.toDoubleOrNull() ?: 0.0,
                        description = description, imageUrl = imageUrl, audioUrl = audioUrl, 
                        pdfUrl = pdfUrl, category = category, mainCategory = mainCategory,
                        isAudioBook = isAudioBook, isInstallmentAvailable = isInstallmentAvailable,
                        modulePrice = modulePrice.toDoubleOrNull() ?: 0.0
                    )) 
                },
                enabled = title.isNotBlank() && author.isNotBlank(), // Enforce required title/author
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(AdaptiveDimensions.StandardButtonHeight),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(if (isCreateMode) "Create Book" else "Save Changes", fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader()) }
        },
        dismissButton = { 
            // --- ACTION: CANCEL --- //
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { 
                Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = AdaptiveTypography.label()) 
            } 
        }
    )
}
