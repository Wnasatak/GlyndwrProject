package assignment1.krzysztofoko.s16001089

import assignment1.krzysztofoko.s16001089.data.Book

/**
 * Global Constants for the Glyndŵr Project.
 * Centralizes strings and configuration to ensure consistency across the UI and logic.
 */
object AppConstants {
    // --- Application Branding ---
    const val VERSION_NAME = "0.17.948 Under Development"
    const val INSTITUTION = "Wrexham Glyndŵr University"
    const val DEVELOPER_NAME = "Krzysztof Oko"
    const val STUDENT_ID = "S16001089"
    const val PROJECT_INFO = "Assignment 1 - CONL709 Mobile App Development"
    const val APP_NAME = "Glyndŵr Store"

    // --- Unified Categories ---
    const val CAT_COURSES = "University Courses"
    const val CAT_GEAR = "University Gear"
    const val CAT_BOOKS = "Books"
    const val CAT_AUDIOBOOKS = "Audio Books"
    const val CAT_FINANCE = "Finance"
    const val CAT_FREE = "Free"
    const val CAT_ALL = "All"

    val MainCategories = listOf(CAT_ALL, CAT_FREE, CAT_COURSES, CAT_GEAR, CAT_BOOKS, CAT_AUDIOBOOKS)

    val SubCategoriesMap = mapOf(
        CAT_BOOKS to listOf("All Genres", "Technology", "Cooking", "Fantasy", "Mystery", "Self-Help"),
        CAT_AUDIOBOOKS to listOf("All Genres", "Self-Help", "Technology", "Cooking", "Mystery"),
        CAT_COURSES to listOf("All Departments", "Science", "Business", "Technology")
    )

    // --- Common Status Labels ---
    const val LABEL_ENROLLED = "Enrolled"
    const val LABEL_PICKED_UP = "Picked Up"
    const val LABEL_PURCHASED = "Purchased"
    const val LABEL_IN_LIBRARY = "In Library"
    const val LABEL_FREE = "FREE"
    const val LABEL_PAID = "Paid"
    const val LABEL_NEW = "NEW"
    const val LABEL_MY_COURSES = "MY COURSES"
    
