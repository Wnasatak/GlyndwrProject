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
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Sub-filter Row
        if (selectedTab == 0) {
            AdminFilterRow(selectedFilter = adminFilter, onFilterChange = { adminFilter = it })
        } else {
            UserFilterRow(selectedFilter = userFilter, onFilterChange = { userFilter = it })
        }

        val baseLogs = if (selectedTab == 0) adminLogs else userLogs
        val activeFilter = if (selectedTab == 0) adminFilter else userFilter
        
        val filteredLogs = baseLogs.filter { log ->
            if (activeFilter == "ALL") true
            else if (activeFilter == "LOGINS") log.action.contains("LOGIN") || log.action.contains("SIGN_UP")
            else if (activeFilter == "ADDED") log.action.contains("CREATED") || log.action.contains("POSTED") || log.action.contains("ADD")
            else if (activeFilter == "EDITED") log.action.contains("EDITED") || log.action.contains("UPDATED")
            else if (activeFilter == "REMOVED") log.action.contains("DELETED") || log.action.contains("REMOVE") || log.action.contains("CLEAR")
            else true
        }

        if (filteredLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No logs found for this filter.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
fun AdminFilterRow(selectedFilter: String, onFilterChange: (String) -> Unit) {
    val filters = listOf(
        "ALL" to Icons.Default.FilterList,
        "LOGINS" to Icons.Default.Login,
        "ADDED" to Icons.Default.AddCircle,
        "EDITED" to Icons.Default.Edit,
        "REMOVED" to Icons.Default.Delete
    )
    
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (id, icon) ->
            FilterPill(id = id, label = id.capitalize(), icon = icon, isSelected = selectedFilter == id) { onFilterChange(id) }
        }
    }
}

@Composable
fun UserFilterRow(selectedFilter: String, onFilterChange: (String) -> Unit) {
    val filters = listOf(
        "ALL" to Icons.Default.FilterList,
        "LOGINS" to Icons.Default.Login,
        "ADDED" to Icons.Default.AddCircle,
        "REMOVED" to Icons.Default.Delete
    )
    
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (id, icon) ->
            FilterPill(id = id, label = id.capitalize(), icon = icon, isSelected = selectedFilter == id) { onFilterChange(id) }
        }
    }
}

@Composable
fun FilterPill(id: String, label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isSelected) Color.White else Color.Gray)
            Spacer(Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = if (isSelected) Color.White else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = log.action.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                    Text(
                        text = sdf.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(text = log.details, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                Icon(imageVector = icon, contentDescription = null, tint = color)
                Spacer(Modifier.width(12.dp))
                Text("Activity Log Detail", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("ACTION TYPE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = log.action.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                    }
                }

                Column {
                    Text("FULL DESCRIPTION", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = log.details, style = MaterialTheme.typography.bodyLarge)
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("INITIATOR", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = log.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "ID: ${log.userId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("TARGET ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = log.targetId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Column {
                    Text("EVENT TIMESTAMP", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = sdf.format(Date(log.timestamp)), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Close")
            }
        }
    )
}

private fun String.capitalize(): String = this.lowercase().replaceFirstChar { it.uppercase() }
