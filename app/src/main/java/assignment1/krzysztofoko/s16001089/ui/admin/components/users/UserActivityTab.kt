package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.Invoice
import assignment1.krzysztofoko.s16001089.data.ReviewLocal
import assignment1.krzysztofoko.s16001089.data.WishlistItem
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth
import java.text.SimpleDateFormat
import java.util.*

/**
 * UserActivityTab.kt
 *
 * This administrative view provides a deep dive into a user's interactions with the university ecosystem.
 * It tracks browsing history, financial acquisitions, wishlist preferences, social feedback (reviews),
 * and search patterns. 
 *
 * The component also includes moderation tools for institutional staff to manage user-generated 
 * content while maintaining an audit trail.
 */

/**
 * Main activity overview tab for administrators.
 *
 * @param browseHistory List of products the user has recently viewed.
 * @param wishlist List of items the user has saved for later consideration.
 * @param searchHistory Chronological list of search queries performed by the user.
 * @param purchasedBooks Catalog of all books and courses acquired by the user.
 * @param commentedBooks Catalog items that the user has reviewed or commented on.
 * @param allReviews List of review objects containing the actual feedback text.
 * @param allInvoices Financial records associated with the user's purchases.
 * @param onDeleteComment Callback to remove a comment from the system.
 * @param onUpdateReview Callback to persist administrative edits to a review.
 * @param onNavigateToBook Navigation callback to view a specific item's detail page.
 */
