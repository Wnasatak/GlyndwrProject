package assignment1.krzysztofoko.s16001089.ui.auth // Package location for authentication components

import androidx.compose.animation.core.Animatable // For managing animated values
import androidx.compose.animation.core.FastOutSlowInEasing // Smooth easing for animations
import androidx.compose.animation.core.tween // Definition for duration-based animations
import androidx.compose.foundation.background // To set background colors or brushes
import androidx.compose.foundation.border // To draw borders around elements
import androidx.compose.foundation.layout.* // Standard layout components like Box, Column, Spacer
import androidx.compose.foundation.shape.CircleShape // Predefined circular shape
import androidx.compose.material3.* // Material Design 3 components
import androidx.compose.runtime.* // Core Compose state management
import androidx.compose.ui.Alignment // Alignment options for layouts
import androidx.compose.ui.Modifier // Tool to decorate and add behavior to UI elements
import androidx.compose.ui.draw.clip // Clips content to a specific shape
import androidx.compose.ui.draw.rotate // Applies rotation to an element
import androidx.compose.ui.draw.scale // Scales an element up or down
import androidx.compose.ui.draw.alpha // Sets transparency levels
import androidx.compose.ui.graphics.Brush // For gradients and advanced coloring
import androidx.compose.ui.graphics.Color // Color definitions
import androidx.compose.ui.layout.ContentScale // Rules for scaling image content
import androidx.compose.ui.text.font.FontWeight // Font weight styles (bold, semi-bold, etc)
import androidx.compose.ui.unit.dp // Density-independent pixel units
import coil.compose.AsyncImage // Asynchronous image loader for Compose

@Composable // Marks function as a UI component
fun AuthLogo( // Component displaying the University logo with a glow
    isLogin: Boolean, // Trigger for the spin animation
    glowScale: Float, // Animated scale value for the glow
    glowAlpha: Float  // Animated transparency value for the glow
) {
    val rotation = remember { Animatable(0f) } // Holds and preserves the rotation state
    LaunchedEffect(isLogin) { // Runs side-effect logic when isLogin changes
        rotation.snapTo(0f) // Instantly resets rotation to zero
        rotation.animateTo( // Starts the smooth spinning animation
            targetValue = 360f, // Spin one full circle
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing) // Set time and curve
        )
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) { // Center-aligned parent container
        Box( // The background "glow" effect box
            modifier = Modifier
                .size(100.dp) // Base size of the glow
                .scale(glowScale) // Applies animated pulse scale
                .alpha(glowAlpha) // Applies animated pulse transparency
                .background( // Creates the radial gradient look
                    brush = Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)
                    ),
                    shape = CircleShape // Shape of the glow
                )
        )
        
        AsyncImage( // The actual University logo image
            model = "file:///android_asset/images/media/GlyndwrUniversity.jpg", // Path to the asset
            contentDescription = "University Logo", // Accessibility description
            modifier = Modifier
                .size(100.dp) // Size of the logo
                .rotate(rotation.value) // Applies the animated spin
                .clip(CircleShape) // Makes the image circular
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape), // Adds a themed border
            contentScale = ContentScale.Crop // Fills the circle perfectly
        )
    }
}

@Composable // Composable for the 2FA / Identity verification state
fun TwoFactorLoading() {
    Column( // Vertical layout container
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), // Takes full width with padding
        horizontalAlignment = Alignment.CenterHorizontally // Centers all child elements
    ) {
        CircularProgressIndicator( // Animated loading spinner
            modifier = Modifier.size(64.dp), // Spinner size
            strokeWidth = 6.dp, // Thickness of the spinner line
            color = MaterialTheme.colorScheme.primary // Themed color
        )
        Spacer(modifier = Modifier.height(24.dp)) // Adds vertical gap between spinner and text
        Text( // Primary status message
            "Verifying Identity...",
            style = MaterialTheme.typography.titleMedium, // Themed text style
            fontWeight = FontWeight.SemiBold, // Slightly bold weight
            color = MaterialTheme.colorScheme.primary // Themed color
        )
        Text( // Secondary sub-message
            "Please wait a moment",
            style = MaterialTheme.typography.bodySmall, // Smaller text style
            color = MaterialTheme.colorScheme.onSurfaceVariant // Subdued surface color
        )
    }
}
