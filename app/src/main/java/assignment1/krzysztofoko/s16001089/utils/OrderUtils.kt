package assignment1.krzysztofoko.s16001089.utils

import java.util.*

/**
 * Centralized utility for generating order references and invoice numbers.
 */
object OrderUtils {

    /**
     * Generates a standard Wrexham order confirmation reference.
     */
    fun generateOrderReference(): String {
        return "WREX-${UUID.randomUUID().toString().take(8).uppercase()}"
    }

    /**
     * Generates a unique invoice number.
     */
    fun generateInvoiceNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        val randomSuffix = UUID.randomUUID().toString().take(4).uppercase()
        return "INV-$timestamp-$randomSuffix"
    }

    /**
     * Generates a longer order number for purchased items.
     */
    fun generateDetailedOrderNumber(): String {
        val datePart = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomPart = (100000..999999).random()
        return "ORD-$datePart-$randomPart"
    }
}
