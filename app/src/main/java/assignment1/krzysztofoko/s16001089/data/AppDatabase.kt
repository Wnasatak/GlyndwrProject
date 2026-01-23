package assignment1.krzysztofoko.s16001089.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Book::class, 
        AudioBook::class, 
        Course::class, 
        Gear::class, 
        UserLocal::class, 
        WishlistItem::class, 
        PurchaseItem::class,
        ReviewLocal::class,
        HistoryItem::class,
        ReviewInteraction::class
    ], 
    version = 1, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun audioBookDao(): AudioBookDao
    abstract fun courseDao(): CourseDao
    abstract fun gearDao(): GearDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glyndwr_database.db"
                )
                .createFromAsset("database/glyndwr_database.db")
                // If the schema in the file doesn't match the code, Room will recreate the tables
                .fallbackToDestructiveMigration() 
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
