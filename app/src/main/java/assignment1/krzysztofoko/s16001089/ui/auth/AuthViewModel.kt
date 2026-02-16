package assignment1.krzysztofoko.s16001089.ui.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.SystemLog
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the authentication state and business logic.
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
    var pendingAuthResult by mutableStateOf<Pair<String, UserLocal?>?>(null)

    private fun addLog(userId: String, userName: String, action: String, details: String, role: String = "user") {
        viewModelScope.launch {
            val type = if (role == "admin") "ADMIN" else "USER"
            db.auditDao().insertLog(SystemLog(
                userId = userId,
                userName = userName,
                action = action,
                targetId = "AUTH",
                details = details,
                logType = type
            ))
        }
    }

    /**
     * Determines if an email should bypass verification requirements for testing.
     */
    private fun isBypassEmail(email: String?): Boolean {
        if (email == null) return false
        val bypassList = listOf("teacher@example.com", "admin@example.com", "student@example.com", "user@example.com") 
        return bypassList.contains(email.lowercase())
    }

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
                    localUser?.let { 
                        db.userDao().upsertUser(it)
                        val action = when(it.role) {
                            "admin" -> "ADMIN_LOGIN"
                            "teacher" -> "TUTOR_LOGIN"
                            else -> "USER_LOGIN"
                        }
                        addLog(it.id, it.name, action, "Successful authentication and session start.", it.role)
                    }
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
                
                if (user?.isEmailVerified == true || isBypassEmail(user?.email)) {
                    viewModelScope.launch {
                        val existing = db.userDao().getUserById(user!!.uid)
                        
                        // REMOVED HARDCODED STUDENT/TEACHER OVERRIDES
                        val updatedRole = when (user.email) {
                            "prokocomp@gmail.com", "admin@example.com" -> "admin"
                            else -> (existing?.role ?: "user")
                        }
                        
                        val userData = UserLocal(
                            id = user.uid,
                            name = existing?.name ?: user.displayName ?: when(user.email) {
                                "admin@example.com" -> "System Admin"
                                "teacher@example.com" -> "Senior Tutor"
                                "student@example.com" -> "Sample Student"
                                else -> "User"
                            },
                            email = user.email ?: trimmedEmail,
                            balance = existing?.balance ?: 0.0,
                            role = updatedRole,
                            photoUrl = existing?.photoUrl ?: user.photoUrl?.toString(),
                            address = existing?.address,
                            phoneNumber = existing?.phoneNumber,
                            selectedPaymentMethod = existing?.selectedPaymentMethod
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
                    val assignedRole = when (trimmedEmail) {
                        "prokocomp@gmail.com", "admin@example.com" -> "admin"
                        else -> "user" // DEFAULT TO USER
                    }
                    
                    val userData = UserLocal(
                        id = user.uid,
                        name = firstName,
                        email = trimmedEmail,
                        balance = 0.0,
                        role = assignedRole
                    )
                    pendingAuthResult = "" to userData
                    addLog(user.uid, firstName, "USER_SIGN_UP", "New user registered: $trimmedEmail", userData.role)
                    
                    user.sendEmailVerification()
                    isVerifyingEmail = true
                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                if (e is FirebaseAuthUserCollisionException) {
                    error = "This email is already registered. Please sign in instead."
                } else {
                    error = e.localizedMessage
                }
            }
    }

    fun handleGoogleSignIn(credential: AuthCredential, idToken: String, context: Context, onCodeSent: (String) -> Unit) {
        isLoading = true
        auth.signInWithCredential(credential)
            .addOnSuccessListener { res ->
                val user = res.user!!
                viewModelScope.launch {
                    val existing = db.userDao().getUserById(user.uid)
                    
                    val updatedRole = when (user.email) {
                        "prokocomp@gmail.com", "admin@example.com" -> "admin"
                        else -> (existing?.role ?: "user")
                    }

                    val userData = UserLocal(
                        id = user.uid,
                        name = existing?.name ?: user.displayName ?: "User",
                        email = user.email ?: "",
                        photoUrl = existing?.photoUrl ?: user.photoUrl?.toString(),
                        balance = existing?.balance ?: 0.0,
                        role = updatedRole,
                        address = existing?.address,
                        phoneNumber = existing?.phoneNumber,
                        selectedPaymentMethod = existing?.selectedPaymentMethod
                    )
                    pendingAuthResult = idToken to userData
                    trigger2FA(context, user.email ?: "", onCodeSent)
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                error = "Google Authentication failed."
            }
    }

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
    
    fun signOut(localUser: UserLocal?) {
        val uid = localUser?.id ?: auth.currentUser?.uid ?: "unknown"
        val name = localUser?.name ?: auth.currentUser?.displayName ?: "User"
        val role = localUser?.role ?: "user"
        val action = when(role) {
            "admin" -> "ADMIN_LOGOUT"
            "teacher" -> "TUTOR_LOGIN"
            else -> "USER_LOGOUT"
        }
        addLog(uid, name, action, "User signed out from the session.", role)
        auth.signOut()
    }
}
