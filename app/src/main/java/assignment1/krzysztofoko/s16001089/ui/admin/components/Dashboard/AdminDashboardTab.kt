package assignment1.krzysztofoko.s16001089.ui.admin.components.Dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.SystemLog
import assignment1.krzysztofoko.s16001089.data.RoleDiscount
import assignment1.krzysztofoko.s16001089.ui.admin.AdminSection
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardTab(viewModel: AdminViewModel, isDarkTheme: Boolean) {
    val users by viewModel.allUsers.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val books by viewModel.allBooks.collectAsState(emptyList())
    val courses by viewModel.allCourses.collectAsState(emptyList())
    val existingDiscounts by viewModel.roleDiscounts.collectAsState()
    
    val pendingApps = applications.count { it.details.status == "PENDING_REVIEW" }
    val approvedApps = applications.count { it.details.status == "APPROVED" }
    val currentUser = FirebaseAuth.getInstance().currentUser

    val localAdmin = users.find { it.email == currentUser?.email }

    var showProjectDetailsPopup by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showGlobalDiscountDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Admin Profile Overview ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(photoUrl = localAdmin?.photoUrl ?: currentUser?.photoUrl?.toString(), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Welcome Back,", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(localAdmin?.name ?: currentUser?.displayName ?: "Administrator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                "ADMIN", 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- System Statistics Rows ---
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Users", value = users.size.toString(), icon = Icons.Default.People, color = MaterialTheme.colorScheme.primary, onClick = { viewModel.setSection(AdminSection.USERS) })
                StatCard(modifier = Modifier.weight(1f), title = "Pending", value = pendingApps.toString(), icon = Icons.Default.Timer, color = Color(0xFFFBC02D), onClick = { viewModel.setSection(AdminSection.APPLICATIONS) })
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Catalog", value = (books.size + courses.size).toString(), icon = Icons.Default.Inventory, color = Color(0xFF4CAF50), onClick = { viewModel.setSection(AdminSection.CATALOG) })
                StatCard(modifier = Modifier.weight(1f), title = "Live Apps", value = approvedApps.toString(), icon = Icons.Default.Assignment, color = Color(0xFF2196F3), onClick = { viewModel.setSection(AdminSection.APPLICATIONS) })
            }
        }

        // --- Project Info Section ---
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("PROJECT DETAILS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                TextButton(onClick = { showProjectDetailsPopup = true }) { Text("View Full Details", style = MaterialTheme.typography.labelSmall) }
            }
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(24.dp), 
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)), 
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProjectInfoRow(Icons.Default.School, "Institution", AppConstants.INSTITUTION)
                    ProjectInfoRow(Icons.Default.Code, "Developer", AppConstants.DEVELOPER_NAME)
                    ProjectInfoRow(Icons.Default.Badge, "Student ID", AppConstants.STUDENT_ID)
                    ProjectInfoRow(Icons.Default.Info, "Version", AppConstants.VERSION_NAME)
                }
            }
        }

        // --- Admin Control Hub ---
        item {
            Text("ADMIN CONTROLS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(24.dp), 
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)), 
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AdminActionButton(Icons.Default.Percent, "Global Discount Config") { showGlobalDiscountDialog = true }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    AdminActionButton(Icons.Default.Security, "System Logs") { viewModel.setSection(AdminSection.LOGS) }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                    AdminActionButton(Icons.Default.Mail, "Broadcast Announcement") { showBroadcastDialog = true }
                }
            }
        }

        item { Spacer(Modifier.height(100.dp)) }
    }

    if (showGlobalDiscountDialog) {
        GlobalDiscountDialog(
            existingDiscounts = existingDiscounts,
            onDismiss = { showGlobalDiscountDialog = false },
            onSave = { role, percent ->
                viewModel.saveRoleDiscount(role, percent)
                showGlobalDiscountDialog = false
            }
        )
    }

    if (showProjectDetailsPopup) {
        AlertDialog(
            onDismissRequest = { showProjectDetailsPopup = false },
            modifier = Modifier.border(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
            title = { Text("Project Documentation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProjectPopupItem(Icons.Default.Info, "Description", AppConstants.PROJECT_INFO)
                    ProjectPopupItem(Icons.Default.AssignmentInd, "Student Name", AppConstants.DEVELOPER_NAME)
                    ProjectPopupItem(Icons.Default.Numbers, "ID Number", AppConstants.STUDENT_ID)
                    ProjectPopupItem(Icons.Default.AccountBalance, "University", AppConstants.INSTITUTION)
                    ProjectPopupItem(Icons.Default.Build, "Build Version", AppConstants.VERSION_NAME)
                }
            },
            confirmButton = { Button(onClick = { showProjectDetailsPopup = false }) { Text("Close") } },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showBroadcastDialog) {
        BroadcastAnnouncementDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { title, message ->
                viewModel.sendBroadcast(title, message)
                showBroadcastDialog = false
            }
        )
    }
}

@Composable
fun GlobalDiscountDialog(existingDiscounts: List<RoleDiscount>, onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    val roles = listOf("student", "teacher", "user", "admin")
    var selectedRole by remember { mutableStateOf("student") }
    
    val initialValue = existingDiscounts.find { it.role == selectedRole }?.discountPercent ?: 0.0
    var discountValue by remember(selectedRole) { mutableFloatStateOf(initialValue.toFloat()) }

    val dialogShape = RoundedCornerShape(28.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), dialogShape),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Percent, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) }
                }
                Spacer(Modifier.height(12.dp))
                Text("Role Discounts", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                Text("Update group-wide pricing", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Target Role:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(roles) { role ->
                        val isSelected = selectedRole == role
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedRole = role },
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = role.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Discount Rate:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("${discountValue.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        valueRange = 0f..100f,
                        steps = 19, 
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedRole, discountValue.toDouble()) }, 
                modifier = Modifier.fillMaxWidth().height(40.dp), 
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Update Group Rate", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(32.dp)) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = dialogShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() }, 
        shape = RoundedCornerShape(24.dp), 
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)), 
        border = BorderStroke(1.2.dp, color.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)); Spacer(Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ProjectInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProjectPopupItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AdminActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium); Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
    }
}
