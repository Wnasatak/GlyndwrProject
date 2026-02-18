package assignment1.krzysztofoko.s16001089.ui.components

import java.util.Locale

/**
 * FormatUtils.kt
 *
 * A collection of utility functions designed for consistent formatting 
 * of data across the application. These functions handle common data transformation 
 * tasks such as currency formatting and resource path normalisation.
 */

/**
 * Shared Price Formatter
 *
 * Formats a [Double] value into a consistent currency string representation. 
 * This ensures that all prices displayed in the UI are uniform and easy to read.
 *
 * @param price The numerical price value to be formatted.
 * @return A [String] representing the price formatted to two decimal places (e.g., "12.99").
 */
fun formatPrice(price: Double): String = String.format(Locale.US, "%.2f", price)

/**
 * Utility to format asset paths for Coil and other loaders.
 *
 * This function normalises raw image or file paths from the database to ensure 
 * they can be correctly loaded by image libraries like Coil. It handles different 
 * URL types and corrects common path mismatches between the database and 
 * the Android assets folder.
 *
 * Transformations:
 * - Strips `file:///android_asset/` prefix if already present for normalisation.
 * - Passes through `http`, `content`, and `file://` URLs that point to external resources.
 * - Corrects folder name mismatches (e.g., mapping `images/books/` to `Books/`).
 *
 * @param url The raw path or URL string to be formatted.
 * @return A fully qualified `file:///android_asset/` URL string for local resources, 
 *         or the original string for external ones.
 */
fun formatAssetUrl(url: String): String {
    if (url.isEmpty()) return ""

    var cleanUrl = url
    // Normalise path by stripping asset prefix if present for consistent processing.
    if (cleanUrl.startsWith("file:///android_asset/")) {
        cleanUrl = cleanUrl.substring("file:///android_asset/".length)
    } else if (cleanUrl.startsWith("file://") || cleanUrl.startsWith("http") || cleanUrl.startsWith("content://")) {
        // External or system-level paths are returned as-is.
        return cleanUrl
    }

    // Fix specific folder naming mismatches between the database records and the actual asset structure.
    if (cleanUrl.startsWith("images/books/", ignoreCase = true)) {
        cleanUrl = "Books/" + cleanUrl.substring("images/books/".length)
    } else if (cleanUrl.startsWith("books/", ignoreCase = true)) {
        cleanUrl = "Books/" + cleanUrl.substring("books/".length)
    }

    // Re-apply the asset prefix to ensure it's a valid local resource URL for Coil.
    return "file:///android_asset/$cleanUrl"
}
