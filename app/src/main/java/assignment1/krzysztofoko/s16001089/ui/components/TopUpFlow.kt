package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.UserLocal
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegratedTopUpDialog(
    user: UserLocal?,
    onDismiss: () -> Unit,
    onManageProfile: () -> Unit,
    onTopUpComplete: (Double) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) } // 0: Summary/Settings, 1: Amount, 2: Confirm
    var selectedAmount by remember { mutableDoubleStateOf(10.0) }
    var customAmount by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = when(currentStep) {
                        0 -> "Payment & Wallet"
                        1 -> "Select Top Up Amount"
                        else -> "Confirm Transaction"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (currentStep > 0) {
                    Text(
                        text = "Step $currentStep of 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = currentStep,
                    label = "topUpStepTransition",
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    }
                ) { step ->
                    when (step) {
                        0 -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Current Balance info
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Current Balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        Text(
                                            "£${String.format(Locale.US, "%.2f", user?.balance ?: 0.0)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Payment Method Header + Manage link
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Active Payment Method", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                    TextButton(
                                        onClick = onManageProfile,
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Manage", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when {
                                                user?.selectedPaymentMethod?.contains("PayPal") == true -> Icons.Default.Payment
                                                user?.selectedPaymentMethod?.contains("Google") == true -> Icons.Default.AccountBalanceWallet
                                                else -> Icons.Default.CreditCard
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Text(user?.selectedPaymentMethod ?: "University Account", fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // Action Buttons
                                Button(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Top Up Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        1 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Select Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(16.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        AmountSelectionCard(5.0, selectedAmount == 5.0 && customAmount.isEmpty(), Modifier.weight(1f)) { 
                                            selectedAmount = 5.0; customAmount = "" 
                                        }
                                        AmountSelectionCard(10.0, selectedAmount == 10.0 && customAmount.isEmpty(), Modifier.weight(1f)) { 
                                            selectedAmount = 10.0; customAmount = "" 
                                        }
                                        AmountSelectionCard(20.0, selectedAmount == 20.0 && customAmount.isEmpty(), Modifier.weight(1f)) { 
                                            selectedAmount = 20.0; customAmount = "" 
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        AmountSelectionCard(50.0, selectedAmount == 50.0 && customAmount.isEmpty(), Modifier.weight(1f)) { 
                                            selectedAmount = 50.0; customAmount = "" 
                                        }
                                        CustomAmountInputBox(
                                            value = customAmount,
                                            onValueChange = { 
                                                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                                    customAmount = it
                                                    selectedAmount = -1.0
                                                }
                                            },
                                            isSelected = customAmount.isNotEmpty(),
                                            modifier = Modifier.weight(2f)
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                                val amtStr = String.format(Locale.US, "%.2f", finalAmt)
                                Icon(
                                    Icons.Default.Security, 
                                    null, 
                                    modifier = Modifier.size(64.dp), 
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text("One Final Step", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Confirm adding £$amtStr to your wallet using your ${user?.selectedPaymentMethod ?: "University Account"}.",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                                
                                if (isProcessing) {
                                    Spacer(Modifier.height(16.dp))
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    Text("Verifying...", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Navigation Footer
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (currentStep == 0) {
                        OutlinedButton(
                            onClick = onDismiss, 
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { currentStep -= 1 },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                if (currentStep == 1) {
                                    val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                                    if (finalAmt > 0) {
                                        currentStep = 2
                                    }
                                } else {
                                    isProcessing = true
                                    val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                                    onTopUpComplete(finalAmt)
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isProcessing && (currentStep != 1 || (customAmount.toDoubleOrNull() ?: selectedAmount) > 0),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (currentStep == 1) "Continue" else "Top up")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AmountSelectionCard(
    amount: Double,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "£${amount.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CustomAmountInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "£", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
