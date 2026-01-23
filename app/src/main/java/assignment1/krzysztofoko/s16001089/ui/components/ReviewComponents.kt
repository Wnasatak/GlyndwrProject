package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.ReviewLocal
import assignment1.krzysztofoko.s16001089.data.UserLocal
import kotlinx.coroutines.launch

@Composable
fun ReviewSection(
    productId: String,
    reviews: List<ReviewLocal>,
    localUser: UserLocal?,
    isLoggedIn: Boolean,
    db: AppDatabase,
    isDarkTheme: Boolean,
    onReviewPosted: () -> Unit = {},
    onReviewDeleted: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val threadedReviews = remember(reviews) {
        val parents = reviews.filter { it.parentReviewId == null }
        parents.map { parent ->
            parent to reviews.filter { it.parentReviewId == parent.reviewId }
        }
    }

    var newComment by remember { mutableStateOf("") }
    var userRating by remember { mutableIntStateOf(5) }

    Column {
        Text(
            text = "User Reviews", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold, 
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        if (isLoggedIn && localUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                        repeat(5) { index ->
                            IconButton(onClick = { userRating = index + 1 }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (index < userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (index < userRating) Color(0xFFFFB300) else Color.Gray
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        placeholder = { Text("What did you think about this?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Button(
                        onClick = {
                            if (newComment.isBlank()) return@Button
                            scope.launch {
                                val review = ReviewLocal(
                                    productId = productId,
                                    userId = localUser.id,
                                    userName = localUser.name,
                                    userPhotoUrl = localUser.photoUrl,
                                    comment = newComment,
                                    rating = userRating
                                )
                                db.userDao().addReview(review)
                                newComment = ""
                                userRating = 5
                                onReviewPosted()
                            }
                        },
                        modifier = Modifier.align(Alignment.End).padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Post Review")
                    }
                }
            }
        }

        if (reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RateReview, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Text("No reviews yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Be the first to share your experience!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        } else {
            threadedReviews.forEach { (parent, replies) ->
                val userHasReplied = localUser != null && replies.any { it.userId == localUser.id }
                
                Column {
                    ReviewItem(
                        review = parent,
                        db = db,
                        productId = productId,
                        currentLocalUser = localUser,
                        isLoggedIn = isLoggedIn,
                        canReply = !userHasReplied,
                        isDarkTheme = isDarkTheme
                    )
                    replies.forEach { reply ->
                        Row {
                            Spacer(modifier = Modifier.width(32.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                ReviewItem(
                                    review = reply,
                                    db = db,
                                    productId = productId,
                                    currentLocalUser = localUser,
                                    isLoggedIn = isLoggedIn,
                                    isReply = true,
                                    isDarkTheme = isDarkTheme
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
fun ReviewItem(
    review: ReviewLocal,
    db: AppDatabase,
    productId: String,
    currentLocalUser: UserLocal?,
    isLoggedIn: Boolean,
    isReply: Boolean = false,
    canReply: Boolean = false,
    isDarkTheme: Boolean
) {
    var isEditing by remember { mutableStateOf(false) }
    var isReplying by remember { mutableStateOf(false) }
    var editComment by remember { mutableStateOf(review.comment) }
    var replyComment by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showInteractionsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val interactions by db.userDao().getInteractionsForReview(review.reviewId).collectAsState(initial = emptyList())
    val userInteraction = interactions.find { it.userId == currentLocalUser?.id }

    val reviewerFlow = remember(review.userId) { db.userDao().getUserFlow(review.userId) }
    val reviewer by reviewerFlow.collectAsState(initial = null)
    val displayPhotoUrl = reviewer?.photoUrl ?: review.userPhotoUrl
    val isOwner = isLoggedIn && currentLocalUser != null && review.userId == currentLocalUser.id

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReply) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                UserAvatar(
                    photoUrl = displayPhotoUrl,
                    modifier = Modifier.size(if (isReply) 32.dp else 40.dp),
                    iconSize = if (isReply) 16 else 24
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = review.userName,
                                fontWeight = FontWeight.Bold,
                                style = if (isReply) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge
                            )
                            if (!isEditing && !isReply) {
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (index < review.rating) Color(0xFFFFB300) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (isOwner && !isEditing) {
                            Box {
                                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = { isEditing = true; showMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            scope.launch { db.userDao().deleteReview(review.reviewId) }
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                                    )
                                }
                            }
                        }
                    }

                    if (isEditing) {
                        OutlinedTextField(
                            value = editComment,
                            onValueChange = { editComment = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isEditing = false }) { Text("Cancel") }
                            Button(
                                onClick = {
                                    scope.launch {
                                        db.userDao().addReview(review.copy(comment = editComment))
                                        isEditing = false
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Save") }
                        }
                    } else {
                        Text(
                            text = review.comment,
                            style = if (isReply) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isOwner && (isLoggedIn || review.likes > 0)) {
                                    Surface(
                                        color = if (userInteraction?.interactionType == "LIKE") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickable(enabled = isLoggedIn) {
                                            if (currentLocalUser != null) {
                                                scope.launch { db.userDao().toggleInteraction(review.reviewId, currentLocalUser.id, currentLocalUser.name, "LIKE") }
                                            }
                                        }
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (userInteraction?.interactionType == "LIKE") Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                                contentDescription = "Like",
                                                modifier = Modifier.size(24.dp),
                                                tint = if (userInteraction?.interactionType == "LIKE") MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                            if (review.likes > 0) {
                                                Text(
                                                    text = " ${review.likes}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (userInteraction?.interactionType == "LIKE") MaterialTheme.colorScheme.primary else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!isOwner && (isLoggedIn || review.dislikes > 0)) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = if (userInteraction?.interactionType == "DISLIKE") MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickable(enabled = isLoggedIn) {
                                            if (currentLocalUser != null) {
                                                scope.launch { db.userDao().toggleInteraction(review.reviewId, currentLocalUser.id, currentLocalUser.name, "DISLIKE") }
                                            }
                                        }
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (userInteraction?.interactionType == "DISLIKE") Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                                                contentDescription = "Dislike",
                                                modifier = Modifier.size(24.dp),
                                                tint = if (userInteraction?.interactionType == "DISLIKE") MaterialTheme.colorScheme.error else Color.Gray
                                            )
                                            if (review.dislikes > 0) {
                                                Text(
                                                    text = " ${review.dislikes}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (userInteraction?.interactionType == "DISLIKE") MaterialTheme.colorScheme.error else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }

                                if (interactions.isNotEmpty()) {
                                    Spacer(Modifier.width(16.dp))
                                    Row(
                                        modifier = Modifier.clickable { showInteractionsDialog = true },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        interactions.take(3).forEachIndexed { index, inter ->
                                            val interUserFlow = remember(inter.userId) { db.userDao().getUserFlow(inter.userId) }
                                            val interUser by interUserFlow.collectAsState(initial = null)
                                            Box(modifier = Modifier.offset(x = (index * -12).dp), contentAlignment = Alignment.BottomEnd) {
                                                UserAvatar(
                                                    photoUrl = interUser?.photoUrl,
                                                    modifier = Modifier.size(30.dp).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                                    iconSize = 18
                                                )
                                                // Added Indicator Badge
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(14.dp).offset(x = 4.dp, y = 4.dp),
                                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                                ) {
                                                    Icon(
                                                        imageVector = if (inter.interactionType == "LIKE") Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(2.dp),
                                                        tint = if (inter.interactionType == "LIKE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                        if (interactions.size > 3) {
                                            Text(
                                                text = "+${interactions.size - 3}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.Gray,
                                                modifier = Modifier.offset(x = (-24).dp).padding(start = 32.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            if (canReply && isLoggedIn && currentLocalUser != null && !isOwner) {
                                TextButton(
                                    onClick = { isReplying = !isReplying },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Reply, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reply", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (isReplying) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = replyComment,
                                onValueChange = { replyComment = it },
                                placeholder = { Text("Write a reply to ${review.userName}...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { isReplying = false; replyComment = "" }) { Text("Cancel", fontSize = 12.sp) }
                                Button(
                                    onClick = {
                                        if (replyComment.isBlank() || currentLocalUser == null) return@Button
                                        scope.launch {
                                            val reply = ReviewLocal(
                                                productId = productId,
                                                userId = currentLocalUser.id,
                                                userName = currentLocalUser.name,
                                                userPhotoUrl = currentLocalUser.photoUrl,
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

    if (showInteractionsDialog) {
        Dialog(onDismissRequest = { showInteractionsDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Reactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    LazyColumn {
                        items(interactions) { interaction ->
                            val interUserFlow = remember(interaction.userId) { db.userDao().getUserFlow(interaction.userId) }
                            val interUser by interUserFlow.collectAsState(initial = null)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(photoUrl = interUser?.photoUrl, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(interaction.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(if (interaction.interactionType == "LIKE") "Liked this review" else "Disliked this review", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Icon(
                                    imageVector = if (interaction.interactionType == "LIKE") Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (interaction.interactionType == "LIKE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    TextButton(
                        onClick = { showInteractionsDialog = false },
                        modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
