package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardScreen
import assignment1.krzysztofoko.s16001089.ui.profile.ProfileScreen
import assignment1.krzysztofoko.s16001089.ui.notifications.NotificationScreen
import assignment1.krzysztofoko.s16001089.ui.classroom.ClassroomScreen
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Navigation graph module for the User Dashboard and associated member features.
 * 
 * This graph coordinates the screens available to authenticated users, including 
 * their personal collection (Dashboard), profile settings, notifications inbox, 
 * and the academic classroom environment.
 */
fun NavGraphBuilder.dashboardNavGraph(
    navController: NavController,           // Global navigation controller for transitions
    currentUserFlow: StateFlow<FirebaseUser?>, // Flow representing the current user session
    allBooks: List<Book>,                   // Global product catalog for resolving item data
    isDarkTheme: Boolean,                   // State of the global app theme
    onToggleTheme: () -> Unit,              // Callback to switch between Dark/Light modes
    onPlayAudio: (Book) -> Unit,            // Handler for global audio playback requests
    isAudioPlaying: Boolean,                // Current playback status of the audio engine
    currentPlayingBookId: String?,          // ID of the book currently being played
    onLogoutClick: () -> Unit               // Handler for the global logout sequence
) {
    /**
     * ROUTE: Dashboard Screen
     * 
     * The central hub for member activity. Displays owned items, wallet balance,
     * and recent history. Integrates with the invoicing system for paid purchases.
     */
    composable(AppConstants.ROUTE_DASHBOARD) { 
        DashboardScreen(
            navController = navController, 
            allBooks = allBooks, 
            onBack = { navController.popBackStack() }, 
            onLogout = onLogoutClick,
            isDarkTheme = isDarkTheme, 
            onToggleTheme = onToggleTheme, 
            // Navigates to the invoice creation flow for a specific purchase
            onViewInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/${it.id}") },
            onPlayAudio = onPlayAudio, 
            currentPlayingBookId = currentPlayingBookId, 
            isAudioPlaying = isAudioPlaying
        )
    }

    /**
     * ROUTE: Profile Screen
     * 
     * Handles personal data management, avatar uploads, and payment method settings.
     * Synchronizes local and cloud user profiles.
     */
    composable(AppConstants.ROUTE_PROFILE) { 
        ProfileScreen(
            navController = navController, 
            onLogout = onLogoutClick, 
            isDarkTheme = isDarkTheme, 
            onToggleTheme = onToggleTheme
        ) 
    }

    /**
     * ROUTE: Notifications Screen
     * 
     * Provides a chronological history of alerts and confirmations.
     * Allows users to jump directly to specific products or their invoices.
     */
    composable(AppConstants.ROUTE_NOTIFICATIONS) {
        NotificationScreen(
            // Logic to navigate to details of the item mentioned in the alert
            onNavigateToItem = { navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$it") },
            // Logic to navigate directly to the financial record of the purchase
            onNavigateToInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE}/$it") },
            onBack = { navController.popBackStack() }, 
            isDarkTheme = isDarkTheme
        )
    }

    /**
     * ROUTE: Classroom Screen
     * 
     * The learning interface for enrolled courses. Uses a mandatory 'courseId'
     * parameter to load specific academic modules, assignments, and grades.
     */
    composable("${AppConstants.ROUTE_CLASSROOM}/{courseId}") { backStackEntry ->
        // Extract the unique identifier for the specific course
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        ClassroomScreen(
            courseId = courseId,
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}
