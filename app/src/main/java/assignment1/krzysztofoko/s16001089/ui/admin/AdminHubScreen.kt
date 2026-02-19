package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground

/**
 * AdminHubScreen.kt
 *
 * This screen acts as the central jumping-off point for all administrative tasks.
 * It provides a clean, card-based navigation menu to access core system management
 * modules such as enrollment, user directory, and the product catalog.
 */

/**
 * The main container for administrative navigation.
 *
 * @param onBack Callback to return to the previous screen (usually the main dashboard).
 * @param onNavigateToApplications Callback to open the enrollment/application hub.
 * @param onNavigateToUsers Callback to open the user directory management.
 * @param onNavigateToCatalog Callback to open the global product inventory management.
 * @param isDarkTheme Flag to determine background and icon styling.
 * @param onToggleTheme Callback to switch between light and dark modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(
    onBack: () -> Unit,
    onNavigateToApplications: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Shared aesthetic background for the administrative portal.
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(AppConstants.TITLE_ADMIN_HUB, fontWeight = FontWeight.Black) },
                    navigationIcon = { 
                        IconButton(onClick = onBack) { 
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") 
                        } 
                    },
                    actions = { 
                        IconButton(onClick = onToggleTheme) { 
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Toggle Theme") 
                        } 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        ) { padding ->
            // MAIN NAVIGATION COLUMN: Arranges the module links vertically.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Enrollment Module
                AdminHubCard(
                    title = "Pending Applications",
                    description = "Review and approve student course applications.",
                    icon = Icons.Default.Assignment,
                    onClick = onNavigateToApplications
                )

                // 2. User Module
                AdminHubCard(
                    title = "User Management",
                    description = "Manage student, tutor, and admin accounts.",
                    icon = Icons.Default.People,
                    onClick = onNavigateToUsers
                )

                // 3. Inventory Module
                AdminHubCard(
                    title = "Catalog Management",
                    description = "Add, edit, or remove books, courses, and gear.",
                    icon = Icons.Default.Inventory,
                    onClick = onNavigateToCatalog
                )
            }
        }
    }
}

/**
 * A reusable navigation card for the Admin Hub.
 * 
 * @param title The primary name of the administrative module.
 * @param description A short summary of what the module controls.
 * @param icon The visual representation of the module.
 * @param onClick The navigation action to perform.
 */
@Composable
fun AdminHubCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Branded Icon Container: Provides a subtle tint behind the icon for visual hierarchy.
            Surface(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Textual Information: Title and short description.
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                @Suppress("DEPRECATION")
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            // Visual hint: Indicates the card is a clickable navigation element.
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
