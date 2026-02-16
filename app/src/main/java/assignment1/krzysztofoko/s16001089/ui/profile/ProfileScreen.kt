package assignment1.krzysztofoko.s16001089.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.TabItem
import assignment1.krzysztofoko.s16001089.ui.admin.TabItemView
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.profile.components.StudentAcademicTab
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Composable representing the User Profile and Settings screen.
 * Re-designed to follow the professional Admin/Tutor user details layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        userThemeDao = AppDatabase.getDatabase(LocalContext.current).userThemeDao(),
        auditDao = AppDatabase.getDatabase(LocalContext.current).auditDao(),
        courseDao = AppDatabase.getDatabase(LocalContext.current).courseDao(),
        classroomDao = AppDatabase.getDatabase(LocalContext.current).classroomDao(),
        bookRepository = BookRepository(AppDatabase.getDatabase(LocalContext.current)),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val localUser by viewModel.localUser.collectAsState(initial = null)
    val userTheme by viewModel.userTheme.collectAsState(initial = null)

    val invoices by viewModel.invoices.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val browseHistory by viewModel.browseHistory.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val enrollments by viewModel.courseEnrollments.collectAsState()
    val grades by viewModel.userGrades.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()
    val purchasedBooks by viewModel.purchasedBooks.collectAsState()
    val commentedBooks by viewModel.commentedBooks.collectAsState()

    LaunchedEffect(localUser, userTheme) {
        localUser?.let { viewModel.initFields(it, userTheme) }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Profile", Icons.Rounded.AccountCircle),
        TabItem("Activity", Icons.Default.Timeline),
        TabItem("Comments", Icons.AutoMirrored.Filled.Comment),
        TabItem("Academic", Icons.Default.School),
        TabItem("Invoices", Icons.AutoMirrored.Filled.ReceiptLong),
        TabItem("Wallet", Icons.Default.AccountBalanceWallet)
    )

    val isTablet = isTablet()
    val infiniteCount = Int.MAX_VALUE
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % tabs.size)
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = if (isTablet) 0 else startPosition)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadAvatar(context, uri) { msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        TopAppBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(
                                        photoUrl = localUser?.photoUrl,
                                        modifier = Modifier.size(40.dp).clickable { photoPickerLauncher.launch("image/*") }
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        val displayName = buildString {
                                            if (!localUser?.title.isNullOrEmpty()) {
                                                append(localUser?.title)
                                                append(" ")
                                            }
                                            append(localUser?.name ?: "User Profile")
                                        }
                                        Text(displayName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                        Text(localUser?.email ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            },
                            actions = {
                                IconButton(onClick = onLogout) {
                                    Icon(Icons.AutoMirrored.Rounded.Logout, AppConstants.BTN_LOG_OUT, tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            LazyRow(
                                state = tabListState,
                                modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth().height(48.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = if (isTablet) Arrangement.Center else Arrangement.Start
                            ) {
                                if (isTablet) {
                                    items(tabs.size) { index ->
                                        TabItemView(
                                            tab = tabs[index],
                                            isSelected = selectedTab == index,
                                            onSelect = { selectedTab = index }
                                        )
                                    }
                                } else {
                                    items(infiniteCount) { index ->
                                        val tabIndex = index % tabs.size
                                        TabItemView(
                                            tab = tabs[tabIndex],
                                            isSelected = selectedTab == tabIndex,
                                            onSelect = { selectedTab = tabIndex }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AdaptiveScreenContainer(
                    maxWidth = AdaptiveWidths.Wide
                ) { isTablet ->
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabContentTransition"
                    ) { targetIndex ->
                        when (targetIndex) {
                            0 -> StudentDetailTab(viewModel, localUser, photoPickerLauncher, snackbarHostState)
                            1 -> UserActivityTab(
                                browseHistory = browseHistory,
                                wishlist = wishlist,
                                searchHistory = searchHistory,
                                purchasedBooks = purchasedBooks,
                                commentedBooks = commentedBooks,
                                allReviews = allReviews,
                                allInvoices = invoices,
                                onNavigateToBook = { bookId ->
                                    navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$bookId")
                                }
                            )
                            2 -> UserCommentsTab(reviews = allReviews, allBooks = allBooks)
                            3 -> StudentAcademicTab(
                                enrollments = enrollments,
                                grades = grades,
                                allCourses = allCourses,
                                onResignRequest = { viewModel.submitResignationRequest(it) { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } } },
                                onChangeRequest = { enrollment, newId -> viewModel.submitCourseChangeRequest(enrollment, newId) { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } } }
                            )
                            4 -> UserInvoicesTab(invoices)
                            5 -> UserWalletTab(transactions)
                        }
                    }
                }
            }
        }
    }
}

/**
 * The primary professional profile tab for students, matching the provided screenshot design.
 */
@Composable
fun StudentDetailTab(
    viewModel: ProfileViewModel,
    localUser: UserLocal?,
    photoPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    var isEditingSettings by remember { mutableStateOf(false) }

    // UI Mock data for biography/department as they are not in the current UserLocal schema yet
    var editBio by remember { mutableStateOf("Enrolled student focusing on advanced academic development and institutional research.") }
    var editDept by remember { mutableStateOf("Computer Science & Engineering") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PROFESSIONAL HEADER
        item {
            AdaptiveDashboardHeader(
                title = "My Profile",
                subtitle = "Your professional university identity",
                icon = Icons.Default.Badge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // IDENTITY CARD
        item {
            AdaptiveDashboardCard { cardIsTablet ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        photoUrl = localUser?.photoUrl,
                        modifier = Modifier.size(if (cardIsTablet) 120.dp else 100.dp),
                        isLarge = true,
                        onClick = { photoPickerLauncher.launch("image/*") }
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = localUser?.name ?: "Loading...",
                        style = if (cardIsTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = editDept,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.adaptiveButtonWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { isEditingSettings = !isEditingSettings },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Edit Info", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = { isEditingSettings = !isEditingSettings },
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    }
                }
            }
        }

        if (isEditingSettings) {
            item {
                AdaptiveDashboardCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Update Professional Info", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = editDept, onValueChange = { editDept = it }, label = { Text("Major / Department") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Biography") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))

                        Button(onClick = { isEditingSettings = false }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Text("Apply Changes")
                        }
                    }
                }
            }

            item {
                AdaptiveDashboardCard {
                    Column(modifier = Modifier.padding(8.dp)) {
                        PersonalInfoSection(
                            title = viewModel.title,
                            onTitleChange = { viewModel.title = it },
                            firstName = viewModel.firstName,
                            onFirstNameChange = { viewModel.firstName = it },
                            surname = viewModel.surname,
                            onSurnameChange = { viewModel.surname = it },
                            phoneNumber = viewModel.phoneNumber,
                            onPhoneNumberChange = { viewModel.phoneNumber = it },
                            email = localUser?.email ?: "",
                            onEditEmail = { /* Popup logic handled in viewModel */ }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.updateProfile { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                    isEditingSettings = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !viewModel.isUploading
                        ) {
                            if (viewModel.isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Save Account Details", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                AdaptiveDashboardCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Biography", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(text = editBio, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdaptiveDashboardCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Email", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(localUser?.email ?: "Not available", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    AdaptiveDashboardCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Student ID", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(localUser?.id?.take(8)?.uppercase() ?: "S16001089", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
