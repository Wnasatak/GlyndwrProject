package assignment1.krzysztofoko.s16001089.ui.auth

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
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.R
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The primary entry point for User Authentication, supporting Login, Registration, 
 * Email Verification, Password Reset, and Two-Factor Authentication (2FA).
 *
 * This screen manages a complex state machine driven by [AuthViewModel]. It features:
 * 1. **Visual Effects**: Uses [HorizontalWavyBackground] and [rememberGlowAnimation] for a modern UI.
 * 2. **Authentication Flows**:
 *    - Standard Email/Password login and registration.
 *    - Google One Tap Sign-In integration.
 *    - Firebase Email Verification.
 *    - Custom SMTP-based 2FA (Identity Verification).
 * 3. **Theming**: Supports dynamic Dark/Light mode transitions.
 *
 * @param onAuthSuccess Callback triggered when the user successfully logs in and clears all verification steps.
 * @param onBack Callback for navigating back to the previous screen.
 * @param isDarkTheme Boolean state representing current system/app theme.
 * @param onToggleTheme Callback to switch between Dark and Light themes.
 * @param snackbarHostState State used to display transient feedback messages (e.g., "Code sent").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db))
    val scope = rememberCoroutineScope()
    
    // Animated properties for the branding logo (pulsing effect)
    val glowAnim = rememberGlowAnimation()
    val glowScale = glowAnim.first
    val glowAlpha = glowAnim.second

    // Automatic navigation upon successful auth after a short delay (for showing the success popup)
    LaunchedEffect(viewModel.showSuccessPopup) {
        if (viewModel.showSuccessPopup) {
            delay(10000) // Allow user to see the success animation
            if (viewModel.showSuccessPopup) {
                viewModel.showSuccessPopup = false
                onAuthSuccess()
            }
        }
    }

    // Handles the response from the Google Sign-In Activity
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken ?: throw IllegalStateException("ID Token missing.")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            
            // Delegate Firebase credential handling to the ViewModel
            viewModel.handleGoogleSignIn(credential, idToken, context) { email ->
                scope.launch { snackbarHostState.showSnackbar("Verification code sent to $email") }
            }
        } catch (e: ApiException) {
            viewModel.isLoading = false
            viewModel.error = "Google Sign-In failed: Status Code ${e.statusCode}"
        } catch (e: Exception) {
            viewModel.isLoading = false
            viewModel.error = "Unexpected error: ${e.localizedMessage}"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Aesthetic animated background
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Hide TopBar during final success state for a cleaner transition
                if (!viewModel.showSuccessPopup && !viewModel.isAuthFinalized) {
                    CenterAlignedTopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { 
                            Text(
                                when {
                                    viewModel.isTwoFactorStep -> AppConstants.TITLE_IDENTITY_VERIFICATION
                                    viewModel.isVerifyingEmail -> AppConstants.TITLE_EMAIL_VERIFICATION
                                    viewModel.isResettingPassword -> AppConstants.TITLE_RESET_PASSWORD
                                    viewModel.isLogin -> AppConstants.TITLE_MEMBER_LOGIN
                                    else -> AppConstants.TITLE_REGISTRATION
                                }, 
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                // Smart back navigation based on current auth state
                                if (viewModel.isTwoFactorStep) { 
                                    viewModel.isTwoFactorStep = false
                                    viewModel.signOut() // Cancel 2FA and logout
                                } else if (viewModel.isResettingPassword) {
                                    viewModel.isResettingPassword = false
                                } else onBack()
                            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                        },
                        actions = {
                            IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                }
            }
        ) { paddingValues ->
            if (!viewModel.showSuccessPopup && !viewModel.isAuthFinalized) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            
                            // BRANCH 1: Two-Factor Authentication Input
                            if (viewModel.isTwoFactorStep) {
                                // Inner loading is handled by the full-screen AuthLoading popup now
                                Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(AppConstants.TITLE_SECURITY_VERIFICATION, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text("A code has been sent to your email. Please enter it below to verify your identity.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                OutlinedTextField(
                                    value = viewModel.entered2FACode, 
                                    onValueChange = { if (it.length <= 6) viewModel.entered2FACode = it; viewModel.error = null }, 
                                    label = { Text("6-Digit Verification Code") }, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, letterSpacing = 6.sp, fontWeight = FontWeight.Black)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = {
                                    if (viewModel.entered2FACode == viewModel.generated2FACode) {
                                        viewModel.finalizeAuth()
                                    } else {
                                        viewModel.error = "Invalid code. Please try again."
                                    }
                                }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                                    Text(AppConstants.BTN_VERIFY_IDENTITY)
                                }
                                TextButton(onClick = { viewModel.trigger2FA(context, viewModel.email) { scope.launch { snackbarHostState.showSnackbar("New code sent") } } }) { Text(AppConstants.BTN_RESEND_CODE) }
                            } 
                            
                            // BRANCH 2: Firebase Email Verification Instruction
                            else if (viewModel.isVerifyingEmail) {
                                Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(AppConstants.TITLE_CHECK_INBOX, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text("We've sent a verification link to:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                Text(viewModel.email, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(onClick = {
                                    viewModel.isLoading = true
                                    // Refresh user state to check if email has been verified
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
                                        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                                            viewModel.trigger2FA(context, viewModel.email) {
                                                scope.launch { snackbarHostState.showSnackbar("Verification code sent") }
                                            }
                                        } else {
                                            viewModel.isLoading = false
                                            viewModel.error = "Email not yet verified."
                                        }
                                    }
                                }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text(AppConstants.BTN_VERIFICATION_DONE) }
                                TextButton(onClick = { viewModel.signOut(); viewModel.isVerifyingEmail = false }) { Text(AppConstants.BTN_BACK_TO_LOGIN) }
                            } 
                            
                            // BRANCH 3: Password Reset Form
                            else if (viewModel.isResettingPassword) {
                                Icon(Icons.Default.LockReset, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(AppConstants.TITLE_ACCOUNT_RECOVERY, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it; viewModel.error = null }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { viewModel.resetPassword() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text(AppConstants.BTN_SEND_RESET_LINK) }
                                TextButton(onClick = { viewModel.isResettingPassword = false; viewModel.error = null }) { Text(AppConstants.BTN_RETURN_TO_LOGIN) }
                            } 
                            
                            // BRANCH 4: Main Login / Sign-Up Form
                            else {
                                AuthLogo(isLogin = viewModel.isLogin, glowScale = glowScale, glowAlpha = glowAlpha)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(text = if (viewModel.isLogin) AppConstants.TITLE_WELCOME_BACK else AppConstants.TITLE_STUDENT_REGISTRATION, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                if (!viewModel.isLogin) {
                                    OutlinedTextField(value = viewModel.firstName, onValueChange = { viewModel.firstName = it; viewModel.error = null }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it; viewModel.error = null }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(value = viewModel.password, onValueChange = { viewModel.password = it; viewModel.error = null }, label = { Text("Password") }, visualTransformation = if (viewModel.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { viewModel.passwordVisible = !viewModel.passwordVisible }) { Icon(if (viewModel.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } }, shape = RoundedCornerShape(12.dp))
                                
                                // Show "Forgot Password" link only after several failed login attempts
                                if (viewModel.isLogin && viewModel.loginAttempts >= 3) {
                                    TextButton(onClick = { viewModel.isResettingPassword = true; viewModel.error = null }) {
                                        Text("Forgot Password?", color = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = {
                                    if (viewModel.isLogin) {
                                        viewModel.handleSignIn(context) { scope.launch { snackbarHostState.showSnackbar("Verification code sent") } }
                                    } else {
                                        viewModel.handleSignUp()
                                    }
                                }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
                                    if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text(if (viewModel.isLogin) AppConstants.BTN_SIGN_IN else AppConstants.BTN_CREATE_ACCOUNT)
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                // Google Authentication Entry Point
                                OutlinedButton(
                                    onClick = {
                                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()
                                        googleSignInLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) { Icon(Icons.Default.AccountCircle, null); Spacer(Modifier.width(12.dp)); Text(if (viewModel.isLogin) AppConstants.BTN_GOOGLE_LOGIN else AppConstants.BTN_GOOGLE_SIGNUP) }

                                TextButton(onClick = { viewModel.isLogin = !viewModel.isLogin; viewModel.error = null; viewModel.loginAttempts = 0 }) { Text(if (viewModel.isLogin) "New student? Register" else "Have an account? Sign In") }
                            }

                            // Error Display
                            AnimatedVisibility(visible = viewModel.error != null) {
                                Card(modifier = Modifier.padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))) {
                                    Text(text = viewModel.error ?: "", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // --- Overlays & Popups ---

        // Global loading overlay for auth transitions (Google Sign-In, 2FA Trigger)
        AppPopups.AuthLoading(show = viewModel.isLoading)

        // Helper popup during demo/development to show the generated 2FA code (since SMTP might be slow/limited)
        AppPopups.AuthDemoCode(
            show = viewModel.showDemoPopup,
            code = viewModel.generated2FACode,
            onDismiss = { viewModel.showDemoPopup = false }
        )
        
        // Final celebration popup after successful login/registration
        AppPopups.AuthSuccess(
            show = viewModel.showSuccessPopup,
            isDarkTheme = isDarkTheme,
            onDismiss = { 
                viewModel.showSuccessPopup = false
                onAuthSuccess() 
            }
        )
    }
}
