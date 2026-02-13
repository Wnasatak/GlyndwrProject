package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.info.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * Navigation graph for informational screens.
 */
fun NavGraphBuilder.infoNavGraph(
    navController: NavController,
    currentTheme: Theme,
    userTheme: UserTheme?,
    onThemeChange: (Theme) -> Unit,
    onOpenThemeBuilder: () -> Unit
) {
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

    composable(AppConstants.ROUTE_INSTRUCTIONS) {
        InstructionScreen(
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            userTheme = userTheme,
            onThemeChange = onThemeChange
        )
    }

    composable(AppConstants.ROUTE_VERSION_INFO) {
        VersionInfoScreen(
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            userTheme = userTheme,
            onThemeChange = onThemeChange
        )
    }

    composable(AppConstants.ROUTE_FUTURE_FEATURES) {
        FutureFeaturesScreen(
            onBack = { navController.popBackStack() },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        )
    }
}
