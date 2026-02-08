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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreatingScreen(
    book: Book,
    onCreationComplete: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var isComplete by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50)
            progress += 0.02f
        }
        isComplete = true
        delay(1000)
        onCreationComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
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
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isTablet) 160.dp else 120.dp)) {
                                if (!isComplete) {
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
                                            .graphicsLayer { rotationZ = rotation.value }
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
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

                            Text(
                                text = if (isComplete) "Invoice Generated!" else "Generating Invoice...",
                                style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = if (isComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

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
