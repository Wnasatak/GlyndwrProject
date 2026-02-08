package assignment1.krzysztofoko.s16001089.ui.admin.components.Apps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import assignment1.krzysztofoko.s16001089.ui.admin.AdminApplicationItem
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ApplicationsTab(
    viewModel: AdminViewModel,
    onReviewApp: (AdminApplicationItem) -> Unit
) {
    val applications by viewModel.applications.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    
    val pending = applications.filter { it.details.status == "PENDING_REVIEW" }
    val enrolled = applications.filter { it.details.status == "ENROLLED" }
    val approved = applications.filter { it.details.status == "APPROVED" }
    val rejected = applications.filter { it.details.status == "REJECTED" }

    val filterOptions = listOf(
        AppsFilterOption("ALL", "All", Icons.Default.FilterList, applications.size, MaterialTheme.colorScheme.primary),
        AppsFilterOption("PENDING", "Pending", Icons.Default.Timer, pending.size, Color(0xFFFBC02D)),
        AppsFilterOption("ENROLLED", "Enrolled", Icons.Default.School, enrolled.size, Color(0xFF673AB7)),
        AppsFilterOption("APPROVED", "Approved", Icons.Default.CheckCircle, approved.size, Color(0xFF4CAF50)),
        AppsFilterOption("REJECTED", "Declined", Icons.Default.Cancel, rejected.size, MaterialTheme.colorScheme.error)
    )

    val isTablet = isTablet()
    val infiniteCount = Int.MAX_VALUE
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % filterOptions.size)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (isTablet) 0 else startPosition)

    val filteredList = when(selectedFilter) {
        "ALL" -> applications
        "PENDING" -> pending
        "ENROLLED" -> enrolled
        "APPROVED" -> approved
        else -> rejected
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Integrated Top Filter Menu ---
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 2.dp
        ) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = if (isTablet) Arrangement.Center else Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTablet) {
                    // Static row for tablets
                    items(filterOptions) { option ->
                        AppsFilterItem(
                            option = option,
                            isSelected = selectedFilter == option.id,
                            onClick = { selectedFilter = option.id }
                        )
                    }
                } else {
                    // Infinite loop for phones
                    items(infiniteCount) { index ->
                        val option = filterOptions[index % filterOptions.size]
                        AppsFilterItem(
                            option = option,
                            isSelected = selectedFilter == option.id,
                            onClick = { selectedFilter = option.id }
                        )
                    }
                }
            }
        }

        // --- Adaptive Content Area ---
        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
            val columns = if (isTablet) 2 else 1
            
            if (filteredList.isEmpty()) {
                AppsEmptyState(
                    icon = filterOptions.find { it.id == selectedFilter }?.icon ?: Icons.Default.Inbox,
                    message = "No ${selectedFilter.lowercase()} applications found."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredList) { app ->
                        AdminApplicationCard(
                            app = app,
                            onApprove = { viewModel.approveApplication(app.details.id, app.details.userId, app.course?.title ?: "Course") },
                            onReject = { viewModel.rejectApplication(app.details.id, app.details.userId, app.course?.title ?: "Course") },
                            onCheck = { onReviewApp(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppsFilterItem(
    option: AppsFilterOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = if (isSelected) option.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) option.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (isSelected) option.color else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isSelected) option.color else Color.Gray
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                color = if (isSelected) option.color else Color.Gray.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Text(
                    text = option.count.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
        }
    }
}

@Composable
fun AdminApplicationCard(
    app: AdminApplicationItem,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCheck: () -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = app.student?.name ?: "Unknown Student", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(text = app.course?.title ?: "Applied for Course", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
                EnrollmentStatusBadge(status = app.details.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppDetailRowItem("Qualification", app.details.lastQualification)
            AppDetailRowItem("Institution", app.details.institution)
            AppDetailRowItem("Submitted", sdf.format(Date(app.details.submittedAt)))
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Motivation Statement:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = app.details.motivationalText, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onCheck,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.FindInPage, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Check Full Application", fontWeight = FontWeight.Bold)
            }

            if (app.details.status == "PENDING_REVIEW") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Decline", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Approve", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (app.details.status == "ENROLLED") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onApprove,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Approve", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AppDetailRowItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

data class AppsFilterOption(val id: String, val label: String, val icon: ImageVector, val count: Int, val color: Color)

@Composable
fun AppsEmptyState(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.Gray.copy(alpha = 0.4f)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
