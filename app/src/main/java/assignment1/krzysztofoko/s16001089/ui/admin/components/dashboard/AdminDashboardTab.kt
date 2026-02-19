package assignment1.krzysztofoko.s16001089.ui.admin.components.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.RoleDiscount
import assignment1.krzysztofoko.s16001089.ui.admin.AdminSection
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth

/**
 * AdminDashboardTab.kt
 *
 * The primary landing page for the Administrative Panel.
 * Provides a high-level summary of system statistics, user status, and quick access 
 * to global configuration tools (Discounts, Logs, Broadcasts).
 *
 * UI Architecture:
 * - Uses [LazyColumn] to support scrollable content on smaller devices.
 * - Wraps sections in [AdaptiveScreenContainer] to maintain optimal line lengths on tablets/desktops.
 * - Employs a card-based layout for clear visual separation of administrative modules.
 */

/**
 * Main dashboard view for administrators.
 * 
 * @param viewModel The state holder for admin data and actions.
 * @param isDarkTheme Used to adjust specific semantic colors (e.g., pending status warnings).
 */
@Composable
fun AdminDashboardTab(viewModel: AdminViewModel, isDarkTheme: Boolean) {
    // --- DATA OBSERVATION ---
    // Collects system-wide state from the Room database / Firebase via the ViewModel.
    val users by viewModel.allUsers.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val books by viewModel.allBooks.collectAsState(emptyList())
    val courses by viewModel.allCourses.collectAsState(emptyList())
    val existingDiscounts by viewModel.roleDiscounts.collectAsState()
    
    // Calculate derived statistics for the overview cards.
    val pendingApps = applications.count { it.details.status == "PENDING_REVIEW" }
    val approvedApps = applications.count { it.details.status == "APPROVED" }
    
    // Auth context for the personalized welcome header.
    val currentUser = FirebaseAuth.getInstance().currentUser
    val localAdmin = users.find { it.email == currentUser?.email }

    // --- POPUP & DIALOG STATES ---
    var showProjectDetailsPopup by remember { mutableStateOf(false) }
    var showGlobalDiscountDialog by remember { mutableStateOf(false) }
    var showSaveSuccessPopup by remember { mutableStateOf(false) }
    
    AdaptiveScreenContainer(
        maxWidth = AdaptiveWidths.Wide
    ) { _ -> 
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = AdaptiveSpacing.contentPadding(), 
                vertical = AdaptiveSpacing.medium()
            ),
            verticalArrangement = Arrangement.spacedBy(AdaptiveSpacing.medium())
        ) {
            // 1. WELCOME SECTION: Displays admin profile and role verification.
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.adaptiveWidth(AdaptiveWidths.Medium),
                        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(AdaptiveSpacing.medium()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                photoUrl = localAdmin?.photoUrl ?: currentUser?.photoUrl?.toString(), 
                                modifier = Modifier.size(AdaptiveDimensions.LargeAvatar)
                            )
                            Spacer(Modifier.width(AdaptiveSpacing.small()))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Welcome Back,", style = AdaptiveTypography.caption(), color = Color.Gray)
                                Text(
                                    text = localAdmin?.name ?: currentUser?.displayName ?: "Administrator", 
                                    style = AdaptiveTypography.headline(), 
                                    fontWeight = FontWeight.Black
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    // Visual badge confirming administrative privileges.
                                    Text(
                                        text = "ADMIN", 
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = AdaptiveTypography.label(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. STATISTICS GRID: Quick overview of key system metrics.
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.adaptiveWidth(AdaptiveWidths.Medium), verticalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small())) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small())) {
                            StatCard(
                                modifier = Modifier.weight(1f), 
                                title = "Users", 
                                value = users.size.toString(), 
                                icon = Icons.Default.People, 
                                color = MaterialTheme.colorScheme.primary, 
                                onClick = { viewModel.setSection(AdminSection.USERS) }
                            )
                            
                            // Highlight pending applications with a warning color (Amber/Orange).
                            val pendingColor = if (isDarkTheme) Color(0xFFFBC02D) else Color(0xFFF57C00)
                            StatCard(
                                modifier = Modifier.weight(1f), 
                                title = "Pending", 
                                value = pendingApps.toString(), 
                                icon = Icons.Default.Timer, 
                                color = pendingColor, 
                                onClick = { viewModel.setSection(AdminSection.APPLICATIONS) }
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small())) {
                            StatCard(
                                modifier = Modifier.weight(1f), 
                                title = "Catalog", 
                                value = (books.size + courses.size).toString(), 
                                icon = Icons.Default.Inventory, 
                                color = Color(0xFF4CAF50), 
                                onClick = { viewModel.setSection(AdminSection.CATALOG) }
                            )
                            StatCard(
                                modifier = Modifier.weight(1f), 
                                title = "Live Apps", 
                                value = approvedApps.toString(), 
                                icon = Icons.AutoMirrored.Filled.Assignment, 
                                color = Color(0xFF2196F3), 
                                onClick = { viewModel.setSection(AdminSection.APPLICATIONS) }
                            )
                        }
                    }
                }
            }

            // 3. PROJECT ATTRIBUTION: Displays developer and institution info from AppConstants.
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.adaptiveWidth(AdaptiveWidths.Medium)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("PROJECT DETAILS", style = AdaptiveTypography.label(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                            TextButton(onClick = { showProjectDetailsPopup = true }) { 
                                Text("View Full Details", style = AdaptiveTypography.hint()) 
                            }
                        }
                        Spacer(Modifier.height(AdaptiveSpacing.extraSmall()))
                        Card(
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()), 
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)), 
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(AdaptiveSpacing.medium()), verticalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small())) {
                                ProjectInfoRow(Icons.Default.School, "Institution", AppConstants.INSTITUTION)
                                ProjectInfoRow(Icons.Default.Code, "Developer", AppConstants.DEVELOPER_NAME)
                                ProjectInfoRow(Icons.Default.Badge, "Student ID", AppConstants.STUDENT_ID)
                                ProjectInfoRow(Icons.Default.Info, "Version", AppConstants.VERSION_NAME)
                            }
                        }
                    }
                }
            }

            // 4. ADMINISTRATIVE ACTIONS: Quick links to critical system tools.
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.adaptiveWidth(AdaptiveWidths.Medium)) {
                        Text("ADMIN CONTROLS", style = AdaptiveTypography.label(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                        Spacer(Modifier.height(AdaptiveSpacing.extraSmall()))
                        Card(
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()), 
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)), 
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(AdaptiveSpacing.small())) {
                                AdminActionButton(Icons.Default.Percent, "Global Discount Config") { showGlobalDiscountDialog = true }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                                AdminActionButton(Icons.Default.Security, "System Logs") { viewModel.setSection(AdminSection.LOGS) }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                                AdminActionButton(Icons.Default.Mail, "Broadcast Announcement") { 
                                    viewModel.setSection(AdminSection.BROADCAST) 
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Spacing for visual balance.
            item { Spacer(Modifier.height(AdaptiveSpacing.large())) }
        }
    }

    // --- OVERLAY DIALOGS ---
    if (showGlobalDiscountDialog) {
        InternalGlobalDiscountDialog(
            existingDiscounts = existingDiscounts,
            onDismiss = { showGlobalDiscountDialog = false },
            onSave = { changes: Map<String, Float> ->
                // Batch update each role discount in the database.
                changes.forEach { (role, percent) ->
                    viewModel.saveRoleDiscount(role, percent.toDouble())
                }
                showGlobalDiscountDialog = false
                showSaveSuccessPopup = true
            }
        )
    }

    // Standard success feedback popups.
    AppPopups.AdminSaveSuccess(show = showSaveSuccessPopup, onDismiss = { showSaveSuccessPopup = false })
    AppPopups.AdminProjectDetails(show = showProjectDetailsPopup, onDismiss = { showProjectDetailsPopup = false })
}

