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
        HistoryItem::class
    ], 
    version = 5, 
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
                // We use createFromAsset so that if the emulator is wiped, 
                // the app will restore itself using the database file located in:
                // assets/database/glyndwr_database.db
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glyndwr_database.db"
                )
                .createFromAsset("database/glyndwr_database.db")
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
