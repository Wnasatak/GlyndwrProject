package assignment1.krzysztofoko.s16001089.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
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
 */
class ProfileViewModel(
    private val userDao: UserDao,
    private val auditDao: AuditDao,
    private val userId: String    
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    val localUser: StateFlow<UserLocal?> = userDao.getUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var isUploading by mutableStateOf(false)
    
    var title by mutableStateOf("")
    var firstName by mutableStateOf("")
    var surname by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var selectedPaymentMethod by mutableStateOf("")
    var selectedAddress by mutableStateOf("")

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

    fun initFields(user: UserLocal) {
        if (firstName.isEmpty() && surname.isEmpty()) {
            title = user.title ?: ""
            val names = user.name.split(" ")
            firstName = names.getOrNull(0) ?: ""
            surname = names.getOrNull(1) ?: ""
            phoneNumber = user.phoneNumber ?: ""
            selectedPaymentMethod = user.selectedPaymentMethod ?: AppConstants.METHOD_UNIVERSITY_ACCOUNT
            selectedAddress = user.address ?: AppConstants.MSG_NO_ADDRESS_YET
        }
    }

    fun updateProfile(onComplete: (String) -> Unit) {
        isUploading = true
        val fullName = "$firstName $surname".trim()
        
        user?.updateProfile(userProfileChangeRequest { displayName = fullName })?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModelScope.launch {
                    localUser.value?.let { u ->
                        userDao.upsertUser(u.copy(
                            name = fullName,
                            title = title.ifEmpty { null },
                            address = selectedAddress,
                            phoneNumber = phoneNumber,
                            selectedPaymentMethod = selectedPaymentMethod
                        ))
                        addLog("EDITED", "User updated their profile information: $fullName")
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
    private val auditDao: AuditDao,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userDao, auditDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
