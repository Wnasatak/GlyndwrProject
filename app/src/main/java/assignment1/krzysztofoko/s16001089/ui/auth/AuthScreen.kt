package assignment1.krzysztofoko.s16001089.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The primary entry point for User Authentication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (role: String) -> Unit,
    onBack: () -> Unit,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db))
    val scope = rememberCoroutineScope()
    
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE

    val glowAnim = rememberGlowAnimation()
    val glowScale = glowAnim.first
    val glowAlpha = glowAnim.second

    val userRole = viewModel.pendingAuthResult?.second?.role ?: "user"

    LaunchedEffect(viewModel.showSuccessPopup) {
        if (viewModel.showSuccessPopup) {
            delay(10000)
            if (viewModel.showSuccessPopup) {
                viewModel.showSuccessPopup = false
                onAuthSuccess(userRole)
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
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
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
                                    else -> "Create Account"
                                }, 
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (viewModel.isTwoFactorStep) { 
                                    viewModel.isTwoFactorStep = false
                                    viewModel.signOut(viewModel.pendingAuthResult?.second)
                                } else if (viewModel.isResettingPassword) {
                                    viewModel.isResettingPassword = false
                                } else onBack()
                            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                        },
                        actions = {
                            // Fixed: Wrapped in Box to ensure correct menu positioning
                            Box {
                                ThemeToggleButton(
                                    currentTheme = currentTheme,
                                    onThemeChange = onThemeChange,
                                    isLoggedIn = false // Guests can change themes
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                }
            }
        ) { paddingValues ->
            if (!viewModel.showSuccessPopup && !viewModel.isAuthFinalized) {
                AdaptiveScreenContainer(
                    modifier = Modifier.padding(paddingValues),
                    maxWidth = AdaptiveWidths.Standard
                ) { isTablet ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = if (isTablet) 0.dp else 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (!isTablet) Modifier.padding(bottom = 8.dp) else Modifier)
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
                                
                                if (viewModel.isTwoFactorStep) {
                                    Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    @Suppress("DEPRECATION")
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
                                        @Suppress("DEPRECATION")
                                        Text(AppConstants.BTN_VERIFY_IDENTITY)
                                    }
                                    TextButton(onClick = { viewModel.trigger2FA(context, viewModel.email) { scope.launch { snackbarHostState.showSnackbar("New code sent") } } }) { Text(AppConstants.BTN_RESEND_CODE) }
                                } 
                                
                                else if (viewModel.isVerifyingEmail) {
                                    Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    @Suppress("DEPRECATION")
                                    Text(AppConstants.TITLE_CHECK_INBOX, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    @Suppress("DEPRECATION")
                                    Text("We've sent a verification link to:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                    Text(viewModel.email, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(32.dp))
                                    Button(onClick = {
                                        viewModel.isLoading = true
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
                                    @Suppress("DEPRECATION")
                                    TextButton(onClick = { viewModel.signOut(viewModel.pendingAuthResult?.second); viewModel.isVerifyingEmail = false }) { Text(AppConstants.BTN_BACK_TO_LOGIN) }
                                } 
                                
                                else if (viewModel.isResettingPassword) {
                                    Icon(Icons.Default.LockReset, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    @Suppress("DEPRECATION")
                                    Text(AppConstants.TITLE_RESET_PASSWORD, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    @Suppress("DEPRECATION")
                                    Text("Enter your email to receive a password reset link.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
                                    
                                    OutlinedTextField(
                                        value = viewModel.email, 
                                        onValueChange = { viewModel.email = it }, 
                                        label = { Text("Email") }, 
                                        modifier = Modifier.fillMaxWidth(), 
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = { Icon(Icons.Default.Email, null) }
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(onClick = { viewModel.resetPassword() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                                        @Suppress("DEPRECATION")
                                        Text(AppConstants.BTN_SEND_RESET_LINK)
                                    }
                                    @Suppress("DEPRECATION")
                                    TextButton(onClick = { viewModel.isResettingPassword = false }) { Text(AppConstants.BTN_RETURN_TO_LOGIN) }
                                }

                                else {
                                    // Default Login/Signup State
                                    AuthLogo(
                                        isLogin = viewModel.isLogin,
                                        glowScale = glowScale,
                                        glowAlpha = glowAlpha
                                    )
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    @Suppress("DEPRECATION")
                                    Text(
                                        text = if (viewModel.isLogin) AppConstants.TITLE_WELCOME_BACK else "Create Account", 
                                        style = MaterialTheme.typography.headlineMedium, 
                                        fontWeight = FontWeight.Black
                                    )
                                    
                                    Spacer(modifier = Modifier.height(32.dp))

                                    if (!viewModel.isLogin) {
                                        OutlinedTextField(
                                            value = viewModel.firstName, 
                                            onValueChange = { viewModel.firstName = it }, 
                                            label = { Text("Full Name") }, 
                                            modifier = Modifier.fillMaxWidth(), 
                                            shape = RoundedCornerShape(12.dp),
                                            leadingIcon = { Icon(Icons.Default.Person, null) }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    OutlinedTextField(
                                        value = viewModel.email, 
                                        onValueChange = { viewModel.email = it }, 
                                        label = { Text("Email") }, 
                                        modifier = Modifier.fillMaxWidth(), 
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = { Icon(Icons.Default.Email, null) }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    OutlinedTextField(
                                        value = viewModel.password, 
                                        onValueChange = { viewModel.password = it }, 
                                        label = { Text("Password") }, 
                                        modifier = Modifier.fillMaxWidth(), 
                                        shape = RoundedCornerShape(12.dp),
                                        visualTransformation = if (viewModel.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                                        trailingIcon = {
                                            IconButton(onClick = { viewModel.passwordVisible = !viewModel.passwordVisible }) {
                                                Icon(if (viewModel.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                            }
                                        }
                                    )

                                    if (viewModel.isLogin) {
                                        @Suppress("DEPRECATION")
                                        TextButton(
                                            onClick = { viewModel.isResettingPassword = true },
                                            modifier = Modifier.align(Alignment.End)
                                        ) { Text("Forgot Password?", style = MaterialTheme.typography.labelMedium) }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = { 
                                            if (viewModel.isLogin) viewModel.handleSignIn(context) { email ->
                                                scope.launch { snackbarHostState.showSnackbar("Verification code sent to $email") }
                                            } 
                                            else viewModel.handleSignUp() 
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                    ) {
                                        if (viewModel.isLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            Text(if (viewModel.isLogin) AppConstants.BTN_SIGN_IN else "Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (viewModel.isLogin) "New to Glyndŵr?" else "Already a member?", style = MaterialTheme.typography.bodyMedium)
                                        TextButton(onClick = { viewModel.isLogin = !viewModel.isLogin }) {
                                            @Suppress("DEPRECATION")
                                            Text(if (viewModel.isLogin) "Register Now" else "Login instead", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                                    OutlinedButton(
                                        onClick = { 
                                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                                .requestEmail()
                                                .build()
                                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                        },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_google_logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        @Suppress("DEPRECATION")
                                        Text(if (viewModel.isLogin) AppConstants.BTN_GOOGLE_LOGIN else AppConstants.BTN_GOOGLE_SIGNUP)
                                    }
                                }
                            }
                        }
                        
                        if (viewModel.error != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 24.dp).fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(12.dp))
                                    Text(viewModel.error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        AppPopups.AuthDemoCode(show = viewModel.showDemoPopup, code = viewModel.generated2FACode, onDismiss = { viewModel.showDemoPopup = false })
        
        AppPopups.AuthSuccess(
            show = viewModel.showSuccessPopup, 
            isDarkTheme = isDarkTheme,
            onDismiss = { 
                viewModel.showSuccessPopup = false
                onAuthSuccess(userRole)
            }
        )
    }
}
