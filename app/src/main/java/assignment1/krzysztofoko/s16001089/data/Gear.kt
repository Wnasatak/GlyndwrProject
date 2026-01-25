package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gear")
data class Gear(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "General",
    val mainCategory: String = "University Gear",
    val sizes: String = "M",
    val colors: String = "Default",
    val stockCount: Int = 10,
    val brand: String = "Wrexham University",
    val isAvailable: Boolean = true,
    val material: String = "Mixed Fibers",
    val sku: String = "WREX-GEAR-000",
    val originalPrice: Double = 0.0, // To show sales
    val isFeatured: Boolean = false,
    val productTags: String = "", // Comma separated tags
    val secondaryImageUrl: String? = null // For a second featured image
)
