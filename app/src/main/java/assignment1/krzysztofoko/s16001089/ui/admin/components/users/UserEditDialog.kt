package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import assignment1.krzysztofoko.s16001089.data.UserLocal
import coil.compose.AsyncImage

/**
 * UserEditDialog.kt
 *
 * This component provides a comprehensive administrative interface for creating or modifying 
 * institutional user accounts. It manages sensitive profile data including contact 
 * information, financial balances, and system-wide roles.
 *
 * Key Features:
 * - Reactive Form State: Manages temporary buffers for all user profile fields.
 * - Image Picker Integration: Allows admins to upload profile photos directly from the device gallery.
 * - Dynamic Role Configuration: Employs a Material 3 Exposed Dropdown for strictly governed role assignment.
 * - Adaptive Layout: Incorporates a scrollable column to ensure the large form remains accessible on compact screens.
 */

/**
 * Main form for user account administration.
 *
 * @param user The initial user data to populate the form (or a blank template for new users).
 * @param isNew Flag determining if the dialog is in "Creation" mode or "Edit" mode.
 * @param onDismiss Callback invoked when the admin cancels the operation.
 * @param onSave Callback invoked with the updated [UserLocal] object upon confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDialog(
    user: UserLocal,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserLocal) -> Unit
) {
    // --- FORM BUFFERS ---
    // These states hold the current input values before they are committed to the database.
    var name by remember { mutableStateOf(user.name) }
    var title by remember { mutableStateOf(user.title ?: "") }
    var email by remember { mutableStateOf(user.email) }
    var address by remember { mutableStateOf(user.address ?: "") }
    var photoUrl by remember { mutableStateOf(user.photoUrl ?: "") }
    var balance by remember { mutableStateOf(user.balance.toString()) }
    var role by remember { mutableStateOf(user.role) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber ?: "") }

    // --- DROPDOWN & MEDIA LOGIC ---
    var roleExpanded by remember { mutableStateOf(false) }
    val roles = listOf("student", "teacher", "tutor", "admin")

    // Activity Result Launcher for selecting a profile image from the system gallery.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUrl = it.toString() } // Store the local URI as the new photo source.
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // High-impact context icon (PersonAdd for new, otherwise Person).
                Icon(
                    imageVector = if (isNew) Icons.Default.PersonAdd else Icons.Default.ManageAccounts, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                @Suppress("DEPRECATION")
                Text(
                    text = if (isNew) "Create New User" else "Edit User Account", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- AVATAR SELECTION AREA ---
                // Interactive profile picture component with an integrated "Edit" badge.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { launcher.launch("image/*") },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (photoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile Preview",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AddAPhoto, "Change Photo", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    // Small floating badge indicating the avatar is editable.
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.padding(6.dp).size(16.dp), tint = Color.White)
                    }
                }

                // --- CORE PROFILE INFORMATION ---
                
                // Institutional Title (e.g., Professor, Dr., Mr.)
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Title (Prof, Dr, etc.)") }, 
                    leadingIcon = { Icon(Icons.Default.Badge, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Full Legal Name
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, 
                    label = { Text("Full Name") }, 
                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Email Address (Primary key - usually locked for existing users)
                OutlinedTextField(
                    value = email, onValueChange = { email = it }, 
                    label = { Text("Email Address") }, 
                    leadingIcon = { Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), 
                    enabled = isNew,
                    shape = RoundedCornerShape(12.dp)
                )

                // --- SYSTEM ROLE ASSIGNMENT ---
                // strictly controlled dropdown to prevent invalid role strings.
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Institutional Role") },
                        leadingIcon = { Icon(Icons.Default.Shield, null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.replaceFirstChar { it.uppercase() }) },
                                onClick = { 
                                    role = r
                                    roleExpanded = false 
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                // --- FINANCIAL & CONTACT INFO ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Manual Balance Adjustment (for corrections or scholarships)
                    OutlinedTextField(
                        value = balance, onValueChange = { balance = it }, 
                        label = { Text("Wallet Balance (£)") }, 
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    // Institutional Contact Number
                    OutlinedTextField(
                        value = phoneNumber, onValueChange = { phoneNumber = it }, 
                        label = { Text("Phone Number") }, 
                        leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Residential or Postal Address
                OutlinedTextField(
                    value = address, onValueChange = { address = it }, 
                    label = { Text("Primary Address") }, 
                    leadingIcon = { Icon(Icons.Default.HomeWork, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            // Validation: Ensure core identity fields are populated before saving.
            Button(
                enabled = name.isNotEmpty() && email.isNotEmpty(),
                onClick = {
                    onSave(user.copy(
                        name = name,
                        title = title.ifEmpty { null },
                        email = email,
                        role = role,
                        balance = balance.toDoubleOrNull() ?: user.balance,
                        phoneNumber = phoneNumber.ifEmpty { null },
                        address = address.ifEmpty { null },
                        photoUrl = photoUrl.ifEmpty { null }
                    ))
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { 
                @Suppress("DEPRECATION")
                Text(
                    text = if (isNew) "Confirm Registration" else "Commit Changes", 
                    fontWeight = FontWeight.Bold
                ) 
            }
        },
        dismissButton = { 
            @Suppress("DEPRECATION")
            TextButton(onClick = onDismiss) { 
                Text(
                    text = "Discard Edits", 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold
                ) 
            } 
        }
    )
}
