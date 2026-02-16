package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Centralized Popup Controller.
 * Refactored to use standardized Design Tokens from AdaptiveUtils.
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
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.LibraryAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AdaptiveDimensions.MediumIconSize)
                )
            },
            title = { Text(AppConstants.TITLE_ADD_TO_LIBRARY, fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
            text = { Text("Do you want to add '$itemTitle' to your collection for free?", textAlign = TextAlign.Center, style = AdaptiveTypography.body()) },
            confirmButton = {
                Button(onClick = onConfirm, shape = RoundedCornerShape(12.dp)) {
                    Text(AppConstants.BTN_ADD_TO_LIBRARY, fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(AppConstants.BTN_CANCEL, style = AdaptiveTypography.label())
                }
            }
        )
    }

    @Composable
    fun AddingToLibraryLoading(show: Boolean, category: String, isAudioBook: Boolean = false) {
        if (!show) return
        LoadingDialog(message = "Processing...")
    }

    @Composable
    fun RemovingFromLibraryLoading(show: Boolean) {
        if (!show) return
        LoadingDialog(message = "Removing...", color = MaterialTheme.colorScheme.error)
    }

    @Composable
    fun AuthLoading(show: Boolean, message: String = "Securing your session...") {
        if (!show) return
        LoadingDialog(message = message)
    }

    /**
     * Reusable high-performance loading dialog.
     */
    @Composable
    private fun LoadingDialog(message: String, color: Color = MaterialTheme.colorScheme.primary) {
        Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Surface(
                modifier = Modifier.size(AdaptiveDimensions.LoadingDialogSize), 
                shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()), 
                color = MaterialTheme.colorScheme.surface, 
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(AdaptiveDimensions.LoadingIndicatorSize), color = color, strokeCap = StrokeCap.Round)
                    Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
                    @Suppress("DEPRECATION")
                    Text(message, style = AdaptiveTypography.sectionHeader(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun LogoutConfirmation(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(AppConstants.TITLE_LOG_OFF, fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
            text = { Text(AppConstants.MSG_LOG_OFF_DESC, style = AdaptiveTypography.body()) },
            confirmButton = {
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    @Suppress("DEPRECATION")
                    Text("Sign Off", fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL, style = AdaptiveTypography.label()) }
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
                modifier = Modifier.fillMaxWidth().padding(AdaptiveSpacing.contentPadding()),
                shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(AdaptiveSpacing.dialogPadding()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(AdaptiveDimensions.LargeIconSize))
                    Spacer(Modifier.height(AdaptiveSpacing.medium()))
                    Text(AppConstants.TITLE_SIGNED_OUT, style = AdaptiveTypography.headline(), fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(AdaptiveSpacing.small()))
                    Text("You have been securely signed out.", textAlign = TextAlign.Center, style = AdaptiveTypography.body())
                    Spacer(Modifier.height(AdaptiveSpacing.large()))
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
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AdaptiveDimensions.MediumIconSize)) },
            title = { Text(AppConstants.TITLE_DEMO_MODE, fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(AppConstants.MSG_DEMO_MODE_DESC, textAlign = TextAlign.Center, style = AdaptiveTypography.body())
                    Spacer(Modifier.height(AdaptiveSpacing.medium()))
                    Text(text = code, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, letterSpacing = 8.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = { Button(onClick = onDismiss) { Text(AppConstants.BTN_GOT_IT, style = AdaptiveTypography.label()) } }
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
                        modifier = Modifier.fillMaxSize().padding(AdaptiveSpacing.large()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(100.dp))
                        Spacer(Modifier.height(AdaptiveSpacing.large()))
                        Text(AppConstants.TITLE_IDENTITY_VERIFIED, style = AdaptiveTypography.display(), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(40.dp))
                        CircularProgressIndicator(
                            progress = { timeLeftMs.toFloat() / totalTime.toFloat() },
                            modifier = Modifier.size(AdaptiveDimensions.LargeIconSize),
                            color = MaterialTheme.colorScheme.primary,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.height(AdaptiveSpacing.large()))
                        Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                            @Suppress("DEPRECATION")
                            Text(AppConstants.BTN_CONTINUE_HOME, fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
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
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(AdaptiveDimensions.MediumIconSize)) },
            title = { Text("Remove from Library", fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
            text = { Text("Are you sure you want to remove '$bookTitle' from your collection?", style = AdaptiveTypography.body()) },
            confirmButton = {
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    @Suppress("DEPRECATION")
                    Text("Remove", fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL, style = AdaptiveTypography.label()) }
            }
        )
    }

    @Composable
    fun AdminSaveSuccess(show: Boolean, onDismiss: () -> Unit) {
        if (!show) return
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(AdaptiveDimensions.MediumIconSize)) },
            title = { Text("Configuration Saved", fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
            text = { Text("The changes have been applied to the system registry successfully.", textAlign = TextAlign.Center, style = AdaptiveTypography.body()) },
            confirmButton = {
                Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                    Text(AppConstants.BTN_GOT_IT, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    @Composable
    fun AdminProjectDetails(show: Boolean, onDismiss: () -> Unit) {
        if (!show) return
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Project Manifesto", fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProjectManifestoRow("Assignment", "Assignment 1 - CONL709")
                    ProjectManifestoRow("Institution", AppConstants.INSTITUTION)
                    ProjectManifestoRow("Developer", AppConstants.DEVELOPER_NAME)
                    ProjectManifestoRow("Student ID", AppConstants.STUDENT_ID)
                    ProjectManifestoRow("Version", AppConstants.VERSION_NAME)
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    @Suppress("DEPRECATION")
                    Text("Close", fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                }
            }
        )
    }

    @Composable
    private fun ProjectManifestoRow(label: String, value: String) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = AdaptiveTypography.label(), color = Color.Gray)
            Text(value, style = AdaptiveTypography.caption(), fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    fun SaveReviewChangesConfirmation(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        if (!show) return
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(AppConstants.TITLE_SAVE_CHANGES, fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
            text = { Text(AppConstants.MSG_SAVE_CHANGES_DESC, style = AdaptiveTypography.body()) },
            confirmButton = {
                Button(onClick = onConfirm) {
                    @Suppress("DEPRECATION")
                    Text(AppConstants.BTN_SAVE, fontWeight = FontWeight.Bold, style = AdaptiveTypography.sectionHeader())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(AppConstants.BTN_DISCARD, color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    @Composable
    fun OrderPurchase(show: Boolean, book: Book?, user: UserLocal?, roleDiscounts: List<RoleDiscount> = emptyList(), onDismiss: () -> Unit, onEditProfile: () -> Unit, onComplete: (Double, String) -> Unit) {
        if (show && book != null) { OrderFlowDialog(book = book, user = user, roleDiscounts = roleDiscounts, onDismiss = onDismiss, onEditProfile = onEditProfile, onComplete = { total, ref -> onComplete(total, ref) }) }
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
        if (show) {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                containerColor = MaterialTheme.colorScheme.surface,
                icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(AdaptiveDimensions.MediumIconSize)) },
                title = { Text(AppConstants.TITLE_DELETE_REVIEW, fontWeight = FontWeight.Black, style = AdaptiveTypography.headline()) },
                text = { Text(AppConstants.MSG_DELETE_REVIEW_DESC, style = AdaptiveTypography.body()) },
                confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { @Suppress("DEPRECATION") Text("Delete Permanently", style = AdaptiveTypography.sectionHeader()) } },
                dismissButton = { TextButton(onClick = onDismiss) { Text(AppConstants.BTN_CANCEL, style = AdaptiveTypography.label()) } }
            )
        }
    }
}
