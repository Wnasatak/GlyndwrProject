package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.theme.*
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun SpinningLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    AsyncImage(
        model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
        contentDescription = "Loading...",
        modifier = modifier
            .clip(CircleShape)
            .rotate(rotation),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun HorizontalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 12000,
    wave1HeightFactor: Float = 0.75f,
    wave2HeightFactor: Float = 0.82f,
    wave1Amplitude: Float = 60f,
    wave2Amplitude: Float = 40f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) WaveSlateDarkBg else Color.White
    val waveColor1 = if (isDarkTheme) WaveSlateDark1 else WaveBlueLight1
    val waveColor2 = if (isDarkTheme) WaveSlateDark2 else WaveBlueLight2

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        
        val width = size.width
        val height = size.height
        
        // Horizontal Wave 1
        val path1 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave1HeightFactor + sin(relativeX * 2 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)
        
        // Horizontal Wave 2
        val path2 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave2HeightFactor + sin(relativeX * 3 * PI - phase * 0.7f).toFloat() * wave2Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path2, color = waveColor1.copy(alpha = 0.5f)) // Use consistent base color
    }
}

@Composable
fun VerticalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 10000,
    wave1WidthFactor: Float = 0.6f,
    wave2WidthFactor: Float = 0.75f,
    wave1Amplitude: Float = 100f,
    wave2Amplitude: Float = 60f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) WaveSlateDarkBg else Color.White
    val waveColor1 = if (isDarkTheme) WaveSlateDark1 else WaveBlueLight1
    val waveColor2 = if (isDarkTheme) WaveSlateDark2 else WaveBlueLight2

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height
        
        // Vertical Wave 1
        val path1 = Path().apply { 
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) { 
                val relativeY = y.toFloat() / height
                val x = width * wave1WidthFactor + sin(relativeY * 1.5 * PI + phase).toFloat() * wave1Amplitude
                lineTo(x, y.toFloat()) 
            }
            lineTo(width, height)
            close() 
        }
        drawPath(path1, color = waveColor1)
        
        // Vertical Wave 2
        val path2 = Path().apply { 
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) { 
                val relativeY = y.toFloat() / height
                val x = width * wave2WidthFactor + sin(relativeY * 2.5 * PI - phase * 0.8f).toFloat() * wave2Amplitude
                lineTo(x, y.toFloat()) 
            }
            lineTo(width, height)
            close() 
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.7f))
    }
}

@Composable
fun InfoCard(
    icon: ImageVector, 
    title: String, 
    content: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
    contentStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    border: BorderStroke? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(text = content, style = contentStyle)
            }
        }
    }
}

@Composable
fun rememberGlowAnimation(): Pair<Float, Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    return scale to alpha
}

@Composable
fun BookItemCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageOverlay: @Composable (BoxScope.() -> Unit)? = null,
    cornerContent: @Composable (BoxScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null,
    statusBadge: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    imageOverlay?.invoke(this)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        trailingContent?.invoke(this)
                    }
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    bottomContent?.invoke(this)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = if (book.isAudioBook) "Audio" else book.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        statusBadge?.invoke(this)
                    }
                }
            }
            
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                cornerContent?.invoke(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onCategorySelected: (String) -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = { onCategorySelected(category) },
        label = { 
            Text(
                text = category, 
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            ) 
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 1.0f)
        )
    )
}

