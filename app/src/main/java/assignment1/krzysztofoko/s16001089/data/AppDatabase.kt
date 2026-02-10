package assignment1.krzysztofoko.s16001089.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Book::class, AudioBook::class, Course::class, Gear::class, UserLocal::class, 
        WishlistItem::class, PurchaseItem::class, ReviewLocal::class, HistoryItem::class, 
        ReviewInteraction::class, Invoice::class, NotificationLocal::class, 
        SearchHistoryItem::class, CourseInstallment::class, ModuleContent::class, 
        Assignment::class, AssignmentSubmission::class, Grade::class, LiveSession::class, 
        ClassroomMessage::class, TutorProfile::class, WalletTransaction::class,
        CourseEnrollmentDetails::class, SystemLog::class, AssignedCourse::class,
        Attendance::class, RoleDiscount::class, UserTheme::class
    ], 
    version = 31, // Bumped to 31 for Theme Persistence
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun audioBookDao(): AudioBookDao
    abstract fun courseDao(): CourseDao
    abstract fun gearDao(): GearDao
    abstract fun userDao(): UserDao
    abstract fun classroomDao(): ClassroomDao
    abstract fun auditDao(): AuditDao
    abstract fun assignedCourseDao(): AssignedCourseDao
    abstract fun userThemeDao(): UserThemeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration 30 to 31: Adds lastSelectedTheme column to user_themes.
         */
        private val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_themes` ADD COLUMN `lastSelectedTheme` TEXT NOT NULL DEFAULT 'DARK'")
            }
        }

        // Migration 29 to 30: Cleans up users_local and ensures user_themes exists
        private val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Recreate users_local to remove unwanted theme columns
                db.execSQL("CREATE TABLE IF NOT EXISTS `users_local_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `photoUrl` TEXT, `title` TEXT, `address` TEXT, `phoneNumber` TEXT, `selectedPaymentMethod` TEXT, `balance` REAL NOT NULL, `role` TEXT NOT NULL, `discountPercent` REAL NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO users_local_new (id, name, email, photoUrl, title, address, phoneNumber, selectedPaymentMethod, balance, role, discountPercent) SELECT id, name, email, photoUrl, title, address, phoneNumber, selectedPaymentMethod, balance, role, discountPercent FROM users_local")
                db.execSQL("DROP TABLE users_local")
                db.execSQL("ALTER TABLE users_local_new RENAME TO users_local")

                // 2. Ensure user_themes exists
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_themes` (
                        `userId` TEXT NOT NULL, 
                        `isCustomThemeEnabled` INTEGER NOT NULL DEFAULT 0, 
                        `customPrimary` INTEGER, 
                        `customOnPrimary` INTEGER, 
                        `customPrimaryContainer` INTEGER, 
                        `customOnPrimaryContainer` INTEGER, 
                        `customSecondary` INTEGER, 
                        `customOnSecondary` INTEGER, 
                        `customSecondaryContainer` INTEGER, 
                        `customOnSecondaryContainer` INTEGER, 
                        `customTertiary` INTEGER, 
                        `customOnTertiary` INTEGER, 
                        `customTertiaryContainer` INTEGER, 
                        `customOnTertiaryContainer` INTEGER, 
                        `customBackground` INTEGER, 
                        `customOnBackground` INTEGER, 
                        `customSurface` INTEGER, 
                        `customOnSurface` INTEGER, 
                        `customIsDark` INTEGER NOT NULL DEFAULT 1, 
                        PRIMARY KEY(`userId`)
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_themes` (`userId` TEXT NOT NULL, `isCustomThemeEnabled` INTEGER NOT NULL DEFAULT 0, `customPrimary` INTEGER, `customOnPrimary` INTEGER, `customPrimaryContainer` INTEGER, `customOnPrimaryContainer` INTEGER, `customSecondary` INTEGER, `customOnSecondary` INTEGER, `customSecondaryContainer` INTEGER, `customOnSecondaryContainer` INTEGER, `customTertiary` INTEGER, `customOnTertiary` INTEGER, `customTertiaryContainer` INTEGER, `customOnTertiaryContainer` INTEGER, `customBackground` INTEGER, `customOnBackground` INTEGER, `customSurface` INTEGER, `customOnSurface` INTEGER, `customIsDark` INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(`userId`))")
            }
        }

        private val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // This migration is now redundant but kept for history
                db.execSQL("ALTER TABLE users_local ADD COLUMN isCustomThemeEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customPrimary INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnPrimary INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customPrimaryContainer INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnPrimaryContainer INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customSecondary INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnSecondary INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customSecondaryContainer INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnSecondaryContainer INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customTertiary INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnTertiary INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customTertiaryContainer INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnTertiaryContainer INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customBackground INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnBackground INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customSurface INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customOnSurface INTEGER")
                db.execSQL("ALTER TABLE users_local ADD COLUMN customIsDark INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `global_discounts` (`role` TEXT NOT NULL, `discountPercent` REAL NOT NULL, PRIMARY KEY(`role`))")
                db.execSQL("ALTER TABLE users_local ADD COLUMN discountPercent REAL NOT NULL DEFAULT 0.0")
            }
        }

        // --- Historical Migrations ---
        private val MIGRATION_1_2 = object : Migration(1, 2) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE gear ADD COLUMN sizes TEXT NOT NULL DEFAULT 'M'"); db.execSQL("ALTER TABLE gear ADD COLUMN colors TEXT NOT NULL DEFAULT 'Default'"); db.execSQL("ALTER TABLE gear ADD COLUMN stockCount INTEGER NOT NULL DEFAULT 10"); db.execSQL("ALTER TABLE gear ADD COLUMN brand TEXT NOT NULL DEFAULT 'Wrexham University'"); db.execSQL("ALTER TABLE gear ADD COLUMN isAvailable INTEGER NOT NULL DEFAULT 1") } }
        private val MIGRATION_2_3 = object : Migration(2, 3) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE gear ADD COLUMN material TEXT NOT NULL DEFAULT 'Mixed Fibers'"); db.execSQL("ALTER TABLE gear ADD COLUMN sku TEXT NOT NULL DEFAULT 'WREX-GEAR-000'"); db.execSQL("ALTER TABLE gear ADD COLUMN originalPrice REAL NOT NULL DEFAULT 0.0"); db.execSQL("ALTER TABLE gear ADD COLUMN isFeatured INTEGER NOT NULL DEFAULT 0"); db.execSQL("ALTER TABLE gear ADD COLUMN productTags TEXT NOT NULL DEFAULT ''") } }
        private val MIGRATION_3_4 = object : Migration(3, 4) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE gear ADD COLUMN secondaryImageUrl TEXT DEFAULT NULL") } }
        private val MIGRATION_4_5 = object : Migration(4, 5) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE purchases ADD COLUMN orderConfirmation TEXT DEFAULT NULL") } }
        private val MIGRATION_5_6 = object : Migration(5, 6) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `invoices` (`invoiceNumber` TEXT NOT NULL, `userId` TEXT NOT NULL, `productId` TEXT NOT NULL, `itemTitle` TEXT NOT NULL, `itemCategory` TEXT NOT NULL, `pricePaid` REAL NOT NULL, `quantity` INTEGER NOT NULL, `purchasedAt` INTEGER NOT NULL, `paymentMethod` TEXT NOT NULL, `billingName` TEXT NOT NULL, `billingEmail` TEXT NOT NULL, `billingAddress` TEXT, PRIMARY KEY(`invoiceNumber`))") } }
        private val MIGRATION_6_7 = object : Migration(6, 7) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE purchases ADD COLUMN mainCategory TEXT NOT NULL DEFAULT 'Books'"); db.execSQL("ALTER TABLE purchases ADD COLUMN totalPricePaid REAL NOT NULL DEFAULT 0.0"); db.execSQL("ALTER TABLE purchases ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1") } }
        private val MIGRATION_7_8 = object : Migration(7, 8) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE invoices ADD COLUMN itemVariant TEXT DEFAULT NULL"); db.execSQL("ALTER TABLE invoices ADD COLUMN discountApplied REAL NOT NULL DEFAULT 0.0"); db.execSQL("ALTER TABLE invoices ADD COLUMN orderReference TEXT DEFAULT NULL") } }
        private val MIGRATION_8_9 = object : Migration(8, 9) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `purchases_new` (`purchaseId` TEXT NOT NULL, `userId` TEXT NOT NULL, `productId` TEXT NOT NULL, `mainCategory` TEXT NOT NULL, `purchasedAt` INTEGER NOT NULL, `paymentMethod` TEXT NOT NULL, `amountFromWallet` REAL NOT NULL, `amountPaidExternal` REAL NOT NULL, `totalPricePaid` REAL NOT NULL, `quantity` INTEGER NOT NULL, `orderConfirmation` TEXT, PRIMARY KEY(`purchaseId`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("DROP TABLE IF EXISTS `purchases` "); db.execSQL("ALTER TABLE `purchases_new` RENAME TO `purchases` ") } }
        private val MIGRATION_9_10 = object : Migration(9, 10) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE notifications ADD COLUMN productId TEXT NOT NULL DEFAULT ''") } }
        private val MIGRATION_10_11 = object : Migration(10, 11) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE users_local ADD COLUMN phoneNumber TEXT DEFAULT NULL"); db.execSQL("CREATE TABLE IF NOT EXISTS `search_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `query` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)"); db.execSQL("CREATE TABLE IF NOT EXISTS `course_installments` (`userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `modulesPaid` INTEGER NOT NULL DEFAULT 1, `totalModules` INTEGER NOT NULL DEFAULT 4, `isFullyPaid` INTEGER NOT NULL DEFAULT 0, `lastPaymentDate` INTEGER NOT NULL, PRIMARY KEY(`userId`, `courseId`))") } }
        private val MIGRATION_11_12 = object : Migration(11, 12) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `classroom_modules` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `contentType` TEXT NOT NULL, `contentUrl` TEXT NOT NULL, `order` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `assignments` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `dueDate` INTEGER NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `grades` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `assignmentId` TEXT NOT NULL, `score` REAL NOT NULL, `feedback` TEXT, `gradedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `live_sessions` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `tutorId` TEXT NOT NULL DEFAULT '', `tutorName` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `streamUrl` TEXT NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))") } }
        private val MIGRATION_12_13 = object : Migration(12, 13) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `assignment_submissions` (`id` TEXT NOT NULL, `assignmentId` TEXT NOT NULL, `userId` TEXT NOT NULL, `content` TEXT NOT NULL, `submittedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `classroom_messages` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `receiverId` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `tutor_profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `photoUrl` TEXT, `department` TEXT NOT NULL, `officeHours` TEXT NOT NULL, `bio` TEXT NOT NULL, PRIMARY KEY(`id`))") } }
        private val MIGRATION_13_14 = object : Migration(13, 14) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `wallet_history` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `paymentMethod` TEXT NOT NULL, `description` TEXT NOT NULL, `orderReference` TEXT, PRIMARY KEY(`id`))") } }
        private val MIGRATION_14_15 = object : Migration(14, 15) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE wallet_history ADD COLUMN productId TEXT DEFAULT NULL") } }
        private val MIGRATION_15_16 = object : Migration(15, 16) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE wallet_history ADD COLUMN purchaseId TEXT DEFAULT NULL") } }
        private val MIGRATION_16_17 = object : Migration(16, 17) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `course_enrollment_details` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `lastQualification` TEXT NOT NULL, `institution` TEXT NOT NULL, `graduationYear` TEXT NOT NULL, `englishProficiencyLevel` TEXT NOT NULL, `dateOfBirth` TEXT NOT NULL, `nationality` TEXT NOT NULL, `gender` TEXT NOT NULL, `emergencyContactName` TEXT NOT NULL, `emergencyContactPhone` TEXT NOT NULL, `motivationalText` TEXT NOT NULL, `cvFileName` TEXT, `portfolioUrl` TEXT, `specialSupportRequirements` TEXT, `status` TEXT NOT NULL, `submittedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))") } }
        private val MIGRATION_17_18 = object : Migration(17, 18) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `system_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `userName` TEXT NOT NULL, `action` TEXT NOT NULL, `targetId` TEXT NOT NULL, `details` TEXT NOT NULL, `logType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)") } }
        private val MIGRATION_18_19 = object : Migration(18, 19) { override fun migrate(db: SupportSQLiteDatabase) { } }
        private val MIGRATION_19_20 = object : Migration(19, 20) { override fun migrate(db: SupportSQLiteDatabase) { } }
        private val MIGRATION_20_21 = object : Migration(20, 21) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `assigned_courses` (`tutorId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `assignedAt` INTEGER NOT NULL, PRIMARY KEY(`tutorId`, `courseId`))") } }
        private val MIGRATION_21_22 = object : Migration(21, 22) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE users_local ADD COLUMN title TEXT DEFAULT NULL"); db.execSQL("ALTER TABLE tutor_profiles ADD COLUMN title TEXT DEFAULT NULL") } }
        private val MIGRATION_22_23 = object : Migration(22, 23) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE assignments ADD COLUMN moduleId TEXT NOT NULL DEFAULT ''") } }
        private val MIGRATION_23_24 = object : Migration(23, 24) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `attendance` (`userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `date` INTEGER NOT NULL, `isPresent` INTEGER NOT NULL, PRIMARY KEY(`userId`, `courseId`, `date`))") } }
        private val MIGRATION_24_25 = object : Migration(24, 25) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE assignments ADD COLUMN allowedFileTypes TEXT NOT NULL DEFAULT 'PDF,DOCX,ZIP'") } }
        private val MIGRATION_25_26 = object : Migration(25, 26) { override fun migrate(db: SupportSQLiteDatabase) { } }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glyndwr_database.db"
                )
                .createFromAsset("database/glyndwr_database.db")
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, 
                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, 
                    MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                    MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                    MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
                    MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25,
                    MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29,
                    MIGRATION_29_30, MIGRATION_30_31
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
