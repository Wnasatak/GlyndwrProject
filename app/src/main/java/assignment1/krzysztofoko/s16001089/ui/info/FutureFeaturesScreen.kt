package assignment1.krzysztofoko.s16001089.ui.info

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.InfoCard

/**
 * Screen presenting the project's development roadmap.
 * 
 * This screen highlights planned improvements and future functionalities such as 
 * AI recommendations, real-time tracking, and expanded community hub features. 
 * It aims to provide transparency about the app's growth and vision.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureFeaturesScreen(
    onBack: () -> Unit,               // Callback to return to the previous screen
    isDarkTheme: Boolean,             // Current global theme state
    onToggleTheme: () -> Unit         // Callback to switch between visual modes
) {
    // Standard layout structure with shared background and scrollable content
    Box(modifier = Modifier.fillMaxSize()) {
        
        // Central background pattern used across the informational module
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Overrides the default Scaffold background
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0), // Resets window padding
                    title = { 
                        Text(
                            text = AppConstants.TITLE_FUTURE_ROADMAP, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
                        }
                    },
                    actions = {
                        // Global theme toggle consistent across all app screens
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Appearance"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // High readability alpha
                    )
                )
            }
        ) { padding ->
            // Chronological list of planned feature cards
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()) // Enables scrolling for long descriptions
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section header highlighting the roadmap intent
                Text(
                    text = AppConstants.TITLE_UPCOMING_IMPROVEMENTS,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                /**
                 * ROADMAP ITEM: AI-Powered Recommendations
                 */
                InfoCard(
                    icon = Icons.Default.SmartToy,
                    title = AppConstants.ROADMAP_AI_TITLE,
                    content = AppConstants.ROADMAP_AI_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                /**
                 * ROADMAP ITEM: Real-Time Order Tracking
                 */
                InfoCard(
                    icon = Icons.Default.LocalShipping,
                    title = AppConstants.ROADMAP_TRACKING_TITLE,
                    content = AppConstants.ROADMAP_TRACKING_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                /**
                 * ROADMAP ITEM: Student Community Hub
                 */
                InfoCard(
                    icon = Icons.Default.Forum,
                    title = AppConstants.ROADMAP_COMMUNITY_TITLE,
                    content = AppConstants.ROADMAP_COMMUNITY_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                /**
                 * ROADMAP ITEM: Offline Access Mode
                 */
                InfoCard(
                    icon = Icons.Default.OfflineBolt,
                    title = AppConstants.ROADMAP_OFFLINE_TITLE,
                    content = AppConstants.ROADMAP_OFFLINE_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                /**
                 * ROADMAP ITEM: External Payment Integrations
                 */
                InfoCard(
                    icon = Icons.Default.Wallet,
                    title = AppConstants.ROADMAP_PAYMENTS_TITLE,
                    content = AppConstants.ROADMAP_PAYMENTS_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Primary affirmation button to exit the roadmap view
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(AppConstants.BTN_EXCITING_STUFF, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
