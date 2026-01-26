package assignment1.krzysztofoko.s16001089.ui.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.UserDao
import assignment1.krzysztofoko.s16001089.data.UserLocal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(
    private val userDao: UserDao,
    private val userId: String
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    val localUser: StateFlow<UserLocal?> = userDao.getUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var isUploading by mutableStateOf(false)
    var firstName by mutableStateOf("")
    var surname by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var selectedPaymentMethod by mutableStateOf("")
    var selectedAddress by mutableStateOf("")

    fun initFields(user: UserLocal) {
        if (firstName.isEmpty() && surname.isEmpty()) {
            val names = user.name.split(" ")
            firstName = names.getOrNull(0) ?: ""
            surname = names.getOrNull(1) ?: ""
            phoneNumber = user.phoneNumber ?: ""
            selectedPaymentMethod = user.selectedPaymentMethod ?: "University Account"
            selectedAddress = user.address ?: "No address added yet"
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
                            address = selectedAddress,
                            phoneNumber = phoneNumber,
                            selectedPaymentMethod = selectedPaymentMethod
                        ))
                    }
                    isUploading = false
                    onComplete("Profile updated successfully!")
                }
            } else {
                isUploading = false
                onComplete("Failed to update Firebase profile.")
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
                }
                
                user?.updateProfile(userProfileChangeRequest { photoUri = Uri.parse(localFileUri) })
                isUploading = false
                onComplete("Avatar updated!")
            } catch (e: Exception) {
                isUploading = false
                onComplete("Error saving image: ${e.message}")
            }
        }
    }
}

class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
