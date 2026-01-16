package assignment1.krzysztofoko.s16001089.data

import com.google.firebase.firestore.PropertyName

data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val price: Double = 0.0,
    @get:PropertyName("audioBook")
    @set:PropertyName("audioBook")
    var isAudioBook: Boolean = false,
    val audioUrl: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "General",
    val mainCategory: String = "Books"
)