    // --- UI Strings: Titles ---
    const val TITLE_MEMBER_LOGIN = "Member Login"
    const val TITLE_REGISTRATION = "Registration"
    const val TITLE_WELCOME_BACK = "Welcome Back"
    const val TITLE_MEMBER_REGISTRATION = "Member Registration"
    const val TITLE_IDENTITY_VERIFICATION = "Identity Verification"
    const val TITLE_EMAIL_VERIFICATION = "Email Verification"
    const val TITLE_RESET_PASSWORD = "Reset Password"
    const val TITLE_CHECK_INBOX = "Check your Inbox"
    const val TITLE_ACCOUNT_RECOVERY = "Account Recovery"
    const val TITLE_SECURITY_VERIFICATION = "Security Verification"
    const val TITLE_DISCOVER_LIBRARY = "Discover Your Library"
    const val TITLE_SIGN_IN_REQUIRED = "Sign In Required"
    const val TITLE_ENROLLMENT_LOCKED = "Enrollment Locked"
    const val TITLE_IDENTITY_VERIFIED = "Identity Verified!"
    const val TITLE_STUDENT_REGISTRATION = "Student Registration"
    const val TITLE_PROFILE_SETTINGS = "Profile Settings"
    const val TITLE_ADMIN_HUB = "Admin Hub"
    const val TITLE_TUTOR_HUB = "Tutor Hub"
    const val TITLE_FACULTY = "Teacher"
    const val TITLE_STUDENT_HUB = "Student Hub"
    const val TITLE_LIGHT_MODE = "Light Mode"
    const val TITLE_DARK_MODE = "Dark Mode"
    const val TITLE_ADMIN_PANEL = "Admin Panel"
    const val TITLE_TUTOR_PANEL = "Tutor Panel"
    const val TITLE_CONTINUE_READING = "Continue Reading"
    const val TITLE_RECENT_ACTIVITY = "Your Recent Activity"
    const val TITLE_RECENTLY_LIKED = "Recently Liked"
    const val TITLE_YOUR_COLLECTION = "Your Collection"
    const val TITLE_ABOUT_APP = "About App"
    const val TITLE_DEVELOPER_DETAILS = "Developer Details"
    const val TITLE_HOW_TO_USE = "How to Use App"
    const val TITLE_WHATS_NEW = "What's New"
    const val TITLE_FUTURE_ROADMAP = "Future Roadmap"
    const val TITLE_UPCOMING_IMPROVEMENTS = "Upcoming Improvements"
    const val TITLE_LATEST_UPDATES = "Latest Updates"
    const val TITLE_WELCOME_STORE = "Welcome to Glyndŵr Store!"
    const val TITLE_NOTIFICATIONS = "Notifications"
    const val TITLE_MESSAGES = "Messages"
    const val TITLE_SIGNED_OUT = "Signed Out"
    const val TITLE_DELETE_REVIEW = "Delete Review"
    const val TITLE_SAVE_CHANGES = "Save Changes"
    const val TITLE_LOG_OFF = "Log Off"
    const val TITLE_DEMO_MODE = "Demo Mode"
    const val TITLE_REMOVE_LIBRARY = "Remove from Library?"
    const val TITLE_STORE = "Store"
    const val TITLE_MORE_OPTIONS = "More Options"
    const val TITLE_OPTIONS = "Options"
    const val TITLE_ADD_TO_LIBRARY = "Add to Library"
    const val TITLE_COURSE_ENROLLMENT = "Course Enrollment"
    const val TITLE_ITEM_RESERVATION = "Item Reservation"
    const val TITLE_OFFICIAL_INVOICE = "Official Invoice"
    const val TITLE_ISSUED_TO = "ISSUED TO"
    const val TITLE_INVOICE_NO = "INVOICE NO"
    const val TITLE_PURCHASED_ITEM = "PURCHASED ITEM"
    const val TITLE_BILL_TO = "BILL TO"
    const val TITLE_DESCRIPTION = "Description"
    const val TITLE_AMOUNT = "Amount"
    const val TITLE_MY_APPLICATIONS = "My Applications"
    const val TITLE_BOOK_DETAILS = "Item Details"
    const val TITLE_AUDIO_DETAILS = "Audiobook Details"
    const val TITLE_COURSE_DETAILS = "Course Details"
    const val TITLE_GEAR_DETAILS = "Gear Details"
    const val TITLE_CLASSROOM = "Classroom"
    const val TITLE_SIMILAR_PRODUCTS = "Similar Products"
    const val TITLE_ORDER_REVIEW = "Order Review"
    const val TITLE_BILLING_INFO = "Billing Info"
    const val TITLE_PAYMENT = "Payment"
    const val TITLE_PROCESSING = "Processing"
    const val TITLE_CONFIRMATION_DETAILS = "Confirmation Details"
    const val TITLE_PAYMENT_PLAN = "Select Payment Plan"

