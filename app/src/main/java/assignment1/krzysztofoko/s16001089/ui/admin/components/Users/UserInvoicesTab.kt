package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Invoice
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserInvoicesTab(invoices: List<Invoice>) {
    var selectedInvoice by remember { mutableStateOf<Invoice?>(null) }
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    if (invoices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.AutoMirrored.Filled.ReceiptLong, 
                    null, 
                    modifier = Modifier.size(64.dp), 
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text("No purchase history found.", color = Color.Gray) 
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp), 
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderDetails("University Receipts (${invoices.size})")
            }
            
            items(invoices) { inv ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedInvoice = inv }, 
                    shape = RoundedCornerShape(16.dp), 
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    ListItem(
                        leadingContent = {
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
                            Text("Inv: ${inv.invoiceNumber} • ${sdf.format(Date(inv.purchasedAt))}") 
                        },
                        trailingContent = { 
                            Text(
                                "£${String.format(Locale.US, "%.2f", inv.pricePaid)}", 
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Invoice Detail Popup
    if (selectedInvoice != null) {
        val inv = selectedInvoice!!
        AlertDialog(
            onDismissRequest = { selectedInvoice = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Official Receipt", fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Billing Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("ISSUED TO", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(inv.billingName, fontWeight = FontWeight.Bold)
                        Text(inv.billingEmail, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        if (!inv.billingAddress.isNullOrBlank()) {
                            Text(inv.billingAddress!!, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    // Item Details
                    Column {
                        Text("PURCHASED ITEM", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(inv.itemTitle, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        Text("${inv.itemCategory} ${if (inv.itemVariant != null) "• ${inv.itemVariant}" else ""}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Totals
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Invoice Number", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(inv.invoiceNumber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(inv.paymentMethod, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        if (!inv.orderReference.isNullOrBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Order Reference", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(inv.orderReference!!, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL PAID", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text("£${String.format(Locale.US, "%.2f", inv.pricePaid)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Text(
                        text = "Transaction Date: ${sdf.format(Date(inv.purchasedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedInvoice = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Receipt")
                }
            }
        )
    }
}
