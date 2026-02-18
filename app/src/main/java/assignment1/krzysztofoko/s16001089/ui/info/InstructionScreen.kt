package assignment1.krzysztofoko.s16001089.ui.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.InfoCard
import assignment1.krzysztofoko.s16001089.ui.components.ThemeToggleButton
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * Instruction Screen providing a user guide for the application.
 * Explains core functionality like browsing, signing in, and purchasing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionScreen(
    onBack: () -> Unit,               // Handler to return to the previous screen
    currentTheme: Theme,              // The active application theme
    userTheme: UserTheme? = null,     // User-specific custom theme data
    onThemeChange: (Theme) -> Unit    // Callback to update global theme state
) {
    // Detect screen configuration to provide a tablet-optimized layout
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    // Resolve theme style for background wave rendering
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> userTheme?.customIsDark ?: true
        else -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Renders the themed animated background
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Allow background to show through
            topBar = {
                // Centered Top App Bar with back navigation and theme switcher
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        @Suppress("DEPRECATION")
                        Text(
                            text = AppConstants.TITLE_HOW_TO_USE, 
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
                    // Semi-transparent surface for glassmorphic effect
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            // Main content container with device-specific alignment
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier
                        // On tablets, constrain width to 600dp for better readability
                        .then(if (isTablet) Modifier.widthIn(max = 600.dp).fillMaxHeight() else Modifier.fillMaxSize())
                        .verticalScroll(rememberScrollState()) // Enable scrolling for long content
                        .padding(24.dp),
                    horizontalAlignment = if (isTablet) Alignment.CenterHorizontally else Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 0.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = AppConstants.TITLE_WELCOME_STORE,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = if (isTablet) Modifier else Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (isTablet) Spacer(modifier = Modifier.height(12.dp))
                    
                    // --- GUIDANCE CARDS ---

                    InfoCard(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "Browse Items",
                        content = "Use the home screen to browse through various books, audio books, and university gear. You can filter by category using the top bar.",
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )
                    
                    InfoCard(
                        icon = Icons.Default.Person,
                        title = "Sign In",
                        content = "Sign in to your account to access your personal dashboard and see your order history. Students get an automatic 10% discount!",
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )
                    
                    InfoCard(
                        icon = Icons.Default.ShoppingCart,
                        title = "Buy & Details",
                        content = "Click on any item to see more details. If you're signed in, you can purchase items and they will appear in your dashboard.",
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )
                    
                    InfoCard(
                        icon = Icons.Default.Settings,
                        title = "Customization",
                        content = "Change between six professional themes from the palette icon in the top bar.",
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )
                    
                    Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 24.dp))
                    
                    // Main action button to acknowledge the guide
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .then(if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth())
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        @Suppress("DEPRECATION")
                        Text(AppConstants.BTN_GOT_IT, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
