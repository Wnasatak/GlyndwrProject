package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.auth.AuthScreen

/**
 * Navigation graph for Authentication and Identity Verification.
 * 
 * This module manages the entry point for user login, registration, 
 * and Two-Factor Authentication (2FA) flows. It handles secure redirection
 * upon successful identity verification.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,       // Main navigation controller
    isDarkTheme: Boolean,               // Current global theme state
    onToggleTheme: () -> Unit           // Theme toggle callback
) {
    /**
     * ROUTE: Authentication Screen
     * This route serves as the unified interface for Login, SignUp, 
     * Password Recovery, and 2FA Verification.
     */
    composable(AppConstants.ROUTE_AUTH) {
        AuthScreen(
            onAuthSuccess = { role ->
                /**
                 * POST-AUTH REDIRECTION:
                 * Once the user is fully verified, we check their role.
                 * Redirects to the appropriate starting point:
                 * - Admin -> Admin Panel
                 * - Teacher/Tutor -> Tutor Panel
                 * - Others -> Home Discovery
                 */
                val targetRoute = when (role) {
                    "admin" -> AppConstants.ROUTE_ADMIN_PANEL
                    "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL
                    else -> AppConstants.ROUTE_HOME
                }
                
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            onBack = { 
                // Standard return path for users who choose to browse as guests
                navController.popBackStack() 
            },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            // Initializes a fresh snackbar host specifically for auth-related feedback
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
