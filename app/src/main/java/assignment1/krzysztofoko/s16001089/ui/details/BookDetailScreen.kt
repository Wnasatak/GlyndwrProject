package assignment1.krzysztofoko.s16001089.ui.details

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    initialBook: Book? = null,
    user: FirebaseUser?,
    onLoginRequired: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onReadBook: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onViewInvoice: (String) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var book by remember { mutableStateOf(initialBook) }
    var loading by remember { mutableStateOf(true) }

    val primaryColor = MaterialTheme.colorScheme.primary

    val localUser by remember(user) {
        if (user != null) db.userDao().getUserFlow(user.uid)
        else flowOf(null)
    }.collectAsState(initial = null)

    val currentUid = user?.uid ?: "guest"
    val wishlistIds by remember(currentUid) {
        if (user != null) db.userDao().getWishlistIds(user.uid) else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val purchaseIds by remember(currentUid) {
        if (user != null) db.userDao().getPurchaseIds(user.uid) else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val inWishlist = remember(wishlistIds) { wishlistIds.contains(bookId) }
    val isOwned = remember(purchaseIds) { purchaseIds.contains(bookId) }
    val allReviews by db.userDao().getReviewsForProduct(bookId).collectAsState(initial = emptyList())
    
    var showOrderFlow by remember { mutableStateOf(false) }

    LaunchedEffect(bookId, user) {
        loading = true
        if (book == null) book = db.bookDao().getBookById(bookId)
        if (user != null) db.userDao().addToHistory(HistoryItem(user.uid, bookId))
        loading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(text = book?.title ?: "Item Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        if (user != null) {
                            IconButton(onClick = {
                                scope.launch {
                                    if (inWishlist) {
                                        db.userDao().removeFromWishlist(user.uid, bookId)
                                        snackbarHostState.showSnackbar("Removed from favorites")
                                    } else {
                                        db.userDao().addToWishlist(WishlistItem(user.uid, bookId))
                                        snackbarHostState.showSnackbar("Added to favorites!")
                                    }
                                }
                            }) { Icon(imageVector = if (inWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Wishlist") }
                        }
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            if (loading && book == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (book == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text("Item details not available.")
                        TextButton(onClick = onBack) { Text("Go Back") }
                    }
                }
            } else {
                book?.let { currentBook ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                        item {
                            ProductHeaderImage(
                                book = currentBook,
                                isOwned = isOwned,
                                isDarkTheme = isDarkTheme,
                                primaryColor = primaryColor
                            )
                            Spacer(modifier = Modifier.height(24.dp))
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
                                    Text(text = currentBook.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "by ${currentBook.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AssistChip(onClick = {}, label = { Text(currentBook.category) }); AssistChip(onClick = {}, label = { Text("Academic Material") }) }
                                    Spacer(modifier = Modifier.height(24.dp)); Text(text = "About this item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp)); Text(text = currentBook.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                if (currentBook.price > 0) {
                                                    Button(onClick = { onViewInvoice(currentBook.id) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)) {
                                                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null); Spacer(Modifier.width(12.dp)); Text("View & Print Invoice", fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Button(onClick = { onReadBook(currentBook.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                        Icon(Icons.Default.AutoStories, null)
                                                        Spacer(Modifier.width(12.dp))
                                                        Text("Read", fontWeight = FontWeight.Bold)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            if (user != null) {
                                                                scope.launch {
                                                                    db.userDao().deletePurchase(user.uid, currentBook.id)
                                                                    snackbarHostState.showSnackbar("Removed from library")
                                                                }
                                                            }
                                                        },
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
                                                    Spacer(Modifier.height(12.dp)); Text("Sign In Required", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text("Sign in to add this item to your library.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { 
                                                        Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("Sign in / Register") 
                                                    }
                                                }
                                            }
                                        } else {
                                            if (currentBook.price == 0.0) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            db.userDao().addPurchase(PurchaseItem(user.uid, currentBook.id))
                                                            snackbarHostState.showSnackbar("Added to your library!")
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                                    shape = RoundedCornerShape(16.dp)
                                                ) {
                                                    Icon(Icons.Default.LibraryAdd, null)
                                                    Spacer(Modifier.width(12.dp))
                                                    Text("Add to Library", fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                val discountedPrice = currentBook.price * 0.9
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "£${String.format(Locale.US, "%.2f", currentBook.price)}", style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                        Spacer(Modifier.width(12.dp)); Text(text = "£${String.format(Locale.US, "%.2f", discountedPrice)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text("STUDENT PRICE (-10%)", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                                                    Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showOrderFlow = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Order now!", fontWeight = FontWeight.Bold) }
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
                                productId = bookId,
                                reviews = allReviews,
                                localUser = localUser,
                                isLoggedIn = user != null,
                                db = db,
                                isDarkTheme = isDarkTheme,
                                onReviewPosted = { scope.launch { snackbarHostState.showSnackbar("Thanks for your review!") } },
                                onLoginClick = onLoginRequired
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            }
        }

        if (showOrderFlow && book != null) {
            OrderFlowDialog(
                book = book!!,
                user = localUser,
                onDismiss = { showOrderFlow = false },
                onEditProfile = { showOrderFlow = false; onNavigateToProfile() },
                onComplete = { 
                    showOrderFlow = false
                    scope.launch { snackbarHostState.showSnackbar("Purchase successful! Item added to your library.") }
                }
            )
        }
    }
}
