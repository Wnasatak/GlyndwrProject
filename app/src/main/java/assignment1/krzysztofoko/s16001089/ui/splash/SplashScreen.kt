package assignment1.krzysztofoko.s16001089.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.components.isTablet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SplashScreen serves as the high-impact visual entry point for the application.
 * It features a sophisticated multi-layered animation system, including spring-based
 * scaling, alpha transitions, and an infinite background sequence.
 * 
 * The screen is designed to remain visible until both a minimum temporal delay
 * is met and essential backend data has been synchronized.
 */
@Composable
fun SplashScreen(
    isLoadingData: Boolean, 
    onTimeout: () -> Unit   
) {
    // Determine screen factor for adaptive layout adjustments
    val isTablet = isTablet()

    // --- ENTRANCE ANIMATIONS ---
    // Controls the initial "pop-in" effect of the branding elements
    val entryScale = remember { Animatable(0.8f) }
    val entryAlpha = remember { Animatable(0f) }
    
    // --- SHADE TRANSITION ---
    // Manages a gradual fade-out of an overlay shade for a cinematic feel
    val startShadeFade = remember { mutableStateOf(false) }
    val shadeAlpha by animateFloatAsState(
        targetValue = if (startShadeFade.value) 0f else 1f,
        animationSpec = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
        label = "shadeFade"
    )

    // --- INFINITE AMBIENT ANIMATIONS ---
    // Provides continuous subtle movement and color shifts while waiting for data
    val infiniteTransition = rememberInfiniteTransition(label = "splashAnimations")
    
    // Background movement progress
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgProgress"
    )

    // Breathing/Pulse effect for the central logo
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Dynamic rainbow hue shift for accented visual elements
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

    // State to track if the minimum required splash duration has elapsed
    var isTimerFinished by remember { mutableStateOf(false) }

    // LIFECYCLE: Orchestrate the sequence of entrance animations and timers
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
        
        // Staggered timing for secondary cinematic effects
        delay(2000)
        startShadeFade.value = true
        
        // Finalize the splash timer after a total of 6 seconds (min wait)
        delay(4000) 
        isTimerFinished = true
    }

    // TRANSITION LOGIC: Triggers the move to the next screen only when data is ready
    LaunchedEffect(isTimerFinished, isLoadingData) {
        if (isTimerFinished && !isLoadingData) {
            onTimeout()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Render the animated background layer
            AnimatedSplashBackground(progress = animationProgress)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Adaptive layout: Balance content position based on screen height
                Spacer(modifier = Modifier.weight(if (isTablet) 0.6f else 1f))

                // Primary branding element with combined animations
                AnimatedSplashLogo(
                    scale = entryScale.value,
                    alpha = entryAlpha.value,
                    pulseScale = pulseScale,
                    shadeAlpha = shadeAlpha,
                    rainbowColor = rainbowColor
                )
                
                Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 24.dp))

                // Institutional branding text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = AppConstants.INSTITUTION,
                        style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(entryAlpha.value)
                    )
                    
                    Text(
                        text = AppConstants.APP_NAME,
                        style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = if (isTablet) 3.sp else 2.sp,
                        modifier = Modifier.alpha(entryAlpha.value)
                    )
                }
                
                // Strong vertical push to anchor branding and footer
                Spacer(modifier = Modifier.weight(if (isTablet) 2.5f else 1f))

                // Footer component displaying development and loading status
                SplashFooter(isLoadingData = isLoadingData, alpha = entryAlpha.value)
            }
        }
    }
}
