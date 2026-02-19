/**
 * Gear.kt
 *
 * This file defines the Gear Room entity, representing physical merchandise or products (like university branded clothing)
 * in the application. It stores detailed product information including variants, stock levels, and promotional flags.
 */

package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents university merchandise (Gear) in the 'gear' database table.
 */
@Entity(tableName = "gear") // Room table definition
data class Gear(
    @PrimaryKey
    val id: String = "", // Unique identifier for the product
    val title: String = "", // Display name of the gear item
    val price: Double = 0.0, // Current selling price
    val description: String = "", // Detailed product description and features
    val imageUrl: String = "", // Primary product image URL
    val category: String = "General", // Sub-category (e.g., Hoodie, Bottle)
    val mainCategory: String = "University Gear", // Top-level category for store filtering
    val sizes: String = "M", // Available sizes (e.g., S, M, L, XL)
    val colors: String = "Default", // Available color variants
    val stockCount: Int = 10, // Remaining inventory count
    val brand: String = "Wrexham University", // Manufacturer or brand name
    val isAvailable: Boolean = true, // Flag to show if the item is currently in stock/for sale
    val material: String = "Mixed Fibers", // Product material composition (e.g., 100% Cotton)
    val sku: String = "WREX-GEAR-000", // Stock Keeping Unit (unique code for inventory)
    val originalPrice: Double = 0.0, // Used to calculate and display discounts or sales
    val isFeatured: Boolean = false, // Flag for highlighting in the store homepage/featured section
    val productTags: String = "", // Comma-separated tags for improved search and filtering
    val secondaryImageUrl: String? = null // Optional second image for product galleries
)
