package assignment1.krzysztofoko.s16001089.data

import android.util.Log

/**
 * Seeder function for initial database data.
 */
suspend fun seedDatabase(db: AppDatabase) {
    Log.d("DatabaseSeeder", "Seeding database...")
    
    // Add Tutor/Teacher user if it doesn't exist
    // Note: This matches the Firebase UID that would be created for teacher@example.com
    // In a real scenario, this would be handled after the first login, 
    // but we can seed it here for the UI to recognize the role.
    val teacherEmail = "teacher@example.com"
    val userDao = db.userDao()
    
    // Check if any user with this email exists
    // (In local DB we might not have the UID yet until they log in, 
    // but we can pre-seed it or let AuthViewModel handle it)
    
    Log.d("DatabaseSeeder", "Database seeding checked.")
}
