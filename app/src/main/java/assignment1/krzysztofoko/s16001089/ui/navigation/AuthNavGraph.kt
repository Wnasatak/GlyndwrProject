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
 * Navigation graph for Authentication and Identity Verification.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit
) {
    composable(AppConstants.ROUTE_AUTH) {
        AuthScreen(
            onAuthSuccess = { role ->
                val targetRoute = when (role) {
                    "admin" -> AppConstants.ROUTE_ADMIN_PANEL
                    "teacher", "tutor" -> AppConstants.ROUTE_TUTOR_PANEL
                    else -> AppConstants.ROUTE_HOME
                }
                
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
