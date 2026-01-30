package assignment1.krzysztofoko.s16001089.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Composable representing the User Profile and Settings screen.
 * Allows users to update personal info, manage addresses, payment methods, and account security.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,             // Callback to handle user logout
    isDarkTheme: Boolean,             // Current theme state
    onToggleTheme: () -> Unit,        // Callback to toggle dark/light mode
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        auditDao = AppDatabase.getDatabase(LocalContext.current).auditDao(),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe local user data from the Room database
    val localUser by viewModel.localUser.collectAsState(initial = null)

    // Sync ViewModel fields with database data when it loads
    LaunchedEffect(localUser) {
        localUser?.let { viewModel.initFields(it) }
    }

    // States to control various settings popups/dialogs
    var showSelectionPopup by remember { mutableStateOf(false) }
    var currentPopupStep by remember { mutableIntStateOf(1) }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var paypalEmail by remember { mutableStateOf("") }

    var showPasswordPopup by remember { mutableStateOf(false) }
    var showAddressPopup by remember { mutableStateOf(false) }
    var showEmailPopup by remember { mutableStateOf(false) }

    // Determine the user's display photo (prefer local DB over Firebase)
    val currentPhotoUrl = localUser?.photoUrl ?: user?.photoUrl?.toString()

    // Launcher for selecting a profile picture from the gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadAvatar(context, uri) { msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent, // Transparent to show wavy background behind
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(AppConstants.TITLE_PROFILE_SETTINGS, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { 
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") 
                    } 
                },
                actions = {
                    // Quick theme toggle
                    IconButton(onClick = onToggleTheme) { 
                        Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) 
                    }
                    // Logout button
                    IconButton(onClick = onLogout) { 
                        Icon(Icons.AutoMirrored.Filled.Logout, AppConstants.BTN_LOG_OUT, tint = MaterialTheme.colorScheme.error) 
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Reusable wavy background component
            HorizontalWavyBackground(isDarkTheme = isDarkTheme)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header displaying profile picture and upload status
                ProfileHeader(
                    photoUrl = currentPhotoUrl,
                    isUploading = viewModel.isUploading,
                    onPickPhoto = { photoPickerLauncher.launch("image/*") }
                )

                // Main card containing editable profile sections
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Section for Name, Phone, and Email
                        PersonalInfoSection(
                            firstName = viewModel.firstName,
                            onFirstNameChange = { viewModel.firstName = it },
                            surname = viewModel.surname,
                            onSurnameChange = { viewModel.surname = it },
                            phoneNumber = viewModel.phoneNumber,
                            onPhoneNumberChange = { viewModel.phoneNumber = it },
                            email = localUser?.email ?: "",
                            onEditEmail = { showEmailPopup = true }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Address management section
                        ProfileAddressSection(
                            address = viewModel.selectedAddress,
                            onChangeAddress = { showAddressPopup = true }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Payment method selection section
                        ProfilePaymentSection(
                            paymentMethod = viewModel.selectedPaymentMethod,
                            onChangePayment = { currentPopupStep = 1; showSelectionPopup = true }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Security (Password) section
                        ProfileSecuritySection(
                            onChangePassword = { showPasswordPopup = true }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Main Save Button
                        Button(
                            onClick = {
                                viewModel.updateProfile { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !viewModel.isUploading
                        ) {
                            if (viewModel.isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(AppConstants.BTN_SAVE_PROFILE, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // --- Dialogs and Popups for specific settings ---

        // Dialog for adding/switching payment methods (Card or PayPal)
        PaymentMethodDialog(
            show = showSelectionPopup,
            currentStep = currentPopupStep,
            selectedMethod = viewModel.selectedPaymentMethod,
            onMethodSelect = { viewModel.selectedPaymentMethod = it },
            onNextStep = { currentPopupStep = 2 },
            onBackStep = { currentPopupStep = 1 },
            onDismiss = { showSelectionPopup = false },
            onConfirm = { finalMethod ->
                viewModel.selectedPaymentMethod = finalMethod
                viewModel.updateProfile { msg ->
                    showSelectionPopup = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            cardNumber = cardNumber,
            onCardNumberChange = { cardNumber = it },
            cardExpiry = cardExpiry,
            onCardExpiryChange = { cardExpiry = it },
            paypalEmail = paypalEmail,
            onPaypalEmailChange = { paypalEmail = it }
        )

        // Popup for requesting a password reset email
        AppPopups.ProfilePasswordChange(
            show = showPasswordPopup,
            userEmail = user?.email ?: "",
            onDismiss = { showPasswordPopup = false },
            onSuccess = { 
                showPasswordPopup = false
                scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_PASSWORD_UPDATED) } 
            }
        )

        // Dialog for managing physical shipping/billing addresses
        AppPopups.AddressManagement(
            show = showAddressPopup,
            onDismiss = { showAddressPopup = false },
            onSave = { newAddr ->
                viewModel.selectedAddress = newAddr
                viewModel.updateProfile { msg ->
                    showAddressPopup = false
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )

        // Popup for changing the primary login email
        AppPopups.ProfileEmailChange(
            show = showEmailPopup,
            currentEmail = user?.email ?: "",
            onDismiss = { showEmailPopup = false },
            onSuccess = { _ -> 
                // Logout and return to Auth screen after email change for security
                viewModel.signOut(localUser)
                navController.navigate(AppConstants.ROUTE_AUTH) { popUpTo(0) } 
            }
        )
    }
}
