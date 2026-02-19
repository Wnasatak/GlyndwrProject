/**
 * AudioBook.kt
 *
 * This file defines the AudioBook Room entity, representing an audiobook in the application.
 * It stores metadata and URLs for streaming audiobook content.
 */

package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audiobooks") // Room table definition
data class AudioBook(
    @PrimaryKey
    val id: String = "", // Unique identifier for the audiobook
    val title: String = "", // Title of the audiobook
    val author: String = "", // Name of the author/narrator
    val price: Double = 0.0, // Purchase price of the audiobook
    val description: String = "", // Detailed description or summary
    val imageUrl: String = "", // URL for the cover art image
    val audioUrl: String = "", // URL for the audio stream file
    val category: String = "General", // Sub-category (e.g., Fiction, Tech)
    val mainCategory: String = "Audio Books" // Top-level category for filtering
)
