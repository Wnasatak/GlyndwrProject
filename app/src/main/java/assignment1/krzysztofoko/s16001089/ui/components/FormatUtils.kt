package assignment1.krzysztofoko.s16001089.ui.components

import java.util.Locale

/**
 * Shared Price Formatter
 */
fun formatPrice(price: Double): String = String.format(Locale.US, "%.2f", price)

/**
 * Utility to format asset paths for Coil and other loaders.
 * Corrects database path mismatches (e.g., 'images/books/' -> 'Books/').
 */
fun formatAssetUrl(url: String): String {
    if (url.isEmpty()) return ""

    var cleanUrl = url
    // Normalize path by stripping asset prefix if present
    if (cleanUrl.startsWith("file:///android_asset/")) {
        cleanUrl = cleanUrl.substring("file:///android_asset/".length)
    } else if (cleanUrl.startsWith("file://") || cleanUrl.startsWith("http") || cleanUrl.startsWith("content://")) {
        return cleanUrl
    }

    // Fix specific folder naming mismatches
    if (cleanUrl.startsWith("images/books/", ignoreCase = true)) {
        cleanUrl = "Books/" + cleanUrl.substring("images/books/".length)
    } else if (cleanUrl.startsWith("books/", ignoreCase = true)) {
        cleanUrl = "Books/" + cleanUrl.substring("books/".length)
    }

    return "file:///android_asset/$cleanUrl"
}
