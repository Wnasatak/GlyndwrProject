package assignment1.krzysztofoko.s16001089.ui.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.InfoCard

/**
 * Instruction Screen providing a user guide for the application.
 * Optimized for both phone (original layout) and tablet (centered/squeezed).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
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
            // Box for global content alignment: Centered on tablet, start-aligned on phone
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier
                        .then(if (isTablet) Modifier.widthIn(max = 600.dp).fillMaxHeight() else Modifier.fillMaxSize())
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = if (isTablet) Alignment.CenterHorizontally else Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 0.dp)
                ) {
                    Text(
                        text = AppConstants.TITLE_WELCOME_STORE,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = if (isTablet) Modifier else Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (isTablet) Spacer(modifier = Modifier.height(12.dp))
                    
                    InfoCard(
                        icon = Icons.Default.MenuBook,
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
                        content = "Toggle between Light and Dark mode using the sun/moon icon in the top bar to suit your preference.",
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    )
                    
                    Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 24.dp))
                    
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .then(if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth())
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(AppConstants.BTN_GOT_IT, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
