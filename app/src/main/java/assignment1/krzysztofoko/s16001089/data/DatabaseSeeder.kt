package assignment1.krzysztofoko.s16001089.data

import com.google.firebase.firestore.FirebaseFirestore

fun seedDatabase() {
    val db = FirebaseFirestore.getInstance()
    val booksCollection = db.collection("books")

    val sampleItems = listOf(
        // --- BOOKS ---
        Book(
            title = "Clean Architecture",
            author = "Robert C. Martin",
            price = 35.99,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Technology",
            description = "Robert C. Martin (Uncle Bob) presents a guide to software structure and design. Learn the principles of building clean, maintainable, and scalable systems.",
            imageUrl = "file:///android_asset/images/books/clean_architecture.jpg"
        ),
        Book(
            title = "The Pragmatic Programmer",
            author = "Andrew Hunt & David Thomas",
            price = 42.50,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Technology",
            description = "One of the most significant books in computer science. It covers topics ranging from personal responsibility and career development to architectural techniques.",
            imageUrl = "file:///android_asset/images/books/pragmatic_programmer.jpg"
        ),
        Book(
            title = "Student Handbook 2024",
            author = "University Admin",
            price = 0.0,
            isAudioBook = false,
            mainCategory = "Books",
            category = "General",
            description = "The official guide for all students. Contains information on campus facilities, academic regulations, and student support services.",
            imageUrl = "file:///android_asset/images/books/handbook.jpg"
        ),
        Book(
            title = "Thinking, Fast and Slow",
            author = "Daniel Kahneman",
            price = 15.99,
            isAudioBook = false,
            mainCategory = "Books",
            category = "Self-Help",
            description = "A world-renowned psychologist explains the two systems that drive the way we think—one fast, intuitive, and emotional; the other slower, more deliberative, and more logical.",
            imageUrl = "file:///android_asset/images/books/thinking_fast_slow.jpg"
        ),

        // --- AUDIO BOOKS ---
        Book(
            title = "The 5 AM Club",
            author = "Robin Sharma",
            price = 14.99,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Self-Help",
            description = "Own your morning. Elevate your life. A revolutionary manifesto for elite performance.",
            imageUrl = "file:///android_asset/images/audio/5am_club.jpg",
            audioUrl = "file:///android_asset/audio/5am_club_sample.mp3"
        ),
        Book(
            title = "Campus Tour (Audio)",
            author = "Student Union",
            price = 0.0,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "General",
            description = "An immersive audio guide exploring the key landmarks and history of the Glyndŵr campus.",
            imageUrl = "file:///android_asset/images/audio/tour.jpg",
            audioUrl = "file:///android_asset/audio/tour_sample.mp3"
        ),
        Book(
            title = "Deep Work (Audio)",
            author = "Cal Newport",
            price = 18.00,
            isAudioBook = true,
            mainCategory = "Audio Books",
            category = "Technology",
            description = "Rules for focused success in a distracted world. Learn how to master complicated information quickly.",
            imageUrl = "file:///android_asset/images/audio/deep_work.jpg",
            audioUrl = "file:///android_asset/audio/deep_work_sample.mp3"
        ),

        // --- UNIVERSITY COURSES ---
        Book(
            title = "Introduction to Data Science",
            author = "Faculty of Computing",
            price = 299.99,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Technology",
            description = "Master the basics of data analysis, Python, and statistics. Ideal for beginners starting their journey in Computing.",
            imageUrl = "file:///android_asset/images/courses/data_science.jpg"
        ),
        Book(
            title = "Academic Writing Workshop",
            author = "Learning Center",
            price = 0.0,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Education",
            description = "Enhance your academic writing skills with this free introductory workshop. Learn about structuring essays and proper citation.",
            imageUrl = "file:///android_asset/images/courses/writing.jpg"
        ),
        Book(
            title = "Advanced Business Analytics",
            author = "Business School",
            price = 450.00,
            isAudioBook = false,
            mainCategory = "University Courses",
            category = "Business",
            description = "Learn to leverage big data for strategic decision making. Includes case studies from industry leaders.",
            imageUrl = "file:///android_asset/images/courses/business_analytics.jpg"
        ),
        
        // --- UNIVERSITY GEAR ---
        Book(
            title = "Glyndŵr Official Hoodie",
            author = "Wrexham University",
            price = 35.00,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Apparel",
            description = "High-quality, comfortable navy hoodie featuring the embroidered University crest. Perfect for campus life.",
            imageUrl = "file:///android_asset/images/gear/official_hoodie.jpg"
        ),
        Book(
            title = "Digital Wallpaper Pack",
            author = "Creative Media",
            price = 0.0,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Digital",
            description = "High-resolution branded wallpapers for your desktop and mobile devices featuring campus scenery.",
            imageUrl = "file:///android_asset/images/gear/wallpapers.jpg"
        ),
        Book(
            title = "Stainless Steel Water Bottle",
            author = "Eco Gear",
            price = 15.50,
            isAudioBook = false,
            mainCategory = "University Gear",
            category = "Lifestyle",
            description = "Stay hydrated with this 750ml vacuum-insulated bottle. Keeps drinks cold for 24h and hot for 12h.",
            imageUrl = "file:///android_asset/images/gear/water_bottle.jpg"
        )
    )

    for (item in sampleItems) {
        val stableId = item.title.lowercase().replace(" ", "_").replace("(", "").replace(")", "")
        booksCollection.document(stableId).set(item.copy(id = stableId))
    }
}
