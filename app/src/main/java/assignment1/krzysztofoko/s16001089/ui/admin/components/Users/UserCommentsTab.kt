package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.ReviewLocal
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth
import java.text.SimpleDateFormat
import java.util.*

/**
 * UserCommentsTab provides an interface for auditing student reviews.
 * Refactored to support both Admin and Student Profile views without a direct ViewModel dependency.
 */
@Composable
fun UserCommentsTab(
    reviews: List<ReviewLocal>, 
    allBooks: List<Book>,
    isAdmin: Boolean = false,
    onDeleteComment: (Int) -> Unit = {},
    onUpdateReview: (ReviewLocal) -> Unit = {}
) {
    var editingReview by remember { mutableStateOf<ReviewLocal?>(null) }
    var reviewToDelete by remember { mutableStateOf<ReviewLocal?>(null) }
    var editedCommentText by remember { mutableStateOf("") }
    var showAdminWarning by remember { mutableStateOf(false) }

    if (reviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.RateReview, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))
                Text("No academic reviews found.", color = Color.Gray) 
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderDetails(if (isAdmin) "Student Feedback Audit (${reviews.size})" else "My Comments (${reviews.size})")
            }
            
            items(reviews) { review ->
                val book = allCoursesFixed(allBooks).find { it.id == review.productId } ?: allBooks.find { it.id == review.productId }
                
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp), 
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book?.title ?: "Unknown Item", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Target ID: ${review.productId}", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray
                                )
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { 
                                    editingReview = review
                                    editedCommentText = review.comment
                                }) { 
                                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) 
                                }
                                IconButton(onClick = { reviewToDelete = review }) { 
                                    Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) 
                                }
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        
                        Text(
                            text = "\"${review.comment}\"", 
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                            Spacer(Modifier.width(6.6.dp))
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(review.timestamp)), 
                                style = MaterialTheme.typography.labelSmall, 
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // --- EDIT DIALOG ---
    if (editingReview != null) {
        Dialog(onDismissRequest = { editingReview = null }) {
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
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.EditNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text("Edit Feedback", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = editedCommentText,
                        onValueChange = { editedCommentText = it },
                        label = { Text("Modified Comment") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { editingReview = null }) { Text("Discard", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(12.dp))
                        Button(onClick = { 
                            if (isAdmin) showAdminWarning = true 
                            else {
                                onUpdateReview(editingReview!!.copy(comment = editedCommentText))
                                editingReview = null
                            }
                        }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text(if (isAdmin) "Review Update" else "Save Update", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- POLICY WARNING OVERRIDE ---
    if (showAdminWarning && editingReview != null) {
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
                    Text("Administrative Audit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        "You are modifying institutional feedback. This action is tracked and must comply with our professional conduct policies.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showAdminWarning = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Re-Evaluate") }
                        Button(
                            onClick = {
                                onUpdateReview(editingReview!!.copy(comment = editedCommentText))
                                editingReview = null
                                showAdminWarning = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Confirm Override", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // --- SAFETY DELETE CONFIRMATION ---
    if (reviewToDelete != null) {
        Dialog(onDismissRequest = { reviewToDelete = null }) {
            Surface(
                modifier = Modifier.padding(24.dp).adaptiveWidth(AdaptiveWidths.Standard),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    @Suppress("DEPRECATION")
                    Text("Remove Comment?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        "Are you sure you want to permanently delete this feedback? This operation cannot be reversed.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { reviewToDelete = null }, modifier = Modifier.weight(1f)) { Text("Keep Comment") }
                        Button(
                            onClick = {
                                onDeleteComment(reviewToDelete!!.reviewId)
                                reviewToDelete = null
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
}

private fun allCoursesFixed(books: List<Book>) = books // Fallback
