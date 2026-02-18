package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * UserComponents.kt
 *
 * This file contains a collection of UI components dedicated to user profile management.
 * It includes a highly customisable avatar component, profile headers, and several
 * sections for editing personal information, addresses, and payment settings.
 */

/**
 * UserAvatar Composable
 *
 * A smart, robust avatar component designed to display user profile pictures.
 * It handles loading states with an animated spinner, provides fallback icons for errors 
 * or missing data, and supports custom overlays and click actions.
 *
 * Key features:
 * - **Smart Loading:** Displays a rotating placeholder icon while the image loads.
 * - **Fallback Logic:** Automatically uses a default local asset if no URL is provided.
 * - **Adaptive Icon Sizing:** Calculates the ideal placeholder icon size based on the 
 *   container's constraints.
 *
 * @param photoUrl The remote URL or local path for the user's photo.
 * @param modifier Custom styling for the avatar container.
 * @param iconSize Optional override for the placeholder icon size.
 * @param isLarge Flag to use a larger placeholder icon style.
 * @param onClick Optional callback for interaction.
 * @param overlay Custom composable to be rendered on top of the avatar (e.g., a progress spinner).
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
    val defaultAvatarPath = "file:///android_asset/images/users/avatars/Avatar_defult.png"
    val isUsingDefault = photoUrl.isNullOrEmpty() || photoUrl.contains("Avatar_defult")

    val avatarModifier = Modifier
        .fillMaxSize()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    // Animation for the rotating loading/placeholder icon.
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
            // Determine icon size based on available space if not explicitly provided.
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
                    // Display rotating placeholder icon while loading.
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
                    // Static fallback icon for errors.
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
 * ProfileHeader Composable
 *
 * A prominent header section for the profile screen, displaying the user's avatar 
 * with an interactive edit button and a loading state for photo uploads.
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
                // Dim the avatar and show a spinner while an upload is in progress.
                if (isUploading) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        )
        // Edit FAB overlaid on the avatar.
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
 * PersonalInfoSection Composable
 *
 * A group of input fields for managing the user's primary identity data,
 * including their formal title, names, and contact details.
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
        
        // Email is displayed in a custom Surface as it requires a multi-step verification process to edit.
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
 * ProfileAddressSection Composable
 *
 * Displays the user's primary residence or billing address in a clear, interactive row.
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
 * ProfilePaymentSection Composable
 *
 * Displays the user's chosen payment method, using contextual icons to visually
 * represent different providers (PayPal, Google Pay, etc.).
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
                // Map payment method name to a corresponding icon.
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
 * ProfileSecuritySection Composable
 *
 * Provides a dedicated area for account security actions, such as changing the password.
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
