package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

/**
 * SelectionOption Composable
 * 
 * A high-level UI component designed for list-based selections within dialogs or settings screens.
 * It provides a consistent look and feel for selectable items, featuring:
 * - A leading icon for visual context.
 * - A primary title label.
 * - A trailing RadioButton to indicate the current selection state.
 * - Interactive feedback via Surface's onClick and dynamic coloring/bordering.
 *
 * @param title The text label describing the option.
 * @param icon The ImageVector icon representing the option.
 * @param isSelected Boolean state indicating if this specific option is active.
 * @param onClick Callback triggered when the entire surface area is tapped.
 */
@Composable
fun SelectionOption(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        // Switch between primary container color when selected and a subtle variant when idle.
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        // Add a primary-colored border only when the item is selected.
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon tint changes based on selection to guide user attention.
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, 
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            // Text becomes bold when selected for better hierarchy.
            @Suppress("DEPRECATION")
            Text(
                title, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, 
                modifier = Modifier.weight(1f)
            )
            // The RadioButton acts as a secondary visual confirmation.
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

/**
 * TopUpDialog Composable
 * 
 * A specialized dialog for managing financial top-ups within the application.
 * It utilizes a two-step wizard-like flow to ensure users review their choice before committing.
 * 
 * Flow:
 * 1. Amount Selection: Choose from presets (£5, £10, £20, £50) or enter a custom decimal value.
 * 2. Confirmation: Review the final amount and trigger the processing state.
 *
 * @param onDismiss Callback invoked when the user cancels or closes the dialog.
 * @param onComplete Callback invoked with the final Double amount once the user confirms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpDialog(onDismiss: () -> Unit, onComplete: (Double) -> Unit) {
    // Current step in the top-up wizard.
    var step by remember { mutableIntStateOf(1) }
    // Tracks the selected preset amount. Defaults to £10.
    var selectedAmount by remember { mutableDoubleStateOf(10.0) }
    // Tracks the raw string input for custom amounts.
    var customAmount by remember { mutableStateOf("") }
    // State to indicate an ongoing backend transaction.
    var isProcessing by remember { mutableStateOf(false) }
    // Pre-defined values for quick selection.
    val amounts = listOf(5.0, 10.0, 20.0, 50.0)

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Top Up Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Step $step of 2", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(24.dp))

                // Smoothly transition between the selection and confirmation screens.
                AnimatedContent(targetState = step, label = "topUpStep") { currentStep ->
                    if (currentStep == 1) {
                        Column {
                            Text("Select Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            // Horizontal row of chips for quick amount selection.
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                amounts.forEach { amt ->
                                    FilterChip(
                                        selected = selectedAmount == amt && customAmount.isEmpty(),
                                        onClick = { selectedAmount = amt; customAmount = "" },
                                        label = { Text("£${amt.toInt()}") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // TextField for granular amount entry.
                            OutlinedTextField(
                                value = customAmount,
                                onValueChange = { customAmount = it },
                                label = { Text("Custom Amount (£)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Payments, null) }
                            )
                        }
                    } else {
                        // Confirmation UI: Summarizes the transaction.
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                            val amtStr = formatPrice(finalAmt) // Formats the double to a localized price string.
                            Icon(Icons.Default.Security, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Confirm Transaction", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "You are about to add £$amtStr to your University Wallet.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            // Loading state shown while the payment is being 'processed'.
                            if (isProcessing) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator()
                                Text("Processing...", modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Navigation Controls: Back/Cancel and Next/Action.
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { if (step == 1) onDismiss() else { step = 1 } },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        Text(if (step == 1) "Cancel" else "Back")
                    }
                    Button(
                        onClick = {
                            if (step == 1) {
                                step = 2 // Progress to confirmation.
                            } else {
                                isProcessing = true
                                val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                                onComplete(finalAmt) // Finalize.
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        Text(if (step == 1) "Next" else "Top Up Now")
                    }
                }
            }
        }
    }
}

/**
 * EmailChangeDialog Composable
 * 
 * A secure 3-step dialog for updating the user's primary email address via Firebase.
 * Security is handled by requiring re-authentication before the update is allowed.
 *
 * Steps:
 * 1. Re-authentication: Verify current ownership via password.
 * 2. Input: Enter the new desired email address.
 * 3. Finalization: Inform the user that a verification email will be sent and they'll be logged out.
 *
 * @param currentEmail The currently logged-in user's email (required for credential creation).
 * @param onDismiss Callback to close the dialog.
 * @param onSuccess Callback invoked when the update process is successfully initiated.
 */
@Composable
fun EmailChangeDialog(currentEmail: String, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var password by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Change Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Step $step of 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                
                // Error display area for validation or Firebase exceptions.
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp), 
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(targetState = step, label = "emailStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when(currentStep) {
                            1 -> {
                                // Re-auth UI
                                Text("Verify Identity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                OutlinedTextField(
                                    value = password, 
                                    onValueChange = { password = it; validationMsg = null }, 
                                    label = { Text("Current Password") }, 
                                    visualTransformation = PasswordVisualTransformation(), 
                                    modifier = Modifier.fillMaxWidth(), 
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            2 -> {
                                // Email Entry UI
                                Text("New Email", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                OutlinedTextField(
                                    value = newEmail, 
                                    onValueChange = { newEmail = it; validationMsg = null }, 
                                    label = { Text("New Email") }, 
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), 
                                    modifier = Modifier.fillMaxWidth(), 
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            3 -> {
                                // Summary and Logout Warning UI
                                Text("Confirmation", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Text(
                                    "Updating to: $newEmail\n\nYou will be logged out to verify your new email.", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    textAlign = TextAlign.Center, 
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                if (loading) { 
                    CircularProgressIndicator() 
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { if (step == 1) onDismiss() else { step--; validationMsg = null } }, 
                            modifier = Modifier.weight(1f)
                        ) { 
                            Text(if (step == 1) "Cancel" else "Back") 
                        }
                        Button(
                            onClick = {
                                when(step) {
                                    1 -> {
                                        // Handle Re-authentication logic via Firebase.
                                        if (password.isEmpty()) { validationMsg = "Please enter your password."; return@Button }
                                        loading = true
                                        val cred = EmailAuthProvider.getCredential(currentEmail, password)
                                        auth.currentUser?.reauthenticate(cred)?.addOnCompleteListener {
                                            loading = false
                                            if (it.isSuccessful) step = 2 else validationMsg = "Incorrect password."
                                        }
                                    }
                                    2 -> {
                                        // Basic format validation before moving to the final step.
                                        if (newEmail.isEmpty() || !newEmail.contains("@")) { validationMsg = "Please enter a valid email."; return@Button }
                                        step = 3
                                    }
                                    3 -> {
                                        // Trigger the sensitive email update action.
                                        loading = true
                                        auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener {
                                            loading = false
                                            if (it.isSuccessful) onSuccess(newEmail) else validationMsg = it.exception?.localizedMessage ?: "Failed."
                                        }
                                    }
                                }
                            }, 
                            modifier = Modifier.weight(1f)
                        ) { 
                            Text(if (step == 3) "Verify & Logout" else "Next") 
                        }
                    }
                }
            }
        }
    }
}

/**
 * PasswordChangeDialog Composable
 * 
 * Provides a structured UI for users to update their account password.
 * Similar to EmailChangeDialog, it uses a 3-step security flow.
 *
 * Steps:
 * 1. Current Password Verification: To prevent unauthorized changes.
 * 2. New Password Input: Defining the new secret.
 * 3. Confirmation: Ensuring the user didn't make a typo in step 2.
 *
 * @param userEmail Current user's email for re-authentication.
 * @param onDismiss Closes the dialog.
 * @param onSuccess Invoked after successful Firebase password update.
 */
@Composable
fun PasswordChangeDialog(userEmail: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp), 
            shape = RoundedCornerShape(28.dp), 
            color = MaterialTheme.colorScheme.surface, 
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Step $step of 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp), 
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)), 
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(text = validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(targetState = step, label = "passStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        when(currentStep) {
                            1 -> {
                                Text("Verify it's you", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(
                                    value = currentPassword, 
                                    onValueChange = { currentPassword = it; validationMsg = null }, 
                                    label = { Text("Current Password") }, 
                                    visualTransformation = PasswordVisualTransformation(), 
                                    modifier = Modifier.fillMaxWidth(), 
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            2 -> {
                                Text("New Password", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(
                                    value = newPassword, 
                                    onValueChange = { newPassword = it; validationMsg = null }, 
                                    label = { Text("New Password") }, 
                                    visualTransformation = PasswordVisualTransformation(), 
                                    modifier = Modifier.fillMaxWidth(), 
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            3 -> {
                                Text("Confirm Password", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(
                                    value = repeatPassword, 
                                    onValueChange = { repeatPassword = it; validationMsg = null }, 
                                    label = { Text("Repeat Password") }, 
                                    visualTransformation = PasswordVisualTransformation(), 
                                    modifier = Modifier.fillMaxWidth(), 
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (loading) { 
                    CircularProgressIndicator() 
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { if (step == 1) onDismiss() else { step--; validationMsg = null } }, 
                            modifier = Modifier.weight(1f), 
                            shape = RoundedCornerShape(12.dp)
                        ) { 
                            Text(if (step == 1) "Cancel" else "Back") 
                        }
                        Button(
                            onClick = {
                                when(step) {
                                    1 -> {
                                        // Re-authenticate user before allowing password change.
                                        if (currentPassword.isEmpty()) { validationMsg = "Please enter your current password first."; return@Button }
                                        loading = true
                                        val credential = EmailAuthProvider.getCredential(userEmail, currentPassword)
                                        auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
                                            loading = false
                                            if (it.isSuccessful) step = 2 else validationMsg = "Incorrect password."
                                        }
                                    }
                                    2 -> {
                                        // Security check: minimum password length.
                                        if (newPassword.isEmpty()) { validationMsg = "Empty password."; return@Button }
                                        if (newPassword.length < 6) { validationMsg = "Too short."; return@Button }
                                        step = 3
                                    }
                                    3 -> {
                                        // Mismatch check.
                                        if (newPassword != repeatPassword) { validationMsg = "Mismatch."; return@Button }
                                        loading = true
                                        auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener {
                                            loading = false
                                            if (it.isSuccessful) onSuccess() else validationMsg = it.exception?.localizedMessage ?: "Failed."
                                        }
                                    }
                                }
                            }, 
                            modifier = Modifier.weight(1f), 
                            shape = RoundedCornerShape(12.dp)
                        ) { 
                            Text(if (step == 3) "Save" else "Next") 
                        }
                    }
                }
            }
        }
    }
}

/**
 * AddressManagementDialog Composable
 * 
 * A cleaner UI for collecting complex address data by splitting fields across two steps.
 * This prevents the keyboard from overlapping fields on smaller screens.
 * 
 * Step 1: Broad location (Street, City).
 * Step 2: Specific location (Postcode, Country).
 *
 * @param onDismiss Closes the dialog.
 * @param onSave Returns the concatenated and formatted address string.
 */
@Composable
fun AddressManagementDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var validationMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp), 
            shape = RoundedCornerShape(28.dp), 
            color = MaterialTheme.colorScheme.surface, 
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Manage Address", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp), 
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)), 
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(text = validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(targetState = step, label = "addrStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (currentStep == 1) {
                            Text("Location Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = street, onValueChange = { street = it; validationMsg = null }, label = { Text("Street Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = city, onValueChange = { city = it; validationMsg = null }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        } else {
                            Text("Final Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = postcode, onValueChange = { postcode = it; validationMsg = null }, label = { Text("Postcode") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = country, onValueChange = { country = it; validationMsg = null }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { if (step == 1) onDismiss() else { step = 1; validationMsg = null } }, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(12.dp)
                    ) { 
                        Text(if (step == 1) "Cancel" else "Back") 
                    }
                    Button(
                        onClick = {
                            if (step == 1) {
                                if (street.isEmpty() || city.isEmpty()) { validationMsg = "Fill all."; return@Button }
                                step = 2
                            } else {
                                if (postcode.isEmpty() || country.isEmpty()) { validationMsg = "Fill all."; return@Button }
                                onSave("$street, $city, $postcode, $country")
                            }
                        }, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(12.dp)
                    ) { 
                        Text(if (step == 2) "Save" else "Next") 
                    }
                }
            }
        }
    }
}

