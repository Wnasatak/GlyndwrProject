package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.UserLocal

/**
 * NavigationComponents.kt
 *
 * This file contains high-level navigation-related UI components that wrap core functionality
 * like the global audio player and common application popups. These components are designed
 * to be integrated into the TopLevelScaffold or the root of the UI tree.
 */

/**
 * A persistent, minimized audio player bar that sits above the navigation bar.
 * This is used when an audio book is playing but the user is navigating other parts of the app.
 *
 * @param currentBook The book currently being played. If null, the bar is hidden.
 * @param externalPlayer The Media3 Player instance managing the audio playback.
 * @param onToggleMinimize Callback to expand the player to full screen.
 * @param onClose Callback to stop playback and dismiss the player.
 */
@Composable
fun IntegratedAudioBar(
    currentBook: Book?,
    externalPlayer: Player?,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit
) {
    if (currentBook != null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(), // Ensures it doesn't overlap with system nav bars
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            tonalElevation = 4.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            // Reuses the core AudioPlayerComponent in 'minimized' mode
            AudioPlayerComponent(
                book = currentBook,
                isMinimized = true,
                onToggleMinimize = onToggleMinimize,
                onClose = onClose,
                isDarkTheme = true, // Force dark/neutral look for the bottom bar
                player = externalPlayer
            )
        }
    }
}

/**
 * A full-screen overlay for the audio player.
 * Features a dimmed background that can be tapped to minimize the player back to the IntegratedAudioBar.
 *
 * @param currentBook The book currently being played.
 * @param isDarkTheme Used to adjust the internal player component's styling.
 * @param externalPlayer The Media3 Player instance.
 * @param onToggleMinimize Callback to return to the minimized state.
 * @param onClose Callback to dismiss the player entirely.
 */
@Composable
fun MaximizedAudioPlayerOverlay(
    currentBook: Book?,
    isDarkTheme: Boolean,
    externalPlayer: Player?,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit
) {
    if (currentBook != null) {
        // Overlay container with a semi-transparent black background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // No visual ripple when clicking background to minimize
                ) { onToggleMinimize() },
            contentAlignment = Alignment.Center
        ) {
            // Adaptive width ensures the player doesn't look stretched on tablets/wide screens
            Box(modifier = Modifier.padding(16.dp).adaptiveWidth(AdaptiveWidths.Medium)) {
                AudioPlayerComponent(
                    book = currentBook,
                    isMinimized = false,
                    onToggleMinimize = onToggleMinimize,
                    onClose = onClose,
                    isDarkTheme = isDarkTheme,
                    player = externalPlayer
                )
            }
        }
    }
}

/**
 * Legacy component for managing global audio player state.
 * @deprecated Superseded by [IntegratedAudioBar] and [MaximizedAudioPlayerOverlay] for better 
 * integration with the Scaffold's bottom content slot.
 */
@Composable
fun GlobalAudioPlayerOverlay(
    showPlayer: Boolean,
    currentBook: Book?,
    isMinimized: Boolean,
    isDarkTheme: Boolean,
    externalPlayer: Player?,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit,
    onSetMinimized: (Boolean) -> Unit,
    userRole: String? = null
) {
    // Kept for backward compatibility if needed in secondary navigation trees
}

/**
 * Centralized manager for common navigation-related popups.
 * Handles the logic for showing/hiding dialogs like Logout and session termination.
 *
 * @param showLogoutConfirm Trigger for the logout confirmation dialog.
 * @param showSignedOutPopup Trigger for the successful logout feedback dialog.
 * @param onLogoutConfirm Execution logic when the user confirms logout.
 * @param onLogoutDismiss Logic to hide the logout dialog without action.
 * @param onSignedOutDismiss Logic to acknowledge the sign-out success.
 */
@Composable
fun AppNavigationPopups(
    showLogoutConfirm: Boolean,
    showSignedOutPopup: Boolean,
    onLogoutConfirm: () -> Unit,
    onLogoutDismiss: () -> Unit,
    onSignedOutDismiss: () -> Unit
) {
    // Show confirmation dialog before ending the session
    if (showLogoutConfirm) {
        AppPopups.LogoutConfirmation(
            onDismiss = onLogoutDismiss,
            onConfirm = onLogoutConfirm
        )
    }

    // Informative popup displayed after a successful sign-out
    AppPopups.SignedOutSuccess(
        show = showSignedOutPopup,
        onDismiss = onSignedOutDismiss
    )
}
