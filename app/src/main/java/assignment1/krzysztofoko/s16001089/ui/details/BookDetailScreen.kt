package assignment1.krzysztofoko.s16001089.ui.details

import androidx.compose.animation.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.SelectionOption
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Review(
    val reviewId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val comment: String = "",
    val rating: Int = 5,
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList()
)

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
    onReadBook: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var book by remember { mutableStateOf(initialBook) }
    var loading by remember { mutableStateOf(true) }
    var isOwned by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    
    var inWishlist by remember { mutableStateOf(false) }
    var reviews by remember { mutableStateOf<List<Review>>(listOf()) }
    var newComment by remember { mutableStateOf("") }
    var userRating by remember { mutableIntStateOf(5) }
    
    var showOrderFlow by remember { mutableStateOf(false) }
    var profilePaymentMethod by remember { mutableStateOf("University Account") }

    LaunchedEffect(bookId, user) {
        loading = true
        if (user != null) {
            // Track "Last Viewed"
            db.collection("users").document(user.uid).collection("history").document(bookId)
                .set(mapOf("viewedAt" to System.currentTimeMillis()), SetOptions.merge())

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val method = doc.getString("selectedPaymentMethod")
                    if (method != null) profilePaymentMethod = method
                }
            db.collection("users").document(user.uid).collection("wishlist").document(bookId).get()
                .addOnSuccessListener { inWishlist = it.exists() }
            
            // Check if item is already in user's library (purchases)
            db.collection("users").document(user.uid).collection("purchases").document(bookId).get()
                .addOnSuccessListener { isOwned = it.exists() }
        }

        db.collection("books").document(bookId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    book = document.toObject(Book::class.java)?.copy(id = document.id)
                    loading = false
                } else { loading = false }
            }
            .addOnFailureListener { loading = false }
    }

    DisposableEffect(bookId) {
        val listener = db.collection("books").document(bookId).collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    reviews = snapshot.documents.mapNotNull { it.toObject(Review::class.java)?.copy(reviewId = it.id) }
                }
            }
        onDispose { listener.remove() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(text = book?.title ?: "Item Details", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        if (user != null) {
                            IconButton(onClick = {
                                val wishlistRef = db.collection("users").document(user.uid).collection("wishlist").document(bookId)
                                if (inWishlist) {
                                    wishlistRef.delete().addOnSuccessListener { inWishlist = false; scope.launch { snackbarHostState.showSnackbar("Removed from favorites") } }
                                } else {
                                    wishlistRef.set(mapOf("addedAt" to System.currentTimeMillis())).addOnSuccessListener { inWishlist = true; scope.launch { snackbarHostState.showSnackbar("Added to your wishlist!") } }
                                }
                            }) {
                                Icon(
                                    imageVector = if (inWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                                    contentDescription = "Wishlist", 
                                    tint = if (inWishlist) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                                )
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
                        Spacer(Modifier.height(16.dp))
                        Text("Item details not available.", color = Color.Gray)
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
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), MaterialTheme.colorScheme.surface))), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = if (currentBook.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)), 
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(text = currentBook.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "by ${currentBook.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AssistChip(onClick = {}, label = { Text(currentBook.category) }, leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) })
                                        AssistChip(onClick = {}, label = { Text(if (currentBook.isAudioBook) "Audio Content" else "Reading Material") })
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(text = "About this item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = currentBook.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            ActionSectionOwned(book = currentBook, isDownloading = isDownloading, progress = downloadProgress, onPlay = onPlayAudio, onRead = { onReadBook(currentBook.id) }, onDownload = {
                                                isDownloading = true
                                                scope.launch { while (downloadProgress < 1f) { delay(300); downloadProgress += 0.1f }; isDownloading = false; snackbarHostState.showSnackbar("Available offline!") }
                                            })
                                        } else if (user == null) { 
                                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                    Spacer(Modifier.height(12.dp))
                                                    Text("Student Discount Available", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text("Sign in to unlock exclusive student pricing.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp))
                                                    Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth()) { Text("Sign In to Purchase") }
                                                }
                                            }
                                        } else if (currentBook.price == 0.0) {
                                            // Free Item - Add to Library button
                                            Button(
                                                onClick = {
                                                    val purchase = mapOf(
                                                        "bookId" to currentBook.id,
                                                        "timestamp" to System.currentTimeMillis(),
                                                        "customerName" to (user.displayName ?: "Student"),
                                                        "paymentMethod" to "Free Access",
                                                        "pricePaid" to 0.0,
                                                        "title" to currentBook.title,
                                                        "author" to currentBook.author,
                                                        "category" to currentBook.category,
                                                        "audioBook" to currentBook.isAudioBook,
                                                        "imageUrl" to currentBook.imageUrl
                                                    )
                                                    db.collection("users").document(user.uid).collection("purchases").document(currentBook.id).set(purchase)
                                                        .addOnSuccessListener {
                                                            isOwned = true
                                                            scope.launch { snackbarHostState.showSnackbar("Added to your Library!") }
                                                        }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Icon(Icons.Default.LibraryAdd, null)
                                                Spacer(Modifier.width(12.dp))
                                                Text("Add to Library", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            }
                                        } else { 
                                            val discountedPrice = currentBook.price * 0.9
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = "£${String.format(Locale.US, "%.2f", currentBook.price)}", style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                    Spacer(Modifier.width(12.dp))
                                                    Text(text = "£${String.format(Locale.US, "%.2f", discountedPrice)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text("STUDENT PRICE (-10%)", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                                                Spacer(modifier = Modifier.height(24.dp))
                                                Button(onClick = { showOrderFlow = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.ShoppingBag, null); Spacer(Modifier.width(12.dp)); Text("Complete Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text("User Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        }

                        if (user != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp), 
                                    shape = RoundedCornerShape(20.dp), 
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Share your experience", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                                            repeat(5) { index ->
                                                IconButton(onClick = { userRating = index + 1 }, modifier = Modifier.size(36.dp)) {
                                                    Icon(imageVector = if (index < userRating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = if (index < userRating) Color(0xFFFFB300) else Color.Gray)
                                                }
                                            }
                                        }
                                        OutlinedTextField(
                                            value = newComment, 
                                            onValueChange = { newComment = it }, 
                                            placeholder = { Text("What did you think about this?") }, 
                                            modifier = Modifier.fillMaxWidth(), 
                                            shape = RoundedCornerShape(12.dp), 
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            )
                                        )
                                        Button(onClick = {
                                            if (newComment.trim().isEmpty()) { scope.launch { snackbarHostState.showSnackbar("Please share a few words about your experience first.") }; return@Button }
                                            val review = Review(
                                                userId = user.uid,
                                                userName = user.displayName ?: "Student",
                                                userPhotoUrl = user.photoUrl?.toString(),
                                                comment = newComment,
                                                rating = userRating,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            // 1. Post review to Book sub-collection
                                            db.collection("books").document(bookId).collection("reviews").add(review)
                                                .addOnSuccessListener { 
                                                    // 2. ALSO save to User's private comment history for Dashboard (NO Index needed)
                                                    db.collection("users").document(user.uid).collection("comments").document(bookId)
                                                        .set(mapOf("commentedAt" to System.currentTimeMillis()), SetOptions.merge())
                                                    
                                                    newComment = ""; userRating = 5; scope.launch { snackbarHostState.showSnackbar("Thanks for your review!") } 
                                                }
                                        }, modifier = Modifier.align(Alignment.End).padding(top = 16.dp), shape = RoundedCornerShape(12.dp)) { Text("Post Review") }
                                    }
                                }
                            }
                        }

                        if (reviews.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No reviews yet. Be the first!", color = Color.Gray, style = MaterialTheme.typography.bodySmall) } }
                        } else {
                            items(reviews) { review -> ReviewItem(review, db, bookId, user?.uid) }
                        }
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            }
        }

        if (showOrderFlow && book != null) {
            OrderFlowDialog(book = book!!, userId = user?.uid ?: "", defaultPaymentMethod = profilePaymentMethod, onDismiss = { showOrderFlow = false }, onComplete = { isOwned = true; showOrderFlow = false; scope.launch { snackbarHostState.showSnackbar("Order completed successfully!") } })
        }
    }
}

