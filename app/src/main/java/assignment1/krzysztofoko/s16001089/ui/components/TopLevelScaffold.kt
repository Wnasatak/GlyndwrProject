package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
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
    unreadCount: Int,
    onDashboardClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMyApplicationsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    content: @Composable (PaddingValues) -> Unit
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    val isAdmin = localUser?.role == "admin"

    Scaffold(
        topBar = {
            if (currentUser != null && 
                currentRoute != null && 
                currentRoute != AppConstants.ROUTE_SPLASH && 
                currentRoute != AppConstants.ROUTE_AUTH &&
                currentRoute != AppConstants.ROUTE_DASHBOARD && 
                currentRoute != AppConstants.ROUTE_PROFILE && 
                currentRoute != AppConstants.ROUTE_NOTIFICATIONS &&
                !currentRoute.contains(AppConstants.ROUTE_PDF_READER) && 
                !currentRoute.contains(AppConstants.ROUTE_INVOICE) && 
                !currentRoute.contains(AppConstants.ROUTE_INVOICE_CREATING)) {
                
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
                        
                        // User Profile Section (Left)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    // Admins go to Admin Hub, students go to Dashboard
                                    onDashboardClick() 
                                }
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
                        
                        // Actions Section (Right)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Only show Wallet for Students
                            if (!isAdmin) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.clickable { onWalletClick() }
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
                            }

                            // Show Store and Profile icons ONLY for Admin
                            if (isAdmin) {
                                // Store Icon (Home)
                                IconButton(onClick = onHomeClick, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        "Store",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(Modifier.width(4.dp))

                                // Profile Icon
                                IconButton(onClick = onProfileClick, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Default.Person,
                                        "Profile",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(Modifier.width(4.dp))
                            }

                            // Notification Bell
                            Box(contentAlignment = Alignment.TopEnd) {
                                IconButton(onClick = onNotificationsClick, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        "Notifications",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                if (unreadCount > 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = CircleShape,
                                        modifier = Modifier.padding(top = 2.dp, end = 2.dp).size(8.dp),
                                        border = BorderStroke(1.dp, Color.White)
                                    ) {}
                                }
                            }

                            Spacer(Modifier.width(4.dp))
                            
                            // Navbar 3 Dots Menu
                            Box {
                                IconButton(onClick = { showMoreMenu = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        "More",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    if (!isAdmin) {
                                        DropdownMenuItem(
                                            text = { Text(AppConstants.TITLE_MY_APPLICATIONS) },
                                            onClick = { 
                                                showMoreMenu = false
                                                onMyApplicationsClick() 
                                            },
                                            leadingIcon = { Icon(Icons.Default.Assignment, null) }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Admin Hub") },
                                            onClick = { 
                                                showMoreMenu = false
                                                onDashboardClick() 
                                            },
                                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text(if (isDarkTheme) "Light Mode" else "Dark Mode") },
                                        onClick = { 
                                            showMoreMenu = false
                                            onToggleTheme() 
                                        },
                                        leadingIcon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        text = { Text(AppConstants.BTN_LOG_OUT, color = MaterialTheme.colorScheme.error) },
                                        onClick = { 
                                            showMoreMenu = false
                                            onLogoutClick() 
                                        },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        content = content
    )
}
