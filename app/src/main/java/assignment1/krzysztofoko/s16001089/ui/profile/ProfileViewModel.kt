package assignment1.krzysztofoko.s16001089.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * ViewModel for the Profile Screen.
 * Enhanced to support decoupled custom theme customization.
 */
class ProfileViewModel(
    private val userDao: UserDao,
    private val userThemeDao: UserThemeDao, // Added decoupled Dao
    private val auditDao: AuditDao,
    private val userId: String    
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    val localUser: StateFlow<UserLocal?> = userDao.getUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userTheme: StateFlow<UserTheme?> = userThemeDao.getThemeFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var isUploading by mutableStateOf(false)
    
    var title by mutableStateOf("")
    var firstName by mutableStateOf("")
    var surname by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var selectedPaymentMethod by mutableStateOf("")
    var selectedAddress by mutableStateOf("")

    // --- Custom Theme States (Full Schema) ---
    var isCustomThemeEnabled by mutableStateOf(false)
    var customPrimary by mutableLongStateOf(0xFFBB86FC)
    var customOnPrimary by mutableLongStateOf(0xFF000000)
    var customPrimaryContainer by mutableLongStateOf(0xFF3700B3)
    var customOnPrimaryContainer by mutableLongStateOf(0xFFFFFFFF)
    
    var customSecondary by mutableLongStateOf(0xFF03DAC6)
    var customOnSecondary by mutableLongStateOf(0xFF000000)
    var customSecondaryContainer by mutableLongStateOf(0xFF018786)
    var customOnSecondaryContainer by mutableLongStateOf(0xFFFFFFFF)
    
    var customTertiary by mutableLongStateOf(0xFF03DAC6)
    var customOnTertiary by mutableLongStateOf(0xFF000000)
    var customTertiaryContainer by mutableLongStateOf(0xFF018786)
    var customOnTertiaryContainer by mutableLongStateOf(0xFFFFFFFF)
    
    var customBackground by mutableLongStateOf(0xFF0F172A)
    var customOnBackground by mutableLongStateOf(0xFFF8FAFC)
    var customSurface by mutableLongStateOf(0xFF1E293B)
    var customOnSurface by mutableLongStateOf(0xFFF8FAFC)
    var customIsDark by mutableStateOf(true)

    private fun addLog(action: String, details: String) {
        viewModelScope.launch {
            val name = localUser.value?.name ?: "Student"
            auditDao.insertLog(SystemLog(
                userId = userId,
                userName = name,
                action = action,
                targetId = "PROFILE",
                details = details,
                logType = "USER"
            ))
        }
    }

    fun initFields(user: UserLocal, theme: UserTheme?) {
        if (firstName.isEmpty() && surname.isEmpty()) {
            title = user.title ?: ""
            val names = user.name.split(" ")
            firstName = names.getOrNull(0) ?: ""
            surname = names.getOrNull(1) ?: ""
            phoneNumber = user.phoneNumber ?: ""
            selectedPaymentMethod = user.selectedPaymentMethod ?: AppConstants.METHOD_UNIVERSITY_ACCOUNT
            selectedAddress = user.address ?: AppConstants.MSG_NO_ADDRESS_YET
            
            // Init theme fields from separated UserTheme entity
            theme?.let { t ->
                isCustomThemeEnabled = t.isCustomThemeEnabled
                customPrimary = t.customPrimary ?: 0xFFBB86FC
                customOnPrimary = t.customOnPrimary ?: 0xFF000000
                customPrimaryContainer = t.customPrimaryContainer ?: 0xFF3700B3
                customOnPrimaryContainer = t.customOnPrimaryContainer ?: 0xFFFFFFFF
                
                customSecondary = t.customSecondary ?: 0xFF03DAC6
                customOnSecondary = t.customOnSecondary ?: 0xFF000000
                customSecondaryContainer = t.customSecondaryContainer ?: 0xFF018786
                customOnSecondaryContainer = t.customOnSecondaryContainer ?: 0xFFFFFFFF
                
                customTertiary = t.customTertiary ?: 0xFF03DAC6
                customOnTertiary = t.customOnTertiary ?: 0xFF000000
                customTertiaryContainer = t.customTertiaryContainer ?: 0xFF018786
                customOnTertiaryContainer = t.customOnTertiaryContainer ?: 0xFFFFFFFF
                
                customBackground = t.customBackground ?: 0xFF0F172A
                customOnBackground = t.customOnBackground ?: 0xFFF8FAFC
                customSurface = t.customSurface ?: 0xFF1E293B
                customOnSurface = t.customOnSurface ?: 0xFFF8FAFC
                customIsDark = t.customIsDark
            }
        }
    }

    fun updateProfile(onComplete: (String) -> Unit) {
        isUploading = true
        val fullName = "$firstName $surname".trim()
        
        user?.updateProfile(userProfileChangeRequest { displayName = fullName })?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModelScope.launch {
                    localUser.value?.let { u ->
                        // Save basic profile data
                        userDao.upsertUser(u.copy(
                            name = fullName,
                            title = title.ifEmpty { null },
                            address = selectedAddress,
                            phoneNumber = phoneNumber,
                            selectedPaymentMethod = selectedPaymentMethod
                        ))

                        // Save decoupled theme data
                        userThemeDao.upsertTheme(UserTheme(
                            userId = userId,
                            isCustomThemeEnabled = isCustomThemeEnabled,
                            customPrimary = customPrimary,
                            customOnPrimary = customOnPrimary,
                            customPrimaryContainer = customPrimaryContainer,
                            customOnPrimaryContainer = customOnPrimaryContainer,
                            customSecondary = customSecondary,
                            customOnSecondary = customOnSecondary,
                            customSecondaryContainer = customSecondaryContainer,
                            customOnSecondaryContainer = customOnSecondaryContainer,
                            customTertiary = customTertiary,
                            customOnTertiary = customOnTertiary,
                            customTertiaryContainer = customTertiaryContainer,
                            customOnTertiaryContainer = customOnTertiaryContainer,
                            customBackground = customBackground,
                            customOnBackground = customOnBackground,
                            customSurface = customSurface,
                            customOnSurface = customOnSurface,
                            customIsDark = customIsDark
                        ))
                        addLog("EDITED", "User updated their profile and separate theme configuration.")
                    }
                    isUploading = false
                    onComplete(AppConstants.MSG_PROFILE_UPDATE_SUCCESS)
                }
            } else {
                isUploading = false
                onComplete(AppConstants.MSG_PROFILE_UPDATE_FAILED)
            }
        }
    }

    fun uploadAvatar(context: Context, uri: Uri, onComplete: (String) -> Unit) {
        isUploading = true
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open stream")
                val avatarFile = File(context.filesDir, "avatar_${userId}_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(avatarFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                
                val localFileUri = Uri.fromFile(avatarFile).toString()
                localUser.value?.let { u ->
                    userDao.upsertUser(u.copy(photoUrl = localFileUri))
                    userDao.updateReviewAvatars(u.id, localFileUri)
                    addLog("EDITED", "User updated their profile avatar.")
                }
                
                user?.updateProfile(userProfileChangeRequest { photoUri = Uri.parse(localFileUri) })
                isUploading = false
                onComplete(AppConstants.MSG_AVATAR_UPDATE_SUCCESS)
            } catch (e: Exception) {
                isUploading = false
                onComplete("${AppConstants.MSG_AVATAR_UPDATE_FAILED}: ${e.message}")
            }
        }
    }

    fun signOut(localUser: UserLocal?) {
        val uid = localUser?.id ?: auth.currentUser?.uid ?: "unknown"
        val name = localUser?.name ?: auth.currentUser?.displayName ?: "User"
        addLog("USER_LOGOUT", "User signed out from profile settings.")
        auth.signOut()
    }
}

class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val userThemeDao: UserThemeDao, // Added Dao
    private val auditDao: AuditDao,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userDao, userThemeDao, auditDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
