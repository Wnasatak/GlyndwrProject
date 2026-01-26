package assignment1.krzysztofoko.s16001089.data

import android.util.Log

/**
 * Seeder function to populate the database with initial sample data.
 * Course seeding has been removed as they are now managed directly from the database.
 */
suspend fun seedDatabase(db: AppDatabase) {
    Log.d("DatabaseSeeder", "Seeding database (skipping courses)...")



    Log.d("DatabaseSeeder", "Database seeding finished!")
}
