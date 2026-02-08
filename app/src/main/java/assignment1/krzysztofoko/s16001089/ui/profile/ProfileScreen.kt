package assignment1.krzysztofoko.s16001089.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
 * Updated with AdaptiveScreenContainer to look good on tablets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,             
    isDarkTheme: Boolean,             
    onToggleTheme: () -> Unit,        
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

    val localUser by viewModel.localUser.collectAsState(initial = null)

    LaunchedEffect(localUser) {
        localUser?.let { viewModel.initFields(it) }
    }

    var showSelectionPopup by remember { mutableStateOf(false) }
    var currentPopupStep by remember { mutableIntStateOf(1) }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var paypalEmail by remember { mutableStateOf("") }

    var showPasswordPopup by remember { mutableStateOf(false) }
    var showAddressPopup by remember { mutableStateOf(false) }
    var showEmailPopup by remember { mutableStateOf(false) }

    val currentPhotoUrl = localUser?.photoUrl ?: user?.photoUrl?.toString()

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
        containerColor = Color.Transparent, 
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
                    IconButton(onClick = onToggleTheme) { 
                        Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) 
                    }
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
            HorizontalWavyBackground(isDarkTheme = isDarkTheme)
            
            AdaptiveScreenContainer(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                maxWidth = 600.dp
            ) { isTablet ->
                Column(
                    modifier = Modifier.padding(horizontal = if (isTablet) 32.dp else 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(
                        photoUrl = currentPhotoUrl,
                        isUploading = viewModel.isUploading,
                        onPickPhoto = { photoPickerLauncher.launch("image/*") }
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            PersonalInfoSection(
                                title = viewModel.title,
                                onTitleChange = { viewModel.title = it },
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
                            
                            ProfileAddressSection(
                                address = viewModel.selectedAddress,
                                onChangeAddress = { showAddressPopup = true }
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            ProfilePaymentSection(
                                paymentMethod = viewModel.selectedPaymentMethod,
                                onChangePayment = { currentPopupStep = 1; showSelectionPopup = true }
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            ProfileSecuritySection(
                                onChangePassword = { showPasswordPopup = true }
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
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
        }

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

        AppPopups.ProfilePasswordChange(
            show = showPasswordPopup,
            userEmail = user?.email ?: "",
            onDismiss = { showPasswordPopup = false },
            onSuccess = { 
                showPasswordPopup = false
                scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_PASSWORD_UPDATED) } 
            }
        )

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

        AppPopups.ProfileEmailChange(
            show = showEmailPopup,
            currentEmail = user?.email ?: "",
            onDismiss = { showEmailPopup = false },
            onSuccess = { _ -> 
                viewModel.signOut(localUser)
                navController.navigate(AppConstants.ROUTE_AUTH) { popUpTo(0) } 
            }
        )
    }
}
