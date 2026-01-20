package assignment1.krzysztofoko.s16001089.data

import com.google.firebase.firestore.FirebaseFirestore

fun seedDatabase() {
    val db = FirebaseFirestore.getInstance()
    val booksCollection = db.collection("books")

    val sampleItems = listOf(
        // --- UNIVERSITY COURSES ---
        Book(
            title = "Introduction to Data Science",
            author = "Faculty of Computing",
            price = 299.99,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Technology",
            description = "Master the basics of data analysis, Python, and statistics. Ideal for beginners starting their journey in Computing."
        ),
        Book(
            title = "Advanced Business Analytics",
            author = "Business School",
            price = 450.00,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Business",
            description = "Learn to leverage big data for strategic decision making. Includes case studies from industry leaders."
        ),
        
        // --- UNIVERSITY GEAR ---
        Book(
            title = "Glyndŵr Official Hoodie",
            author = "Wrexham University",
            price = 35.00,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Apparel",
            description = "High-quality, comfortable navy hoodie featuring the embroidered University crest. Perfect for campus life."
        ),
        Book(
            title = "Executive Notebook & Pen Set",
            author = "University Stationery",
            price = 12.99,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Stationery",
            description = "Premium A5 leather-bound notebook with a matching engraved pen. Ideal for lecture notes."
        ),

        // --- BOOKS ---
        Book(
            title = "Clean Architecture",
            author = "Robert C. Martin",
            price = 35.99,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Technology",
            description = "A craftsman's guide to software structure and design. Essential reading for aspiring software engineers."
        ),
        Book(
            title = "The Art of Cooking",
            author = "Chef Julia Child",
            price = 19.50,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Cooking",
            description = "Master the fundamentals of French cuisine with clear instructions and timeless techniques."
        ),

        // --- AUDIO BOOKS ---
        Book(
            title = "The 5 AM Club",
            author = "Robin Sharma",
            price = 14.99,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Self-Help",
            description = "Own your morning. Elevate your life. A revolutionary manifesto for elite performance."
        ),
        Book(
            title = "Deep Work (Audio)",
            author = "Cal Newport",
            price = 18.00,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Technology",
            description = "Rules for focused success in a distracted world. Learn how to master complicated information quickly."
        )
    )

    for (item in sampleItems) {
        val stableId = item.title.lowercase().replace(" ", "_").replace("(", "").replace(")", "")
        booksCollection.document(stableId).set(item.copy(id = stableId))
    }
}
