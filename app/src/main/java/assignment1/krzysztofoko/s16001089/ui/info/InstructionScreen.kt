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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.InfoCard

/**
 * Instruction Screen providing a user guide for the application.
 * 
 * This screen explains the core features of the Glyndŵr Store, such as browsing,
 * signing in for discounts, making purchases, and personalizing the app theme.
 * It uses a consistent visual style with the rest of the 'Info' module.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionScreen(
    onBack: () -> Unit,               // Callback to return to the previous screen
    isDarkTheme: Boolean,             // Current global theme state
    onToggleTheme: () -> Unit         // Callback to flip between Dark and Light mode
) {
    // Root container allowing for background layering
    Box(modifier = Modifier.fillMaxSize()) {
        
        // The shared wavy background component used for all informational screens
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Transparent background to show the waves behind
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                        }
                    },
                    actions = {
                        // Quick theme toggle icon button in the header
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Switch Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Glass-morphism effect
                    )
                )
            }
        ) { padding ->
            // Scrollable column to accommodate varying screen sizes and content lengths
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()) // Enables vertical scrolling
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section welcome header
                Text(
                    text = AppConstants.TITLE_WELCOME_STORE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                /**
                 * Feature Guide: Browse Items
                 * Explains the main Home screen navigation and filtering.
                 */
                InfoCard(
                    icon = Icons.Default.MenuBook,
                    title = "Browse Items",
                    content = "Use the home screen to browse through various books, audio books, and university gear. You can filter by category using the top bar.",
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )
                
                /**
                 * Feature Guide: Authentication
                 * Highlights the benefits of logging in, such as the student discount.
                 */
                InfoCard(
                    icon = Icons.Default.Person,
                    title = "Sign In",
                    content = "Sign in to your account to access your personal dashboard and see your order history. Students get an automatic 10% discount!",
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )
                
                /**
                 * Feature Guide: Purchases
                 * Describes the workflow for viewing item details and adding them to the collection.
                 */
                InfoCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "Buy & Details",
                    content = "Click on any item to see more details. If you're signed in, you can purchase items and they will appear in your dashboard.",
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )
                
                /**
                 * Feature Guide: Customization
                 * Explains how to use the theme toggle for accessibility and preference.
                 */
                InfoCard(
                    icon = Icons.Default.Settings,
                    title = "Customization",
                    content = "Toggle between Light and Dark mode using the sun/moon icon in the top bar to suit your preference.",
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Primary 'Dismiss' button at the bottom of the guide
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(AppConstants.BTN_GOT_IT, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
