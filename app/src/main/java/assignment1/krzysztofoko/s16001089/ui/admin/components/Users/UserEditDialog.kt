package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDialog(
    user: UserLocal,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserLocal) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var title by remember { mutableStateOf(user.title ?: "") }
    var email by remember { mutableStateOf(user.email) }
    var address by remember { mutableStateOf(user.address ?: "") }
    var photoUrl by remember { mutableStateOf(user.photoUrl ?: "") }
    var balance by remember { mutableStateOf(user.balance.toString()) }
    var role by remember { mutableStateOf(user.role) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber ?: "") }

    // Role Dropdown State
    var roleExpanded by remember { mutableStateOf(false) }
    val roles = listOf("student", "teacher", "tutor", "admin")

    // Image Picker Integration
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUrl = it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(if (isNew) "Create New User" else "Edit User Account", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Friendly Photo Selector
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
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.padding(6.dp).size(16.dp), tint = Color.White)
                    }
                }

                // Friendly Input Group
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, 
                    label = { Text("Title (Prof, Dr, etc.)") }, 
                    leadingIcon = { Icon(Icons.Default.Badge, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, 
                    label = { Text("Full Name") }, 
                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email, onValueChange = { email = it }, 
                    label = { Text("Email Address") }, 
                    leadingIcon = { Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), 
                    enabled = isNew,
                    shape = RoundedCornerShape(12.dp)
                )

                // Role Dropdown Selector
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("System Role") },
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = balance, onValueChange = { balance = it }, 
                        label = { Text("Balance (£)") }, 
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = phoneNumber, onValueChange = { phoneNumber = it }, 
                        label = { Text("Phone") }, 
                        leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = address, onValueChange = { address = it }, 
                    label = { Text("Home Address") }, 
                    leadingIcon = { Icon(Icons.Default.HomeWork, null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
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
            ) { Text(if (isNew) "Create User" else "Save Changes", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) 
            } 
        }
    )
}
