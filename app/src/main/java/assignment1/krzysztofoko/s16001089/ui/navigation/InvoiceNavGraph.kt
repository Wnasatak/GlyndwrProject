package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.InvoiceCreatingScreen
import assignment1.krzysztofoko.s16001089.ui.info.InvoiceScreen
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * Navigation graph dedicated to the Invoicing feature.
 */
fun NavGraphBuilder.invoiceNavGraph(
    navController: NavController,
    allBooks: List<Book>,
    currentUserDisplayName: String,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit
) {
    val isDarkTheme = currentTheme == Theme.DARK
    val onToggleTheme = {
        val next = if (isDarkTheme) Theme.LIGHT else Theme.DARK
        onThemeChange(next)
    }

    composable(
        route = "${AppConstants.ROUTE_INVOICE_CREATING}/{bookId}?ref={ref}",
        arguments = listOf(
            navArgument("bookId") { type = NavType.StringType },
            navArgument("ref") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    ) { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val orderRef = backStackEntry.arguments?.getString("ref")
        
        val selectedBook = if (bookId == AppConstants.ID_TOPUP) {
            Book(id = AppConstants.ID_TOPUP, title = "Wallet Top-Up", mainCategory = AppConstants.CAT_FINANCE, category = "Finance")
        } else {
            allBooks.find { it.id == bookId }
        }

        selectedBook?.let { book ->
            InvoiceCreatingScreen(
                book = book,
                onCreationComplete = {
                    val finalRoute = if (orderRef != null) "${AppConstants.ROUTE_INVOICE}/$bookId?ref=$orderRef"
                                     else "${AppConstants.ROUTE_INVOICE}/$bookId"
                    
                    navController.navigate(finalRoute) {
                        popUpTo("${AppConstants.ROUTE_INVOICE_CREATING}/$bookId") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }

    composable(
        route = "${AppConstants.ROUTE_INVOICE}/{bookId}?ref={ref}",
        arguments = listOf(
            navArgument("bookId") { type = NavType.StringType },
            navArgument("ref") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    ) { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val orderRef = backStackEntry.arguments?.getString("ref")
        
        val selectedBook = if (bookId == AppConstants.ID_TOPUP) {
            Book(id = AppConstants.ID_TOPUP, title = "Wallet Top-Up", mainCategory = AppConstants.CAT_FINANCE, category = "Finance")
        } else {
            allBooks.find { it.id == bookId }
        }

        selectedBook?.let { book ->
            InvoiceScreen(
                book = book,
                userName = currentUserDisplayName,
                onBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                orderRef = orderRef
            )
        }
    }
}