@Composable
fun UserActivityTab(
    browseHistory: List<Book>, 
    wishlist: List<Pair<WishlistItem, Book>>, 
    searchHistory: List<String>,
    purchasedBooks: List<Book>,
    commentedBooks: List<Book>,
    allReviews: List<ReviewLocal>,
    allInvoices: List<Invoice>,
    onDeleteComment: (Int) -> Unit = {},
    onUpdateReview: (ReviewLocal) -> Unit = {},
    onNavigateToBook: (String) -> Unit = {}
) {
    // --- DIALOG & POPUP STATE MANAGEMENT ---
    // These states control which "Drill-down" detail view is currently visible.
    var selectedReviewForPopup by remember { mutableStateOf<ReviewLocal?>(null) }
    var selectedPurchaseForPopup by remember { mutableStateOf<Pair<Book, Invoice?>?>(null) }
    var selectedWishForPopup by remember { mutableStateOf<Pair<WishlistItem, Book>?>(null) }
    var selectedRecentForPopup by remember { mutableStateOf<Book?>(null) }
    
    // --- MODERATION UI STATE ---
    // isEditingReview: Toggles between read-only review display and administrative edit mode.
    var isEditingReview by remember { mutableStateOf(false) }
    var editedComment by remember { mutableStateOf("") }
    // showAdminWarning: Safeguard before committing changes to institutional feedback.
    var showAdminWarning by remember { mutableStateOf(false) }
    // showDeleteConfirm: Modal confirmation for permanent content removal.
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp), 
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- SECTION 1: BROWSING BEHAVIOUR ---
        item { 
            ActivitySectionDetails(
                title = "Recently Viewed", 
                items = browseHistory, 
                onItemClick = { selectedRecentForPopup = it }
            ) 
        }
        
        // --- SECTION 2: ACQUISITION HISTORY ---
        item { 
            ActivitySectionDetails(
                title = "Purchased Items", 
                items = purchasedBooks, 
                onItemClick = { book ->
                    // Resolve the matching invoice for the selected book to provide financial detail.
                    val invoice = allInvoices.find { it.productId == book.id }
                    selectedPurchaseForPopup = book to invoice
                }
            ) 
        }
        
        // --- SECTION 3: ACADEMIC INTERESTS ---
        item { 
            ActivitySectionDetails(
                title = "Wishlist", 
                items = wishlist.map { it.second }, 
                onItemClick = { book ->
                    selectedWishForPopup = wishlist.find { it.second.id == book.id }
                }
            ) 
        }
        
        // --- SECTION 4: SOCIAL ENGAGEMENT & FEEDBACK ---
        item { 
            ActivitySectionDetails(
                title = "Reviewed Items", 
                items = commentedBooks, 
                onItemClick = { book ->
                    // Resolve the specific review object for moderation tasks.
                    val review = allReviews.find { it.productId == book.id }
                    if (review != null) {
                        selectedReviewForPopup = review
                        editedComment = review.comment
                        isEditingReview = false
                    }
                }
            ) 
        }
        
        // --- SECTION 5: AUDIT LOG (SEARCH PATTERNS) ---
        item {
            Column {
                Text(
                    text = "RECENT SEARCHES", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                if (searchHistory.isEmpty()) {
                    Text("No searches recorded.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                } else {
                    // Display search queries as interactive chips (limited to top 10 for performance).
                    FlowRowDetails(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(8.dp), 
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        searchHistory.take(10).forEach { query -> 
                            SuggestionChip(
                                onClick = {}, 
                                label = { Text(query, style = MaterialTheme.typography.labelSmall) }, 
                                shape = RoundedCornerShape(8.dp)
                            ) 
                        }
                    }
                }
            }
        }
    }

    // --- RECENTLY VIEWED DIALOG ---
    // Displays a quick summary of a browsed item with navigation to its full page.
    if (selectedRecentForPopup != null) {
        val book = selectedRecentForPopup!!
        Dialog(onDismissRequest = { selectedRecentForPopup = null }) {
            Surface(
                modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard), 
                shape = RoundedCornerShape(28.dp), 
                color = MaterialTheme.colorScheme.surface, 
                tonalElevation = 6.dp, 
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) { 
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) } 
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text("Browsing History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(book.title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Text("By ${book.author}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text(book.description, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { selectedRecentForPopup = null }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Close") }
                        Button(onClick = { selectedRecentForPopup = null; onNavigateToBook(book.id) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("View Item") }
                    }
                }
            }
        }
    }

    // --- PURCHASE DETAILS DIALOG ---
    // Displays transaction data for acquired items, distinguishing between paid and free resources.
    if (selectedPurchaseForPopup != null) {
        val (book, invoice) = selectedPurchaseForPopup!!
        val isFree = book.price <= 0.0
        Dialog(onDismissRequest = { selectedPurchaseForPopup = null }) {
            Surface(
                modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard), 
                shape = RoundedCornerShape(28.dp), 
                color = MaterialTheme.colorScheme.surface, 
                tonalElevation = 6.dp, 
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) { 
                            Box(contentAlignment = Alignment.Center) { Icon(if(isFree) Icons.Default.Inventory else Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) } 
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text(if (isFree) "Pickup Record" else "Purchase Record", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(book.title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Text("Category: ${book.mainCategory}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    // Conditional metadata display based on payment status.
                    if (!isFree && invoice != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DetailRow("Invoice", invoice.invoiceNumber)
                            DetailRow("Paid", "£${String.format(Locale.US, "%.2f", invoice.pricePaid)}")
                            DetailRow("Method", invoice.paymentMethod)
                        }
                    } else if (isFree) {
                        Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("FREE ACADEMIC RESOURCE", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 11.sp) }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { selectedPurchaseForPopup = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Close") }
                }
            }
        }
    }

    // --- WISHLIST DIALOG ---
    // Displays details for saved items, including the timestamp of when they were added.
    if (selectedWishForPopup != null) {
        val (wish, book) = selectedWishForPopup!!
        Dialog(onDismissRequest = { selectedWishForPopup = null }) {
            Surface(
                modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard), 
                shape = RoundedCornerShape(28.dp), 
                color = MaterialTheme.colorScheme.surface, 
                tonalElevation = 6.dp, 
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) } }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text("Wishlist Item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(book.title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Text("By ${book.author}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    DetailRow("Added On", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(wish.addedAt)))
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { selectedWishForPopup = null }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Close") }
                        Button(onClick = { selectedWishForPopup = null; onNavigateToBook(book.id) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("View Item") }
                    }
                }
            }
        }
    }

    // --- MANAGE REVIEW DIALOG (MODERATION HUB) ---
    // Central dialog for viewing, editing, or deleting student-authored reviews.
    if (selectedReviewForPopup != null) {
        val review = selectedReviewForPopup!!
        Dialog(onDismissRequest = { if(!isEditingReview) selectedReviewForPopup = null }) {
            Surface(
                modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard), 
                shape = RoundedCornerShape(28.dp), 
                color = MaterialTheme.colorScheme.surface, 
                tonalElevation = 6.dp, 
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) { 
                            Box(contentAlignment = Alignment.Center) { Icon(if(isEditingReview) Icons.Default.EditNote else Icons.Default.RateReview, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) } 
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text(if(isEditingReview) "Edit Comment" else "Review Detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(24.dp))
                    // ADMINISTRATIVE OVERRIDE: Admin can modify feedback text.
                    if (isEditingReview) {
                        OutlinedTextField(value = editedComment, onValueChange = { editedComment = it }, label = { Text("Modified Feedback") }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), shape = RoundedCornerShape(16.dp))
                    } else {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) { 
                            Text(text = review.comment, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp) 
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (isEditingReview) {
                            TextButton(onClick = { isEditingReview = false }) { Text("Cancel") }
                            Button(onClick = { showAdminWarning = true }, shape = RoundedCornerShape(12.dp)) { 
                                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Review Changes") 
                            }
                        } else {
                            // DESTRUCTIVE ACTION: Trigger delete confirmation.
                            FilledTonalIconButton(onClick = { showDeleteConfirm = true }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Default.DeleteOutline, null) }
                            Row { 
                                OutlinedButton(onClick = { selectedReviewForPopup = null }, shape = RoundedCornerShape(12.dp)) { Text("Close") }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { isEditingReview = true }, shape = RoundedCornerShape(12.dp)) { 
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit") 
                                } 
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MODERATION: PERMANENT DELETE CONFIRMATION ---
    if (showDeleteConfirm && selectedReviewForPopup != null) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Surface(
                modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    @Suppress("DEPRECATION")
                    Text("Remove Comment?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        "Are you sure you want to permanently delete this student feedback? This operation cannot be reversed and will be logged in the system audit trail.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showDeleteConfirm = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Keep Comment") }
                        Button(
                            onClick = {
                                onDeleteComment(selectedReviewForPopup!!.reviewId)
                                selectedReviewForPopup = null
                                showDeleteConfirm = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete Now", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // --- MODERATION: ADMINISTRATIVE POLICY WARNING ---
    if (showAdminWarning) {
        Dialog(onDismissRequest = { showAdminWarning = false }) {
            Surface(
                modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard), 
                shape = RoundedCornerShape(28.dp), 
                color = MaterialTheme.colorScheme.surface, 
                tonalElevation = 12.dp, 
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Gavel, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    @Suppress("DEPRECATION")
                    Text("Administrative Override", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    @Suppress("DEPRECATION")
                    Text("You are modifying public institutional feedback. This action is tracked and must comply with university conduct policies.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                    Spacer(Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { showAdminWarning = false }, modifier = Modifier.weight(1f)) { Text("Review Again") }
                        Button(onClick = { onUpdateReview(selectedReviewForPopup!!.copy(comment = editedComment)); selectedReviewForPopup = null; showAdminWarning = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Confirm Edit") }
                    }
                }
            }
        }
    }
}

/**
 * Reusable layout row for displaying key-value data points in activity dialogs.
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        @Suppress("DEPRECATION")
        Text(text = "$label:", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
        @Suppress("DEPRECATION")
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
    }
}
