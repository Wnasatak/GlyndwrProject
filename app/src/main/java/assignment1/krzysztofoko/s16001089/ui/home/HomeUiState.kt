package assignment1.krzysztofoko.s16001089.ui.home

import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.WalletTransaction

/**
 * Immutable data class representing the UI state of the Home Screen.
 * 
 * This class follows the MVI (Model-View-Intent) pattern, capturing all 
 * information needed to render the screen in a single, predictable object.
 */
data class HomeUiState(
    // The master list of all available items in the store
    val allBooks: List<Book> = emptyList(),
    // The list of items currently visible after applying category and search filters
    val filteredBooks: List<Book> = emptyList(),
    
    // Sets containing IDs of items the user has liked or purchased (for fast lookups)
    val wishlistIds: Set<String> = emptySet(),
    val purchasedIds: Set<String> = emptySet(),
    
    // Current filter selections
    val selectedMainCategory: String = AppConstants.CAT_ALL,
    val selectedSubCategory: String = "All Genres",
    
    // Search state
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    // Real-time product suggestions as the user types
    val suggestions: List<Book> = emptyList(),
    // History of the user's past search terms
    val recentSearches: List<String> = emptyList(),
    
    // Network/DB status flags
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Transient state for the "Remove from Library" confirmation dialog
    val bookToRemove: Book? = null,
    
    // Wallet-specific history data
    val walletHistory: List<WalletTransaction> = emptyList(),
    val showWalletHistory: Boolean = false
)
