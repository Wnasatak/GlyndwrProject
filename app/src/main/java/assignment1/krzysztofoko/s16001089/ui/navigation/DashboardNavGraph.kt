package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardScreen
import assignment1.krzysztofoko.s16001089.ui.profile.ProfileScreen
import assignment1.krzysztofoko.s16001089.ui.profile.EditProfileScreen
import assignment1.krzysztofoko.s16001089.ui.notifications.NotificationScreen
import assignment1.krzysztofoko.s16001089.ui.classroom.ClassroomScreen
import assignment1.krzysztofoko.s16001089.ui.admin.AdminPanelScreen
import assignment1.krzysztofoko.s16001089.ui.admin.AdminUserDetailsScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorPanelScreen
import assignment1.krzysztofoko.s16001089.ui.messages.MessagesScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseUser
import androidx.media3.common.Player
import kotlinx.coroutines.flow.StateFlow

/**
 * Navigation graph module for the User Dashboard and associated member features.
 * Defines the main authenticated routes for students, tutors, and admins.
 */
fun NavGraphBuilder.dashboardNavGraph(
    navController: NavController, // Controls the app's navigation stack
    currentUserFlow: StateFlow<FirebaseUser?>, // Observed stream of the currently logged-in user
    allBooks: List<Book>, // Global library data used for display in dashboard
    currentTheme: Theme, // Current visual theme state
    onThemeChange: (Theme) -> Unit, // Callback to update the global theme
    onOpenThemeBuilder: () -> Unit, // Callback to launch the custom theme creation tool
    onPlayAudio: (Book) -> Unit, // Trigger for playing an audiobook
    externalPlayer: Player? = null, // Global media player instance
    isAudioPlaying: Boolean, // State of the global audio player
    currentPlayingBookId: String?, // ID of the book currently active in the player
    currentPlayingBook: Book?, // Full book object currently active in the player
    onLogoutClick: () -> Unit // Global sign-out logic
) {
    // Utility flag to determine if the UI should render in a "Dark" style based on theme choice
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM

    // Comprehensive theme change handler that also triggers the builder for custom themes
    val onFullThemeChange = { theme: Theme ->
        onThemeChange(theme)
        if (theme == Theme.CUSTOM) onOpenThemeBuilder()
    }

    // --- DASHBOARD ROUTE ---
    composable(AppConstants.ROUTE_DASHBOARD) {
        DashboardScreen(
            navController = navController,
            allBooks = allBooks,
            onBack = { navController.popBackStack() },
            onLogout = onLogoutClick,
            isDarkTheme = isDarkTheme,
            onThemeChange = onFullThemeChange,
            onViewInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/${it.id}") },
            onPlayAudio = onPlayAudio,
            currentPlayingBookId = currentPlayingBookId,
            isAudioPlaying = isAudioPlaying,
            currentTheme = currentTheme,
            onOpenThemeBuilder = onOpenThemeBuilder
        )
    }

    // --- PROFILE ROUTES ---
    composable(AppConstants.ROUTE_PROFILE) {
        ProfileScreen(
            navController = navController,
            onLogout = onLogoutClick,
            isDarkTheme = isDarkTheme
        )
    }

    composable(AppConstants.ROUTE_EDIT_PROFILE) {
        EditProfileScreen(
            navController = navController,
            onLogout = onLogoutClick,
            isDarkTheme = isDarkTheme
        )
    }

    // --- NOTIFICATION HUB ---
    composable(AppConstants.ROUTE_NOTIFICATIONS) {
        val currentUser by currentUserFlow.collectAsState()
        val isAdmin = currentUser?.email == "prokocomp@gmail.com"

        NotificationScreen(
            onNavigateToItem = { navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$it") },
            onNavigateToInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE}/$it") },
            onNavigateToMessages = { navController.navigate(AppConstants.ROUTE_MESSAGES) },
            onBack = {
                if (isAdmin) {
                    navController.navigate(AppConstants.ROUTE_ADMIN_PANEL) {
                        popUpTo(AppConstants.ROUTE_ADMIN_PANEL) { inclusive = true }
                    }
                } else {
                    navController.popBackStack()
                }
            },
            isDarkTheme = isDarkTheme
        )
    }

    // --- MESSAGING ---
    composable(AppConstants.ROUTE_MESSAGES) {
        MessagesScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme
        )
    }

    // --- CLASSROOM ---
    composable("${AppConstants.ROUTE_CLASSROOM}/{courseId}") { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        ClassroomScreen(
            courseId = courseId,
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme
        )
    }

    // --- ADMIN PANEL ---
    composable(
        route = "${AppConstants.ROUTE_ADMIN_PANEL}?section={section}",
        arguments = listOf(
            navArgument("section") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val section = backStackEntry.arguments?.getString("section")
        AdminPanelScreen(
            onBack = { navController.popBackStack() },
            onNavigateToUserDetails = { userId ->
                navController.navigate("${AppConstants.ROUTE_ADMIN_USER_DETAILS}/$userId")
            },
            onNavigateToProfile = { 
                navController.navigate(AppConstants.ROUTE_EDIT_PROFILE) 
            },
            onNavigateToBookDetails = { bookId ->
                navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$bookId")
            },
            onExploreMore = {
                navController.navigate(AppConstants.ROUTE_HOME)
            },
            allBooks = allBooks,
            currentTheme = currentTheme,
            onThemeChange = onFullThemeChange,
            onLogoutClick = { onLogoutClick() },
            initialSection = section
        )
    }

    // --- ADMIN USER MANAGEMENT ---
    composable("${AppConstants.ROUTE_ADMIN_USER_DETAILS}/{userId}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId") ?: ""
        AdminUserDetailsScreen(
            userId = userId,
            onBack = { navController.popBackStack() },
            navController = navController,
            currentTheme = currentTheme,
            onThemeChange = onFullThemeChange
        )
    }

    // --- TUTOR PANEL ---
    composable(
        route = "${AppConstants.ROUTE_TUTOR_PANEL}?section={section}",
        arguments = listOf(
            navArgument("section") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val section = backStackEntry.arguments?.getString("section")
        TutorPanelScreen(
            onBack = { navController.popBackStack() },
            onNavigateToStore = { navController.navigate(AppConstants.ROUTE_HOME) },
            onNavigateToProfile = { navController.navigate(AppConstants.ROUTE_PROFILE) },
            onLogout = onLogoutClick,
            onNavigateToDeveloper = { navController.navigate(AppConstants.ROUTE_DEVELOPER) },
            onNavigateToInstruction = { navController.navigate(AppConstants.ROUTE_INSTRUCTIONS) },
            onOpenThemeBuilder = onOpenThemeBuilder,
            currentTheme = currentTheme,
            onThemeChange = onFullThemeChange,
            onPlayAudio = onPlayAudio,
            externalPlayer = externalPlayer,
            currentPlayingBookId = currentPlayingBookId,
            currentPlayingBook = currentPlayingBook,
            isAudioPlaying = isAudioPlaying,
            initialSection = section,
            onStopPlayer = {
                externalPlayer?.stop()
                externalPlayer?.clearMediaItems()
            }
        )
    }
}
