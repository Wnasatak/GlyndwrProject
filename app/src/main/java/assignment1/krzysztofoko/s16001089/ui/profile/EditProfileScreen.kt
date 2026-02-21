package assignment1.krzysztofoko.s16001089.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
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
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Screen for modifying user account information.
 * Handles editing of personal identity, contact details, payment preferences, and security.
 * Logic: Synchronizes local ViewModel state with Room database and Firebase Auth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,     // Controls the back-stack navigation // Used for navigating between screens
    onLogout: () -> Unit,             // Callback to trigger global sign-out // Triggered when the logout button is clicked
    isDarkTheme: Boolean,             // UI visual state flag // Determines if the theme is dark or light
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(), // Data access for user information
        userThemeDao = AppDatabase.getDatabase(LocalContext.current).userThemeDao(), // Data access for user theme
        auditDao = AppDatabase.getDatabase(LocalContext.current).auditDao(), // Data access for system logs
        courseDao = AppDatabase.getDatabase(LocalContext.current).courseDao(), // Data access for courses
        classroomDao = AppDatabase.getDatabase(LocalContext.current).classroomDao(), // Data access for classroom data
        bookRepository = BookRepository(AppDatabase.getDatabase(LocalContext.current)), // Data access for book repository
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // Current user ID from Firebase
    ))
) {
    val context = LocalContext.current // Local context for UI operations
    val auth = FirebaseAuth.getInstance() // Firebase authentication instance
    val user = auth.currentUser // Current Firebase user
    val scope = rememberCoroutineScope() // Coroutine scope for launching side effects
    val snackbarHostState = remember { SnackbarHostState() } // State for snackbar notifications

    // Stream user profile and theme data from database
    val localUser by viewModel.localUser.collectAsState(initial = null) // Collects local user state from Room
    val userTheme by viewModel.userTheme.collectAsState(initial = null) // Collects user theme state from Room

    // Synchronize UI input fields when data is first loaded from DB
    LaunchedEffect(localUser, userTheme) {
        localUser?.let { viewModel.initFields(it, userTheme) } // Initialises the fields when user data is available
    }

    // --- STATE CONTROLS FOR MODAL OVERLAYS ---
    var showSelectionPopup by remember { mutableStateOf(false) } // Payment method selection visibility state
    var currentPopupStep by remember { mutableIntStateOf(1) }    // Step tracker for multi-part dialogs
    var cardNumber by remember { mutableStateOf("") } // Card number input state
    var cardExpiry by remember { mutableStateOf("") } // Card expiry input state
    var paypalEmail by remember { mutableStateOf("") } // Paypal email input state

    var showPasswordPopup by remember { mutableStateOf(false) } // Password reset dialog visibility state
    var showAddressPopup by remember { mutableStateOf(false) }  // Home address editor visibility state
    var showEmailPopup by remember { mutableStateOf(false) }    // Account email editor visibility state

    // Determine which photo to show (Local DB has priority over Firebase)
    val currentPhotoUrl = localUser?.photoUrl ?: user?.photoUrl?.toString() // Fallback to Firebase if local photo is null

    // Activity result handler for picking images from device gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // Launches the image picker
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadAvatar(context, uri) { msg -> // Uploads the selected avatar to local storage
                scope.launch { snackbarHostState.showSnackbar(msg) } // Shows a snackbar with the result message
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent, // Allow background animations to show through
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Host for showing snackbar messages
        topBar = {
            // Standard centered toolbar with back button and logout action
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0), // Adjusts the top app bar padding
                title = { Text(AppConstants.TITLE_PROFILE_SETTINGS, fontWeight = FontWeight.ExtraBold) }, // Title text
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Back button action
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") // Back icon
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) { // Logout button action
                        Icon(Icons.AutoMirrored.Rounded.Logout, AppConstants.BTN_LOG_OUT, tint = MaterialTheme.colorScheme.error) // Logout icon
                    }
                },
                // Glassmorphic surface styling
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) // Semi-transparent surface color
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Themed, animated background
            HorizontalWavyBackground(isDarkTheme = isDarkTheme) // Animated wavy background

            // Adaptive container ensures the form stays centered and correctly sized on tablets
            AdaptiveScreenContainer(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .verticalScroll(rememberScrollState()), // Enables vertical scrolling
                maxWidth = 600.dp // Max width for content container
            ) { isTablet ->
                Column(
                    modifier = Modifier.padding(horizontal = if (isTablet) 32.dp else 16.dp, vertical = 16.dp), // Adjusts padding for tablets and phones
                    horizontalAlignment = Alignment.CenterHorizontally // Centers content horizontally
                ) {
                    // Profile Header: User avatar and image update trigger
                    ProfileHeader(
                        photoUrl = currentPhotoUrl, // Current user photo URL
                        isUploading = viewModel.isUploading, // State for upload progress
                        onPickPhoto = { photoPickerLauncher.launch("image/*") } // Action to pick a new photo
                    )

                    // Main Settings Card
                    Card(
                        modifier = Modifier.fillMaxWidth(), // Card fills available width
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Semi-transparent container color
                        ),
                        shape = RoundedCornerShape(24.dp), // Rounded card corners
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Card elevation for shadow
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)) // Subtle card border
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) { // Content padding within the card
                            // Section 1: Personal metadata (Name, Title, Phone, Email)
                            PersonalInfoSection(
                                title = viewModel.title, // User title (e.g., Mr, Ms)
                                onTitleChange = { viewModel.title = it }, // Title change action
                                firstName = viewModel.firstName, // User first name
                                onFirstNameChange = { viewModel.firstName = it }, // First name change action
                                surname = viewModel.surname, // User surname
                                onSurnameChange = { viewModel.surname = it }, // Surname change action
                                phoneNumber = viewModel.phoneNumber, // User phone number
                                onPhoneNumberChange = { viewModel.phoneNumber = it }, // Phone number change action
                                email = localUser?.email ?: "", // Current user email
                                onEditEmail = { showEmailPopup = true } // Action to open the email edit popup
                            )

                            Spacer(modifier = Modifier.height(32.dp)) // Vertical space between sections

                            // Section 2: Home Address management
                            ProfileAddressSection(
                                address = viewModel.selectedAddress, // User current address
                                onChangeAddress = { showAddressPopup = true } // Action to open the address management popup
                            )

                            Spacer(modifier = Modifier.height(32.dp)) // Vertical space between sections

                            // Section 3: Billing and Payment preferences
                            ProfilePaymentSection(
                                paymentMethod = viewModel.selectedPaymentMethod, // Selected payment method
                                onChangePayment = { currentPopupStep = 1; showSelectionPopup = true } // Action to open payment method selection popup
                            )

                            Spacer(modifier = Modifier.height(32.dp)) // Vertical space between sections

                            // Section 4: Authentication Security
                            ProfileSecuritySection(
                                onChangePassword = { showPasswordPopup = true } // Action to open the password change popup
                            )

                            Spacer(modifier = Modifier.height(32.dp)) // Vertical space between sections

                            // Primary Action: Save all changes back to database
                            Button(
                                onClick = {
                                    viewModel.updateProfile { msg -> // Saves all profile and theme changes to DB
                                        scope.launch { snackbarHostState.showSnackbar(msg) } // Shows result snackbar message
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp), // Button size
                                shape = RoundedCornerShape(12.dp), // Rounded button corners
                                enabled = !viewModel.isUploading // Disables button while uploading avatar
                            ) {
                                if (viewModel.isUploading) { // Show progress indicator if uploading
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else { // Default button label
                                    @Suppress("DEPRECATION")
                                    Text(AppConstants.BTN_SAVE_PROFILE, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    // Bottom spacing for ergonomic scrolling
                    Spacer(modifier = Modifier.height(16.dp)) // Spacing at the bottom of the screen
                }
            }
        }

        // --- MANAGEMENT DIALOGS ---

        // Dialog for choosing and configuring payment methods (Card, PayPal, etc.)
        PaymentMethodDialog(
            show = showSelectionPopup, // Controls dialog visibility
            currentStep = currentPopupStep, // Multi-step dialog tracker
            selectedMethod = viewModel.selectedPaymentMethod, // Selected method state
            onMethodSelect = { viewModel.selectedPaymentMethod = it }, // Handles method selection // Corrected reference
            onNextStep = { currentPopupStep = 2 }, // Advances to next step
            onBackStep = { currentPopupStep = 1 }, // Returns to previous step
            onDismiss = { showSelectionPopup = false }, // Closes the dialog
            onConfirm = { finalMethod -> // Finalizes the selection
                viewModel.selectedPaymentMethod = finalMethod
                viewModel.updateProfile { msg -> // Persist the new method
                    showSelectionPopup = false
                    scope.launch { snackbarHostState.showSnackbar(msg) } // Inform user of success
                }
            },
            cardNumber = cardNumber, // Card number state
            onCardNumberChange = { cardNumber = it }, // Update card number
            cardExpiry = cardExpiry, // Card expiry state
            onCardExpiryChange = { cardExpiry = it }, // Update card expiry
            paypalEmail = paypalEmail, // Paypal email state
            onPaypalEmailChange = { paypalEmail = it } // Update paypal email
        )

        // Handles secure password update requests
        AppPopups.ProfilePasswordChange(
            show = showPasswordPopup, // Controls dialog visibility
            userEmail = user?.email ?: "", // User email for password reset email
            onDismiss = { showPasswordPopup = false }, // Closes the dialog
            onSuccess = { // Action on successful request
                showPasswordPopup = false
                scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_PASSWORD_UPDATED) } // Inform user of success
            }
        )

        // Utility dialog for editing the primary home/billing address
        AppPopups.AddressManagement(
            show = showAddressPopup, // Controls dialog visibility
            onDismiss = { showAddressPopup = false }, // Closes the dialog
            onSave = { newAddr -> // Action to save new address
                viewModel.selectedAddress = newAddr
                viewModel.updateProfile { msg -> // Persist the new address
                    showAddressPopup = false
                    scope.launch { snackbarHostState.showSnackbar(msg) } // Inform user of success
                }
            }
        )

        // Handles major account identifiers change (triggers session logout on success for security)
        AppPopups.ProfileEmailChange(
            show = showEmailPopup, // Controls dialog visibility
            currentEmail = user?.email ?: "", // Current email for validation
            onDismiss = { showEmailPopup = false }, // Closes the dialog
            onSuccess = { _ -> // Action on successful email update
                viewModel.signOut(localUser) // Logout for security
                navController.navigate(AppConstants.ROUTE_AUTH) { popUpTo(0) } // Redirect to login screen
            }
        )
    }
}
