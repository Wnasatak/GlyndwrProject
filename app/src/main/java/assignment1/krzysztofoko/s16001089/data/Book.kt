/**
 * Book.kt
 *
 * This file defines the Book Room entity, representing a physical or digital book in the application.
 * It stores metadata, pricing information, and URLs for accessing book content (PDF or Audio).
 */

package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Core Book entity representing products in the 'books' table.
 * The constructor contains all columns expected by the database schema to prevent crashes.
 */
@Entity(tableName = "books") // Room table definition
data class Book(
    @PrimaryKey
    val id: String = "", // Unique identifier for the book
    val title: String = "", // Title of the book
    val author: String = "", // Author of the book
    val price: Double = 0.0, // Purchase price
    val isAudioBook: Boolean = false, // Flag indicating if an audio version is available
    val audioUrl: String = "", // URL for the audio stream if applicable
    val pdfUrl: String = "", // URL for the PDF file if digital
    val description: String = "", // Detailed book description
    val imageUrl: String = "", // URL for the book cover image
    val category: String = "General", // Sub-category for filtering (e.g., Fiction, Science)
    val mainCategory: String = "Books", // Top-level category (e.g., Books, Gear)
    val isInstallmentAvailable: Boolean = false, // Flag for flexible payment options
    val modulePrice: Double = 0.0 // Price per module if installments are enabled
) {
    /**
     * Transient field for order confirmation, fetched from 'purchases' table at runtime.
     * This field is not persisted in the 'books' table.
     */
    @Ignore
    var orderConfirmation: String? = null
}
