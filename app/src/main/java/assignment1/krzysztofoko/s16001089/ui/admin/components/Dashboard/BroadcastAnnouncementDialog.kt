package assignment1.krzysztofoko.s16001089.ui.admin.components.Dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.*

/**
 * A professional, high-impact dialog for sending system-wide announcements.
 * Fully optimized for smartphone display sizes with robust action handling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastAnnouncementDialog(
    viewModel: AdminViewModel,
    onDismiss: () -> Unit,
    onSend: (String, String, List<String>, String?) -> Unit // title, message, roles, specificUserId
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    
    val targetOptions = listOf(
        BroadcastTarget("All", "Everyone", Icons.Default.Public),
        BroadcastTarget("Students", "Enrolled students", Icons.Default.School),
        BroadcastTarget("Teachers", "Faculty members", Icons.Default.Person),
        BroadcastTarget("Admins", "System admins", Icons.Default.AdminPanelSettings),
        BroadcastTarget("Users", "Standard users", Icons.Default.PeopleOutline),
        BroadcastTarget("Specific", "Single individual", Icons.Default.PersonSearch)
    )
    
    var selectedTargetId by remember { mutableStateOf<String?>("All") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    
    // User Search State
    var userSearchQuery by remember { mutableStateOf("") }
    val allUsers by viewModel.allUsers.collectAsState()
    var selectedUser by remember { mutableStateOf<UserLocal?>(null) }

    val filteredUsers = remember(userSearchQuery, allUsers) {
        if (userSearchQuery.length < 2) emptyList()
        else allUsers.filter { 
            it.name.contains(userSearchQuery, ignoreCase = true) || 
            it.email.contains(userSearchQuery, ignoreCase = true) 
        }.take(3) // Smaller list for mobile
    }

    val selectedTarget = targetOptions.find { it.id == selectedTargetId }
    val isTablet = isTablet()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 1f),
            tonalElevation = 12.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(if (isTablet) 24.dp else 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER ---
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(if (isTablet) 56.dp else 44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isTablet) 32.dp else 24.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Broadcast Center", 
                    fontWeight = FontWeight.Black, 
                    style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Urgent group communication tool", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))

                // --- AUDIENCE SELECTOR ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("TARGET AUDIENCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = isMenuExpanded,
                        onExpandedChange = { isMenuExpanded = !isMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = selectedTarget?.icon ?: Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = selectedTarget?.id ?: "Select", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                                    Text(text = selectedTarget?.description ?: "Choose", style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                                                Text(target.id, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                Text(target.description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                AnimatedVisibility(visible = selectedTargetId == "Specific") {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Text("FIND USER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = if (selectedUser != null) selectedUser!!.name else userSearchQuery,
                            onValueChange = { 
                                userSearchQuery = it
                                if (selectedUser != null) selectedUser = null 
                            },
                            placeholder = { 
                                Text(
                                    text = "Find user...", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
                            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = { if(selectedUser != null) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        if (filteredUsers.isNotEmpty() && selectedUser == null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                shape = RoundedCornerShape(12.dp),
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
                                            UserAvatar(photoUrl = user.photoUrl, modifier = Modifier.size(28.dp))
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(user.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                Text(user.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- COMPOSE ---
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(24.dp))

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
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Transmit Announcement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                TextButton(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("Discard Draft", color = Color.Gray, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
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
