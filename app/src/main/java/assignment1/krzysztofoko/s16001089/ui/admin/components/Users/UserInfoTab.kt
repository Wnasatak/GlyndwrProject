package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminUserDetailsViewModel
import coil.compose.AsyncImage
import java.util.*

@Composable
fun UserInfoTab(user: UserLocal?, viewModel: AdminUserDetailsViewModel) {
    if (user == null) return
    var showEditDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp), 
        verticalArrangement = Arrangement.spacedBy(16.dp), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(150.dp), 
                            shape = CircleShape, 
                            color = MaterialTheme.colorScheme.primaryContainer, 
                            border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl, 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().clip(CircleShape), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(user.name.take(1), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(text = user.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text(text = user.email, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = user.role.uppercase(), 
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), 
                            style = MaterialTheme.typography.labelMedium, 
                            fontWeight = FontWeight.Black, 
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            InfoSectionDetails("Contact Information") {
                InfoRowDetails(Icons.Default.Phone, "Phone", user.phoneNumber ?: "Not provided")
                InfoRowDetails(Icons.Default.Home, "Address", user.address ?: "Not provided")
            }
        }

        item {
            InfoSectionDetails("Account Status") {
                val balanceFormatted = String.format(Locale.US, "%.2f", user.balance)
                InfoRowDetails(Icons.Default.AccountBalanceWallet, "Balance", "£$balanceFormatted")
                InfoRowDetails(Icons.Default.Percent, "Personal Discount", "${user.discountPercent}%")
                InfoRowDetails(Icons.Default.Badge, "User ID", user.id)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showEditDialog = true }, 
                    modifier = Modifier.weight(1f).height(56.dp), 
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Info", fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = { showDiscountDialog = true }, 
                    modifier = Modifier.weight(1f).height(56.dp), 
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Percent, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Discount", fontWeight = FontWeight.Black)
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    if (showEditDialog) {
        UserEditDialog(
            user = user, 
            isNew = false, 
            onDismiss = { showEditDialog = false }, 
            onSave = { updated -> 
                viewModel.updateUser(updated)
                showEditDialog = false 
            }
        )
    }

    if (showDiscountDialog) {
        var discountText by remember { mutableStateOf(user.discountPercent.toString()) }
        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text("Edit Personal Discount", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Set a specific discount percentage for this user.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) discountText = it },
                        label = { Text("Discount (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val discount = discountText.toDoubleOrNull() ?: 0.0
                    viewModel.updateUser(user.copy(discountPercent = discount))
                    showDiscountDialog = false
                }) { Text("Save Discount") }
            },
            dismissButton = { TextButton(onClick = { showDiscountDialog = false }) { Text("Cancel") } }
        )
    }
}