/**
 * A compact information card displaying a metric, label, and icon.
 * Used in the Statistics Grid section of the dashboard.
 */
@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() }, 
        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()), 
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)), 
        border = BorderStroke(1.2.dp, color.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(AdaptiveSpacing.small()), horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = color, modifier = Modifier.size(AdaptiveDimensions.SmallIconSize))
            Spacer(Modifier.height(AdaptiveSpacing.extraSmall()))
            Text(text = value, style = AdaptiveTypography.headline(), fontWeight = FontWeight.Black, color = color)
            Text(text = title, style = AdaptiveTypography.caption(), color = Color.Gray)
        }
    }
}

/**
 * Displays a single row of project information (e.g., Developer name).
 */
@Composable
fun ProjectInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(AdaptiveSpacing.small()))
        Column {
            Text(label, style = AdaptiveTypography.hint(), color = Color.Gray)
            Text(value, style = AdaptiveTypography.body(), fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * A navigational or action row used in the Administrative Controls card.
 */
@Composable
fun AdminActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = AdaptiveSpacing.extraSmall(), horizontal = 4.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(AdaptiveSpacing.small()))
        Text(label, style = AdaptiveTypography.body(), fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
    }
}

/**
 * A specialized dialog for managing group-based pricing discounts.
 * 
 * Features:
 * - Tabbed role selection via [LazyRow].
 * - Dual input modes: Slider for quick adjustments and TextField for precision.
 * - State buffering: Changes are stored locally in [pendingChanges] until the user confirms.
 */
