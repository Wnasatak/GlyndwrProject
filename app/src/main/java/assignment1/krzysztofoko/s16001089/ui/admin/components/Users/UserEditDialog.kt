package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import coil.compose.AsyncImage

@Composable
fun UserEditDialog(
    user: UserLocal,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserLocal) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var address by remember { mutableStateOf(user.address ?: "") }
    var photoUrl by remember { mutableStateOf(user.photoUrl ?: "") }
    var balance by remember { mutableStateOf(user.balance.toString()) }
    var role by remember { mutableStateOf(user.role) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Create New User" else "Edit User Account", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Text(name.takeIf { it.isNotEmpty() }?.take(1) ?: "?", style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), enabled = isNew)
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role (admin/student)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Wallet Balance") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Home Address") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text("Profile Photo URL") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotEmpty() && email.isNotEmpty(),
                onClick = {
                    onSave(user.copy(
                        name = name,
                        email = email,
                        role = role,
                        balance = balance.toDoubleOrNull() ?: user.balance,
                        phoneNumber = phoneNumber.ifEmpty { null },
                        address = address.ifEmpty { null },
                        photoUrl = photoUrl.ifEmpty { null }
                    ))
                }
            ) { Text(if (isNew) "Create User" else "Save Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
