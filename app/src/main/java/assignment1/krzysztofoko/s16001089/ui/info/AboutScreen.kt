package assignment1.krzysztofoko.s16001089.ui.info

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.UserTheme
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

/**
 * Main 'About' screen of the application.
 * Displays institutional information, project details, and provides navigation to help and developer information.
 *
 * REQUIREMENT: Intents (8%) - Implicit Intent Demonstration.
 * This screen provides one-click access to system apps via Implicit Intents:
 * 1. ACTION_SENDTO (Email)
 * 2. ACTION_DIAL (Phone)
 * 3. ACTION_VIEW (Maps)
 * 4. ACTION_VIEW (Web Browser)
 *
 * @param onBack Callback to navigate to the previous screen.
 * @param onDeveloperClick Callback to navigate to the developer info screen.
 * @param onInstructionClick Callback to navigate to the app usage/instructions screen.
 * @param onOpenThemeBuilder Callback to open the custom theme builder dialog.
 * @param currentTheme The currently selected application theme enum.
 * @param userTheme The custom theme data for the user, if available.
 * @param onThemeChange Callback to handle theme selection changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onDeveloperClick: () -> Unit,
    onInstructionClick: () -> Unit,
    onOpenThemeBuilder: () -> Unit,
    currentTheme: Theme,
    userTheme: UserTheme? = null,
    onThemeChange: (Theme) -> Unit
) {
    val context = LocalContext.current
    
    // --- ANIMATION STATE ---
    // Animate the logo rotation once when the screen appears.
    val logoRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoRotation.animateTo(360f, tween(1200, easing = FastOutSlowInEasing))
    }

    // Determine if the current theme is dark to adjust background and other elements.
    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> userTheme?.customIsDark ?: true
        else -> false
    }
    
    // A continuous pulsating glow animation for the logo's background.
    val glowAnim = rememberGlowAnimation()
    val accentColor = MaterialTheme.colorScheme.primary
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Root container with a decorative background.
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Make Scaffold transparent to show the wavy background.
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0), // Remove default insets to manage padding manually.
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
                        // Theme toggle button in the top app bar for quick access.
                        ThemeToggleButton(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange,
                            onOpenCustomBuilder = onOpenThemeBuilder,
                            isLoggedIn = currentUser != null
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Semi-transparent for a modern look.
                    )
                )
            }
        ) { padding ->
            // Adaptive container handles different screen sizes gracefully (e.g., tablets).
            AdaptiveScreenContainer(
                modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
                maxWidth = AdaptiveWidths.Standard
            ) { isTablet ->
                Column(
                    modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- BRANDED LOGO SECTION ---
                    // A decorative logo display with multiple layers for a dynamic effect.
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isTablet) 180.dp else 140.dp)) {
                        // Pulsating glow effect behind the logo.
                        Box(
                            modifier = Modifier
                                .size(if (isTablet) 140.dp else 110.dp)
                                .scale(glowAnim.first)
                                .alpha(glowAnim.second)
                                .background(Brush.radialGradient(listOf(accentColor.copy(alpha = 0.8f), Color.Transparent)), CircleShape)
                        )
                        // Main logo surface with shadow and border.
                        Surface(
                            modifier = Modifier.size(if (isTablet) 120.dp else 90.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shadowElevation = 8.dp,
                            border = BorderStroke(4.dp, accentColor.copy(alpha = glowAnim.second))
                        ) {
                            AsyncImage(
                                model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(if (isTablet) 110.dp else 80.dp)
                                    .graphicsLayer { rotationZ = logoRotation.value } // Apply rotation animation.
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = AppConstants.APP_NAME, 
                        style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.primary, 
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- INSTITUTIONAL CARDS ---
                    // Reusable cards displaying key information.
                    InfoCard(
                        icon = Icons.Default.School, 
                        title = "INSTITUTION", 
                        content = AppConstants.INSTITUTION, 
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoCard(
                        icon = Icons.AutoMirrored.Filled.Assignment, 
                        title = "PROJECT INFO", 
                        content = AppConstants.PROJECT_INFO, 
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- PROFESSIONAL SUPPORT HUB (IMPLICIT INTENTS) ---
                    // This section demonstrates implicit intents to interact with other Android apps.
                    Text(
                        text = "UNIVERSITY SUPPORT HUB", 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // 1. IMPLICIT INTENT: EMAIL - Opens an email client.
                            SupportItem(
                                icon = Icons.Default.Email,
                                title = "Email Admissions",
                                description = "enquiries@wrexham.ac.uk",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:")
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf("enquiries@wrexham.ac.uk"))
                                        putExtra(Intent.EXTRA_SUBJECT, "Student Query - ${AppConstants.STUDENT_ID}")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            
                            // 2. IMPLICIT INTENT: PHONE - Opens the phone dialer.
                            SupportItem(
                                icon = Icons.Default.Phone,
                                title = "Call Main Campus",
                                description = "+44 (0)1978 293439",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+441978293439"))
                                    context.startActivity(intent)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            
                            // 3. IMPLICIT INTENT: MAPS - Opens a map application to a specific location.
                            SupportItem(
                                icon = Icons.Default.LocationOn,
                                title = "Visit Wrexham Campus",
                                description = "Mold Rd, LL11 2AW",
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:53.0526,-3.0062?q=Wrexham+University")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps") // Explicitly prefer Google Maps if available.
                                    context.startActivity(mapIntent)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 4. IMPLICIT INTENT: WEB BROWSER - Opens a web browser to a URL.
                            SupportItem(
                                icon = Icons.Default.Language,
                                title = "Official Website",
                                description = "www.wrexham.ac.uk",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wrexham.ac.uk"))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- NAVIGATION ACTIONS ---
                    // Buttons to navigate to other informational screens.
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onInstructionClick, 
                            modifier = Modifier.weight(1f).height(56.dp), 
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Usage", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = onDeveloperClick, 
                            modifier = Modifier.weight(1.2f).height(56.dp), 
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Developer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    
                    // --- APP VERSION ---
                    Text(
                        text = "Version ${AppConstants.VERSION_NAME}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), 
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }
}

/**
 * A reusable list item for the Support Hub.
 *
 * @param icon The leading icon for the item.
 * @param title The primary text for the item.
 * @param description The secondary text (subtitle).
 * @param onClick The action to perform when the item is clicked.
 */
@Composable
private fun SupportItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container with a colored background.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(16.dp))
        // Text content.
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Trailing chevron to indicate it's clickable.
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}
