package assignment1.krzysztofoko.s16001089

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import assignment1.krzysztofoko.s16001089.playback.PlaybackService
import assignment1.krzysztofoko.s16001089.ui.AppNavigation
import assignment1.krzysztofoko.s16001089.ui.theme.GlyndwrProjectTheme
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

/**
 * MainActivity is the core entry point and host activity for the application.
 * It manages the lifecycle of essential system-level components including:
 * 1. UI Root: Setting the Compose content with the global theme wrapper.
 * 2. Media Playback: Initializing and managing the Media3 [MediaController] for background audio.
 * 3. Responsive Design: Calculating [WindowSizeClass] for adaptive layout logic.
 * 4. State Management: Handling high-level theme transitions (Light/Dark/Custom).
 */
class MainActivity : ComponentActivity() {
    /** Future object for asynchronous MediaController initialization. */
    private var controllerFuture: ListenableFuture<MediaController>? = null
    
    /** The active MediaController instance connected to the PlaybackService. */
    private var mediaController: MediaController? = null

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MEDIA INITIALIZATION: Establish a session token connection to the PlaybackService
        // This allows the UI to control audio even if the activity is recreated.
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        setContent {
            // RESPONSIVE UI: Determine the current screen size class (Compact, Medium, Expanded)
            val windowSizeClass = calculateWindowSizeClass(this)
            
            // GLOBAL THEME STATE: Controls the application's appearance (Defaults to DARK)
            var currentTheme by remember { mutableStateOf(Theme.DARK) }
            
            // PLAYER STATE: Reactive state to provide the media player instance to child components
            var playerState by remember { mutableStateOf<Player?>(null) }

            // ASYNC SERVICE SYNC: Connects the MediaController when the service is ready
            LaunchedEffect(controllerFuture) {
                controllerFuture?.addListener({
                    try {
                        mediaController = controllerFuture?.get()
                        playerState = mediaController
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, MoreExecutors.directExecutor())
            }

            // UI COMPOSITION: Wrap the entire navigation tree in the custom theme
            GlyndwrProjectTheme(theme = currentTheme) {
                AppNavigation(
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme -> currentTheme = newTheme },
                    externalPlayer = playerState,
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }

    /**
     * CLEANUP: Safely releases the Media3 future to prevent memory leaks 
     * and ensure the service connection is terminated correctly.
     */
    override fun onDestroy() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}
