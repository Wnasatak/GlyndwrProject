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
 * Provides a breakdown of costs, discounts, and payment methods.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    book: Book,                       // The product associated with the invoice
    userName: String,                 // User display name for the receipt
    onBack: () -> Unit,               // Handler to return to previous view
    isDarkTheme: Boolean,             // Visual state flag
    onToggleTheme: () -> Unit,        // Handler for theme switching
    orderRef: String? = null          // Optional specific reference ID for database lookup
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    // Formatting for the purchase date and time
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    // UI state for the fetched invoice record
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch the invoice details from the local database based on available IDs
    LaunchedEffect(book.id, orderRef) {
        invoice = if (orderRef != null) {
            db.userDao().getInvoiceByReference(userId, orderRef)
        } else {
            db.userDao().getInvoiceForProduct(userId, book.id)
        }
        isLoading = false
    }

    // Animation for the institutional logo
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic animated themed background
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Allow background to show through
            topBar = {
                // Toolbar with title and theme control
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = AppConstants.TITLE_OFFICIAL_INVOICE, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.ExtraBold
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
            // --- DATA CONDITIONAL RENDERING ---
            if (isLoading) {
                // Loading state
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (invoice == null) {
                // Fallback for missing records
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
                
                // Adaptive container ensures content remains centered and correctly sized on tablets
                AdaptiveScreenContainer(
                    modifier = Modifier.padding(top = padding.calculateTopPadding()).verticalScroll(rememberScrollState()),
                    maxWidth = AdaptiveWidths.Standard
                ) { isTablet ->
                    Column(
                        modifier = Modifier.padding(AdaptiveSpacing.contentPadding()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- THE MAIN INVOICE CARD ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(if (isTablet) 32.dp else 24.dp)) {
                                // Header: Branding and Logo
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        @Suppress("DEPRECATION")
                                        Text(text = AppConstants.APP_NAME, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                        @Suppress("DEPRECATION")
                                        Text(text = AppConstants.LABEL_STORE_TAGLINE, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    AdaptiveBrandedLogo(
                                        model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                        contentDescription = "Logo",
                                        logoSize = if (isTablet) 60.dp else 52.dp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // User and Technical Metadata
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        @Suppress("DEPRECATION")
                                        Text(AppConstants.TITLE_ISSUED_TO, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(currentInvoice.billingName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                                        Text("${AppConstants.TEXT_EMAIL}: ${currentInvoice.billingEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (!currentInvoice.billingAddress.isNullOrBlank()) {
                                            Text(currentInvoice.billingAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                        @Suppress("DEPRECATION")
                                        Text(AppConstants.TITLE_INVOICE_NO, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(text = currentInvoice.invoiceNumber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                                    }
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                
                                // Detailed Item Information
                                @Suppress("DEPRECATION")
                                Text(AppConstants.TITLE_PURCHASED_ITEM, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val pricePaid = currentInvoice.pricePaid
                                val basePrice = if (pricePaid > 0) pricePaid + currentInvoice.discountApplied else 0.0
                                val discount = currentInvoice.discountApplied

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Item Visual Category Icon
                                    Surface(modifier = Modifier.size(50.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
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
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(currentInvoice.itemTitle, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                                        var categoryText = currentInvoice.itemCategory
                                        if (!currentInvoice.itemVariant.isNullOrEmpty()) { categoryText = "$categoryText • ${currentInvoice.itemVariant}" }
                                        Text(categoryText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    // Original price before potential student discounts
                                    Text("£" + String.format(Locale.US, "%.2f", basePrice), fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // --- FINANCIAL SUMMARY ---
                                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(20.dp)) {
                                    InvoiceSummaryRow(AppConstants.LABEL_SUBTOTAL, "£" + String.format(Locale.US, "%.2f", basePrice))
                                    // Highlight student discount in green if applied
                                    if (discount > 0) { 
                                        Spacer(Modifier.height(8.dp))
                                        InvoiceSummaryRow(AppConstants.LABEL_STUDENT_DISCOUNT_VAL, "-£" + String.format(Locale.US, "%.2f", discount), color = Color(0xFF4CAF50)) 
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    // Final amount charged
                                    InvoiceSummaryRow(AppConstants.LABEL_TOTAL_PAID, "£" + String.format(Locale.US, "%.2f", pricePaid), isTotal = true)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    @Suppress("DEPRECATION")
                                    Text(text = "${AppConstants.LABEL_PAID_VIA} ${currentInvoice.paymentMethod}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    if (!currentInvoice.orderReference.isNullOrEmpty()) {
                                        Text(text = "${AppConstants.LABEL_REFERENCE}: ${currentInvoice.orderReference}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.padding(top = 2.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(40.dp))
                                // Receipt validation text
                                @Suppress("DEPRECATION")
                                Text(text = "${AppConstants.MSG_THANK_YOU_STORE}\n${AppConstants.LABEL_COMPUTER_GENERATED}", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.fillMaxWidth(), lineHeight = 16.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Action: Generates a physical PDF file from the UI data for external use
                        Button(
                            onClick = { 
                                val pRecord = PurchaseItem(purchaseId = UUID.randomUUID().toString(), userId = currentInvoice.userId, productId = currentInvoice.productId, mainCategory = currentInvoice.itemCategory, purchasedAt = currentInvoice.purchasedAt, paymentMethod = currentInvoice.paymentMethod, amountFromWallet = currentInvoice.pricePaid, amountPaidExternal = 0.0, totalPricePaid = currentInvoice.pricePaid, quantity = currentInvoice.quantity, orderConfirmation = currentInvoice.orderReference)
                                generateAndSaveInvoicePdf(context, book, currentInvoice.billingName, currentInvoice.invoiceNumber, formattedDate, pRecord)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(Modifier.width(12.dp))
                            @Suppress("DEPRECATION")
                            Text(AppConstants.BTN_DOWNLOAD_PDF, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Reusable Row Component for formatted key-value pairs in the financial summary.
 */
@Composable
fun InvoiceSummaryRow(
    label: String,                    // Text description (e.g., Subtotal)
    value: String,                    // Formatted currency value
    isTotal: Boolean = false,         // Flag to apply larger bold styling
    color: Color = Color.Unspecified  // Optional color override (e.g., green for discounts)
) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceBetween, 
        verticalAlignment = Alignment.CenterVertically
    ) {
        @Suppress("DEPRECATION")
        Text(
            text = label, 
            modifier = Modifier.weight(1f),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, 
            fontWeight = if (isTotal) FontWeight.Black else FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isTotal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        @Suppress("DEPRECATION")
        Text(
            text = value, 
            style = if (isTotal) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge, 
            fontWeight = if (isTotal) FontWeight.Black else FontWeight.Bold, 
            color = if (isTotal) MaterialTheme.colorScheme.primary else color,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
