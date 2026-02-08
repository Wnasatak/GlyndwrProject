package assignment1.krzysztofoko.s16001089.ui.info

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen displaying a detailed digital receipt/invoice for a specific purchase.
 * Optimized with AdaptiveScreenContainer for better presentation on tablets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    book: Book,                       
    userName: String,                 
    onBack: () -> Unit,               
    isDarkTheme: Boolean,             
    onToggleTheme: () -> Unit,
    orderRef: String? = null          
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(book.id, orderRef) {
        invoice = if (orderRef != null) {
            db.userDao().getInvoiceByReference(userId, orderRef)
        } else {
            db.userDao().getInvoiceForProduct(userId, book.id)
        }
        isLoading = false
    }

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = AppConstants.TITLE_OFFICIAL_INVOICE, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { padding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (invoice == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(AppConstants.MSG_INVOICE_NOT_FOUND)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                val currentInvoice = invoice!!
                val formattedDate = sdf.format(Date(currentInvoice.purchasedAt))
                
                AdaptiveScreenContainer(
                    modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
                    maxWidth = AdaptiveWidths.Standard
                ) { isTablet ->
                    Column(
                        modifier = Modifier.padding(AdaptiveSpacing.contentPadding()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = AppConstants.APP_NAME, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                        Text(text = AppConstants.LABEL_STORE_TAGLINE, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                    }
                                    AsyncImage(
                                        model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(52.dp).graphicsLayer { rotationZ = rotation.value }.clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(AppConstants.TITLE_ISSUED_TO, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(currentInvoice.billingName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${AppConstants.TEXT_EMAIL}: ${currentInvoice.billingEmail}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        if (!currentInvoice.billingAddress.isNullOrBlank()) {
                                            Text(currentInvoice.billingAddress, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                        Text(AppConstants.TITLE_INVOICE_NO, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(text = currentInvoice.invoiceNumber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.End)
                                    }
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                
                                Text(AppConstants.TITLE_PURCHASED_ITEM, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val pricePaid = currentInvoice.pricePaid
                                val basePrice = if (pricePaid > 0) pricePaid + currentInvoice.discountApplied else 0.0
                                val discount = currentInvoice.discountApplied

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when(currentInvoice.itemCategory) {
                                                AppConstants.CAT_AUDIOBOOKS -> Icons.Default.Headphones
                                                AppConstants.CAT_COURSES -> Icons.Default.School
                                                AppConstants.CAT_GEAR -> Icons.Default.Inventory
                                                AppConstants.CAT_FINANCE -> Icons.Default.AccountBalanceWallet
                                                else -> Icons.AutoMirrored.Filled.MenuBook
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(currentInvoice.itemTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        var categoryText = currentInvoice.itemCategory
                                        if (!currentInvoice.itemVariant.isNullOrEmpty()) { categoryText = "$categoryText • ${currentInvoice.itemVariant}" }
                                        Text(categoryText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Text("£" + String.format(Locale.US, "%.2f", basePrice), fontWeight = FontWeight.Medium)
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(16.dp)) {
                                    InvoiceSummaryRow(AppConstants.LABEL_SUBTOTAL, "£" + String.format(Locale.US, "%.2f", basePrice))
                                    if (discount > 0) { InvoiceSummaryRow(AppConstants.LABEL_STUDENT_DISCOUNT_VAL, "-£" + String.format(Locale.US, "%.2f", discount), color = Color(0xFF2E7D32)) }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    InvoiceSummaryRow(AppConstants.LABEL_TOTAL_PAID, "£" + String.format(Locale.US, "%.2f", pricePaid), isTotal = true)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${AppConstants.LABEL_PAID_VIA} ${currentInvoice.paymentMethod}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                                    if (!currentInvoice.orderReference.isNullOrEmpty()) {
                                        Text(text = "${AppConstants.LABEL_REFERENCE}: ${currentInvoice.orderReference}", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(40.dp))
                                Text(text = "${AppConstants.MSG_THANK_YOU_STORE}\n${AppConstants.LABEL_COMPUTER_GENERATED}", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { 
                                val pRecord = PurchaseItem(purchaseId = UUID.randomUUID().toString(), userId = currentInvoice.userId, productId = currentInvoice.productId, mainCategory = currentInvoice.itemCategory, purchasedAt = currentInvoice.purchasedAt, paymentMethod = currentInvoice.paymentMethod, amountFromWallet = currentInvoice.pricePaid, amountPaidExternal = 0.0, totalPricePaid = currentInvoice.pricePaid, quantity = currentInvoice.quantity, orderConfirmation = currentInvoice.orderReference)
                                generateAndSaveInvoicePdf(context, book, currentInvoice.billingName, currentInvoice.invoiceNumber, formattedDate, pRecord)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(Modifier.width(12.dp))
                            Text(AppConstants.BTN_DOWNLOAD_PDF, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Row Component for Invoice Financials.
 */
@Composable
fun InvoiceSummaryRow(
    label: String,
    value: String,
    isTotal: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceBetween, 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            modifier = Modifier.weight(1f),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, 
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value, 
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium, 
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, 
            color = if (isTotal) MaterialTheme.colorScheme.primary else color,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
