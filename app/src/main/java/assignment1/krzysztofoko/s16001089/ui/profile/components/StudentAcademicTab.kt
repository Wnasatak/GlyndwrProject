package assignment1.krzysztofoko.s16001089.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
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
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.EnrollmentStatusBadge
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth
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
    grades: List<Grade>,
    allCourses: List<Course>,
    onResignRequest: (CourseEnrollmentDetails) -> Unit = {},
    onChangeRequest: (CourseEnrollmentDetails, String) -> Unit = { _, _ -> }
) {
    var showResignConfirm by remember { mutableStateOf<CourseEnrollmentDetails?>(null) }
    var showCourseChangeFlow by remember { mutableStateOf<CourseEnrollmentDetails?>(null) }
    var showSuccessPopup by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { SectionHeaderDetails("My Course Enrollments") }

        if (enrollments.isEmpty()) {
            item { 
                Text(
                    "No active course enrollments.", 
                    color = Color.Gray, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                ) 
            }
        } else {
            items(enrollments) { enrollment ->
                val course = allCourses.find { it.id == enrollment.courseId }
                val requestedCourse = enrollment.requestedCourseId?.let { reqId -> allCourses.find { it.id == reqId } }
                val isPending = enrollment.status == "PENDING_REVIEW"
                val isChangeRequest = requestedCourse != null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                @Suppress("DEPRECATION")
                                Text(
                                    text = course?.title ?: "Unknown Course", 
                                    fontWeight = FontWeight.Black, 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Course ID: ${enrollment.courseId}", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray
                                )
                            }
                            EnrollmentStatusBadge(status = enrollment.status)
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
                        @Suppress("DEPRECATION")
                        Text(
                            text = sdf.format(Date(enrollment.submittedAt)), 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isPending) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HourglassEmpty, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Your application is being checked",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { showCourseChangeFlow = enrollment },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Change Course", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showResignConfirm = enrollment },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error, 
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Resign", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
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
                ) { Text("Submit Request", fontWeight = FontWeight.Bold) }
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
                            fontWeight = FontWeight.Black
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
                                Text("New Selection:", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                                Text(selectedCourse?.title ?: "", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))

                                Spacer(Modifier.height(40.dp))
                                Button(
                                    onClick = { onComplete(selectedCourse!!.id) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
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
