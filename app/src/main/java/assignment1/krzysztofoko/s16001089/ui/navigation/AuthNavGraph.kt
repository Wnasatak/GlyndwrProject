package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.auth.AuthScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * Navigation graph extension for Authentication and Identity Verification.
 * This function defines the routes and screen transitions related to logging in and sign-up.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController, // Controls the app's navigation stack and backstack // Passed from top-level navigation
    currentTheme: Theme,          // The active application theme (DARK, SKY, FOREST, etc.) // Used for consistent UI styling
    onThemeChange: (Theme) -> Unit // Callback function to trigger theme switching from the auth screen // Updates global state
) {
    // Define the entry point for authentication using the centralized route constant
    composable(AppConstants.ROUTE_AUTH) { // Maps the ROUTE_AUTH string to the AuthScreen composable
        AuthScreen(
            onAuthSuccess = { role -> // Lambda logic executed immediately after a successful Firebase/Local login
                // Determine the destination route based on the user's assigned role stored in the database
                val targetRoute = when (role) {
                    "admin" -> AppConstants.ROUTE_ADMIN_PANEL // Administrators are redirected to System Logs and User Management
                    "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL // Tutors/Teachers go to their specialized management dashboard
                    else -> AppConstants.ROUTE_HOME // Students and standard users proceed to the main Home screen
                }
                
                // Navigate to the determined role-based panel
                navController.navigate(targetRoute) {
                    // Remove the authentication screen from the backstack history
                    popUpTo(navController.graph.startDestinationId) { 
                        inclusive = true // Ensures the user cannot navigate "back" into the login screen once signed in
                    }
                }
            },
            onBack = { navController.popBackStack() }, // Handles the hardware back button or UI back arrow // Reverts to previous screen
            currentTheme = currentTheme, // Pass the current visual theme into the authentication UI
            onThemeChange = onThemeChange, // Pass the theme toggle handler into the authentication UI
            snackbarHostState = remember { SnackbarHostState() } // Manages state for login error messages, password resets, and success alerts
        )
    }
}
