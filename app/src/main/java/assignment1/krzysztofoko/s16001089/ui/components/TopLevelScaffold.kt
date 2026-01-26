package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

/**
 * A shared Scaffold wrapper that provides the branded University TopBar
 * and consistent layout padding for all main screens.
 */
@Composable
fun TopLevelScaffold(
    currentUser: FirebaseUser?,
    localUser: UserLocal?,
    currentRoute: String?,
    onDashboardClick: () -> Unit,
    onLogoutClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            // Show TopBar only when logged in and on specific main screens
            if (currentUser != null && 
                currentRoute != null && 
                currentRoute != "splash" && 
                currentRoute != "auth" &&
                currentRoute != "dashboard" && 
                currentRoute != "profile" && 
                currentRoute != "pdfReader/{bookId}" && 
                currentRoute != "invoice/{bookId}" && 
                currentRoute != "invoiceCreating/{bookId}") {
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val firstName = localUser?.name?.split(" ")?.firstOrNull() ?: currentUser.displayName?.split(" ")?.firstOrNull() ?: "User"
                        
                        // User Profile Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDashboardClick() }
                                .padding(4.dp)
                        ) {
                            UserAvatar(
                                photoUrl = localUser?.photoUrl ?: currentUser.photoUrl?.toString(),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Hi, $firstName",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        // Wallet & Actions Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { onDashboardClick() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    val balanceText = String.format(Locale.US, "%.2f", localUser?.balance ?: 0.0)
                                    Text(
                                        text = "£$balanceText",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = onLogoutClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        content = content
    )
}
