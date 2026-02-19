/**
 * Course.kt
 *
 * This file defines the Course Room entity, representing an academic course offered by the university.
 * It stores course details, pricing, and administrative information such as the department.
 */

package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a university course within the local database.
 */
@Entity(tableName = "courses") // Room table definition
data class Course(
    @PrimaryKey
    val id: String = "", // Unique identifier for the course
    val title: String = "", // Display title of the course
    val price: Double = 0.0, // Total cost of the course
    val description: String = "", // Detailed course overview and syllabus information
    val imageUrl: String = "", // URL for the course's promotional image or icon
    val category: String = "General", // Sub-category for classification (e.g., Computing, Arts)
    val mainCategory: String = "University Courses", // Top-level category for filtering
    val department: String = "Wrexham University", // The academic department hosting the course
    val isInstallmentAvailable: Boolean = true, // Flag indicating if the course can be paid for per module
    val modulePrice: Double = 0.0 // The price per individual module if installments are enabled
)
