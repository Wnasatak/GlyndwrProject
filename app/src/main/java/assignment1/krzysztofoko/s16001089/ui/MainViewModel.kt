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
import assignment1.krzysztofoko.s16001089.utils.NetworkMonitor
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
 * 
 * New Feature: Integrated Network Monitoring (8% requirement).
 */
class MainViewModel(
    private val repository: BookRepository, // Source for institutional data (Books, Courses)
    private val db: AppDatabase,            // Direct access to the Room DB for user-specific tables
    private val networkMonitor: NetworkMonitor? = null // Monitor for real-time connectivity status
) : ViewModel() {

    // Firebase Authentication instance for session handling
    private val auth = FirebaseAuth.getInstance()

    // --- CONNECTIVITY STATE --- //
    
    /** 
     * Reactive flow representing the device's internet connection status.
     * UI components observe this to show/hide the "Offline Mode" banner.
     */
    val isOnline: StateFlow<Boolean> = networkMonitor?.isOnline ?: MutableStateFlow(true).asStateFlow()

    // --- AUTHENTICATION & PROFILE STATE --- //
    
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val localUser: StateFlow<UserLocal?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userDao().getUserFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO) 
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userTheme: StateFlow<UserTheme?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userThemeDao().getThemeFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // --- CATALOGUE & SYNC STATE --- //

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val walletHistory: StateFlow<List<WalletTransaction>> = _currentUser.flatMapLatest { user ->
        if (user != null) db.userDao().getWalletHistory(user.uid)
        else flowOf(emptyList())
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- GLOBAL MEDIA PLAYER STATE --- //
    
    var currentPlayingBook by mutableStateOf<Book?>(null)
        private set
    
    var isAudioPlaying by mutableStateOf(false)
        private set
    
    var showPlayer by mutableStateOf(false)
    var isPlayerMinimized by mutableStateOf(false)

    // --- GLOBAL UI DIALOG CONTROLS --- //
    var showLogoutConfirm by mutableStateOf(false) 
    var showSignedOutPopup by mutableStateOf(false) 
    var showWalletHistory by mutableStateOf(false)

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        val wasLoggedIn = _currentUser.value != null
        _currentUser.value = user
        if (wasLoggedIn && user == null) {
            showSignedOutPopup = true
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        // Start monitoring network when VM is created
        networkMonitor?.startMonitoring()
    }

    fun refreshData() {
        // Data refreshes automatically via Repository Flows
    }

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

    fun onPlayAudio(book: Book, player: Player?) {
        if (currentPlayingBook?.id == book.id) {
            if (player?.isPlaying == true) player.pause() else player?.play()
        } else {
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

    fun syncPlayerState(isPlaying: Boolean) {
        isAudioPlaying = isPlaying
    }

    fun stopPlayer(player: Player?) {
        showPlayer = false
        player?.stop()
        currentPlayingBook = null
    }

    fun signOut(navController: NavController) {
        showLogoutConfirm = false
        auth.signOut() 
        navController.navigate(AppConstants.ROUTE_HOME) {
            popUpTo(0) { inclusive = true } 
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        // Cleanup listener to prevent memory leaks
        networkMonitor?.stopMonitoring()
    }
}

/**
 * Factory provides dependencies (Repository, DB, NetworkMonitor) to the MainViewModel.
 */
class MainViewModelFactory(
    private val repository: BookRepository,
    private val db: AppDatabase,
    private val networkMonitor: NetworkMonitor? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, db, networkMonitor) as T
    }
}
