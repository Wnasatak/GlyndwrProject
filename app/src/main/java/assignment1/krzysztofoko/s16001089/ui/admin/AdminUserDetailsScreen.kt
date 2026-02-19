package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.admin.components.users.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.*

/**
 * AdminUserDetailsScreen.kt
 *
 * This screen provides university administrators with a comprehensive 360-degree view of a 
 * specific student's profile and system activity. It serves as a central hub for student 
 * auditing, financial verification, and academic status management.
 *
 * Architecture & Design:
 * - **State Isolation:** The ViewModel is scoped to the specific userId to prevent data leakage.
 * - **Modular UI:** Content is split into domain-specific tabs (Info, Academic, Wallet, etc.).
 * - **Responsiveness:** Implements a dynamic tab navigation bar that adapts based on screen width.
 * - **Motion Design:** Uses AnimatedContent for smooth transitions between different data views.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailsScreen(
    userId: String, // The primary key of the user being audited.
    onBack: () -> Unit, // Navigation callback to exit the detail view.
    navController: NavController, // Used for navigating to related products or invoices.
    currentTheme: Theme, // Current system-wide theme enum.
    onThemeChange: (Theme) -> Unit, // Callback to update the system theme.
    // ViewModel setup: Injects dependencies and binds to the lifecycle of this screen.
    viewModel: AdminUserDetailsViewModel = viewModel(factory = AdminUserDetailsViewModelFactory(
        userId = userId,
        db = AppDatabase.getDatabase(LocalContext.current)
    ))
) {
    // --- 1. REACTIVE STATE OBSERVATION ---
    // We observe multiple data streams from the Room database. 
    // Any update to these records (e.g. a new invoice or a grade change) 
    // will trigger an automatic UI update for the active tab.
    
    val user by viewModel.user.collectAsState() 
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

    // --- 2. THEMATIC STYLING ---
    // Logic to determine if aesthetic backgrounds (wavy patterns) should use dark or light variants.
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || (currentTheme == Theme.CUSTOM)
    
    // --- 3. TAB NAVIGATION CONFIGURATION ---
    // selectedTab: Index of the currently active administrative module.
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Definition of the primary audit modules.
    val tabs = listOf(
        TabItem("Info", Icons.Default.Info),
        TabItem("Activity", Icons.Default.Timeline),
        TabItem("Comments", Icons.AutoMirrored.Filled.Comment),
        TabItem("Academic", Icons.Default.School),
        TabItem("Invoices", Icons.AutoMirrored.Filled.ReceiptLong),
        TabItem("Wallet", Icons.Default.AccountBalanceWallet)
    )

    // RESPONSIVE NAV BAR LOGIC:
    // On mobile, we use a very high item count to simulate an infinite circular carousel.
    // On tablets (isTablet == true), we reset to a standard, non-looping centered layout.
    val isTablet = isTablet()
    val infiniteCount = Int.MAX_VALUE
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % tabs.size)
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = if (isTablet) 0 else startPosition)

    Box(modifier = Modifier.fillMaxSize()) {
        // Universal branded background for the administration portal.
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent, // Ensures the wavy background is visible.
            topBar = {
                // --- INTEGRATED APP BAR & TAB BAR ---
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        // PRIMARY ROW: User summary and theme controls.
                        TopAppBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            title = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Displays the student's profile picture or initial.
                                    UserAvatar(
                                        photoUrl = user?.photoUrl,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        // Build identity string including institutional titles (e.g. Dr. Nilson).
                                        val displayName = buildString {
                                            if (!user?.title.isNullOrEmpty()) {
                                                append(user?.title)
                                                append(" ")
                                            }
                                            append(user?.name ?: "User Details")
                                        }
                                        Text(displayName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                        Text(user?.email ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            },
                            navigationIcon = { 
                                IconButton(onClick = onBack) { 
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Directory") 
                                } 
                            },
                            actions = { 
                                // Quick theme toggle for the auditing administrator.
                                ThemeToggleButton(
                                    currentTheme = currentTheme,
                                    onThemeChange = onThemeChange,
                                    isLoggedIn = true // Set to true because Admin is authenticated.
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                        
                        // SECONDARY ROW: The scrollable module selector (Tabs).
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            LazyRow(
                                state = tabListState,
                                modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth().height(48.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = if (isTablet) Arrangement.Center else Arrangement.Start
                            ) {
                                if (isTablet) {
                                    // Static list rendering for wide screens.
                                    items(tabs.size) { index ->
                                        TabItemView(
                                            tab = tabs[index],
                                            isSelected = selectedTab == index,
                                            onSelect = { selectedTab = index }
                                        )
                                    }
                                } else {
                                    // Looping carousel rendering for standard mobile screens.
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
            // --- MAIN CONTENT AREA (AUDIT MODULES) ---
            // We use AnimatedContent to provide professional fade transitions when switching tabs.
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Adaptive container ensures content stays readable on tablets (max width constraint).
                AdaptiveScreenContainer(
                    maxWidth = AdaptiveWidths.Wide
                ) { _ ->
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabContentTransition"
                    ) { targetIndex ->
                        // Dispatcher: Renders the appropriate audit component based on selected tab.
                        when (targetIndex) {
                            0 -> UserInfoTab(user, viewModel) // Core profile data & account settings.
                            1 -> UserActivityTab( // Comprehensive activity trail (browsing, history).
                                browseHistory = browseHistory, 
                                wishlist = wishlist, 
                                searchHistory = searchHistory, 
                                purchasedBooks = purchasedBooks, 
                                commentedBooks = commentedBooks, 
                                allReviews = allReviews,
                                allInvoices = invoices,
                                onDeleteComment = { viewModel.deleteComment(it) },
                                onUpdateReview = { viewModel.updateReview(it) },
                                onNavigateToBook = { bookId ->
                                    navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$bookId")
                                }
                            )
                            2 -> UserCommentsTab( // Social feedback auditing and moderation tools.
                                reviews = allReviews, 
                                allBooks = allBooks,
                                isAdmin = true,
                                onDeleteComment = { viewModel.deleteComment(it) },
                                onUpdateReview = { viewModel.updateReview(it) }
                            )
                            3 -> UserAcademicTab( // Enrollment status and Grade Book records.
                                enrollments = enrollments, 
                                grades = grades, 
                                allCourses = allCourses,
                                onUpdateStatus = { eid, status -> viewModel.updateEnrollmentStatus(eid, status) }
                            )
                            4 -> UserInvoicesTab(invoices) // Billing audit and PDF receipt generation.
                            5 -> UserWalletTab(transactions) // Ledger auditing for all currency flows.
                        }
                    }
                }
            }
        }
    }
}

/**
 * TabItemView Composable
 *
 * A specialized interactive chip for the tab bar.
 * Uses a 'pill' design that expands to reveal its label when active.
 *
 * @param tab Configuration data for the tab (title, icon).
 * @param isSelected Flag indicating if this tab is the active one.
 * @param onSelect Navigation callback triggered on click.
 */
@Composable
fun TabItemView(tab: TabItem, isSelected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            // Semantic coloring: Active tabs highlight with the primary brand color.
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = tab.icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp),
                // Adjusts contrast based on selection state.
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            // Animated reveal of the text label for a polished feel.
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Text(
                    text = tab.title,
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Data model for configuring detail view tabs.
 */
data class TabItem(val title: String, val icon: ImageVector)
