package assignment1.krzysztofoko.s16001089.data

import com.google.firebase.firestore.FirebaseFirestore

fun seedDatabase() {
    val db = FirebaseFirestore.getInstance()
    val booksCollection = db.collection("books")

    val sampleItems = listOf(
        Book(
            title = "Introduction to Data Science",
            author = "Faculty of Computing",
            price = 299.99,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Science",
            description = "Master the basics of data analysis, Python, and statistics. Ideal for beginners."
        ),
        Book(
            title = "Clean Architecture",
            author = "Robert C. Martin",
            price = 35.99,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Technology",
            description = "A craftsman's guide to software structure and design. Essential for developers."
        ),
        Book(
            title = "The 5 AM Club (Audio)",
            author = "Robin Sharma",
            price = 14.99,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Self-Help",
            description = "Own your morning. Elevate your life. Narrative self-improvement masterpiece."
        )
        // Add other items here...
    )

    for (item in sampleItems) {
        // Use a stable ID (like a slug of the title) so reviews persist across re-seeds
        val stableId = item.title.lowercase().replace(" ", "_").replace("(", "").replace(")", "")
        booksCollection.document(stableId).set(item.copy(id = stableId))
    }
}
