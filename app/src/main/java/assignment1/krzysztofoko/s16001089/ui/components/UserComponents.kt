package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.R
import assignment1.krzysztofoko.s16001089.data.UserLocal
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.util.Locale

/**
 * UserComponents.kt
 *
 * This file contains modular UI components for user profile management, identification,
 * and data entry. It follows Material 3 design principles while incorporating
 * custom animations and university-specific branding.
 */

/**
 * DigitalStudentID Composable: A high-fidelity virtual identification card.
 * 
 * Features:
 * - Role-Based Theming: Colors dynamically adjust to reflect Admin (Gold), Tutor (Blue), or Student (Primary) status.
 * - Holographic Security Effect: An infinite animation loop that moves a white gradient sweep across the card, 
 *   simulating a physical security hologram.
 * - Identification Data: Displays user name, a truncated UUID as a student/staff number, and an expiry date.
 * - Interactive Elements: Includes a placeholder QR code for campus verification scanning.
 * 
 * @param user The local user profile data to populate the card.
 * @param modifier Custom layout modifiers for the card container.
 */
@Composable
fun DigitalStudentID(
    user: UserLocal?,
    modifier: Modifier = Modifier
) {
    val role = user?.role?.lowercase(Locale.ROOT) ?: "student"
    
    // --- ROLE RESOLUTION ---
    // Assign specific branding colors based on the user's institutional hierarchy.
    val (primaryColor, secondaryColor) = when (role) {
        "admin" -> Color(0xFFFFD700) to Color(0xFF434343) // Gold & Charcoal for administrators.
        "teacher", "tutor" -> Color(0xFF3B82F6) to Color(0xFF1E3A8A) // Blue & Navy for academic staff.
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer // Default theme for students.
    }

    // --- SECURITY ANIMATION ---
    // The 'shineOffset' drives the holographic light sweep. 
    // It cycles from -500f to 1000f to ensure the sweep fully clears the card bounds.
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineOffset"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(secondaryColor, secondaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.2f))
                    )
                )
        ) {
            // --- HOLOGRAPHIC OVERLAY ---
            // A semi-transparent white gradient that "shines" over the background.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.1f), Color.Transparent),
                            start = androidx.compose.ui.geometry.Offset(shineOffset, 0f),
                            end = androidx.compose.ui.geometry.Offset(shineOffset + 200f, 400f)
                        )
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Card Header: University branding and Role Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(24.dp), shape = CircleShape, color = Color.White) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "WREXHAM UNIVERSITY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Surface(
                        color = primaryColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = role.uppercase(Locale.ROOT),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (role == "admin") Color.Black else Color.White
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Card Body: User Avatar and Identity Details
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        photoUrl = user?.photoUrl,
                        modifier = Modifier
                            .size(90.dp)
                            .border(2.dp, primaryColor, CircleShape)
                    )
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = (user?.name ?: "Full Name").uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            // We truncate the internal database ID to create a readable student/staff number.
                            text = "ID: ${user?.id?.take(8)?.uppercase(Locale.ROOT) ?: "--------"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "EXPIRES: 09/2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Security QR Code for verification by campus staff.
                    Surface(
                        modifier = Modifier.size(60.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = "Scan",
                            modifier = Modifier.padding(4.dp),
                            tint = Color.Black
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Card Footer: Security certification mark
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "OFFICIAL DIGITAL CREDENTIAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * UserAvatar Composable: A robust, state-aware profile image component.
 * 
 * Features:
 * - Async Loading: Uses Coil for high-performance image fetching.
 * - Loading Feedback: Displays a rotating account icon while the image is being fetched.
 * - Error Handling: Automatically falls back to a person icon if the URL is invalid or the fetch fails.
 * - Smart Fallback: If no photoUrl is provided, it attempts to load a default local asset.
 * 
 * @param photoUrl The URI of the user image.
 * @param modifier Sizing and layout modifiers.
 * @param iconSize Explicit size for the placeholder icon.
 * @param isLarge If true, uses a larger 'Person' icon for fallback.
 * @param contentScale Image fit strategy.
 * @param onClick Optional tap action.
 * @param overlay Custom composable to draw on top of the avatar (e.g., loading spinner or badge).
 */
@Composable
fun UserAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: Int? = null,
    isLarge: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null,
    overlay: @Composable (BoxScope.() -> Unit)? = null
) {
    // Assets bundled with the APK for offline/first-run fallback.
    val defaultAvatarPath = "file:///android_asset/images/users/avatars/Avatar_defult.png"
    val isUsingDefault = photoUrl.isNullOrEmpty() || photoUrl.contains("Avatar_defult")

    val avatarModifier = Modifier
        .fillMaxSize()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    // Visual feedback for asynchronous operations.
    val infiniteTransition = rememberInfiniteTransition(label = "avatarLoad")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "rotation"
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Transparent
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val autoIconSize = when {
                isLarge && maxWidth > 100.dp -> 110.dp
                isLarge -> 40.dp
                else -> maxWidth * 0.8f
            }
            val finalIconSize = iconSize?.dp ?: autoIconSize

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (!photoUrl.isNullOrEmpty()) photoUrl else defaultAvatarPath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = avatarModifier,
                contentScale = if (isUsingDefault) ContentScale.Fit else contentScale,
                loading = {
                    // Show a spinning placeholder while loading.
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            if (isLarge) Icons.Default.Person else Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(finalIconSize).rotate(rotation),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                },
                error = {
                    // Show a static fallback icon on network failure.
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(
                            if (isLarge) Icons.Default.Person else Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(finalIconSize),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            overlay?.invoke(this)
        }
    }
}

/**
 * ProfileHeader Composable: The top-level visual block for the profile screen.
 * Encapsulates the avatar and the photo picker trigger.
 */
@Composable
fun ProfileHeader(
    photoUrl: String?,
    isUploading: Boolean,
    onPickPhoto: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(vertical = 24.dp)) {
        UserAvatar(
            photoUrl = photoUrl,
            modifier = Modifier.size(130.dp),
            isLarge = true,
            onClick = { if (!isUploading) onPickPhoto() },
            overlay = {
                // Dim the avatar and show a spinner during active server-side photo upload.
                if (isUploading) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        )
        // Edit Floating Action Button shortcut.
        SmallFloatingActionButton(
            onClick = { if (!isUploading) onPickPhoto() },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.offset(x = (-8).dp, y = (-8).dp)
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White)
        }
    }
}

