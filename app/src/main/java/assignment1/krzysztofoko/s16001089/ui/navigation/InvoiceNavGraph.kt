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

/**
 * Navigation graph dedicated to the Invoicing feature.
 * 
 * Updated to support deep-linking via order references, ensuring that
 * users view the specific receipt for each transaction.
 */
fun NavGraphBuilder.invoiceNavGraph(
    navController: NavController,
    allBooks: List<Book>,
    currentUserDisplayName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    /**
     * ROUTE: Invoice Creating
     * Now accepts an optional 'ref' query parameter.
     */
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
                    // Carry over the reference to the final viewer
                    val finalRoute = if (orderRef != null) "${AppConstants.ROUTE_INVOICE}/$bookId?ref=$orderRef"
                                     else "${AppConstants.ROUTE_INVOICE}/$bookId"
                    
                    navController.navigate(finalRoute) {
                        // Pop the 'creating' screen so back navigation goes to the Dashboard/History
                        popUpTo("${AppConstants.ROUTE_INVOICE_CREATING}/$bookId") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }

    /**
     * ROUTE: Final Invoice Viewer
     * Now accepts an optional 'ref' query parameter to lookup unique transactions.
     */
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
                orderRef = orderRef // Pass the reference to force a specific record lookup
            )
        }
    }
}
