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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

/**
 * Main 'About' screen of the application.
 * Displays institutional information, project details, and provides navigation to help and developer information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,               // Logic to navigate back to the previous screen
    onDeveloperClick: () -> Unit,     // Logic to navigate to the developer details screen
    onInstructionClick: () -> Unit,   // Logic to navigate to the usage instructions screen
    onOpenThemeBuilder: () -> Unit,   // Logic to open the custom theme creation tool
    currentTheme: Theme,              // The active application theme (DARK, SKY, FOREST, etc.)
    userTheme: UserTheme? = null,     // User-specific custom theme data from the database
    onThemeChange: (Theme) -> Unit    // Callback to update the global theme state
) {
    // --- ANIMATION STATE ---
    // Controls the initial 360-degree rotation of the University Logo
    val logoRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoRotation.animateTo(360f, tween(1200, easing = FastOutSlowInEasing))
    }

    // --- THEME LOGIC ---
    // Determines if the current screen context should be treated as "Dark Mode"
    // This affects the background waves and top app bar colors.
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> userTheme?.customIsDark ?: true
        else -> false
    }
    
    // Remembers the scale and alpha values for the glowing effect behind the logo
    val glowAnim = rememberGlowAnimation()
    val accentColor = MaterialTheme.colorScheme.primary
    val currentUser = FirebaseAuth.getInstance().currentUser

    Box(modifier = Modifier.fillMaxSize()) {
        // Renders the dynamic, themed wavy background
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Allow the wavy background to show through
            topBar = {
                // Centered App Bar with Title and Theme Switcher
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = AppConstants.TITLE_ABOUT_APP, 
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
                        // Integrated toggle for switching themes or opening the Custom Builder
                        ThemeToggleButton(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange,
                            onOpenCustomBuilder = onOpenThemeBuilder,
                            isLoggedIn = currentUser != null
                        )
                    },
                    // Use surface color with high opacity for the top bar glassmorphism
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            // Adaptive container ensures the content width stays readable on tablets
            AdaptiveScreenContainer(
                modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
                maxWidth = AdaptiveWidths.Standard
            ) { isTablet ->
                Column(
                    modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- BRANDED LOGO SECTION ---
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isTablet) 220.dp else 180.dp)) {
                        // Background Glow Effect: Pulses based on glowAnim scale/alpha
                        Box(
                            modifier = Modifier
                                .size(if (isTablet) 160.dp else 130.dp)
                                .scale(glowAnim.first)
                                .alpha(glowAnim.second)
                                .background(Brush.radialGradient(listOf(accentColor.copy(alpha = 0.8f), Color.Transparent)), CircleShape)
                        )
                        // Main Logo Container: Rounded surface with a themed border
                        Surface(
                            modifier = Modifier.size(if (isTablet) 140.dp else 110.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shadowElevation = 8.dp,
                            border = BorderStroke(4.dp, accentColor.copy(alpha = glowAnim.second))
                        ) {
                            AsyncImage(
                                model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(if (isTablet) 130.dp else 100.dp)
                                    .graphicsLayer { rotationZ = logoRotation.value } // Apply spin animation
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    // Application Identity
                    Text(
                        text = AppConstants.APP_NAME, 
                        style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.primary, 
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // --- CONTENT CARDS ---
                    // Institutional Information
                    InfoCard(
                        icon = Icons.Default.School, 
                        title = "INSTITUTION", 
                        content = AppConstants.INSTITUTION, 
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Project Context Information
                    InfoCard(
                        icon = Icons.AutoMirrored.Filled.Assignment, 
                        title = "PROJECT INFO", 
                        content = AppConstants.PROJECT_INFO, 
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // --- NAVIGATION ACTIONS ---
                    // Instruction/Help Button (Secondary Style)
                    Button(
                        onClick = onInstructionClick, 
                        modifier = Modifier.fillMaxWidth().height(60.dp), 
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, null)
                        Spacer(Modifier.width(12.dp))
                        Text(AppConstants.TITLE_HOW_TO_USE, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Developer Details Button (Primary Style)
                    Button(
                        onClick = onDeveloperClick, 
                        modifier = Modifier.fillMaxWidth().height(60.dp), 
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Person, null)
                        Spacer(Modifier.width(12.dp))
                        Text(AppConstants.TITLE_DEVELOPER_DETAILS, fontWeight = FontWeight.ExtraBold)
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    // Technical Version Footer
                    Text(
                        text = "Version ${AppConstants.VERSION_NAME}", 
                        style = MaterialTheme.typography.titleSmall, 
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), 
                        textAlign = TextAlign.Center, 
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}
