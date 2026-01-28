package assignment1.krzysztofoko.s16001089.ui.details.course

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Detailed Screen for University Course products.
 * 
 * This screen manages the enrollment workflow for academic courses. It provides
 * department info, course descriptions, and handles complex enrollment rules 
 * (e.g. limiting students to one paid course at a time). It also serves as the 
 * entry point to the digital Classroom environment for enrolled students.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,                 // Unique ID of the course to display
    user: FirebaseUser?,              // Current authentication state
    onBack: () -> Unit,               // Return navigation
    onLoginRequired: () -> Unit,      // Redirect to auth module
    onToggleTheme: () -> Unit,        // Global theme toggle
    isDarkTheme: Boolean,             // Global theme state
    onNavigateToProfile: () -> Unit,  // Profile edit link
    onViewInvoice: (String) -> Unit,  // Receipt viewer link
    onEnterClassroom: (String) -> Unit, // Direct entry to classroom modules
    // ViewModel initialization with manual dependency injection via factory
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

    // Observe reactive state flows from the ViewModel
    val course by viewModel.course.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val enrolledPaidCourseTitle by viewModel.enrolledPaidCourseTitle.collectAsState()
    val inWishlist by viewModel.inWishlist.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    
    // UI Local state for dialog management
    var showOrderFlow by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var showAddConfirm by remember { mutableStateOf(false) }
    var isProcessingAddition by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        // High-fidelity animated background
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent,
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
                        // User-specific favorites toggle
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
                        IconButton(onClick = onToggleTheme) { 
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) 
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            // CONDITIONAL UI: Loading -> Not Found -> Course Content
            if (loading && course == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (course == null) {
                // Error state for missing data
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text(AppConstants.MSG_COURSE_NOT_FOUND)
                        TextButton(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                // Success: Course metadata successfully loaded
                course?.let { currentCourse ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                        
                        // Header: Large product image with ownership status
                        item {
                            ProductHeaderImage(
                                book = currentCourse.toBook(),
                                isOwned = isOwned,
                                isDarkTheme = isDarkTheme,
                                primaryColor = primaryColor
                            )
                            Spacer(Modifier.height(24.dp))
                        }

                        // Info Section: Metadata, Installments, and Course Description
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(text = currentCourse.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "${AppConstants.TEXT_DEPARTMENT}: ${currentCourse.department}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    
                                    // Visual Metadata Chips
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                                        AssistChip(onClick = {}, label = { Text(currentCourse.category) })
                                        // Specific Course Badge for installment availability
                                        if (currentCourse.isInstallmentAvailable) {
                                            AssistChip(onClick = {}, label = { Text(AppConstants.TEXT_INSTALLMENTS_AVAILABLE) }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp)) })
                                        }
                                    }
                                    
                                    // Detailed Course Syllabus/Description
                                    Spacer(modifier = Modifier.height(24.dp)); Text(text = AppConstants.SECTION_DESCRIPTION_COURSE, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp)); Text(text = currentCourse.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    // DYNAMIC ACTION LOGIC: Handles Enrolled vs Guest vs New Enrollment
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            /**
                                             * STATE: ENROLLED
                                             * Provides access to academic modules and financial receipts.
                                             */
                                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                ViewInvoiceButton(price = currentCourse.price, onClick = { onViewInvoice(currentCourse.id) })

                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Button(onClick = { onEnterClassroom(currentCourse.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                        Icon(Icons.Default.School, null)
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(AppConstants.BTN_ENTER_CLASSROOM, fontWeight = FontWeight.Bold)
                                                    }
                                                    // Allow unenrollment only for free courses
                                                    if (currentCourse.price <= 0) {
                                                        OutlinedButton(
                                                            onClick = { showRemoveConfirmation = true }, 
                                                            modifier = Modifier.height(56.dp), 
                                                            shape = RoundedCornerShape(16.dp), 
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), 
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                                        ) { 
                                                            Icon(Icons.Default.DeleteOutline, null) 
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (user == null) {
                                            /**
                                             * STATE: GUEST
                                             * Calls for student authentication to unlock enrollment.
                                             */
                                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.LockPerson, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                    Spacer(Modifier.height(12.dp)); Text(AppConstants.TITLE_ENROLLMENT_LOCKED, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text(AppConstants.MSG_SIGN_IN_PROMPT_COURSE, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { 
                                                        Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(AppConstants.BTN_SIGN_IN_ENROLL) 
                                                    }
                                                }
                                            }
                                        } else {
                                            /**
                                             * STATE: AUTHENTICATED (NOT ENROLLED)
                                             * Handles free enrollment vs checkout flow.
                                             */
                                            if (currentCourse.price == 0.0) {
                                                // Instant Free Enrollment
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = AppConstants.LABEL_FREE, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    Button(onClick = { showAddConfirm = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                        Icon(Icons.Default.AddTask, null); Spacer(Modifier.width(12.dp)); Text(AppConstants.BTN_ENROLL_FREE, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                // Check for course collision rules (limit of one paid course)
                                                if (enrolledPaidCourseTitle != null) {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                                    ) {
                                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                                            Spacer(Modifier.width(16.dp))
                                                            Column {
                                                                Text(text = "Already Enrolled", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                                                Text(text = "You are currently enrolled in: '$enrolledPaidCourseTitle'. You can only be enrolled in one paid course at a time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    // Checkout flow with student discount and installment options
                                                    val discountedPrice = currentCourse.price * 0.9
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(text = "£" + String.format(Locale.US, "%.2f", currentCourse.price), style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                            Spacer(Modifier.width(12.dp))
                                                            Text(text = "£" + String.format(Locale.US, "%.2f", discountedPrice), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                        }
                                                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text(text = AppConstants.TEXT_STUDENT_RATE, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                                                        if (currentCourse.isInstallmentAvailable) {
                                                            Text(text = "or £" + String.format(Locale.US, "%.2f", currentCourse.modulePrice) + " per module", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
                                                        }
                                                        Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showOrderFlow = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text(AppConstants.BTN_ENROLL_NOW, fontWeight = FontWeight.Bold) }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Social Section: Student feedback and course ratings
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            ReviewSection(productId = courseId, reviews = allReviews, localUser = localUser, isLoggedIn = user != null, db = AppDatabase.getDatabase(LocalContext.current), isDarkTheme = isDarkTheme, onReviewPosted = { scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_THANKS_REVIEW) } }, onLoginClick = onLoginRequired)
                        }
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            }
        }

        // FLOW OVERLAY: Order and Academic Enrollment
        if (showOrderFlow && course != null) {
            AppPopups.OrderPurchase(show = showOrderFlow, book = course!!.toBook(), user = localUser, onDismiss = { showOrderFlow = false }, onEditProfile = { showOrderFlow = false; onNavigateToProfile() }, onComplete = { finalPrice, orderRef -> viewModel.handlePurchaseComplete(context, finalPrice, orderRef) { msg -> showOrderFlow = false; scope.launch { snackbarHostState.showSnackbar(msg) } } })
        }

        // FLOW OVERLAY: Permanent Deletion Confirmation
        AppPopups.RemoveFromLibraryConfirmation(show = showRemoveConfirmation, bookTitle = course?.title ?: "", isCourse = true, onDismiss = { showRemoveConfirmation = false }, onConfirm = { viewModel.removePurchase { msg -> showRemoveConfirmation = false; scope.launch { snackbarHostState.showSnackbar(msg) } } })

        // FLOW OVERLAY: Free Enrollment Confirmation
        AppPopups.AddToLibraryConfirmation(
            show = showAddConfirm,
            itemTitle = course?.title ?: "",
            category = AppConstants.CAT_COURSES,
            isAudioBook = false,
            onDismiss = { showAddConfirm = false },
            onConfirm = {
                showAddConfirm = false
                isProcessingAddition = true
                scope.launch {
                    delay(2000) // Visual delay for asynchronous DB enrollment
                    viewModel.addFreePurchase(context) { msg ->
                        isProcessingAddition = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            }
        )

        // Persistent Spinner for Background Enrollment Tasks
        AppPopups.AddingToLibraryLoading(
            show = isProcessingAddition,
            category = AppConstants.CAT_COURSES,
            isAudioBook = false
        )
    }
}
