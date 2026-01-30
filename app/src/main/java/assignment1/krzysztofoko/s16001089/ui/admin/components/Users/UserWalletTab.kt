package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import assignment1.krzysztofoko.s16001089.data.WalletTransaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserWalletTab(transactions: List<WalletTransaction>) {
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
                Text("No wallet activity recorded.", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderDetails("Transaction Ledger (${transactions.size})")
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
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Container
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
                            Text(
                                text = "${sdfDate.format(Date(tx.timestamp))} • ${sdfTime.format(Date(tx.timestamp))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val prefix = if (isTopUp) "+" else "-"
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

    // Transaction Detail Popup
    if (selectedTransaction != null) {
        val tx = selectedTransaction!!
        val isTopUp = tx.type == "TOP_UP"
        val statusColor = if (isTopUp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

        AlertDialog(
            onDismissRequest = { selectedTransaction = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isTopUp) Icons.Default.TrendingUp else Icons.Default.Receipt,
                        null,
                        tint = statusColor
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Transaction Detail", fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isTopUp) "DEPOSIT" else "PURCHASE",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (isTopUp) "+" else "-"}£${String.format(Locale.US, "%.2f", tx.amount)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Metadata
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionMetadataRow("Date", sdfDate.format(Date(tx.timestamp)))
                        TransactionMetadataRow("Time", sdfTime.format(Date(tx.timestamp)))
                        TransactionMetadataRow("Method", tx.paymentMethod)
                        if (!tx.orderReference.isNullOrBlank()) {
                            TransactionMetadataRow("Reference", tx.orderReference!!)
                        }
                        if (!tx.productId.isNullOrBlank() && tx.productId != "TOPUP") {
                            TransactionMetadataRow("Product ID", tx.productId!!)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTransaction = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun TransactionMetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}
