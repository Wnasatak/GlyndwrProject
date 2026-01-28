package assignment1.krzysztofoko.s16001089.ui.details.book

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
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Detailed Information Screen for a Book item.
 * 
 * Provides a comprehensive overview of a specific book, including its cover, author, 
 * detailed description, and user reviews. It manages various interaction flows such 
 * as purchasing, adding to favorites, reading (if owned), and deleting from collection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,                   // Unique ID of the book to display
    initialBook: Book? = null,        // Pre-fetched book data (optional)
    user: FirebaseUser?,              // Current Firebase authentication state
    onLoginRequired: () -> Unit,      // Redirect callback for unauthenticated actions
    onBack: () -> Unit,               // Navigation return callback
    isDarkTheme: Boolean,             // App-wide theme state
    onToggleTheme: () -> Unit,        // Global theme toggle function
    onReadBook: (String) -> Unit,     // Triggers the PDF reader module
    onNavigateToProfile: () -> Unit,  // Link to profile for data editing
    onViewInvoice: (String) -> Unit,  // Triggers the digital receipt viewer
    // ViewModel initialization with custom factory for DAO injection
    viewModel: BookDetailViewModel = viewModel(factory = BookDetailViewModelFactory(
        bookDao = AppDatabase.getDatabase(LocalContext.current).bookDao(),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        bookId = bookId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Observe reactive state streams from the ViewModel
    val book by viewModel.book.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val inWishlist by viewModel.inWishlist.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    
    // UI Local State for interaction overlays
    var showOrderFlow by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }
    var showAddConfirm by remember { mutableStateOf(false) }
    var isProcessingAddition by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        // High-readability background with customized wave amplitudes
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = book?.title ?: AppConstants.TITLE_BOOK_DETAILS, 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.BTN_BACK) } },
                    actions = {
                        // Wishlist toggle visible only to logged-in users
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
            // CONDITIONAL CONTENT: Loading Spinner -> Item Not Found -> Product Details
            if (loading && book == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (book == null) {
                // Error display when product data cannot be resolved
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text(AppConstants.MSG_ITEM_NOT_FOUND)
                        TextButton(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                // Success: Product content is loaded
                book?.let { currentBook ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                        
                        // Header: Large product image with dynamic status overlays
                        item {
                            ProductHeaderImage(
                                book = currentBook,
                                isOwned = isOwned,
                                isDarkTheme = isDarkTheme,
                                primaryColor = primaryColor
                            )
                            Spacer(Modifier.height(24.dp))
                        }

                        // Info Section: Title, Author, Description, and Actions
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
                                    // Primary metadata
                                    Text(text = currentBook.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "${AppConstants.TEXT_BY} ${currentBook.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    
                                    // Metadata Chips
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                                        AssistChip(onClick = {}, label = { Text(currentBook.category) })
                                        AssistChip(onClick = {}, label = { Text(AppConstants.TEXT_ACADEMIC_MATERIAL) }) 
                                    }
                                    
                                    // Detailed description body
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(text = AppConstants.SECTION_ABOUT_ITEM, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = currentBook.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    // CONTEXTUAL ACTIONS: Owned vs Guest vs Purchase Flow
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            // Actions for items already in the user's library
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                ViewInvoiceButton(price = currentBook.price, onClick = { onViewInvoice(currentBook.id) })
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    // PDF Reader trigger
                                                    Button(onClick = { onReadBook(currentBook.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                        Icon(Icons.Default.AutoStories, null)
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(AppConstants.BTN_READ_NOW, fontWeight = FontWeight.Bold)
                                                    }
                                                    
                                                    // Allow deletion only for free digital items (hardcopy gear remains in library)
                                                    if (currentBook.price <= 0) {
                                                        OutlinedButton(
                                                            onClick = { showRemoveConfirm = true },
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
                                            // Call to action for unauthenticated users
                                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.LockPerson, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                    Spacer(Modifier.height(12.dp)); Text(AppConstants.TITLE_SIGN_IN_REQUIRED, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text(AppConstants.MSG_SIGN_IN_PROMPT_BOOK, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { 
                                                        Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(AppConstants.BTN_SIGN_IN_REGISTER) 
                                                    }
                                                }
                                            }
                                        } else {
                                            // Purchase Logic for authenticated users
                                            if (currentBook.price == 0.0) {
                                                // Free item collection flow
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = AppConstants.LABEL_FREE, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    Button(
                                                        onClick = { showAddConfirm = true },
                                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                                        shape = RoundedCornerShape(16.dp)
                                                    ) {
                                                        Icon(Icons.Default.LibraryAdd, null)
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(AppConstants.BTN_ADD_TO_LIBRARY, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                // Paid item checkout flow with student discount display
                                                val discountedPrice = currentBook.price * 0.9
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val pPrice = String.format(Locale.US, "%.2f", currentBook.price)
                                                        val dPrice = String.format(Locale.US, "%.2f", discountedPrice)
                                                        Text(text = "£$pPrice", style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                        Spacer(Modifier.width(12.dp)); Text(text = "£$dPrice", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text(AppConstants.TEXT_STUDENT_DISCOUNT, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                                                    Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showOrderFlow = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text(AppConstants.BTN_ORDER_NOW, fontWeight = FontWeight.Bold) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Social Section: Collaborative user reviews and ratings
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            ReviewSection(
                                productId = bookId,
                                reviews = allReviews,
                                localUser = localUser,
                                isLoggedIn = user != null,
                                db = AppDatabase.getDatabase(LocalContext.current),
                                isDarkTheme = isDarkTheme,
                                onReviewPosted = { scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_THANKS_REVIEW) } },
                                onLoginClick = onLoginRequired
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            }
        }

        /**
         * FLOW OVERLAY: Order and Payment
         * Handles the multi-step transaction process for paid items.
         */
        if (showOrderFlow && book != null) {
            OrderFlowDialog(
                book = book!!,
                user = localUser,
                onDismiss = { showOrderFlow = false },
                onEditProfile = { showOrderFlow = false; onNavigateToProfile() },
                onComplete = { finalPrice, orderRef -> 
                    viewModel.handlePurchaseComplete(context, finalPrice, orderRef) { msg ->
                        showOrderFlow = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            )
        }

        /**
         * FLOW OVERLAY: Removal Confirmation
         * Safety check before deleting an item from the local database.
         */
        AppPopups.RemoveFromLibraryConfirmation(
            show = showRemoveConfirm,
            bookTitle = book?.title ?: "",
            onDismiss = { showRemoveConfirm = false },
            onConfirm = {
                viewModel.removePurchase { msg ->
                    showRemoveConfirm = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )

        /**
         * FLOW OVERLAY: Free Item Collection
         * Handles immediate addition of free content to the user's library.
         */
        AppPopups.AddToLibraryConfirmation(
            show = showAddConfirm,
            itemTitle = book?.title ?: "",
            category = book?.mainCategory ?: AppConstants.CAT_BOOKS,
            isAudioBook = book?.isAudioBook ?: false,
            onDismiss = { showAddConfirm = false },
            onConfirm = {
                showAddConfirm = false
                isProcessingAddition = true
                scope.launch {
                    delay(2000) // Aesthetic delay to simulate processing
                    viewModel.addFreePurchase(context) { msg ->
                        isProcessingAddition = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            }
        )

        /**
         * Persistent Loading Overlay for asynchronous collection updates.
         */
        AppPopups.AddingToLibraryLoading(
            show = isProcessingAddition,
            category = book?.mainCategory ?: AppConstants.CAT_BOOKS,
            isAudioBook = book?.isAudioBook ?: false
        )
    }
}
