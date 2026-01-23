package assignment1.krzysztofoko.s16001089.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.R
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var isVerifyingEmail by remember { mutableStateOf(false) }
    var isResettingPassword by remember { mutableStateOf(false) }
    var isTwoFactorStep by remember { mutableStateOf(false) }
    var entered2FACode by remember { mutableStateOf("") }
    var generated2FACode by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    // Light pulsing animation setup
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val trigger2FA = {
        val code = (100000..999999).random().toString()
        generated2FACode = code
        Toast.makeText(context, "Verification code: $code", Toast.LENGTH_LONG).show()
        isTwoFactorStep = true
        isLoading = false
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken ?: throw IllegalStateException("ID Token missing.")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            isLoading = true
            auth.signInWithCredential(credential).addOnSuccessListener { res ->
                val user = res.user!!
                scope.launch {
                    val existing = db.userDao().getUserByEmail(user.email ?: "")
                    if (existing == null) {
                        db.userDao().upsertUser(UserLocal(
                            id = user.uid,
                            name = user.displayName ?: "Student",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString(),
                            balance = 1000.0,
                            role = "student"
                        ))
                    } else {
                        db.userDao().upsertUser(existing.copy(
                            name = user.displayName ?: existing.name,
                            photoUrl = user.photoUrl?.toString() ?: existing.photoUrl
                        ))
                    }
                    trigger2FA()
                }
            }.addOnFailureListener { e ->
                isLoading = false
                error = "Firebase Auth failed: ${e.localizedMessage}"
                Log.e("AuthScreen", "Firebase error", e)
            }
        } catch (e: ApiException) {
            isLoading = false
            error = "Google Sign-In failed: Status Code ${e.statusCode}\nMake sure SHA-1 is added to Firebase and Support Email is set."
            Log.e("AuthScreen", "Google error: ${e.statusCode}", e)
        } catch (e: Exception) {
            isLoading = false
            error = "Unexpected error: ${e.localizedMessage}"
            Log.e("AuthScreen", "Unknown error", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            when {
                                isTwoFactorStep -> "Identity Verification"
                                isVerifyingEmail -> "Email Verification"
                                isResettingPassword -> "Reset Password"
                                isLogin -> "Member Login"
                                else -> "Registration"
                            }, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isTwoFactorStep) { isTwoFactorStep = false; auth.signOut() } else onBack()
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isDarkTheme) 0.dp else 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        ), 
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) 
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        else 
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ), 
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isTwoFactorStep) {
                            Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Security Verification", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text("A code has been sent to your academic email. (Demo: Code is in the notification below)", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            OutlinedTextField(
                                value = entered2FACode, 
                                onValueChange = { if (it.length <= 6) entered2FACode = it; error = null }, 
                                label = { Text("6-Digit Verification Code") }, 
                                modifier = Modifier.fillMaxWidth(), 
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, letterSpacing = 6.sp, fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                if (entered2FACode == generated2FACode) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Successfully logged in")
                                    }
                                    onAuthSuccess()
                                } else {
                                    error = "Invalid code. Please try again."
                                }
                            }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text("Verify Identity")
                            }
                            TextButton(onClick = { trigger2FA() }) { Text("Resend Code") }
                        } else if (isVerifyingEmail) {
                            Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Check your Inbox", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("We've sent a verification link to:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                            Text(email, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(onClick = {
                                isLoading = true
                                auth.currentUser?.reload()?.addOnCompleteListener {
                                    isLoading = false
                                    if (auth.currentUser?.isEmailVerified == true) trigger2FA() else error = "Email not yet verified."
                                }
                            }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Verification Done") }
                            TextButton(onClick = { auth.signOut(); isVerifyingEmail = false }) { Text("Back to Login") }
                        } else if (isResettingPassword) {
                            Icon(Icons.Default.LockReset, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Account Recovery", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = email, onValueChange = { email = it; error = null }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                if (email.trim().isEmpty()) { error = "Email is required."; return@Button }
                                isLoading = true
                                auth.sendPasswordResetEmail(email.trim()).addOnSuccessListener { isLoading = false; error = "Reset link sent!" }.addOnFailureListener { isLoading = false; error = "Failed to send link." }
                            }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Send Reset Link") }
                            TextButton(onClick = { isResettingPassword = false; error = null }) { Text("Return to Login") }
                        } else {
                            val rotation = remember { Animatable(0f) }
                            LaunchedEffect(isLogin) {
                                rotation.snapTo(0f)
                                rotation.animateTo(
                                    targetValue = 360f,
                                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                                )
                            }

                            // Pulsing light behind the logo
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .scale(glowScale)
                                        .alpha(glowAlpha)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)
                                            ),
                                            shape = CircleShape
                                        )
                                )
                                
                                AsyncImage(
                                    model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                    contentDescription = "University Logo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .rotate(rotation.value)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = if (isLogin) "Welcome Back" else "Student Registration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            if (!isLogin) {
                                OutlinedTextField(value = firstName, onValueChange = { firstName = it; error = null }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            OutlinedTextField(value = email, onValueChange = { email = it; error = null }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = password, onValueChange = { password = it; error = null }, label = { Text("Password") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } }, shape = RoundedCornerShape(12.dp))
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                val trimmedEmail = email.trim()
                                if (trimmedEmail.isEmpty() || password.isEmpty() || (!isLogin && firstName.isEmpty())) { error = "Please fill all fields."; return@Button }
                                isLoading = true
                                if (isLogin) {
                                    auth.signInWithEmailAndPassword(trimmedEmail, password).addOnSuccessListener { res ->
                                        if (res.user?.isEmailVerified == true) trigger2FA() else { isVerifyingEmail = true; res.user?.sendEmailVerification(); isLoading = false }
                                    }.addOnFailureListener { isLoading = false; error = "Login failed. Check your password." }
                                } else {
                                    auth.createUserWithEmailAndPassword(trimmedEmail, password).addOnSuccessListener { res ->
                                        val user = res.user!!
                                        scope.launch {
                                            db.userDao().upsertUser(UserLocal(
                                                id = user.uid, 
                                                name = firstName, 
                                                email = trimmedEmail,
                                                balance = 1000.0
                                            ))
                                            user.sendEmailVerification()
                                            isVerifyingEmail = true
                                            isLoading = false
                                        }
                                    }.addOnFailureListener { isLoading = false; error = it.localizedMessage }
                                }
                            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text(if (isLogin) "Sign In" else "Create Account")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()
                                    googleSignInLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) { Icon(Icons.Default.AccountCircle, null); Spacer(Modifier.width(12.dp)); Text(if (isLogin) "Google Login" else "Google Sign up") }

                            TextButton(onClick = { isLogin = !isLogin; error = null }) { Text(if (isLogin) "New student? Register" else "Have an account? Sign In") }
                        }

                        AnimatedVisibility(visible = error != null) {
                            Card(modifier = Modifier.padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))) {
                                Text(text = error ?: "", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
