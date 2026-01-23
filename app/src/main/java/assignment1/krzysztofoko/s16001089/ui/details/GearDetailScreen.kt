package assignment1.krzysztofoko.s16001089.ui.details

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearDetailScreen(
    gearId: String,
    initialGear: Gear? = null,
    user: FirebaseUser?,
    onLoginRequired: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onViewInvoice: (String) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var gear by remember { mutableStateOf(initialGear) }
    var loading by remember { mutableStateOf(true) }
    var selectedSize by remember { mutableStateOf("M") }
    
    val sizes = listOf("S", "M", "L", "XL")
    val primaryColor = MaterialTheme.colorScheme.primary

    val localUser by remember(user) {
        if (user != null) db.userDao().getUserFlow(user.uid)
        else flowOf(null)
    }.collectAsState(initial = null)

    val purchaseIds by remember(user?.uid) {
        if (user != null) db.userDao().getPurchaseIds(user.uid) else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val isOwned = remember(purchaseIds) { purchaseIds.contains(gearId) }
    val allReviews by db.userDao().getReviewsForProduct(gearId).collectAsState(initial = emptyList())
    
    var showOrderFlow by remember { mutableStateOf(false) }

    LaunchedEffect(gearId, user) {
        loading = true
        if (gear == null) {
            val bookStub = db.bookDao().getBookById(gearId)
            if (bookStub != null) {
                gear = Gear(
                    id = bookStub.id,
                    title = bookStub.title,
                    price = bookStub.price,
                    description = bookStub.description,
                    imageUrl = bookStub.imageUrl,
                    category = bookStub.category,
                    mainCategory = bookStub.mainCategory
                )
            }
        }
        if (user != null) db.userDao().addToHistory(HistoryItem(user.uid, gearId))
        loading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(text = gear?.title ?: "Gear Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                )
            }
        ) { paddingValues ->
            if (loading && gear == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (gear == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text("Item details not available.")
                        TextButton(onClick = onBack) { Text("Go Back") }
                    }
                }
            } else {
                gear?.let { currentGear ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                        item {
                            // Reusing the beautiful animated header but passing a custom "Book" object created from Gear
                            ProductHeaderImage(
                                book = Book(
                                    id = currentGear.id,
                                    title = currentGear.title,
                                    imageUrl = currentGear.imageUrl,
                                    mainCategory = currentGear.mainCategory,
                                    price = currentGear.price
                                ),
                                isOwned = isOwned,
                                isDarkTheme = isDarkTheme,
                                primaryColor = primaryColor
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)), 
                                shape = RoundedCornerShape(24.dp), 
                                border = BorderStroke(
                                    1.dp, 
                                    if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) 
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(text = currentGear.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "Official University Merchandise", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // SIZE SELECTION - New functionality for Gear
                                    Text(text = "Select Size", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        sizes.forEach { size ->
                                            Surface(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clickable { selectedSize = size },
                                                shape = CircleShape,
                                                color = if (selectedSize == size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                border = if (selectedSize == size) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(text = size, fontWeight = FontWeight.Bold, color = if (selectedSize == size) Color.White else MaterialTheme.colorScheme.onSurface)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(text = "About this item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp)); Text(text = currentGear.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (isOwned) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                if (currentGear.price > 0) {
                                                    Button(onClick = { onViewInvoice(currentGear.id) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)) {
                                                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null); Spacer(Modifier.width(12.dp)); Text("View Order Details", fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        if (user != null) {
                                                            scope.launch {
                                                                db.userDao().deletePurchase(user.uid, currentGear.id)
                                                                snackbarHostState.showSnackbar("Item removed from your collection")
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                                ) {
                                                    Icon(Icons.Default.DeleteOutline, null); Spacer(Modifier.width(12.dp)); Text("Remove Item")
                                                }
                                            }
                                        } else if (user == null) {
                                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.LockPerson, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                                    Spacer(Modifier.height(12.dp)); Text("Sign In Required", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text("Sign in to purchase this gear.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(20.dp)); Button(onClick = onLoginRequired, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { 
                                                        Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("Sign in to Shop") 
                                                    }
                                                }
                                            }
                                        } else {
                                            if (currentGear.price == 0.0) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            db.userDao().addPurchase(PurchaseItem(user.uid, currentGear.id))
                                                            snackbarHostState.showSnackbar("Item reserved for collection!")
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                                    shape = RoundedCornerShape(16.dp)
                                                ) {
                                                    Icon(Icons.Default.AddShoppingCart, null)
                                                    Spacer(Modifier.width(12.dp))
                                                    Text("Get for Free", fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                val discountedPrice = currentGear.price * 0.9
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = "£${String.format(Locale.US, "%.2f", currentGear.price)}", style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                        Spacer(Modifier.width(12.dp)); Text(text = "£${String.format(Locale.US, "%.2f", discountedPrice)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) { Text("STUDENT PRICE (-10%)", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                                                    Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { showOrderFlow = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Order Now!", fontWeight = FontWeight.Bold) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            ReviewSection(
                                productId = gearId,
                                reviews = allReviews,
                                localUser = localUser,
                                isLoggedIn = user != null,
                                db = db,
                                isDarkTheme = isDarkTheme,
                                onReviewPosted = { scope.launch { snackbarHostState.showSnackbar("Thanks for your feedback!") } },
                                onLoginClick = onLoginRequired
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            }
        }

        if (showOrderFlow && gear != null) {
            // Reusing OrderFlowDialog but adapting it for Gear properties
            OrderFlowDialog(
                book = Book(id = gear!!.id, title = gear!!.title, price = gear!!.price),
                user = localUser,
                onDismiss = { showOrderFlow = false },
                onEditProfile = { showOrderFlow = false; onNavigateToProfile() },
                onComplete = { 
                    showOrderFlow = false
                    scope.launch { snackbarHostState.showSnackbar("Order successful! Collect your item at the Student Hub.") }
                }
            )
        }
    }
}
