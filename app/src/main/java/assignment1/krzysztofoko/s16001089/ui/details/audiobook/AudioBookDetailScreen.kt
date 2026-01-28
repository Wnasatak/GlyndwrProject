package assignment1.krzysztofoko.s16001089.ui.details.audiobook

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Detailed Screen for Audiobook products.
 * 
 * Provides an immersive view of an audiobook, including narration credits, descriptions,
 * and social reviews. Integrates with the global Media3 player for instant listening
 * and manages complex purchase and enrollment flows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioBookDetailScreen(
    bookId: String,                   // Unique identifier for the audiobook
    initialBook: Book? = null,        // Pre-loaded book data
    user: FirebaseUser?,              // Firebase user state for auth-locked actions
    onLoginRequired: () -> Unit,      // Callback to redirect to auth
    onBack: () -> Unit,               // Callback to return to the previous screen
    isDarkTheme: Boolean,             // Current global theme state
    onToggleTheme: () -> Unit,        // Function to flip theme
    onPlayAudio: (Book) -> Unit,      // Handler for the global media player
    onNavigateToProfile: () -> Unit,  // Link to user profile
    onViewInvoice: (String) -> Unit,  // Link to digital receipt viewer
    // ViewModel setup with custom factory for multi-DAO dependency injection
    viewModel: AudioBookViewModel = viewModel(factory = AudioBookViewModelFactory(
        bookDao = AppDatabase.getDatabase(LocalContext.current).bookDao(),
        audioBookDao = AppDatabase.getDatabase(LocalContext.current).audioBookDao(),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        bookId = bookId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Observation of reactive state streams from the ViewModel
    val book by viewModel.book.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val inWishlist by viewModel.inWishlist.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    
    // UI Local State for interaction dialogs
    var showOrderFlow by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var showAddConfirm by remember { mutableStateOf(false) }
    var isProcessingAddition by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        // High-readability wavy background consistent with product details theme
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = book?.title ?: AppConstants.TITLE_AUDIO_DETAILS, 
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
            // CONDITIONAL VIEW BRANCHING: Loading -> Error -> Product View
            if (loading && book == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (book == null) {
                // Not Found state logic
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text(AppConstants.MSG_AUDIOBOOK_NOT_FOUND)
                        TextButton(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                // Success: Product details resolved
                book?.let { currentBook ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                        
                        // Header Image Section with conditional 'Owned' badge
                        item {
                            ProductHeaderImage(
                                book = currentBook,
                                isOwned = isOwned,
                                isDarkTheme = isDarkTheme,
                                primaryColor = primaryColor
                            )
                            Spacer(Modifier.height(24.dp))
                        }

                        // Informational Card: Metadata, Description, and Purchases
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
                                    // Title and narration credits
                                    Text(text = currentBook.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "${AppConstants.TEXT_NARRATED_BY} ${currentBook.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    
                                    // Visual Chips for categorization
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                                        AssistChip(onClick = {}, label = { Text(currentBook.category) })
                                        AssistChip(onClick = {}, label = { Text(AppConstants.TEXT_AUDIO_CONTENT) }) 
                                    }
                                    
                                    // About/Description Section
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(text = AppConstants.SECTION_ABOUT_AUDIO, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = currentBook.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    // DYNAMIC ACTION BOX: Context-aware buttons (Play, Buy, Remove, Login)
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            /**
                                             * State: Owned
                                             * Provides listening control and financial review.
                                             */
                                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                ViewInvoiceButton(price = currentBook.price, onClick = { onViewInvoice(currentBook.id) })

                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Button(onClick = { onPlayAudio(currentBook) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                        Icon(Icons.Default.PlayCircleFilled, null)
                                                        Spacer(Modifier.width(12.dp))
                                                        @Suppress("DEPRECATION")
                                                        Text(AppConstants.BTN_LISTEN_NOW, fontWeight = FontWeight.Bold)
                                                    }
                                                    // Removal only permitted for free collectibles
                                                    if (currentBook.price <= 0) {
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
                                             * State: Guest
                                             * Locked call-to-action urging the user to sign in for collection access.
                                             */
                                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.LockPerson, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                    Spacer(Modifier.height(12.dp)); Text(AppConstants.TITLE_SIGN_IN_REQUIRED, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text(AppConstants.MSG_SIGN_IN_PROMPT_AUDIO, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { 
                                                        Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(AppConstants.BTN_SIGN_IN_REGISTER) 
                                                    }
                                                }
                                            }
                                        } else {
                                            /**
                                             * State: Authenticated but not owned
                                             * Checkout options for free vs paid audiobooks.
                                             */
                                            if (currentBook.price == 0.0) {
                                                // Instant Free Add
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
                                                // Paid item checkout with student discount display
                                                val discountedPrice = currentBook.price * 0.9
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "£${String.format(Locale.US, "%.2f", currentBook.price)}", style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                        Spacer(Modifier.width(12.dp)); Text(text = "£${String.format(Locale.US, "%.2f", discountedPrice)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text(AppConstants.TEXT_STUDENT_DISCOUNT, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                                                    Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showOrderFlow = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text(AppConstants.BTN_BUY_NOW, fontWeight = FontWeight.Bold) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Collaborative User Feedback Section
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
         * FLOW OVERLAY: Unified Order Management
         * Handles the multi-stage payment and validation process.
         */
        if (showOrderFlow && book != null) {
            AppPopups.OrderPurchase(
                show = showOrderFlow,
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
         * FLOW OVERLAY: Permanent Deletion Confirmation
         */
        AppPopups.RemoveFromLibraryConfirmation(
            show = showRemoveConfirmation,
            bookTitle = book?.title ?: "",
            onDismiss = { showRemoveConfirmation = false },
            onConfirm = {
                viewModel.removePurchase { msg ->
                    showRemoveConfirmation = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )

        /**
         * FLOW OVERLAY: Free Item Collection Confirmation
         */
        AppPopups.AddToLibraryConfirmation(
            show = showAddConfirm,
            itemTitle = book?.title ?: "",
            category = book?.mainCategory ?: AppConstants.CAT_AUDIOBOOKS,
            isAudioBook = true,
            onDismiss = { showAddConfirm = false },
            onConfirm = {
                showAddConfirm = false
                isProcessingAddition = true
                scope.launch {
                    delay(2000) // Visual buffer for background DB processing
                    viewModel.addFreePurchase(context) { msg ->
                        isProcessingAddition = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            }
        )

        /**
         * Dynamic Loading Spinner for Library Updates
         */
        AppPopups.AddingToLibraryLoading(
            show = isProcessingAddition,
            category = book?.mainCategory ?: AppConstants.CAT_AUDIOBOOKS,
            isAudioBook = true
        )
    }
}
