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

@Composable
fun IntegratedAudioBar(
    currentBook: Book?,
    externalPlayer: Player?,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit
) {
    if (currentBook != null) {
        Surface(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            tonalElevation = 4.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            AudioPlayerComponent(
                book = currentBook,
                isMinimized = true,
                onToggleMinimize = onToggleMinimize,
                onClose = onClose,
                isDarkTheme = true, // Irrelevant for minimized bar
                player = externalPlayer
            )
        }
    }
}

@Composable
fun MaximizedAudioPlayerOverlay(
    currentBook: Book?,
    isDarkTheme: Boolean,
    externalPlayer: Player?,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit
) {
    if (currentBook != null) {
        // Full screen dimmed background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggleMinimize() },
            contentAlignment = Alignment.Center
        ) {
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
    // This component is now deprecated by IntegratedAudioBar and MaximizedAudioPlayerOverlay
    // but kept for backward compatibility if needed elsewhere
}

@Composable
fun AppNavigationPopups(
    showLogoutConfirm: Boolean,
    showSignedOutPopup: Boolean,
    onLogoutConfirm: () -> Unit,
    onLogoutDismiss: () -> Unit,
    onSignedOutDismiss: () -> Unit
) {
    if (showLogoutConfirm) {
        AppPopups.LogoutConfirmation(
            onDismiss = onLogoutDismiss,
            onConfirm = onLogoutConfirm
        )
    }

    AppPopups.SignedOutSuccess(
        show = showSignedOutPopup,
        onDismiss = onSignedOutDismiss
    )
}
