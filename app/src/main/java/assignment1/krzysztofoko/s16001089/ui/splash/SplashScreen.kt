package assignment1.krzysztofoko.s16001089.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * The initial landing screen of the application.
 * Updated to use centralized isTablet() utility for better scaling on large screens.
 */
@Composable
fun SplashScreen(
    isLoadingData: Boolean, 
    onTimeout: () -> Unit   
) {
    val isTablet = isTablet()

    val entryScale = remember { Animatable(0.8f) }
    val entryAlpha = remember { Animatable(0f) }
    
    val startShadeFade = remember { mutableStateOf(false) }
    val shadeAlpha by animateFloatAsState(
        targetValue = if (startShadeFade.value) 0f else 1f,
        animationSpec = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
        label = "shadeFade"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashAnimations")
    
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgProgress"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

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

    var isTimerFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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
        
        delay(2000)
        startShadeFade.value = true
        
        delay(4000) 
        isTimerFinished = true
    }

    LaunchedEffect(isTimerFinished, isLoadingData) {
        if (isTimerFinished && !isLoadingData) {
            onTimeout()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedSplashBackground(progress = animationProgress)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                AnimatedSplashLogo(
                    scale = entryScale.value,
                    alpha = entryAlpha.value,
                    pulseScale = pulseScale,
                    shadeAlpha = shadeAlpha,
                    rainbowColor = rainbowColor
                )
                
                Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 24.dp))

                // Institutional Branding Text - Larger on tablets
                Text(
                    text = AppConstants.INSTITUTION,
                    style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().alpha(entryAlpha.value)
                )
                
                // Application Name - Larger on tablets
                Text(
                    text = AppConstants.APP_NAME,
                    style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = if (isTablet) 4.sp else 2.sp,
                    modifier = Modifier.fillMaxWidth().alpha(entryAlpha.value)
                )
                
                Spacer(modifier = Modifier.weight(1f))

                SplashFooter(isLoadingData = isLoadingData, alpha = entryAlpha.value)
            }
        }
    }
}
