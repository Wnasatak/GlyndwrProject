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
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage

/**
 * CourseEditDialog.kt
 *
 * This component provides a comprehensive administrative interface for managing university courses.
 * It facilitates the creation of new courses and the modification of existing ones, including
 * support for digital assets (images) via native system file pickers.
 *
 * Key Features:
 * - Native Image Integration: Uses ActivityResultContracts for secure device storage access.
 * - Responsive UI: Adjusts spacing, font sizes, and layouts for both smartphones and tablets.
 * - Installment Logic: Built-in configuration for payment plans and module pricing.
 * - Adaptive Design: Leverages centralized theme components for consistent branding.
 */

/**
 * CourseEditDialog Composable
 *
 * The primary modal for administrative course management.
 *
 * @param course The [Course] data model to populate the form.
 * @param onDismiss Callback to close the dialog without saving changes.
 * @param onSave Callback to persist the updated [Course] object back to the database.
 */
@Composable
fun CourseEditDialog(
    course: Course, 
    onDismiss: () -> Unit, 
    onSave: (Course) -> Unit
) {
    // --- MODE & ADAPTIVE LOGIC --- //
    // Determines if we are adding a fresh record or editing an existing one by checking key fields.
    val isCreateMode = course.title.isEmpty() && course.department.isEmpty() && course.price == 0.0
    val isTablet = isTablet() // Responsive check for specialized layout adjustments.

    // --- STATE MANAGEMENT --- //
    // Local state variables synchronized with the [Course] data model for reactive form updates.
    var title by remember { mutableStateOf(course.title) }
    var price by remember { mutableStateOf(if (isCreateMode) "" else course.price.toString()) }
    var description by remember { mutableStateOf(course.description) }
    var imageUrl by remember { mutableStateOf(course.imageUrl) }
    var category by remember { mutableStateOf(course.category) }
    var department by remember { mutableStateOf(course.department) }
    var isInstallmentAvailable by remember { mutableStateOf(course.isInstallmentAvailable) }
    var modulePrice by remember { mutableStateOf(if (isCreateMode) "" else course.modulePrice.toString()) }

    // State to toggle the manual URL input field if the user prefers typing over file picking.
    var showUrlInputForImage by remember { mutableStateOf(false) }

    // --- NATIVE FILE PICKER --- //
    // Launcher for selecting course promotional images from the device's gallery or file system.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { imageUrl = it.toString() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.94f),
        title = {
            // --- HEADER: Dialog Icon and Title --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCreateMode) Icons.Default.LibraryAdd else Icons.Default.School, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(if (isTablet) 16.dp else 12.dp))
                Text(
                    text = if (isCreateMode) "Create New Course" else "Edit Course Details", 
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
                    .verticalScroll(rememberScrollState()) // Ensures accessibility on smaller screens.
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
                                // Renders the selected image with a standard crop and rounded corners.
                                AsyncImage(
                                    model = formatAssetUrl(imageUrl), 
                                    contentDescription = "Course preview", 
                                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(8.dp)), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Default placeholder icon for courses.
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.School,
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
                        // Triggers the system file picker for images.
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
                        
                        // Toggles manual URL input for remote image hosting.
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

                // --- SECTION: BASIC COURSE INFO --- //
                // Primary Course Title
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Course Title", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.Title, null, modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption(),
                    singleLine = true
                )
                
                // Academic Department
                OutlinedTextField(
                    value = department, onValueChange = { department = it }, 
                    label = { Text("Department", style = AdaptiveTypography.label()) }, 
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption(),
                    singleLine = true
                )

                // Responsive Pricing and Category layout.
                if (isTablet) {
                    // On wide screens, display fields side-by-side.
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category", style = AdaptiveTypography.label()) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    }
                } else {
                    // On mobile screens, stack fields vertically to maintain readability.
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (£)", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category", style = AdaptiveTypography.label()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(16.dp)) }, textStyle = AdaptiveTypography.caption(), singleLine = true)
                }

                // Extensive Course Description
                OutlinedTextField(
                    value = description, onValueChange = { description = it }, 
                    label = { Text("Description", style = AdaptiveTypography.label()) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 2, 
                    shape = RoundedCornerShape(12.dp), 
                    textStyle = AdaptiveTypography.caption()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // --- SECTION: INSTALLMENT CONFIGURATION --- //
                // Specialized sub-form for managing payment plans.
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isInstallmentAvailable, onCheckedChange = { isInstallmentAvailable = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Installment Payments", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        // Only show module pricing if installments are enabled.
                        if (isInstallmentAvailable) {
                            OutlinedTextField(
                                value = modulePrice, 
                                onValueChange = { modulePrice = it }, 
                                label = { Text("Price/Module (£)", style = AdaptiveTypography.hint()) }, 
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                                shape = RoundedCornerShape(12.dp), 
                                leadingIcon = { Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(18.dp)) }, 
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
            // --- ACTION: PERSIST DATA --- //
            Button(
                onClick = { 
                    // Pack the local form state back into the [Course] domain model and execute save.
                    onSave(course.copy(
                        title = title, price = price.toDoubleOrNull() ?: 0.0, 
                        description = description, imageUrl = imageUrl, category = category,
                        department = department, isInstallmentAvailable = isInstallmentAvailable,
                        modulePrice = modulePrice.toDoubleOrNull() ?: 0.0
                    )) 
                },
                enabled = title.isNotBlank() && department.isNotBlank(), // Enforce basic validation rules.
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(if (isTablet) 50.dp else 44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(if (isCreateMode) "Create Course" else "Save Changes", fontWeight = FontWeight.Bold, fontSize = if (isTablet) 16.sp else 14.sp) }
        },
        dismissButton = { 
            // --- ACTION: CANCEL --- //
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { 
                Text(if (isCreateMode) "Cancel" else "Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = AdaptiveTypography.caption()) 
            } 
        }
    )
}
