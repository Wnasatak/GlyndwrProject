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
            category = "Science",
            description = "Master the basics of data analysis, Python, and statistics. Ideal for beginners."
        ),
        Book(
            title = "International Business Law",
            author = "School of Business",
            price = 249.00,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Business",
            description = "Legal frameworks for global trade, commerce, and international agreements."
        ),
        Book(
            title = "Advanced Cyber Security",
            author = "Tech Faculty",
            price = 350.00,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Technology",
            description = "Protecting enterprise networks from modern threats and ethical hacking basics."
        ),
        Book(
            title = "Digital Marketing Mastery",
            author = "School of Business",
            price = 199.00,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Business",
            description = "Learn SEO, SEM, and social media strategies to grow any business."
        ),

        // --- UNIVERSITY GEAR ---
        Book(
            title = "Wrexham Uni Hoodie",
            author = "Uni Merchandise",
            price = 45.00,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Apparel",
            description = "Stay warm with our official heavy-cotton hoodie featuring the embroidered university crest."
        ),
        Book(
            title = "Eco-Friendly Stationery Set",
            author = "Uni Merchandise",
            price = 15.50,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Stationery",
            description = "Sustainable pens, notebooks, and folders for the eco-conscious student."
        ),
        Book(
            title = "Official Graduation Bear",
            author = "Uni Merchandise",
            price = 20.00,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Gifts",
            description = "A perfect keepsake for your graduation day."
        ),

        // --- BOOKS ---
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
            title = "The Mage's Tower",
            author = "Brandon Sanderson",
            price = 21.00,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Fantasy",
            description = "An epic tale of magic and betrayal in a city floating in the sky."
        ),
        Book(
            title = "Sherlock Holmes: The Beginning",
            author = "Arthur Conan Doyle",
            price = 12.00,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Mystery",
            description = "The classic cases of the world's greatest detective, newly edited."
        ),
        Book(
            title = "Gordon's Masterclass",
            author = "Gordon Ramsay",
            price = 22.99,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Cooking",
            description = "Learn professional restaurant techniques for your own home kitchen."
        ),

        // --- AUDIO BOOKS ---
        Book(
            title = "The 5 AM Club (Audio)",
            author = "Robin Sharma",
            price = 14.99,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Self-Help",
            description = "Own your morning. Elevate your life. Narrative self-improvement masterpiece."
        ),
        Book(
            title = "Death on the Nile (Audio)",
            author = "Agatha Christie",
            price = 10.50,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Mystery",
            description = "Hercule Poirot's famous Egyptian mystery brought to life by a full cast."
        ),
        Book(
            title = "Bread Baking Masterclass (Audio)",
            author = "Paul Hollywood",
            price = 11.50,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Cooking",
            description = "Listen to step-by-step guides for the perfect crust and crumb."
        ),

        // --- FREE ITEMS ---
        Book(
            title = "Uni Student Handbook",
            author = "Administration",
            price = 0.0,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "General",
            description = "Essential guide for all new students. Free for everyone."
        ),
        Book(
            title = "Meditation for Students (Audio)",
            author = "Wellness Center",
            price = 0.0,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Self-Help",
            description = "FREE guided sessions to help you reduce exam stress and focus better."
        )
    )

    for (item in sampleItems) {
        booksCollection.add(item)
            .addOnSuccessListener { documentReference ->
                booksCollection.document(documentReference.id).update("id", documentReference.id)
            }
    }
}
