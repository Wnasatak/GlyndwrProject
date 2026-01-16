package assignment1.krzysztofoko.s16001089.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    
    val names = user?.displayName?.split(" ") ?: listOf("", "")
    var firstName by remember { mutableStateOf(names.getOrNull(0) ?: "") }
    var surname by remember { mutableStateOf(names.drop(1).joinToString(" ")) }
    
    var email by remember { mutableStateOf(user?.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf(user?.photoUrl) }
    
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Section
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
            ) {
                if (selectedPhotoUri != null) {
                    AsyncImage(
                        model = selectedPhotoUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                SmallFloatingActionButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Name Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Badge, null) }
                )
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Security Verification", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text("Required only to change Email or Password", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            // Current Password (For Re-authentication)
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.VpnKey, null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // New Password Fields
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                placeholder = { Text("Leave blank to keep current") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LockClock, null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
            
            AnimatedVisibility(visible = message != null) {
                Text(
                    text = message ?: "",
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                        isError = true
                        message = "Passwords do not match!"
                        return@Button
                    }

                    loading = true
                    isError = false
                    message = "Verifying and updating..."

                    // 1. Re-authenticate if changing sensitive data
                    val needsReauth = (email != user?.email || newPassword.isNotEmpty())
                    
                    if (needsReauth && currentPassword.isEmpty()) {
                        isError = true
                        message = "Please enter current password to verify identity."
                        loading = false
                        return@Button
                    }

                    if (needsReauth) {
                        val credential = EmailAuthProvider.getCredential(user?.email!!, currentPassword)
                        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                performFullUpdate(user, firstName, surname, selectedPhotoUri, email, newPassword) { success, msg ->
                                    isError = !success
                                    message = msg
                                    loading = false
                                }
                            } else {
                                isError = true
                                message = "Verification failed: ${reauthTask.exception?.localizedMessage}"
                                loading = false
                            }
                        }
                    } else {
                        // Only updating basic profile (Name/Photo)
                        performFullUpdate(user!!, firstName, surname, selectedPhotoUri, email, "") { success, msg ->
                            isError = !success
                            message = msg
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Update Profile")
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun performFullUpdate(
    user: com.google.firebase.auth.FirebaseUser,
    firstName: String,
    surname: String,
    photoUri: Uri?,
    newEmail: String,
    newPass: String,
    onResult: (Boolean, String) -> Unit
) {
    val profileUpdates = userProfileChangeRequest {
        displayName = "$firstName $surname".trim()
        this.photoUri = photoUri
    }

    user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
        if (profileTask.isSuccessful) {
            if (newEmail.isNotEmpty() && newEmail != user.email) {
                user.updateEmail(newEmail).addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        if (newPass.isNotEmpty()) {
                            user.updatePassword(newPass).addOnCompleteListener { passTask ->
                                if (passTask.isSuccessful) onResult(true, "Profile, Email and Password updated!")
                                else onResult(false, "Profile & Email updated, but Password failed: ${passTask.exception?.localizedMessage}")
                            }
                        } else onResult(true, "Profile and Email updated!")
                    } else onResult(false, "Profile updated, but Email failed: ${emailTask.exception?.localizedMessage}")
                }
            } else if (newPass.isNotEmpty()) {
                user.updatePassword(newPass).addOnCompleteListener { passTask ->
                    if (passTask.isSuccessful) onResult(true, "Profile and Password updated!")
                    else onResult(false, "Profile updated, but Password failed: ${passTask.exception?.localizedMessage}")
                }
            } else {
                onResult(true, "Profile updated successfully!")
            }
        } else {
            onResult(false, "Profile update failed: ${profileTask.exception?.localizedMessage}")
        }
    }
}
