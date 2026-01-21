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
    val mainCategory: String = "University Gear"
)
