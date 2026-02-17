package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import assignment1.krzysztofoko.s16001089.data.WalletTransaction
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import assignment1.krzysztofoko.s16001089.ui.components.adaptiveWidth
import java.text.SimpleDateFormat
import java.util.*

/**
 * UserWalletTab provides an administrative ledger of a user's financial activity.
 */
@Composable
fun UserWalletTab(
    transactions: List<WalletTransaction>,
    isAdmin: Boolean = false
) {
    var selectedTransaction by remember { mutableStateOf<WalletTransaction?>(null) }
    val sdfDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                @Suppress("DEPRECATION")
                Text("No wallet activity recorded.", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderDetails(if (isAdmin) "Transaction Ledger (${transactions.size})" else "My Transactions (${transactions.size})")
            }

            items(transactions) { tx ->
                val isTopUp = tx.type == "TOP_UP"
                val statusColor = if (isTopUp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTransaction = tx },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(statusColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTopUp) Icons.Default.TrendingUp else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            @Suppress("DEPRECATION")
                            Text(
                                text = "${sdfDate.format(Date(tx.timestamp))} • ${sdfTime.format(Date(tx.timestamp))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val prefix = if (isTopUp) "+" else "-"
                            @Suppress("DEPRECATION")
                            Text(
                                text = "$prefix£${String.format(Locale.US, "%.2f", tx.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                            Surface(
                                color = statusColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tx.type.replace("_", " "),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // High-end Themed Transaction Detail Dialog
    if (selectedTransaction != null) {
        val tx = selectedTransaction!!
        val isTopUp = tx.type == "TOP_UP"
        val statusColor = if (isTopUp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

        Dialog(
            onDismissRequest = { selectedTransaction = null },
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
                    // HEADER section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isTopUp) Icons.Default.TrendingUp else Icons.Default.Receipt, 
                                    null, 
                                    tint = statusColor, 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        @Suppress("DEPRECATION")
                        Text("Transaction Detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }

                    Spacer(Modifier.height(24.dp))

                    // HIGH-CONTRAST SUMMARY CARD
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = statusColor.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            @Suppress("DEPRECATION")
                            Text(
                                text = if (isTopUp) "DEPOSIT" else "PURCHASE",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            @Suppress("DEPRECATION")
                            Text(
                                text = "${if (isTopUp) "+" else "-"}£${String.format(Locale.US, "%.2f", tx.amount)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // METADATA LIST
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TransactionMetadataRow("Date", sdfDate.format(Date(tx.timestamp)))
                        TransactionMetadataRow("Time", sdfTime.format(Date(tx.timestamp)))
                        TransactionMetadataRow("Payment Method", tx.paymentMethod)
                        
                        if (!tx.orderReference.isNullOrBlank()) {
                            TransactionMetadataRow("Order Reference", tx.orderReference!!)
                        }
                        if (!tx.productId.isNullOrBlank() && tx.productId != "TOPUP") {
                            TransactionMetadataRow("Product Identifier", tx.productId!!)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ACTION BUTTON
                    Button(
                        onClick = { selectedTransaction = null },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Detail", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionMetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        @Suppress("DEPRECATION")
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
        @Suppress("DEPRECATION")
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
