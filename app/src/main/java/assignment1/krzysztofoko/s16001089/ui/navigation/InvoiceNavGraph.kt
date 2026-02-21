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
 * Navigation graph dedicated to the Invoicing and Transaction Receipt features.
 */
fun NavGraphBuilder.invoiceNavGraph(
    navController: NavController,   // Main app navigation control
    allBooks: List<Book>,           // Data source for identifying the item being invoiced
    currentUserDisplayName: String, // Name of the user to be printed on the receipt
    currentTheme: Theme,            // Current theme for UI styling
    onThemeChange: (Theme) -> Unit  // Theme update callback
) {
    // Correctly determine if the theme is dark based on all supported dark variants
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM
    
    // Compact helper for theme toggling within the invoice screens
    val onToggleTheme = {
        val next = if (isDarkTheme) Theme.LIGHT else Theme.DARK
        onThemeChange(next)
    }

    // --- ROUTE: INVOICE PROCESSING ---
    // Displays a transitional screen simulating the generation/validation of a purchase record.
    composable(
        route = "${AppConstants.ROUTE_INVOICE_CREATING}/{bookId}?ref={ref}",
        arguments = listOf(
            navArgument("bookId") { type = NavType.StringType }, // Unique ID of course or wallet top-up
            navArgument("ref") { type = NavType.StringType; nullable = true; defaultValue = null } // Optional receipt reference
        )
    ) { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val orderRef = backStackEntry.arguments?.getString("ref")
        
        // Identify if this is a standard course purchase or a special "Wallet Top-Up" event
        val selectedBook = if (bookId == AppConstants.ID_TOPUP) {
            Book(id = AppConstants.ID_TOPUP, title = "Wallet Top-Up", mainCategory = AppConstants.CAT_FINANCE, category = "Finance")
        } else {
            allBooks.find { it.id == bookId }
        }

        selectedBook?.let { book ->
            InvoiceCreatingScreen(
                book = book,
                onCreationComplete = {
                    // Navigate to the final receipt once the "creation" logic is done
                    val finalRoute = if (orderRef != null) "${AppConstants.ROUTE_INVOICE}/$bookId?ref=$orderRef"
                                     else "${AppConstants.ROUTE_INVOICE}/$bookId"
                    
                    navController.navigate(finalRoute) {
                        // Ensure we remove the processing screen from the backstack history
                        popUpTo("${AppConstants.ROUTE_INVOICE_CREATING}/$bookId") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }

    // --- ROUTE: DIGITAL RECEIPT ---
    // Displays the final formatted invoice for user download or viewing.
    composable(
        route = "${AppConstants.ROUTE_INVOICE}/{bookId}?ref={ref}",
        arguments = listOf(
            navArgument("bookId") { type = NavType.StringType },
            navArgument("ref") { type = NavType.StringType; nullable = true; defaultValue = null }
        )
    ) { backStackEntry ->
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val orderRef = backStackEntry.arguments?.getString("ref")
        
        // mirrored item identification logic
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
