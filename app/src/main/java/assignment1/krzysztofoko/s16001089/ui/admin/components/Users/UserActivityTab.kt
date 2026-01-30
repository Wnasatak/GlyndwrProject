package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.Invoice
import assignment1.krzysztofoko.s16001089.data.ReviewLocal
import assignment1.krzysztofoko.s16001089.data.WishlistItem
import assignment1.krzysztofoko.s16001089.ui.admin.AdminUserDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserActivityTab(
    browseHistory: List<Book>, 
    wishlist: List<Pair<WishlistItem, Book>>, 
    searchHistory: List<String>,
    purchasedBooks: List<Book>,
    commentedBooks: List<Book>,
    viewModel: AdminUserDetailsViewModel,
    onNavigateToBook: (String) -> Unit = {}
) {
    val allReviews by viewModel.allReviews.collectAsState()
    val allInvoices by viewModel.invoices.collectAsState()
    
    var selectedReviewForPopup by remember { mutableStateOf<ReviewLocal?>(null) }
    var selectedPurchaseForPopup by remember { mutableStateOf<Pair<Book, Invoice?>?>(null) }
    var selectedWishForPopup by remember { mutableStateOf<Pair<WishlistItem, Book>?>(null) }
    var selectedRecentForPopup by remember { mutableStateOf<Book?>(null) }
    
    var isEditingReview by remember { mutableStateOf(false) }
    var editedComment by remember { mutableStateOf("") }
    var showAdminWarning by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { 
            ActivitySectionDetails(
                title = "Recently Viewed", 
                items = browseHistory,
                onItemClick = { book ->
                    selectedRecentForPopup = book
                }
            ) 
        }
        
        item { 
            ActivitySectionDetails(
                title = "Purchased Items", 
                items = purchasedBooks,
                onItemClick = { book ->
                    val invoice = allInvoices.find { it.productId == book.id }
                    selectedPurchaseForPopup = book to invoice
                }
            ) 
        }
        
        item { 
            ActivitySectionDetails(
                title = "Wishlist", 
                items = wishlist.map { it.second },
                onItemClick = { book ->
                    selectedWishForPopup = wishlist.find { it.second.id == book.id }
                }
            ) 
        }
        
        item { 
            ActivitySectionDetails(
                title = "Reviewed Items", 
                items = commentedBooks,
                onItemClick = { book ->
                    val review = allReviews.find { it.productId == book.id }
                    if (review != null) {
                        selectedReviewForPopup = review
                        editedComment = review.comment
                        isEditingReview = false
                    }
                }
            ) 
        }
        
        item {
            Column {
                Text("RECENT SEARCHES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                if (searchHistory.isEmpty()) {
                    Text("No searches.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                } else {
                    FlowRowDetails(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

    // Recently Viewed Detail Popup
    if (selectedRecentForPopup != null) {
        val book = selectedRecentForPopup!!
        AlertDialog(
            onDismissRequest = { selectedRecentForPopup = null },
            title = { Text("Browsing Activity", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(book.title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Text("By ${book.author}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text(book.description, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Text("The student recently viewed this item in the store.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { 
                    selectedRecentForPopup = null
                    onNavigateToBook(book.id)
                }) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRecentForPopup = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Purchase Detail Popup
    if (selectedPurchaseForPopup != null) {
        val (book, invoice) = selectedPurchaseForPopup!!
        val isFree = book.price <= 0.0
        
        AlertDialog(
            onDismissRequest = { selectedPurchaseForPopup = null },
            title = { Text(if (isFree) "Item Pickup Details" else "Purchase Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(book.title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Text("By ${book.author}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text("Category: ${book.mainCategory}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    if (isFree) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f), 
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("FREE PICKUP", fontWeight = FontWeight.Black, color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Text("This item was added to the student's library as a free university resource.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else if (invoice != null) {
                        Column {
                            Text("TRANSACTION RECORD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            DetailRow("Invoice No", invoice.invoiceNumber)
                            DetailRow("Order Ref", invoice.orderReference ?: "N/A")
                            DetailRow("Paid", "£" + String.format(Locale.US, "%.2f", invoice.pricePaid))
                            DetailRow("Method", invoice.paymentMethod)
                            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(invoice.purchasedAt))
                            DetailRow("Date", dateStr)
                        }
                    } else {
                        Text("Purchase record found but invoice details are missing.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    selectedPurchaseForPopup = null
                    onNavigateToBook(book.id)
                }) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPurchaseForPopup = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Wishlist Detail Popup
    if (selectedWishForPopup != null) {
        val (wish, book) = selectedWishForPopup!!
        AlertDialog(
            onDismissRequest = { selectedWishForPopup = null },
            title = { Text("Wishlist Item", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(book.title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                            Text("By ${book.author}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text(book.description, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Column {
                        Text("SAVED ON", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(4.dp))
                        val saveDateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(wish.addedAt))
                        Text(saveDateStr, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    selectedWishForPopup = null
                    onNavigateToBook(book.id)
                }) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWishForPopup = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Comment Management Popup
    if (selectedReviewForPopup != null) {
        AlertDialog(
            onDismissRequest = { selectedReviewForPopup = null },
            title = { Text("Manage Review", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Product: ${selectedReviewForPopup!!.productId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    if (isEditingReview) {
                        OutlinedTextField(
                            value = editedComment,
                            onValueChange = { editedComment = it },
                            label = { Text("Edit Comment") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = selectedReviewForPopup!!.comment,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (isEditingReview) {
                    Button(onClick = {
                        showAdminWarning = true
                    }) {
                        Text("Save")
                    }
                } else {
                    IconButton(onClick = { isEditingReview = true }) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isEditingReview) {
                        IconButton(onClick = {
                            viewModel.deleteComment(selectedReviewForPopup!!.reviewId)
                            selectedReviewForPopup = null
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { selectedReviewForPopup = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Admin Warning Popup for Editing Comments
    if (showAdminWarning) {
        AlertDialog(
            onDismissRequest = { showAdminWarning = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(32.dp)) },
            title = { Text("Policy Warning", fontWeight = FontWeight.Black) },
            text = { 
                Text(
                    "You are modifying a comment submitted by another user. Editing student feedback may impact data integrity. Are you sure this change is necessary?",
                    textAlign = TextAlign.Center
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateReview(selectedReviewForPopup!!.copy(comment = editedComment))
                        selectedReviewForPopup = null
                        showAdminWarning = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirm Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminWarning = false }) {
                    Text("Review Again")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "$label:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}
