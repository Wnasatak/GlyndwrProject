package assignment1.krzysztofoko.s16001089.data

import android.util.Log

// This function fills our local database with some starting data so the app isn't empty
suspend fun seedDatabase(db: AppDatabase) { 
    // Just a log message to let us know in the console that seeding has started
    Log.d("DatabaseSeeder", "Starting to seed local database with expanded catalog and user data...") 
    
    // 1. BOOKS SECTION
    val sampleBooks = listOf( 
        Book(title = "Clean Architecture", author = "Robert C. Martin", price = 35.99, mainCategory = "Books", category = "Technology", description = "A comprehensive guide to software structure and design patterns. Learn how to create systems that are easy to maintain, test, and evolve over time using the principles of Clean Architecture.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "The Pragmatic Programmer", author = "Andrew Hunt", price = 42.50, mainCategory = "Books", category = "Technology", description = "Classic computer science book that provides practical advice for software engineers. This anniversary edition covers everything from personal responsibility to career development.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Student Handbook", author = "University Admin", price = 0.0, mainCategory = "Books", category = "General", description = "The official guide for all students at Wrexham University. Contains essential information about campus facilities, academic regulations, and student support services available to you.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Kotlin in Action", author = "Dmitry Jemerov", price = 45.00, mainCategory = "Books", category = "Technology", description = "Master the Kotlin programming language for JVM, Android, and more. This book guides you through the language features and best practices for writing clean, idiomatic code.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Design Patterns", author = "Erich Gamma", price = 54.99, mainCategory = "Books", category = "Technology", description = "The definitive guide to reusable object-oriented software. Learn the fundamental patterns used by experts to solve common problems in software design and architecture.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Library Orientation Guide", author = "Library Services", price = 0.0, mainCategory = "Books", category = "General", description = "Learn how to make the most of the university library, including digital resources and study space bookings.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Research Methods 101", author = "Academic Support", price = 0.0, mainCategory = "Books", category = "Education", description = "A beginner's guide to conducting academic research and referencing properly at university level.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png")
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    db.bookDao().insertAll(sampleBooks)

    // 2. AUDIOBOOKS SECTION
    // We update the URLs and Images to point to the new nested assets structure
    val sampleAudio = listOf( 
        AudioBook(
            title = "Atomic Habits", 
            author = "James Clear", 
            price = 12.00, 
            mainCategory = "Audio Books", 
            category = "Self-Help", 
            description = "Transform your life with tiny changes. This audiobook explains how small habits can lead to remarkable results.", 
            imageUrl = "file:///android_asset/audiobooks/audiobook-example-1/audiobook-example1.webp", 
            audioUrl = "audiobooks/audiobook-example-1/Audiobook-example1.mp3"
        ),
        AudioBook(
            title = "Deep Work", 
            author = "Cal Newport", 
            price = 15.50, 
            mainCategory = "Audio Books", 
            category = "Productivity", 
            description = "In a world of constant distraction, the ability to focus on cognitively demanding tasks is a superpower.", 
            imageUrl = "file:///android_asset/audiobooks/audiobook-example-2/audiobook-example2.webp", 
            audioUrl = "audiobooks/audiobook-example-2/Audiobook-example2.mp3"
        ),
        AudioBook(
            title = "The Lean Startup", 
            author = "Eric Ries", 
            price = 18.00, 
            mainCategory = "Audio Books", 
            category = "Business", 
            description = "A must-listen for aspiring entrepreneurs. Discover a scientific approach to creating and managing successful startups.", 
            imageUrl = "file:///android_asset/audiobooks/audiobook-example-3/audiobook-example3.webp", 
            audioUrl = "audiobooks/audiobook-example-3/Audiobook-example3.mp3"
        ),
        AudioBook(
            title = "Thinking, Fast and Slow", 
            author = "Daniel Kahneman", 
            price = 22.00, 
            mainCategory = "Audio Books", 
            category = "Psychology", 
            description = "Nobel laureate Daniel Kahneman takes you on a groundbreaking tour of the mind and explains the two systems that drive the way we think.", 
            imageUrl = "file:///android_asset/audiobooks/audiobook-example-4/audiobook-example4.webp", 
            audioUrl = "audiobooks/audiobook-example-4/Audiobook-example4.mp3"
        ),
        AudioBook(
            title = "The 5 AM Club", 
            author = "Robin Sharma", 
            price = 14.99, 
            mainCategory = "Audio Books", 
            category = "Self-Help", 
            description = "Legendary leadership expert Robin Sharma introduces the 5 AM Club concept based on a revolutionary morning routine.", 
            imageUrl = "file:///android_asset/audiobooks/audiobook-example-5/audiobook-example5.webp", 
            audioUrl = "audiobooks/audiobook-example-5/Audiobook-example5.mp3"
        ),
        AudioBook(
            title = "Meditation for Students", 
            author = "Wellness Center", 
            price = 0.0, 
            mainCategory = "Audio Books", 
            category = "Health", 
            description = "Guided 10-minute meditation sessions specifically designed to reduce exam stress and improve focus.", 
            imageUrl = "file:///android_asset/audiobooks/audiobook-example-6/audiobook-example6.webp", 
            audioUrl = "audiobooks/audiobook-example-6/Audiobook-example6.mp3"
        ),
        AudioBook(
            title = "Campus History Tour", 
            author = "Uni Heritage", 
            price = 0.0, 
            mainCategory = "Audio Books", 
            category = "General", 
            description = "A narrated walking tour of the Wrexham University campus history and notable landmarks.", 
            imageUrl = "file:///android_asset/audiobooks/audiobook-example-7/audiobook-example7.webp", 
            audioUrl = "audiobooks/audiobook-example-7/Audiobook-example7.mp3"
        )
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    
    // Using insertAll with REPLACE ensures we update the paths without affecting related data (comments/purchases)
    db.audioBookDao().insertAll(sampleAudio)

    // 3. COURSES SECTION
    val sampleCourses = listOf(
        Course(title = "BSc Computer Science", price = 9250.00, category = "Technology", description = "A comprehensive degree program covering software engineering, data structures, and algorithms.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1200.0),
        Course(title = "MA Creative Media", price = 8500.00, category = "Arts", description = "Push the boundaries of digital storytelling and media production.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1500.0),
        Course(title = "MBA Business Admin", price = 12000.00, category = "Business", description = "Develop the leadership skills needed for the global business environment.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 2000.0),
        Course(title = "BEng Cyber Security", price = 9250.00, category = "Technology", description = "Specialized engineering degree focused on network security and threat prevention.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1200.0),
        Course(title = "BSc Psychology", price = 9250.00, category = "Science", description = "Explore the complexities of the human mind and behavior.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1200.0),
        Course(title = "Intro to Python", price = 0.0, category = "Technology", description = "A free introductory course for students from any department wanting to learn the basics of coding with Python.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "CS Dept", modulePrice = 0.0),
        Course(title = "Academic Writing", price = 0.0, category = "General", description = "A short course designed to help you improve your essay writing, structuring, and academic tone.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Arts Dept", modulePrice = 0.0)
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    db.courseDao().insertAll(sampleCourses)

    // 4. GEAR SECTION
    val sampleGear = listOf(
        Gear(title = "Official Hoodie", price = 35.00, category = "Apparel", description = "Premium navy blue hoodie featuring the university crest.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Pro Tech Backpack", price = 55.00, category = "Accessories", description = "Ergonomically designed laptop bag with dedicated compartments for your tech and books.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Insulated Water Bottle", price = 15.00, category = "Lifestyle", description = "Durable stainless steel bottle that keeps drinks cold for 24 hours or hot for 12.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Deluxe Stationery Set", price = 25.00, category = "Office", description = "Complete set including a professional notebook, engraved pens, and organizational accessories.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "University Sports Jersey", price = 40.00, category = "Apparel", description = "High-performance athletic wear designed for Wrexham University teams.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Freshers Wristband", price = 0.0, category = "Event", description = "Free entry wristband for the orientation events during the first week of term.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Campus Map Kit", price = 0.0, category = "General", description = "A high-quality physical map kit and pocket guide for navigating the university buildings.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png")
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    db.gearDao().insertAll(sampleGear)


    Log.d("DatabaseSeeder", "Database Seeding Finished!") 
}
