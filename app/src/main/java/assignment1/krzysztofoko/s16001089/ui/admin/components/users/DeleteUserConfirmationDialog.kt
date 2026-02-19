package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * DeleteUserConfirmationDialog.kt
 *
 * A specialized modal dialog used to verify high-stakes administrative actions.
 * It implements a mandatory text-matching safeguard ("DELETE") to prevent 
 * accidental removal of user accounts.
 */

/**
 * Renders a safety-first confirmation dialog for account deletion.
 *
 * @param userName The display name of the user target for deletion.
 * @param onDismiss Invoked when the admin cancels the action.
 * @param onConfirm Invoked when the admin confirms deletion by matching the safeguard text.
 */
@Composable
fun DeleteUserConfirmationDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // --- STATE MANAGEMENT ---
    // Tracks the user's manual entry for verification.
    var confirmationText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        // The title uses the system 'error' color to signal a destructive action.
        title = { 
            Text(
                text = "Delete Account", 
                fontWeight = FontWeight.Black, 
                color = MaterialTheme.colorScheme.error
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                @Suppress("DEPRECATION")
                Text(
                    text = "Are you sure you want to permanently delete the account for $userName? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                @Suppress("DEPRECATION")
                Text(
                    text = "To confirm, please type DELETE below:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                // safeguard input field
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    placeholder = { Text("Type DELETE here") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            // The button remains disabled until the safeguard string exactly matches "DELETE".
            Button(
                onClick = onConfirm,
                enabled = confirmationText.trim().equals("DELETE", ignoreCase = true),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Confirm Deletion", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
