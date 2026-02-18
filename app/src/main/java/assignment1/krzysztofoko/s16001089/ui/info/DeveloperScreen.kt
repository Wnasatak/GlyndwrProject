package assignment1.krzysztofoko.s16001089.ui.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import coil.compose.AsyncImage

/**
 * Screen displaying the developer's credentials and project metadata.
 * Provides institutional context and links to technical version information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    onBack: () -> Unit,               // Logic to return to the previous screen
    onVersionClick: () -> Unit,       // Logic to navigate to detailed build notes
    onFutureFeaturesClick: () -> Unit, // Logic to navigate to the roadmap
    currentTheme: Theme,              // The active application theme
    userTheme: UserTheme? = null,     // Custom theme state for background sync
    onThemeChange: (Theme) -> Unit    // Global theme update handler
) {
    // Collect animation values for the pulsing glow behind the avatar
    val (glowScale, glowAlpha) = rememberGlowAnimation()
    val vibrantVioletColor = Color(0xFF9D4EDD) // Branded accent color for the developer section
    
    // Resolve visual state for background waves
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> userTheme?.customIsDark ?: true
        else -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Themed, animated background waves
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Transparent scaffold to show wavy background
            topBar = {
                // Toolbar with centralized title and theme switcher
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = AppConstants.TITLE_DEVELOPER_DETAILS, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                        }
                    },
                    actions = {
                        ThemeToggleButton(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange
                        )
                    },
                    // Glassmorphic top bar surface
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            // Use adaptive container to ensure content stays centered and readable on tablets
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
                    // --- DEVELOPER AVATAR SECTION ---
                    Box(
                        contentAlignment = Alignment.Center, 
                        modifier = Modifier.size(if (isTablet) 220.dp else 180.dp)
                    ) {
                        // Background Glow: Pulses in size and transparency
                        Box(
                            modifier = Modifier
                                .size(if (isTablet) 160.dp else 130.dp)
                                .scale(glowScale)
                                .alpha(glowAlpha)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(vibrantVioletColor, Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Main Portrait Surface with high elevation and branded border
                        Surface(
                            modifier = Modifier.size(if (isTablet) 180.dp else 150.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shadowElevation = 10.dp,
                            border = BorderStroke(
                                width = 4.dp, 
                                color = vibrantVioletColor.copy(alpha = glowAlpha)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Renders the official developer photo from assets
                                AsyncImage(
                                    model = "file:///android_asset/images/users/avatars/KrzysztofOko.jpeg",
                                    contentDescription = "Official Photo",
                                    modifier = Modifier
                                        .size(if (isTablet) 170.dp else 140.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    // --- PERSONAL IDENTITY CARD ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(AdaptiveSpacing.contentPadding()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppConstants.DEVELOPER_NAME,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "${AppConstants.TEXT_STUDENT_ID}: ${AppConstants.STUDENT_ID}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // --- PROJECT CONTEXT CARD ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppConstants.INSTITUTION,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = AppConstants.PROJECT_INFO,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // --- TECHNICAL NAVIGATION ---

                    // Links to technical build version history
                    Button(
                        onClick = onVersionClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        @Suppress("DEPRECATION")
                        Text("Version Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    }

                    // Links to the development roadmap
                    Button(
                        onClick = onFutureFeaturesClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Upcoming, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        @Suppress("DEPRECATION")
                        Text(AppConstants.TITLE_FUTURE_ROADMAP, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
