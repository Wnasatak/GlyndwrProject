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
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.LOCAL_USER_ID
import assignment1.krzysztofoko.s16001089.data.PurchaseItem
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.generateAndSaveInvoicePdf
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    book: Book,
    userName: String,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val currentDate = sdf.format(Date())
    val invoiceId = "INV-${System.currentTimeMillis().toString().takeLast(6)}"

    var purchaseRecord by remember { mutableStateOf<PurchaseItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(book.id) {
        purchaseRecord = db.userDao().getPurchaseRecord(LOCAL_USER_ID, book.id)
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
                    title = { Text("Official Invoice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = AppConstants.APP_NAME, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Official University Store", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                }
                                AsyncImage(
                                    model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                    contentDescription = "University Logo",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .graphicsLayer { rotationZ = rotation.value }
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1.1f)) {
                                    Text("ISSUED TO", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(userName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Student ID: ${AppConstants.STUDENT_ID}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.9f)) {
                                    Text("INVOICE NO", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(invoiceId, fontWeight = FontWeight.Bold)
                                    Text(currentDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.End)
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            Text("PURCHASED ITEM", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Use actual prices from record if available
                            val actualTotal = purchaseRecord?.let { it.amountFromWallet + it.amountPaidExternal } ?: (book.price * 0.9)
                            val actualBase = actualTotal / 0.9
                            val calculatedDiscount = actualBase * 0.1

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${book.category} • ${if (book.isAudioBook) "Digital" else "Hardcopy"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text("£${String.format(Locale.US, "%.2f", actualBase)}", fontWeight = FontWeight.Medium)
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(16.dp)) {
                                InvoiceSummaryRow("Subtotal", "£${String.format(Locale.US, "%.2f", actualBase)}")
                                InvoiceSummaryRow("Student Discount (10%)", "-£${String.format(Locale.US, "%.2f", calculatedDiscount)}", color = Color(0xFF2E7D32))
                                
                                purchaseRecord?.let { record ->
                                    // Only show as deduction if it was a split payment
                                    if (record.amountFromWallet > 0 && record.amountPaidExternal > 0) {
                                        InvoiceSummaryRow("Account Balance Applied", "-£${String.format(Locale.US, "%.2f", record.amountFromWallet)}", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                
                                val finalChargeLabel: String
                                val finalAmount: Double

                                if (purchaseRecord != null) {
                                    if (purchaseRecord!!.amountFromWallet > 0 && purchaseRecord!!.amountPaidExternal > 0) {
                                        finalChargeLabel = "Paid via ${purchaseRecord!!.paymentMethod}"
                                        finalAmount = purchaseRecord!!.amountPaidExternal
                                    } else if (purchaseRecord!!.paymentMethod == "University Account") {
                                        finalChargeLabel = "Paid via University Account"
                                        finalAmount = purchaseRecord!!.amountFromWallet
                                    } else {
                                        finalChargeLabel = "Paid via ${purchaseRecord!!.paymentMethod}"
                                        finalAmount = purchaseRecord!!.amountPaidExternal
                                    }
                                } else {
                                    finalChargeLabel = "Total Paid"
                                    finalAmount = actualTotal
                                }
                                
                                InvoiceSummaryRow(finalChargeLabel, "£${String.format(Locale.US, "%.2f", finalAmount)}", isTotal = true)
                            }
                            
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            Text(
                                text = "Thank you for supporting our university store!\nThis is an official computer-generated document.",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            generateAndSaveInvoicePdf(context, book, userName, invoiceId, currentDate, purchaseRecord)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Download PDF Invoice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceSummaryRow(label: String, value: String, isTotal: Boolean = false, color: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
