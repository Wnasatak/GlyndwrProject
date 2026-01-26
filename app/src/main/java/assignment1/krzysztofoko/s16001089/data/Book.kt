package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Core Book entity representing products in the 'books' table.
 * The constructor contains all columns expected by the database schema to prevent crashes.
 */
@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val price: Double = 0.0,
    val isAudioBook: Boolean = false,
    val audioUrl: String = "",
    val pdfUrl: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "General",
    val mainCategory: String = "Books",
    val isInstallmentAvailable: Boolean = false,
    val modulePrice: Double = 0.0
) {
    // transient field for order confirmation, fetched from 'purchases' table at runtime
    @Ignore
    var orderConfirmation: String? = null
}
