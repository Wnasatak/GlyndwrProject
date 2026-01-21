package assignment1.krzysztofoko.s16001089.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.SelectionOption
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var emailDisplay by remember { mutableStateOf(user?.email ?: "") }
    
    var localPreviewUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    var selectedPaymentMethod by remember { mutableStateOf("University Account") }
    var selectedAddress by remember { mutableStateOf("No address added yet") }
    var currentBalance by remember { mutableDoubleStateOf(0.0) }
    var currentRole by remember { mutableStateOf("student") }
    var savedPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Restore missing state variables
    var showSelectionPopup by remember { mutableStateOf(false) }
    var currentPopupStep by remember { mutableIntStateOf(1) }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var paypalEmail by remember { mutableStateOf("") }

    var showPasswordPopup by remember { mutableStateOf(false) }
    var showAddressPopup by remember { mutableStateOf(false) }
    var showEmailPopup by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            val localUser = db.userDao().getUserById(user.uid)
            if (localUser != null) {
                val names = localUser.name.split(" ")
                firstName = names.getOrNull(0) ?: ""
                surname = names.getOrNull(1) ?: ""
                selectedPaymentMethod = localUser.selectedPaymentMethod ?: "University Account"
                selectedAddress = localUser.address ?: "No address added yet"
                currentBalance = localUser.balance
                currentRole = localUser.role
                savedPhotoUrl = localUser.photoUrl
            }
        }
    }

    val currentPhotoUrl = localPreviewUri?.toString() ?: savedPhotoUrl ?: user?.photoUrl?.toString()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && user != null) {
            isUploading = true
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val avatarFile = File(context.filesDir, "avatar_${user.uid}.jpg")
                    val outputStream = FileOutputStream(avatarFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    val localFileUri = Uri.fromFile(avatarFile).toString()
                    savedPhotoUrl = localFileUri

                    // Update user local profile
                    db.userDao().upsertUser(UserLocal(
                        id = user.uid,
                        name = "$firstName $surname",
                        email = user.email ?: "",
                        photoUrl = localFileUri,
                        address = selectedAddress,
                        selectedPaymentMethod = selectedPaymentMethod,
                        balance = currentBalance,
                        role = currentRole
                    ))
                    
                    // NEW: Update all existing reviews to use the new avatar
                    db.userDao().updateReviewAvatars(user.uid, localFileUri)
                    
                    user.updateProfile(userProfileChangeRequest { photoUri = Uri.parse(localFileUri) })
                    isUploading = false
                    snackbarHostState.showSnackbar("Avatar updated everywhere!")
                } catch (e: Exception) {
                    isUploading = false
                    snackbarHostState.showSnackbar("Error saving image.")
                }
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
                        photoUrl = currentPhotoUrl,
                        modifier = Modifier.size(130.dp),
                        isLarge = true,
                        onClick = { if (!isUploading) photoPickerLauncher.launch("image/*") },
                        overlay = {
                            if (isUploading) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(color = Color.White) }
                            }
                        }
                    )
                    SmallFloatingActionButton(onClick = { if (!isUploading) photoPickerLauncher.launch("image/*") }, shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.offset(x = (-8).dp, y = (-8).dp)) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White) }
                }

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = surname, onValueChange = { surname = it }, label = { Text("Surname") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(onClick = { showEmailPopup = true }, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text("Email Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text(emailDisplay, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
                                Text("Edit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp)); Text("Active Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(onClick = { showAddressPopup = true }, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(16.dp)); Text(selectedAddress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Text("Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp)); Text("Active Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(onClick = { currentPopupStep = 1; showSelectionPopup = true }, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = when { selectedPaymentMethod.contains("Google") -> Icons.Default.AccountBalanceWallet; selectedPaymentMethod.contains("PayPal") -> Icons.Default.Payment; selectedPaymentMethod.contains("Uni") -> Icons.Default.School; else -> Icons.Default.CreditCard }, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(16.dp)); Text(selectedPaymentMethod, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
                            isUploading = true
                            val profileUpdates = userProfileChangeRequest { displayName = "$firstName $surname".trim() }
                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    scope.launch {
                                        db.userDao().upsertUser(UserLocal(
                                            id = user.uid,
                                            name = "$firstName $surname",
                                            email = user.email ?: "",
                                            photoUrl = savedPhotoUrl,
                                            address = selectedAddress,
                                            selectedPaymentMethod = selectedPaymentMethod,
                                            balance = currentBalance,
                                            role = currentRole
                                        ))
                                        isUploading = false
                                        snackbarHostState.showSnackbar("Profile saved locally!")
                                    }
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), enabled = !isUploading) {
                            if (isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text("Save General Profile", fontWeight = FontWeight.Bold)
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
                                    SelectionOption("Credit or Debit Card", Icons.Default.AddCard, selectedPaymentMethod.contains("Card")) { selectedPaymentMethod = "Credit or Debit Card"; currentPopupStep = 2 }
                                    SelectionOption("Google Pay", Icons.Default.AccountBalanceWallet, selectedPaymentMethod == "Google Pay") { selectedPaymentMethod = "Google Pay"; currentPopupStep = 2 }
                                    SelectionOption("PayPal", Icons.Default.Payment, selectedPaymentMethod == "PayPal") { selectedPaymentMethod = "PayPal"; currentPopupStep = 2 }
                                    SelectionOption("University Account", Icons.Default.School, selectedPaymentMethod == "University Account") { selectedPaymentMethod = "University Account"; currentPopupStep = 2 }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(onClick = { showSelectionPopup = false }) { Text("Cancel") }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Details Confirmation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    when {
                                        selectedPaymentMethod.contains("Card") -> {
                                            OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("Card Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                OutlinedTextField(value = cardExpiry, onValueChange = { cardExpiry = it }, label = { Text("MM/YY") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("CVV") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                                            }
                                        }
                                        selectedPaymentMethod.contains("PayPal") -> {
                                            Icon(Icons.Default.Payment, null, modifier = Modifier.size(64.dp), tint = Color(0xFF003087))
                                            Spacer(modifier = Modifier.height(16.dp))
                                            OutlinedTextField(value = paypalEmail, onValueChange = { paypalEmail = it }, label = { Text("PayPal Email") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Email, null) }, shape = RoundedCornerShape(12.dp))
                                        }
                                        else -> {
                                            Icon(if (selectedPaymentMethod.contains("Google")) Icons.Default.AccountBalanceWallet else Icons.Default.School, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(16.dp)); Text("Authorize link to your account safely.", textAlign = TextAlign.Center)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(onClick = { currentPopupStep = 1 }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Back") }
                                        Button(onClick = { 
                                            if (selectedPaymentMethod.contains("Card")) {
                                                selectedPaymentMethod = "Card Ending in ${cardNumber.takeLast(4).ifEmpty { "4242" }}"
                                            }
                                            scope.launch {
                                                db.userDao().upsertUser(UserLocal(
                                                    id = user!!.uid,
                                                    name = "$firstName $surname",
                                                    email = user.email ?: "",
                                                    photoUrl = savedPhotoUrl,
                                                    address = selectedAddress,
                                                    selectedPaymentMethod = selectedPaymentMethod,
                                                    balance = currentBalance,
                                                    role = currentRole
                                                ))
                                                showSelectionPopup = false 
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

        if (showPasswordPopup) { PasswordChangeDialog(userEmail = user?.email ?: "", onDismiss = { showPasswordPopup = false }, onSuccess = { showPasswordPopup = false; scope.launch { snackbarHostState.showSnackbar("Got it! Your password has been updated safely.") } }) }
        if (showAddressPopup) { AddressManagementDialog(onDismiss = { showAddressPopup = false }, onSave = { newAddr -> 
            val userId = user?.uid ?: return@AddressManagementDialog
            selectedAddress = newAddr
            showAddressPopup = false
            scope.launch {
                db.userDao().upsertUser(UserLocal(
                    id = userId,
                    name = "$firstName $surname",
                    email = user.email ?: "",
                    photoUrl = savedPhotoUrl,
                    address = newAddr,
                    selectedPaymentMethod = selectedPaymentMethod,
                    balance = currentBalance,
                    role = currentRole
                ))
                snackbarHostState.showSnackbar("Your address has been updated successfully.")
            }
        }) }
        if (showEmailPopup) { EmailChangeDialog(currentEmail = user?.email ?: "", onDismiss = { showEmailPopup = false }, onSuccess = { _ -> auth.signOut(); navController.navigate("auth") { popUpTo(0) } }) }
    }
}

@Composable
fun EmailChangeDialog(currentEmail: String, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var password by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Change Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Step $step of 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp)); Text(validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedContent(targetState = step, label = "emailStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when(currentStep) {
                            1 -> {
                                Text("Verify Identity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Text("Enter current password to proceed.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = password, onValueChange = { password = it; validationMsg = null }, label = { Text("Current Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            2 -> {
                                Text("New Email", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Text("Enter your new email address.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = newEmail, onValueChange = { newEmail = it; validationMsg = null }, label = { Text("New Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            3 -> {
                                Text("Confirmation", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Text("Updating to: $newEmail\n\nYou will be logged out to verify your new email.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                if (loading) { CircularProgressIndicator() } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { if (step == 1) onDismiss() else { step--; validationMsg = null } }, modifier = Modifier.weight(1f)) { Text(if (step == 1) "Cancel" else "Back") }
                        Button(onClick = {
                            when(step) {
                                1 -> {
                                    if (password.isEmpty()) { validationMsg = "Please enter your password."; return@Button }
                                    loading = true
                                    val cred = EmailAuthProvider.getCredential(currentEmail, password)
                                    auth.currentUser?.reauthenticate(cred)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) step = 2 else validationMsg = "Incorrect password."
                                    }
                                }
                                2 -> {
                                    if (newEmail.isEmpty() || !newEmail.contains("@")) { validationMsg = "Please enter a valid email."; return@Button }
                                    step = 3
                                }
                                3 -> {
                                    loading = true
                                    auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) onSuccess(newEmail) else validationMsg = it.exception?.localizedMessage ?: "Failed."
                                    }
                                }
                            }
                        }, modifier = Modifier.weight(1f)) { Text(if (step == 3) "Verify & Logout" else "Next") }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordChangeDialog(userEmail: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Step $step of 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp)); Text(text = validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedContent(targetState = step, label = "passStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        when(currentStep) {
                            1 -> {
                                Text("Verify it's you", style = MaterialTheme.typography.titleMedium)
                                Text("Please enter your current password so we know it's you.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it; validationMsg = null }, label = { Text("Current Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            2 -> {
                                Text("New Password", style = MaterialTheme.typography.titleMedium)
                                Text("Pick a strong password (6-20 characters).", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; validationMsg = null }, label = { Text("New Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                            3 -> {
                                Text("Confirm Password", style = MaterialTheme.typography.titleMedium)
                                Text("Please type the new password one more time.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                OutlinedTextField(value = repeatPassword, onValueChange = { repeatPassword = it; validationMsg = null }, label = { Text("Repeat Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                if (loading) { CircularProgressIndicator() } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { if (step == 1) onDismiss() else { step--; validationMsg = null } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 1) "Cancel" else "Back") }
                        Button(onClick = {
                            when(step) {
                                1 -> {
                                    if (currentPassword.isEmpty()) { validationMsg = "Please enter your current password first."; return@Button }
                                    loading = true
                                    val credential = EmailAuthProvider.getCredential(userEmail, currentPassword)
                                    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) step = 2 else validationMsg = "The password you entered doesn't seem right. Please check it."
                                    }
                                }
                                2 -> {
                                    if (newPassword.isEmpty()) { validationMsg = "The new password box is empty. Please fill it in."; return@Button }
                                    if (newPassword.length < 6) { validationMsg = "That's a bit too short! Try at least 6 characters."; return@Button }
                                    if (newPassword.length > 20) { validationMsg = "That's a bit too long! Keep it under 20 characters."; return@Button }
                                    step = 3
                                }
                                3 -> {
                                    if (repeatPassword.isEmpty()) { validationMsg = "Please repeat your new password here."; return@Button }
                                    if (newPassword != repeatPassword) { validationMsg = "The passwords don't match. Please type them again carefully."; return@Button }
                                    loading = true
                                    auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener {
                                        loading = false
                                        if (it.isSuccessful) onSuccess() else validationMsg = it.exception?.localizedMessage ?: "Something went wrong. Please try again."
                                    }
                                }
                            }
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 3) "Save" else "Next") }
                    }
                }
            }
        }
    }
}

@Composable
fun AddressManagementDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var validationMsg by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Manage Address", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Step $step of 2", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                AnimatedVisibility(visible = validationMsg != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp)); Text(text = validationMsg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedContent(targetState = step, label = "addrStepAnim") { currentStep ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (currentStep == 1) {
                            Text("Location Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = street, onValueChange = { street = it; validationMsg = null }, label = { Text("Street Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = city, onValueChange = { city = it; validationMsg = null }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        } else {
                            Text("Final Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = postcode, onValueChange = { postcode = it; validationMsg = null }, label = { Text("Postcode") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = country, onValueChange = { country = it; validationMsg = null }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (step == 1) onDismiss() else { step = 1; validationMsg = null } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 1) "Cancel" else "Back") }
                    Button(onClick = {
                        if (step == 1) {
                            if (street.isEmpty()) { validationMsg = "Please tell us your street address."; return@Button }
                            if (city.isEmpty()) { validationMsg = "The city box is empty. Please fill it in."; return@Button }
                            step = 2
                        } else {
                            if (postcode.isEmpty()) { validationMsg = "We need your postcode to continue."; return@Button }
                            if (country.isEmpty()) { validationMsg = "Please enter your country."; return@Button }
                            onSave("$street, $city, $postcode, $country")
                        }
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text(if (step == 2) "Save" else "Next") }
                }
            }
        }
    }
}
