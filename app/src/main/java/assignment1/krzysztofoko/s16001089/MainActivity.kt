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
 * Main entry point of the application.
 * Handles Activity lifecycle, theme switching, and Media3 controller initialization for audiobooks.
 */
class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            var currentTheme by remember { mutableStateOf(Theme.DARK) }
            var playerState by remember { mutableStateOf<Player?>(null) }

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

            // Note: Edge-to-edge status bar logic moved to AppNavigation to support Custom Themes
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

    override fun onDestroy() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}