@Composable
fun SelectionOption(
    title: String, 
    icon: ImageVector, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        shape = RoundedCornerShape(12.dp), 
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f), 
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
fun UserAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: Int = 24,
    isLarge: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null,
    overlay: @Composable (BoxScope.() -> Unit)? = null
) {
    val defaultAvatarPath = "file:///android_asset/images/users/avatars/Avatar_defult.png"
    val isUsingDefault = photoUrl.isNullOrEmpty() || photoUrl.contains("Avatar_defult")
    
    val avatarModifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    val infiniteTransition = rememberInfiniteTransition(label = "avatarLoad")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (!photoUrl.isNullOrEmpty()) photoUrl else defaultAvatarPath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = avatarModifier,
                contentScale = if (isUsingDefault) ContentScale.Fit else contentScale,
                loading = { 
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        AsyncImage(
                            model = "file:///android_asset/images/media/Glyndwr_University_Logo.png",
                            contentDescription = "Loading...",
                            modifier = Modifier.fillMaxSize().clip(CircleShape).rotate(rotation),
                            contentScale = ContentScale.Fit
                        )
                    } 
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isLarge) Icons.Default.Person else Icons.Default.AccountCircle, 
                            contentDescription = null, 
                            modifier = Modifier.size(iconSize.dp), 
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            overlay?.invoke(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpDialog(onDismiss: () -> Unit, onComplete: (Double) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var selectedAmount by remember { mutableDoubleStateOf(10.0) }
    var customAmount by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val amounts = listOf(5.0, 10.0, 20.0, 50.0)

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Top Up Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Step $step of 2", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AnimatedContent(targetState = step, label = "topUpStep") { currentStep ->
                    if (currentStep == 1) {
                        Column {
                            Text("Select Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                amounts.forEach { amt ->
                                    FilterChip(
                                        selected = selectedAmount == amt && customAmount.isEmpty(),
                                        onClick = { selectedAmount = amt; customAmount = "" },
                                        label = { Text("£${amt.toInt()}") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = customAmount,
                                onValueChange = { customAmount = it },
                                label = { Text("Custom Amount (£)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Payments, null) }
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                            val amtStr = String.format(Locale.US, "%.2f", finalAmt)
                            Icon(Icons.Default.Security, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Confirm Payment", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "You are about to add £$amtStr to your University Wallet.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            if (isProcessing) {
                                Spacer(Modifier.height(16.dp))
                                CircularProgressIndicator()
                                Text("Processing...", modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (step == 1) onDismiss() else step = 1 }, modifier = Modifier.weight(1f), enabled = !isProcessing) { 
                        Text(if (step == 1) "Cancel" else "Back") 
                    }
                    Button(
                        onClick = {
                            if (step == 1) {
                                step = 2
                            } else {
                                isProcessing = true
                                val finalAmt = customAmount.toDoubleOrNull() ?: selectedAmount
                                onComplete(finalAmt)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        Text(if (step == 1) "Next" else "Confirm & Pay")
                    }
                }
            }
        }
    }
}

@Composable
fun EmailChangeDialog(currentEmail: String, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var password by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Change Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Step $step of 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp)); Text(validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedContent(targetState = step, label = "emailStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when(currentStep) {
                            1 -> {
                                Text("Verify Identity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Text("Enter current password to proceed.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = password, onValueChange = { password = it; validationMsg = null }, label = { Text("Current Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            2 -> {
                                Text("New Email", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Text("Enter your new email address.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = newEmail, onValueChange = { newEmail = it; validationMsg = null }, label = { Text("New Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            3 -> {
                                Text("Confirmation", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Text("Updating to: $newEmail\n\nYou will be logged out to verify your new email.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                if (loading) { CircularProgressIndicator() } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { if (step == 1) onDismiss() else { step--; validationMsg = null } }, modifier = Modifier.weight(1f)) { Text(if (step == 1) "Cancel" else "Back") }
                        Button(onClick = {
                            when(step) {
                                1 -> {
                                    if (password.isEmpty()) { validationMsg = "Please enter your password."; return@Button }
                                    loading = true
                                    val cred = EmailAuthProvider.getCredential(currentEmail, password)
                                    auth.currentUser?.reauthenticate(cred)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) step = 2 else validationMsg = "Incorrect password."
                                    }
                                }
                                2 -> {
                                    if (newEmail.isEmpty() || !newEmail.contains("@")) { validationMsg = "Please enter a valid email."; return@Button }
                                    step = 3
                                }
                                3 -> {
                                    loading = true
                                    auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) onSuccess(newEmail) else validationMsg = it.exception?.localizedMessage ?: "Failed."
                                    }
                                }
                            }
                        }, modifier = Modifier.weight(1f)) { Text(if (step == 3) "Verify & Logout" else "Next") }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordChangeDialog(userEmail: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Step $step of 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp)); Text(text = validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedContent(targetState = step, label = "passStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        when(currentStep) {
                            1 -> {
                                Text("Verify it's you", style = MaterialTheme.typography.titleMedium)
                                Text("Please enter your current password so we know it's you.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it; validationMsg = null }, label = { Text("Current Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            2 -> {
                                Text("New Password", style = MaterialTheme.typography.titleMedium)
                                Text("Pick a strong password (6-20 characters).", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; validationMsg = null }, label = { Text("New Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            3 -> {
                                Text("Confirm Password", style = MaterialTheme.typography.titleMedium)
                                Text("Please type the new password one more time.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = repeatPassword, onValueChange = { repeatPassword = it; validationMsg = null }, label = { Text("Repeat Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                if (loading) { CircularProgressIndicator() } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { if (step == 1) onDismiss() else { step--; validationMsg = null } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 1) "Cancel" else "Back") }
                        Button(onClick = {
                            when(step) {
                                1 -> {
                                    if (currentPassword.isEmpty()) { validationMsg = "Please enter your current password first."; return@Button }
                                    loading = true
                                    val credential = EmailAuthProvider.getCredential(userEmail, currentPassword)
                                    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) step = 2 else validationMsg = "The password you entered doesn't seem right. Please check it."
                                    }
                                }
                                2 -> {
                                    if (newPassword.isEmpty()) { validationMsg = "The new password box is empty. Please fill it in."; return@Button }
                                    if (newPassword.length < 6) { validationMsg = "That's a bit too short! Try at least 6 characters."; return@Button }
                                    if (newPassword.length > 20) { validationMsg = "That's a bit too long! Keep it under 20 characters."; return@Button }
                                    step = 3
                                }
                                3 -> {
                                    if (repeatPassword.isEmpty()) { validationMsg = "Please repeat your new password here."; return@Button }
                                    if (newPassword != repeatPassword) { validationMsg = "The passwords don't match. Please type them again carefully."; return@Button }
                                    loading = true
                                    auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) onSuccess() else validationMsg = it.exception?.localizedMessage ?: "Something went wrong. Please try again."
                                    }
                                }
                            }
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 3) "Save" else "Next") }
                    }
                }
            }
        }
    }
}

@Composable
fun AddressManagementDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Manage Address", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Step $step of 2", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp)); Text(text = validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedContent(targetState = step, label = "addrStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (currentStep == 1) {
                            Text("Location Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = street, onValueChange = { street = it; validationMsg = null }, label = { Text("Street Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = city, onValueChange = { city = it; validationMsg = null }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        } else {
                            Text("Final Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = postcode, onValueChange = { postcode = it; validationMsg = null }, label = { Text("Postcode") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = country, onValueChange = { country = it; validationMsg = null }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (step == 1) onDismiss() else { step = 1; validationMsg = null } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 1) "Cancel" else "Back") }
                    Button(onClick = {
                        if (step == 1) {
                            if (street.isEmpty()) { validationMsg = "Please tell us your street address."; return@Button }
                            if (city.isEmpty()) { validationMsg = "The city box is empty. Please fill it in."; return@Button }
                            step = 2
                        } else {
                            if (postcode.isEmpty()) { validationMsg = "We need your postcode to continue."; return@Button }
                            if (country.isEmpty()) { validationMsg = "Please enter your country."; return@Button }
                            onSave("$street, $city, $postcode, $country")
                        }
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 2) "Save" else "Next") }
                }
            }
        }
    }
}
