package assignment1.krzysztofoko.s16001089.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 4, 
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gear ADD COLUMN sizes TEXT NOT NULL DEFAULT 'M'")
                db.execSQL("ALTER TABLE gear ADD COLUMN colors TEXT NOT NULL DEFAULT 'Default'")
                db.execSQL("ALTER TABLE gear ADD COLUMN stockCount INTEGER NOT NULL DEFAULT 10")
                db.execSQL("ALTER TABLE gear ADD COLUMN brand TEXT NOT NULL DEFAULT 'Wrexham University'")
                db.execSQL("ALTER TABLE gear ADD COLUMN isAvailable INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gear ADD COLUMN material TEXT NOT NULL DEFAULT 'Mixed Fibers'")
                db.execSQL("ALTER TABLE gear ADD COLUMN sku TEXT NOT NULL DEFAULT 'WREX-GEAR-000'")
                db.execSQL("ALTER TABLE gear ADD COLUMN originalPrice REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE gear ADD COLUMN isFeatured INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE gear ADD COLUMN productTags TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gear ADD COLUMN secondaryImageUrl TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glyndwr_database.db"
                )
                .createFromAsset("database/glyndwr_database.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
