package assignment1.krzysztofoko.s16001089.ui.admin.components.profile

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
 * AdminDetailScreen.kt
 *
 * This screen displays the professional and institutional profile of the currently 
 * authenticated administrator. It is designed to be a "Staff Profile" hub, distinct 
 * from general user settings, focusing on work-related identifiers like Department 
 * and Professional Biography.
 *
 * UI Architecture:
 * - Reactive Data Flow: Subscribes to the admin user stream from the ViewModel.
 * - State Management: Uses local 'remembered' state for the edit-mode toggle and 
 *   temporary field buffers.
 * - Adaptive Layout: Utilizes specialized 'Adaptive' components to ensure a 
 *   professional appearance on both mobile phones and tablets.
 */

/**
 * The main entry point for the Admin Profile Screen.
 *
 * @param viewModel The state holder providing access to the current admin's profile data.
 * @param onNavigateToSettings Callback to trigger navigation to the account-wide settings screen.
 */
@Composable
fun AdminDetailScreen(
    viewModel: AdminViewModel,
    onNavigateToSettings: () -> Unit
) {
    // --- 1. DATA SYNCHRONIZATION ---
    // Collect the latest administrator data from the database. 
    // This allows the UI to update automatically if profile information is changed elsewhere.
    val adminUser by viewModel.currentAdminUser.collectAsState()

    // --- 2. LOCAL UI STATE MANAGEMENT ---
    // isEditing: Tracks if the UI is in 'View Mode' or 'Edit Mode'.
    var isEditing by remember { mutableStateOf(false) }
    
    // TEMPORARY STORAGE: These variables buffer changes while in Edit Mode.
    // In a production environment, these fields (Bio and Department) would ideally be 
    // persisted in the 'UserLocal' table within the Room database.
    var editBio by remember { mutableStateOf("Head of System Administration and Catalog Management. Responsible for maintaining digital infrastructure and ensuring the integrity of the university's online course catalog.") }
    var editDept by remember { mutableStateOf("Digital Infrastructure & IT Services") }

    // AdaptiveScreenContainer ensures the profile content is centered and width-constrained for readability.
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Standard) { isTablet ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Uses dynamic spacing based on screen size (Mobile vs Tablet).
            contentPadding = PaddingValues(AdaptiveSpacing.contentPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER SECTION ---
            // Displays the screen title and context icon.
            item {
                AdaptiveDashboardHeader(
                    title = "My Profile",
                    subtitle = "Your professional university identity",
                    icon = Icons.Default.Badge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- PRIMARY IDENTITY CARD ---
            // Displays high-level information: Avatar, Name, and Department/Status.
            item {
                AdaptiveDashboardCard { cardIsTablet ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Picture with adaptive sizing.
                        UserAvatar(
                            photoUrl = adminUser?.photoUrl,
                            modifier = Modifier.size(if (cardIsTablet) 120.dp else 100.dp),
                            isLarge = true
                        )
                        Spacer(Modifier.height(16.dp))

                        // Full name sourced from the admin record.
                        Text(
                            text = adminUser?.name ?: "Loading...",
                            style = if (cardIsTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        
                        // Sub-label showing either the department or the editing status.
                        Text(
                            text = if (isEditing) "Updating Professional Info" else editDept,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        // --- INTERACTIVE ACTION AREA ---
                        if (!isEditing) {
                            // VIEW MODE ACTIONS: Edit Info & Global Settings access.
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
                            // EDIT MODE ACTIONS: Commit changes or Discard.
                            Row(
                                modifier = Modifier.adaptiveButtonWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { isEditing = false }, 
                                    shape = RoundedCornerShape(12.dp), 
                                    modifier = Modifier.weight(1f)
                                ) { 
                                    Text("Cancel") 
                                }
                                Button(
                                    onClick = { isEditing = false }, 
                                    shape = RoundedCornerShape(12.dp), 
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }

            // --- CONTENT SECTION ---
            // Switches between input forms and informational display cards.
            if (isEditing) {
                // PROFESSIONAL EDIT FORM
                item {
                    AdaptiveDashboardCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Professional Information", 
                                fontWeight = FontWeight.Black, 
                                color = MaterialTheme.colorScheme.primary, 
                                style = MaterialTheme.typography.titleMedium
                            )
                            OutlinedTextField(
                                value = editDept, 
                                onValueChange = { editDept = it }, 
                                label = { Text("Department") }, 
                                modifier = Modifier.fillMaxWidth(), 
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = editBio, 
                                onValueChange = { editBio = it }, 
                                label = { Text("Biography") }, 
                                modifier = Modifier.fillMaxWidth(), 
                                minLines = 3, 
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            } else {
                // READ-ONLY DISPLAY
                item {
                    AdminInfoCard(
                        icon = Icons.Default.Info,
                        title = "Biography",
                        content = editBio
                    )
                }

                // COMPACT METRICS: Email and Access Level/Role summary.
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

            // Final bottom spacing to ensure visibility above the system navigation bar.
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

/**
 * A standardized card for displaying titled informational sections.
 *
 * @param icon The representative icon for the section.
 * @param title The section heading.
 * @param content The primary text content.
 */
@Composable
fun AdminInfoCard(icon: ImageVector, title: String, content: String) {
    AdaptiveDashboardCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Branded Icon Wrapper
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                    shape = CircleShape, 
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { 
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) 
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title, 
                    fontWeight = FontWeight.Black, 
                    style = MaterialTheme.typography.titleSmall, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = content, 
                style = MaterialTheme.typography.bodyMedium, 
                lineHeight = 22.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A compact information card used for displaying single key-value data points.
 *
 * @param modifier Applied for layout positioning (e.g., weights in Rows).
 * @param icon The identifying icon.
 * @param label Small descriptive heading.
 * @param value The actual data string.
 */
@Composable
fun AdminMiniCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    AdaptiveDashboardCard(modifier = modifier) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                shape = CircleShape, 
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { 
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) 
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.Gray, 
                fontWeight = FontWeight.Bold
            )
            @Suppress("DEPRECATION")
            Text(
                text = value, 
                style = MaterialTheme.typography.bodySmall, 
                fontWeight = FontWeight.Black, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
