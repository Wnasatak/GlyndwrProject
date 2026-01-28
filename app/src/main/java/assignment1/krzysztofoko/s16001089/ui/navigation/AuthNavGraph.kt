package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.auth.AuthScreen

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    composable(AppConstants.ROUTE_AUTH) {
        AuthScreen(
            onAuthSuccess = {
                navController.navigate(AppConstants.ROUTE_HOME) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
