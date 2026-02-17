package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.Player
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
import assignment1.krzysztofoko.s16001089.ui.admin.AdminSection
import assignment1.krzysztofoko.s16001089.ui.admin.AdminUserDetailsScreen
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorPanelScreen
import assignment1.krzysztofoko.s16001089.ui.messages.MessagesScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Navigation graph module for the User Dashboard and associated member features.
 */
fun NavGraphBuilder.dashboardNavGraph(
    navController: NavController,
    currentUserFlow: StateFlow<FirebaseUser?>,
    allBooks: List<Book>,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onOpenThemeBuilder: () -> Unit,
    onPlayAudio: (Book) -> Unit,
    isAudioPlaying: Boolean,
    currentPlayingBookId: String?,
    onLogoutClick: () -> Unit
) {
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM

    // Centralized theme change logic
    val onThemeToggle = {
        val nextTheme = if (isDarkTheme) Theme.LIGHT else Theme.DARK
        onThemeChange(nextTheme)
    }

    val onFullThemeChange = { theme: Theme ->
        onThemeChange(theme)
        if (theme == Theme.CUSTOM) onOpenThemeBuilder()
    }

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

    composable(AppConstants.ROUTE_MESSAGES) {
        MessagesScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme
        )
    }

    composable("${AppConstants.ROUTE_CLASSROOM}/{courseId}") { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        ClassroomScreen(
            courseId = courseId,
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme
        )
    }

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
                // Fix: Point Profile Settings to EditProfile route
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
            currentPlayingBookId = currentPlayingBookId,
            isAudioPlaying = isAudioPlaying,
            initialSection = section
        )
    }
}
