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
import assignment1.krzysztofoko.s16001089.data.AudioBook
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage

/**
 * AudioBookEditDialog.kt
 *
 * This file provides a comprehensive administrative interface for adding or modifying
 * audiobook resources. It features native system file pickers for real image and audio 
 * uploading, and is fully optimised for smartphones using adaptive design principles.
 */

/**
 * AudioBookEditDialog Composable
 *
 * The primary modal for administrative audiobook management.
 *
 * @param audioBook The [AudioBook] object to be edited (or a blank one for creation).
 * @param onDismiss Callback invoked when the user cancels the operation.
 * @param onSave Callback invoked with the updated [AudioBook] data upon confirmation.
 */
@Composable
fun AudioBookEditDialog(
    audioBook: AudioBook, 
    onDismiss: () -> Unit, 
    onSave: (AudioBook) -> Unit
) {
    // --- MODE & ADAPTIVE LOGIC --- //
    // Detect if we are in 'Create' or 'Edit' mode based on the initial audiobook state.
    val isCreateMode = audioBook.title.isEmpty() && audioBook.author.isEmpty() && audioBook.price == 0.0
    val isTablet = isTablet() // Responsive check for layout adjustments.

    // --- STATE MANAGEMENT --- //
    // Local state for form fields, synchronised with the provided audiobook data.
    var title by remember { mutableStateOf(audioBook.title) }
    var author by remember { mutableStateOf(audioBook.author) }
    var price by remember { mutableStateOf(if (isCreateMode) "" else audioBook.price.toString()) }
    var description by remember { mutableStateOf(audioBook.description) }
    var imageUrl by remember { mutableStateOf(audioBook.imageUrl) }
    var audioUrl by remember { mutableStateOf(audioBook.audioUrl) }
    var category by remember { mutableStateOf(audioBook.category) }

    // Control visibility for manual URL entry fields.
    var showUrlInputForImage by remember { mutableStateOf(false) }
    var showUrlInputForAudio by remember { mutableStateOf(false) }

    // --- NATIVE FILE PICKERS --- //
    // Launcher for selecting cover images from the device storage.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } }

    // Launcher for selecting audio files (MP3/M4A) from the device storage.
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { audioUrl = it.toString() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.94f),
        title = {
            // --- HEADER: Dialog Title and Icon --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCreateMode) Icons.Default.LibraryAdd else Icons.Default.Headphones, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(if (isTablet) 16.dp else 12.dp))
                Text(
                    text = if (isCreateMode) "Add New Audiobook" else "Edit Audiobook Details", 
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
                    .verticalScroll(rememberScrollState()) // Allow the form to scroll on small screens.
            ) {
                // --- SECTION: IMAGE PREVIEW & UPLOAD --- //
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(if (isTablet) 140.dp else 100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                // Live preview of the selected cover image.
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(8.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Placeholder icon if no image is selected.
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
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
                        // Launch system file picker.
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
                        
                        // Toggle manual URL input field.
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

                // --- SECTION: BASIC RESOURCE INFO --- //
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Audiobook Title", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = author, onValueChange = { author = it }, 
                    label = { Text("Narrator / Author", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption(),
                    singleLine = true
                )

                // Layout price and genre side-by-side on tablets, vertically on mobile.
                if (isTablet) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Genre", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    }
                } else {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
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

                // --- SECTION: DIGITAL ASSETS (Audio management) --- //
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Audio Resources", style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            // Launch system audio picker.
                            Button(
                                onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(if (isTablet) 40.dp else 36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.AudioFile, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Upload", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            
                            // Toggle manual audio path input.
                            OutlinedButton(
                                onClick = { showUrlInputForAudio = !showUrlInputForAudio },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(if (isTablet) 40.dp else 36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Manual", style = AdaptiveTypography.hint(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        
                        if (showUrlInputForAudio) {
                            OutlinedTextField(
                                value = audioUrl, onValueChange = { audioUrl = it }, 
                                label = { Text("Audio URL / Path", style = AdaptiveTypography.hint()) }, 
                                modifier = Modifier.fillMaxWidth(), 
                                shape = RoundedCornerShape(12.dp),
                                textStyle = AdaptiveTypography.caption(),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            // --- ACTION: SAVE RESOURCE --- //
            Button(
                onClick = { 
                    // Pack all local state back into the data model and trigger the save callback.
                    onSave(audioBook.copy(
                        title = title, author = author, price = price.toDoubleOrNull() ?: 0.0, 
                        description = description, imageUrl = imageUrl, audioUrl = audioUrl, category = category
                    )) 
                },
                enabled = title.isNotBlank() && author.isNotBlank(), // Basic validation.
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(if (isTablet) 50.dp else 44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(if (isCreateMode) "Add Audiobook" else "Save Changes", fontWeight = FontWeight.Bold, fontSize = if (isTablet) 16.sp else 14.sp) }
        },
        dismissButton = { 
            // --- ACTION: CANCEL --- //
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { 
                Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = AdaptiveTypography.caption()) 
            } 
        }
    )
}
