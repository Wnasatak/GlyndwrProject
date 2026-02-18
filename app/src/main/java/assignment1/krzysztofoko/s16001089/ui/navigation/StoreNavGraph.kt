package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.details.pdf.PdfReaderScreen
import assignment1.krzysztofoko.s16001089.ui.details.audiobook.AudioBookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.book.BookDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.course.CourseDetailScreen
import assignment1.krzysztofoko.s16001089.ui.details.course.CourseEnrollmentScreen
import assignment1.krzysztofoko.s16001089.ui.details.gear.GearDetailScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Navigation graph module for product details and purchase/consumption flows.
 * This function dynamically routes the user to the correct detail screen based on the item type.
 */
fun NavGraphBuilder.storeNavGraph(
    navController: NavController,           // Reference to the main navigation controller
    currentUserFlow: StateFlow<FirebaseUser?>, // Observed stream of the current user's session
    allBooks: List<Book>,                   // The master list of all products (Books, Gear, Courses)
    currentTheme: Theme,                    // The active visual theme selection
    onThemeChange: (Theme) -> Unit,         // Callback to update the global theme
    onPlayAudio: (Book) -> Unit             // Trigger for audio playback service
) {
    // --- ROUTE: DYNAMIC PRODUCT DETAILS ---
    // Maps item IDs to specific screens based on their category (Audiobook vs Gear vs Course vs Book).
    composable("${AppConstants.ROUTE_BOOK_DETAILS}/{bookId}") { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val selectedBook = allBooks.find { it.id == bookId }
        val currentUser by currentUserFlow.collectAsState()
        
        when {
            // Case 1: The item is flagged as an Audiobook
            selectedBook?.isAudioBook == true -> {
                AudioBookDetailScreen(
                    bookId = bookId,
                    user = currentUser,
                    onLoginRequired = { navController.navigate(AppConstants.ROUTE_AUTH) },
                    onBack = { navController.popBackStack() },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange,
                    onPlayAudio = onPlayAudio,
                    onNavigateToProfile = { navController.navigate(AppConstants.ROUTE_PROFILE) },
                    onViewInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/$it") }
                )
            }
            // Case 2: The item belongs to the Merchandise/Gear category
            selectedBook?.mainCategory == AppConstants.CAT_GEAR -> {
                GearDetailScreen(
                    navController = navController,
                    gearId = bookId,
                    user = currentUser,
                    onLoginRequired = { navController.navigate(AppConstants.ROUTE_AUTH) },
                    onBack = { navController.popBackStack() },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange,
                    onNavigateToProfile = { navController.navigate(AppConstants.ROUTE_PROFILE) },
                    onViewInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/$it") }
                )
            }
            // Case 3: The item is an academic Course
            selectedBook?.mainCategory == AppConstants.CAT_COURSES -> {
                CourseDetailScreen(
                    courseId = bookId,
                    user = currentUser,
                    onLoginRequired = { navController.navigate(AppConstants.ROUTE_AUTH) },
                    onBack = { navController.popBackStack() },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange,
                    onNavigateToProfile = { navController.navigate(AppConstants.ROUTE_PROFILE) },
                    onViewInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/$it") },
                    onEnterClassroom = { navController.navigate("${AppConstants.ROUTE_CLASSROOM}/$it") },
                    onStartEnrollment = { navController.navigate("${AppConstants.ROUTE_COURSE_ENROLLMENT}/$it") }
                )
            }
            // Default: Standard Digital or Physical Book
            else -> {
                BookDetailScreen(
                    bookId = bookId,
                    initialBook = selectedBook,
                    user = currentUser,
                    onLoginRequired = { navController.navigate(AppConstants.ROUTE_AUTH) },
                    onBack = { navController.popBackStack() },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange,
                    onReadBook = { navController.navigate("${AppConstants.ROUTE_PDF_READER}/$it") },
                    onNavigateToProfile = { navController.navigate(AppConstants.ROUTE_PROFILE) },
                    onViewInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/$it") }
                )
            }
        }
    }

    // --- ROUTE: COURSE ENROLLMENT WORKFLOW ---
    // Handles the step-by-step registration process for students.
    composable("${AppConstants.ROUTE_COURSE_ENROLLMENT}/{courseId}") { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        CourseEnrollmentScreen(
            courseId = courseId,
            onBack = { navController.popBackStack() },
            onEnrollmentSuccess = {
                // Redirect to dashboard and remove enrollment screens from history
                navController.navigate(AppConstants.ROUTE_DASHBOARD) {
                    popUpTo("${AppConstants.ROUTE_BOOK_DETAILS}/$courseId") { inclusive = true }
                }
            },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        )
    }

    // --- ROUTE: PDF DOCUMENT READER ---
    // A dedicated full-screen viewer for digital book content.
    composable("${AppConstants.ROUTE_PDF_READER}/{bookId}") { backStackEntry -> 
        PdfReaderScreen(
            bookId = backStackEntry.arguments?.getString("bookId") ?: "",
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        ) 
    }
}
