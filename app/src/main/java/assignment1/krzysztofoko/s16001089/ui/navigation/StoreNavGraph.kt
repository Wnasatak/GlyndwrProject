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

fun NavGraphBuilder.storeNavGraph(
    navController: NavController,
    currentUserFlow: StateFlow<FirebaseUser?>,
    allBooks: List<Book>,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onPlayAudio: (Book) -> Unit
) {
    composable("${AppConstants.ROUTE_BOOK_DETAILS}/{bookId}") { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val selectedBook = allBooks.find { it.id == bookId }
        val currentUser by currentUserFlow.collectAsState()
        
        when {
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

    composable("${AppConstants.ROUTE_COURSE_ENROLLMENT}/{courseId}") { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        CourseEnrollmentScreen(
            courseId = courseId,
            onBack = { navController.popBackStack() },
            onEnrollmentSuccess = {
                navController.navigate(AppConstants.ROUTE_DASHBOARD) {
                    popUpTo("${AppConstants.ROUTE_BOOK_DETAILS}/$courseId") { inclusive = true }
                }
            },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        )
    }

    composable("${AppConstants.ROUTE_PDF_READER}/{bookId}") { backStackEntry -> 
        PdfReaderScreen(
            bookId = backStackEntry.arguments?.getString("bookId") ?: "",
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        ) 
    }
}
