package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.UserLocal

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
    userRole: String? = null // Added userRole to handle different positions
) {
    if (showPlayer && currentBook != null) {
        // Background Dimming when maximized
        if (!isMinimized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSetMinimized(true) }
            )
        }

        // Animated Player Component
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                // Different bottom padding based on role: 
                // Tutors/Admins have bottom menu, so we keep the 80.dp offset.
                // Students don't have it in the hub, so we use 0.dp.
                val isManagementRole = userRole == "teacher" || userRole == "tutor" || userRole == "admin"
                val bottomPadding = if (isMinimized && isManagementRole) 80.dp else 0.dp
                
                Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                    AudioPlayerComponent(
                        book = currentBook,
                        isMinimized = isMinimized,
                        onToggleMinimize = onToggleMinimize,
                        onClose = onClose,
                        isDarkTheme = isDarkTheme,
                        player = externalPlayer
                    )
                }
            }
        }
    }
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
