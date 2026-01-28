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

/**
 * ViewModel for the Profile Screen.
 * 
 * Manages the state and logic for viewing and updating user profile information,
 * including personal details, contact information, payment methods, and profile pictures.
 * It synchronizes data between Firebase Authentication and the local Room database.
 */
class ProfileViewModel(
    private val userDao: UserDao, // Data Access Object for local user data operations
    private val userId: String    // The unique identifier for the current user
) : ViewModel() {

    // Firebase Authentication instance to manage the cloud user profile
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    /**
     * Flow representing the local user profile data from the Room database.
     * It is converted into a StateFlow to be easily observed by the Compose UI.
     */
    val localUser: StateFlow<UserLocal?> = userDao.getUserFlow(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Observable UI States for the profile form fields ---
    
    // Flag to track background operations (like uploading an image) to show loading spinners
    var isUploading by mutableStateOf(false)
    
    // Editable fields for user details
    var firstName by mutableStateOf("")
    var surname by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var selectedPaymentMethod by mutableStateOf("")
    var selectedAddress by mutableStateOf("")

    /**
     * Initializes the UI fields with data from the UserLocal entity.
     * Only performs initialization if the name fields are currently empty to avoid overwriting user input.
     */
    fun initFields(user: UserLocal) {
        if (firstName.isEmpty() && surname.isEmpty()) {
            val names = user.name.split(" ")
            firstName = names.getOrNull(0) ?: ""
            surname = names.getOrNull(1) ?: ""
            phoneNumber = user.phoneNumber ?: ""
            selectedPaymentMethod = user.selectedPaymentMethod ?: AppConstants.METHOD_UNIVERSITY_ACCOUNT
            selectedAddress = user.address ?: AppConstants.MSG_NO_ADDRESS_YET
        }
    }

    /**
     * Updates the user's profile information.
     * 
     * This process involves:
     * 1. Updating the display name in Firebase Authentication.
     * 2. On success, updating the corresponding record in the local Room database.
     */
    fun updateProfile(onComplete: (String) -> Unit) {
        isUploading = true
        val fullName = "$firstName $surname".trim()
        
        // Update cloud profile in Firebase
        user?.updateProfile(userProfileChangeRequest { displayName = fullName })?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // If cloud update succeeds, persist changes to the local Room DB
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
                    onComplete(AppConstants.MSG_PROFILE_UPDATE_SUCCESS)
                }
            } else {
                // Handle failure
                isUploading = false
                onComplete(AppConstants.MSG_PROFILE_UPDATE_FAILED)
            }
        }
    }

    /**
     * Handles the selection and local storage of a new profile picture.
     * 
     * 1. Copies the image from the provided URI to the app's internal storage.
     * 2. Updates the 'photoUrl' in the local database.
     * 3. Updates all user review avatars to reflect the new picture.
     * 4. Updates the Firebase Authentication profile with the new URI.
     */
    fun uploadAvatar(context: Context, uri: Uri, onComplete: (String) -> Unit) {
        isUploading = true
        viewModelScope.launch {
            try {
                // Access the selected image via content resolver
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open stream")
                
                // Create a permanent local file in internal storage
                val avatarFile = File(context.filesDir, "avatar_${userId}_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(avatarFile)
                
                // Perform the file copy
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                
                val localFileUri = Uri.fromFile(avatarFile).toString()
                
                // Persist the new file path to the local user record and existing reviews
                localUser.value?.let { u ->
                    userDao.upsertUser(u.copy(photoUrl = localFileUri))
                    userDao.updateReviewAvatars(u.id, localFileUri)
                }
                
                // Sync the change with Firebase Authentication
                user?.updateProfile(userProfileChangeRequest { photoUri = Uri.parse(localFileUri) })
                
                isUploading = false
                onComplete(AppConstants.MSG_AVATAR_UPDATE_SUCCESS)
            } catch (e: Exception) {
                // Error handling for I/O operations
                isUploading = false
                onComplete("${AppConstants.MSG_AVATAR_UPDATE_FAILED}: ${e.message}")
            }
        }
    }
}

/**
 * Factory class to provide dependencies (UserDao and userId) to the ProfileViewModel.
 */
class ProfileViewModelFactory(
    private val userDao: UserDao,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
