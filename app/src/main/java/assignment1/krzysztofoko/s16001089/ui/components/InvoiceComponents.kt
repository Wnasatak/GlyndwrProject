package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

/**
 * InvoiceComponents.kt
 *
 * This file houses specialized UI components for the invoice generation workflow.
 * Its primary purpose is to provide a visually engaging and informative "waiting" state
 * while the system generates official PDF documentation.
 */

/**
 * InvoiceCreatingScreen Composable
 *
 * An immersive, full-screen experience that notifies the user that their invoice is being
 * generated. It uses various animations and progress indicators to create a sense of activity 
 * and professional service.
 *
 * Key Features:
 * - **Animated Progress:** Features both a circular and linear progress indicator synced to a 
 *   simulated background task.
 * - **Branded Shimmer:** Displays the university logo with a subtle rotation during processing.
 * - **Adaptive Layout:** Utilises `AdaptiveScreenContainer` to ensure the content looks 
 *   properly centred and scaled on both mobile and tablet devices.
 * - **Status Transition:** Automatically transitions from a "Generating" to a "Complete" state 
 *   with an animated checkmark icon.
 *
 * @param book The book or service being purchased, used to display its title in the success message.
 * @param onCreationComplete Callback triggered once the simulated progress reaches 100%.
 * @param onBack Callback for the navigation top bar's back button.
 * @param isDarkTheme Flag to ensure background and UI elements adapt to the user's theme.
 * @param onToggleTheme Callback to allow theme switching during the generation process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreatingScreen(
    book: Book,
    onCreationComplete: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // Simulated progress state (0.0 to 1.0).
    var progress by remember { mutableFloatStateOf(0f) }
    // Flag to indicate when the generation process is finished.
    var isComplete by remember { mutableStateOf(false) }

    // Animation for the rotating logo.
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Effect to simulate the time-consuming process of PDF generation and encryption.
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50) // Update frequency.
            progress += 0.02f // Increment step.
        }
        isComplete = true // Mark as finished.
        delay(1000) // Brief pause for user to see the success state.
        onCreationComplete() // Notify the parent to navigate away.
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Add a branded wavy background for visual depth.
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent, // Allow the wavy background to show through.
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text("Processing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
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
            // Use the adaptive container to handle tablet sizing automatically.
            AdaptiveScreenContainer(
                modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()),
                maxWidth = AdaptiveWidths.Standard
            ) { isTablet ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AdaptiveSpacing.contentPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Central Content Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius()),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier.padding(if (isTablet) 48.dp else 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Primary Visual Area (Spinner or Checkmark)
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isTablet) 160.dp else 120.dp)) {
                                if (!isComplete) {
                                    // Show circular progress and logo while processing.
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxSize(),
                                        strokeWidth = if (isTablet) 10.dp else 8.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                    AsyncImage(
                                        model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(if (isTablet) 80.dp else 64.dp)
                                            .graphicsLayer { rotationZ = rotation.value } // Apply the rotation animation.
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Reveal the success checkmark once finished.
                                    this@Column.AnimatedVisibility(
                                        visible = isComplete,
                                        enter = scaleIn() + fadeIn()
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Complete",
                                            modifier = Modifier.size(if (isTablet) 140.dp else 100.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Dynamic Title Text
                            @Suppress("DEPRECATION")
                            Text(
                                text = if (isComplete) "Invoice Generated!" else "Generating Invoice...",
                                style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = if (isComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Dynamic Body Text
                            Text(
                                text = if (isComplete) 
                                    "Your official document for '${book.title}' is ready for viewing."
                                    else "Please wait while we prepare your academic purchase records and apply student discounts.",
                                style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Gray,
                                lineHeight = if (isTablet) 26.sp else 22.sp
                            )

                            // Linear Progress and Percentage Label
                            if (!isComplete) {
                                Spacer(modifier = Modifier.height(32.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isTablet) 10.dp else 8.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(if (isTablet) 64.dp else 48.dp))
                    
                    // Certification Footer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().alpha(0.6f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Description, null, modifier = Modifier.size(if (isTablet) 20.dp else 16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Certified by Glyndŵr University Academic Records",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
