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
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavController,           
    currentUserFlow: StateFlow<FirebaseUser?>, 
    isDataLoading: Boolean,                 
    loadError: String?,                     
    currentTheme: Theme,                   
    onThemeChange: (Theme) -> Unit,
    onOpenThemeBuilder: () -> Unit, // Added
    onRefresh: () -> Unit,                  
    onPlayAudio: (Book) -> Unit,            
    currentPlayingBookId: String?,          
    isAudioPlaying: Boolean                 
) {
    /**
     * ROUTE: Home Screen
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
        val category = backStackEntry.arguments?.getString("category")
        val currentUser by currentUserFlow.collectAsState()
        
        HomeScreen(
            userId = currentUser?.uid ?: "",
            initialCategory = category,
            navController = navController, 
            isLoggedIn = currentUser != null, 
            isLoading = isDataLoading, 
            error = loadError, 
            onRefresh = onRefresh, 
            onAboutClick = { navController.navigate(AppConstants.ROUTE_ABOUT) }, 
            currentTheme = currentTheme, 
            onThemeChange = { theme ->
                onThemeChange(theme)
                if (theme == Theme.CUSTOM) onOpenThemeBuilder()
            },
            onPlayAudio = onPlayAudio,
            currentPlayingBookId = currentPlayingBookId,
            isAudioPlaying = isAudioPlaying
        )
    }
}
