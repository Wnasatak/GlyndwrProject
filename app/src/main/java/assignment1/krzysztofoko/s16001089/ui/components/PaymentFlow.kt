package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFlowDialog(
    book: Book,
    user: UserLocal?,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onComplete: (Double, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedPlanIndex by remember { mutableIntStateOf(0) }
    var fullName by remember { mutableStateOf(user?.name ?: "") }
    var isProcessing by remember { mutableStateOf(false) }
    
    val initialBalance = user?.balance ?: 0.0
    val basePrice = if (book.mainCategory == AppConstants.CAT_COURSES && selectedPlanIndex == 1) book.modulePrice else book.price
    val finalPrice = basePrice * 0.9
    
    // Explicit type for currentPaymentMethod to resolve inference error
    var currentPaymentMethod: String by remember { 
        mutableStateOf(if (initialBalance >= finalPrice) AppConstants.METHOD_UNIVERSITY_ACCOUNT else AppConstants.METHOD_PAYPAL) 
    }
    var useWalletBalance by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    val userBalance = user?.balance ?: 0.0
    val isBalanceInsufficient = userBalance < finalPrice

    val amountFromWallet: Double = remember(currentPaymentMethod, useWalletBalance, userBalance, finalPrice) {
        if (currentPaymentMethod == AppConstants.METHOD_UNIVERSITY_ACCOUNT) {
            if (userBalance >= finalPrice) finalPrice else userBalance
        } else if (useWalletBalance) {
            minOf(userBalance, finalPrice)
        } else {
            0.0
        }
    }
    
    val amountToPayExternal = (finalPrice - amountFromWallet).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (step) {
                            1 -> AppConstants.TITLE_ORDER_REVIEW
                            2 -> AppConstants.TITLE_BILLING_INFO
                            3 -> AppConstants.TITLE_PAYMENT
                            else -> AppConstants.TITLE_PROCESSING
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step $step of 3",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                LinearProgressIndicator(
                    progress = step / 3f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                )

                Box(modifier = Modifier.wrapContentHeight()) {
                    AnimatedContent(targetState = step, label = "orderStep") { currentStep ->
                        when (currentStep) {
                            1 -> Step1Review(book, book.mainCategory == AppConstants.CAT_COURSES, selectedPlanIndex, basePrice, finalPrice) { selectedPlanIndex = it }
                            2 -> Step2Billing(fullName, user?.email ?: "") { fullName = it }
                            3 -> Step3Payment(
                                user = user,
                                currentMethod = currentPaymentMethod,
                                finalPrice = finalPrice,
                                useWalletBalance = useWalletBalance,
                                amountFromWallet = amountFromWallet,
                                amountToPayExternal = amountToPayExternal,
                                onMethodChange = { currentPaymentMethod = it },
                                onToggleWallet = { useWalletBalance = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        ) { Text(AppConstants.BTN_BACK) }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        ) { Text(AppConstants.BTN_CANCEL) }
                    }

                    val canProceed = if (step == 3) {
                        if (currentPaymentMethod == AppConstants.METHOD_UNIVERSITY_ACCOUNT) !isBalanceInsufficient else true
                    } else true

                    Button(
                        onClick = {
                            if (step < 3) {
                                step++
                            } else {
                                isProcessing = true
                                scope.launch {
                                    delay(1500)
                                    val purchaseId = UUID.randomUUID().toString()
                                    val orderConf = OrderUtils.generateOrderReference()
                                    val invoiceNum = OrderUtils.generateInvoiceNumber()
                                    
                                    if (amountFromWallet > 0) {
                                        val updatedUser = user?.copy(balance = userBalance - amountFromWallet)
                                        if (updatedUser != null) db.userDao().upsertUser(updatedUser)
                                    }
                                    
                                    db.userDao().addPurchase(PurchaseItem(
                                        purchaseId = purchaseId,
                                        userId = user?.id ?: "",
                                        productId = book.id,
                                        mainCategory = book.mainCategory,
                                        purchasedAt = System.currentTimeMillis(),
                                        paymentMethod = currentPaymentMethod,
                                        amountFromWallet = amountFromWallet,
                                        amountPaidExternal = amountToPayExternal,
                                        totalPricePaid = finalPrice,
                                        quantity = 1,
                                        orderConfirmation = orderConf
                                    ))

                                    db.userDao().addInvoice(Invoice(
                                        invoiceNumber = invoiceNum,
                                        userId = user?.id ?: "",
                                        productId = book.id,
                                        itemTitle = book.title,
                                        itemCategory = book.mainCategory,
                                        itemVariant = null,
                                        pricePaid = finalPrice,
                                        discountApplied = basePrice * 0.1,
                                        quantity = 1,
                                        purchasedAt = System.currentTimeMillis(),
                                        paymentMethod = currentPaymentMethod,
                                        orderReference = orderConf,
                                        billingName = fullName,
                                        billingEmail = user?.email ?: "",
                                        billingAddress = user?.address
                                    ))

                                    db.userDao().addWalletTransaction(WalletTransaction(
                                        id = UUID.randomUUID().toString(),
                                        userId = user?.id ?: "",
                                        type = "PURCHASE",
                                        amount = finalPrice,
                                        paymentMethod = currentPaymentMethod,
                                        description = "Purchase: ${book.title}",
                                        orderReference = orderConf,
                                        productId = book.id
                                    ))

                                    // Check if it's NOT a course before adding a purchase notification here.
                                    // Courses handle their own enrollment confirmation in CourseDetailViewModel.
                                    if (book.mainCategory != AppConstants.CAT_COURSES) {
                                        val notificationTitle = when (book.mainCategory) {
                                            AppConstants.CAT_BOOKS -> AppConstants.NOTIF_TITLE_BOOK_PURCHASED
                                            AppConstants.CAT_AUDIOBOOKS -> AppConstants.NOTIF_TITLE_AUDIOBOOK_PURCHASED
                                            AppConstants.CAT_GEAR -> AppConstants.NOTIF_TITLE_PRODUCT_PURCHASED
                                            else -> AppConstants.NOTIF_TITLE_PRODUCT_PURCHASED
                                        }

                                        db.userDao().addNotification(NotificationLocal(
                                            id = UUID.randomUUID().toString(),
                                            userId = user?.id ?: "",
                                            productId = book.id,
                                            title = notificationTitle,
                                            message = "Your order for '${book.title}' has been confirmed. Ref: $orderConf",
                                            timestamp = System.currentTimeMillis(),
                                            isRead = false,
                                            type = "PURCHASE"
                                        ))
                                    }

                                    onComplete(finalPrice, orderConf)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing && canProceed
                    ) {
                        if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        else Text(if (step == 3) AppConstants.BTN_PAY_NOW else AppConstants.BTN_CONTINUE)
                    }
                }
            }
        }
    }
}

@Composable
fun Step1Review(book: Book, isCourse: Boolean, selectedPlanIndex: Int, basePrice: Double, finalPrice: Double, onPlanSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isCourse) Icons.Default.School else Icons.Default.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (selectedPlanIndex == 1) AppConstants.LABEL_MODULE_ACCESS else AppConstants.LABEL_FULL_ACCESS, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (isCourse && book.isInstallmentAvailable) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(AppConstants.TITLE_PAYMENT_PLAN, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(selected = selectedPlanIndex == 0, onClick = { onPlanSelect(0) }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text(AppConstants.LABEL_FULL_COURSE) }
                SegmentedButton(selected = selectedPlanIndex == 1, onClick = { onPlanSelect(1) }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text(AppConstants.LABEL_INSTALLMENT) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        DetailRow(AppConstants.LABEL_PRICE, "£" + String.format(Locale.US, "%.2f", basePrice))
        DetailRow(AppConstants.LABEL_STUDENT_DISCOUNT_VAL, "-£" + String.format(Locale.US, "%.2f", basePrice * 0.1))
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        DetailRow(AppConstants.LABEL_TOTAL_AMOUNT, "£" + String.format(Locale.US, "%.2f", finalPrice), isTotal = true)
    }
}

@Composable
fun Step2Billing(fullName: String, email: String, onNameChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(AppConstants.TITLE_CONFIRMATION_DETAILS, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = fullName, onValueChange = onNameChange, label = { Text(AppConstants.LABEL_FULL_NAME) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = {}, label = { Text(AppConstants.TEXT_EMAIL) }, modifier = Modifier.fillMaxWidth(), enabled = false, shape = RoundedCornerShape(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3Payment(
    user: UserLocal?,
    currentMethod: String,
    finalPrice: Double,
    useWalletBalance: Boolean,
    amountFromWallet: Double,
    amountToPayExternal: Double,
    onMethodChange: (String) -> Unit,
    onToggleWallet: (Boolean) -> Unit
) {
    val balance = user?.balance ?: 0.0
    val isInsufficientForWallet = balance < finalPrice
    val methods = listOf(AppConstants.METHOD_UNIVERSITY_ACCOUNT, AppConstants.METHOD_PAYPAL, AppConstants.METHOD_GOOGLE_PAY, AppConstants.METHOD_CREDIT_CARD)
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (currentMethod == AppConstants.METHOD_UNIVERSITY_ACCOUNT && isInsufficientForWallet) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        AppConstants.MSG_INSUFFICIENT_FUNDS,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Text(AppConstants.TITLE_PAYMENT, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentPaymentMethodDisplayName(currentMethod, balance),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                leadingIcon = {
                    Icon(
                        imageVector = getPaymentMethodIcon(currentMethod),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                methods.forEach { method ->
                    val isAcc = method == AppConstants.METHOD_UNIVERSITY_ACCOUNT
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(method, fontWeight = FontWeight.Bold)
                                if (isAcc) {
                                    Text(
                                        text = "${AppConstants.LABEL_BALANCE}: £" + String.format(Locale.US, "%.2f", balance),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isInsufficientForWallet) MaterialTheme.colorScheme.error else Color.Gray
                                    )
                                }
                            }
                        },
                        onClick = {
                            onMethodChange(method)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(getPaymentMethodIcon(method), contentDescription = null)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        AnimatedVisibility(visible = currentMethod != AppConstants.METHOD_UNIVERSITY_ACCOUNT && balance > 0) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleWallet(!useWalletBalance) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (useWalletBalance) MaterialTheme.colorScheme.secondary else Color.Transparent)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = useWalletBalance, onCheckedChange = { onToggleWallet(it) })
                        Spacer(Modifier.width(8.dp))
                        Column {
                            val potentialUsage = minOf(balance, finalPrice)
                            Text(text = "Apply wallet balance (£" + String.format(Locale.US, "%.2f", potentialUsage) + ")", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Pay only the remaining balance via $currentMethod.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(AppConstants.LABEL_ORDER_TOTAL, "£" + String.format(Locale.US, "%.2f", finalPrice))
                if (amountFromWallet > 0 && currentMethod != AppConstants.METHOD_UNIVERSITY_ACCOUNT) {
                    DetailRow(AppConstants.LABEL_WALLET_USAGE, "-£" + String.format(Locale.US, "%.2f", amountFromWallet))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                
                val displayAmount = if (currentMethod == AppConstants.METHOD_UNIVERSITY_ACCOUNT) amountFromWallet else amountToPayExternal
                
                DetailRow(
                    label = if (currentMethod == AppConstants.METHOD_UNIVERSITY_ACCOUNT) "${AppConstants.LABEL_FINAL} (${AppConstants.METHOD_UNIVERSITY_ACCOUNT})" else "${AppConstants.LABEL_FINAL} ($currentMethod)",
                    value = "£" + String.format(Locale.US, "%.2f", displayAmount),
                    isTotal = true
                )
            }
        }
    }
}

fun currentPaymentMethodDisplayName(method: String, balance: Double): String {
    return if (method == AppConstants.METHOD_UNIVERSITY_ACCOUNT) {
        "$method (£" + String.format(Locale.US, "%.2f", balance) + ")"
    } else {
        method
    }
}

fun getPaymentMethodIcon(method: String): ImageVector {
    return when (method) {
        AppConstants.METHOD_UNIVERSITY_ACCOUNT -> Icons.Default.AccountBalance
        AppConstants.METHOD_PAYPAL -> Icons.Default.Payment
        AppConstants.METHOD_GOOGLE_PAY -> Icons.Default.Smartphone
        else -> Icons.Default.CreditCard
    }
}

@Composable
fun DetailRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            modifier = Modifier.weight(1f),
            style = if(isTotal) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium, 
            fontWeight = if (isTotal) FontWeight.ExtraBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value, 
            style = if(isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, 
            fontWeight = if (isTotal) FontWeight.Black else FontWeight.Normal, 
            color = if (isTotal) MaterialTheme.colorScheme.primary else Color.Unspecified,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
