package assignment1.krzysztofoko.s16001089.data

import android.util.Log

suspend fun seedDatabase(db: AppDatabase) {
    Log.d("DatabaseSeeder", "Starting to seed local database with expanded catalog and user data...")
    
    // 1. Books
    val sampleBooks = listOf(
        Book(title = "Clean Architecture", author = "Robert C. Martin", price = 35.99, mainCategory = "Books", category = "Technology", description = "A comprehensive guide to software structure and design patterns. Learn how to create systems that are easy to maintain, test, and evolve over time using the principles of Clean Architecture.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "The Pragmatic Programmer", author = "Andrew Hunt", price = 42.50, mainCategory = "Books", category = "Technology", description = "Classic computer science book that provides practical advice for software engineers. This anniversary edition covers everything from personal responsibility to career development.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Student Handbook", author = "University Admin", price = 0.0, mainCategory = "Books", category = "General", description = "The official guide for all students at Wrexham University. Contains essential information about campus facilities, academic regulations, and student support services available to you.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Kotlin in Action", author = "Dmitry Jemerov", price = 45.00, mainCategory = "Books", category = "Technology", description = "Master the Kotlin programming language for JVM, Android, and more. This book guides you through the language features and best practices for writing clean, idiomatic code.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Design Patterns", author = "Erich Gamma", price = 54.99, mainCategory = "Books", category = "Technology", description = "The definitive guide to reusable object-oriented software. Learn the fundamental patterns used by experts to solve common problems in software design and architecture.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        // Added Free Staff
        Book(title = "Library Orientation Guide", author = "Library Services", price = 0.0, mainCategory = "Books", category = "General", description = "Learn how to make the most of the university library, including digital resources and study space bookings.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Book(title = "Research Methods 101", author = "Academic Support", price = 0.0, mainCategory = "Books", category = "Education", description = "A beginner's guide to conducting academic research and referencing properly at university level.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png")
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    db.bookDao().insertAll(sampleBooks)

    // 2. AudioBooks
    val sampleAudio = listOf(
        AudioBook(title = "Atomic Habits", author = "James Clear", price = 12.00, mainCategory = "Audio Books", category = "Self-Help", description = "Transform your life with tiny changes. This audiobook explains how small habits can lead to remarkable results by leveraging the science of behavior change and habit formation.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        AudioBook(title = "Deep Work", author = "Cal Newport", price = 15.50, mainCategory = "Audio Books", category = "Productivity", description = "In a world of constant distraction, the ability to focus on cognitively demanding tasks is a superpower. Learn strategies to cultivate deep work habits and achieve peak productivity.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        AudioBook(title = "The Lean Startup", author = "Eric Ries", price = 18.00, mainCategory = "Audio Books", category = "Business", description = "A must-listen for aspiring entrepreneurs. Discover a scientific approach to creating and managing successful startups in an age when companies need to innovate more than ever.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        AudioBook(title = "Thinking, Fast and Slow", author = "Daniel Kahneman", price = 22.00, mainCategory = "Audio Books", category = "Psychology", description = "Nobel laureate Daniel Kahneman takes you on a groundbreaking tour of the mind and explains the two systems that drive the way we think and make decisions.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        AudioBook(title = "The 5 AM Club", author = "Robin Sharma", price = 14.99, mainCategory = "Audio Books", category = "Self-Help", description = "Legendary leadership expert Robin Sharma introduces the 5 AM Club concept, based on a revolutionary morning routine that has helped his clients maximize their productivity and health.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        // Added Free Staff
        AudioBook(title = "Meditation for Students", author = "Wellness Center", price = 0.0, mainCategory = "Audio Books", category = "Health", description = "Guided 10-minute meditation sessions specifically designed to reduce exam stress and improve focus.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        AudioBook(title = "Campus History Tour", author = "Uni Heritage", price = 0.0, mainCategory = "Audio Books", category = "General", description = "A narrated walking tour of the Wrexham University campus history and notable landmarks.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png")
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    db.audioBookDao().insertAll(sampleAudio)

    // 3. Courses
    val sampleCourses = listOf(
        Course(title = "BSc Computer Science", price = 9250.00, category = "Technology", description = "A comprehensive degree program covering software engineering, data structures, and algorithms. Prepare for a career in the technology industry with hands-on projects and expert guidance.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1200.0),
        Course(title = "MA Creative Media", price = 8500.00, category = "Arts", description = "Push the boundaries of digital storytelling and media production. This postgraduate course focuses on innovation in film, animation, and digital content creation for modern platforms.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1500.0),
        Course(title = "MBA Business Admin", price = 12000.00, category = "Business", description = "Develop the leadership skills needed for the global business environment. This MBA covers strategic management, finance, and marketing to prepare you for executive roles.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 2000.0),
        Course(title = "BEng Cyber Security", price = 9250.00, category = "Technology", description = "Specialized engineering degree focused on network security and threat prevention. Learn to protect critical infrastructure and data in an increasingly digital and interconnected world.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1200.0),
        Course(title = "BSc Psychology", price = 9250.00, category = "Science", description = "Explore the complexities of the human mind and behavior. This accredited program provides a scientific foundation in psychological theory and research methods for aspiring professionals.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Wrexham University", modulePrice = 1200.0),
        // Added Free Staff
        Course(title = "Intro to Python", price = 0.0, category = "Technology", description = "A free introductory course for students from any department wanting to learn the basics of coding with Python.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "CS Dept", modulePrice = 0.0),
        Course(title = "Academic Writing", price = 0.0, category = "General", description = "A short course designed to help you improve your essay writing, structuring, and academic tone.", imageUrl = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg", department = "Arts Dept", modulePrice = 0.0)
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    db.courseDao().insertAll(sampleCourses)

    // 4. Gear
    val sampleGear = listOf(
        Gear(title = "Official Hoodie", price = 35.00, category = "Apparel", description = "Premium navy blue hoodie featuring the university crest. Made from high-quality, sustainable cotton for maximum cotton comfort and style during your studies on campus.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Pro Tech Backpack", price = 55.00, category = "Accessories", description = "Ergonomically designed laptop bag with dedicated compartments for your tech and books. Features water-resistant material and padded straps for the ultimate student commuter.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Insulated Water Bottle", price = 15.00, category = "Lifestyle", description = "Durable stainless steel bottle that keeps drinks cold for 24 hours or hot for 12. Help reduce plastic waste while staying hydrated during your long library sessions.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Deluxe Stationery Set", price = 25.00, category = "Office", description = "Complete set including a professional notebook, engraved pens, and organizational accessories. Everything you need to take perfect notes and keep your academic life on track.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "University Sports Jersey", price = 40.00, category = "Apparel", description = "High-performance athletic wear designed for Wrexham University teams. Breathable, moisture-wicking fabric that ensures you stay cool and comfortable while representing your uni.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        // Added Free Staff
        Gear(title = "Freshers Wristband", price = 0.0, category = "Event", description = "Free entry wristband for the orientation events during the first week of term.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png"),
        Gear(title = "Campus Map Kit", price = 0.0, category = "General", description = "A high-quality physical map kit and pocket guide for navigating the university buildings.", imageUrl = "file:///android_asset/images/media/Glyndwr_University_Logo.png")
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }
    db.gearDao().insertAll(sampleGear)
    
    // 5. User Seeding
    val krisId = "user_kris_001"
    val demoUser = UserLocal(
        id = krisId,
        name = "Krzysztof Oko",
        email = "prokocomp@gmail.com",
        balance = 1000.0,
        address = "Wrexham University Campus, Mold Road, LL11 2AW",
        role = "student",
        photoUrl = "file:///android_asset/images/users/avatars/Krzysztof-Oko.jpeg"
    )
    db.userDao().upsertUser(demoUser)

    val andrejId = "wzYECwgHwTn7Ebhx8vPk7Epuaat2" // UID from Database Inspector
    val andrejUser = UserLocal(
        id = andrejId,
        name = "Andrej Nilsen", // Updated spelling from prompt
        email = "wnasatak@gmail.com",
        balance = 500.0,
        address = "Student Village, Wrexham",
        role = "student",
        photoUrl = "file:///android_asset/images/users/avatars/Krzysztof-Oko.jpeg" // Assigned requested avatar
    )
    db.userDao().upsertUser(andrejUser)
    
    // 6. Review Seeding
    db.userDao().addReview(ReviewLocal(
        productId = "clean_architecture",
        userId = krisId,
        userName = "Krzysztof Oko",
        userPhotoUrl = "file:///android_asset/images/users/avatars/Krzysztof-Oko.jpeg",
        comment = "An absolute masterpiece. Every developer should read this.",
        rating = 5
    ))
    db.userDao().addReview(ReviewLocal(
        productId = "clean_architecture",
        userId = andrejId,
        userName = "Andrej Nilsen",
        userPhotoUrl = "file:///android_asset/images/users/avatars/Krzysztof-Oko.jpeg",
        comment = "Yes, thats thru! 😊",
        rating = 5,
        parentReviewId = 1
    ))
    
    Log.d("DatabaseSeeder", "Database Seeding Finished!")
}
