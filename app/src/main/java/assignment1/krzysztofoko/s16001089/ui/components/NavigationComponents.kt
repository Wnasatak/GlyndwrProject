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
 * IntegratedAudioBar Composable
 *
 * A persistent, minimised audio player bar that sits neatly above the bottom navigation bar.
 * This component ensures that the user can continue listening to an audiobook while they 
 * navigate through other sections of the application.
 *
 * Key features:
 * - **Unobtrusive Presence:** Stays fixed at the bottom of the screen.
 * - **System Awareness:** Respects system navigation bars via `navigationBarsPadding`.
 * - **Reuse:** Leverages the `AudioPlayerComponent` in its minimised state for consistency.
 *
 * @param currentBook The `Book` object currently being played. If null, the bar remains hidden.
 * @param externalPlayer The Media3 `Player` instance that manages the actual audio stream.
 * @param onToggleMinimize Callback to expand the player to its full-screen immersive state.
 * @param onClose Callback to halt playback and dismiss the player bar entirely.
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
                .navigationBarsPadding(), // Ensures the bar doesn't overlap with system controls.
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            tonalElevation = 4.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            // Reuses the core player component, passing 'true' for the isMinimized parameter.
            AudioPlayerComponent(
                book = currentBook,
                isMinimized = true,
                onToggleMinimize = onToggleMinimize,
                onClose = onClose,
                isDarkTheme = true, // Force a sleek, dark appearance for the bottom bar.
                player = externalPlayer
            )
        }
    }
}

/**
 * MaximizedAudioPlayerOverlay Composable
 *
 * A full-screen, immersive overlay for the audio player. It provides a focused environment
 * for playback control, featuring a dimmed background that can be tapped to return to the 
 * previous screen.
 *
 * @param currentBook The `Book` object currently being played.
 * @param isDarkTheme Flag used to adjust internal styling based on the active theme.
 * @param externalPlayer The Media3 `Player` instance.
 * @param onToggleMinimize Callback to return the player to its minimised bar state.
 * @param onClose Callback to stop the audio and dismiss the player.
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
        // Immersive overlay container with a semi-transparent black background.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // No visual ripple when clicking the background to dismiss.
                ) { onToggleMinimize() },
            contentAlignment = Alignment.Center
        ) {
            // Adaptive width ensures the player maintains a professional look on tablets and wide screens.
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
 * integration with the main Scaffold's bottom content area.
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
    // This component is kept for backward compatibility with older navigation structures.
}

/**
 * AppNavigationPopups Composable
 *
 * A centralised manager for all common navigation-triggered popups.
 * It handles the logic for rendering essential dialogs like logout confirmation and 
 * post-sign-out feedback, ensuring consistent behaviour across the entire app.
 *
 * @param showLogoutConfirm Trigger flag for the logout confirmation dialog.
 * @param showSignedOutPopup Trigger flag for the successful logout feedback popup.
 * @param onLogoutConfirm Execution logic invoked when the user confirms they want to log out.
 * @param onLogoutDismiss Logic to hide the logout dialog without taking action.
 * @param onSignedOutDismiss Logic to acknowledge and close the sign-out success message.
 */
@Composable
fun AppNavigationPopups(
    showLogoutConfirm: Boolean,
    showSignedOutPopup: Boolean,
    onLogoutConfirm: () -> Unit,
    onLogoutDismiss: () -> Unit,
    onSignedOutDismiss: () -> Unit
) {
    // Renders the confirmation dialog before officially ending the user's session.
    if (showLogoutConfirm) {
        AppPopups.LogoutConfirmation(
            onDismiss = onLogoutDismiss,
            onConfirm = onLogoutConfirm
        )
    }

    // Renders the informative popup displayed after a successful sign-out event.
    AppPopups.SignedOutSuccess(
        show = showSignedOutPopup,
        onDismiss = onSignedOutDismiss
    )
}