    // --- UI Strings: Buttons ---
    const val BTN_SIGN_IN = "Sign In"
    const val BTN_CREATE_ACCOUNT = "Create Account"
    const val BTN_GOOGLE_LOGIN = "Google Login"
    const val BTN_GOOGLE_SIGNUP = "Google Sign up"
    const val BTN_VERIFY_IDENTITY = "Verify Identity"
    const val BTN_RESEND_CODE = "Resend Code"
    const val BTN_VERIFICATION_DONE = "Verification Done"
    const val BTN_BACK_TO_LOGIN = "Back to Login"
    const val BTN_SEND_RESET_LINK = "Send Reset Link"
    const val BTN_RETURN_TO_LOGIN = "Return to Login"
    const val BTN_LOG_OUT = "Sign Off"
    const val BTN_SAVE_PROFILE = "Save General Profile"
    const val BTN_READ_NOW = "Read Now"
    const val BTN_LISTEN_NOW = "Listen Now"
    const val BTN_ENTER_CLASSROOM = "Enter Classroom"
    const val BTN_ORDER_NOW = "Order now!"
    const val BTN_BUY_NOW = "Buy Now!"
    const val BTN_ENROLL_NOW = "Enroll Now"
    const val BTN_ENROLL_FREE = "Enroll for Free"
    const val BTN_ADD_TO_LIBRARY = "Add to Library"
    const val BTN_PICKUP_FREE = "Pick it up for FREE"
    const val BTN_VIEW_INVOICE = "View Invoice"
    const val BTN_PICKUP_INFO = "Where to pickup?"
    const val BTN_DOWNLOAD_PDF = "Download PDF Invoice"
    const val BTN_GO_BACK = "Go Back"
    const val BTN_CLOSE = "Close"
    const val BTN_PREVIOUS = "Previous"
    const val BTN_NEXT = "Next"
    const val BTN_CANCEL = "Cancel"
    const val BTN_PAY_NOW = "Pay Now"
    const val BTN_CONTINUE = "Continue"
    const val BTN_BACK = "Back"
    const val BTN_GOT_IT = "Got it!"
    const val BTN_EXCITING_STUFF = "Exciting stuff!"
    const val BTN_VIEW_PRODUCT_DETAILS = "View Product Details"
    const val BTN_DELETE_NOTIFICATION = "Delete Notification"
    const val BTN_DISMISS = "Dismiss"
    const val BTN_SAVE = "Save"
    const val BTN_DISCARD = "Discard"
    const val BTN_REMOVE = "Remove"
    const val BTN_LOG_OFF = "Sign Off"
    const val BTN_CLEAR_ALL = "Clear All"
    const val BTN_DELETE = "Delete"
    const val BTN_CONTINUE_HOME = "Continue to Home"
    const val BTN_TOP_UP = "Top Up"
    const val BTN_EXPLORE_STORE = "Explore Store"
    const val BTN_SIGN_IN_REGISTER = "Sign in / Register"
    const val BTN_SIGN_IN_ENROLL = "Sign in to Enroll"
    const val BTN_SIGN_IN_SHOP = "Sign In to Shop"
    
    // --- Text Content & Sections ---
    const val SECTION_ABOUT_ITEM = "About this item"
    const val SECTION_ABOUT_AUDIO = "About this audiobook"
    const val SECTION_DESCRIPTION_COURSE = "Course Description"
    const val SECTION_DESCRIPTION_GEAR = "Description"
    const val TEXT_BY = "by"
    const val TEXT_NARRATED_BY = "Narrated by"
    const val TEXT_DEPARTMENT = "Department"
    const val TEXT_ACADEMIC_MATERIAL = "Academic Material"
    const val TEXT_AUDIO_CONTENT = "Audio Content"
    const val TEXT_INSTALLMENTS_AVAILABLE = "Installments Available"
    const val TEXT_READING = "Reading..."
    const val TEXT_STUDENT_DISCOUNT = "STUDENT PRICE (-10%)"
    const val TEXT_STUDENT_RATE = "STUDENT RATE (-10%)"
    const val TEXT_DEFAULT = "Default"
    const val TEXT_EMAIL = "Email"
    const val TEXT_STUDENT_ID = "Student ID"
    const val TEXT_STUDENT = "Student"
    const val TEXT_ALL_CAUGHT_UP = "All caught up!"
    const val TEXT_REDIRECTING = "Redirecting in"
    const val TEXT_SECONDS = "seconds..."
    const val TEXT_SMTP_IMPLEMENTED = "SMTP Verification Implemented!"
    const val TEXT_WELCOME = "Welcome"
    const val TEXT_ACCOUNT_BALANCE = "Account Balance"
    const val TEXT_ACCOUNT_BALANCE_TOPUP = "Top up your balance"
    const val TEXT_ADMIN_CONTROLS = "Admin Controls: Manage Catalog & Users"
    const val TEXT_TUTOR_CONTROLS = "Tutor Controls: Manage Courses & Students"
    const val TEXT_DIGITAL_AUDIO = "Digital Audio"
    const val TEXT_HARDCOPY = "Hardcopy"

    // --- Classroom Tabs ---
    const val TAB_MODULES = "Modules"
    const val TAB_ASSIGNMENTS = "Assignments"
    const val TAB_PERFORMANCE = "Grades"
    
