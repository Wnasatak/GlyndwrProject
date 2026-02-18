package assignment1.krzysztofoko.s16001089.ui.home

import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.LiveSession
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.data.WalletTransaction

/**
 * HomeUiState.kt
 *
 * This immutable data class represents the complete "Source of Truth" for the Home Screen's UI.
 * By encapsulating all possible UI states into a single object, we ensure a predictable and 
 * unidirectional data flow, making the UI easier to reason about and debug.
 */
data class HomeUiState(
    val allBooks: List<Book> = emptyList(), // The full master list of items from the database.
    val filteredBooks: List<Book> = emptyList(), // The current subset of books being displayed.
    val wishlistIds: Set<String> = emptySet(), // IDs of items currently liked by the user.
    val purchasedIds: Set<String> = emptySet(), // IDs of items already owned by the user.
    val unreadNotificationsCount: Int = 0, // Number of pending alerts for the user.
    val applicationsMap: Map<String, String> = emptyMap(), // Maps course IDs to their application status strings.
    val selectedMainCategory: String = AppConstants.CAT_ALL, // The currently active top-level filter.
    val selectedSubCategory: String = "All Genres", // The currently active secondary filter.
    val searchQuery: String = "", // The raw text currently in the search field.
    val isSearchVisible: Boolean = false, // Master toggle for the search overlay UI.
    val suggestions: List<Book> = emptyList(), // Live search results based on the current query.
    val recentSearches: List<String> = emptyList(), // User's historical search terms.
    val isLoading: Boolean = false, // Global loading flag for the initial data fetch.
    val error: String? = null, // Holds error messages if a database operation fails.
    val bookToRemove: Book? = null, // Tracks the specific item targeted for library removal.
    val walletHistory: List<WalletTransaction> = emptyList(), // List of the user's financial movements.
    val showWalletHistory: Boolean = false, // Toggle for the wallet history bottom sheet.
    val localUser: UserLocal? = null, // Live profile data for the authenticated session.
    val activeLiveSessions: List<LiveSession> = emptyList() // Live course sessions currently in progress.
)
