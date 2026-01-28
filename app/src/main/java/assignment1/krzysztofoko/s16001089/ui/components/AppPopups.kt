package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.UserLocal
import kotlinx.coroutines.delay

/**
 * Centralized Popup Controller.
 * Fully aligned with AppConstants.kt definitions.
 */
object AppPopups {

    /**
     * Confirmation dialog for adding a free item with custom templates.
     */
    @Composable
    fun AddToLibraryConfirmation(
        show: Boolean,
        itemTitle: String,
        category: String,
        isAudioBook: Boolean = false,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        if (!show) return

        val icon = when {
            isAudioBook -> Icons.Default.Headphones
            category == AppConstants.CAT_GEAR -> Icons.Default.Checkroom
            category == AppConstants.CAT_COURSES -> Icons.Default.School
            else -> Icons.Default.LibraryAdd
        }

        val title = when {
            category == AppConstants.CAT_COURSES -> AppConstants.TITLE_COURSE_ENROLLMENT
            category == AppConstants.CAT_GEAR -> AppConstants.TITLE_ITEM_RESERVATION
            else -> AppConstants.TITLE_ADD_TO_LIBRARY
        }

        val prompt = when {
            isAudioBook -> "Do you want to add the digital audiobook '$itemTitle' to your library collection?"
            category == AppConstants.CAT_GEAR -> "Would you like to reserve '$itemTitle' for free pick-up at the Student Hub?"
            category == AppConstants.CAT_COURSES -> "Are you ready to enroll in the academic course '$itemTitle' for free?"
            else -> "Do you want to add the digital book '$itemTitle' to your collection for free?"
        }

        val confirmText = when {
            category == AppConstants.CAT_GEAR -> AppConstants.BTN_PICKUP_FREE
            category == AppConstants.CAT_COURSES -> AppConstants.BTN_ENROLL_FREE
            else -> AppConstants.BTN_ADD_TO_LIBRARY
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
            title = { Text(title, fontWeight = FontWeight.ExtraBold) },
            text = { Text(prompt, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = onConfirm, shape = RoundedCornerShape(12.dp)) {
                    Text(confirmText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(AppConstants.BTN_CANCEL)
                }
            }
        )
    }

    /**
     * Animated loading popup shown while adding an item to the library.
     */
    @Composable
    fun AddingToLibraryLoading(show: Boolean, category: String, isAudioBook: Boolean = false) {
        if (!show) return
        val icon = when { 
            isAudioBook -> Icons.Default.CloudDownload 
            category == AppConstants.CAT_GEAR -> Icons.Default.ShoppingBag
            category == AppConstants.CAT_COURSES -> Icons.Default.School 
            else -> Icons.Default.LibraryAdd 
        }
        val loadingText = when { 
            category == AppConstants.CAT_GEAR -> "Reserving your item..." 
            category == AppConstants.CAT_COURSES -> "Processing enrollment..." 
            else -> "Syncing with library..." 
        }

        Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Surface(modifier = Modifier.size(200.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "rotation")
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(80.dp).rotate(rotation), color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp, trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), strokeCap = StrokeCap.Round)
                        Icon(icon, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(loadingText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun LogoutConfirmation(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(onDismissRequest = onDismiss, title = { Text(AppConstants.TITLE_LOG_OFF, fontWeight = FontWeight.Bold) }, text = { Text(AppConstants.MSG_LOG_OFF_DESC) }, confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(AppConstants.BTN_LOG_OUT, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL) } })
    }

    @Composable
    fun SignedOutSuccess(show: Boolean, onDismiss: () -> Unit) {
        if (show) {
            var timeLeft by remember { mutableIntStateOf(5) }
            LaunchedEffect(Unit) { while (timeLeft > 0) { delay(1000); timeLeft-- }; onDismiss() }
            AlertDialog(onDismissRequest = onDismiss, icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) }, title = { Text(AppConstants.TITLE_SIGNED_OUT) }, text = { Text("${AppConstants.MSG_SIGNED_OUT_DESC} $timeLeft seconds.", textAlign = TextAlign.Center) }, confirmButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CLOSE) } })
        }
    }

    @Composable
    fun AuthDemoCode(show: Boolean, code: String, onDismiss: () -> Unit) {
        if (show) {
            Dialog(onDismissRequest = onDismiss) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Security, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp)); Text(AppConstants.TITLE_DEMO_MODE, fontWeight = FontWeight.Bold)
                        Text(AppConstants.MSG_DEMO_MODE_DESC, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = code, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, letterSpacing = 8.sp)
                        Spacer(modifier = Modifier.height(24.dp)); Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(AppConstants.BTN_GOT_IT) }
                    }
                }
            }
        }
    }

    @Composable
    fun AuthSuccess(show: Boolean, onDismiss: () -> Unit) {
        if (show) {
            var timeLeft by remember { mutableIntStateOf(10) }
            LaunchedEffect(Unit) { while (timeLeft > 0) { delay(1000); timeLeft-- }; onDismiss() }
            Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(120.dp), tint = Color(0xFF4CAF50))
                        Text(AppConstants.TITLE_IDENTITY_VERIFIED, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                        Text(AppConstants.MSG_IDENTITY_VERIFIED_DESC, textAlign = TextAlign.Center)
                        Text("${AppConstants.TEXT_REDIRECTING} $timeLeft ${AppConstants.TEXT_SECONDS}", modifier = Modifier.padding(top = 12.dp))
                        Spacer(Modifier.height(48.dp)); Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text(AppConstants.BTN_CONTINUE_HOME) }
                    }
                }
            }
        }
    }

    @Composable
    fun RemoveFromLibraryConfirmation(show: Boolean, bookTitle: String, isCourse: Boolean = false, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        if (show) {
            AlertDialog(onDismissRequest = onDismiss, icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) }, title = { Text(AppConstants.TITLE_REMOVE_LIBRARY, fontWeight = FontWeight.Bold) }, text = { Text(AppConstants.MSG_REMOVE_LIBRARY_DESC) }, confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(AppConstants.BTN_REMOVE, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL) } })
        }
    }

    @Composable
    fun OrderPurchase(show: Boolean, book: Book?, user: UserLocal?, onDismiss: () -> Unit, onEditProfile: () -> Unit, onComplete: (Double, String) -> Unit) {
        if (show && book != null) { OrderFlowDialog(book = book, user = user, onDismiss = onDismiss, onEditProfile = onEditProfile, onComplete = onComplete) }
    }

    @Composable
    fun ProductQuickView(show: Boolean, book: Book?, onDismiss: () -> Unit, onReadMore: (String) -> Unit) {
        if (show && book != null) { QuickViewDialog(book = book, onDismiss = onDismiss, onReadMore = onReadMore) }
    }

    @Composable
    fun WalletTopUp(show: Boolean, user: UserLocal?, onDismiss: () -> Unit, onTopUpComplete: (Double) -> Unit, onManageProfile: () -> Unit) {
        if (show) { IntegratedTopUpDialog(user = user, onDismiss = onDismiss, onTopUpComplete = onTopUpComplete, onManageProfile = onManageProfile) }
    }

    @Composable
    fun ProfileEmailChange(show: Boolean, currentEmail: String, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
        if (show) { EmailChangeDialog(currentEmail = currentEmail, onDismiss = onDismiss, onSuccess = onSuccess) }
    }

    @Composable
    fun ProfilePasswordChange(show: Boolean, userEmail: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
        if (show) { PasswordChangeDialog(userEmail = userEmail, onDismiss = onDismiss, onSuccess = onSuccess) }
    }

    @Composable
    fun AddressManagement(show: Boolean, onDismiss: () -> Unit, onSave: (String) -> Unit) {
        if (show) { AddressManagementDialog(onDismiss = onDismiss, onSave = onSave) }
    }

    @Composable
    fun DeleteReviewConfirmation(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        if (show) { AlertDialog(onDismissRequest = onDismiss, title = { Text(AppConstants.TITLE_DELETE_REVIEW, fontWeight = FontWeight.Bold) }, text = { Text(AppConstants.MSG_DELETE_REVIEW_DESC) }, confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(AppConstants.BTN_DELETE, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL) } }) }
    }

    @Composable
    fun SaveReviewChangesConfirmation(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        if (show) { AlertDialog(onDismissRequest = onDismiss, title = { Text(AppConstants.TITLE_SAVE_CHANGES, fontWeight = FontWeight.Bold) }, text = { Text(AppConstants.MSG_SAVE_CHANGES_DESC) }, confirmButton = { Button(onClick = onConfirm) { Text(AppConstants.BTN_SAVE, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_DISCARD) } }) }
    }
}
