package assignment1.krzysztofoko.s16001089.ui.home

import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.WalletTransaction

data class HomeUiState(
    val allBooks: List<Book> = emptyList(),
    val filteredBooks: List<Book> = emptyList(),
    val wishlistIds: Set<String> = emptySet(),
    val purchasedIds: Set<String> = emptySet(),
    val selectedMainCategory: String = AppConstants.CAT_ALL,
    val selectedSubCategory: String = "All Genres",
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val suggestions: List<Book> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val bookToRemove: Book? = null,
    val walletHistory: List<WalletTransaction> = emptyList(),
    val showWalletHistory: Boolean = false
)
