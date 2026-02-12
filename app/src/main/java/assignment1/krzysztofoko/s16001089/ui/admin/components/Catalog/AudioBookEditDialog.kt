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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.AudioBook
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

/**
 * AudioBookEditDialog provides a comprehensive administrative interface for adding or modifying
 * audiobook resources. Features native system file pickers for real image and audio uploading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioBookEditDialog(
    audioBook: AudioBook, 
    onDismiss: () -> Unit, 
    onSave: (AudioBook) -> Unit
) {
    // Detect mode: Create vs Edit
    val isCreateMode = audioBook.title.isEmpty() && audioBook.author.isEmpty() && audioBook.price == 0.0

    // Local state management for form fields
    var title by remember { mutableStateOf(audioBook.title) }
    var author by remember { mutableStateOf(audioBook.author) }
    var price by remember { mutableStateOf(if (isCreateMode) "" else audioBook.price.toString()) }
    var description by remember { mutableStateOf(audioBook.description) }
    var imageUrl by remember { mutableStateOf(audioBook.imageUrl) }
    var audioUrl by remember { mutableStateOf(audioBook.audioUrl) }
    var category by remember { mutableStateOf(audioBook.category) }

    // UI visibility states
    var showUrlInputForImage by remember { mutableStateOf(false) }
    var showUrlInputForAudio by remember { mutableStateOf(false) }

    // NATIVE FILE PICKERS
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { audioUrl = it.toString() } }

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
                    // HEADER section with dynamic mode-based iconography
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCreateMode) Icons.Default.LibraryAdd else Icons.Default.Headphones, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            text = if (isCreateMode) "Add New Audiobook" else "Edit Audiobook Details", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // IMAGE PREVIEW & UPLOAD
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
                                // THEMED PLACEHOLDER
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
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
                        
                        AnimatedVisibility(visible = showUrlInputForImage || imageUrl.startsWith("content://")) {
                            OutlinedTextField(
                                value = imageUrl, onValueChange = { imageUrl = it },
                                label = { Text("Image Path / URI") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                trailingIcon = { if (imageUrl.isNotEmpty()) IconButton(onClick = { imageUrl = "" }) { Icon(Icons.Default.Clear, null) } }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // BASIC INFO
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Audiobook Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(20.dp)) })
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Narrator / Author") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp)) })
                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) })
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Genre") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) })
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))

                    Spacer(Modifier.height(20.dp))

                    // DIGITAL ASSETS: Audio management
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Audio Resources", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AudioFile, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Upload Audio", fontSize = 12.sp)
                                }
                                
                                OutlinedButton(
                                    onClick = { showUrlInputForAudio = !showUrlInputForAudio },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Manual Path", fontSize = 12.sp)
                                }
                            }
                            
                            AnimatedVisibility(visible = showUrlInputForAudio || audioUrl.startsWith("content://")) {
                                OutlinedTextField(
                                    value = audioUrl, onValueChange = { audioUrl = it }, 
                                    label = { Text("Audio Path / URI") }, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    trailingIcon = { if (audioUrl.isNotEmpty()) IconButton(onClick = { audioUrl = "" }) { Icon(Icons.Default.Clear, null) } }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ACTIONS
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) {
                            Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { 
                                onSave(audioBook.copy(
                                    title = title, author = author, price = price.toDoubleOrNull() ?: 0.0, 
                                    description = description, imageUrl = imageUrl, audioUrl = audioUrl, category = category
                                )) 
                            },
                            enabled = title.isNotBlank() && author.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(if (isCreateMode) "Add Audiobook" else "Save Changes", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    )
}
