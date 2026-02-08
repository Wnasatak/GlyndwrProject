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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.SystemLog
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import java.text.SimpleDateFormat
import java.util.*

/**
 * Optimized System Logs Screen.
 * Integrated Tab menu with TopAppBar for both phone and tablet.
 * Automatically filters out system maintenance logs for a cleaner view.
 */
@Composable
fun UsersLogsTab(viewModel: AdminViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } 
    val tabs = listOf("Admin Actions", "User Activity")
    
    val adminLogs by viewModel.adminLogs.collectAsState(initial = emptyList())
    val userLogs by viewModel.userLogs.collectAsState(initial = emptyList())

    var adminFilter by remember { mutableStateOf("ALL") }
    var userFilter by remember { mutableStateOf("ALL") }
    var idSearchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    var selectedLogForDetail by remember { mutableStateOf<SystemLog?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Integrated Full-Width Tab Row
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 2.dp
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                text = title, 
                                fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Bold, 
                                fontSize = 14.sp
                            ) 
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        }

        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                
                // Optimized Search and Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSearchVisible) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isSearchVisible = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search, 
                                    contentDescription = "Search", 
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
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
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            placeholder = { Text("Filter by ID, Name or Action...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    isSearchVisible = false
                                    idSearchQuery = "" 
                                }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
                            },
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                val baseLogs = if (selectedTab == 0) adminLogs else userLogs
                val activeFilter = if (selectedTab == 0) adminFilter else userFilter
                
                val filteredLogs = baseLogs.filter { log ->
                    // EXCLUSION: Don't show automated maintenance logs
                    val isMaintenance = log.details.contains("Automated log cleanup", ignoreCase = true)
                    if (isMaintenance) return@filter false

                    val matchesCategory = if (activeFilter == "ALL") true
                        else if (activeFilter == "LOGINS") log.action.contains("LOGIN") || log.action.contains("SIGN_UP")
                        else if (activeFilter == "ADDED") log.action.contains("CREATED") || log.action.contains("POSTED") || log.action.contains("ADD")
                        else if (activeFilter == "EDITED") log.action.contains("EDITED") || log.action.contains("UPDATED")
                        else if (activeFilter == "REMOVED") log.action.contains("DELETED") || log.action.contains("REMOVE") || log.action.contains("CLEAR")
                        else true

                    val matchesSearch = if (idSearchQuery.isEmpty()) true
                        else log.userId.contains(idSearchQuery, ignoreCase = true) || 
                             log.userName.contains(idSearchQuery, ignoreCase = true) ||
                             log.targetId.contains(idSearchQuery, ignoreCase = true) ||
                             log.action.contains(idSearchQuery, ignoreCase = true)

                    matchesCategory && matchesSearch
                }

                if (filteredLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.2f))
                            Spacer(Modifier.height(16.dp))
                            Text("No activity matching your criteria.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredLogs) { log ->
                            LogItemCard(log, onClick = { selectedLogForDetail = log })
                        }
                    }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 16.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(filters) { (id, icon) ->
            FilterPillSmall(id = id, icon = icon, isSelected = selectedFilter == id) { onFilterChange(id) }
        }
    }
}

@Composable
fun FilterPillSmall(id: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = CircleShape,
        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = id, modifier = Modifier.size(16.dp), tint = if (isSelected) Color.White else Color.Gray)
            if (isSelected) {
                Spacer(Modifier.width(6.dp))
                Text(text = id.capitalizeText(), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Black)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.action.replace("_", " "),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = color,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = sdf.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
                Spacer(Modifier.height(1.dp))
                Text(
                    text = log.details, 
                    style = MaterialTheme.typography.bodySmall, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "By: ${log.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
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
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("Activity Log Detail", fontWeight = FontWeight.Black, fontSize = 18.sp)
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
                        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = log.action.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                    }
                }

                Column {
                    Text("FULL DESCRIPTION", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = log.details, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("INITIATOR", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = log.userName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(text = "ID: ${log.userId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("TARGET ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = log.targetId, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Column {
                    Text("EVENT TIMESTAMP", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = sdf.format(Date(log.timestamp)), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(44.dp).fillMaxWidth(0.4f)) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun String.capitalizeText(): String = this.lowercase().replaceFirstChar { it.uppercase() }
