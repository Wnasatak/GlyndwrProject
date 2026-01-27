package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ProfileHeader(
    photoUrl: String?,
    isUploading: Boolean,
    onPickPhoto: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(vertical = 24.dp)) {
        UserAvatar(
            photoUrl = photoUrl,
            modifier = Modifier.size(130.dp),
            isLarge = true,
            onClick = { if (!isUploading) onPickPhoto() },
            overlay = {
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        )
        SmallFloatingActionButton(
            onClick = { if (!isUploading) onPickPhoto() },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.offset(x = (-8).dp, y = (-8).dp)
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White)
        }
    }
}

@Composable
fun PersonalInfoSection(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    surname: String,
    onSurnameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    email: String,
    onEditEmail: () -> Unit
) {
    Column {
        Text(
            "Personal Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text("First Name") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = surname,
                onValueChange = onSurnameChange,
                label = { Text("Surname") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            onClick = onEditEmail,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Email Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Text("Edit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ProfileAddressSection(
    address: String,
    onChangeAddress: () -> Unit
) {
    Column {
        Text(
            "Active Address",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            onClick = onChangeAddress,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text(
                    address,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text("Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ProfilePaymentSection(
    paymentMethod: String,
    onChangePayment: () -> Unit
) {
    Column {
        Text(
            "Active Payment Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            onClick = onChangePayment,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        paymentMethod.contains("Google") -> Icons.Default.AccountBalanceWallet
                        paymentMethod.contains("PayPal") -> Icons.Default.Payment
                        paymentMethod.contains("Uni") -> Icons.Default.School
                        else -> Icons.Default.CreditCard
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Text(paymentMethod, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ProfileSecuritySection(
    onChangePassword: () -> Unit
) {
    Column {
        Text(
            "Account Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Lock, null)
            Spacer(Modifier.width(8.dp))
            Text("Change Account Password")
        }
    }
}

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
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(targetState = currentStep, label = "popupStepTransition") { step ->
                    if (step == 1) {
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Details Confirmation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(24.dp))
                            when {
                                selectedMethod.contains("Card") -> {
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
                                            value = "",
                                            onValueChange = {},
                                            label = { Text("CVV") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                                selectedMethod.contains("PayPal") -> {
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
                                        val finalMethod = if (selectedMethod.contains("Card")) {
                                            "Card Ending in " + cardNumber.takeLast(4).ifEmpty { "4242" }
                                        } else selectedMethod
                                        onConfirm(finalMethod)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Confirm") }
                            }
                        }
                    }
                }
            }
        }
    }
}
