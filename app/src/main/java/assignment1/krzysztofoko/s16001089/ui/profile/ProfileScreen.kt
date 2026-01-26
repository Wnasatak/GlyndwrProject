package assignment1.krzysztofoko.s16001089.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.AppPopups
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.SelectionOption
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val localUserFlow = remember(viewModel) { viewModel.localUser }
    val localUser by localUserFlow.collectAsState(initial = null)

    // Initialize ViewModel fields once data is loaded
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
                title = { Text("Profile Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Log Out", tint = MaterialTheme.colorScheme.error) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalWavyBackground(isDarkTheme = isDarkTheme)
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(vertical = 24.dp)) {
                    UserAvatar(
                        photoUrl = currentPhotoUrl, modifier = Modifier.size(130.dp), isLarge = true,
                        onClick = { if (!viewModel.isUploading) photoPickerLauncher.launch("image/*") },
                        overlay = { if (viewModel.isUploading) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) } } }
                    )
                    SmallFloatingActionButton(onClick = { if (!viewModel.isUploading) photoPickerLauncher.launch("image/*") }, shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.offset(x = (-8).dp, y = (-8).dp)) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White) }
                }

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(value = viewModel.firstName, onValueChange = { viewModel.firstName = it }, label = { Text("First Name") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = viewModel.surname, onValueChange = { viewModel.surname = it }, label = { Text("Surname") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.phoneNumber,
                            onValueChange = { viewModel.phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(onClick = { showEmailPopup = true }, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text("Email Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text(localUser?.email ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
                                Text("Edit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp)); Text("Active Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(onClick = { showAddressPopup = true }, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(16.dp)); Text(viewModel.selectedAddress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Text("Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp)); Text("Active Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(onClick = { currentPopupStep = 1; showSelectionPopup = true }, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = when { viewModel.selectedPaymentMethod.contains("Google") -> Icons.Default.AccountBalanceWallet; viewModel.selectedPaymentMethod.contains("PayPal") -> Icons.Default.Payment; viewModel.selectedPaymentMethod.contains("Uni") -> Icons.Default.School; else -> Icons.Default.CreditCard }, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(16.dp)); Text(viewModel.selectedPaymentMethod, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp)); Text("Account Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { showPasswordPopup = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Lock, null); Spacer(Modifier.width(8.dp)); Text("Change Account Password")
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = {
                            viewModel.updateProfile { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), enabled = !viewModel.isUploading) {
                            if (viewModel.isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text("Save General Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showSelectionPopup) {
            Dialog(onDismissRequest = { showSelectionPopup = false }) {
                Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedContent(targetState = currentPopupStep, label = "popupStepTransition") { step ->
                            if (step == 1) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Choose Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    SelectionOption("Credit or Debit Card", Icons.Default.AddCard, viewModel.selectedPaymentMethod.contains("Card")) { viewModel.selectedPaymentMethod = "Credit or Debit Card"; currentPopupStep = 2 }
                                    SelectionOption("Google Pay", Icons.Default.AccountBalanceWallet, viewModel.selectedPaymentMethod == "Google Pay") { viewModel.selectedPaymentMethod = "Google Pay"; currentPopupStep = 2 }
                                    SelectionOption("PayPal", Icons.Default.Payment, viewModel.selectedPaymentMethod == "PayPal") { viewModel.selectedPaymentMethod = "PayPal"; currentPopupStep = 2 }
                                    SelectionOption("University Account", Icons.Default.School, viewModel.selectedPaymentMethod == "University Account") { viewModel.selectedPaymentMethod = "University Account"; currentPopupStep = 2 }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(onClick = { showSelectionPopup = false }) { Text("Cancel") }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Details Confirmation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    when {
                                        viewModel.selectedPaymentMethod.contains("Card") -> {
                                            OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("Card Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                OutlinedTextField(value = cardExpiry, onValueChange = { cardExpiry = it }, label = { Text("MM/YY") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("CVV") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                                            }
                                        }
                                        viewModel.selectedPaymentMethod.contains("PayPal") -> {
                                            Icon(Icons.Default.Payment, null, modifier = Modifier.size(64.dp), tint = Color(0xFF003087))
                                            Spacer(modifier = Modifier.height(16.dp))
                                            OutlinedTextField(value = paypalEmail, onValueChange = { paypalEmail = it }, label = { Text("PayPal Email") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Email, null) }, shape = RoundedCornerShape(12.dp))
                                        }
                                        else -> {
                                            Icon(if (viewModel.selectedPaymentMethod.contains("Google")) Icons.Default.AccountBalanceWallet else Icons.Default.School, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(16.dp)); Text("Authorize link to your account safely.", textAlign = TextAlign.Center)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(onClick = { currentPopupStep = 1 }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Back") }
                                        Button(onClick = {
                                            val finalMethod = if (viewModel.selectedPaymentMethod.contains("Card")) { "Card Ending in " + cardNumber.takeLast(4).ifEmpty { "4242" } } else viewModel.selectedPaymentMethod
                                            viewModel.selectedPaymentMethod = finalMethod
                                            viewModel.updateProfile { msg ->
                                                showSelectionPopup = false
                                                scope.launch { snackbarHostState.showSnackbar(msg) }
                                            }
                                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Confirm") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AppPopups.ProfilePasswordChange(
            show = showPasswordPopup,
            userEmail = user?.email ?: "",
            onDismiss = { showPasswordPopup = false },
            onSuccess = { showPasswordPopup = false; scope.launch { snackbarHostState.showSnackbar("Password updated safely.") } }
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
            onSuccess = { _ -> auth.signOut(); navController.navigate("auth") { popUpTo(0) } }
        )
    }
}
