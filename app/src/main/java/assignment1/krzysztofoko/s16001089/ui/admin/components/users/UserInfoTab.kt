package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import androidx.compose.foundation.BorderStroke
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

/**
 * UserInfoTab.kt
 *
 * This tab provides a focused overview of a user's primary identity and contact information.
 * It is used within the Administrative User Details screen to allow staff to quickly
 * verify a student's profile and perform direct account modifications.
 */

/**
 * Main information tab for student profile auditing.
 *
 * @param user The [UserLocal] object containing the student's data.
 * @param viewModel The [AdminUserDetailsViewModel] for handling data updates.
 */
@Composable
fun UserInfoTab(user: UserLocal?, viewModel: AdminUserDetailsViewModel) {
    // If user data is missing, we exit early to prevent rendering empty UI.
    if (user == null) return

    // --- LOCAL UI STATE ---
    // Controls the visibility of the UserEditDialog.
    var showEditDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp), 
        verticalArrangement = Arrangement.spacedBy(16.dp), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. PRIMARY IDENTITY CARD ---
        // Contains the avatar, full name, email, and role badge.
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image Container
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(150.dp), 
                            shape = CircleShape, 
                            color = MaterialTheme.colorScheme.primaryContainer, 
                            border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            if (user.photoUrl != null) {
                                // Load user image with Coil.
                                AsyncImage(
                                    model = user.photoUrl, 
                                    contentDescription = "Profile Picture", 
                                    modifier = Modifier.fillMaxSize().clip(CircleShape), 
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // FALLBACK: Initial letter fallback if no photo exists.
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.name.take(1), 
                                        style = MaterialTheme.typography.displayLarge, 
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // User Identity details.
                    Text(
                        text = user.name, 
                        style = MaterialTheme.typography.headlineMedium, 
                        fontWeight = FontWeight.Black, 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = user.email, 
                        color = Color.Gray, 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // High-contrast role badge.
                    Surface(
                        color = MaterialTheme.colorScheme.primary, 
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        @Suppress("DEPRECATION")
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

        // --- 2. CONTACT INFORMATION SECTION ---
        item {
            InfoSectionDetails("Contact Information") {
                InfoRowDetails(Icons.Default.Phone, "Phone", user.phoneNumber ?: "Not provided")
                InfoRowDetails(Icons.Default.Home, "Address", user.address ?: "Not provided")
            }
        }

        // --- 3. ACCOUNT FINANCIAL STATUS ---
        item {
            InfoSectionDetails("Account Status") {
                // Formatting currency using UK/US standard.
                val balanceFormatted = String.format(Locale.US, "%.2f", user.balance)
                InfoRowDetails(Icons.Default.AccountBalanceWallet, "Balance", "£$balanceFormatted")
                InfoRowDetails(Icons.Default.Badge, "User ID", user.id)
            }
        }

        // --- 4. ADMINISTRATIVE ACTIONS ---
        // Prominent button to trigger the editing workflow.
        item {
            Button(
                onClick = { showEditDialog = true }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(12.dp))
                @Suppress("DEPRECATION")
                Text("Edit Account Details", fontWeight = FontWeight.Black)
            }
        }
        
        // Ensure content is not obscured by navigation bars.
        item { Spacer(Modifier.height(20.dp)) }
    }

    // --- OVERLAY: USER EDITOR ---
    // Opens when showEditDialog is true, passing the current user record.
    if (showEditDialog) {
        UserEditDialog(
            user = user, 
            isNew = false, 
            onDismiss = { showEditDialog = false }, 
            onSave = { updated -> 
                // persist updates via the ViewModel logic.
                viewModel.updateUser(updated)
                showEditDialog = false 
            }
        )
    }
}
