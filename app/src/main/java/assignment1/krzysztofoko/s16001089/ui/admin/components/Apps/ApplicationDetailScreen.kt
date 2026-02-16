package assignment1.krzysztofoko.s16001089.ui.admin.components.Apps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.admin.AdminApplicationItem
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * High-fidelity full-screen application review interface.
 * Optimized for tablets with AdaptiveScreenContainer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailScreen(
    app: AdminApplicationItem,
    onBack: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isDarkTheme: Boolean
) {
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }
    val isChangeRequest = app.details.requestedCourseId != null
    val isWithdrawal = app.details.isWithdrawal

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            when {
                                isWithdrawal -> "Review Withdrawal Request"
                                isChangeRequest -> "Review Course Change"
                                else -> "Review Application"
                            }, 
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        ) 
                    },
                    navigationIcon = { 
                        IconButton(onClick = onBack) { 
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null) 
                        } 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Header Card: Student Identity
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.98f) 
                                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp), 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                if (app.student?.photoUrl != null) {
                                    AsyncImage(
                                        model = app.student.photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = app.student?.name?.take(1) ?: "?", 
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            
                            Spacer(Modifier.width(20.dp))
                            
                            Column {
                                Text(
                                    text = app.student?.name ?: "Unknown Student", 
                                    fontWeight = FontWeight.Black, 
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = app.student?.email ?: "No Email Address", 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Withdrawal or Enrollment Transition Section
                    if (isWithdrawal) {
                        ApplicationSection("Withdrawal Information", Icons.Default.Warning, isDarkTheme) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "REASON: Institutional Withdrawal Request", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "The student has requested to resign from: ${app.course?.title ?: "Current Program"}", 
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else if (isChangeRequest) {
                        ApplicationSection("Enrollment Change Details", Icons.Default.SwapHoriz, isDarkTheme) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CURRENT", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(app.course?.title ?: "Unknown", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                }
                                
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("REQUESTED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(app.requestedCourse?.title ?: "New Program", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    } else {
                        ApplicationSection("Enrollment Program", Icons.Default.School, isDarkTheme) {
                            AppDetailRow(Icons.Default.Class, "Selected Course", app.course?.title ?: "Institutional Program")
                        }
                    }

                    // Information Sections
                    ApplicationSection("Personal Information", Icons.Default.Person, isDarkTheme) {
                        AppDetailRow(Icons.Default.Cake, "Date of Birth", app.details.dateOfBirth)
                        AppDetailRow(Icons.Default.Public, "Nationality", app.details.nationality)
                        AppDetailRow(Icons.Default.Wc, "Gender", app.details.gender)
                    }

                    ApplicationSection("Academic Background", Icons.Default.School, isDarkTheme) {
                        AppDetailRow(Icons.Default.HistoryEdu, "Last Qualification", app.details.lastQualification)
                        AppDetailRow(Icons.Default.Business, "Institution", app.details.institution)
                        AppDetailRow(Icons.Default.Event, "Graduation Year", app.details.graduationYear)
                        AppDetailRow(Icons.Default.Translate, "English Proficiency", app.details.englishProficiencyLevel)
                    }

                    ApplicationSection("Motivation Statement", Icons.Default.FormatQuote, isDarkTheme) {
                        @Suppress("DEPRECATION")
                        Text(
                            text = app.details.motivationalText, 
                            style = MaterialTheme.typography.bodyMedium, 
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Decision Buttons (Only for Pending)
                    if (app.details.status == "PENDING_REVIEW") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp), 
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Reject", fontWeight = FontWeight.Black)
                            }
                            
                            Button(
                                onClick = onApprove,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWithdrawal) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (isWithdrawal) "Confirm" else "Approve", fontWeight = FontWeight.Black)
                            }
                        }
                    } else {
                        // Show Current Status if not pending
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                            color = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = if (!isDarkTheme) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "APPLICATION STATUS: ",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    app.details.status,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (app.details.status == "APPROVED") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationSection(
    title: String, 
    icon: ImageVector,
    isDarkTheme: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title.uppercase(), 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.Black, 
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDarkTheme) 0.3f else 1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp), 
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun AppDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp), 
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
