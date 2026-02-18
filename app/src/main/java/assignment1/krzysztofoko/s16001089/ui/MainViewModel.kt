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
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MainViewModel.kt
 *
 * This ViewModel serves as the application's central state orchestrator.
 * It manages the global lifecycle of the application, including user authentication,
 * data synchronisation, global media playback, and user-defined theme persistence.
 */
class MainViewModel(
    private val repository: BookRepository, // Source for institutional data (Books, Courses)
    private val db: AppDatabase             // Direct access to the Room DB for user-specific tables
) : ViewModel() {

    // Firebase Authentication instance for session handling
    private val auth = FirebaseAuth.getInstance()

    // --- AUTHENTICATION & PROFILE STATE --- //
    
    /** Current Firebase User session. Triggers downstream flow updates when login/logout occurs. */
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    /** 
     * Reactive flow for the local User profile (from Room). 
     * Automatically switches the database listener when the Firebase User ID changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val localUser: StateFlow<UserLocal?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userDao().getUserFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO) // Execute DB queries on background thread
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /** 
     * Persistent user theme preferences (last selected theme, custom colours).
     * Synchronised with the local user database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val userTheme: StateFlow<UserTheme?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userThemeDao().getThemeFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // --- CATALOGUE & SYNC STATE --- //

    /** 
     * Master list of all available books, courses, and gear.
     * Combines institutional data with personalised user flags (purchased, favourite).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val allBooks: StateFlow<List<Book>> = _currentUser
        .flatMapLatest { user ->
            repository.getAllCombinedData(user?.uid ?: "")
        }
        .onEach { _isDataLoading.value = false } // Stop loading indicator once data arrives
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Tracks if background data fetching is active
    private val _isDataLoading = MutableStateFlow(true)
    val isDataLoading = _isDataLoading.asStateFlow()

    // Holds potential error messages from failed network or DB operations
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError = _loadError.asStateFlow()

    /** Tracks the unread count for user-specific alerts (e.g., new messages, grades). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            db.userDao().getNotificationsForUser(user.uid).map { list ->
                list.count { !it.isRead } // Dynamically calculate unread items
            }
        } else flowOf(0)
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    /** Chronological record of user financial activity (Purchases, Top-ups). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val walletHistory: StateFlow<List<WalletTransaction>> = _currentUser.flatMapLatest { user ->
        if (user != null) db.userDao().getWalletHistory(user.uid)
        else flowOf(emptyList())
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- GLOBAL MEDIA PLAYER STATE --- //
    
    /** The specific Book object currently active in the audio player. */
    var currentPlayingBook by mutableStateOf<Book?>(null)
        private set
    
    /** Reflects the real-time 'Playing' status of the underlying Media3 instance. */
    var isAudioPlaying by mutableStateOf(false)
        private set
    
    /** Controls whether the audio player UI is visible to the user. */
    var showPlayer by mutableStateOf(false)
    
    /** Switches the player between a compact bar and a full-screen overlay. */
    var isPlayerMinimized by mutableStateOf(false)

    // --- GLOBAL UI DIALOG CONTROLS --- //
    var showLogoutConfirm by mutableStateOf(false)   // "Are you sure?" dialog
    var showSignedOutPopup by mutableStateOf(false)  // Toast-style success feedback
    var showWalletHistory by mutableStateOf(false)   // Bottom sheet visibility

    /** 
     * Handles authentication state changes (Login/Logout).
     * Triggers visual feedback when a session is closed.
     */
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        val wasLoggedIn = _currentUser.value != null
        _currentUser.value = user
        
        // If user was logged in and now is null, show sign-out confirmation
        if (wasLoggedIn && user == null) {
            showSignedOutPopup = true
        }
    }

    init {
        // Initialize the session listener immediately
        auth.addAuthStateListener(authListener)
    }

    /** 
     * Manual trigger to refresh institutional data.
     * Note: Most data is reactive via Room Flow, so this is rarely needed.
     */
    fun refreshData() {
        // Data refreshes automatically via Repository Flows
    }

    /** 
     * Saves the current theme selection to the database for persistence across sessions.
     */
    fun updateThemePersistence(theme: Theme) {
        val user = auth.currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val dao = db.userThemeDao()
            val existing = dao.getThemeById(user.uid) ?: UserTheme(userId = user.uid)
            // Save both the flag for custom mode and the name of the last preset
            dao.upsertTheme(existing.copy(
                isCustomThemeEnabled = (theme == Theme.CUSTOM),
                lastSelectedTheme = theme.name
            ))
        }
    }

    /** 
     * Unified handler for audio playback actions.
     * Logic: Toggles pause/play if the same item is clicked, or loads a new item if different.
     */
    fun onPlayAudio(book: Book, player: Player?) {
        if (currentPlayingBook?.id == book.id) {
            // Control existing playback
            if (player?.isPlaying == true) player.pause() else player?.play()
        } else {
            // Initialize new playback session
            currentPlayingBook = book
            showPlayer = true
            isPlayerMinimized = false
            player?.let { p ->
                val mediaItem = MediaItem.Builder()
                    .setUri("asset:///${book.audioUrl}") // Load from app assets
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

    /** Updates local playing status to sync UI icons with ExoPlayer state. */
    fun syncPlayerState(isPlaying: Boolean) {
        isAudioPlaying = isPlaying
    }

    /** Completely shuts down audio playback and resets player state. */
    fun stopPlayer(player: Player?) {
        showPlayer = false
        player?.stop()
        currentPlayingBook = null
    }

    /** 
     * Executes the secure logout process and resets navigation to the home screen.
     */
    fun signOut(navController: NavController) {
        showLogoutConfirm = false
        auth.signOut() 
        navController.navigate(AppConstants.ROUTE_HOME) {
            popUpTo(0) { inclusive = true } // Clear entire backstack
        }
    }

    /** Lifecycle Cleanup: Ensures listeners are detached to prevent memory leaks. */
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}

/**
 * Factory provides dependencies (Repository, DB) to the MainViewModel during initialization.
 */
class MainViewModelFactory(
    private val repository: BookRepository,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, db) as T
    }
}
