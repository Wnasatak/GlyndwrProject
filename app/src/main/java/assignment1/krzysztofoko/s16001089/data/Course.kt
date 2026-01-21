package assignment1.krzysztofoko.s16001089.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "General",
    val mainCategory: String = "University Courses",
    val department: String = "Wrexham University",
    val isInstallmentAvailable: Boolean = true,
    val modulePrice: Double = 0.0
)
