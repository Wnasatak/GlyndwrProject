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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * Screen displaying the version history and "What's New" information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionInfoScreen(
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
                    title = { 
                        Text(
                            text = AppConstants.TITLE_WHATS_NEW, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
                maxWidth = AdaptiveWidths.Standard
            ) { isTablet ->
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = AppConstants.TITLE_LATEST_UPDATES,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = if (isTablet) Modifier else Modifier.padding(bottom = 8.dp)
                    )
                    
                    InfoCard(
                        icon = Icons.Default.PictureAsPdf,
                        title = AppConstants.VER_READER_TITLE,
                        content = AppConstants.VER_READER_DESC,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )

                    InfoCard(
                        icon = Icons.Default.History,
                        title = AppConstants.VER_FINAL_DEMO_TITLE,
                        content = AppConstants.VER_FINAL_DEMO_DESC,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )

                    InfoCard(
                        icon = Icons.Default.Security,
                        title = AppConstants.VER_SECURITY_TITLE,
                        content = AppConstants.VER_SECURITY_DESC,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )

                    InfoCard(
                        icon = Icons.Default.Comment,
                        title = AppConstants.VER_REVIEWS_TITLE,
                        content = AppConstants.VER_REVIEWS_DESC,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )

                    InfoCard(
                        icon = Icons.Default.ColorLens,
                        title = AppConstants.VER_THEME_TITLE,
                        content = AppConstants.VER_THEME_DESC,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )

                    InfoCard(
                        icon = Icons.Default.Inventory,
                        title = AppConstants.VER_CATALOG_TITLE,
                        content = AppConstants.VER_CATALOG_DESC,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )
                    
                    Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 12.dp))
                    
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
                        Text(AppConstants.BTN_CLOSE, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
