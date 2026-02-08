package assignment1.krzysztofoko.s16001089.ui.details.gear

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
 * Detailed Information Screen for University Gear.
 * Fixed tablet layout and bottom bar positioning.
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
    
    val isTablet = isTablet()

    val gear by viewModel.gear.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val similarGear by viewModel.similarGear.collectAsState()
    val selectedSize by viewModel.selectedSize.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    
    val localUser by viewModel.localUser.collectAsState()
    val roleDiscounts by viewModel.roleDiscounts.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val orderConfirmation by viewModel.orderConfirmation.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    val effectiveDiscount = remember(localUser, roleDiscounts) {
        val uRole = localUser?.role ?: "user"
        val roleRate = roleDiscounts.find { it.role == uRole }?.discountPercent ?: 0.0
        val individualRate = localUser?.discountPercent ?: 0.0
        maxOf(roleRate, individualRate)
    }

    var showPickupPopup by remember { mutableStateOf(false) }
    var showOrderFlow by remember { mutableStateOf(false) }
    var showAddConfirm by remember { mutableStateOf(false) }
    var isProcessingAddition by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(gear?.title ?: "Details", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                    actions = { IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                )
            },
            bottomBar = {
                if (gear != null && !loading) {
                    val unitPrice = gear!!.price * ((100.0 - effectiveDiscount) / 100.0)
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)).padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.widthIn(max = AdaptiveWidths.Medium)) {
                            GearBottomActionBar(
                                isOwned = isOwned,
                                price = unitPrice * quantity,
                                stockCount = gear!!.stockCount,
                                quantity = quantity,
                                isLoggedIn = user != null,
                                onViewInvoice = { onViewInvoice(gear!!.id) },
                                onPickupInfo = { showPickupPopup = true },
                                onLoginRequired = onLoginRequired,
                                onCheckout = { showOrderFlow = true },
                                onFreePickup = { showAddConfirm = true }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            if (loading && gear == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (gear == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Item not found") }
            } else {
                gear?.let { item ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight().widthIn(max = AdaptiveWidths.Medium),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    ProductHeaderImage(book = item.toBook(), isOwned = isOwned, isDarkTheme = isDarkTheme, primaryColor = MaterialTheme.colorScheme.primary)
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().offset(y = (-24).dp),
                                    shape = RoundedCornerShape(32.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(Modifier.weight(1f)) {
                                                Text("Wrexham University", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                val discountPrice = item.price * ((100.0 - effectiveDiscount) / 100.0)
                                                if (item.price > 0) {
                                                    if (effectiveDiscount > 0) {
                                                        Text("£${String.format("%.2f", item.price)}", style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                                                        Text("£${String.format("%.2f", discountPrice)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                    } else {
                                                        Text("£${String.format("%.2f", item.price)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                                                    }
                                                } else {
                                                    Text("FREE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                                                }
                                            }
                                        }
                                        
                                        Spacer(Modifier.height(16.dp))
                                        GearTagsSection(item.productTags)
                                        Spacer(Modifier.height(24.dp))
                                        
                                        GearOptionSelectors(
                                            sizes = item.sizes, selectedSize = selectedSize, onSizeSelected = { viewModel.setSelectedSize(it) },
                                            colors = item.colors, selectedColor = selectedColor, onColorSelected = { viewModel.setSelectedColor(it) }, onColorClick = {}
                                        )
                                        
                                        GearStockIndicator(item.stockCount, quantity, isOwned, item.price <= 0) { viewModel.setQuantity(it) }
                                        
                                        Spacer(Modifier.height(32.dp))
                                        Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(item.description, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                                        
                                        Spacer(Modifier.height(32.dp))
                                        GearSpecsCard(item.material, item.sku, item.category)
                                        
                                        if (similarGear.isNotEmpty()) {
                                            Spacer(Modifier.height(40.dp))
                                            Text("Similar Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            UniversalProductSlider(similarGear.map { it.toBook() }) { navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${it.id}") }
                                        }
                                        
                                        Spacer(Modifier.height(40.dp))
                                        ReviewSection(productId = gearId, reviews = allReviews, localUser = localUser, isLoggedIn = user != null, db = AppDatabase.getDatabase(context), isDarkTheme = isDarkTheme, onReviewPosted = {}, onLoginClick = onLoginRequired)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showOrderFlow && gear != null) {
            AppPopups.OrderPurchase(show = showOrderFlow, book = gear!!.toBook(), user = localUser, roleDiscounts = roleDiscounts, onDismiss = { showOrderFlow = false }, onEditProfile = { showOrderFlow = false; onNavigateToProfile() }, onComplete = { price, ref -> viewModel.handlePurchaseComplete(context, quantity, price, ref) { showOrderFlow = false; scope.launch { snackbarHostState.showSnackbar(it) } } })
        }
        if (showPickupPopup) { PickupInfoDialog(orderConfirmation, onDismiss = { showPickupPopup = false }) }
        AppPopups.AddToLibraryConfirmation(
            show = showAddConfirm, 
            itemTitle = gear?.title ?: "", 
            category = AppConstants.CAT_GEAR, 
            onDismiss = { showAddConfirm = false }, 
            onConfirm = { 
                showAddConfirm = false; 
                isProcessingAddition = true; 
                scope.launch { 
                    delay(2000); 
                    viewModel.handleFreePickup(context) { 
                        isProcessingAddition = false; 
                        scope.launch { snackbarHostState.showSnackbar(it) } 
                    } 
                } 
            }
        )
        AppPopups.AddingToLibraryLoading(isProcessingAddition, AppConstants.CAT_GEAR)
    }
}
