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
        ReviewInteraction::class,
        Invoice::class,
        NotificationLocal::class,
        SearchHistoryItem::class,
        CourseInstallment::class
    ], 
    version = 11, 
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE purchases ADD COLUMN orderConfirmation TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoices` (
                        `invoiceNumber` TEXT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `productId` TEXT NOT NULL, 
                        `itemTitle` TEXT NOT NULL, 
                        `itemCategory` TEXT NOT NULL, 
                        `pricePaid` REAL NOT NULL, 
                        `quantity` INTEGER NOT NULL, 
                        `purchasedAt` INTEGER NOT NULL, 
                        `paymentMethod` TEXT NOT NULL, 
                        `billingName` TEXT NOT NULL, 
                        `billingEmail` TEXT NOT NULL, 
                        `billingAddress` TEXT, 
                        PRIMARY KEY(`invoiceNumber`)
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE purchases ADD COLUMN mainCategory TEXT NOT NULL DEFAULT 'Books'")
                db.execSQL("ALTER TABLE purchases ADD COLUMN totalPricePaid REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE purchases ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE invoices ADD COLUMN itemVariant TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE invoices ADD COLUMN discountApplied REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE invoices ADD COLUMN orderReference TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new Purchases table with unique purchaseId as Primary Key
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `purchases_new` (
                        `purchaseId` TEXT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `productId` TEXT NOT NULL, 
                        `mainCategory` TEXT NOT NULL, 
                        `purchasedAt` INTEGER NOT NULL, 
                        `paymentMethod` TEXT NOT NULL, 
                        `amountFromWallet` REAL NOT NULL, 
                        `amountPaidExternal` REAL NOT NULL, 
                        `totalPricePaid` REAL NOT NULL, 
                        `quantity` INTEGER NOT NULL, 
                        `orderConfirmation` TEXT, 
                        PRIMARY KEY(`purchaseId`)
                    )
                """.trimIndent())
                
                // 2. Create the Notifications table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notifications` (
                        `id` TEXT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `message` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `isRead` INTEGER NOT NULL, 
                        `type` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 3. Drop old purchases and rename new one
                db.execSQL("DROP TABLE IF EXISTS `purchases` ")
                db.execSQL("ALTER TABLE `purchases_new` RENAME TO `purchases` ")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notifications ADD COLUMN productId TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users_local ADD COLUMN phoneNumber TEXT DEFAULT NULL")
                db.execSQL("CREATE TABLE IF NOT EXISTS `search_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `query` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `course_installments` (`userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `modulesPaid` INTEGER NOT NULL DEFAULT 1, `totalModules` INTEGER NOT NULL DEFAULT 4, `isFullyPaid` INTEGER NOT NULL DEFAULT 0, `lastPaymentDate` INTEGER NOT NULL, PRIMARY KEY(`userId`, `courseId`))")
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
