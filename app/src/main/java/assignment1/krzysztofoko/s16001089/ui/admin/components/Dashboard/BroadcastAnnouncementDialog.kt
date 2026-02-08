package assignment1.krzysztofoko.s16001089.ui.admin.components.Dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.isTablet

@Composable
fun BroadcastAnnouncementDialog(
    onDismiss: () -> Unit,
    onSend: (String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    
    val targetOptions = listOf(
        BroadcastTarget("All", "Everyone in the system", Icons.Default.Public),
        BroadcastTarget("Students", "Only enrolled students", Icons.Default.School),
        BroadcastTarget("Teachers", "Tutors and faculty", Icons.Default.Person),
        BroadcastTarget("Admins", "System administrators", Icons.Default.AdminPanelSettings),
        BroadcastTarget("Users", "Standard registered users", Icons.Default.PeopleOutline)
    )
    var selectedTargetId by remember { mutableStateOf<String?>("All") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val selectedTarget = targetOptions.find { it.id == selectedTargetId }
    val dialogShape = RoundedCornerShape(28.dp)
    val isTablet = isTablet()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .widthIn(max = 500.dp) // Constrain width for tablet
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), dialogShape),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Broadcast Center", fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                Text("Send urgent notifications to groups", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                
                // --- Compact Audience Selector ---
                Column {
                    Text("Target Audience", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isMenuExpanded = true },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = selectedTarget?.icon ?: Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedTarget?.id ?: "Select Audience",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = selectedTarget?.description ?: "Choose who receives this",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = if (isMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier
                                .width(if (isTablet) 400.dp else 280.dp) // Optimized width for dropdown
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            targetOptions.forEach { target ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(target.icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(target.id, fontWeight = FontWeight.Bold)
                                                Text(target.description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTargetId = target.id
                                        isMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // --- Compose Message ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Compose Message", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        placeholder = { Text("e.g., Important Security Update") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message Content") },
                        placeholder = { Text("Write your announcement details here...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && message.isNotBlank() && selectedTargetId != null && !isSending,
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
                    onSend(title, message, roles)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Transmit Announcement", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Text("Discard Draft", color = Color.Gray)
            }
        },
        shape = dialogShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

data class BroadcastTarget(
    val id: String,
    val description: String,
    val icon: ImageVector
)
