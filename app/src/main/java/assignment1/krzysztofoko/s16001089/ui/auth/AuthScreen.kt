package assignment1.krzysztofoko.s16001089.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
 * This screen handles all aspects of the user session lifecycle, including:
 * - Email & Password Login / Registration
 * - Google Social Authentication (OAuth)
 * - Password Reset
 * - Post-registration Email Verification
 * - Two-Factor Authentication (2FA) via email code
 * - Role-based navigation upon successful authentication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (role: String) -> Unit, // Callback after full verification. Navigates based on the user's role.
    onBack: () -> Unit,                    // Standard back navigation action.
    currentTheme: Theme,                   // The active application theme (e.g., DARK, LIGHT, SKY) for styling.
    onThemeChange: (Theme) -> Unit,        // Callback to update the global theme state from the header toggle.
    snackbarHostState: SnackbarHostState   // State manager for showing feedback messages (e.g., "Code sent").
) {
    // --- INITIALIZATION ---
    val context = LocalContext.current // Access to Android Context for system operations like Google Sign-In.
    val db = AppDatabase.getDatabase(context) // Initialize the local Room database instance.
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(db)) // Main business logic handler.
    val scope = rememberCoroutineScope() // Coroutine scope tied to the composable's lifecycle.
    
    // Resolve the current theme to a simple dark/light boolean for component styling.
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM

    // --- ANIMATIONS & STATE ---
    val glowAnim = rememberGlowAnimation()
    val glowScale = glowAnim.first
    val glowAlpha = glowAnim.second

    val userRole = viewModel.pendingAuthResult?.second?.role ?: "user"

    // Automatically navigate away from the success screen after a delay.
    LaunchedEffect(viewModel.showSuccessPopup) {
        if (viewModel.showSuccessPopup) {
            delay(10000)
            if (viewModel.showSuccessPopup) {
                viewModel.showSuccessPopup = false
                onAuthSuccess(userRole)
            }
        }
    }

    // --- GOOGLE SIGN-IN ---
    // This launcher handles the result from the Google Sign-In activity.
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken ?: throw IllegalStateException("ID Token missing.")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            viewModel.handleGoogleSignIn(credential, idToken, context) { email ->
                scope.launch { snackbarHostState.showSnackbar("Verification code sent to $email") }
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            viewModel.isLoading = false
            viewModel.error = "Google Sign-In failed: Status Code ${e.statusCode}"
        } catch (e: Exception) {
            // Handle other exceptions
            viewModel.isLoading = false
            viewModel.error = "Unexpected error: ${e.localizedMessage}"
        }
    }

    // --- MAIN UI COMPOSITION ---
    // We use a Box with fillMaxSize to ensure the background covers the ENTIRE screen,
    // including the area behind the keyboard and navigation bars.
    Box(modifier = Modifier.fillMaxSize()) {
        // Background flows under system bars and keyboard seamlessly.
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0), // Scaffold ignores system insets
            topBar = {
                // The top bar is hidden on the success screen.
                if (!viewModel.showSuccessPopup && !viewModel.isAuthFinalized) {
                    CenterAlignedTopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { 
                            Text(
                                // Dynamically set the title based on the current auth state.
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
                                // Custom back navigation logic based on the current auth state.
                                if (viewModel.isTwoFactorStep) { 
                                    viewModel.isTwoFactorStep = false
                                    viewModel.signOut(viewModel.pendingAuthResult?.second)
                                } else if (viewModel.isResettingPassword) {
                                    viewModel.isResettingPassword = false
                                } else onBack()
                            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                        },
                        actions = {
                            ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, isLoggedIn = false)
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
                    val scrollState = rememberScrollState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = if (isTablet) 0.dp else 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))

                        // Primary Auth Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            shadowElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                
                                // --- TWO-FACTOR AUTHENTICATION VIEW ---
                                if (viewModel.isTwoFactorStep) {
                                    Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(AppConstants.TITLE_SECURITY_VERIFICATION, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Text("A code has been sent to your email.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    OutlinedTextField(
                                        value = viewModel.entered2FACode, 
                                        onValueChange = { if (it.length <= 6) viewModel.entered2FACode = it }, 
                                        label = { Text("6-Digit Code") }, 
                                        modifier = Modifier.fillMaxWidth(), 
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, letterSpacing = 6.sp, fontWeight = FontWeight.Black)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(onClick = {
                                        if (viewModel.entered2FACode == viewModel.generated2FACode) viewModel.finalizeAuth()
                                        else viewModel.error = "Invalid code."
                                    }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                                        Text(AppConstants.BTN_VERIFY_IDENTITY)
                                    }
                                    TextButton(onClick = { viewModel.trigger2FA(context, viewModel.email) { } }) { Text(AppConstants.BTN_RESEND_CODE) }
                                } 
                                // --- EMAIL VERIFICATION VIEW ---
                                else if (viewModel.isVerifyingEmail) {
                                    Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(AppConstants.TITLE_CHECK_INBOX, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text(viewModel.email, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(32.dp))
                                    Button(onClick = {
                                        viewModel.isLoading = true
                                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
                                            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                                                viewModel.trigger2FA(context, viewModel.email) { }
                                            } else {
                                                viewModel.isLoading = false
                                                viewModel.error = "Email not yet verified."
                                            }
                                        }
                                    }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text(AppConstants.BTN_VERIFICATION_DONE) }
                                    TextButton(onClick = { viewModel.signOut(viewModel.pendingAuthResult?.second); viewModel.isVerifyingEmail = false }) { Text(AppConstants.BTN_BACK_TO_LOGIN) }
                                } 
                                // --- PASSWORD RESET VIEW ---
                                else if (viewModel.isResettingPassword) {
                                    Icon(Icons.Default.LockReset, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(AppConstants.TITLE_RESET_PASSWORD, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    
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
                                        Text(AppConstants.BTN_SEND_RESET_LINK)
                                    }
                                    TextButton(onClick = { viewModel.isResettingPassword = false }) { Text(AppConstants.BTN_RETURN_TO_LOGIN) }
                                }
                                // --- LOGIN / REGISTER VIEW ---
                                else {
                                    AuthLogo(isLogin = viewModel.isLogin, glowScale = glowScale, glowAlpha = glowAlpha)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(text = if (viewModel.isLogin) AppConstants.TITLE_WELCOME_BACK else "Create Account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(32.dp))

                                    // Show "Full Name" field only on the registration screen.
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
                                        TextButton(onClick = { viewModel.isResettingPassword = true }, modifier = Modifier.align(Alignment.End)) { Text("Forgot Password?", style = MaterialTheme.typography.labelMedium) }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Main action button (Sign In or Create Account)
                                    Button(
                                        onClick = { 
                                            if (viewModel.isLogin) viewModel.handleSignIn(context) { email -> scope.launch { snackbarHostState.showSnackbar("Code sent to $email") } } 
                                            else viewModel.handleSignUp() 
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                        else Text(if (viewModel.isLogin) AppConstants.BTN_SIGN_IN else "Create Account", fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Toggle between Login and Register views
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (viewModel.isLogin) "New to Glyndŵr?" else "Already a member?", style = MaterialTheme.typography.bodyMedium)
                                        TextButton(onClick = { viewModel.isLogin = !viewModel.isLogin }) {
                                            Text(if (viewModel.isLogin) "Register Now" else "Login instead", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                                    // Google Sign-In button
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
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(if (viewModel.isLogin) AppConstants.BTN_GOOGLE_LOGIN else AppConstants.BTN_GOOGLE_SIGNUP)
                                    }
                                }
                            }
                        }
                        
                        // --- ERROR DISPLAY ---
                        // Shows a distinct error panel if the viewModel reports an error.
                        if (viewModel.error != null) {
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(top = 24.dp).fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(12.dp))
                                    Text(viewModel.error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        
                        // REQUIREMENT: Seamless Lifting.
                        // By using windowInsetsBottomHeight(WindowInsets.ime), we create a Spacer
                        // that grows exactly with the keyboard. Because it's at the bottom of 
                        // the scrollable Column, it pushes the content up. Since the Scaffold
                        // and ScreenContainer have NO insets or padding at the bottom, the 
                        // background flows perfectly behind the keyboard with no white lines.
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
                        // Additional spacer for the navigation bar area to ensure content doesn't
                        // hide behind the keyboard or system bar when scrolled to the end.
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // --- POPUPS ---
        // These are displayed on top of the main UI.
        AppPopups.AuthDemoCode(show = viewModel.showDemoPopup, code = viewModel.generated2FACode, onDismiss = { viewModel.showDemoPopup = false })
        AppPopups.AuthSuccess(show = viewModel.showSuccessPopup, isDarkTheme = isDarkTheme, onDismiss = { viewModel.showSuccessPopup = false; onAuthSuccess(userRole) })
    }
}
