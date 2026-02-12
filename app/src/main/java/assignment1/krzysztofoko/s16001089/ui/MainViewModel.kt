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
 * MainViewModel serves as the application's central state hub.
 * It manages the global lifecycle of the application, including user authentication,
 * data synchronization, global media playback, and user-defined theme persistence.
 *
 * Key Responsibilities:
 * 1. Session Management: Real-time tracking of Firebase Auth state.
 * 2. Reactive Data Streams: Exposing Room database flows for user profile, theme, and catalog.
 * 3. Media Coordination: Interfacing with Media3 for unified audio playback across the app.
 * 4. Global UI Orchestration: Managing state for top-level dialogs, loading screens, and overlays.
 */
class MainViewModel(
    private val repository: BookRepository, 
    private val db: AppDatabase             
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // --- AUTHENTICATION STATE ---
    
    /** Current Firebase User session. Triggers downstream flow updates upon change. */
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    /** 
     * Local user profile data synchronized with the Firebase UID. 
     * Automatically switches streams when the current user changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val localUser: StateFlow<UserLocal?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userDao().getUserFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO) 
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /** 
     * User-defined theme settings (Custom colors, Dark mode preference). 
     * Persisted in the local database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val userTheme: StateFlow<UserTheme?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userThemeDao().getThemeFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // --- DATA & SYNC STATE ---

    /** 
     * The primary catalog stream. Combines institutional data (Books, Courses, Gear) 
     * with the user's personal ownership and favorite status.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val allBooks: StateFlow<List<Book>> = _currentUser
        .flatMapLatest { user ->
            repository.getAllCombinedData(user?.uid ?: "")
        }
        .onEach { _isDataLoading.value = false }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isDataLoading = MutableStateFlow(true)
    val isDataLoading = _isDataLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError = _loadError.asStateFlow()

    /** Count of unread notifications for the current user. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            db.userDao().getNotificationsForUser(user.uid).map { list ->
                list.count { !it.isRead }
            }
        } else flowOf(0)
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    /** Complete transaction history for the user's university wallet. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val walletHistory: StateFlow<List<WalletTransaction>> = _currentUser.flatMapLatest { user ->
        if (user != null) db.userDao().getWalletHistory(user.uid)
        else flowOf(emptyList())
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- GLOBAL PLAYER STATE ---
    
    /** The book or audiobook currently loaded in the media player. */
    var currentPlayingBook by mutableStateOf<Book?>(null)
        private set
    
    /** Indicates if the media player is currently in the 'Playing' state. */
    var isAudioPlaying by mutableStateOf(false)
        private set
    
    /** Controls the visibility of the global audio player interface. */
    var showPlayer by mutableStateOf(false)
    
    /** Controls the toggle between the minimized bar and maximized overlay player. */
    var isPlayerMinimized by mutableStateOf(false)

    // --- GLOBAL UI OVERLAYS ---
    var showLogoutConfirm by mutableStateOf(false)   
    var showSignedOutPopup by mutableStateOf(false)  
    var showWalletHistory by mutableStateOf(false)

    /** 
     * Internal listener to track authentication transitions. 
     * Triggers UI feedback on successful sign-out.
     */
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        val wasLoggedIn = _currentUser.value != null
        _currentUser.value = user
        
        // Visual feedback for session termination
        if (wasLoggedIn && user == null) {
            showSignedOutPopup = true
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    /** 
     * Manually triggers a data refresh. 
     * Implementation Note: Most data is already reactive via Flow. 
     */
    fun refreshData() {
        // Handled automatically by Room Flows
    }

    /** 
     * Persists the user's selected theme choice to the local database.
     * This ensures the application maintains its visual identity across sessions.
     */
    fun updateThemePersistence(theme: Theme) {
        val user = auth.currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val dao = db.userThemeDao()
            val existing = dao.getThemeById(user.uid) ?: UserTheme(userId = user.uid)
            dao.upsertTheme(existing.copy(
                isCustomThemeEnabled = (theme == Theme.CUSTOM),
                lastSelectedTheme = theme.name
            ))
        }
    }

    /** 
     * Initiates or toggles audio playback for a specific resource.
     * 
     * @param book The resource to play.
     * @param player The Media3 Player instance (ExoPlayer).
     */
    fun onPlayAudio(book: Book, player: Player?) {
        if (currentPlayingBook?.id == book.id) {
            // Toggle playback for the current item
            if (player?.isPlaying == true) player.pause() else player?.play()
        } else {
            // Load and play a new item
            currentPlayingBook = book
            showPlayer = true
            isPlayerMinimized = false
            player?.let { p ->
                val mediaItem = MediaItem.Builder()
                    .setUri("asset:///${book.audioUrl}") 
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

    /** Synchronizes local UI state with the Media3 player status. */
    fun syncPlayerState(isPlaying: Boolean) {
        isAudioPlaying = isPlaying
    }

    /** Terminates playback and hides the player interface. */
    fun stopPlayer(player: Player?) {
        showPlayer = false
        player?.stop()
        currentPlayingBook = null
    }

    /** 
     * Securely terminates the current user session and clears the navigation stack.
     */
    fun signOut(navController: NavController) {
        showLogoutConfirm = false
        auth.signOut() 
        navController.navigate(AppConstants.ROUTE_HOME) {
            popUpTo(0) { inclusive = true }
        }
    }

    /** Lifecycle cleanup: Removes system-level listeners to prevent memory leaks. */
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}

/**
 * Factory for creating [MainViewModel] with required repository and database dependencies.
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