    // --- Invoicing & Labels ---
    const val LABEL_SUBTOTAL = "Subtotal"
    const val LABEL_STUDENT_DISCOUNT_VAL = "Student Discount"
    const val LABEL_TOTAL_PAID = "Total Paid"
    const val LABEL_PAID_VIA = "Paid via"
    const val LABEL_REFERENCE = "Reference"
    const val LABEL_WALLET_BALANCE_APPLIED = "Wallet Balance Applied"
    const val LABEL_WALLET_USAGE = "Wallet usage"
    const val LABEL_STORE_TAGLINE = "Official University Store"
    const val LABEL_COMPUTER_GENERATED = "This is an official computer-generated document."
    const val LABEL_FULL_ACCESS = "Full Access"
    const val LABEL_MODULE_ACCESS = "Module 1 Access"
    const val LABEL_FULL_COURSE = "Full Course"
    const val LABEL_INSTALLMENT = "Installment"
    const val LABEL_PRICE = "Price"
    const val LABEL_TOTAL_AMOUNT = "Total Amount"
    const val LABEL_FULL_NAME = "Full Name"
    const val LABEL_ORDER_TOTAL = "Order Total"
    const val LABEL_FINAL = "Final"
    const val LABEL_BALANCE = "Balance"
    
    // --- System & Error Messages ---
    const val MSG_INVOICE_NOT_FOUND = "Invoice record not found."
    const val MSG_THANK_YOU_STORE = "Thank you for supporting our university store!"
    const val MSG_THANK_YOU_ACADEMIC = "Thank you for your academic purchase!"
    const val MSG_APPRECIATE_SUPPORT = "We appreciate your support of the Wrexham University community."
    const val MSG_INVOICE_SAVED = "Invoice saved to Downloads"
    const val MSG_PDF_ERROR = "Error saving PDF"
    const val MSG_WALLET_TOPUP_SUCCESS = "added to your wallet!"
    const val MSG_SYNC_DATABASE = "Synchronising with database..."
    const val MSG_SYNC_COMPLETE = "Database synchronized! ✓"
    const val MSG_ITEM_NOT_FOUND = "Item details not available."
    const val MSG_AUDIOBOOK_NOT_FOUND = "Audiobook not found."
    const val MSG_COURSE_NOT_FOUND = "Course details not found."
    const val MSG_NO_RECENTLY_VIEWED = "No recently viewed items yet."
    const val MSG_NO_RECENT_REVIEWS = "No recent reviews."
    const val MSG_FAVORITES_EMPTY = "Your favorites list is empty."
    const val MSG_INSUFFICIENT_FUNDS = "Insufficient funds. Select another method or top up your wallet."
    const val MSG_EMPTY_NOTIFICATIONS = "Your notifications inbox is currently empty. Check back later for order updates and news!"
    const val MSG_LIBRARY_EMPTY = "Your library is empty"
    const val MSG_GET_ITEMS_PROMPT = "Get books, courses or gear to see them here."
    const val MSG_PURCHASE_SUCCESS = "Purchase successful! Item added to your library."
    const val MSG_ENROLL_SUCCESS = "Enrollment successful!"
    const val MSG_ENROLL_FREE_SUCCESS = "You have successfully enrolled in your free course!"
    const val MSG_ENROLL_PAID_SUCCESS = "You have successfully enrolled in your course!"
    const val MSG_SIGN_IN_PROMPT_BOOK = "Sign in to add this item to your library."
    const val MSG_SIGN_IN_PROMPT_AUDIO = "Sign in to add this audiobook to your library and listen."
    const val MSG_SIGN_IN_PROMPT_COURSE = "Please sign in to enroll in this university course."
    const val MSG_THANKS_REVIEW = "Thanks for your review!"
    const val MSG_REMOVED_LIBRARY = "Removed from library"
    const val MSG_ADDED_TO_LIBRARY = "Added to your library!"
    const val MSG_REMOVED_FAVORITES = "Removed from favorites"
    const val MSG_ADDED_FAVORITES = "Added to favorites!"
    const val MSG_PASSWORD_UPDATED = "Password updated safely."
    const val MSG_NO_MODULES = "No course modules available yet."
    const val MSG_NO_ASSIGNMENTS = "No assignments found for this course."
    const val MSG_NO_GRADES = "Grades will appear here once assignments are marked."
    const val MSG_GEAR_PICKUP_SUCCESS = "Success! Please pick up your item at Student Hub."
    const val MSG_ORDER_SUCCESS = "Order successful!"
    const val MSG_PROFILE_UPDATE_SUCCESS = "Profile updated successfully!"
    const val MSG_PROFILE_UPDATE_FAILED = "Failed to update Firebase profile."
    const val MSG_AVATAR_UPDATE_SUCCESS = "Avatar updated!"
    const val MSG_AVATAR_UPDATE_FAILED = "Error saving image"
    const val MSG_NO_ADDRESS_YET = "No address added yet"
    const val MSG_IDENTITY_VERIFIED_DESC = "Your security check was successful.\\nY are now fully logged in to the Glyndwr University portal."
    const val MSG_SIGNED_OUT_DESC = "You have been securely signed out. This message will close in"
    const val MSG_REMOVE_LIBRARY_DESC = "Are you sure you want to remove this item from your library? You can always add it back later if it's still available."
    const val MSG_DELETE_REVIEW_DESC = "Are you sure you want to permanently delete your review? This action cannot be undone."
    const val MSG_SAVE_CHANGES_DESC = "Do you want to save the changes to your review?"
    const val MSG_LOG_OFF_DESC = "Are you sure you want to sign off?"
    const val MSG_DEMO_MODE_DESC = "For this demonstration, your code is provided below, but please also check your email! 😊"
    const val MSG_PAID_COURSE_LIMIT = "You can only be enrolled in one paid course at a time."

