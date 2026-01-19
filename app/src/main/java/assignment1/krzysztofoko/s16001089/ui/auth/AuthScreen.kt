package assignment1.krzysztofoko.s16001089.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.ui.info.HorizontalWavyBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var isVerifyingEmail by remember { mutableStateOf(false) }
    var isResettingPassword by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Lockout logic states
    var loginAttempts by remember { mutableIntStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutTimeRemaining by remember { mutableLongStateOf(0L) }
    
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    // Lockout timer effect
    LaunchedEffect(isLockedOut) {
        if (isLockedOut) {
            lockoutTimeRemaining = 600L // 10 minutes in seconds
            while (lockoutTimeRemaining > 0) {
                delay(1000)
                lockoutTimeRemaining--
            }
            isLockedOut = false
            loginAttempts = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            if (isVerifyingEmail) "Verify Email" 
                            else if (isResettingPassword) "Reset Password"
                            else if (isLogin) "Sign In" 
                            else "Create Account", 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            when {
                                isVerifyingEmail -> isVerifyingEmail = false
                                isResettingPassword -> { isResettingPassword = false; error = null }
                                else -> onBack()
                            }
                        }) {
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
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isVerifyingEmail) {
                            // --- EMAIL VERIFICATION UI ---
                            Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Verify your Email", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "We've sent a verification link to:", 
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(email, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Button(
                                onClick = {
                                    isLoading = true
                                    auth.currentUser?.reload()?.addOnCompleteListener {
                                        isLoading = false
                                        if (auth.currentUser?.isEmailVerified == true) {
                                            onAuthSuccess()
                                        } else {
                                            error = "Email not verified yet. Please check your inbox."
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                else Text("I have verified my email", fontWeight = FontWeight.Bold)
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    auth.currentUser?.sendEmailVerification()
                                    error = "Verification email resent!"
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Resend Verification Link")
                            }
                            
                            TextButton(onClick = { 
                                auth.signOut()
                                isVerifyingEmail = false 
                            }) {
                                Text("Back to Login")
                            }

                        } else if (isResettingPassword) {
                            // --- PASSWORD RESET UI ---
                            Icon(Icons.Default.LockReset, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Reset Password", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Enter your email to receive a reset link", 
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; error = null },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    if (email.trim().isEmpty()) {
                                        error = "Please enter your email"
                                        return@Button
                                    }
                                    isLoading = true
                                    auth.sendPasswordResetEmail(email.trim())
                                        .addOnSuccessListener { 
                                            isLoading = false
                                            error = "Reset link sent to $email"
                                        }
                                        .addOnFailureListener { 
                                            isLoading = false
                                            error = it.localizedMessage
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                else Text("Send Reset Link", fontWeight = FontWeight.Bold)
                            }
                            
                            TextButton(onClick = { isResettingPassword = false; error = null }) {
                                Text("Back to Sign In")
                            }

                        } else {
                            // --- LOGIN / REGISTER UI ---
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLogin) Icons.Default.LockOpen else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = if (isLogin) "Welcome Back" else "Join Glyndŵr Store",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            if (isLockedOut && isLogin) {
                                // --- LOCKED OUT UI ---
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.LockClock, null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Account Locked", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                        val minutes = lockoutTimeRemaining / 60
                                        val seconds = lockoutTimeRemaining % 60
                                        Text(
                                            "Try again in ${minutes}m ${seconds}s",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text("Forgot your password?", style = MaterialTheme.typography.labelMedium)
                                        Button(
                                            onClick = { isResettingPassword = true; error = null },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.padding(top = 8.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Go to Reset Password", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (!isLogin) {
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it; error = null },
                                    label = { Text("First Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; error = null },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLockedOut || !isLogin
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; error = null },
                                label = { Text("Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLockedOut || !isLogin
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (isLogin && !isLockedOut) {
                                TextButton(
                                    onClick = { isResettingPassword = true; error = null },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Forgot Password?", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        val trimmedEmail = email.trim()
                                        if (trimmedEmail.isEmpty() || password.isEmpty() || (!isLogin && firstName.isEmpty())) {
                                            error = "Please fill in all fields"
                                            return@Button
                                        }

                                        isLoading = true
                                        if (isLogin) {
                                            auth.signInWithEmailAndPassword(trimmedEmail, password)
                                                .addOnSuccessListener { result ->
                                                    isLoading = false
                                                    loginAttempts = 0
                                                    if (result.user?.isEmailVerified == true) {
                                                        onAuthSuccess()
                                                    } else {
                                                        isVerifyingEmail = true
                                                        result.user?.sendEmailVerification()
                                                    }
                                                }
                                                .addOnFailureListener { 
                                                    isLoading = false
                                                    loginAttempts++
                                                    if (loginAttempts >= 3) {
                                                        isLockedOut = true
                                                        error = "Too many failed attempts. Locked for 10 min."
                                                    } else {
                                                        error = "${it.localizedMessage} (Attempt $loginAttempts of 3)"
                                                    }
                                                }
                                        } else {
                                            auth.createUserWithEmailAndPassword(trimmedEmail, password)
                                                .addOnSuccessListener { result ->
                                                    val user = result.user
                                                    val profileUpdates = userProfileChangeRequest {
                                                        displayName = firstName.trim()
                                                    }
                                                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                                        user.sendEmailVerification()
                                                        isLoading = false
                                                        isVerifyingEmail = true
                                                    }
                                                }
                                                .addOnFailureListener { 
                                                    isLoading = false
                                                    error = it.localizedMessage
                                                }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isLockedOut || !isLogin
                                ) {
                                    Text(if (isLogin) "Sign In" else "Create Account", fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                TextButton(onClick = { 
                                    isLogin = !isLogin
                                    error = null
                                }) {
                                    Text(if (isLogin) "New member? Create account" else "Already a member? Sign in")
                                }
                            }
                        }

                        AnimatedVisibility(visible = error != null) {
                            Text(
                                text = error ?: "",
                                color = if (error?.contains("verified") == true || error?.contains("resent") == true || error?.contains("sent to") == true || error?.contains("link sent") == true) 
                                        MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AuthWavyBackground(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "authWave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val waveColor1 = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE) 
    val waveColor2 = if (isDarkTheme) Color(0xFF334155) else Color(0xFFBFDBFE) 

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height
        
        // Large horizontal waves at the bottom
        val path1 = Path().apply {
            moveTo(0f, height * 0.65f)
            for (x in 0..width.toInt() step 15) {
                val relX = x.toFloat() / width
                val y = height * 0.7f + Math.sin((relX * Math.PI + phase).toDouble()).toFloat() * 60f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path1, color = waveColor1)

        val path2 = Path().apply {
            moveTo(0f, height * 0.75f)
            for (x in 0..width.toInt() step 15) {
                val relX = x.toFloat() / width
                val y = height * 0.8f + Math.sin((relX * 1.5 * Math.PI - phase * 0.8f).toDouble()).toFloat() * 40f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.7f))
    }
}
