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

class AuthViewModel(private val db: AppDatabase) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var firstName by mutableStateOf("")
    var isLogin by mutableStateOf(true)
    var isVerifyingEmail by mutableStateOf(false)
    var isResettingPassword by mutableStateOf(false)
    var isTwoFactorStep by mutableStateOf(false)
    var isAuthFinalized by mutableStateOf(false)
    
    var entered2FACode by mutableStateOf("")
    var generated2FACode by mutableStateOf("")
    
    var showDemoPopup by mutableStateOf(false)
    var showSuccessPopup by mutableStateOf(false)
    
    var loginAttempts by mutableIntStateOf(0)
    var pendingAuthResult by mutableStateOf<Pair<String, UserLocal?>?>(null)
    
    var passwordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun trigger2FA(context: Context, userEmail: String, onCodeSent: (String) -> Unit) {
        isLoading = true
        val code = (100000..999999).random().toString()
        generated2FACode = code
        
        viewModelScope.launch {
            try {
                val success = EmailUtils.send2FACode(context, userEmail, code)
                if (success) {
                    isTwoFactorStep = true
                    showDemoPopup = true
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
                error = "Login failed."
            }
    }

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
                error = "Auth failed."
            }
    }

    fun resetPassword() {
        if (email.trim().isEmpty()) {
            error = "Email is required."
            return
        }
        isLoading = true
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                isLoading = false
                error = "Reset link sent!"
            }
            .addOnFailureListener {
                isLoading = false
                error = "Failed to send link."
            }
    }
    
    fun signOut() {
        auth.signOut()
    }
}
