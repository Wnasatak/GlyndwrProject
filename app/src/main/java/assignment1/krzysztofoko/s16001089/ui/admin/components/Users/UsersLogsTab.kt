package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.SystemLog
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UsersLogsTab(viewModel: AdminViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Admin, 1: User
    val tabs = listOf("Admin Actions", "User Activity")
    
    val adminLogs by viewModel.adminLogs.collectAsState(initial = emptyList())
    val userLogs by viewModel.userLogs.collectAsState(initial = emptyList())

    var adminFilter by remember { mutableStateOf("ALL") }
    var userFilter by remember { mutableStateOf("ALL") }
    var idSearchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    var selectedLogForDetail by remember { mutableStateOf<SystemLog?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
            }
        }

        // Action Row with Search Toggle and Sub-filters
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSearchVisible) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchVisible = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Search, "Show Search", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    if (selectedTab == 0) {
                        AdminFilterRowSmall(selectedFilter = adminFilter, onFilterChange = { adminFilter = it })
                    } else {
                        UserFilterRowSmall(selectedFilter = userFilter, onFilterChange = { userFilter = it })
                    }
                }
            } else {
                OutlinedTextField(
                    value = idSearchQuery,
                    onValueChange = { idSearchQuery = it },
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    placeholder = { Text("Search ID or Name...", fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(24.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { 
                            isSearchVisible = false
                            idSearchQuery = "" 
                        }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(24.dp)) }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }

        val baseLogs = if (selectedTab == 0) adminLogs else userLogs
        val activeFilter = if (selectedTab == 0) adminFilter else userFilter
        
        val filteredLogs = baseLogs.filter { log ->
            val matchesCategory = if (activeFilter == "ALL") true
                else if (activeFilter == "LOGINS") log.action.contains("LOGIN") || log.action.contains("SIGN_UP")
                else if (activeFilter == "ADDED") log.action.contains("CREATED") || log.action.contains("POSTED") || log.action.contains("ADD")
                else if (activeFilter == "EDITED") log.action.contains("EDITED") || log.action.contains("UPDATED")
                else if (activeFilter == "REMOVED") log.action.contains("DELETED") || log.action.contains("REMOVE") || log.action.contains("CLEAR")
                else true

            val matchesSearch = if (idSearchQuery.isEmpty()) true
                else log.userId.contains(idSearchQuery, ignoreCase = true) || 
                     log.userName.contains(idSearchQuery, ignoreCase = true) ||
                     log.targetId.contains(idSearchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }

        if (filteredLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching logs.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredLogs) { log ->
                    LogItemCard(log, onClick = { selectedLogForDetail = log })
                }
            }
        }
    }

    if (selectedLogForDetail != null) {
        LogDetailPopup(
            log = selectedLogForDetail!!,
            onDismiss = { selectedLogForDetail = null }
        )
    }
}

@Composable
fun AdminFilterRowSmall(selectedFilter: String, onFilterChange: (String) -> Unit) {
    val filters = listOf(
        "ALL" to Icons.Default.FilterList,
        "LOGINS" to Icons.Default.Login,
        "ADDED" to Icons.Default.AddCircle,
        "EDITED" to Icons.Default.Edit,
        "REMOVED" to Icons.Default.Delete
    )
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filters) { (id, icon) ->
            FilterPillSmall(id = id, icon = icon, isSelected = selectedFilter == id) { onFilterChange(id) }
        }
    }
}

@Composable
fun UserFilterRowSmall(selectedFilter: String, onFilterChange: (String) -> Unit) {
    val filters = listOf(
        "ALL" to Icons.Default.FilterList,
        "LOGINS" to Icons.Default.Login,
        "ADDED" to Icons.Default.AddCircle,
        "REMOVED" to Icons.Default.Delete
    )
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filters) { (id, icon) ->
            FilterPillSmall(id = id, icon = icon, isSelected = selectedFilter == id) { onFilterChange(id) }
        }
    }
}

@Composable
fun FilterPillSmall(id: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(CircleShape).clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = CircleShape,
        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = id, modifier = Modifier.size(18.dp), tint = if (isSelected) Color.White else Color.Gray)
            if (isSelected) {
                Spacer(Modifier.width(6.dp))
                Text(text = id.capitalize(), style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LogItemCard(log: SystemLog, onClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    
    val (icon, color) = remember(log.action) {
        when {
            log.action.contains("CREATED") || log.action.contains("POSTED") || log.action.contains("SIGN_UP") || log.action.contains("ADD") -> Icons.Default.AddCircle to Color(0xFF4CAF50)
            log.action.contains("EDITED") || log.action.contains("UPDATED") -> Icons.Default.Edit to Color(0xFF2196F3)
            log.action.contains("DELETED") || log.action.contains("REMOVE") || log.action.contains("CLEAR") -> Icons.Default.Delete to Color(0xFFF44336)
            log.action.contains("APPROVED") -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
            log.action.contains("REJECTED") -> Icons.Default.Cancel to Color(0xFFF44336)
            log.action.contains("LOGIN") -> Icons.Default.Login to Color(0xFF4CAF50)
            log.action.contains("LOGOUT") -> Icons.Default.Logout to Color.Gray
            log.action.contains("SEARCH") -> Icons.Default.Search to Color(0xFF2196F3)
            log.action.contains("VIEW") -> Icons.Default.Visibility to Color(0xFF2196F3)
            log.action.contains("PURCHASE") -> Icons.Default.ShoppingCart to Color(0xFF4CAF50)
            else -> Icons.Default.History to Color.Gray
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = color)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = log.action.replace("_", " "),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                    Text(
                        text = sdf.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(text = log.details, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(
                    text = "By: ${log.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LogDetailPopup(log: SystemLog, onDismiss: () -> Unit) {
    val sdf = remember { SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm:ss", Locale.getDefault()) }
    
    val (icon, color) = remember(log.action) {
        when {
            log.action.contains("CREATED") || log.action.contains("POSTED") || log.action.contains("SIGN_UP") || log.action.contains("ADD") -> Icons.Default.AddCircle to Color(0xFF4CAF50)
            log.action.contains("EDITED") || log.action.contains("UPDATED") -> Icons.Default.Edit to Color(0xFF2196F3)
            log.action.contains("DELETED") || log.action.contains("REMOVE") || log.action.contains("CLEAR") -> Icons.Default.Delete to Color(0xFFF44336)
            log.action.contains("APPROVED") -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
            log.action.contains("REJECTED") -> Icons.Default.Cancel to Color(0xFFF44336)
            log.action.contains("LOGIN") -> Icons.Default.Login to Color(0xFF4CAF50)
            log.action.contains("LOGOUT") -> Icons.Default.Logout to Color.Gray
            log.action.contains("SEARCH") -> Icons.Default.Search to Color(0xFF2196F3)
            log.action.contains("VIEW") -> Icons.Default.Visibility to Color(0xFF2196F3)
            log.action.contains("PURCHASE") -> Icons.Default.ShoppingCart to Color(0xFF4CAF50)
            else -> Icons.Default.History to Color.Gray
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Text("Activity Log Detail", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column {
                    Text("ACTION TYPE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 6.dp),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = log.action.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                    }
                }

                Column {
                    Text("FULL DESCRIPTION", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = log.details, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("INITIATOR", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = log.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "ID: ${log.userId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("TARGET ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = log.targetId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Column {
                    Text("EVENT TIMESTAMP", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = sdf.format(Date(log.timestamp)), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp), modifier = Modifier.height(48.dp).fillMaxWidth(0.4f)) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun String.capitalize(): String = this.lowercase().replaceFirstChar { it.uppercase() }
