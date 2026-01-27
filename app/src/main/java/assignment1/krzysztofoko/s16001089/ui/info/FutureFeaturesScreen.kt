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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureFeaturesScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppConstants.TITLE_UPCOMING_IMPROVEMENTS,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                InfoCard(
                    icon = Icons.Default.SmartToy,
                    title = AppConstants.ROADMAP_AI_TITLE,
                    content = AppConstants.ROADMAP_AI_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                InfoCard(
                    icon = Icons.Default.LocalShipping,
                    title = AppConstants.ROADMAP_TRACKING_TITLE,
                    content = AppConstants.ROADMAP_TRACKING_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                InfoCard(
                    icon = Icons.Default.Forum,
                    title = AppConstants.ROADMAP_COMMUNITY_TITLE,
                    content = AppConstants.ROADMAP_COMMUNITY_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                InfoCard(
                    icon = Icons.Default.OfflineBolt,
                    title = AppConstants.ROADMAP_OFFLINE_TITLE,
                    content = AppConstants.ROADMAP_OFFLINE_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                InfoCard(
                    icon = Icons.Default.Wallet,
                    title = AppConstants.ROADMAP_PAYMENTS_TITLE,
                    content = AppConstants.ROADMAP_PAYMENTS_DESC,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
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
