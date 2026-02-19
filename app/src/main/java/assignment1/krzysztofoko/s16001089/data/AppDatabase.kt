/**
 * AppDatabase.kt
 *
 * This file defines the main Room database configuration for the Glyndwr application.
 * It serves as the central access point for the persisted data, grouping all
 * entities and providing access to their respective DAOs.
 *
 * Key Responsibilities:
 * 1. Database versioning and entity registration for all application modules.
 * 2. Migration logic for handling schema updates (versions 1 to 34).
 * 3. Singleton pattern implementation for efficient database instance management.
 * 4. Pre-population from the asset-based template "glyndwr_database.db".
 */

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
        Attendance::class, RoleDiscount::class, UserTheme::class, EnrollmentHistory::class
    ], 
    version = 34, // Current database version, incremented for schema changes
    exportSchema = false // Disables schema export to a JSON file
)
abstract class AppDatabase : RoomDatabase() {
    // Abstract methods to retrieve DAO instances for various entities
    abstract fun bookDao(): BookDao // Access to Book data
    abstract fun audioBookDao(): AudioBookDao // Access to AudioBook data
    abstract fun courseDao(): CourseDao // Access to Course data
    abstract fun gearDao(): GearDao // Access to Gear/Product data
    abstract fun userDao(): UserDao // Access to User profile data
    abstract fun classroomDao(): ClassroomDao // Access to Classroom and module data
    abstract fun auditDao(): AuditDao // Access to System logs and audit trails
    abstract fun assignedCourseDao(): AssignedCourseDao // Access to Tutor-Course assignments
    abstract fun userThemeDao(): UserThemeDao // Access to User-specific theme settings

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null // Thread-safe singleton instance

