package assignment1.krzysztofoko.s16001089.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.EnrollmentStatusBadge
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.SectionHeaderDetails
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dedicated Academic Tab for Students.
 * Manages course enrollments, grades, and the multi-step course change process.
 */
@Composable
fun StudentAcademicTab(
    enrollments: List<CourseEnrollmentDetails>,
    history: List<EnrollmentHistory> = emptyList(),
    grades: List<Grade>,
    allCourses: List<Course>,
    userLocal: UserLocal? = null,
    onResignRequest: (CourseEnrollmentDetails) -> Unit = {},
    onChangeRequest: (CourseEnrollmentDetails, String) -> Unit = { _, _ -> }
) {
    var showResignConfirm by remember { mutableStateOf<CourseEnrollmentDetails?>(null) }
    var showCourseChangeFlow by remember { mutableStateOf<CourseEnrollmentDetails?>(null) }
    var showApplicationDetails by remember { mutableStateOf<CourseEnrollmentDetails?>(null) }
    var selectedHistoryItem by remember { mutableStateOf<EnrollmentHistory?>(null) }
    var showSuccessPopup by remember { mutableStateOf<String?>(null) }

    val activeEnrollments = enrollments.filter { it.status != "APPROVED" || !it.isWithdrawal }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { SectionHeaderDetails("My Course Enrollments") }

        if (activeEnrollments.isEmpty()) {
            item { 
                Text(
                    "No active course enrollments.", 
                    color = Color.Gray, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                ) 
            }
        } else {
            items(activeEnrollments) { enrollment ->
                val course = allCourses.find { it.id == enrollment.courseId }
                val requestedCourse = enrollment.requestedCourseId?.let { reqId -> allCourses.find { it.id == reqId } }
                val isPending = enrollment.status == "PENDING_REVIEW"
                val isDeclined = enrollment.status == "REJECTED"
                val isEnrolled = enrollment.status == "ENROLLED" || enrollment.status == "APPROVED" || isDeclined
                val isWithdrawal = enrollment.isWithdrawal

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course?.title ?: "Unknown Course", 
                                    fontWeight = FontWeight.Black, 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontSize = 18.sp,
                                    lineHeight = 22.sp
                                )
                                Text(
                                    text = "Course ID: ${enrollment.courseId}", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray
                                )
                            }
                            
                            if (isEnrolled) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape)
                                        .border(2.dp, Color(0xFF4CAF50).copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                EnrollmentStatusBadge(status = enrollment.status)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Transition UI if a change is requested
                        if (isPending && requestedCourse != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("FROM", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(course?.title ?: "Unknown", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward, 
                                        null, 
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 8.dp).size(16.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("TO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                        Text(requestedCourse.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }

                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        Text(
                            text = "Enrollment Date:", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.primary, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sdf.format(Date(enrollment.submittedAt)), 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = Color.Gray
                        )

                        if (isDeclined) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Your recent request was declined by administration. You remain enrolled in this course.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isPending) {
                            val statusText = if (isWithdrawal) {
                                "Withdrawal request submitted to administration."
                            } else {
                                "Your course change request has been submitted."
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isWithdrawal) Icons.Default.Info else Icons.Default.HourglassEmpty,
                                            null,
                                            tint = if (isWithdrawal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Submitted on: ${sdf.format(Date(enrollment.submittedAt))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Check Application - Icon Only
                                IconButton(
                                    onClick = { showApplicationDetails = enrollment },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Description, 
                                        contentDescription = "Check Application", 
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (enrollment.status == "ENROLLED" || enrollment.status == "APPROVED" || isDeclined) {
                                    // Change Course Button
                                    OutlinedButton(
                                        onClick = { showCourseChangeFlow = enrollment },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.6.dp))
                                        @Suppress("DEPRECATION")
                                        Text("Change Course", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }

                                // Resign Button
                                Button(
                                    onClick = { showResignConfirm = enrollment },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.6.dp))
                                    @Suppress("DEPRECATION")
                                    Text("Resign", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { SectionHeaderDetails("Enrollment History") }
        if (history.isEmpty()) {
            item { 
                Text(
                    "No past enrollment records found.", 
                    color = Color.Gray, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                ) 
            }
        } else {
            items(history) { record ->
                val course = allCourses.find { it.id == record.courseId }
                val prevCourse = record.previousCourseId?.let { pid -> allCourses.find { it.id == pid } }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedHistoryItem = record },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (record.status == "CHANGED") "Changed to ${course?.title ?: "Unknown"}" else "Withdrawn from ${course?.title ?: "Unknown"}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(record.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            color = if (record.status == "CHANGED") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (record.status == "CHANGED") Icons.Default.SwapHoriz else Icons.Default.History,
                                    contentDescription = null,
                                    tint = if (record.status == "CHANGED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SectionHeaderDetails("Academic Results") }
        if (grades.isEmpty()) {
            item { 
                Text(
                    "Academic results will appear here once finalized.", 
                    color = Color.Gray, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                ) 
            }
        } else {
            items(grades) { grade ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    ListItem(
                        headlineContent = { 
                            @Suppress("DEPRECATION")
                            Text(
                                "Score: ${grade.score}%", 
                                fontWeight = FontWeight.Black, 
                                color = if (grade.score >= 40) Color(0xFF4CAF50) else Color.Red
                            ) 
                        },
                        supportingContent = { 
                            Text(
                                grade.feedback ?: "Feedback pending from instructor", 
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        trailingContent = { 
                            @Suppress("DEPRECATION")
                            Text(
                                SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(grade.gradedAt)), 
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
        item { Spacer(Modifier.height(40.dp)) }
    }

    if (selectedHistoryItem != null) {
        val record = selectedHistoryItem!!
        val course = allCourses.find { it.id == record.courseId }
        val prevCourse = record.previousCourseId?.let { pid -> allCourses.find { it.id == pid } }
        
        Dialog(onDismissRequest = { selectedHistoryItem = null }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = (if (record.status == "CHANGED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (record.status == "CHANGED") Icons.Default.SwapHoriz else Icons.Default.History,
                                null,
                                tint = if (record.status == "CHANGED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        text = if (record.status == "CHANGED") "Course Change Record" else "Withdrawal Record",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(record.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    if (record.status == "CHANGED") {
                        DetailRow("Previous Course", prevCourse?.title ?: "N/A")
                        Spacer(Modifier.height(8.dp))
                        DetailRow("New Course", course?.title ?: "N/A")
                    } else {
                        DetailRow("Course Title", course?.title ?: "N/A")
                        DetailRow("Action", "Institutional Withdrawal")
                    }
                    
                    DetailRow("Log ID", "#HIST-${record.id}")
                    
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { selectedHistoryItem = null },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        @Suppress("DEPRECATION")
                        Text("Close Record", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showResignConfirm != null) {
        AlertDialog(
            onDismissRequest = { showResignConfirm = null },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                    }
                }
            },
            title = { 
                @Suppress("DEPRECATION")
                Text(
                    "Resign from Course", 
                    fontWeight = FontWeight.Black, 
                    textAlign = TextAlign.Center, 
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = { 
                Text(
                    "You have requested to resign from this course. Our dedicated administrative team is currently reviewing your request. Please note that this process may take up to 2-3 business days.", 
                    textAlign = TextAlign.Center
                ) 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onResignRequest(showResignConfirm!!)
                        showResignConfirm = null
                        showSuccessPopup = "You have requested to resign from the course. Our team is currently checking your request and will notify you once the process is complete."
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) { @Suppress("DEPRECATION") Text("Submit Request", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResignConfirm = null },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel", color = Color.Gray) }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showCourseChangeFlow != null) {
        StudentCourseChangeDialog(
            currentEnrollment = showCourseChangeFlow!!,
            allCourses = allCourses,
            onDismiss = { showCourseChangeFlow = null },
            onComplete = { newCourseId -> 
                onChangeRequest(showCourseChangeFlow!!, newCourseId)
                showCourseChangeFlow = null
                showSuccessPopup = "Your course change request has been submitted successfully. Our administrative staff is reviewing the transition details."
            }
        )
    }

    if (showApplicationDetails != null) {
        CourseApplicationDetailsDialog(
            enrollment = showApplicationDetails!!,
            courseTitle = allCourses.find { it.id == showApplicationDetails!!.courseId }?.title ?: "Unknown Course",
            userLocal = userLocal,
            onDismiss = { showApplicationDetails = null }
        )
    }

    if (showSuccessPopup != null) {
        AlertDialog(
            onDismissRequest = { showSuccessPopup = null },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                    }
                }
            },
            title = { 
                @Suppress("DEPRECATION")
                Text(
                    "Application Submitted", 
                    fontWeight = FontWeight.Black, 
                    textAlign = TextAlign.Center, 
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = { Text(showSuccessPopup!!, textAlign = TextAlign.Center) },
            confirmButton = {
                Button(
                    onClick = { showSuccessPopup = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun CourseApplicationDetailsDialog(
    enrollment: CourseEnrollmentDetails,
    courseTitle: String,
    userLocal: UserLocal? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                @Suppress("DEPRECATION")
                Text(
                    "Application Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    courseTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                DetailRow("Status", enrollment.status)
                DetailRow("Submitted On", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(enrollment.submittedAt)))
                
                if (userLocal != null) {
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    @Suppress("DEPRECATION")
                    Text("User Profile Data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    DetailRow("Full Name", userLocal.name)
                    DetailRow("Email", userLocal.email)
                    DetailRow("Phone", userLocal.phoneNumber ?: "N/A")
                    DetailRow("Address", userLocal.address ?: "N/A")
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                @Suppress("DEPRECATION")
                Text("Enrollment Info", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                
                DetailRow("Nationality", enrollment.nationality)
                DetailRow("English Level", enrollment.englishProficiencyLevel)
                DetailRow("Last Qual.", enrollment.lastQualification)
                DetailRow("Institution", enrollment.institution)
                DetailRow("Graduation", enrollment.graduationYear)
                DetailRow("Date of Birth", enrollment.dateOfBirth)
                DetailRow("Gender", enrollment.gender)
                DetailRow("Emergency Contact", enrollment.emergencyContactName)
                DetailRow("Emergency Phone", enrollment.emergencyContactPhone)
                
                if (!enrollment.portfolioUrl.isNullOrEmpty()) {
                    DetailRow("Portfolio", enrollment.portfolioUrl)
                }
                if (!enrollment.specialSupportRequirements.isNullOrEmpty()) {
                    DetailRow("Support Req.", enrollment.specialSupportRequirements)
                }
                
                Spacer(Modifier.height(12.dp))
                @Suppress("DEPRECATION")
                Text("Motivational Statement:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                Text(
                    enrollment.motivationalText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp)
                )

                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        @Suppress("DEPRECATION")
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        @Suppress("DEPRECATION")
        Text(
            value, 
            style = MaterialTheme.typography.bodySmall, 
            fontWeight = FontWeight.Bold, 
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StudentCourseChangeDialog(
    currentEnrollment: CourseEnrollmentDetails,
    allCourses: List<Course>,
    onDismiss: () -> Unit,
    onComplete: (String) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCourses = remember(searchQuery, allCourses) {
        allCourses.filter { it.id != currentEnrollment.courseId && it.title.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentStep > 1) {
                            IconButton(onClick = { currentStep-- }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        }
                        @Suppress("DEPRECATION")
                        Text(
                            text = if (currentStep == 1) "Course Change Request" else "Select New Course",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                // Step Indicator
                Row(
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        val step = index + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(if (step <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentStep) {
                        1 -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.SwapHoriz, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                @Suppress("DEPRECATION")
                                Text("Reason for Change", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "You are requesting to change your current enrollment. This process involves selecting a new course and submitting it for administrative review.",
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    @Suppress("DEPRECATION")
                                    Text("Begin Selection", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.ChevronRight, null)
                                }
                            }
                        }
                        2 -> {
                            Column {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search available courses...") },
                                    leadingIcon = { Icon(Icons.Default.Search, null) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredCourses) { course ->
                                        val isSelected = selectedCourse?.id == course.id
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable { selectedCourse = course },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            ),
                                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                                     else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        ) {
                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    @Suppress("DEPRECATION")
                                                    Text(course.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                    @Suppress("DEPRECATION")
                                                    Text(course.department, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Button(
                                    onClick = { currentStep = 3 },
                                    enabled = selectedCourse != null,
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    @Suppress("DEPRECATION")
                                    Text("Confirm Selection", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        3 -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(80.dp))
                                Spacer(Modifier.height(24.dp))
                                @Suppress("DEPRECATION")
                                Text("Ready to Submit", fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(12.dp))
                                @Suppress("DEPRECATION")
                                Text("New Selection:", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                                @Suppress("DEPRECATION")
                                Text(selectedCourse?.title ?: "", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))

                                Spacer(Modifier.height(40.dp))
                                Button(
                                    onClick = { onComplete(selectedCourse!!.id) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    @Suppress("DEPRECATION")
                                    Text("Submit for Review", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
