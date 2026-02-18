package assignment1.krzysztofoko.s16001089.ui.admin.components.apps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

/**
 * AppsTab.kt
 *
 * This component provides the primary administrative interface for managing course applications.
 * It features a comprehensive filtering system and an adaptive grid layout to efficiently 
 * handle student enrolment requests, course changes, and withdrawals.
 */

/**
 * ApplicationsTab Composable
 *
 * The main container for the enrolment management hub.
 *
 * @param viewModel The state holder for administrative operations.
 * @param onReviewApp Callback to open the detailed review screen for a specific application.
 */
@Composable
fun ApplicationsTab(
    viewModel: AdminViewModel, // Administrative logic coordinator
    onReviewApp: (AdminApplicationItem) -> Unit // Navigation trigger for full application details
) {
    // Collect the stream of academic applications from the database via the ViewModel.
    val applications by viewModel.applications.collectAsState()
    
    // UI State for managing the current filter selection (All, Pending, etc.).
    var selectedFilter by remember { mutableStateOf("ALL") }
    
    // Categorise applications by status for quick filtering and counter badges.
    val pending = applications.filter { it.details.status == "PENDING_REVIEW" }
    val enrolled = applications.filter { it.details.status == "ENROLLED" }
    val approved = applications.filter { it.details.status == "APPROVED" }
    val rejected = applications.filter { it.details.status == "REJECTED" }

    // Configuration for the top filter pill items.
    val filterOptions = listOf(
        AppsFilterOption("ALL", "All", Icons.Default.FilterList, applications.size, MaterialTheme.colorScheme.primary),
        AppsFilterOption("PENDING", "Pending", Icons.Default.Timer, pending.size, Color(0xFFFBC02D)),
        AppsFilterOption("ENROLLED", "Enrolled", Icons.Default.School, enrolled.size, Color(0xFF673AB7)),
        AppsFilterOption("APPROVED", "Approved", Icons.Default.CheckCircle, approved.size, Color(0xFF4CAF50)),
        AppsFilterOption("REJECTED", "Declined", Icons.Default.Cancel, rejected.size, MaterialTheme.colorScheme.error)
    )

    val isTablet = isTablet() // Determine if device has a large screen for layout adjustments.
    val infiniteCount = Int.MAX_VALUE // Used for the infinite carousel effect on mobile.
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % filterOptions.size)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (isTablet) 0 else startPosition)

    // Apply the user's selected filter to the master list.
    val filteredList = when(selectedFilter) {
        "ALL" -> applications
        "PENDING" -> pending
        "ENROLLED" -> enrolled
        "APPROVED" -> approved
        else -> rejected
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- TOP TOOLBAR: Status Filtering --- //
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
                    // Regular row for tablet users.
                    items(filterOptions) { option ->
                        AppsFilterItem(
                            option = option,
                            isSelected = selectedFilter == option.id,
                            onClick = { selectedFilter = option.id }
                        )
                    }
                } else {
                    // Infinite scrolling filter bar for mobile users.
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

        // --- CONTENT AREA: Adaptive Application Grid --- //
        AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { screenIsTablet ->
            val columns = if (screenIsTablet) 2 else 1 // Multi-column layout for tablets.
            
            if (filteredList.isEmpty()) {
                // Display informative empty state if current filter has no results.
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
                            // Immediate processing actions directly from the card.
                            onApprove = { viewModel.approveApplication(app.details.id, app.details.userId, app.course?.title ?: "Course") },
                            onReject = { viewModel.rejectApplication(app.details.id, app.details.userId, app.course?.title ?: "Course") },
                            onCheck = { onReviewApp(app) } // Navigate to full detail review.
                        )
                    }
                }
            }
        }
    }
}

/**
 * AppsFilterItem Composable
 *
 * An interactive pill for the top filter bar.
 */
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
            @Suppress("DEPRECATION")
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isSelected) option.color else Color.Gray
            )
            Spacer(Modifier.width(6.dp))
            // Badge showing the count of applications in this specific state.
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

/**
 * AdminApplicationCard Composable
 *
 * Summarised view of a student's enrolment request.
 */
@Composable
fun AdminApplicationCard(
    app: AdminApplicationItem, // The data wrapper for the application and related student/course info
    onApprove: () -> Unit, // Handler for the confirm/approve button
    onReject: () -> Unit, // Handler for the decline button
    onCheck: () -> Unit = {} // Handler for opening full detail review
) {
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val isChangeRequest = app.details.requestedCourseId != null // True if student wants to swap courses
    val isWithdrawal = app.details.isWithdrawal // True if student wants to leave the institution

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- HEADER: Basic Info & Status Badge --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = app.student?.name ?: "Unknown Student", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    
                    // Visual indicators for specific request types.
                    if (isWithdrawal) {
                        Surface(color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            @Suppress("DEPRECATION")
                            Text(text = "WITHDRAWAL REQUEST", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        }
                    } else if (isChangeRequest) {
                        Surface(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            @Suppress("DEPRECATION")
                            Text(text = "COURSE CHANGE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        Text(text = app.course?.title ?: "Applied for Course", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                // Custom badge showing current processing status (e.g., PENDING).
                EnrollmentStatusBadge(status = app.details.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // --- NOTIFICATION BLOCKS: Detailed context for special requests --- //
            if (isWithdrawal) {
                Surface(color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        @Suppress("DEPRECATION")
                        Text("Student has requested to withdraw from this course.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else if (isChangeRequest) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            @Suppress("DEPRECATION")
                            Text("FROM", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            @Suppress("DEPRECATION")
                            Text(app.course?.title ?: "Unknown", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(horizontal = 8.dp).size(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            @Suppress("DEPRECATION")
                            Text("TO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            @Suppress("DEPRECATION")
                            Text(app.requestedCourse?.title ?: "New Program", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // --- DATA ROWS: Summary of academic qualifications --- //
            AppDetailRowItem("Qualification", app.details.lastQualification)
            AppDetailRowItem("Institution", app.details.institution)
            AppDetailRowItem("Submitted", sdf.format(Date(app.details.submittedAt)))
            
            Spacer(modifier = Modifier.height(12.dp))
            @Suppress("DEPRECATION")
            Text("Motivation Statement:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = app.details.motivationalText, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // --- ACTION BUTTONS --- //
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

            // Only show approval/rejection controls if the application is still being reviewed.
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
                        Text(if (isWithdrawal) "Confirm" else "Approve", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (app.details.status == "ENROLLED") {
                // Secondary approval path for already enrolled students.
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

/**
 * Reusable helper for displaying a labelled data value.
 */
@Composable
fun AppDetailRowItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        @Suppress("DEPRECATION")
        Text("$label: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
        @Suppress("DEPRECATION")
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Configuration data for the filter categories.
 */
data class AppsFilterOption(val id: String, val label: String, val icon: ImageVector, val count: Int, val color: Color)

/**
 * Standardised view displayed when a filter results in no applications.
 */
@Composable
fun AppsEmptyState(icon: ImageVector, message: String) {
    @Suppress("DEPRECATION")
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
