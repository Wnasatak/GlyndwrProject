package assignment1.krzysztofoko.s16001089.ui.admin.components.dashboard

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.*

/**
 * BroadcastAnnouncementDialog.kt
 *
 * This component provides a professional administrative interface for sending system-wide 
 * or targeted announcements. It features a responsive design, accessibility-focused 
 * high-contrast UI, and an integrated user search for individual targeting.
 *
 * Design Architecture:
 * - Uses a centered Surface acting as a modal dialog.
 * - Employs a scrollable Column to handle variable content heights on smaller screens.
 * - Utilizes AnimatedVisibility for smooth transitions between target modes.
 */

/**
 * The primary composable for composing and sending broadcasts.
 *
 * @param viewModel The shared AdminViewModel used to access the user directory and system state.
 * @param onDismiss Invoked when the user cancels the operation; typically closes the dialog.
 * @param onSend Invoked when the 'Transmit' button is clicked. 
 *               Parameters: (title, message, targetRoles, targetUserId)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastAnnouncementDialog(
    viewModel: AdminViewModel,
    onDismiss: () -> Unit,
    onSend: (String, String, List<String>, String?) -> Unit
) {
    // --- PERSISTENT UI STATE ---
    // Tracks the text content of the announcement.
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    // Tracks the processing state to prevent double-submission and show progress.
    var isSending by remember { mutableStateOf(false) }
    
    // --- TARGETING CONFIGURATION ---
    // Pre-defined segments for broadcasting. These are mapped to database roles.
    val targetOptions = remember {
        listOf(
            BroadcastTarget("All", "Everyone in the system", Icons.Default.Public),
            BroadcastTarget("Students", "Enrolled student accounts", Icons.Default.School),
            BroadcastTarget("Teachers", "Faculty and staff accounts", Icons.Default.Person),
            BroadcastTarget("Admins", "Administrative personnel", Icons.Default.AdminPanelSettings),
            BroadcastTarget("Users", "Standard registered users", Icons.Default.PeopleOutline),
            BroadcastTarget("Specific", "Target a single individual", Icons.Default.PersonSearch)
        )
    }
    
    // Tracks which target category is currently selected.
    var selectedTargetId by remember { mutableStateOf<String?>("All") }
    // State for the Material 3 Exposed Dropdown Menu.
    var isMenuExpanded by remember { mutableStateOf(false) }
    
    // --- INDIVIDUAL USER SEARCH LOGIC ---
    // Only used if selectedTargetId == "Specific".
    var userSearchQuery by remember { mutableStateOf("") }
    // Observes the global user list from the ViewModel.
    val allUsers by viewModel.allUsers.collectAsState()
    // Stores the user selected from search results.
    var selectedUser by remember { mutableStateOf<UserLocal?>(null) }

    /**
     * Filtered User Results:
     * Dynamically filters the global user list based on the search query.
     * Logic: Starts searching after 2 characters, matches name or email, limits to 3 results.
     */
    val filteredUsers = remember(userSearchQuery, allUsers) {
        if (userSearchQuery.length < 2) emptyList()
        else allUsers.filter { 
            it.name.contains(userSearchQuery, ignoreCase = true) || 
            it.email.contains(userSearchQuery, ignoreCase = true) 
        }.take(3)
    }

    // Identifies the active target object for UI mapping (icons, descriptions).
    val selectedTarget = remember(selectedTargetId) { targetOptions.find { it.id == selectedTargetId } }
    val isTablet = isTablet()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        // Main dialog container with high-contrast styling.
        Surface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = AdaptiveSpacing.medium())
                .animateContentSize(),
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            // DESIGN CHOICE: We use pure surface color instead of tonal elevation to maintain 
            // brand color integrity in custom themes like Light Green or Dark Blue.
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .padding(AdaptiveSpacing.medium())
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER SECTION ---
                // Visual indicator (Campaign icon) and branding.
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(AdaptiveDimensions.MediumAvatar)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Campaign, 
                            contentDescription = "Broadcast Icon", 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(AdaptiveDimensions.SmallIconSize)
                        )
                    }
                }
                Spacer(Modifier.height(AdaptiveSpacing.small()))
                Text(
                    text = "Broadcast Center", 
                    fontWeight = FontWeight.Black, 
                    style = AdaptiveTypography.headline(),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "System-wide notification utility", 
                    style = AdaptiveTypography.hint(), 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(AdaptiveSpacing.medium()))

                // --- AUDIENCE SELECTION SECTION ---
                // Uses a Material 3 Exposed Dropdown to select the target group.
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "RECIPIENT GROUP", 
                        style = AdaptiveTypography.label(), 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(AdaptiveSpacing.extraSmall()))
                    
                    ExposedDropdownMenuBox(
                        expanded = isMenuExpanded,
                        onExpandedChange = { isMenuExpanded = !isMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // The "Anchor" or the field that triggers the dropdown.
                        Surface(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(AdaptiveSpacing.itemRadius())
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = selectedTarget?.icon ?: Icons.Default.Groups, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedTarget?.id ?: "Select Group", 
                                        style = AdaptiveTypography.body(), 
                                        fontWeight = FontWeight.Black, 
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = selectedTarget?.description ?: "Choose target", 
                                        style = AdaptiveTypography.hint(), 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded)
                            }
                        }

                        // The actual list of target options.
                        ExposedDropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            targetOptions.forEach { target ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(target.icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(target.id, style = AdaptiveTypography.body(), fontWeight = FontWeight.Bold)
                                                Text(target.description, style = AdaptiveTypography.hint(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTargetId = target.id
                                        isMenuExpanded = false
                                        // Reset specific user if switching away from 'Specific'.
                                        selectedUser = null 
                                    }
                                )
                            }
                        }
                    }
                }

                // --- CONDITIONAL USER SEARCH ---
                // Appears only when targeting a specific individual.
                AnimatedVisibility(
                    visible = selectedTargetId == "Specific",
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = AdaptiveSpacing.small())) {
                        Text("FIND USER", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(AdaptiveSpacing.extraSmall()))
                        
                        OutlinedTextField(
                            value = if (selectedUser != null) selectedUser!!.name else userSearchQuery,
                            onValueChange = { 
                                userSearchQuery = it
                                if (selectedUser != null) selectedUser = null 
                            },
                            placeholder = { Text(text = "Search by name or email...", style = AdaptiveTypography.caption()) },
                            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = { 
                                if(selectedUser != null) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                            singleLine = true,
                            textStyle = AdaptiveTypography.body(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // UI for the dynamic search results.
                        if (filteredUsers.isNotEmpty() && selectedUser == null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column {
                                    filteredUsers.forEach { user ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedUser = user; userSearchQuery = user.name }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            UserAvatar(photoUrl = user.photoUrl, modifier = Modifier.size(AdaptiveDimensions.SmallAvatar))
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(user.name, style = AdaptiveTypography.caption(), fontWeight = FontWeight.Bold)
                                                Text(user.email, style = AdaptiveTypography.hint(), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(AdaptiveSpacing.medium()))

                // --- CONTENT COMPOSITION ---
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                    singleLine = true,
                    textStyle = AdaptiveTypography.body(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(Modifier.height(AdaptiveSpacing.small()))

                // Message Body Field
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Detailed Message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                    minLines = 3,
                    textStyle = AdaptiveTypography.body(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(Modifier.height(AdaptiveSpacing.medium()))

                // --- ACTION BUTTONS ---
                // Transmission Button
                Button(
                    enabled = title.isNotBlank() && message.isNotBlank() && 
                             (selectedTargetId != "Specific" || selectedUser != null) && !isSending,
                    onClick = {
                        isSending = true
                        /**
                         * Role Mapping Logic:
                         * Translates the UI friendly target categories into 
                         * specific database role identifiers.
                         */
                        val roles = when(selectedTargetId) {
                            "All" -> listOf("student", "teacher", "admin", "user", "tutor")
                            "Students" -> listOf("student")
                            "Teachers" -> listOf("teacher", "tutor")
                            "Admins" -> listOf("admin")
                            "Users" -> listOf("user")
                            else -> emptyList()
                        }
                        onSend(title, message, roles, selectedUser?.id)
                    },
                    modifier = Modifier.fillMaxWidth().height(AdaptiveDimensions.StandardButtonHeight),
                    shape = RoundedCornerShape(AdaptiveSpacing.itemRadius())
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Transmit Announcement", 
                            fontWeight = FontWeight.Bold, 
                            style = AdaptiveTypography.sectionHeader()
                        )
                    }
                }

                // Cancellation Button
                TextButton(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(
                        text = "Discard Draft", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold, 
                        style = AdaptiveTypography.sectionHeader()
                    )
                }
            }
        }
    }
}

/**
 * BroadcastTarget Data Model
 * 
 * Represents a selectable audience segment.
 * @param id The display name of the target group.
 * @param description A short summary of who is included in this group.
 * @param icon The visual symbol representing the group.
 */
data class BroadcastTarget(
    val id: String,
    val description: String,
    val icon: ImageVector
)
