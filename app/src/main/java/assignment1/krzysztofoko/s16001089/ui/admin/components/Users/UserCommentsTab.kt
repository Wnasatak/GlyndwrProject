package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.ReviewLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminUserDetailsViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserCommentsTab(reviews: List<ReviewLocal>, viewModel: AdminUserDetailsViewModel) {
    val allBooks by viewModel.allBooks.collectAsState()
    var editingReview by remember { mutableStateOf<ReviewLocal?>(null) }
    var reviewToDelete by remember { mutableStateOf<ReviewLocal?>(null) }
    var editedCommentText by remember { mutableStateOf("") }

    if (reviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Text("No comments found.", color = Color.Gray) 
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reviews) { review ->
                val book = allBooks.find { it.id == review.productId }
                
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp), 
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book?.title ?: "Unknown Item", 
                                    style = MaterialTheme.typography.titleSmall, 
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "ID: ${review.productId}", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray
                                )
                            }
                            
                            Row {
                                IconButton(onClick = { 
                                    editingReview = review
                                    editedCommentText = review.comment
                                }) { 
                                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) 
                                }
                                IconButton(onClick = { reviewToDelete = review }) { 
                                    Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) 
                                }
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                        
                        Text(
                            text = review.comment, 
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(review.timestamp)), 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }

    // Edit Comment Dialog
    if (editingReview != null) {
        AlertDialog(
            onDismissRequest = { editingReview = null },
            title = { Text("Edit Comment", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editedCommentText,
                    onValueChange = { editedCommentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Comment") },
                    minLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateReview(editingReview!!.copy(comment = editedCommentText))
                    editingReview = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text("Delete Comment?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("Are you sure you want to permanently delete this comment? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteComment(reviewToDelete!!.reviewId)
                        reviewToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
