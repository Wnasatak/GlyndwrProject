package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.data.RoleDiscount
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.util.*

/**
 * Centralized Popup Controller.
 * Fully optimized to follow the Pro Signature Design System.
 */
object AppPopups {

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

        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(ProDesign.CardRadius),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { 
                Icon(
                    imageVector = Icons.Default.LibraryAdd, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(32.dp)
                ) 
            },
            title = { Text(AppConstants.TITLE_ADD_TO_LIBRARY, fontWeight = FontWeight.Black) },
            text = { Text("Do you want to add '$itemTitle' to your collection for free?", textAlign = TextAlign.Center) },
            confirmButton = {
                Button(onClick = onConfirm, shape = RoundedCornerShape(12.dp)) {
                    Text(AppConstants.BTN_ADD_TO_LIBRARY, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(AppConstants.BTN_CANCEL)
                }
            }
        )
    }

    @Composable
    fun AddingToLibraryLoading(show: Boolean, category: String, isAudioBook: Boolean = false) {
        if (!show) return
        Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Surface(modifier = Modifier.size(200.dp), shape = RoundedCornerShape(ProDesign.CardRadius), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(60.dp), color = MaterialTheme.colorScheme.primary, strokeCap = StrokeCap.Round)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Processing...", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun RemovingFromLibraryLoading(show: Boolean) {
        if (!show) return
        Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Surface(modifier = Modifier.size(200.dp), shape = RoundedCornerShape(ProDesign.CardRadius), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(60.dp), color = MaterialTheme.colorScheme.error, strokeCap = StrokeCap.Round)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Removing...", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun AuthLoading(show: Boolean, message: String = "Securing your session...") {
        if (!show) return
        Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Surface(modifier = Modifier.size(200.dp), shape = RoundedCornerShape(ProDesign.CardRadius), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(60.dp), color = MaterialTheme.colorScheme.primary, strokeCap = StrokeCap.Round)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(message, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }

    @Composable
    fun LogoutConfirmation(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(ProDesign.CardRadius),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(AppConstants.TITLE_LOG_OFF, fontWeight = FontWeight.Black) },
            text = { Text(AppConstants.MSG_LOG_OFF_DESC) },
            confirmButton = { 
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { 
                    Text("Sign Off", fontWeight = FontWeight.Bold) 
                } 
            },
            dismissButton = { 
                TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL) } 
            }
        )
    }

    @Composable
    fun SignedOutSuccess(show: Boolean, onDismiss: () -> Unit) {
        if (!show) return
        var timeLeft by remember { mutableStateOf(3) }
        LaunchedEffect(Unit) { while (timeLeft > 0) { delay(1000); timeLeft-- }; onDismiss() }
        
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(ProDesign.CardRadius),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(AppConstants.TITLE_SIGNED_OUT, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Text("You have been securely signed out.", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress = { timeLeft.toFloat() / 3f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }

    @Composable
    fun AuthDemoCode(show: Boolean, code: String, onDismiss: () -> Unit) {
        if (!show) return
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(ProDesign.CardRadius),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
            title = { Text(AppConstants.TITLE_DEMO_MODE, fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(AppConstants.MSG_DEMO_MODE_DESC, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(text = code, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, letterSpacing = 8.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = { Button(onClick = onDismiss) { Text(AppConstants.BTN_GOT_IT) } }
        )
    }

    @Composable
    fun AuthSuccess(show: Boolean, isDarkTheme: Boolean, onDismiss: () -> Unit) {
        if (!show) return
        val totalTime = 5000 
        var timeLeftMs by remember { mutableStateOf(totalTime) }
        
        LaunchedEffect(Unit) {
            val start = System.currentTimeMillis()
            while (timeLeftMs > 0) {
                val elapsed = (System.currentTimeMillis() - start).toInt()
                timeLeftMs = (totalTime - elapsed).coerceAtLeast(0)
                delay(50)
            }
            onDismiss()
        }

        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalWavyBackground(isDarkTheme = isDarkTheme)
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(100.dp))
                        Spacer(Modifier.height(24.dp))
                        Text(AppConstants.TITLE_IDENTITY_VERIFIED, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(40.dp))
                        CircularProgressIndicator(
                            progress = { timeLeftMs.toFloat() / totalTime.toFloat() },
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onDismiss, shape = RoundedCornerShape(ProDesign.MenuRadius)) {
                            Text(AppConstants.BTN_CONTINUE_HOME, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RemoveFromLibraryConfirmation(show: Boolean, bookTitle: String, isCourse: Boolean = false, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        if (!show) return
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(ProDesign.CardRadius),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove from Library", fontWeight = FontWeight.Black) },
            text = { Text("Are you sure you want to remove '$bookTitle' from your collection?") },
            confirmButton = { 
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { 
                    Text("Remove", fontWeight = FontWeight.Bold) 
                } 
            },
            dismissButton = { 
                TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL) } 
            }
        )
    }

    @Composable
    fun OrderPurchase(show: Boolean, book: Book?, user: UserLocal?, roleDiscounts: List<RoleDiscount> = emptyList(), onDismiss: () -> Unit, onEditProfile: () -> Unit, onComplete: (Double, String) -> Unit) {
        if (show && book != null) { OrderFlowDialog(book = book, user = user, roleDiscounts = roleDiscounts, onDismiss = onDismiss, onEditProfile = onEditProfile, onComplete = onComplete) }
    }

    @Composable
    fun ProductQuickView(show: Boolean, book: Book?, onDismiss: () -> Unit, onReadMore: (String) -> Unit) {
        if (show && book != null) { QuickViewDialog(book = book, onDismiss = onDismiss, onReadMore = onReadMore) }
    }

    @Composable
    fun WalletTopUp(show: Boolean, user: UserLocal?, onDismiss: () -> Unit, onTopUpComplete: (Double) -> Unit, onManageProfile: () -> Unit) {
        if (show) { 
            IntegratedTopUpDialog(
                user = user, 
                onDismiss = onDismiss, 
                onTopUpComplete = onTopUpComplete, 
                onManageProfile = onManageProfile
            ) 
        }
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
        if (show) { 
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(ProDesign.CardRadius),
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Delete Review", fontWeight = FontWeight.Black) },
                text = { Text("Permanently delete this comment? This action cannot be undone.") },
                confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete", fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
            )
        }
    }

    @Composable
    fun SaveReviewChangesConfirmation(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        if (show) { 
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(ProDesign.CardRadius),
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Save Changes", fontWeight = FontWeight.Black) },
                text = { Text("Do you want to save the edits to your review?") },
                confirmButton = { Button(onClick = onConfirm) { Text("Save", fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Discard") } }
            )
        }
    }

    @Composable
    fun AppInfoDialog(show: Boolean, onDismiss: () -> Unit) {
        if (!show) return
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(ProDesign.CardRadius),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { 
                Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = Color.White, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                    AsyncImage(model = "file:///android_asset/images/media/GlyndwrUniversity.jpg", contentDescription = "App Logo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            },
            title = { Text("Wrexham University Portal", fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Version 1.0.0 (Stable)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("Developed as part of the Mobile App Development module.", textAlign = TextAlign.Center)
                }
            },
            confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
        )
    }

    @Composable
    fun AdminProjectDetails(show: Boolean, onDismiss: () -> Unit) {
        if (show) {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(ProDesign.CardRadius),
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.border(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(ProDesign.CardRadius)),
                title = { Text("Project Documentation", fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PopupItem(Icons.Rounded.Info, "Description", AppConstants.PROJECT_INFO)
                        PopupItem(Icons.Rounded.AccountCircle, "Student Name", AppConstants.DEVELOPER_NAME)
                        PopupItem(Icons.Rounded.Settings, "Build Version", AppConstants.VERSION_NAME)
                    }
                },
                confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
            )
        }
    }

    @Composable
    fun AdminSaveSuccess(show: Boolean, onDismiss: () -> Unit) {
        if (show) {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(ProDesign.CardRadius),
                containerColor = MaterialTheme.colorScheme.surface,
                icon = { Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
                title = { Text("Update Successful", fontWeight = FontWeight.Black) },
                text = { Text("Role-based discounts have been applied across the university system.", textAlign = TextAlign.Center) },
                confirmButton = { Button(onClick = onDismiss) { Text("Perfect") } }
            )
        }
    }

    @Composable
    private fun PopupItem(icon: ImageVector, label: String, value: String) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
