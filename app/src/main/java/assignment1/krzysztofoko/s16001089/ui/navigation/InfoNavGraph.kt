package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.info.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * Navigation graph extension for informational and support screens.
 * Contains routes for the "About" section, Developer credentials, User manuals, and Roadmap.
 */
fun NavGraphBuilder.infoNavGraph(
    navController: NavController,   // Controls the app's navigation stack
    currentTheme: Theme,            // The active visual theme selection
    userTheme: UserTheme?,          // User-specific custom theme data from DB
    onThemeChange: (Theme) -> Unit, // Callback to update the global theme state
    onOpenThemeBuilder: () -> Unit  // Callback to launch the custom theme creation tool
) {
    // --- ROUTE: ABOUT ---
    // Primary high-level overview of the application purpose and university context.
    composable(AppConstants.ROUTE_ABOUT) {
        AboutScreen(
            onBack = { navController.popBackStack() },
            onDeveloperClick = { navController.navigate(AppConstants.ROUTE_DEVELOPER) },
            onInstructionClick = { navController.navigate(AppConstants.ROUTE_INSTRUCTIONS) },
            onOpenThemeBuilder = onOpenThemeBuilder,
            currentTheme = currentTheme,
            userTheme = userTheme,
            onThemeChange = onThemeChange
        )
    }

    // --- ROUTE: DEVELOPER ---
    // Provides details about the programmer and provides links to technical metadata.
    composable(AppConstants.ROUTE_DEVELOPER) {
        DeveloperScreen(
            onBack = { navController.popBackStack() },
            onVersionClick = { navController.navigate(AppConstants.ROUTE_VERSION_INFO) },
            onFutureFeaturesClick = { navController.navigate(AppConstants.ROUTE_FUTURE_FEATURES) },
            currentTheme = currentTheme,
            userTheme = userTheme,
            onThemeChange = onThemeChange
        )
    }

    // --- ROUTE: INSTRUCTIONS ---
    // A digital manual providing guidance on how to use core application features.
    composable(AppConstants.ROUTE_INSTRUCTIONS) {
        InstructionScreen(
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            userTheme = userTheme,
            onThemeChange = onThemeChange
        )
    }

    // --- ROUTE: VERSION INFO ---
    // Displays the current build version, build date, and specific release notes.
    composable(AppConstants.ROUTE_VERSION_INFO) {
        VersionInfoScreen(
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            userTheme = userTheme,
            onThemeChange = onThemeChange
        )
    }

    // --- ROUTE: FUTURE FEATURES ---
    // Outlines the application roadmap and potential future enhancements.
    composable(AppConstants.ROUTE_FUTURE_FEATURES) {
        FutureFeaturesScreen(
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        )
    }
}
