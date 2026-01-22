package assignment1.krzysztofoko.s16001089.ui.details

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.OrderFlowDialog
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.auth.FirebaseAuth
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
    onPlayAudio: (Book) -> Unit,
    onReadBook: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onViewInvoice: (String) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var book by mutableStateOf(initialBook)
    var loading by mutableStateOf(true)

    // FETCH DATA LOCALLY USING FIXED ID (Consistent with whole project)
    val localUser by db.userDao().getUserFlow(LOCAL_USER_ID).collectAsState(initial = null)

    val wishlistIds by remember(user) {
        user?.let { db.userDao().getWishlistIds(LOCAL_USER_ID) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val purchaseIds by remember(user) {
        user?.let { db.userDao().getPurchaseIds(LOCAL_USER_ID) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val inWishlist = remember(wishlistIds) { wishlistIds.contains(bookId) }
    val isOwned = remember(purchaseIds) { purchaseIds.contains(bookId) }

    val allReviews by db.userDao().getReviewsForProduct(bookId).collectAsState(initial = emptyList())

    val threadedReviews = remember(allReviews) {
        val parents = allReviews.filter { it.parentReviewId == null }
        parents.map { parent ->
            parent to allReviews.filter { it.parentReviewId == parent.reviewId }
        }
    }

    var newComment by remember { mutableStateOf("") }
    var userRating by remember { mutableIntStateOf(5) }
    var showOrderFlow by remember { mutableStateOf(false) }

    LaunchedEffect(bookId, user) {
        loading = true
        if (book == null) {
            book = db.bookDao().getBookById(bookId)
        }
        if (user != null) {
            db.userDao().addToHistory(HistoryItem(LOCAL_USER_ID, bookId))
        }
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
                    title = { 
                        Text(text = book?.title ?: "Item Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) 
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        if (user != null) {
                            IconButton(onClick = {
                                scope.launch {
                                    if (inWishlist) {
                                        db.userDao().removeFromWishlist(LOCAL_USER_ID, bookId)
                                    } else {
                                        db.userDao().addToWishlist(WishlistItem(LOCAL_USER_ID, bookId))
                                    }
                                }
                            }) {
                                Icon(imageVector = if (inWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Wishlist")
                            }
                        }
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { padding ->
            if (loading && book == null) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (book == null) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text("Item details not available.")
                        TextButton(onClick = onBack) { Text("Go Back") }
                    }
                }
            } else {
                book?.let { currentBook ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(280.dp), 
                                shape = RoundedCornerShape(24.dp), 
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), 
                                shadowElevation = 8.dp, 
                                border = BorderStroke(
                                    1.dp, 
                                    if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) 
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), MaterialTheme.colorScheme.surface)))) {
                                    Icon(imageVector = if (currentBook.isAudioBook) Icons.Default.Headphones else if (currentBook.mainCategory == "University Courses") Icons.Default.School else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(120.dp).align(Alignment.Center), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                    
                                    if (isOwned) {
                                        // Paid Badge - Top Left (Only if price > 0)
                                        if (currentBook.price > 0) {
                                            Surface(
                                                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                            ) {
                                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Paid,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "Paid",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        // Status Badge - Top Right (Purchased / In Library)
                                        Surface(
                                            modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LibraryAddCheck, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = if (currentBook.price > 0) "PURCHASED" else "IN LIBRARY",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
                                    Spacer(modifier = Modifier.height(16.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AssistChip(onClick = {}, label = { Text(currentBook.category) }); AssistChip(onClick = {}, label = { Text(if (currentBook.isAudioBook) "Audio Content" else "Academic Material") }) }
                                    Spacer(modifier = Modifier.height(24.dp)); Text(text = "About this item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp)); Text(text = currentBook.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                if (currentBook.price > 0) {
                                                    Button(onClick = { onViewInvoice(currentBook.id) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)) {
                                                        Icon(Icons.Default.ReceiptLong, null); Spacer(Modifier.width(12.dp)); Text("View & Print Invoice", fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Button(onClick = { if (currentBook.isAudioBook) onPlayAudio(currentBook) else onReadBook(currentBook.id) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                                                        Icon(if (currentBook.isAudioBook) Icons.Default.PlayCircleFilled else Icons.Default.AutoStories, null)
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(if (currentBook.isAudioBook) "Listen" else "Read", fontWeight = FontWeight.Bold)
                                                    }
                                                    if (currentBook.price == 0.0 || currentBook.isAudioBook) {
                                                        OutlinedButton(
                                                            onClick = {
                                                                if (user != null) {
                                                                    scope.launch {
                                                                        db.userDao().deletePurchase(LOCAL_USER_ID, currentBook.id)
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
                                            }
                                        } else if (user == null) {
                                            val isFree = currentBook.price == 0.0
                                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(if (isFree) Icons.Default.LibraryAdd else Icons.Default.LockPerson, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                    Spacer(Modifier.height(12.dp)); Text("Sign In Required", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text(if (isFree) "Sign in to add this free item to your library." else "Sign in to order items and access course installments.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth()) { Text(if (isFree) "Add to library" else "Sign In to Purchase") }
                                                }
                                            }
                                        } else {
                                            if (currentBook.price == 0.0) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            db.userDao().addPurchase(PurchaseItem(LOCAL_USER_ID, currentBook.id))
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

                        item { Spacer(modifier = Modifier.height(32.dp)); Text("User Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp)) }

                        if (user != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp), 
                                    shape = RoundedCornerShape(24.dp), 
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)), 
                                    border = BorderStroke(
                                        1.dp, 
                                        if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) 
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text("Share your experience", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) { repeat(5) { index -> IconButton(onClick = { userRating = index + 1 }, modifier = Modifier.size(36.dp)) { Icon(imageVector = if (index < userRating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = if (index < userRating) Color(0xFFFFB300) else Color.Gray) } } }
                                        OutlinedTextField(value = newComment, onValueChange = { newComment = it }, placeholder = { Text("What did you think about this?") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                                        Button(onClick = {
                                            if (newComment.isBlank()) return@Button
                                            scope.launch {
                                                val review = ReviewLocal(productId = bookId, userId = LOCAL_USER_ID, userName = user.displayName ?: "Student", userPhotoUrl = user.photoUrl?.toString(), comment = newComment, rating = userRating)
                                                db.userDao().addReview(review)
                                                newComment = ""; userRating = 5
                                                snackbarHostState.showSnackbar("Thanks for your review!")
                                            }
                                        }, modifier = Modifier.align(Alignment.End).padding(top = 16.dp), shape = RoundedCornerShape(16.dp)) { Text("Post Review") }
                                    }
                                }
                            }
                        }

                        if (allReviews.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 24.dp), 
                                    shape = RoundedCornerShape(24.dp), 
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)), 
                                    border = BorderStroke(
                                        1.dp, 
                                        if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) 
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.RateReview, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                        Spacer(Modifier.height(16.dp)); Text("No reviews yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Be the first to share your experience!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            items(threadedReviews) { (parent, replies) ->
                                Column {
                                    ReviewItem(parent, db, bookId, LOCAL_USER_ID, canReply = true, isDarkTheme = isDarkTheme)
                                    replies.forEach { reply ->
                                        Row {
                                            Spacer(modifier = Modifier.width(32.dp))
                                            Box(modifier = Modifier.weight(1f)) {
                                                ReviewItem(reply, db, bookId, LOCAL_USER_ID, isReply = true, isDarkTheme = isDarkTheme)
                                            }
                                        }
                                    }
                                }
                            }
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

@Composable
fun ReviewItem(review: ReviewLocal, db: AppDatabase, bookId: String, currentUserId: String?, isReply: Boolean = false, canReply: Boolean = false, isDarkTheme: Boolean) {
    var isEditing by remember { mutableStateOf(false) }
    var isReplying by remember { mutableStateOf(false) }
    var editComment by remember { mutableStateOf(review.comment) }
    var replyComment by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isReply) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(
            1.dp, 
            if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(modifier = Modifier.size(if (isReply) 32.dp else 40.dp), shape = CircleShape, color = Color.Transparent) {
                    SubcomposeAsyncImage(model = review.userPhotoUrl ?: "file:///android_asset/images/users/avatars/Avatar_defult.png", contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop, error = { Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(40.dp)) })
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(review.userName, fontWeight = FontWeight.Bold, style = if (isReply) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge)
                            if (!isEditing && !isReply) { Row { repeat(5) { index -> Icon(imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (index < review.rating) Color(0xFFFFB300) else Color.Gray) } } }
                        }
                        if (currentUserId == review.userId && !isEditing) {
                            Box {
                                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.MoreVert, null, tint = Color.Gray) }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Edit") }, onClick = { isEditing = true; showMenu = false }, leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) })
                                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { scope.launch { db.userDao().deleteReview(review.reviewId) }; showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) })
                                }
                            }
                        }
                    }
                    if (isEditing) {
                        OutlinedTextField(value = editComment, onValueChange = { editComment = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isEditing = false }) { Text("Cancel") }
                            Button(onClick = {
                                scope.launch {
                                    db.userDao().addReview(review.copy(comment = editComment))
                                    isEditing = false
                                }
                            }, shape = RoundedCornerShape(12.dp)) { Text("Save") }
                        }
                    } else {
                        Text(review.comment, style = if (isReply) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
                        if (canReply && currentUserId != null && currentUserId != review.userId) {
                            TextButton(onClick = { isReplying = !isReplying }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Reply, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reply", fontSize = 12.sp)
                            }
                        }
                    }
                    if (isReplying) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = replyComment,
                                onValueChange = { replyComment = it },
                                placeholder = { Text("Write a reply...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { isReplying = false; replyComment = "" }) { Text("Cancel", fontSize = 12.sp) }
                                Button(
                                    onClick = {
                                        if (replyComment.isBlank()) return@Button
                                        scope.launch {
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            val reply = ReviewLocal(
                                                productId = bookId,
                                                userId = LOCAL_USER_ID,
                                                userName = currentUser?.displayName ?: "Student",
                                                userPhotoUrl = currentUser?.photoUrl?.toString(),
                                                comment = replyComment,
                                                rating = 0,
                                                parentReviewId = review.reviewId
                                            )
                                            db.userDao().addReview(reply)
                                            isReplying = false
                                            replyComment = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Reply", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