/**
 * PersonalInfoSection Composable: Form for editing core identity fields.
 */
@Composable
fun PersonalInfoSection(
    title: String,
    onTitleChange: (String) -> Unit,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    surname: String,
    onSurnameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    email: String,
    onEditEmail: () -> Unit
) {
    Column {
        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title (e.g. Prof, Dr, Mr, Ms)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = firstName, onValueChange = onFirstNameChange, label = { Text("First Name") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = surname, onValueChange = onSurnameChange, label = { Text("Surname") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Email is shown in a read-only-style surface because changing it usually requires 
        // a specialized auth flow (re-authentication).
        Surface(
            onClick = onEditEmail,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Email Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Text("Edit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * ProfileAddressSection Composable: Card for managing physical or billing address.
 */
@Composable
fun ProfileAddressSection(
    address: String,
    onChangeAddress: () -> Unit
) {
    Column {
        @Suppress("DEPRECATION")
        Text("Active Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            onClick = onChangeAddress,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text(address, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text("Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}

/**
 * ProfilePaymentSection Composable: Card for managing the preferred payment method.
 */
@Composable
fun ProfilePaymentSection(
    paymentMethod: String,
    onChangePayment: () -> Unit
) {
    Column {
        @Suppress("DEPRECATION")
        Text("Active Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            onClick = onChangePayment,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        paymentMethod.contains("Google") -> Icons.Default.AccountBalanceWallet
                        paymentMethod.contains("PayPal") -> Icons.Default.Payment
                        paymentMethod.contains("Uni") -> Icons.Default.School
                        else -> Icons.Default.CreditCard
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Text(paymentMethod, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}

/**
 * ProfileSecuritySection Composable: Action buttons for account protection.
 */
@Composable
fun ProfileSecuritySection(
    onChangePassword: () -> Unit
) {
    Column {
        @Suppress("DEPRECATION")
        Text("Account Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Lock, null)
            Spacer(Modifier.width(8.dp))
            Text("Change Account Password")
        }
    }
}
