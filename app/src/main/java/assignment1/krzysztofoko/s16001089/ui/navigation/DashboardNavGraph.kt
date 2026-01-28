package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.dashboard.DashboardScreen
import assignment1.krzysztofoko.s16001089.ui.profile.ProfileScreen
import assignment1.krzysztofoko.s16001089.ui.notifications.NotificationScreen
import assignment1.krzysztofoko.s16001089.ui.classroom.ClassroomScreen
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

fun NavGraphBuilder.dashboardNavGraph(
    navController: NavController,
    currentUserFlow: StateFlow<FirebaseUser?>,
    allBooks: List<Book>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayAudio: (Book) -> Unit,
    isAudioPlaying: Boolean,
    currentPlayingBookId: String?,
    onLogoutClick: () -> Unit
) {
    composable(AppConstants.ROUTE_DASHBOARD) { 
        DashboardScreen(
            navController = navController, 
            allBooks = allBooks, 
            onBack = { navController.popBackStack() }, 
            onLogout = onLogoutClick,
            isDarkTheme = isDarkTheme, 
            onToggleTheme = onToggleTheme, 
            onViewInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE_CREATING}/${it.id}") },
            onPlayAudio = onPlayAudio, 
            currentPlayingBookId = currentPlayingBookId, 
            isAudioPlaying = isAudioPlaying
        )
    }

    composable(AppConstants.ROUTE_PROFILE) { 
        ProfileScreen(
            navController = navController, 
            onLogout = onLogoutClick, 
            isDarkTheme = isDarkTheme, 
            onToggleTheme = onToggleTheme
        ) 
    }

    composable(AppConstants.ROUTE_NOTIFICATIONS) {
        NotificationScreen(
            onNavigateToItem = { navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/$it") },
            onNavigateToInvoice = { navController.navigate("${AppConstants.ROUTE_INVOICE}/$it") },
            onBack = { navController.popBackStack() }, 
            isDarkTheme = isDarkTheme
        )
    }

    composable("${AppConstants.ROUTE_CLASSROOM}/{courseId}") { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
        ClassroomScreen(
            courseId = courseId,
            onBack = { navController.popBackStack() },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}
