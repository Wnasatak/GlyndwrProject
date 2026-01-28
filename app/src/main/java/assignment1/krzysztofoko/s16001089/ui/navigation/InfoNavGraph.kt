package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.info.*

fun NavGraphBuilder.infoNavGraph(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    composable(AppConstants.ROUTE_ABOUT) {
        AboutScreen(
            onBack = { navController.popBackStack() },
            onDeveloperClick = { navController.navigate(AppConstants.ROUTE_DEVELOPER) },
            onInstructionClick = { navController.navigate(AppConstants.ROUTE_INSTRUCTIONS) },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
    composable(AppConstants.ROUTE_DEVELOPER) {
        DeveloperScreen(
            onBack = { navController.popBackStack() },
            onVersionClick = { navController.navigate(AppConstants.ROUTE_VERSION_INFO) },
            onFutureFeaturesClick = { navController.navigate(AppConstants.ROUTE_FUTURE_FEATURES) },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
    composable(AppConstants.ROUTE_INSTRUCTIONS) {
        InstructionScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
    composable(AppConstants.ROUTE_VERSION_INFO) {
        VersionInfoScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
    composable(AppConstants.ROUTE_FUTURE_FEATURES) {
        FutureFeaturesScreen(
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}
