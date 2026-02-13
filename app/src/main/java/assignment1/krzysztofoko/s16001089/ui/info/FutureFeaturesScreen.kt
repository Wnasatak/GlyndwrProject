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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * Screen showcasing the development roadmap and planned upcoming features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureFeaturesScreen(
    onBack: () -> Unit,
    currentTheme: Theme,
    userTheme: UserTheme? = null,
    onThemeChange: (Theme) -> Unit
) {
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> userTheme?.customIsDark ?: true
        else -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(AppConstants.TITLE_FUTURE_ROADMAP, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        ThemeToggleButton(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            AdaptiveScreenContainer(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                maxWidth = AdaptiveWidths.Standard
            ) { isTablet ->
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = AppConstants.TITLE_UPCOMING_IMPROVEMENTS,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = if (isTablet) Modifier else Modifier.padding(bottom = 8.dp)
                    )

                    RoadmapItem(
                        icon = Icons.Default.SmartToy,
                        title = "AI-POWERED RECOMMENDATIONS",
                        description = "Personalized item suggestions based on your viewing and purchase history using advanced machine learning models."
                    )

                    RoadmapItem(
                        icon = Icons.Default.LocalShipping,
                        title = "REAL-TIME TRACKING",
                        description = "Live tracking for physical university gear orders with push notifications for every step of the delivery process."
                    )

                    RoadmapItem(
                        icon = Icons.Default.Forum,
                        title = "COMMUNITY HUB",
                        description = "Discussion forums for university courses where students can share notes, ask questions, and collaborate on projects."
                    )

                    RoadmapItem(
                        icon = Icons.Default.OfflineBolt,
                        title = "FULL OFFLINE MODE",
                        description = "Enhanced offline capabilities allowing students to access all purchased courses and reading materials without any internet connection."
                    )

                    RoadmapItem(
                        icon = Icons.Default.Payment,
                        title = "EXTERNAL PAYMENTS",
                        description = "Integration with major regional banks and crypto-wallets to provide more flexibility in payment options beyond the University Account."
                    )

                    Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 8.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        @Suppress("DEPRECATION")
                        Text(AppConstants.BTN_EXCITING_STUFF, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapItem(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                @Suppress("DEPRECATION")
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
