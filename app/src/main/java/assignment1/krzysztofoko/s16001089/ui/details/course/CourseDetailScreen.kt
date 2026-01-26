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
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Screen displaying detailed information about a University Course.
 * Follows the MVVM pattern with CourseDetailViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    user: FirebaseUser?,
    onLoginRequired: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onViewInvoice: (String) -> Unit,
    viewModel: CourseDetailViewModel = viewModel(factory = CourseDetailViewModelFactory(
        courseDao = AppDatabase.getDatabase(LocalContext.current).courseDao(),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        courseId = courseId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val course by viewModel.course.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val inWishlist by viewModel.inWishlist.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    
    var showOrderFlow by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(text = course?.title ?: "Course Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        if (user != null) {
                            IconButton(onClick = {
                                viewModel.toggleWishlist { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }) { Icon(imageVector = if (inWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Wishlist") }
                        }
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            if (loading && course == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (course == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text("Course details not found.")
                        TextButton(onClick = onBack) { Text("Go Back") }
                    }
                }
            } else {
                course?.let { currentCourse ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                        item {
                            // Reusing ProductHeaderImage but passing Book representation
                            ProductHeaderImage(
                                book = Book(id = currentCourse.id, title = currentCourse.title, imageUrl = currentCourse.imageUrl),
                                isOwned = isOwned,
                                isDarkTheme = isDarkTheme,
                                primaryColor = primaryColor
                            )
                            Spacer(Modifier.height(24.dp))
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(text = currentCourse.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "Department: ${currentCourse.department}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                                        AssistChip(onClick = {}, label = { Text(currentCourse.category) })
                                        if (currentCourse.isInstallmentAvailable) {
                                            AssistChip(onClick = {}, label = { Text("Installments Available") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp)) })
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp)); Text(text = "Course Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp)); Text(text = currentCourse.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Button(onClick = { /* Navigate to course content */ }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                    Icon(Icons.Default.School, null)
                                                    Spacer(Modifier.width(12.dp))
                                                    Text("Enter Classroom", fontWeight = FontWeight.Bold)
                                                }
                                                if (currentCourse.price > 0) {
                                                    OutlinedButton(
                                                        onClick = { onViewInvoice(currentCourse.id) },
                                                        modifier = Modifier.height(56.dp),
                                                        shape = RoundedCornerShape(16.dp),
                                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                                    ) {
                                                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null)
                                                    }
                                                } else {
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
                                        } else if (user == null) {
                                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.LockPerson, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                    Spacer(Modifier.height(12.dp)); Text("Enrollment Locked", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text("Please sign in to enroll in this university course.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { 
                                                        Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("Sign in to Enroll") 
                                                    }
                                                }
                                            }
                                        } else {
                                            if (currentCourse.price == 0.0) {
                                                Button(
                                                    onClick = {
                                                        viewModel.addFreePurchase { msg ->
                                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                                    shape = RoundedCornerShape(16.dp)
                                                ) {
                                                    Icon(Icons.Default.AddTask, null)
                                                    Spacer(Modifier.width(12.dp))
                                                    Text("Enroll for Free", fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                val discountedPrice = currentCourse.price * 0.9
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "£${String.format(Locale.US, "%.2f", currentCourse.price)}", style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                        Spacer(Modifier.width(12.dp)); Text(text = "£${String.format(Locale.US, "%.2f", discountedPrice)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text("STUDENT RATE (-10%)", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                                                    if (currentCourse.isInstallmentAvailable) {
                                                        Text(text = "or £${String.format(Locale.US, "%.2f", currentCourse.modulePrice)} per module", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
                                                    }
                                                    Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showOrderFlow = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Enroll Now", fontWeight = FontWeight.Bold) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            ReviewSection(
                                productId = courseId,
                                reviews = allReviews,
                                localUser = localUser,
                                isLoggedIn = user != null,
                                db = AppDatabase.getDatabase(LocalContext.current),
                                isDarkTheme = isDarkTheme,
                                onReviewPosted = { scope.launch { snackbarHostState.showSnackbar("Review submitted!") } },
                                onLoginClick = onLoginRequired
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            }
        }

        if (showOrderFlow && course != null) {
            // Mapping Course to Book for existing OrderFlowDialog
            val courseAsBook = Book(
                id = course!!.id,
                title = course!!.title,
                price = course!!.price,
                imageUrl = course!!.imageUrl,
                category = course!!.category,
                mainCategory = course!!.mainCategory,
                isInstallmentAvailable = course!!.isInstallmentAvailable,
                modulePrice = course!!.modulePrice
            )
            AppPopups.OrderPurchase(
                show = showOrderFlow,
                book = courseAsBook,
                user = localUser,
                onDismiss = { showOrderFlow = false },
                onEditProfile = { showOrderFlow = false; onNavigateToProfile() },
                onComplete = { 
                    showOrderFlow = false
                    scope.launch { snackbarHostState.showSnackbar("Enrollment successful! Access your course in the Dashboard.") }
                }
            )
        }

        AppPopups.RemoveFromLibraryConfirmation(
            show = showRemoveConfirmation,
            bookTitle = course?.title ?: "",
            onDismiss = { showRemoveConfirmation = false },
            onConfirm = {
                viewModel.removePurchase { msg ->
                    showRemoveConfirmation = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )
    }
}
