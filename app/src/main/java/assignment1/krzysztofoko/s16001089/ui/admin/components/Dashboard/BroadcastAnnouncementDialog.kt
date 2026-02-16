package assignment1.krzysztofoko.s16001089.ui.admin.components.Dashboard

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
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.*

/**
 * A professional, high-impact component for sending system-wide announcements.
 * Refactored for High Contrast: Ensures visibility in custom themes (like Light Green) 
 * by avoiding tonal tints and using sharp outlines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastAnnouncementDialog(
    viewModel: AdminViewModel,
    onDismiss: () -> Unit,
    onSend: (String, String, List<String>, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    
    val targetOptions = remember {
        listOf(
            BroadcastTarget("All", "Everyone", Icons.Default.Public),
            BroadcastTarget("Students", "Enrolled students", Icons.Default.School),
            BroadcastTarget("Teachers", "Faculty members", Icons.Default.Person),
            BroadcastTarget("Admins", "System admins", Icons.Default.AdminPanelSettings),
            BroadcastTarget("Users", "Standard users", Icons.Default.PeopleOutline),
            BroadcastTarget("Specific", "Single individual", Icons.Default.PersonSearch)
        )
    }
    
    var selectedTargetId by remember { mutableStateOf<String?>("All") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    
    var userSearchQuery by remember { mutableStateOf("") }
    val allUsers by viewModel.allUsers.collectAsState()
    var selectedUser by remember { mutableStateOf<UserLocal?>(null) }

    val filteredUsers = remember(userSearchQuery, allUsers) {
        if (userSearchQuery.length < 2) emptyList()
        else allUsers.filter { 
            it.name.contains(userSearchQuery, ignoreCase = true) || 
            it.email.contains(userSearchQuery, ignoreCase = true) 
        }.take(3)
    }

    val selectedTarget = remember(selectedTargetId) { targetOptions.find { it.id == selectedTargetId } }
    val isTablet = isTablet()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = AdaptiveSpacing.medium())
                .animateContentSize(),
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            // HIGH CONTRAST FIX: Use pure surface without tonal elevation to avoid "muddy" green tints.
            // Added real shadowElevation to make it pop from the background.
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            // HIGH CONTRAST FIX: Increased border alpha and used 'outline' for a sharper edge.
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .padding(AdaptiveSpacing.medium())
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER ---
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(AdaptiveDimensions.MediumAvatar)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AdaptiveDimensions.SmallIconSize))
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
                    text = "Urgent group communication tool", 
                    style = AdaptiveTypography.hint(), 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(AdaptiveSpacing.medium()))

                // --- AUDIENCE SELECTOR ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("TARGET AUDIENCE", style = AdaptiveTypography.label(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(AdaptiveSpacing.extraSmall()))
                    
                    ExposedDropdownMenuBox(
                        expanded = isMenuExpanded,
                        onExpandedChange = { isMenuExpanded = !isMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            // HIGH CONTRAST FIX: Pure surface for the input field background.
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(AdaptiveSpacing.itemRadius())
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = selectedTarget?.icon ?: Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = selectedTarget?.id ?: "Select", style = AdaptiveTypography.body(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = selectedTarget?.description ?: "Choose", style = AdaptiveTypography.hint(), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded)
                            }
                        }

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
                                        selectedUser = null 
                                    }
                                )
                            }
                        }
                    }
                }

                // --- USER SEARCH ---
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
                            placeholder = { Text(text = "Find user...", style = AdaptiveTypography.caption()) },
                            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = { if(selectedUser != null) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AdaptiveSpacing.itemRadius()),
                            singleLine = true,
                            textStyle = AdaptiveTypography.body(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

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

                // --- COMPOSE ---
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
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

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
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

                // --- ACTIONS ---
                Button(
                    enabled = title.isNotBlank() && message.isNotBlank() && 
                             (selectedTargetId != "Specific" || selectedUser != null) && !isSending,
                    onClick = {
                        isSending = true
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
                        Text("Transmit Announcement", fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                    }
                }

                TextButton(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("Discard Draft", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                }
            }
        }
    }
}

data class BroadcastTarget(
    val id: String,
    val description: String,
    val icon: ImageVector
)