@Composable
private fun InternalGlobalDiscountDialog(
    existingDiscounts: List<RoleDiscount>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Float>) -> Unit
) {
    val roles = listOf("student", "teacher", "user", "admin")
    var selectedRole by remember { mutableStateOf("student") }
    
    // Buffer for changes to prevent immediate (and potentially accidental) database writes.
    val pendingChanges = remember { mutableStateMapOf<String, Float>() }
    
    // Initialize the buffer from current database state.
    LaunchedEffect(existingDiscounts) {
        existingDiscounts.forEach { pendingChanges[it.role] = it.discountPercent.toFloat() }
    }

    val currentPercent = pendingChanges[selectedRole] ?: 0f
    
    // UI state for precision numeric input.
    var isEditingManually by remember { mutableStateOf(false) }
    var manualText by remember(selectedRole, isEditingManually) { 
        mutableStateOf(currentPercent.toInt().toString()) 
    }

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
                Text("Role Discounts", fontWeight = FontWeight.Black, style = AdaptiveTypography.sectionHeader())
                Text("Batch update group rates", style = AdaptiveTypography.hint(), color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // ROLE SELECTION TABS
                Text("Target Role:", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(roles) { role ->
                        val isSelected = selectedRole == role
                        val rolePercent = pendingChanges[role] ?: 0f
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    selectedRole = role
                                    isEditingManually = false
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = role.replaceFirstChar { it.uppercase() },
                                    style = AdaptiveTypography.label(),
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Text(
                                    text = "${rolePercent.toInt()}%",
                                    style = AdaptiveTypography.caption(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                // ADJUSTMENT CONTROLS (Slider & TextField)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Discount Rate:", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold)
                        if (isEditingManually) {
                            // Precision Input
                            OutlinedTextField(
                                value = manualText,
                                onValueChange = { input ->
                                    if (input.length <= 3 && input.all { it.isDigit() }) {
                                        manualText = input
                                        val newVal = input.toFloatOrNull() ?: 0f
                                        pendingChanges[selectedRole] = newVal.coerceIn(0f, 100f)
                                    }
                                },
                                modifier = Modifier.width(80.dp),
                                textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 18.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                suffix = { Text("%") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        } else {
                            // Display / Trigger manual entry
                            Text(
                                text = "${currentPercent.toInt()}%", 
                                style = AdaptiveTypography.headline(), 
                                fontWeight = FontWeight.Black, 
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { isEditingManually = true }
                            )
                        }
                    }
                    Slider(
                        value = currentPercent,
                        onValueChange = { 
                            pendingChanges[selectedRole] = it
                            isEditingManually = false 
                        },
                        valueRange = 0f..100f,
                        steps = 100, 
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
                Text(
                    "Note: Switches between roles above will preserve your temporary changes until you click save.",
                    style = AdaptiveTypography.hint(),
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(pendingChanges.toMap()) }, 
                modifier = Modifier.fillMaxWidth().height(48.dp), 
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save All Changes", fontWeight = FontWeight.Black)
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
