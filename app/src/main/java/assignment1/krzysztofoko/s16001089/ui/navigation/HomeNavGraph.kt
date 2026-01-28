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
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Navigation graph module for the Primary Discovery (Home) experience.
 * 
 * This module manages the main landing page of the application where users can
 * browse the product catalog, filter by categories, and initiate audio playback.
 * It supports deep-linking or internal navigation with category parameters.
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavController,           // Global navigation controller for screen transitions
    currentUserFlow: StateFlow<FirebaseUser?>, // Reactive flow tracking the current user's auth state
    isDataLoading: Boolean,                 // State flag indicating if the master catalog is still loading
    loadError: String?,                     // Optional error message for network or database failures
    isDarkTheme: Boolean,                   // Current global visual theme state
    onToggleTheme: () -> Unit,              // Callback function to switch app appearance
    onRefresh: () -> Unit,                  // Callback to trigger a manual data re-sync
    onPlayAudio: (Book) -> Unit,            // Functional callback to handle global media player requests
    currentPlayingBookId: String?,          // ID of the currently active media item for UI highlighting
    isAudioPlaying: Boolean                 // Global status of the audio engine
) {
    /**
     * ROUTE: Home Screen
     * 
     * Supports an optional query parameter "?category={category}" which allows
     * other screens (like the Dashboard) to navigate back to Home with a pre-applied filter.
     */
    composable(
        route = "${AppConstants.ROUTE_HOME}?category={category}",
        arguments = listOf(
            navArgument("category") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        // Extract the category filter from the navigation arguments
        val category = backStackEntry.arguments?.getString("category")
        
        // Collect the current user state to determine if student discounts should be shown
        val currentUser by currentUserFlow.collectAsState()
        
        /**
         * The Main Home Entry Point.
         * Orchestrates the display of the promotion banners, category filters, 
         * and the actual scrollable product list.
         */
        HomeScreen(
            userId = currentUser?.uid ?: "",
            initialCategory = category,
            navController = navController, 
            isLoggedIn = currentUser != null, 
            isLoading = isDataLoading, 
            error = loadError, 
            onRefresh = onRefresh, 
            onAboutClick = { navController.navigate(AppConstants.ROUTE_ABOUT) }, 
            isDarkTheme = isDarkTheme, 
            onToggleTheme = onToggleTheme,
            onPlayAudio = onPlayAudio,
            currentPlayingBookId = currentPlayingBookId,
            isAudioPlaying = isAudioPlaying
        )
    }
}
