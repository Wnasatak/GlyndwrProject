package assignment1.krzysztofoko.s16001089.ui.auth

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

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
    
    var loginAttempts by remember { mutableIntStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutTimeRemaining by remember { mutableLongStateOf(0L) }
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(isLockedOut) {
        if (isLockedOut) {
            lockoutTimeRemaining = 600L 
            while (lockoutTimeRemaining > 0) {
                delay(1000)
                lockoutTimeRemaining--
            }
            isLockedOut = false
            loginAttempts = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(
            isDarkTheme = isDarkTheme, 
            animationDuration = 15000,
            wave1HeightFactor = 0.7f,
            wave2HeightFactor = 0.8f
        )
        
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
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isVerifyingEmail) {
                            Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Verify your Email", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("We've sent a link to:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                            Text(email, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(onClick = {
                                isLoading = true
                                auth.currentUser?.reload()?.addOnCompleteListener {
                                    isLoading = false
                                    if (auth.currentUser?.isEmailVerified == true) onAuthSuccess() else error = "Please verify your email first."
                                }
                            }, modifier = Modifier.fillMaxWidth()) { Text("I have verified my email") }
                            TextButton(onClick = { auth.signOut(); isVerifyingEmail = false }) { Text("Back to Login") }
                        } else if (isResettingPassword) {
                            Icon(Icons.Default.LockReset, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Reset Password", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = email, onValueChange = { email = it; error = null }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                if (email.trim().isEmpty()) { error = "Please enter your email."; return@Button }
                                isLoading = true
                                auth.sendPasswordResetEmail(email.trim()).addOnSuccessListener { isLoading = false; error = "Reset link sent!" }.addOnFailureListener { isLoading = false; error = it.localizedMessage }
                            }, modifier = Modifier.fillMaxWidth()) { Text("Send Reset Link") }
                            TextButton(onClick = { isResettingPassword = false; error = null }) { Text("Back to Sign In") }
                        } else {
                            Text(text = if (isLogin) "Welcome Back" else "Create Account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            if (!isLogin) {
                                OutlinedTextField(value = firstName, onValueChange = { firstName = it; error = null }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            OutlinedTextField(value = email, onValueChange = { email = it; error = null }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = password, onValueChange = { password = it; error = null }, label = { Text("Password") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } }, shape = RoundedCornerShape(12.dp))
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                val trimmedEmail = email.trim()
                                if (trimmedEmail.isEmpty() || password.isEmpty() || (!isLogin && firstName.isEmpty())) { error = "Please fill in all boxes."; return@Button }
                                isLoading = true
                                if (isLogin) {
                                    auth.signInWithEmailAndPassword(trimmedEmail, password).addOnSuccessListener { res -> if (res.user?.isEmailVerified == true) onAuthSuccess() else { isVerifyingEmail = true; res.user?.sendEmailVerification() }; isLoading = false }.addOnFailureListener { isLoading = false; error = "Please check your password." }
                                } else {
                                    auth.createUserWithEmailAndPassword(trimmedEmail, password).addOnSuccessListener { res ->
                                        val userId = res.user?.uid ?: return@addOnSuccessListener
                                        val userMap = mapOf(
                                            "firstName" to firstName.trim(),
                                            "email" to trimmedEmail,
                                            "balance" to 0.0,
                                            "selectedPaymentMethod" to "University Account",
                                            "photoUrl" to "file:///android_asset/images/users/avatars/Avatar_defult.png"
                                        )
                                        db.collection("users").document(userId).set(userMap)
                                        
                                        res.user?.updateProfile(userProfileChangeRequest { 
                                            displayName = firstName.trim()
                                            photoUri = Uri.parse("file:///android_asset/images/users/avatars/Avatar_defult.png") 
                                        })?.addOnCompleteListener { 
                                            res.user?.sendEmailVerification()
                                            isVerifyingEmail = true
                                            isLoading = false 
                                        }
                                    }.addOnFailureListener { isLoading = false; error = it.localizedMessage }
                                }
                            }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text(if (isLogin) "Sign In" else "Create Account")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                HorizontalDivider(modifier = Modifier.weight(1f)); Text(" OR ", style = MaterialTheme.typography.labelSmall, color = Color.Gray); HorizontalDivider(modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { /* Google logic */ },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Continue with Google", fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { isLogin = !isLogin; error = null }) { Text(if (isLogin) "New member? Create account" else "Already a member? Sign in") }
                        }

                        AnimatedVisibility(visible = error != null) {
                            Card(modifier = Modifier.padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))) {
                                Text(text = error ?: "", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
