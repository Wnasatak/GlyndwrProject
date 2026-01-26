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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: BookRepository,
    private val db: AppDatabase
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // User State
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val localUser: StateFlow<UserLocal?> = _currentUser
        .flatMapLatest { user ->
            if (user != null) db.userDao().getUserFlow(user.uid)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Data State
    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks = _allBooks.asStateFlow()

    private val _isDataLoading = MutableStateFlow(true)
    val isDataLoading = _isDataLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError = _loadError.asStateFlow()

    // Player State
    var currentPlayingBook by mutableStateOf<Book?>(null)
        private set
    var isAudioPlaying by mutableStateOf(false)
        private set
    var showPlayer by mutableStateOf(false)
    var isPlayerMinimized by mutableStateOf(false)

    // UI State
    var showLogoutConfirm by mutableStateOf(false)
    var showSignedOutPopup by mutableStateOf(false) // Added this state

    private val authListener = FirebaseAuth.AuthStateListener { 
        _currentUser.value = it.currentUser 
    }

    init {
        auth.addAuthStateListener(authListener)
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isDataLoading.value = true
            _loadError.value = null
            try {
                seedDatabase(db)
                repository.getAllCombinedData().collect { combined ->
                    _allBooks.value = combined ?: emptyList()
                    _isDataLoading.value = false
                }
            } catch (_: Exception) {
                _loadError.value = "Offline Database Error"
                _isDataLoading.value = false
            }
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
        showSignedOutPopup = true // Trigger the success popup
        navController.navigate("home") { popUpTo(0) }
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
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, db) as T
    }
}
