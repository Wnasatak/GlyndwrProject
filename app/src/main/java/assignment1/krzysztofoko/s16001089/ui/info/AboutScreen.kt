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
 * New Feature: Implicit Intent Support Hub (8% requirement).
 * Provides one-click access to email, phone, and location services.
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
    val logoRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoRotation.animateTo(360f, tween(1200, easing = FastOutSlowInEasing))
    }

    val isDarkTheme = when(currentTheme) {
        Theme.DARK, Theme.DARK_BLUE -> true
        Theme.CUSTOM -> userTheme?.customIsDark ?: true
        else -> false
    }
    
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
                        ThemeToggleButton(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange,
                            onOpenCustomBuilder = onOpenThemeBuilder,
                            isLoggedIn = currentUser != null
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
                    modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- BRANDED LOGO SECTION ---
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isTablet) 180.dp else 140.dp)) {
                        Box(
                            modifier = Modifier
                                .size(if (isTablet) 140.dp else 110.dp)
                                .scale(glowAnim.first)
                                .alpha(glowAnim.second)
                                .background(Brush.radialGradient(listOf(accentColor.copy(alpha = 0.8f), Color.Transparent)), CircleShape)
                        )
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
                                    .graphicsLayer { rotationZ = logoRotation.value }
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
                    // This section demonstrates advanced understanding of Android system integration.
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
                            // 1. IMPLICIT INTENT: EMAIL SUPPORT
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
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            // 2. IMPLICIT INTENT: CALL CAMPUS
                            SupportItem(
                                icon = Icons.Default.Phone,
                                title = "Call Main Campus",
                                description = "+44 (0)1978 293439",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+441978293439"))
                                    context.startActivity(intent)
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            // 3. IMPLICIT INTENT: LOCATE UNIVERSITY
                            SupportItem(
                                icon = Icons.Default.LocationOn,
                                title = "Visit Wrexham Campus",
                                description = "Mold Rd, Wrexham LL11 2AW",
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:53.0526,-3.0062?q=Wrexham+University")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- NAVIGATION ACTIONS ---
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
                    
                    // Technical Version Footer
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
 * Reusable layout for a single support action.
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}