@Composable
fun ReviewItem(review: Review, db: FirebaseFirestore, bookId: String, currentUserId: String?) {
    var currentPhotoUrl by remember { mutableStateOf(review.userPhotoUrl) }
    var isEditing by remember { mutableStateOf(false) }
    var editComment by remember { mutableStateOf(review.comment) }
    var editRating by remember { mutableIntStateOf(review.rating) }
    var showMenu by remember { mutableStateOf(false) }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(review.userId) {
        if (review.userId.isNotEmpty()) {
            db.collection("users").document(review.userId).get()
                .addOnSuccessListener { doc ->
                    val liveUrl = doc.getString("photoUrl")
                    if (liveUrl != null) currentPhotoUrl = liveUrl
                }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove Review") },
            text = { Text("Are you sure you want to permanently delete your review?") },
            confirmButton = {
                TextButton(onClick = { 
                    db.collection("books").document(bookId).collection("reviews").document(review.reviewId).delete()
                    db.collection("users").document(currentUserId!!).collection("comments").document(bookId).delete()
                    showDeleteConfirm = false 
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    if (showEditConfirm) {
        AlertDialog(
            onDismissRequest = { showEditConfirm = false },
            title = { Text("Update Review") },
            text = { Text("Do you want to save the changes to your review?") },
            confirmButton = {
                TextButton(onClick = { 
                    db.collection("books").document(bookId).collection("reviews").document(review.reviewId)
                        .update(mapOf("comment" to editComment, "rating" to editRating))
                        .addOnSuccessListener { isEditing = false; showEditConfirm = false }
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { showEditConfirm = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp), 
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                val defaultAvatarPath = "file:///android_asset/images/users/avatars/Avatar_defult.png"
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.Transparent) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(if (!currentPhotoUrl.isNullOrEmpty()) currentPhotoUrl else defaultAvatarPath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = { Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(40.dp)) }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(review.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            if (!isEditing) {
                                Row { repeat(5) { index -> Icon(imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (index < review.rating) Color(0xFFFFB300) else Color.Gray) } }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isLiked = currentUserId != null && review.likedBy.contains(currentUserId)
                            val isDisliked = currentUserId != null && review.dislikedBy.contains(currentUserId)
                            val isOwnComment = currentUserId == review.userId

                            // Hide reactions entirely for own comments and unlogged users
                            if (!isOwnComment && currentUserId != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val ref = db.collection("books").document(bookId).collection("reviews").document(review.reviewId)
                                            if (isLiked) {
                                                ref.update("likes", FieldValue.increment(-1), "likedBy", FieldValue.arrayRemove(currentUserId))
                                            } else {
                                                ref.update("likes", FieldValue.increment(1), "likedBy", FieldValue.arrayUnion(currentUserId))
                                                if (isDisliked) {
                                                    ref.update("dislikes", FieldValue.increment(-1), "dislikedBy", FieldValue.arrayRemove(currentUserId))
                                                }
                                            }
                                        }, 
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                            contentDescription = "Like",
                                            tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(review.likes.toString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    
                                    Spacer(Modifier.width(4.dp))
                                    
                                    IconButton(
                                        onClick = {
                                            val ref = db.collection("books").document(bookId).collection("reviews").document(review.reviewId)
                                            if (isDisliked) {
                                                ref.update("dislikes", FieldValue.increment(-1), "dislikedBy", FieldValue.arrayRemove(currentUserId))
                                            } else {
                                                ref.update("dislikes", FieldValue.increment(1), "dislikedBy", FieldValue.arrayUnion(currentUserId))
                                                if (isLiked) {
                                                    ref.update("likes", FieldValue.increment(-1), "likedBy", FieldValue.arrayRemove(currentUserId))
                                                }
                                            }
                                        }, 
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                                            contentDescription = "Dislike",
                                            tint = if (isDisliked) MaterialTheme.colorScheme.error else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(review.dislikes.toString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }

                            if (currentUserId == review.userId && !isEditing) {
                                Box {
                                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                                    }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Edit Review") },
                                            onClick = { isEditing = true; showMenu = false },
                                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Remove Review", color = MaterialTheme.colorScheme.error) },
                                            onClick = { showDeleteConfirm = true; showMenu = false },
                                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (isEditing) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            repeat(5) { index ->
                                IconButton(onClick = { editRating = index + 1 }, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = if (index < editRating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = if (index < editRating) Color(0xFFFFB300) else Color.Gray)
                                }
                            }
                        }
                        OutlinedTextField(value = editComment, onValueChange = { editComment = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isEditing = false; editComment = review.comment; editRating = review.rating }) { Text("Cancel") }
                            Button(onClick = { showEditConfirm = true }) { Text("Save") }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Text(review.comment, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(top = 4.dp))
                            if (review.timestamp > 0) {
                                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                Text(
                                    text = sdf.format(Date(review.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderFlowDialog(book: Book, userId: String, defaultPaymentMethod: String, onDismiss: () -> Unit, onComplete: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val db = FirebaseFirestore.getInstance()
    var fullName by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName ?: "") }
    var selectedMethod by remember { mutableStateOf(defaultPaymentMethod) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = when(step) { 1 -> "Order Review"; 2 -> "Billing Info"; 3 -> "Payment"; else -> "Success" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Step $step of 3", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                LinearProgressIndicator(progress = { step / 3f }, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(6.dp).clip(CircleShape))
                Box(modifier = Modifier.height(300.dp)) {
                    AnimatedContent(targetState = step, transitionSpec = { if (targetState > initialState) { slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut() } else { slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut() }.using(SizeTransform(clip = false)) }, label = "orderStepTransition") { currentStep ->
                        when(currentStep) {
                            1 -> {
                                val discountedPrice = book.price * 0.9
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary) }
                                            Spacer(Modifier.width(16.dp))
                                            Column { Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("by ${book.author}", style = MaterialTheme.typography.bodySmall) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    DetailRow("Original Price", "£${String.format(Locale.US, "%.2f", book.price)}")
                                    DetailRow("Student Discount", "-£${String.format(Locale.US, "%.2f", book.price * 0.1)}")
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    DetailRow("Total Amount", "£${String.format(Locale.US, "%.2f", discountedPrice)}", isTotal = true)
                                }
                            }
                            2 -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Confirmation Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Confirm the name for your certificate.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedTextField(value = FirebaseAuth.getInstance().currentUser?.email ?: "", onValueChange = {}, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), enabled = false, leadingIcon = { Icon(Icons.Default.Email, null) }, shape = RoundedCornerShape(12.dp))
                                }
                            }
                            3 -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Your preferred method is pre-selected.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SelectionOption("Credit or Debit Card", Icons.Default.AddCard, selectedMethod.contains("Card")) { selectedMethod = "Credit or Debit Card" }
                                    SelectionOption("Google Pay", Icons.Default.AccountBalanceWallet, selectedMethod == "Google Pay") { selectedMethod = "Google Pay" }
                                    SelectionOption("PayPal", Icons.Default.Payment, selectedMethod == "PayPal") { selectedMethod = "PayPal" }
                                    SelectionOption("University Account", Icons.Default.School, selectedMethod == "University Account") { selectedMethod = "University Account" }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 1) { OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Back") } } else { TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") } }
                    Button(onClick = { if (step < 3) { step++ } else {
                        val purchase = mapOf(
                            "bookId" to book.id, 
                            "timestamp" to System.currentTimeMillis(), 
                            "customerName" to fullName, 
                            "paymentMethod" to selectedMethod, 
                            "pricePaid" to (book.price * 0.9), 
                            "title" to book.title, 
                            "author" to book.author, 
                            "category" to book.category, 
                            "audioBook" to book.isAudioBook, 
                            "imageUrl" to book.imageUrl
                        )
                        db.collection("users").document(userId).collection("purchases").document(book.id).set(purchase).addOnSuccessListener { onComplete() }
                    } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 3) "Pay Now" else "Continue") }
                }
            }
        }
    }
}

@Composable
fun ActionSectionOwned(book: Book, isDownloading: Boolean, progress: Float, onPlay: (Book) -> Unit, onRead: () -> Unit, onDownload: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (progress < 1f && !isDownloading) { OutlinedButton(onClick = onDownload, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CloudDownload, null); Spacer(Modifier.width(8.dp)); Text("Save for Offline Use") } } else if (isDownloading) { LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) }
        Button(onClick = { if (book.isAudioBook) onPlay(book) else onRead() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(if (book.isAudioBook) Icons.Default.PlayCircleFilled else Icons.Default.AutoStories, null); Spacer(Modifier.width(8.dp)); Text(if (book.isAudioBook) "Listen to Audio" else "Read Online") }
    }
}

@Composable
fun DetailRow(label: String, value: String, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, color = if (isTotal) MaterialTheme.colorScheme.primary else Color.Unspecified)
    }
}
