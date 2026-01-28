package assignment1.krzysztofoko.s16001089

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

/**
 * Main entry point of the application.
 * Handles Activity lifecycle, theme switching, and Media3 controller initialization for audiobooks.
 */
class MainActivity : ComponentActivity() {
    // Future to handle asynchronous initialization of the Media3 Controller
    private var controllerFuture: ListenableFuture<MediaController>? = null
    // The actual media controller used to interact with the PlaybackService
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Media3 session token for background audio playback
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        setContent {
            // Global app theme state
            var isDarkTheme by remember { mutableStateOf(true) }
            // State holding the player instance, used by UI components to control playback
            var playerState by remember { mutableStateOf<Player?>(null) }

            // Listener to capture the media controller when it's ready
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

            // Dynamically update status bar color when theme changes
            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }

            // Apply global theme and set up root navigation
            GlyndwrProjectTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    externalPlayer = playerState
                )
            }
        }
    }

    override fun onDestroy() {
        // Properly release the media controller to prevent memory leaks
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}
