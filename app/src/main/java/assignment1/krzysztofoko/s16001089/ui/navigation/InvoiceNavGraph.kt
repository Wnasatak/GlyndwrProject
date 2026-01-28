package assignment1.krzysztofoko.s16001089.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.InvoiceCreatingScreen
import assignment1.krzysztofoko.s16001089.ui.info.InvoiceScreen

/**
 * Navigation graph dedicated to the Invoicing feature.
 * 
 * This module handles the multi-step process of checking for an existing invoice,
 * creating a new one if necessary (simulated generation), and displaying the final
 * digital receipt. It supports both standard products (Books, Gear, Courses) 
 * and special technical IDs like Wallet Top-Ups.
 */
fun NavGraphBuilder.invoiceNavGraph(
    navController: NavController,       // Main navigation controller
    allBooks: List<Book>,               // Master list of products to resolve item details
    currentUserDisplayName: String,     // Name of the current user for billing display
    isDarkTheme: Boolean,               // Global theme state
    onToggleTheme: () -> Unit           // Theme toggle callback
) {
    /**
     * ROUTE: Invoice Creating
     * This intermediate screen handles the "generation" logic.
     * It ensures a record exists in the database before the user views the receipt.
     */
    composable("${AppConstants.ROUTE_INVOICE_CREATING}/{bookId}") { backStackEntry ->
        // Extract the unique identifier for the product
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        
        /**
         * Product Resolution Logic:
         * 1. Check if the ID matches a special system transaction (Top-Up).
         * 2. Otherwise, find the matching item in the global product catalog.
         */
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
            // Display the creation/syncing screen
            InvoiceCreatingScreen(
                book = book,
                onCreationComplete = {
                    /**
                     * Navigation Transition:
                     * Once the invoice is ready in the DB, move to the final viewer.
                     * 'popUpTo' with 'inclusive = true' removes the creating screen 
                     * from the backstack so users don't go back to the "loading" state.
                     */
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

    /**
     * ROUTE: Final Invoice Viewer
     * Displays the official receipt with branding and a download option.
     */
    composable("${AppConstants.ROUTE_INVOICE}/{bookId}") { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        
        // Resolve product details again for the final display
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
            // Display the high-fidelity invoice screen
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
