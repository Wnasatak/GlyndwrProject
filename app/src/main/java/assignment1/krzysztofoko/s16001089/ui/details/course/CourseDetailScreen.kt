package assignment1.krzysztofoko.s16001089.ui.details.course

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * CourseDetailScreen.kt
 *
 * This file contains the primary UI implementation for the university course details screen.
 * It manages a complex enrolment workflow, allowing users to view course descriptions,
 * apply for courses, track their application status, and finalise enrolment for both
 * free and paid academic programmes.
 */

/**
 * CourseDetailScreen Composable
 *
 * An immersive, data-driven screen that serves as the gateway to academic courses.
 *
 * Key features:
 * - **Application Workflow:** Displays different UI states based on the user's application
 *   status (PENDING, APPROVED, REJECTED).
 * - **Constraint Handling:** Prevents users from enrolling in multiple paid courses
 *   simultaneously, maintaining university policy.
 * - **Responsive Layout:** Adapts content widths and spacing for optimal display on tablets.
 * - **Dynamic Actions:** Switches primary actions between "Apply", "Complete Enrolment",
 *   and "Enter Classroom" based on the user's current status.
 *
 * @param courseId Unique identifier for the course.
 * @param user The current Firebase user session.
 * @param onBack Callback for navigation.
 * @param onLoginRequired Callback to trigger the auth flow for guests.
 * @param onEnterClassroom Callback to open the course's virtual learning environment.
 * @param onStartEnrollment Callback to begin the formal application process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    user: FirebaseUser?,
    onBack: () -> Unit,
    onLoginRequired: () -> Unit,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onNavigateToProfile: () -> Unit,
    onViewInvoice: (String) -> Unit,
    onEnterClassroom: (String) -> Unit,
    onStartEnrollment: (String) -> Unit,
    viewModel: CourseDetailViewModel = viewModel(factory = CourseDetailViewModelFactory(
        courseDao = AppDatabase.getDatabase(LocalContext.current).courseDao(),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        courseId = courseId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    // Determine if the background should use dark mode variants.
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM

    val isTablet = isTablet()

    // --- VIEWMODEL STATE OBSERVATION --- //
    val course by viewModel.course.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val applicationDetails by viewModel.applicationDetails.collectAsState()
    val enrolledPaidCourseTitle by viewModel.enrolledPaidCourseTitle.collectAsState()
    val inWishlist by viewModel.inWishlist.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    // --- UI INTERACTION FLAGS --- //
    var showOrderFlow by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        // Branded background layer for visual depth.
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent, // Let the wavy background shine through.
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            text = course?.title ?: AppConstants.TITLE_COURSE_DETAILS,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.BTN_BACK) } },
                    actions = {
                        // Wishlist functionality for authenticated users.
                        if (user != null) {
                            IconButton(onClick = {
                                viewModel.toggleWishlist { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }) {
                                Icon(
                                    imageVector = if (inWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Wishlist"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            // --- MAIN CONTENT AREA --- //
            if (loading && course == null) {
                // Initial data fetch loading state.
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (course == null) {
                // Handle case where course data is missing.
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text(AppConstants.MSG_COURSE_NOT_FOUND)
                        @Suppress("DEPRECATION")
                        TextButton(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                course?.let { currentCourse ->
                    AdaptiveScreenContainer(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        maxWidth = 650.dp
                    ) { isTablet ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = if (isTablet) 32.dp else 12.dp, vertical = 16.dp)
                        ) {
                            // Section 1: Dynamic Header.
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Box(modifier = if (isTablet) Modifier.widthIn(max = 500.dp) else Modifier.fillMaxWidth()) {
                                        ProductHeaderImage(book = currentCourse.toBook(), isOwned = isOwned, isDarkTheme = isDarkTheme, primaryColor = primaryColor)
                                    }
                                }
                                Spacer(Modifier.height(if (isTablet) 32.dp else 24.dp))
                            }

                            // Section 2: Course Metadata and Action Zone.
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                    shape = RoundedCornerShape(if (isTablet) 32.dp else 24.dp),
                                    border = BorderStroke(1.dp, if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                                ) {
                                    Column(modifier = Modifier.padding(if (isTablet) 32.dp else 20.dp)) {
                                        @Suppress("DEPRECATION")
                                        Text(text = currentCourse.title, style = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                        @Suppress("DEPRECATION")
                                        Text(text = "${AppConstants.TEXT_DEPARTMENT}: ${currentCourse.department}", style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            AssistChip(onClick = {}, label = { Text(currentCourse.category) })
                                            if (currentCourse.isInstallmentAvailable) {
                                                AssistChip(
                                                    onClick = {},
                                                    label = { Text(AppConstants.TEXT_INSTALLMENTS_AVAILABLE, style = MaterialTheme.typography.labelSmall) },
                                                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null, Modifier.size(14.dp)) }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(if (isTablet) 40.dp else 24.dp)); @Suppress("DEPRECATION") Text(text = AppConstants.SECTION_DESCRIPTION_COURSE, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp)); @Suppress("DEPRECATION") Text(text = currentCourse.description, style = MaterialTheme.typography.bodyLarge, lineHeight = if (isTablet) 28.sp else 24.sp)

                                        Spacer(modifier = Modifier.height(if (isTablet) 40.dp else 32.dp))

                                        // --- ENROLMENT ACTION LOGIC --- //
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            if (isOwned) {
                                                // 1. ALREADY ENROLLED
                                                Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                    ViewInvoiceButton(price = currentCourse.price, onClick = { onViewInvoice(currentCourse.id) }, modifier = if (isTablet) Modifier.widthIn(max = 400.dp) else Modifier.fillMaxWidth())
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = if (isTablet) Modifier.widthIn(max = 500.dp) else Modifier.fillMaxWidth()) {
                                                        Button(onClick = { onEnterClassroom(currentCourse.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                            Icon(Icons.Default.School, null)
                                                            Spacer(Modifier.width(12.dp))
                                                            @Suppress("DEPRECATION")
                                                            Text(AppConstants.BTN_ENTER_CLASSROOM, fontWeight = FontWeight.Bold)
                                                        }
                                                        // Free courses can be removed.
                                                        if (currentCourse.price <= 0) {
                                                            @Suppress("DEPRECATION")
                                                            OutlinedButton(onClick = { showRemoveConfirmation = true }, modifier = Modifier.height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))) {
                                                                Icon(Icons.Default.DeleteOutline, null)
                                                            }
                                                        }
                                                    }
                                                }
                                            } else if (user == null) {
                                                // 2. GUEST USER (Auth required)
                                                Card(modifier = if (isTablet) Modifier.widthIn(max = 500.dp).align(Alignment.Center) else Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(Icons.Default.LockPerson, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                        Spacer(Modifier.height(12.dp)); @Suppress("DEPRECATION") Text(AppConstants.TITLE_ENROLLMENT_LOCKED, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                        Text(AppConstants.MSG_SIGN_IN_PROMPT_COURSE, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                        Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                                        Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        @Suppress("DEPRECATION")
                                                        Text(AppConstants.BTN_SIGN_IN_ENROLL)
                                                    }
                                                    }
                                                }
                                            } else if (currentCourse.price > 0 && enrolledPaidCourseTitle != null) {
                                                // 3. CONFLICT: Already enrolled in another paid programme.
                                                Card(modifier = if (isTablet) Modifier.widthIn(max = 500.dp).align(Alignment.Center) else Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))) {
                                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                                        Spacer(Modifier.width(16.dp))
                                                        @Suppress("DEPRECATION")
                                                        Column {
                                                            Text(text = "Already Enrolled", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                                            Text(text = "You are currently enrolled in: '$enrolledPaidCourseTitle'. You can only be enrolled in one paid course at a time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                                        }
                                                    }
                                                }
                                            } else {
                                                // 4. APPLICATION STATE HANDLING
                                                when (applicationDetails?.status) {
                                                    "PENDING_REVIEW" -> {
                                                        // Case: Waiting for staff assessment.
                                                        Card(modifier = if (isTablet) Modifier.widthIn(max = 500.dp).align(Alignment.Center) else Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))) {
                                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.PendingActions, null, tint = MaterialTheme.colorScheme.secondary)
                                                                Spacer(Modifier.width(16.dp))
                                                                Text("Application Pending Review. We will notify you once staff has assessed your details.", style = MaterialTheme.typography.bodyMedium)
                                                            }
                                                        }
                                                    }
                                                    "APPROVED" -> {
                                                        // Case: Application successful, ready for payment/final enrolment.
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                            Card(modifier = if (isTablet) Modifier.widthIn(max = 500.dp).padding(bottom = 16.dp) else Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                                                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                                                                    Spacer(Modifier.width(12.dp))
                                                                    Text("Your application was APPROVED! Please complete enrolment below.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                                                }
                                                            }

                                                            if (currentCourse.price == 0.0) {
                                                                Button(onClick = { viewModel.finalizeEnrollment(context) { } }, modifier = if (isTablet) Modifier.width(400.dp).height(56.dp) else Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                                    Text("Complete Free Enrolment", fontWeight = FontWeight.Bold)
                                                                }
                                                            } else {
                                                                val discountedPrice = currentCourse.price * 0.9 // Fixed 10% student discount.
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(text = "£" + String.format(Locale.US, "%.2f", discountedPrice), style = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                                }
                                                                Spacer(modifier = Modifier.height(16.dp))
                                                                Button(onClick = { showOrderFlow = true }, modifier = if (isTablet) Modifier.width(400.dp).height(56.dp) else Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                                    @Suppress("DEPRECATION")
                                                                    Text("Pay & Enrol Now", fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    "REJECTED" -> {
                                                        // Case: Application declined.
                                                        Card(modifier = if (isTablet) Modifier.widthIn(max = 500.dp).align(Alignment.Center) else Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))) {
                                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                                                                Spacer(Modifier.width(16.dp))
                                                                Text("Application Declined. Please contact academic services for more information.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        // Case: No active application. Start the process.
                                                        if (currentCourse.price == 0.0) {
                                                            // Free courses skip the application/review phase.
                                                            Button(
                                                                onClick = {
                                                                    viewModel.finalizeEnrollment(context) {
                                                                        scope.launch { snackbarHostState.showSnackbar("Successfully Enrolled!") }
                                                                    }
                                                                },
                                                                modifier = if (isTablet) Modifier.width(400.dp).height(56.dp) else Modifier.fillMaxWidth().height(56.dp),
                                                                shape = RoundedCornerShape(16.dp)
                                                            ) {
                                                                Icon(Icons.Default.School, null)
                                                                Spacer(Modifier.width(12.dp))
                                                                @Suppress("DEPRECATION")
                                                                Text(AppConstants.BTN_ENROLL_NOW, fontWeight = FontWeight.Bold)
                                                            }
                                                        } else {
                                                            // Paid courses require a formal application.
                                                            Button(onClick = { onStartEnrollment(currentCourse.id) }, modifier = if (isTablet) Modifier.width(400.dp).height(56.dp) else Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                                Icon(Icons.Default.Assignment, null); Spacer(Modifier.width(12.dp)); Text("Apply for Course", fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 3: Shared User Reviews.
                            item {
                                Spacer(modifier = Modifier.height(32.dp))
                                ReviewSection(productId = courseId, reviews = allReviews, localUser = localUser, isLoggedIn = user != null, db = AppDatabase.getDatabase(LocalContext.current), isDarkTheme = isDarkTheme, onReviewPosted = { scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_THANKS_REVIEW) } }, onLoginClick = onLoginRequired)
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }

        // --- SECONDARY DIALOGS AND OVERLAYS --- //

        // Workflow for paid course enrolment.
        if (showOrderFlow && course != null) {
            AppPopups.OrderPurchase(
                show = showOrderFlow,
                book = course!!.toBook(),
                user = localUser,
                onDismiss = { showOrderFlow = false },
                onEditProfile = { showOrderFlow = false; onNavigateToProfile() },
                onComplete = { finalPrice, orderRef ->
                    viewModel.finalizeEnrollment(context, isPaid = true, finalPrice = finalPrice, orderRef = orderRef) {
                        showOrderFlow = false
                        scope.launch { snackbarHostState.showSnackbar("Enrolment Complete!") }
                    }
                }
            )
        }

        // Confirmation for deleting free academic material.
        AppPopups.RemoveFromLibraryConfirmation(show = showRemoveConfirmation, bookTitle = course?.title ?: "", isCourse = true, onDismiss = { showRemoveConfirmation = false }, onConfirm = { viewModel.removePurchase { msg -> showRemoveConfirmation = false; scope.launch { snackbarHostState.showSnackbar(msg) } } })
    }
}
