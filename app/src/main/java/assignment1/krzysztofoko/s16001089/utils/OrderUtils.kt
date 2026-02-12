package assignment1.krzysztofoko.s16001089.utils

import java.util.*

/**
 * OrderUtils is a centralized utility provider for the application's transaction system.
 * It ensures that every order, invoice, and reservation has a globally unique,
 * trackable, and formatted identification string suitable for institutional auditing.
 */
object OrderUtils {

    /**
     * Generates a standard institutional order confirmation reference.
     * Uses the "WREX-" prefix followed by a truncated UUID for a balance between
     * uniqueness and readability in user interfaces.
     *
     * Example: WREX-A1B2C3D4
     *
     * @return A unique 8-character alphanumeric reference string.
     */
    fun generateOrderReference(): String {
        return "WREX-${UUID.randomUUID().toString().take(8).uppercase()}"
    }

    /**
     * Generates a unique invoice number for financial records.
     * Combines a timestamp suffix for temporal tracking with a random alphanumeric
     * suffix to prevent collisions during high-volume periods.
     *
     * Example: INV-123456-F7G8
     *
     * @return A formatted invoice string starting with 'INV-'.
     */
    fun generateInvoiceNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        val randomSuffix = UUID.randomUUID().toString().take(4).uppercase()
        return "INV-$timestamp-$randomSuffix"
    }

    /**
     * Generates a high-precision detailed order number specifically for purchased assets.
     * This format embeds the current date (YYYYMMDD), making it highly efficient
     * for database indexing and customer support lookups.
     *
     * Example: ORD-20231027-456789
     *
     * @return A long-format order number with date-based sorting capability.
     */
    fun generateDetailedOrderNumber(): String {
        val datePart = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomPart = (100000..999999).random()
        return "ORD-$datePart-$randomPart"
    }
}
