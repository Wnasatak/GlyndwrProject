package assignment1.krzysztofoko.s16001089.ui.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the authentication state and business logic.
 * 
 * It handles interactions with Firebase Authentication, Google Sign-In, and the local Room database.
 * The ViewModel manages a multi-step authentication process:
 * 1. Initial Login/Sign-up (Firebase)
 * 2. Email Verification (Firebase)
 * 3. Two-Factor Authentication (SMTP via EmailUtils)
 * 4. Local User Profile creation/sync (Room)
 *
 * @property db The [AppDatabase] instance used for persisting user profile information locally.
 */
class AuthViewModel(private val db: AppDatabase) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // --- Form State ---
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var firstName by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    
    // --- Navigation & UI Flow State ---
    var isLogin by mutableStateOf(true)
    var isVerifyingEmail by mutableStateOf(false)
    var isResettingPassword by mutableStateOf(false)
    var isTwoFactorStep by mutableStateOf(false)
    var isAuthFinalized by mutableStateOf(false)
    
    // --- 2FA State ---
    var entered2FACode by mutableStateOf("")
    var generated2FACode by mutableStateOf("")
    
    // --- Popups & Feedback ---
    var showDemoPopup by mutableStateOf(false)
    var showSuccessPopup by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    
    // --- Security & Session ---
    var loginAttempts by mutableIntStateOf(0)
    /**
     * Holds temporary user data after Firebase success but before 2FA completion.
     * The first part of the Pair is the ID Token (if Google), second is the Local User object.
     */
    var pendingAuthResult by mutableStateOf<Pair<String, UserLocal?>?>(null)

    /**
     * Generates a 6-digit random code and attempts to send it to the user's email via SMTP.
     * If successful, it moves the UI to the 2FA input step.
     *
     * @param context Android context for retrieving resources/strings.
     * @param userEmail The recipient's email address.
     * @param onCodeSent Callback invoked after the email is successfully dispatched.
     */
    fun trigger2FA(context: Context, userEmail: String, onCodeSent: (String) -> Unit) {
        isLoading = true
        val code = (100000..999999).random().toString()
        generated2FACode = code
        
        viewModelScope.launch {
            try {
                val success = EmailUtils.send2FACode(context, userEmail, code)
                if (success) {
                    isTwoFactorStep = true
                    showDemoPopup = true // Show the code in a popup for demo purposes (convenience)
                    onCodeSent(userEmail)
                } else {
                    error = "Failed to send verification code."
                }
            } catch (e: Exception) {
                error = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Completes the authentication process by persisting the [UserLocal] data into the
     * local Room database and triggering the success UI.
     */
    fun finalizeAuth() {
        isLoading = true
        viewModelScope.launch {
            try {
                pendingAuthResult?.let { (_, localUser) ->
                    localUser?.let { db.userDao().upsertUser(it) }
                    isAuthFinalized = true
                    showSuccessPopup = true
                    isTwoFactorStep = false
                }
            } catch (e: Exception) {
                error = "Finalization failed."
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Handles traditional Email/Password Sign-In.
     * - Validates input.
     * - Authenticates with Firebase.
     * - Checks if email is verified; if not, triggers Firebase verification email.
     * - If verified, prepares [UserLocal] and triggers the 2FA step.
     *
     * @param context Android context for 2FA trigger.
     * @param onCodeSent Callback for UI feedback.
     */
    fun handleSignIn(context: Context, onCodeSent: (String) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            error = "Fields cannot be empty."
            return
        }
        isLoading = true
        auth.signInWithEmailAndPassword(trimmedEmail, password)
            .addOnSuccessListener { res ->
                val user = res.user
                loginAttempts = 0
                if (user?.isEmailVerified == true) {
                    viewModelScope.launch {
                        val existing = db.userDao().getUserById(user.uid)
                        val userData = existing ?: UserLocal(
                            id = user.uid,
                            name = user.displayName ?: "Student",
                            email = user.email ?: trimmedEmail,
                            balance = 1000.0,
                            role = "student"
                        )
                        pendingAuthResult = "" to userData
                        trigger2FA(context, trimmedEmail, onCodeSent)
                    }
                } else {
                    isVerifyingEmail = true
                    user?.sendEmailVerification()
                    isLoading = false
                }
            }
            .addOnFailureListener {
                isLoading = false
                loginAttempts++
                error = "Login failed. Please check your credentials."
            }
    }

    /**
     * Handles User Registration.
     * - Creates a new Firebase account.
     * - Prepares a new [UserLocal] profile with a default balance.
     * - Sends a Firebase verification email and moves to verification UI.
     */
    fun handleSignUp() {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty() || firstName.isEmpty()) {
            error = "Fields cannot be empty."
            return
        }
        isLoading = true
        auth.createUserWithEmailAndPassword(trimmedEmail, password)
            .addOnSuccessListener { res ->
                val user = res.user!!
                viewModelScope.launch {
                    val userData = UserLocal(
                        id = user.uid,
                        name = firstName,
                        email = trimmedEmail,
                        balance = 1000.0,
                        role = "student"
                    )
                    pendingAuthResult = "" to userData
                    user.sendEmailVerification()
                    isVerifyingEmail = true
                    isLoading = false
                }
            }
            .addOnFailureListener {
                isLoading = false
                error = it.localizedMessage
            }
    }

    /**
     * Handles authentication via Google credentials.
     * - Signs in to Firebase with the provided [AuthCredential].
     * - Synchronizes or creates the local user profile from Google profile data.
     * - Triggers 2FA even for Google users for consistent security.
     *
     * @param credential Firebase AuthCredential obtained from Google Sign-In.
     * @param idToken The Google ID Token.
     * @param context Android context for 2FA trigger.
     * @param onCodeSent Callback for UI feedback.
     */
    fun handleGoogleSignIn(credential: AuthCredential, idToken: String, context: Context, onCodeSent: (String) -> Unit) {
        isLoading = true
        auth.signInWithCredential(credential)
            .addOnSuccessListener { res ->
                val user = res.user!!
                viewModelScope.launch {
                    val existing = db.userDao().getUserById(user.uid)
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
                    trigger2FA(context, user.email ?: "", onCodeSent)
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                error = "Google Authentication failed."
            }
    }

    /**
     * Triggers a Firebase password reset email to the current email address.
     */
    fun resetPassword() {
        if (email.trim().isEmpty()) {
            error = "Email is required for password reset."
            return
        }
        isLoading = true
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                isLoading = false
                error = "Reset link sent to your email!"
            }
            .addOnFailureListener {
                isLoading = false
                error = "Failed to send reset link. Check if the email is correct."
            }
    }
    
    /**
     * Signs the user out of the Firebase session.
     */
    fun signOut() {
        auth.signOut()
    }
}
