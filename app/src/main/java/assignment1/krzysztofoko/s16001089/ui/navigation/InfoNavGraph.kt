package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.info.*

/**
 * Navigation graph for informational screens.
 * 
 * This module defines the routes and screen initializations for the "About" section 
 * of the application, including developer details, usage instructions, version history, 
 * and the future roadmap.
 */
fun NavGraphBuilder.infoNavGraph(
    navController: NavController,       // The central navigation controller for the app
    isDarkTheme: Boolean,               // Global theme state (Dark/Light mode)
    onToggleTheme: () -> Unit           // Callback function to switch between themes
) {
    /**
     * ROUTE: About Screen
     * The primary entry point for general application information.
     */
    composable(AppConstants.ROUTE_ABOUT) {
        AboutScreen(
            onBack = { navController.popBackStack() }, // Returns to the previous screen
            onDeveloperClick = { navController.navigate(AppConstants.ROUTE_DEVELOPER) }, // Navigate to Developer info
            onInstructionClick = { navController.navigate(AppConstants.ROUTE_INSTRUCTIONS) }, // Navigate to User Manual
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }

    /**
     * ROUTE: Developer Screen
     * Displays credentials and technical details about the app's creator.
     */
    composable(AppConstants.ROUTE_DEVELOPER) {
        DeveloperScreen(
            onBack = { navController.popBackStack() },
            onVersionClick = { navController.navigate(AppConstants.ROUTE_VERSION_INFO) }, // Navigate to what's new
            onFutureFeaturesClick = { navController.navigate(AppConstants.ROUTE_FUTURE_FEATURES) }, // Navigate to Roadmap
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }

    /**
     * ROUTE: Instructions Screen
     * Provides a step-by-step guide on how to use the Glyndŵr Store features.
     */
    composable(AppConstants.ROUTE_INSTRUCTIONS) {
        InstructionScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }

    /**
     * ROUTE: Version Info Screen
     * Shows the current version name and a detailed changelog of recent updates.
     */
    composable(AppConstants.ROUTE_VERSION_INFO) {
        VersionInfoScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }

    /**
     * ROUTE: Future Features Screen
     * Displays the project roadmap and upcoming planned improvements.
     */
    composable(AppConstants.ROUTE_FUTURE_FEATURES) {
        FutureFeaturesScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}
