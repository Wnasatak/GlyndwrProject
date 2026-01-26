package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.UserLocal
import kotlinx.coroutines.delay

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
     * Full-screen Success view after 2FA verification with an animated countdown timer.
     */
    @Composable
    fun AuthSuccess(
        show: Boolean,
        onDismiss: () -> Unit
    ) {
        if (show) {
            var timeLeft by remember { mutableIntStateOf(10) }
            val progress by animateFloatAsState(
                targetValue = timeLeft / 10f,
                animationSpec = tween(durationMillis = 1000),
                label = "timerProgress"
            )

            LaunchedEffect(Unit) {
                while (timeLeft > 0) {
                    delay(1000)
                    timeLeft--
                }
                onDismiss()
            }

            Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalWavyBackground(isDarkTheme = true)
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                modifier = Modifier.size(120.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                "Identity Verified!",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "Your security check was successful.\nYou are now fully logged in to the Glyndwr University portal.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 24.sp
                            )
                            
                            Spacer(modifier = Modifier.height(48.dp))

                            // Animated Circular Timer
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFF4CAF50),
                                    strokeWidth = 6.dp,
                                    trackColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                    strokeCap = StrokeCap.Round,
                                )
                                Text(
                                    text = timeLeft.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            
                            Text(
                                "Redirecting in $timeLeft seconds...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(48.dp))
                            
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text(
                                    "Continue to Home",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Shows a confirmation that the user has successfully signed out with a 5-second auto-dismiss.
     */
    @Composable
    fun SignedOutSuccess(
        show: Boolean,
        onDismiss: () -> Unit
    ) {
        if (show) {
            var timeLeft by remember { mutableIntStateOf(5) }
            val progress by animateFloatAsState(
                targetValue = timeLeft / 5f,
                animationSpec = tween(durationMillis = 1000),
                label = "logoutTimerProgress"
            )

            LaunchedEffect(Unit) {
                while (timeLeft > 0) {
                    delay(1000)
                    timeLeft--
                }
                onDismiss()
            }

            AlertDialog(
                onDismissRequest = onDismiss,
                icon = { 
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 3.dp,
                            trackColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                    }
                },
                title = { Text("Signed Out") },
                text = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("You have been securely signed out. This message will close in $timeLeft seconds.", textAlign = TextAlign.Center)
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            )
        }
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

    @Composable
    fun EmailChangeDialog(
        currentEmail: String,
        onDismiss: () -> Unit,
        onSuccess: (String) -> Unit
    ) {
        // Placeholder
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

    /**
     * Confirmation popup for deleting a user review.
     */
    @Composable
    fun DeleteReviewConfirmation(
        show: Boolean,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        if (show) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Delete Review") },
                text = { Text("Are you sure you want to permanently delete your review? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
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

    /**
     * Confirmation popup for saving changes to a review.
     */
    @Composable
    fun SaveReviewChangesConfirmation(
        show: Boolean,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        if (show) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Save Changes") },
                text = { Text("Do you want to save the changes to your review?") },
                confirmButton = {
                    Button(onClick = onConfirm) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Discard")
                    }
                }
            )
        }
    }

    /**
     * Confirmation popup for logging out.
     */
    @Composable
    fun LogoutConfirmation(
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Log Off") },
            text = { Text("Are you sure you want to log off?") },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Off", fontWeight = FontWeight.Bold)
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