    // --- Roadmap Items ---
    const val ROADMAP_AI_TITLE = "AI-POWERED RECOMMENDATIONS"
    const val ROADMAP_AI_DESC = "Personalized item suggestions based on your viewing and purchase history using advanced machine learning models."
    const val ROADMAP_TRACKING_TITLE = "REAL-TIME TRACKING"
    const val ROADMAP_TRACKING_DESC = "Live tracking for physical university gear orders with push notifications for every step of the delivery process."
    const val ROADMAP_COMMUNITY_TITLE = "COMMUNITY HUB"
    const val ROADMAP_COMMUNITY_DESC = "Discussion forums for university courses where students can share notes, ask questions, and collaborate on projects."
    const val ROADMAP_OFFLINE_TITLE = "FULL OFFLINE MODE"
    const val ROADMAP_OFFLINE_DESC = "Enhanced offline capabilities allowing students to access all purchased courses and reading materials without any internet connection."
    const val ROADMAP_OFFLINE_TITLE_2 = "OFFLINE MODE" // Added to avoid conflict
    const val ROADMAP_PAYMENTS_TITLE = "EXTERNAL PAYMENTS"
    const val ROADMAP_PAYMENTS_DESC = "Integration with major regional banks and crypto-wallets to provide more flexibility in payment options beyond the University Account."

    // --- Version Info Items ---
    const val VER_READER_TITLE = "ENHANCED PDF READER"
    const val VER_READER_DESC = "Introduced a high-performance PDF renderer with pinch-to-zoom, night mode, sepia mode, and full-screen reading capabilities."
    const val VER_FINAL_DEMO_TITLE = "VERSION 1.0.0 (FINAL DEMO)"
    const val VER_FINAL_DEMO_DESC = "The complete student hub experience is here! This final release brings polished security, interactive features, and a cohesive design across all platforms."
    const val VER_SECURITY_TITLE = "SECURE AUTHENTICATION"
    const val VER_SECURITY_DESC = "Added 2FA (Two-Factor Authentication) verification for all logins to ensure student data protection. Includes a simulated demo mode for presentations."
    const val VER_REVIEWS_TITLE = "INTERACTIVE REVIEWS"
    const val VER_REVIEWS_DESC = "You can now like and dislike comments! Reviews include real-time timestamps and confirmation popups for edits and removals."
    const val VER_THEME_TITLE = "THEME ENHANCEMENTS"
    const val VER_THEME_DESC = "Unified the visual style across all informative screens. High-readability solid backgrounds (0.95 alpha) and professional borders applied to all cards."
    const val VER_CATALOG_TITLE = "EXPANDED CATALOG"
    const val VER_CATALOG_DESC = "The store now includes University Courses, official Glyndŵr Gear, and Audio Books with student discounts automatically applied."

