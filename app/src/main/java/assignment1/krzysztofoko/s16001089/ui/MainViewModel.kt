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
 * Global ViewModel for the application.
 */
class MainViewModel(
    private val repository: BookRepository, 
    private val db: AppDatabase             
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val localUser: StateFlow<UserLocal?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userDao().getUserFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO) 
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userTheme: StateFlow<UserTheme?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userThemeDao().getThemeFlow(user.uid)
            else flowOf(null)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allBooks: StateFlow<List<Book>> = _currentUser
        .flatMapLatest { user ->
            repository.getAllCombinedData(user?.uid ?: "")
        }
        .onEach { _isDataLoading.value = false } // Signal splash screen to proceed
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val walletHistory: StateFlow<List<WalletTransaction>> = _currentUser.flatMapLatest { user ->
        if (user != null) db.userDao().getWalletHistory(user.uid)
        else flowOf(emptyList())
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var currentPlayingBook by mutableStateOf<Book?>(null)
        private set
    
    var isAudioPlaying by mutableStateOf(false)
        private set
    
    var showPlayer by mutableStateOf(false)
    var isPlayerMinimized by mutableStateOf(false)

    var showLogoutConfirm by mutableStateOf(false)   
    var showSignedOutPopup by mutableStateOf(false)  
    var showWalletHistory by mutableStateOf(false)

    private val authListener = FirebaseAuth.AuthStateListener { 
        _currentUser.value = it.currentUser 
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun refreshData() {
        // Handled automatically by the allBooks flow
    }

    /**
     * Updates the custom theme toggle and last selected theme in the database.
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
        showSignedOutPopup = true
        navController.navigate("home") {
            popUpTo(0) { inclusive = true }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}

class MainViewModelFactory(
    private val repository: BookRepository,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, db) as T
    }
}
