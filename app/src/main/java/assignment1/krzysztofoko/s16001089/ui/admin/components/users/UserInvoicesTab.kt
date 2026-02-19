package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Invoice
import assignment1.krzysztofoko.s16001089.data.PurchaseItem
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth
import assignment1.krzysztofoko.s16001089.ui.components.generateAndSaveInvoicePdf
import java.text.SimpleDateFormat
import java.util.*

/**
 * UserInvoicesTab.kt
 *
 * This administrative component provides a structured ledger of all financial transactions 
 * (purchases and top-ups) for a specific user. It allows staff to audit expenditures, 
 * verify billing details, and generate PDF replicas of official university receipts.
 *
 * Design Architecture:
 * - Reactive Ledger: Dynamically lists all invoice records associated with the user.
 * - Drill-Down Detail: Utilizes a high-end modal Dialog to provide granular transaction metadata.
 * - External Integration: Provides a bridge to the system's PDF generation utility for "Print" functionality.
 */

/**
 * Main financial audit tab for student accounts.
 *
 * @param invoices Chronological list of [Invoice] records for the user.
 * @param isAdmin Flag to customize header labels for administrative context.
 */
@Composable
fun UserInvoicesTab(
    invoices: List<Invoice>,
    isAdmin: Boolean = false
) {
    val context = LocalContext.current
    
    // --- LOCAL STATE ---
    // Tracks the specific invoice currently being viewed in the detail dialog.
    var selectedInvoice by remember { mutableStateOf<Invoice?>(null) }
    
    // Formatting utility for consistent chronological display.
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    // EMPTY STATE: Displayed when the user has no transaction history.
    if (invoices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong, 
                    contentDescription = null, 
                    modifier = Modifier.size(64.dp), 
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                @Suppress("DEPRECATION")
                Text("No purchase history found.", color = Color.Gray) 
            }
        }
    } else {
        // --- INVOICE LEDGER LIST ---
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderDetails(
                    if (isAdmin) "University Receipts (${invoices.size})" 
                    else "My Receipts (${invoices.size})"
                )
            }
            
            items(invoices) { inv ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedInvoice = inv }, 
                    shape = RoundedCornerShape(16.dp), 
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    ListItem(
                        leadingContent = {
                            // Category-based icon indicator.
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when(inv.itemCategory) {
                                        AppConstants.CAT_AUDIOBOOKS -> Icons.Default.Headphones
                                        AppConstants.CAT_COURSES -> Icons.Default.School
                                        AppConstants.CAT_GEAR -> Icons.Default.Inventory
                                        AppConstants.CAT_FINANCE -> Icons.Default.AccountBalanceWallet
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        headlineContent = { 
                            Text(inv.itemTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) 
                        },
                        supportingContent = { 
                            @Suppress("DEPRECATION")
                            Text("Inv: ${inv.invoiceNumber} • ${sdf.format(Date(inv.purchasedAt))}") 
                        },
                        trailingContent = { 
                            @Suppress("DEPRECATION")
                            Text(
                                text = "£${String.format(Locale.US, "%.2f", inv.pricePaid)}", 
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            // Ensure content isn't obscured by the bottom navigation bar.
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // --- TRANSACTION DETAIL DIALOG ---
    // Provides a full breakdown of the selected receipt including billing info and print options.
    if (selectedInvoice != null) {
        val inv = selectedInvoice!!
        Dialog(
            onDismissRequest = { selectedInvoice = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .adaptiveWidth(AdaptiveWidths.Standard),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // HEADER: Title and PDF Export Action.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text("Official Receipt", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        
                        // Action: Invoke external PDF generation utility.
                        IconButton(
                            onClick = {
                                val pRecord = PurchaseItem(
                                    purchaseId = UUID.randomUUID().toString(),
                                    userId = inv.userId,
                                    productId = inv.productId,
                                    mainCategory = inv.itemCategory,
                                    purchasedAt = inv.purchasedAt,
                                    paymentMethod = inv.paymentMethod,
                                    amountFromWallet = inv.pricePaid,
                                    amountPaidExternal = 0.0,
                                    totalPricePaid = inv.pricePaid,
                                    quantity = inv.quantity,
                                    orderConfirmation = inv.orderReference
                                )
                                generateAndSaveInvoicePdf(context, null, inv.billingName, inv.invoiceNumber, sdf.format(Date(inv.purchasedAt)), pRecord)
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Print, "Export PDF", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // SECTION: BILLING ENTITY (Issued To)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        @Suppress("DEPRECATION")
                        Text("ISSUED TO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        @Suppress("DEPRECATION")
                        Text(inv.billingName, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                        @Suppress("DEPRECATION")
                        Text(inv.billingEmail, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        if (!inv.billingAddress.isNullOrBlank()) {
                            @Suppress("DEPRECATION")
                            Text(inv.billingAddress!!, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // SECTION: ITEM SUMMARY
                    Column {
                        @Suppress("DEPRECATION")
                        Text("PURCHASED ITEM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        @Suppress("DEPRECATION")
                        Text(inv.itemTitle, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        @Suppress("DEPRECATION")
                        Text("${inv.itemCategory} ${if (inv.itemVariant != null) "• ${inv.itemVariant}" else ""}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(16.dp))

                    // SECTION: TRANSACTION METADATA
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Invoice Number", inv.invoiceNumber)
                        DetailRow("Payment Method", inv.paymentMethod)
                        if (!inv.orderReference.isNullOrBlank()) {
                            DetailRow("Order Reference", inv.orderReference!!)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // HIGHLIGHT: Total Consideration
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), 
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                @Suppress("DEPRECATION")
                                Text("TOTAL PAID", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                @Suppress("DEPRECATION")
                                Text("£${String.format(Locale.US, "%.2f", inv.pricePaid)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    @Suppress("DEPRECATION")
                    Text(
                        text = "Processed on ${sdf.format(Date(inv.purchasedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(24.dp))

                    // DISMISSAL
                    Button(
                        onClick = { selectedInvoice = null },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Receipt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Reusable layout row for displaying key-value data points in the invoice detail view.
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        @Suppress("DEPRECATION")
        Text(text = "$label:", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Medium)
        @Suppress("DEPRECATION")
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
