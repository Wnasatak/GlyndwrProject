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
import assignment1.krzysztofoko.s16001089.ui.home.HomeScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Navigation graph module for the Primary Discovery (Home) experience.
 * This file defines the entry point for the store/discovery part of the app.
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavController,           // Reference to the main app navigation controller
    currentUserFlow: StateFlow<FirebaseUser?>, // Flow providing the current user's session state
    isDataLoading: Boolean,                 // State indicating if book/course data is currently fetching
    loadError: String?,                     // Holds any error messages from data operations
    currentTheme: Theme,                    // The currently selected visual theme
    onThemeChange: (Theme) -> Unit,         // Callback to change the global theme
    onOpenThemeBuilder: () -> Unit,         // Callback to launch the custom theme creation dialog
    onRefresh: () -> Unit,                  // Logic to re-trigger data loading (pull-to-refresh)
    onPlayAudio: (Book) -> Unit,            // Logic to start playing an audiobook
    currentPlayingBookId: String?,          // ID of the book currently active in the media player
    isAudioPlaying: Boolean                 // Playback state (playing vs paused)
) {
    /**
     * ROUTE: Home Screen
     * The main landing page where users discover courses and books.
     * Supports an optional 'category' parameter for deep-linking into specific genres.
     */
    composable(
        // Route definition with an optional query parameter for category filtering
        route = "${AppConstants.ROUTE_HOME}?category={category}",
        arguments = listOf(
            navArgument("category") { 
                type = NavType.StringType // Argument is a plain string
                nullable = true           // Can be null if no filter is active
                defaultValue = null       // Defaults to null (show all)
            }
        )
    ) { backStackEntry ->
        // Extract the category argument from the navigation entry
        val category = backStackEntry.arguments?.getString("category")
        
        // Collect the current user state into Compose state
        val currentUser by currentUserFlow.collectAsState()
        
        // Render the main Home Screen UI
        HomeScreen(
            userId = currentUser?.uid ?: "", // Pass current user ID or empty string if guest
            initialCategory = category,      // Pass deep-linked category to the UI
            navController = navController,   // Allow the screen to trigger further navigation
            isLoggedIn = currentUser != null, // Boolean flag for authentication status
            isLoading = isDataLoading,       // Visual indicator for data fetching
            error = loadError,               // Display error state if data fails
            onRefresh = onRefresh,           // Handler for manual data refresh
            onAboutClick = { navController.navigate(AppConstants.ROUTE_ABOUT) }, // Navigate to information page
            currentTheme = currentTheme,     // Pass theme for internal styling
            onThemeChange = { theme ->       // Wrapper for theme changes
                onThemeChange(theme)
                // If user selects CUSTOM, immediately open the build tool
                if (theme == Theme.CUSTOM) onOpenThemeBuilder()
            },
            onPlayAudio = onPlayAudio,       // Handler for audio playback triggers
            currentPlayingBookId = currentPlayingBookId, // Sync UI with active playback
            isAudioPlaying = isAudioPlaying  // Sync play/pause button state
        )
    }
}