        /**
         * Migration 33 to 34: Creates the enrollment_history table.
         */
        private val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Creates a new table for tracking user enrollment status changes over time
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enrollment_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `courseId` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `previousCourseId` TEXT
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration 32 to 33: Adds isWithdrawal column to course_enrollment_details.
         */
        private val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Adds a flag to track if an enrollment record represents a withdrawal request
                db.execSQL("ALTER TABLE `course_enrollment_details` ADD COLUMN `isWithdrawal` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Migration 31 to 32: Adds requestedCourseId column to course_enrollment_details.
         */
        private val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Adds field to store the ID of a course requested during change-of-course processes
                db.execSQL("ALTER TABLE `course_enrollment_details` ADD COLUMN `requestedCourseId` TEXT")
            }
        }

        /**
         * Migration 30 to 31: Adds lastSelectedTheme column to user_themes.
         */
        private val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Persists the user's last chosen theme mode (e.g., DARK, LIGHT, SYSTEM)
                db.execSQL("ALTER TABLE `user_themes` ADD COLUMN `lastSelectedTheme` TEXT NOT NULL DEFAULT 'DARK'")
            }
        }

        // Migration 29 to 30: Cleans up users_local and ensures user_themes exists
        private val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Recreate users_local to remove redundant theme columns originally stored there
                db.execSQL("CREATE TABLE IF NOT EXISTS `users_local_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `photoUrl` TEXT, `title` TEXT, `address` TEXT, `phoneNumber` TEXT, `selectedPaymentMethod` TEXT, `balance` REAL NOT NULL, `role` TEXT NOT NULL, `discountPercent` REAL NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO users_local_new (id, name, email, photoUrl, title, address, phoneNumber, selectedPaymentMethod, balance, role, discountPercent) SELECT id, name, email, photoUrl, title, address, phoneNumber, selectedPaymentMethod, balance, role, discountPercent FROM users_local")
                db.execSQL("DROP TABLE users_local") // Remove old table
                db.execSQL("ALTER TABLE users_local_new RENAME TO users_local") // Rename new table to original

                // 2. Ensure user_themes exists as the primary store for personalization
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

        // Migration 28 to 29: Introduces standalone user_themes table
        private val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_themes` (`userId` TEXT NOT NULL, `isCustomThemeEnabled` INTEGER NOT NULL DEFAULT 0, `customPrimary` INTEGER, `customOnPrimary` INTEGER, `customPrimaryContainer` INTEGER, `customOnPrimaryContainer` INTEGER, `customSecondary` INTEGER, `customOnSecondary` INTEGER, `customSecondaryContainer` INTEGER, `customOnSecondaryContainer` INTEGER, `customTertiary` INTEGER, `customOnTertiary` INTEGER, `customTertiaryContainer` INTEGER, `customOnTertiaryContainer` INTEGER, `customBackground` INTEGER, `customOnBackground` INTEGER, `customSurface` INTEGER, `customOnSurface` INTEGER, `customIsDark` INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(`userId`))")
            }
        }

        // Migration 27 to 28: Historical attempt to put theme columns in users_local
        private val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
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

        // Migration 26 to 27: Adds role-based global discounts
        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `global_discounts` (`role` TEXT NOT NULL, `discountPercent` REAL NOT NULL, PRIMARY KEY(`role`))")
                db.execSQL("ALTER TABLE users_local ADD COLUMN discountPercent REAL NOT NULL DEFAULT 0.0")
            }
        }

        // --- Historical Migrations (1-25) ---
        // Version 1 to 2: Basic Gear enhancements
        private val MIGRATION_1_2 = object : Migration(1, 2) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE gear ADD COLUMN sizes TEXT NOT NULL DEFAULT 'M'"); db.execSQL("ALTER TABLE gear ADD COLUMN colors TEXT NOT NULL DEFAULT 'Default'"); db.execSQL("ALTER TABLE gear ADD COLUMN stockCount INTEGER NOT NULL DEFAULT 10"); db.execSQL("ALTER TABLE gear ADD COLUMN brand TEXT NOT NULL DEFAULT 'Wrexham University'"); db.execSQL("ALTER TABLE gear ADD COLUMN isAvailable INTEGER NOT NULL DEFAULT 1") } }
        // Version 2 to 3: Detailed Gear product info
        private val MIGRATION_2_3 = object : Migration(2, 3) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE gear ADD COLUMN material TEXT NOT NULL DEFAULT 'Mixed Fibers'"); db.execSQL("ALTER TABLE gear ADD COLUMN sku TEXT NOT NULL DEFAULT 'WREX-GEAR-000'"); db.execSQL("ALTER TABLE gear ADD COLUMN originalPrice REAL NOT NULL DEFAULT 0.0"); db.execSQL("ALTER TABLE gear ADD COLUMN isFeatured INTEGER NOT NULL DEFAULT 0"); db.execSQL("ALTER TABLE gear ADD COLUMN productTags TEXT NOT NULL DEFAULT ''") } }
        // Version 3 to 4: Adds secondary images
        private val MIGRATION_3_4 = object : Migration(3, 4) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE gear ADD COLUMN secondaryImageUrl TEXT DEFAULT NULL") } }
        // Version 4 to 5: Adds order confirmation to purchases
        private val MIGRATION_4_5 = object : Migration(4, 5) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE purchases ADD COLUMN orderConfirmation TEXT DEFAULT NULL") } }
        // Version 5 to 6: Introduces invoice system
        private val MIGRATION_5_6 = object : Migration(5, 6) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `invoices` (`invoiceNumber` TEXT NOT NULL, `userId` TEXT NOT NULL, `productId` TEXT NOT NULL, `itemTitle` TEXT NOT NULL, `itemCategory` TEXT NOT NULL, `pricePaid` REAL NOT NULL, `quantity` INTEGER NOT NULL, `purchasedAt` INTEGER NOT NULL, `paymentMethod` TEXT NOT NULL, `billingName` TEXT NOT NULL, `billingEmail` TEXT NOT NULL, `billingAddress` TEXT, PRIMARY KEY(`invoiceNumber`))") } }
        // Version 6 to 7: purchase category and pricing updates
        private val MIGRATION_6_7 = object : Migration(6, 7) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE purchases ADD COLUMN mainCategory TEXT NOT NULL DEFAULT 'Books'"); db.execSQL("ALTER TABLE purchases ADD COLUMN totalPricePaid REAL NOT NULL DEFAULT 0.0"); db.execSQL("ALTER TABLE purchases ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1") } }
        // Version 7 to 8: invoice variant and discount details
        private val MIGRATION_7_8 = object : Migration(7, 8) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE invoices ADD COLUMN itemVariant TEXT DEFAULT NULL"); db.execSQL("ALTER TABLE invoices ADD COLUMN discountApplied REAL NOT NULL DEFAULT 0.0"); db.execSQL("ALTER TABLE invoices ADD COLUMN orderReference TEXT DEFAULT NULL") } }
        // Version 8 to 9: schema overhaul for purchases and notifications
        private val MIGRATION_8_9 = object : Migration(8, 9) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `purchases_new` (`purchaseId` TEXT NOT NULL, `userId` TEXT NOT NULL, `productId` TEXT NOT NULL, `mainCategory` TEXT NOT NULL, `purchasedAt` INTEGER NOT NULL, `paymentMethod` TEXT NOT NULL, `amountFromWallet` REAL NOT NULL, `amountPaidExternal` REAL NOT NULL, `totalPricePaid` REAL NOT NULL, `quantity` INTEGER NOT NULL, `orderConfirmation` TEXT, PRIMARY KEY(`purchaseId`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("DROP TABLE IF EXISTS `purchases` "); db.execSQL("ALTER TABLE `purchases_new` RENAME TO `purchases` ") } }
        // Version 9 to 10: links notifications to products
        private val MIGRATION_9_10 = object : Migration(9, 10) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE notifications ADD COLUMN productId TEXT NOT NULL DEFAULT ''") } }
        // Version 10 to 11: Adds phone, search history, and course installments
        private val MIGRATION_10_11 = object : Migration(10, 11) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE users_local ADD COLUMN phoneNumber TEXT DEFAULT NULL"); db.execSQL("CREATE TABLE IF NOT EXISTS `search_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `query` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)"); db.execSQL("CREATE TABLE IF NOT EXISTS `course_installments` (`userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `modulesPaid` INTEGER NOT NULL DEFAULT 1, `totalModules` INTEGER NOT NULL DEFAULT 4, `isFullyPaid` INTEGER NOT NULL DEFAULT 0, `lastPaymentDate` INTEGER NOT NULL, PRIMARY KEY(`userId`, `courseId`))") } }
        // Version 11 to 12: Adds classroom modules, assignments, grades, and sessions
        private val MIGRATION_11_12 = object : Migration(11, 12) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `classroom_modules` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `contentType` TEXT NOT NULL, `contentUrl` TEXT NOT NULL, `order` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `assignments` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `dueDate` INTEGER NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `grades` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `assignmentId` TEXT NOT NULL, `score` REAL NOT NULL, `feedback` TEXT, `gradedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `live_sessions` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `tutorId` TEXT NOT NULL DEFAULT '', `tutorName` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `streamUrl` TEXT NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))") } }
        // Version 12 to 13: Adds submissions, messages, and tutor profiles
        private val MIGRATION_12_13 = object : Migration(12, 13) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `assignment_submissions` (`id` TEXT NOT NULL, `assignmentId` TEXT NOT NULL, `userId` TEXT NOT NULL, `content` TEXT NOT NULL, `submittedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `classroom_messages` (`id` TEXT NOT NULL, `courseId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `receiverId` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, PRIMARY KEY(`id`))"); db.execSQL("CREATE TABLE IF NOT EXISTS `tutor_profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `photoUrl` TEXT, `department` TEXT NOT NULL, `officeHours` TEXT NOT NULL, `bio` TEXT NOT NULL, PRIMARY KEY(`id`))") } }
        // Version 13 to 14: Adds wallet transaction history
        private val MIGRATION_13_14 = object : Migration(13, 14) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `wallet_history` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `paymentMethod` TEXT NOT NULL, `description` TEXT NOT NULL, `orderReference` TEXT, PRIMARY KEY(`id`))") } }
        // Version 14 to 15: link wallet to products
        private val MIGRATION_14_15 = object : Migration(14, 15) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE wallet_history ADD COLUMN productId TEXT DEFAULT NULL") } }
        // Version 15 to 16: link wallet to specific purchases
        private val MIGRATION_15_16 = object : Migration(15, 16) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE wallet_history ADD COLUMN purchaseId TEXT DEFAULT NULL") } }
        // Version 16 to 17: Adds complex enrollment details
        private val MIGRATION_16_17 = object : Migration(16, 17) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `course_enrollment_details` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `lastQualification` TEXT NOT NULL, `institution` TEXT NOT NULL, `graduationYear` TEXT NOT NULL, `englishProficiencyLevel` TEXT NOT NULL, `dateOfBirth` TEXT NOT NULL, `nationality` TEXT NOT NULL, `gender` TEXT NOT NULL, `emergencyContactName` TEXT NOT NULL, `emergencyContactPhone` TEXT NOT NULL, `motivationalText` TEXT NOT NULL, `cvFileName` TEXT, `portfolioUrl` TEXT, `specialSupportRequirements` TEXT, `status` TEXT NOT NULL, `submittedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))") } }
        // Version 17 to 18: Adds logging system
        private val MIGRATION_17_18 = object : Migration(17, 18) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `system_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `userName` TEXT NOT NULL, `action` TEXT NOT NULL, `targetId` TEXT NOT NULL, `details` TEXT NOT NULL, `logType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)") } }
        private val MIGRATION_18_19 = object : Migration(18, 19) { override fun migrate(db: SupportSQLiteDatabase) { } } // No-op migration
        private val MIGRATION_19_20 = object : Migration(19, 20) { override fun migrate(db: SupportSQLiteDatabase) { } } // No-op migration
        // Version 20 to 21: Assigned courses for tutors
        private val MIGRATION_20_21 = object : Migration(20, 21) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `assigned_courses` (`tutorId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `assignedAt` INTEGER NOT NULL, PRIMARY KEY(`tutorId`, `courseId`))") } }
        // Version 21 to 22: Title fields for users and tutors
        private val MIGRATION_21_22 = object : Migration(21, 22) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE users_local ADD COLUMN title TEXT DEFAULT NULL"); db.execSQL("ALTER TABLE tutor_profiles ADD COLUMN title TEXT DEFAULT NULL") } }
        // Version 22 to 23: module link for assignments
        private val MIGRATION_22_23 = object : Migration(22, 23) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE assignments ADD COLUMN moduleId TEXT NOT NULL DEFAULT ''") } }
        // Version 23 to 24: Attendance tracking
        private val MIGRATION_23_24 = object : Migration(23, 24) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `attendance` (`userId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `date` INTEGER NOT NULL, `isPresent` INTEGER NOT NULL, PRIMARY KEY(`userId`, `courseId`, `date`))") } }
        // Version 24 to 25: submission file type constraints
        private val MIGRATION_24_25 = object : Migration(24, 25) { override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("ALTER TABLE assignments ADD COLUMN allowedFileTypes TEXT NOT NULL DEFAULT 'PDF,DOCX,ZIP'") } }
        private val MIGRATION_25_26 = object : Migration(25, 26) { override fun migrate(db: SupportSQLiteDatabase) { } } // No-op migration

        /**
         * Provides the singleton instance of the AppDatabase.
         * Creates and initializes the database if it hasn't been instantiated yet.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // Lock to ensure single creation in multi-threaded environment
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glyndwr_database.db" // Database file name
                )
                .createFromAsset("database/glyndwr_database.db") // Initial seed data from assets
                .addMigrations( // Register all migration paths
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, 
                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, 
                    MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                    MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                    MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
                    MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25,
                    MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29,
                    MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33,
                    MIGRATION_33_34
                )
                .fallbackToDestructiveMigration() // Recreates tables if migrations are missing
                .build() // Finalize database creation
                INSTANCE = instance
                instance // Return the created instance
            }
        }
    }
}