    // --- Notification Metadata ---
    const val NOTIF_TITLE_BOOK_PICKED_UP = "Book Picked Up"
    const val NOTIF_TITLE_AUDIOBOOK_PICKED_UP = "Audiobook Picked Up"
    const val NOTIF_TITLE_COURSE_ENROLLED = "Course Enrollment Confirmed"
    const val NOTIF_TITLE_GEAR_PICKUP = "Pick-up Available"
    const val NOTIF_TITLE_BOOK_PURCHASED = "Book Purchased"
    const val NOTIF_TITLE_AUDIOBOOK_PURCHASED = "Audiobook Purchased"
    const val NOTIF_TITLE_PRODUCT_PURCHASED = "Product Purchased"
    const val NOTIF_TYPE_PURCHASE = "PURCHASE"
    const val NOTIF_TYPE_PICKUP = "PICKUP"

    // --- Payment Methods ---
    const val METHOD_FREE_LIBRARY = "Free Library"
    const val METHOD_FREE_ENROLLMENT = "Free Enrollment"
    const val METHOD_FREE_PICKUP = "Free Pickup"
    const val METHOD_UNIVERSITY_ACCOUNT = "University Account"
    const val METHOD_PAYPAL = "PayPal"
    const val METHOD_GOOGLE_PAY = "Google Pay"
    const val METHOD_CREDIT_CARD = "Credit Card"

    // --- Menu Items ---
    const val MENU_REMOVE_FROM_LIBRARY = "Remove from Library"

    // --- Navigation Routes ---
    const val ROUTE_SPLASH = "splash"
    const val ROUTE_HOME = "home"
    const val ROUTE_AUTH = "auth"
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_NOTIFICATIONS = "notifications"
    const val ROUTE_MESSAGES = "messages"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_ABOUT = "about"
    const val ROUTE_DEVELOPER = "developer"
    const val ROUTE_INSTRUCTIONS = "instructions"
    const val ROUTE_VERSION_INFO = "version_info"
    const val ROUTE_FUTURE_FEATURES = "future_features"
    const val ROUTE_ADMIN_PANEL = "admin_panel"
    const val ROUTE_TUTOR_PANEL = "tutor_panel"
    const val ROUTE_CLASSROOM = "classroom"
    const val ROUTE_BOOK_DETAILS = "bookDetails"
    const val ROUTE_PDF_READER = "pdfReader"
    const val ROUTE_INVOICE_CREATING = "invoiceCreating"
    const val ROUTE_INVOICE = "invoice"
    const val ROUTE_LATEST = "latest"
    const val ROUTE_MY_APPLICATIONS = "my_applications"
    const val ROUTE_ADMIN_USER_DETAILS = "admin_user_details"
    const val ROUTE_COURSE_ENROLLMENT = "course_enrollment"

    // --- Filter Labels ---
    const val FILTER_ALL = "All"
    const val FILTER_BOOKS = "Books"
    const val FILTER_AUDIOBOOKS = "Audiobooks"
    const val FILTER_GEAR = "Gear"
    const val FILTER_COURSES = "Courses"

    // --- Technical IDs ---
    const val ID_TOPUP = "TOPUP"

    /**
     * Centralized logic to determine the status label for a book/item.
     */
    fun getItemStatusLabel(book: Book): String {
        return when {
            book.mainCategory == CAT_COURSES -> LABEL_ENROLLED
            book.price > 0.0 -> LABEL_PURCHASED
            book.mainCategory == CAT_GEAR -> LABEL_PICKED_UP
            else -> LABEL_IN_LIBRARY
        }
    }

    /**
     * Returns the default subcategory name based on the main category.
     */
    fun getDefaultSubcategory(mainCat: String): String {
        return if (mainCat == CAT_COURSES) "All Departments" else "All Genres"
    }

    /**
     * Maps Filter Names to Store Categories
     */
    fun mapFilterToCategory(filter: String): String {
        return when (filter) {
            FILTER_BOOKS -> CAT_BOOKS
            FILTER_AUDIOBOOKS -> CAT_AUDIOBOOKS
            FILTER_GEAR -> CAT_GEAR
            FILTER_COURSES -> CAT_COURSES
            else -> CAT_ALL
        }
    }
}
