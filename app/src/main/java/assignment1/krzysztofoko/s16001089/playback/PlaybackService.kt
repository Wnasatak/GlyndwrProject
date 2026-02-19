package assignment1.krzysztofoko.s16001089.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import assignment1.krzysztofoko.s16001089.MainActivity

/**
 * PlaybackService is a MediaSessionService that manages background audio playback.
 * It integrates with Media3 to provide a robust playback experience that continues
 * even when the app is in the background or minimized.
 *
 * This service is responsible for:
 * 1. Initializing the ExoPlayer instance.
 * 2. Creating and managing the MediaSession.
 * 3. Handling system-level callbacks like notification dismissal and task removal.
 */
class PlaybackService : MediaSessionService() {
    // The MediaSession allows external controllers (like notifications or lock screen) to interact with the player.
    private var mediaSession: MediaSession? = null

    /**
     * Called when the service is created. This is where the player and media session are initialized.
     */
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ExoPlayer, the core engine for media playback.
        val player = ExoPlayer.Builder(this).build()
        
        // Create an intent that points to our MainActivity. 
        // This is used when the user taps on the playback notification to return to the app.
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the MediaSession and link it to the player.
        // The session activity defines which activity to launch when the notification is clicked.
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
            
        // Add a listener to handle foreground state changes and stop foreground behavior when paused.
        player.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                // If playback is paused, we can stop the foreground service but keep the notification visible.
                if (!playWhenReady) {
                    stopForeground(false)
                }
            }
        })
    }

    /**
     * Required method for MediaSessionService. 
     * Returns the session that controllers (like the system UI or Bluetooth devices) should connect to.
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * Called when the user swipes away the app from the recent tasks list.
     * This ensures playback stops and resources are cleaned up when the app is dismissed by the user.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            player.pause() // Pause playback immediately.
            player.stop()  // Stop the player.
            player.clearMediaItems() // Clear the playlist.
        }
        stopSelf() // Stop the service itself.
        super.onTaskRemoved(rootIntent)
    }

    /**
     * Called when the service is destroyed. 
     * Essential for preventing memory leaks by releasing the player and session resources.
     */
    override fun onDestroy() {
        mediaSession?.run {
            player.stop()    // Ensure player is stopped.
            player.release() // Release ExoPlayer resources.
            release()        // Release MediaSession resources.
            mediaSession = null
        }
        super.onDestroy()
    }
}
