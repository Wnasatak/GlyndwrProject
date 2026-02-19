package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import coil.compose.AsyncImage
import java.util.Locale

/**
 * AdminUserCard.kt
 *
 * A specialized list item component used in the Administrative User Directory.
 * It provides a high-level summary of a user's account, including their profile picture,
 * role, and current financial balance.
 */

/**
 * Renders a clickable card representing a system user.
 *
 * @param user The [UserLocal] data model containing student/staff information.
 * @param onClick Triggered when the card is pressed; usually navigates to full user details.
 * @param onDelete Triggered when the trash icon is pressed; invokes a deletion workflow.
 */
@Composable
fun AdminUserCard(user: UserLocal, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        // Use a high-opacity surface color to ensure legibility against wavy backgrounds.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- AVATAR SECTION ---
            if (user.photoUrl != null) {
                // Render the user's uploaded profile picture.
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // FALLBACK: Display a colored circle with the user's first initial.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape), 
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.takeIf { it.isNotEmpty() }?.take(1)?.uppercase() ?: "?", 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // --- INFORMATION SECTION ---
            Column(modifier = Modifier.weight(1f)) {
                // Dynamically build the display name including institutional titles (e.g., Dr. Nilson).
                val displayName = buildString {
                    if (!user.title.isNullOrEmpty()) {
                        append(user.title)
                        append(" ")
                    }
                    append(user.name.ifEmpty { "New User" })
                }
                
                Text(
                    text = displayName, 
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                @Suppress("DEPRECATION")
                Text(
                    text = user.email, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )

                // Role and Financial Summary Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = "Role: ${user.role.uppercase()}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        text = "Balance: £${String.format(Locale.US, "%.2f", user.balance)}", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // --- ACTIONS ---
            // Destructive action button for account removal.
            IconButton(onClick = onDelete) { 
                Icon(
                    imageVector = Icons.Default.Delete, 
                    contentDescription = "Delete User", 
                    tint = MaterialTheme.colorScheme.error 
                ) 
            }
        }
    }
}
