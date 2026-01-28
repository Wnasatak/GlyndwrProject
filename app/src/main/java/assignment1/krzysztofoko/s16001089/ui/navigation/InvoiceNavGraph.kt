package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.InvoiceCreatingScreen
import assignment1.krzysztofoko.s16001089.ui.info.InvoiceScreen

fun NavGraphBuilder.invoiceNavGraph(
    navController: NavController,
    allBooks: List<Book>,
    currentUserDisplayName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    composable("${AppConstants.ROUTE_INVOICE_CREATING}/{bookId}") { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val selectedBook = if (bookId == AppConstants.ID_TOPUP) {
            Book(
                id = AppConstants.ID_TOPUP, 
                title = "Wallet Top-Up", 
                mainCategory = AppConstants.CAT_FINANCE,
                category = "Finance"
            )
        } else {
            allBooks.find { it.id == bookId }
        }

        selectedBook?.let { book ->
            InvoiceCreatingScreen(
                book = book,
                onCreationComplete = {
                    navController.navigate("${AppConstants.ROUTE_INVOICE}/$bookId") {
                        popUpTo("${AppConstants.ROUTE_INVOICE_CREATING}/$bookId") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }

    composable("${AppConstants.ROUTE_INVOICE}/{bookId}") { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val selectedBook = if (bookId == AppConstants.ID_TOPUP) {
            Book(
                id = AppConstants.ID_TOPUP, 
                title = "Wallet Top-Up", 
                mainCategory = AppConstants.CAT_FINANCE,
                category = "Finance"
            )
        } else {
            allBooks.find { it.id == bookId }
        }

        selectedBook?.let { book ->
            InvoiceScreen(
                book = book,
                userName = currentUserDisplayName,
                onBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }
}
