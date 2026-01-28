package assignment1.krzysztofoko.s16001089.ui.info

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage

/**
 * Main 'About' screen of the application.
 * 
 * This screen provides users with general information about the app, 
 * including the university name, project details, and the current version.
 * It serves as a hub for navigating to more specific information like 
 * developer details and usage instructions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,               // Callback to return to previous screen
    onDeveloperClick: () -> Unit,     // Callback to navigate to Developer details
    onInstructionClick: () -> Unit,   // Callback to navigate to 'How to Use'
    isDarkTheme: Boolean,             // Current theme state
    onToggleTheme: () -> Unit         // Callback to toggle theme
) {
    /**
     * Logo Rotation Animation:
     * One-time 360-degree spin when the screen is first loaded to draw attention.
     */
    val logoRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoRotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    /**
     * Glow Animation:
     * Continuous pulsating effect used for the background of the university logo.
     */
    val glowAnim = rememberGlowAnimation()
    val glowScale = glowAnim.first
    val glowAlpha = glowAnim.second

    val accentColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        // Unified wavy background consistent with the rest of the info screens
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Allows the wavy background to show through
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(AppConstants.TITLE_ABOUT_APP, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Action icon to switch between Light and Dark mode
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Branding Section: pulsing logo with rotation
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    // Pulstating Glow Layer
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(glowScale)
                            .alpha(glowAlpha)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.8f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    // Logo Container Card
                    Surface(
                        modifier = Modifier.size(110.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shadowElevation = 8.dp,
                        border = BorderStroke(
                            width = 4.dp, 
                            color = accentColor.copy(alpha = glowAlpha)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                                contentDescription = "Glyndŵr Logo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .graphicsLayer { rotationZ = logoRotation.value }
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Official Application Name
                Text(
                    text = AppConstants.APP_NAME,
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Information Card: Institution Name
                InfoCard(
                    icon = Icons.Default.School,
                    title = "INSTITUTION",
                    content = AppConstants.INSTITUTION,
                    contentStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Information Card: Project/Module Context
                InfoCard(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "PROJECT INFO",
                    content = AppConstants.PROJECT_INFO,
                    contentStyle = MaterialTheme.typography.bodyMedium,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action: Navigation to 'How to Use' instructions
                Button(
                    onClick = onInstructionClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(AppConstants.TITLE_HOW_TO_USE, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Action: Navigation to Developer profile
                Button(
                    onClick = onDeveloperClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(AppConstants.TITLE_DEVELOPER_DETAILS, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Static Footer showing current app version
                @Suppress("DEPRECATION")
                Text(
                    text = "Version ${AppConstants.VERSION_NAME}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
        }
    }
}