/**
 * PaymentMethodDialog Composable
 * 
 * A comprehensive dialog for handling various payment methods. It acts as a shell that 
 * delegates its internal state (like cardNumber or paypalEmail) to its caller, making it 
 * highly adaptable to different view models.
 *
 * It supports:
 * - Card (Credit/Debit) entry with basic CVV and Expiry fields.
 * - PayPal email entry.
 * - Branded options like Google Pay and University Account.
 *
 * @param show Control flag for dialog visibility.
 * @param currentStep Current wizard step (1: Select, 2: Detail).
 * @param selectedMethod Current chosen method name.
 * @param onConfirm Final callback passing the formatted method summary (e.g. "Card Ending in 4242").
 */
@Composable
fun PaymentMethodDialog(
    show: Boolean,
    currentStep: Int,
    selectedMethod: String,
    onMethodSelect: (String) -> Unit,
    onNextStep: () -> Unit,
    onBackStep: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardExpiry: String,
    onCardExpiryChange: (String) -> Unit,
    paypalEmail: String,
    onPaypalEmailChange: (String) -> Unit
) {
    // Early exit if the dialog is not meant to be visible.
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Smoothly slide or fade between selection and detail entry.
                AnimatedContent(targetState = currentStep, label = "popupStepTransition") { step ->
                    if (step == 1) {
                        // Step 1: List of all supported payment methods.
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Choose Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(24.dp))
                            SelectionOption("Credit or Debit Card", Icons.Default.AddCard, selectedMethod.contains("Card")) { onMethodSelect("Credit or Debit Card"); onNextStep() }
                            SelectionOption("Google Pay", Icons.Default.AccountBalanceWallet, selectedMethod == "Google Pay") { onMethodSelect("Google Pay"); onNextStep() }
                            SelectionOption("PayPal", Icons.Default.Payment, selectedMethod == "PayPal") { onMethodSelect("PayPal"); onNextStep() }
                            SelectionOption("University Account", Icons.Default.School, selectedMethod == "University Account") { onMethodSelect("University Account"); onNextStep() }
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = onDismiss) { Text("Cancel") }
                        }
                    } else {
                        // Step 2: Specific detail entry UI based on user selection in Step 1.
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Details Confirmation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            when {
                                selectedMethod.contains("Card") -> {
                                    // Complex multi-field card entry.
                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = onCardNumberChange,
                                        label = { Text("Card Number") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = onCardExpiryChange,
                                            label = { Text("MM/YY") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        OutlinedTextField(
                                            value = "", // Placeholder/Static CVV for demonstration purposes.
                                            onValueChange = {},
                                            label = { Text("CVV") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                                selectedMethod.contains("PayPal") -> {
                                    // Single field email entry for PayPal.
                                    Icon(Icons.Default.Payment, null, modifier = Modifier.size(64.dp), tint = Color(0xFF003087))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = paypalEmail,
                                        onValueChange = onPaypalEmailChange,
                                        label = { Text("PayPal Email") },
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = { Icon(Icons.Default.Email, null) },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                else -> {
                                    // Generic 'Authorize' screen for simpler account-based links.
                                    Icon(
                                        if (selectedMethod.contains("Google")) Icons.Default.AccountBalanceWallet else Icons.Default.School,
                                        null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Authorize link to your account safely.", textAlign = TextAlign.Center)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = onBackStep, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Back") }
                                Button(
                                    onClick = {
                                        // Final string generation for display purposes in the main profile/payment screen.
                                        val finalMethod = if (selectedMethod.contains("Card")) {
                                            "Card Ending in " + cardNumber.takeLast(4).ifEmpty { "4242" }
                                        } else selectedMethod
                                        onConfirm(finalMethod)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { 
                                    Text("Confirm") 
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
