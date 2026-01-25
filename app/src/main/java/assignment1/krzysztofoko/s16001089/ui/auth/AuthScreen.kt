package assignment1.krzysztofoko.s16001089.ui.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import assignment1.krzysztofoko.s16001089.ui.components.AppPopups
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.rememberGlowAnimation
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
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
    var showDemoPopup by remember { mutableStateOf(false) }
    
    var loginAttempts by remember { mutableIntStateOf(0) }
    var pendingAuthResult by remember { mutableStateOf<Pair<String, UserLocal?>?>(null) }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val (glowScale, glowAlpha) = rememberGlowAnimation()

    val trigger2FA = { userEmail: String ->
        isLoading = true
        val code = (100000..999999).random().toString()
        generated2FACode = code
        
        scope.launch {
            val success = EmailUtils.send2FACode(context, userEmail, code)
            isLoading = false
            if (success) {
                isTwoFactorStep = true
                showDemoPopup = true 
                snackbarHostState.showSnackbar("Verification code sent to $userEmail")
            } else {
                error = "Failed to send verification code via SMTP. Please check your credentials."
            }
        }
    }

    val finalizeAuth = {
        isLoading = true
        scope.launch {
            pendingAuthResult?.let { (_, localUser) ->
                localUser?.let { db.userDao().upsertUser(it) }
                snackbarHostState.showSnackbar("Successfully logged in")
                onAuthSuccess()
            }
        }
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
                    val userData = if (existing == null) {
                        UserLocal(
                            id = user.uid,
                            name = user.displayName ?: "Student",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString(),
                            balance = 1000.0,
                            role = "student"
                        )
                    } else {
                        existing.copy(
                            name = user.displayName ?: existing.name,
                            photoUrl = user.photoUrl?.toString() ?: existing.photoUrl
                        )
                    }
                    
                    pendingAuthResult = idToken to userData
                    trigger2FA(user.email ?: "")
                }
            }.addOnFailureListener { e ->
                isLoading = false
                error = "Firebase Auth failed: ${e.localizedMessage}"
            }
        } catch (e: ApiException) {
            isLoading = false
            error = "Google Sign-In failed: Status Code ${e.statusCode}"
        } catch (e: Exception) {
            isLoading = false
            error = "Unexpected error: ${e.localizedMessage}"
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
                            if (isTwoFactorStep) { 
                                isTwoFactorStep = false
                                auth.signOut() 
                            } else if (isResettingPassword) {
                                isResettingPassword = false
                            } else onBack()
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                            if (isLoading) {
                                TwoFactorLoading()
                            } else {
                                Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Security Verification", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("A code has been sent to your email. Please enter it below to verify your identity.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                
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
                                        finalizeAuth()
                                    } else {
                                        error = "Invalid code. Please try again."
                                    }
                                }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                                    Text("Verify Identity")
                                }
                                TextButton(onClick = { trigger2FA(email) }) { Text("Resend Code") }
                            }
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
                                    if (auth.currentUser?.isEmailVerified == true) trigger2FA(auth.currentUser?.email ?: email) else error = "Email not yet verified."
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
                            AuthLogo(isLogin = isLogin, glowScale = glowScale, glowAlpha = glowAlpha)

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
                            
                            if (isLogin && loginAttempts >= 3) {
                                TextButton(onClick = { isResettingPassword = true; error = null }) {
                                    Text("Forgot Password?", color = MaterialTheme.colorScheme.error)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                val trimmedEmail = email.trim()
                                if (trimmedEmail.isEmpty() || password.isEmpty() || (!isLogin && firstName.isEmpty())) { error = "Please fill all fields."; return@Button }
                                isLoading = true
                                if (isLogin) {
                                    auth.signInWithEmailAndPassword(trimmedEmail, password).addOnSuccessListener { res ->
                                        loginAttempts = 0 
                                        if (res.user?.isEmailVerified == true) {
                                            pendingAuthResult = "" to null 
                                            trigger2FA(trimmedEmail)
                                        } else {
                                            isVerifyingEmail = true
                                            res.user?.sendEmailVerification()
                                            isLoading = false
                                        }
                                    }.addOnFailureListener { e ->
                                        isLoading = false
                                        loginAttempts++
                                        error = "Login failed. Check your password."
                                    }
                                } else {
                                    auth.createUserWithEmailAndPassword(trimmedEmail, password).addOnSuccessListener { res ->
                                        val user = res.user!!
                                        scope.launch {
                                            val userData = UserLocal(
                                                id = user.uid, 
                                                name = firstName, 
                                                email = trimmedEmail,
                                                balance = 1000.0
                                            )
                                            pendingAuthResult = "" to userData
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

                            TextButton(onClick = { isLogin = !isLogin; error = null; loginAttempts = 0 }) { Text(if (isLogin) "New student? Register" else "Have an account? Sign In") }
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

        // Use Centralized Popup System
        AppPopups.AuthDemoCode(
            show = showDemoPopup,
            code = generated2FACode,
            onDismiss = { showDemoPopup = false }
        )
    }
}
