package assignment1.krzysztofoko.s16001089.ui.details.gear

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Detailed Information Screen for University Gear (Merchandise).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearDetailScreen(
    navController: NavController,     
    gearId: String,                   
    initialGear: Gear? = null,        
    user: FirebaseUser?,              
    onLoginRequired: () -> Unit,      
    onBack: () -> Unit,               
    isDarkTheme: Boolean,             
    onToggleTheme: () -> Unit,        
    onNavigateToProfile: () -> Unit,  
    onViewInvoice: (String) -> Unit,  
    viewModel: GearViewModel = viewModel(factory = GearViewModelFactory(
        gearDao = AppDatabase.getDatabase(LocalContext.current).gearDao(),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        auditDao = AppDatabase.getDatabase(LocalContext.current).auditDao(),
        gearId = gearId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val gear by viewModel.gear.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val similarGear by viewModel.similarGear.collectAsState()
    val selectedSize by viewModel.selectedSize.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val selectedImageIndex by viewModel.selectedImageIndex.collectAsState()
    
    val localUser by viewModel.localUser.collectAsState()
    val roleDiscounts by viewModel.roleDiscounts.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val orderConfirmation by viewModel.orderConfirmation.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    // Dynamic Discount Calculation
    val effectiveDiscount = remember(localUser, roleDiscounts) {
        val userRole = localUser?.role ?: "user"
        val roleRate = roleDiscounts.find { it.role == userRole }?.discountPercent ?: 0.0
        val individualRate = localUser?.discountPercent ?: 0.0
        maxOf(roleRate, individualRate)
    }

    var showPickupPopup by remember { mutableStateOf(false) }
    var showOrderFlow by remember { mutableStateOf(false) }
    var showAddConfirm by remember { mutableStateOf(false) }
    var isProcessingAddition by remember { mutableStateOf(false) }

    val images = remember(gear) { listOfNotNull(gear?.imageUrl, gear?.secondaryImageUrl) }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme, wave1HeightFactor = 0.45f, wave2HeightFactor = 0.65f, wave1Amplitude = 80f, wave2Amplitude = 100f)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = gear?.title ?: AppConstants.TITLE_GEAR_DETAILS, 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return") } },
                    actions = { IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) } },
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
                        Spacer(Modifier.height(16.dp)); Text(AppConstants.MSG_ITEM_NOT_FOUND)
                        TextButton(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                gear?.let { currentGear ->
                    val isFree = currentGear.price <= 0
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(bottom = 120.dp)) {
                        item {
                            ProductHeaderImage(book = currentGear.toBook(), isOwned = isOwned, isDarkTheme = isDarkTheme, primaryColor = MaterialTheme.colorScheme.primary)
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().offset(y = (-24).dp),
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "Wrexham University", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            Text(text = currentGear.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                        }
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (currentGear.price > 0) {
                                                val discountMultiplier = (100.0 - effectiveDiscount) / 100.0
                                                val discountedPrice = currentGear.price * discountMultiplier
                                                
                                                if (effectiveDiscount > 0) {
                                                    Text(text = "£${String.format(Locale.US, "%.2f", currentGear.price)}", style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                    Text(text = "£${String.format(Locale.US, "%.2f", discountedPrice)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    
                                                    val roleName = localUser?.role?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "User"
                                                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 4.dp)) { 
                                                        Text(
                                                            text = "${roleName.uppercase()} DISCOUNT (-${effectiveDiscount.toInt()}%)", 
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                                                            color = Color(0xFF2E7D32), 
                                                            fontWeight = FontWeight.Bold, 
                                                            fontSize = 10.sp
                                                        ) 
                                                    }
                                                } else {
                                                    Text(text = "£${String.format(Locale.US, "%.2f", currentGear.price)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                                                }
                                            } else {
                                                Text(text = "FREE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    GearTagsSection(tags = currentGear.productTags)
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(24.dp))

                                    GearOptionSelectors(
                                        sizes = currentGear.sizes,
                                        selectedSize = selectedSize,
                                        onSizeSelected = { viewModel.setSelectedSize(it) },
                                        colors = currentGear.colors,
                                        selectedColor = selectedColor,
                                        onColorSelected = { viewModel.setSelectedColor(it) },
                                        onColorClick = { color ->
                                            if (images.size > 1) {
                                                viewModel.setSelectedImageIndex(if (color.contains("Pink", ignoreCase = true)) 1 else 0)
                                            }
                                        }
                                    )

                                    GearStockIndicator(stockCount = currentGear.stockCount, quantity = quantity, isOwned = isOwned, isFree = isFree, onQuantityChange = { viewModel.setQuantity(it) })

                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(text = AppConstants.SECTION_DESCRIPTION_GEAR, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Text(text = currentGear.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    GearSpecsCard(material = currentGear.material, sku = currentGear.sku, category = currentGear.category)
                                    
                                    if (similarGear.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(40.dp))
                                        Text(text = AppConstants.TITLE_SIMILAR_PRODUCTS, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(16.dp))
                                        UniversalProductSlider(products = similarGear.map { it.toBook() }, onProductClick = { selectedBook -> navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${selectedBook.id}") })
                                    }

                                    Spacer(modifier = Modifier.height(40.dp))
                                    ReviewSection(productId = gearId, reviews = allReviews, localUser = localUser, isLoggedIn = user != null, db = AppDatabase.getDatabase(LocalContext.current), isDarkTheme = isDarkTheme, onReviewPosted = { scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_THANKS_REVIEW) } }, onLoginClick = onLoginRequired)
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
                val discountMultiplier = (100.0 - effectiveDiscount) / 100.0
                val unitPrice = currentGear.price * discountMultiplier
                GearBottomActionBar(
                    isOwned = isOwned,
                    price = unitPrice * quantity,
                    stockCount = currentGear.stockCount,
                    quantity = quantity,
                    isLoggedIn = user != null,
                    onViewInvoice = { onViewInvoice(currentGear.id) },
                    onPickupInfo = { showPickupPopup = true },
                    onLoginRequired = onLoginRequired,
                    onCheckout = { showOrderFlow = true },
                    onFreePickup = { showAddConfirm = true }
                )
            }
        }

        if (showOrderFlow && gear != null) {
            AppPopups.OrderPurchase(
                show = showOrderFlow,
                book = gear!!.toBook(),
                user = localUser,
                roleDiscounts = roleDiscounts,
                onDismiss = { showOrderFlow = false },
                onEditProfile = { showOrderFlow = false; onNavigateToProfile() },
                onComplete = { finalPrice, orderRef -> 
                    viewModel.handlePurchaseComplete(context, quantity, finalPrice, orderRef) { msg ->
                        showOrderFlow = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            )
        }

        if (showPickupPopup) { PickupInfoDialog(orderConfirmation = orderConfirmation, onDismiss = { showPickupPopup = false }) }

        AppPopups.AddToLibraryConfirmation(
            show = showAddConfirm,
            itemTitle = gear?.title ?: "",
            category = AppConstants.CAT_GEAR,
            onDismiss = { showAddConfirm = false },
            onConfirm = {
                showAddConfirm = false
                isProcessingAddition = true
                scope.launch {
                    delay(2000)
                    viewModel.handleFreePickup(context) { msg ->
                        isProcessingAddition = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            }
        )

        AppPopups.AddingToLibraryLoading(show = isProcessingAddition, category = AppConstants.CAT_GEAR)
    }
}
