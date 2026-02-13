package assignment1.krzysztofoko.s16001089.ui.admin.components.Profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.*

/**
 * AdminDetailScreen displays the professional institutional profile of the CURRENTLY SIGNED-IN Admin.
 * It is distinct from the general account settings.
 */
@Composable
fun AdminDetailScreen(
    viewModel: AdminViewModel,
    onNavigateToSettings: () -> Unit
) {
    // REACTIVE STATE: Synchronizes with the currently signed-in Admin's user record
    val adminUser by viewModel.currentAdminUser.collectAsState()

    // UI STATE: Local persistence for bio/dept since they aren't part of UserLocal yet
    // In a production app, these would be separate fields in the database.
    var isEditing by remember { mutableStateOf(false) }
    var editBio by remember { mutableStateOf("Head of System Administration and Catalog Management.") }
    var editDept by remember { mutableStateOf("Digital Infrastructure") }

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Standard) { isTablet ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AdaptiveSpacing.contentPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER
            item {
                AdaptiveDashboardHeader(
                    title = "My Profile",
                    subtitle = "Your professional university identity",
                    icon = Icons.Default.Badge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // PRIMARY PROFILE CARD
            item {
                AdaptiveDashboardCard { cardIsTablet ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(
                            photoUrl = adminUser?.photoUrl,
                            modifier = Modifier.size(if (cardIsTablet) 120.dp else 100.dp),
                            isLarge = true
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = adminUser?.name ?: "Loading...",
                            style = if (cardIsTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isEditing) "Updating Professional Info" else editDept,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        
                        if (!isEditing) {
                            Row(
                                modifier = Modifier.adaptiveButtonWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { isEditing = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit Info", fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(Modifier.width(8.dp))
                                
                                FilledIconButton(
                                    onClick = onNavigateToSettings,
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Settings, "Account Settings")
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.adaptiveButtonWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(onClick = { isEditing = false }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) { Text("Cancel") }
                                Button(onClick = { isEditing = false }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Save")
                                }
                            }
                        }
                    }
                }
            }

            // CONTENT SECTIONS
            if (isEditing) {
                item {
                    AdaptiveDashboardCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Professional Information", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(value = editDept, onValueChange = { editDept = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Biography") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            } else {
                item {
                    AdminInfoCard(
                        icon = Icons.Default.Info,
                        title = "Biography",
                        content = editBio
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminMiniCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Email,
                            label = "Email Address",
                            value = adminUser?.email ?: "Not available"
                        )
                        AdminMiniCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Security,
                            label = "User Role",
                            value = adminUser?.role?.uppercase() ?: "ADMIN"
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AdminInfoCard(icon: ImageVector, title: String, content: String) {
    AdaptiveDashboardCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Text(text = content, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminMiniCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    AdaptiveDashboardCard(modifier = modifier) {
        Column {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            @Suppress("DEPRECATION")
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
