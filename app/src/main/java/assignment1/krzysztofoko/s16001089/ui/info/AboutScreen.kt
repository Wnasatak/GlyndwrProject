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
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

/**
 * Main 'About' screen of the application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,               
    onDeveloperClick: () -> Unit,     
    onInstructionClick: () -> Unit,   
    onOpenThemeBuilder: () -> Unit, 
    currentTheme: Theme,             
    onThemeChange: (Theme) -> Unit         
) {
    val logoRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoRotation.animateTo(360f, tween(1200, easing = FastOutSlowInEasing))
    }

    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || (currentTheme == Theme.CUSTOM)
    val glowAnim = rememberGlowAnimation()
    val accentColor = MaterialTheme.colorScheme.primary
    val currentUser = FirebaseAuth.getInstance().currentUser

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
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
                        ThemeToggleButton(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange,
                            onOpenCustomBuilder = onOpenThemeBuilder,
                            isLoggedIn = currentUser != null
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { padding ->
            AdaptiveScreenContainer(
                modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
                maxWidth = AdaptiveWidths.Standard
            ) { isTablet ->
                Column(
                    modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Branded Logo Section
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isTablet) 220.dp else 180.dp)) {
                        Box(modifier = Modifier.size(if (isTablet) 160.dp else 130.dp).scale(glowAnim.first).alpha(glowAnim.second).background(Brush.radialGradient(listOf(accentColor.copy(alpha = 0.8f), Color.Transparent)), CircleShape))
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
                                modifier = Modifier.size(if (isTablet) 130.dp else 100.dp).graphicsLayer { rotationZ = logoRotation.value }.clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = AppConstants.APP_NAME, style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))

                    // Reusable Appearance Selector component
                    if (currentUser != null) {
                        AppAppearanceSelector(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange,
                            onOpenDesigner = onOpenThemeBuilder
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    InfoCard(icon = Icons.Default.School, title = "INSTITUTION", content = AppConstants.INSTITUTION, containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoCard(icon = Icons.AutoMirrored.Filled.Assignment, title = "PROJECT INFO", content = AppConstants.PROJECT_INFO, containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(onClick = onInstructionClick, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, null); Spacer(Modifier.width(12.dp)); Text(AppConstants.TITLE_HOW_TO_USE, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDeveloperClick, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Default.Person, null); Spacer(Modifier.width(12.dp)); Text(AppConstants.TITLE_DEVELOPER_DETAILS, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "Version ${AppConstants.VERSION_NAME}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
                }
            }
        }
    }
}
