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
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

/**
 * CourseEditDialog provides a comprehensive administrative interface for adding or modifying
 * university courses. Features native system file pickers for real image uploading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditDialog(
    course: Course, 
    onDismiss: () -> Unit, 
    onSave: (Course) -> Unit
) {
    // Mode Detection
    val isCreateMode = course.title.isEmpty() && course.department.isEmpty() && course.price == 0.0

    // Local state management for form fields
    var title by remember { mutableStateOf(course.title) }
    var price by remember { mutableStateOf(if (isCreateMode) "" else course.price.toString()) }
    var description by remember { mutableStateOf(course.description) }
    var imageUrl by remember { mutableStateOf(course.imageUrl) }
    var category by remember { mutableStateOf(course.category) }
    var department by remember { mutableStateOf(course.department) }
    var isInstallmentAvailable by remember { mutableStateOf(course.isInstallmentAvailable) }
    var modulePrice by remember { mutableStateOf(if (isCreateMode) "" else course.modulePrice.toString()) }

    // UI visibility states
    var showUrlInputForImage by remember { mutableStateOf(false) }

    // NATIVE FILE PICKER
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } }

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
                    // HEADER with dynamic title and icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCreateMode) Icons.Default.LibraryAdd else Icons.Default.School, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text(
                            text = if (isCreateMode) "Create New Course" else "Edit Course Details", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // IMAGE PREVIEW & UPLOAD
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.size(140.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(12.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // THEMED PLACEHOLDER
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.School,
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
                                Text("Upload Cover", fontSize = 12.sp)
                            }
                            
                            OutlinedButton(onClick = { showUrlInputForImage = !showUrlInputForImage }, shape = RoundedCornerShape(8.dp)) {
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
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Course Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(20.dp)) })
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(20.dp)) })
                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) })
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) })
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Course Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))

                    Spacer(Modifier.height(20.dp))

                    // INSTALLMENT CONFIGURATION
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isInstallmentAvailable, onCheckedChange = { isInstallmentAvailable = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                                Text("Enable Installment Payments", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            if (isInstallmentAvailable) {
                                OutlinedTextField(value = modulePrice, onValueChange = { modulePrice = it }, label = { Text("Price per Module (£)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(18.dp)) })
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ACTION BAR
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) {
                            @Suppress("DEPRECATION")
                            Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { 
                                onSave(course.copy(
                                    title = title, price = price.toDoubleOrNull() ?: 0.0, 
                                    description = description, imageUrl = imageUrl, category = category,
                                    department = department, isInstallmentAvailable = isInstallmentAvailable,
                                    modulePrice = modulePrice.toDoubleOrNull() ?: 0.0
                                )) 
                            },
                            enabled = title.isNotBlank() && department.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(if (isCreateMode) "Create Course" else "Save Changes", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    )
}
