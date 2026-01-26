package assignment1.krzysztofoko.s16001089.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isLoadingData: Boolean, 
    onTimeout: () -> Unit
) {
    // Animation states for the logo entry
    val entryScale = remember { Animatable(0.8f) }
    val entryAlpha = remember { Animatable(0f) }
    
    // Animation for the violet shade/glow disappearance
    val startShadeFade = remember { mutableStateOf(false) }
    val shadeAlpha by animateFloatAsState(
        targetValue = if (startShadeFade.value) 0f else 1f,
        animationSpec = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
        label = "shadeFade"
    )

    // Infinite transition for the "swimming" background and pulsing logo
    val infiniteTransition = rememberInfiniteTransition(label = "splashAnimations")
    
    // Swimming background progress
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgProgress"
    )

    // Pulsing logo animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Rainbow Hue animation for the flashing logo effect
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

    // WAIT FOR BOTH TIMEOUT AND DATA LOADING
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

    val waterOverlay1 = Color(0xFF1E88E5).copy(alpha = 0.6f)
    val waterOverlay2 = Color(0xFF1565C0).copy(alpha = 0.7f)
    val waterOverlay3 = Color(0xFF0D47A1).copy(alpha = 0.8f)

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "file:///android_asset/images/media/GlyndwrUniversityCampus.jpg",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val width = size.width
                        val height = size.height

                        val brush = Brush.linearGradient(
                            colors = listOf(waterOverlay1, waterOverlay2, waterOverlay3, waterOverlay2, waterOverlay1),
                            start = Offset(
                                x = -width + (width * 2 * animationProgress),
                                y = -height + (height * 2 * animationProgress)
                            ),
                            end = Offset(
                                x = width * 2 * animationProgress,
                                y = height * 2 * animationProgress
                            )
                        )
                        drawRect(brush)
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(350.dp)
                            .scale(entryScale.value * pulseScale * 1.2f)
                            .alpha(entryAlpha.value * shadeAlpha)
                            .drawBehind {
                                drawCircle(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            rainbowColor.copy(alpha = 0.8f),
                                            rainbowColor.copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = size.minDimension / 2
                                    )
                                )
                            }
                    )

                    AsyncImage(
                        model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
                        contentDescription = "Glyndŵr University Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .scale(entryScale.value * pulseScale)
                            .alpha(entryAlpha.value),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(rainbowColor, BlendMode.Modulate)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = AppConstants.INSTITUTION,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(entryAlpha.value)
                )
                
                Text(
                    text = AppConstants.APP_NAME,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(entryAlpha.value)
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }

            // 4. Bottom Footer (Loading Animation + Version)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLoadingData) "Synchronising with database..." else "Database synchronized! ✓",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .alpha(entryAlpha.value) 
                )

                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .alpha(entryAlpha.value)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Version ${AppConstants.VERSION_NAME}",
                    modifier = Modifier.alpha(entryAlpha.value * 0.9f),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
