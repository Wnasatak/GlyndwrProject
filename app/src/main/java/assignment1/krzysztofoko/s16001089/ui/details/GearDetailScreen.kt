package assignment1.krzysztofoko.s16001089.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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
    
    var selectedSize by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var showPickupPopup by remember { mutableStateOf(false) }

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

    suspend fun refreshGear() {
        val updated = db.gearDao().getGearById(gearId)
        if (updated != null) gear = updated
    }

    LaunchedEffect(gearId, user) {
        loading = true
        if (gear == null) {
            val fetchedGear = db.gearDao().getGearById(gearId)
            if (fetchedGear != null) {
                gear = fetchedGear
                selectedSize = fetchedGear.sizes.split(",").firstOrNull() ?: "M"
                selectedColor = fetchedGear.colors.split(",").firstOrNull() ?: "Default"
            }
        } else {
            selectedSize = gear?.sizes?.split(",")?.firstOrNull() ?: "M"
            selectedColor = gear?.colors?.split(",")?.firstOrNull() ?: "Default"
        }
        if (user != null) db.userDao().addToHistory(HistoryItem(user.uid, gearId))
        loading = false
    }

    val images = remember(gear) { listOfNotNull(gear?.imageUrl, gear?.secondaryImageUrl) }

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
                    val isFree = currentGear.price <= 0
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(bottom = 100.dp)) {
                        item {
                            GearImageGallery(
                                images = images,
                                selectedImageIndex = selectedImageIndex,
                                onImageClick = { selectedImageIndex = it },
                                isFeatured = currentGear.isFeatured,
                                title = currentGear.title
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().offset(y = (-24).dp),
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    GearHeaderSection(gear = currentGear)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    GearTagsSection(tags = currentGear.productTags)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(24.dp))

                                    GearOptionSelectors(
                                        sizes = currentGear.sizes,
                                        selectedSize = selectedSize,
                                        onSizeSelected = { selectedSize = it },
                                        colors = currentGear.colors,
                                        selectedColor = selectedColor,
                                        onColorSelected = { selectedColor = it },
                                        onColorClick = { color ->
                                            if (images.size > 1) {
                                                selectedImageIndex = if (color.contains("Pink", ignoreCase = true)) 1 else 0
                                            }
                                        }
                                    )

                                    GearStockIndicator(
                                        stockCount = currentGear.stockCount,
                                        quantity = quantity,
                                        isOwned = isOwned,
                                        isFree = isFree,
                                        onQuantityChange = { quantity = it }
                                    )

                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(text = "Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = currentGear.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    GearSpecsCard(material = currentGear.material, sku = currentGear.sku, category = currentGear.category)
                                    Spacer(modifier = Modifier.height(32.dp))

                                    ReviewSection(
                                        productId = gearId,
                                        reviews = allReviews,
                                        localUser = localUser,
                                        isLoggedIn = user != null,
                                        db = db,
                                        isDarkTheme = isDarkTheme,
                                        onReviewPosted = { scope.launch { snackbarHostState.showSnackbar("Thanks for your review!") } },
                                        onLoginClick = onLoginRequired
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (gear != null && !loading) {
            val currentGear = gear!!
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                GearBottomActionBar(
                    isOwned = isOwned,
                    price = currentGear.price,
                    stockCount = currentGear.stockCount,
                    quantity = quantity,
                    isLoggedIn = user != null,
                    onViewInvoice = { onViewInvoice(currentGear.id) },
                    onPickupInfo = { showPickupPopup = true },
                    onLoginRequired = onLoginRequired,
                    onCheckout = { showOrderFlow = true },
                    onFreePickup = {
                        scope.launch {
                            db.userDao().addPurchase(PurchaseItem(user!!.uid, currentGear.id))
                            db.gearDao().reduceStock(currentGear.id, 1)
                            refreshGear()
                            snackbarHostState.showSnackbar("Success! Please pick up your item at Student Hub.")
                        }
                    }
                )
            }
        }

        if (showOrderFlow && gear != null) {
            OrderFlowDialog(
                book = Book(id = gear!!.id, title = gear!!.title, price = gear!!.price * quantity),
                user = localUser,
                onDismiss = { showOrderFlow = false },
                onEditProfile = { showOrderFlow = false; onNavigateToProfile() },
                onComplete = { 
                    showOrderFlow = false
                    scope.launch { 
                        db.gearDao().reduceStock(gear!!.id, quantity)
                        refreshGear()
                        snackbarHostState.showSnackbar("Order successful!") 
                    }
                }
            )
        }

        if (showPickupPopup) PickupInfoDialog(onDismiss = { showPickupPopup = false })
    }
}
