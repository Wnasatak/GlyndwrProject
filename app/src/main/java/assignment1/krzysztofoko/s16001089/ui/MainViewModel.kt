package assignment1.krzysztofoko.s16001089.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Global ViewModel for the application.
 * Manages the top-level application state, including authentication, shared product data,
 * notifications, wallet history, and the global audio player state.
 */
class MainViewModel(
    private val repository: BookRepository, // Repository for fetching products (books, courses, gear)
    private val db: AppDatabase             // Direct database handle for low-level DAO operations
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- User Authentication State ---
    
    // Flow representing the current Firebase User (notifies UI on login/logout)
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    // Flow representing extended user profile data from the local Room database
    @OptIn(ExperimentalCoroutinesApi::class)
    val localUser: StateFlow<UserLocal?> = _currentUser
        .flatMapLatest { user ->
            // Switches to a new local database flow every time the Firebase user changes
            if (user != null) db.userDao().getUserFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO) // Ensures database operations run off the main thread
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Global Data State ---
    
    // Master list of all products fetched from the combined data repository
    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks = _allBooks.asStateFlow()

    // Flags to track loading progress and handle UI spinners
    private val _isDataLoading = MutableStateFlow(true)
    val isDataLoading = _isDataLoading.asStateFlow()

    // Holds connectivity or caching error messages
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError = _loadError.asStateFlow()

    // --- Shared Notification State ---
    
    // Counts unread alerts for the current user to display badges on icons
    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            db.userDao().getNotificationsForUser(user.uid).map { list ->
                list.count { !it.isRead }
            }
        } else flowOf(0)
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Wallet History State ---
    
    // Provides a globally accessible stream of transaction history (Top-ups and Purchases)
    @OptIn(ExperimentalCoroutinesApi::class)
    val walletHistory: StateFlow<List<WalletTransaction>> = _currentUser.flatMapLatest { user ->
        if (user != null) db.userDao().getWalletHistory(user.uid)
        else flowOf(emptyList())
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Global Audio Player UI State ---
    
    // Tracks which book is currently loaded in the media session
    var currentPlayingBook by mutableStateOf<Book?>(null)
        private set
    
    // Tracks playback status (Playing vs Paused) for icon updates
    var isAudioPlaying by mutableStateOf(false)
        private set
    
    // Controls visibility of the persistent player overlay/floating bar
    var showPlayer by mutableStateOf(false)
    var isPlayerMinimized by mutableStateOf(false)

    // --- Shared UI Dialog States ---
    var showLogoutConfirm by mutableStateOf(false)   // Controls the logout confirmation prompt
    var showSignedOutPopup by mutableStateOf(false)  // Controls the "Securely Signed Out" timer popup
    var showWalletHistory by mutableStateOf(false)   // Controls the visibility of the global wallet sheet

    // Listener to update the internal state whenever Firebase auth changes
    private val authListener = FirebaseAuth.AuthStateListener { 
        _currentUser.value = it.currentUser 
    }

    init {
        auth.addAuthStateListener(authListener)
        refreshData() // Load initial data on startup
    }

    /**
     * Refreshes the application catalog.
     * Seeds the database if empty and collects combined data from all DAOs.
     */
    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isDataLoading.value = true
            _loadError.value = null
            try {
                seedDatabase(db) // Ensures default content exists in the local DB
                repository.getAllCombinedData().collect { combined ->
                    _allBooks.value = combined ?: emptyList()
                    _isDataLoading.value = false
                }
            } catch (e: Exception) {
                _loadError.value = "Connectivity error. Using local cache."
                _isDataLoading.value = false
            }
        }
    }

    /**
     * Entry point for starting audiobook playback.
     * Integrates with the Media3 player instance passed from MainActivity.
     */
    fun onPlayAudio(book: Book, player: Player?) {
        // Toggle pause/play if it's the same book already loaded
        if (currentPlayingBook?.id == book.id) {
            if (player?.isPlaying == true) player.pause() else player?.play()
        } else {
            // Load a new media item if switching books
            currentPlayingBook = book
            showPlayer = true
            isPlayerMinimized = false
            player?.let { p ->
                val mediaItem = MediaItem.Builder()
                    .setUri("asset:///${book.audioUrl}") // Audio files are played from local assets
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(book.title)
                            .setArtist(book.author)
                            .build()
                    )
                    .build()
                p.setMediaItem(mediaItem)
                p.prepare()
                p.play()
            }
        }
    }

    /**
     * Updates the UI playback state (called by listeners in the UI layer).
     */
    fun syncPlayerState(isPlaying: Boolean) {
        isAudioPlaying = isPlaying
    }

    /**
     * Stops the player and hides the persistent UI bar.
     */
    fun stopPlayer(player: Player?) {
        showPlayer = false
        player?.stop()
        currentPlayingBook = null
    }

    /**
     * Handles the secure logout flow and redirects the user to the home screen.
     */
    fun signOut(navController: NavController) {
        showLogoutConfirm = false
        auth.signOut()
        showSignedOutPopup = true
        navController.navigate("home") { popUpTo(0) }
    }

    override fun onCleared() {
        super.onCleared()
        // Prevents memory leaks by removing the auth listener
        auth.removeAuthStateListener(authListener)
    }
}

/**
 * Factory class to create MainViewModel with manual dependency injection.
 */
class MainViewModelFactory(
    private val repository: BookRepository,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, db) as T
    }
}
