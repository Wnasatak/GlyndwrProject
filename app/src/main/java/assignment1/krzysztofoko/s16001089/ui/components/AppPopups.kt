package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.UserLocal

/**
 * Centralized Popup Controller for the entire project.
 * This object holds all the different types of dialogs and popups used across the app.
 */
object AppPopups {

    /**
     * Shows the Demo Mode code popup
     */
    @Composable
    fun AuthDemoCode(
        show: Boolean,
        code: String,
        onDismiss: () -> Unit
    ) {
        DemoCodePopup(show = show, code = code, onDismiss = onDismiss)
    }

    /**
     * Shows the Quick Summary view for a product
     */
    @Composable
    fun ProductQuickView(
        show: Boolean,
        book: Book?,
        onDismiss: () -> Unit,
        onReadMore: (String) -> Unit
    ) {
        if (show && book != null) {
            QuickViewDialog(book = book, onDismiss = onDismiss, onReadMore = onReadMore)
        }
    }

    /**
     * Shows the Wallet Top-Up dialog
     */
    @Composable
    fun WalletTopUp(
        show: Boolean,
        user: UserLocal?,
        onDismiss: () -> Unit,
        onTopUpComplete: (Double) -> Unit,
        onManageProfile: () -> Unit
    ) {
        if (show) {
            IntegratedTopUpDialog(
                user = user,
                onDismiss = onDismiss,
                onTopUpComplete = onTopUpComplete,
                onManageProfile = onManageProfile
            )
        }
    }

    /**
     * Shows the Order/Purchase flow dialog
     */
    @Composable
    fun OrderPurchase(
        show: Boolean,
        book: Book?,
        user: UserLocal?,
        onDismiss: () -> Unit,
        onEditProfile: () -> Unit,
        onComplete: () -> Unit
    ) {
        if (show && book != null) {
            OrderFlowDialog(
                book = book,
                user = user,
                onDismiss = onDismiss,
                onEditProfile = onEditProfile,
                onComplete = onComplete
            )
        }
    }

    /**
     * Shows the Profile Email Change dialog
     */
    @Composable
    fun ProfileEmailChange(
        show: Boolean,
        currentEmail: String,
        onDismiss: () -> Unit,
        onSuccess: (String) -> Unit
    ) {
        if (show) {
            EmailChangeDialog(currentEmail = currentEmail, onDismiss = onDismiss, onSuccess = onSuccess)
        }
    }

    /**
     * Shows the Profile Password Change dialog
     */
    @Composable
    fun ProfilePasswordChange(
        show: Boolean,
        userEmail: String,
        onDismiss: () -> Unit,
        onSuccess: () -> Unit
    ) {
        if (show) {
            PasswordChangeDialog(userEmail = userEmail, onDismiss = onDismiss, onSuccess = onSuccess)
        }
    }

    /**
     * Shows the Address Management dialog
     */
    @Composable
    fun AddressManagement(
        show: Boolean,
        onDismiss: () -> Unit,
        onSave: (String) -> Unit
    ) {
        if (show) {
            AddressManagementDialog(onDismiss = onDismiss, onSave = onSave)
        }
    }

    /**
     * Confirmation popup for removing a book from the library.
     */
    @Composable
    fun RemoveFromLibraryConfirmation(
        show: Boolean,
        bookTitle: String,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        if (show) {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Remove from Library?") },
                text = { Text("Are you sure you want to remove '$bookTitle' from your library? You can always add it back later if it's still available.") },
                confirmButton = {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Internal Demo Code Popup implementation
 */
@Composable
private fun DemoCodePopup(
    show: Boolean,
    code: String,
    onDismiss: () -> Unit
) {
    if (show) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Security, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Demo Mode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "For this demonstration, your code is provided below, but please also check your email! 😊",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = code,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 8.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "SMTP Verification Implemented!",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFE91E63)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Got it")
                    }
                }
            }
        }
    }
}
