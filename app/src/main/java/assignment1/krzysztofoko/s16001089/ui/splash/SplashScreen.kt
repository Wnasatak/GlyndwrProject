package assignment1.krzysztofoko.s16001089.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The initial landing screen of the application.
 * 
 * This screen serves two primary purposes:
 * 1. Visual Branding: Showcases high-fidelity animations (Logo scale, pulses, rainbow hues).
 * 2. Data Synchronization: Acts as a gateway that only transitions to the home screen 
 *    once both the minimum display time has elapsed and the global data loading is finished.
 */
@Composable
fun SplashScreen(
    isLoadingData: Boolean, // External state indicating if database synchronization is ongoing
    onTimeout: () -> Unit   // Navigation callback to transition to the main app
) {
    /**
     * Entry Animation States:
     * Controls the initial "pop-in" effect of the branding elements.
     */
    val entryScale = remember { Animatable(0.8f) }
    val entryAlpha = remember { Animatable(0f) }
    
    /**
     * Visual State: Violet Shade/Glow.
     * Manages the disappearance of the background ambient light after the initial entry.
     */
    val startShadeFade = remember { mutableStateOf(false) }
    val shadeAlpha by animateFloatAsState(
        targetValue = if (startShadeFade.value) 0f else 1f,
        animationSpec = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
        label = "shadeFade"
    )

    /**
     * Infinite Loop Animations:
     * Defines the continuous movement patterns that stay active as long as the screen is visible.
     */
    val infiniteTransition = rememberInfiniteTransition(label = "splashAnimations")
    
    // Background Movement: Progresses from 0 to 1 every 8 seconds to drive path animations
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgProgress"
    )

    // Logo Pulsing: Subtly scales the logo up and down to simulate a "breathing" effect
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Logo Rainbow Hue: Cycles through the HSV color space to create a shifting color border/glow
    val rainbowHue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbowHue"
    )
    val rainbowColor = Color.hsv(rainbowHue, 0.6f, 1f)

    /**
     * Synchronization Logic:
     * Tracks the minimum display timer.
     */
    var isTimerFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Parallel launch for entry animations
        launch {
            entryScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            entryAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500)
            )
        }
        
        // Wait 2 seconds before starting the ambient light fade out
        delay(2000)
        startShadeFade.value = true
        
        // Minimum total splash duration (4 seconds)
        delay(4000) 
        isTimerFinished = true
    }

    /**
     * Gateway Effect:
     * Only allows the user into the app if:
     * 1. The 4-second timer is done.
     * 2. The ViewModel has finished loading all books/courses from the DB.
     */
    LaunchedEffect(isTimerFinished, isLoadingData) {
        if (isTimerFinished && !isLoadingData) {
            onTimeout()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Renders the moving path background (extracted to components)
            AnimatedSplashBackground(progress = animationProgress)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Renders the multi-layered animated logo (extracted to components)
                AnimatedSplashLogo(
                    scale = entryScale.value,
                    alpha = entryAlpha.value,
                    pulseScale = pulseScale,
                    shadeAlpha = shadeAlpha,
                    rainbowColor = rainbowColor
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // Institutional Branding Text
                Text(
                    text = AppConstants.INSTITUTION,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().alpha(entryAlpha.value)
                )
                
                // Application Name with specialized spacing
                Text(
                    text = AppConstants.APP_NAME,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.fillMaxWidth().alpha(entryAlpha.value)
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }

            // Renders the dynamic loading progress text at the bottom
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                SplashFooter(isLoadingData = isLoadingData, alpha = entryAlpha.value)
            }
        }
    }
}
