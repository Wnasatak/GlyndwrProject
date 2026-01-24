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

class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
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

            GlyndwrProjectTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    externalPlayer = playerState
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // We don't release in onDestroy because we want it to survive rotations
        // but if we are really closing, we should. 
        // Actually, MediaController is usually managed in onStart/onStop
    }

    override fun onDestroy() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onDestroy()
    }
}
