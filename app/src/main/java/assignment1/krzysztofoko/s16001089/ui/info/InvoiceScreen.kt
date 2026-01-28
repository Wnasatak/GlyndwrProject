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
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.generateAndSaveInvoicePdf
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen displaying a detailed digital receipt/invoice for a specific purchase.
 *
 * This screen fetches structured invoice data from the local Room database using the
 * user ID and product ID. It presents a high-fidelity visual breakdown of the transaction,
 * including sub-totals, discounts, billing info, and university branding.
 *
 * It also features a "Download PDF" button that generates a native Android PDF document
 * by reconstructing the transaction data into a format required by the PDF generator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    book: Book,                       // The core product object containing metadata like title and price
    userName: String,                 // The current authenticated user's name for display
    onBack: () -> Unit,               // Callback function to navigate back to the previous screen
    isDarkTheme: Boolean,             // State flag indicating if Dark Mode is active
    onToggleTheme: () -> Unit         // Function to flip the app's global theme state
) {
    // Standard Context and Database handle for Room interactions
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    // SimpleDateFormat used to convert Long timestamps (milliseconds) into human-readable strings
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    // UI state to hold the invoice record fetched from the DB
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    /**
     * Effect Hook: Fetches the invoice record whenever this screen is initialized.
     * Searches the 'invoices' table for a record matching the current user and product.
     */
    LaunchedEffect(book.id) {
        invoice = db.userDao().getInvoiceForProduct(userId, book.id)
        isLoading = false
    }

    /**
     * Animation State: Controls the initial rotation of the University Logo.
     * Uses Animatable to smoothly transition from 0 to 360 degrees.
     */
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Root layout using Box to allow layering the wavy background behind the Scaffold
    Box(modifier = Modifier.fillMaxSize()) {
        
        // Reusable animated wavy background component (handles dark/light mode automatically)
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Makes Scaffold transparent so the background shows through
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0), // Resets default system window insets
                    title = { 
                        Text(
                            text = AppConstants.TITLE_OFFICIAL_INVOICE, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return to collection")
                        }
                    },
                    actions = {
                        // Quick theme switcher located in the top bar
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Change appearance"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Semi-transparency for glass effect
                    )
                )
            }
        ) { padding ->
            // Conditional UI Rendering based on Loading/Found/Not Found states
            if (isLoading) {
                // Centered Loading Spinner
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (invoice == null) {
                /**
                 * Error State: Rendered if no invoice matches the product ID.
                 * Usually happens for 'Free' items that don't generate financial records.
                 */
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(AppConstants.MSG_INVOICE_NOT_FOUND)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                // Data Success State: Unwrap the invoice record
                val currentInvoice = invoice!!
                val formattedDate = sdf.format(Date(currentInvoice.purchasedAt))
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()) // Enables scrolling for small screens
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /**
                     * The Main Invoice 'Paper' Card.
                     * Mimics a physical printed invoice with borders and branding.
                     */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            
                            // Header Section: Store Branding and Logo
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = AppConstants.APP_NAME, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    Text(text = AppConstants.LABEL_STORE_TAGLINE, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                }
                                AsyncImage(
                                    model = "file:///android_asset/images/media/GlyndwrUniversity.jpg", // Load institution logo from local assets
                                    contentDescription = "Official Logo",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .graphicsLayer { rotationZ = rotation.value } // Apply the rotation animation
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Client vs Metadata Section (Names, Emails, Dates)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                // Left Column: Billing Recipient info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(AppConstants.TITLE_ISSUED_TO, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(currentInvoice.billingName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${AppConstants.TEXT_EMAIL}: ${currentInvoice.billingEmail}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    if (!currentInvoice.billingAddress.isNullOrBlank()) {
                                        Text(currentInvoice.billingAddress, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                                // Right Column: Transaction metadata (ID and Date)
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                    Text(AppConstants.TITLE_INVOICE_NO, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = currentInvoice.invoiceNumber, 
                                        style = MaterialTheme.typography.bodySmall, 
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.End)
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            // Purchase Details: Item category icon and title
                            Text(AppConstants.TITLE_PURCHASED_ITEM, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Re-calculate pricing components for the summary rows
                            val pricePaid = currentInvoice.pricePaid
                            val basePrice = if (pricePaid > 0) pricePaid + currentInvoice.discountApplied else 0.0
                            val discount = currentInvoice.discountApplied

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Background container for the category icon
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
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
                                
                                // Product Title and Description
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(currentInvoice.itemTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    var categoryText = currentInvoice.itemCategory
                                    // Append variant info (e.g. Size/Color) if it exists
                                    if (!currentInvoice.itemVariant.isNullOrEmpty()) {
                                        categoryText = "$categoryText • ${currentInvoice.itemVariant}"
                                    }
                                    Text(categoryText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                
                                // Price display formatted to 2 decimal places
                                Text("£" + String.format(Locale.US, "%.2f", basePrice), fontWeight = FontWeight.Medium)
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // FINANCIAL SUMMARY: Breakdown of Subtotal, Discounts, and Final Total
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                // Initial price before any student discounts
                                InvoiceSummaryRow(AppConstants.LABEL_SUBTOTAL, "£" + String.format(Locale.US, "%.2f", basePrice))
                                
                                // Only show discount row if a student discount was actually applied
                                if (discount > 0) {
                                    InvoiceSummaryRow(AppConstants.LABEL_STUDENT_DISCOUNT_VAL, "-£" + String.format(Locale.US, "%.2f", discount), color = Color(0xFF2E7D32))
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                
                                // FINAL TOTAL: The actual amount charged to the user's wallet
                                InvoiceSummaryRow(AppConstants.LABEL_TOTAL_PAID, "£" + String.format(Locale.US, "%.2f", pricePaid), isTotal = true)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Transaction footer details
                                Text(
                                    text = "${AppConstants.LABEL_PAID_VIA} ${currentInvoice.paymentMethod}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                )

                                // Official reference code for manual lookup or support
                                if (!currentInvoice.orderReference.isNullOrEmpty()) {
                                    Text(
                                        text = "${AppConstants.LABEL_REFERENCE}: ${currentInvoice.orderReference}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            // Official Footer Disclaimer
                            Text(
                                text = "${AppConstants.MSG_THANK_YOU_STORE}\n${AppConstants.LABEL_COMPUTER_GENERATED}",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    /**
                     * ACTION: DOWNLOAD PDF
                     * This button initiates the native PDF generation process.
                     * It maps the database 'Invoice' entity back into a 'PurchaseItem' model
                     * which is required by the shared generateAndSaveInvoicePdf utility.
                     */
                    Button(
                        onClick = { 
                            // Create a temporary domain model for the PDF renderer
                            val pRecord = PurchaseItem(
                                purchaseId = UUID.randomUUID().toString(),
                                userId = currentInvoice.userId,
                                productId = currentInvoice.productId,
                                mainCategory = currentInvoice.itemCategory,
                                purchasedAt = currentInvoice.purchasedAt,
                                paymentMethod = currentInvoice.paymentMethod,
                                amountFromWallet = currentInvoice.pricePaid,
                                amountPaidExternal = 0.0,
                                totalPricePaid = currentInvoice.pricePaid,
                                quantity = currentInvoice.quantity,
                                orderConfirmation = currentInvoice.orderReference
                            )
                            // Call the utility function to draw onto a PDF Canvas and save to the 'Downloads' folder
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

/**
 * Reusable Row Component for Invoice Financials.
 * Ensures consistent spacing and typography between labels (e.g. 'Subtotal') and their values.
 */
@Composable
fun InvoiceSummaryRow(
    label: String,           // Row description (e.g. 'Student Discount')
    value: String,           // Value string (e.g. '-£2.50')
    isTotal: Boolean = false, // If true, applies bold and larger font for the grand total
    color: Color = Color.Unspecified // Optional custom color (used for green discount text)
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
