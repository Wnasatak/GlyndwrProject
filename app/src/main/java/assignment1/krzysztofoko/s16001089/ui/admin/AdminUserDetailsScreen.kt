package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.*
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Screen for Administrators to view and manage specific student details.
 * Contains tabs for Info, Activity, Comments, Academic, Invoices, and Wallet history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailsScreen(
    userId: String,
    onBack: () -> Unit,
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: AdminUserDetailsViewModel = viewModel(factory = AdminUserDetailsViewModelFactory(
        userId = userId,
        db = AppDatabase.getDatabase(LocalContext.current)
    ))
) {
    val user by viewModel.user.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val browseHistory by viewModel.browseHistory.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val installments by viewModel.courseInstallments.collectAsState()
    val grades by viewModel.userGrades.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()
    val purchasedBooks by viewModel.purchasedBooks.collectAsState()
    val commentedBooks by viewModel.commentedBooks.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Info", Icons.Default.Info),
        TabItem("Activity", Icons.Default.Timeline),
        TabItem("Comments", Icons.AutoMirrored.Filled.Comment),
        TabItem("Academic", Icons.Default.School),
        TabItem("Invoices", Icons.AutoMirrored.Filled.ReceiptLong),
        TabItem("Wallet", Icons.Default.AccountBalanceWallet)
    )

    // Looping Tab Row State
    val infiniteCount = Int.MAX_VALUE
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % tabs.size)
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = startPosition)

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
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
                                        photoUrl = user?.photoUrl,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
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
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") 
                                } 
                            },
                            actions = { 
                                IconButton(onClick = onToggleTheme) { 
                                    Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) 
                                } 
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                        
                        // Icon-driven Looping Tab Navigation
                        LazyRow(
                            state = tabListState,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(infiniteCount) { index ->
                                val tabIndex = index % tabs.size
                                val tab = tabs[tabIndex]
                                val isSelected = selectedTab == tabIndex
                                
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { selectedTab = tabIndex }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = tab.icon, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        AnimatedVisibility(visible = isSelected) {
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
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContentTransition"
                ) { targetIndex ->
                    when (targetIndex) {
                        0 -> UserInfoTab(user, viewModel)
                        1 -> UserActivityTab(
                            browseHistory = browseHistory, 
                            wishlist = wishlist, 
                            searchHistory = searchHistory, 
                            purchasedBooks = purchasedBooks, 
                            commentedBooks = commentedBooks, 
                            viewModel = viewModel,
                            onNavigateToBook = { bookId ->
                                navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$bookId")
                            }
                        )
                        2 -> UserCommentsTab(allReviews, viewModel)
                        3 -> UserAcademicTab(installments, grades, allBooks)
                        4 -> UserInvoicesTab(invoices)
                        5 -> UserWalletTab(transactions)
                    }
                }
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)
