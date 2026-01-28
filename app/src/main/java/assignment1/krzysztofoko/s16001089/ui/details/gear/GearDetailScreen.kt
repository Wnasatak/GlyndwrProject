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

/**
 * Detailed Information Screen for University Gear (Merchandise).
 * 
 * This screen provides a high-fidelity interface for browsing physical products.
 * It includes an image gallery, variant selection (Size/Color), live stock tracking, 
 * technical specifications, and a related products carousel. It handles both 
 * paid checkout flows and free item reservation workflows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearDetailScreen(
    navController: NavController,     // Global navigation controller
    gearId: String,                   // Unique identifier for the gear item
    initialGear: Gear? = null,        // Optional pre-fetched gear data
    user: FirebaseUser?,              // Current Firebase authentication session
    onLoginRequired: () -> Unit,      // Callback to prompt user authentication
    onBack: () -> Unit,               // Navigation return callback
    isDarkTheme: Boolean,             // Current app-wide theme state
    onToggleTheme: () -> Unit,        // Function to flip theme state
    onNavigateToProfile: () -> Unit,  // Link to user profile screen
    onViewInvoice: (String) -> Unit,  // Link to digital receipt viewer
    // ViewModel setup with factory injection for Gear-specific DAOs
    viewModel: GearViewModel = viewModel(factory = GearViewModelFactory(
        gearDao = AppDatabase.getDatabase(LocalContext.current).gearDao(),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        gearId = gearId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Observation of reactive UI states from the ViewModel
    val gear by viewModel.gear.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val similarGear by viewModel.similarGear.collectAsState()
    val selectedSize by viewModel.selectedSize.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val selectedImageIndex by viewModel.selectedImageIndex.collectAsState()
    
    val localUser by viewModel.localUser.collectAsState()
    val isOwned by viewModel.isOwned.collectAsState()
    val orderConfirmation by viewModel.orderConfirmation.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    // UI flags for dialog and overlay visibility
    var showPickupPopup by remember { mutableStateOf(false) }
    var showOrderFlow by remember { mutableStateOf(false) }
    var showAddConfirm by remember { mutableStateOf(false) }
    var isProcessingAddition by remember { mutableStateOf(false) }

    // Logic to build the image gallery from primary and secondary sources
    val images = remember(gear) { listOfNotNull(gear?.imageUrl, gear?.secondaryImageUrl) }

    Box(modifier = Modifier.fillMaxSize()) {
        // High-readability wavy background consistent across product details
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
                    navigationIcon = { 
                        IconButton(onClick = onBack) { 
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return") 
                        } 
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) { 
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) 
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { paddingValues ->
            // CONDITIONAL UI: Loading -> Not Found -> Product Content
            if (loading && gear == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (gear == null) {
                // Handle cases where the item ID cannot be resolved
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp)); Text(AppConstants.MSG_ITEM_NOT_FOUND)
                        TextButton(onClick = onBack) { Text(AppConstants.BTN_GO_BACK) }
                    }
                }
            } else {
                // Success: Display Product Gallery and Information
                gear?.let { currentGear ->
                    val isFree = currentGear.price <= 0
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(bottom = 120.dp)) {
                        
                        // Header Image with support for multiple gallery indices
                        item {
                            ProductHeaderImage(
                                book = currentGear.toBook(),
                                isOwned = isOwned,
                                isDarkTheme = isDarkTheme,
                                primaryColor = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Main Product Card: Handles detailed info and selections
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().offset(y = (-24).dp),
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    // Basic Title and Price section
                                    GearHeaderSection(gear = currentGear)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    // Dynamic attribute tags
                                    GearTagsSection(tags = currentGear.productTags)
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(24.dp))

                                    /**
                                     * SELECTORS: Size and Color
                                     * Updates ViewModel state and automatically flips gallery images
                                     * when specific colors (like Pink) are selected.
                                     */
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

                                    // Inventory counter and quantity picker
                                    GearStockIndicator(
                                        stockCount = currentGear.stockCount,
                                        quantity = quantity,
                                        isOwned = isOwned,
                                        isFree = isFree,
                                        onQuantityChange = { viewModel.setQuantity(it) }
                                    )

                                    // Detailed description body
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(text = AppConstants.SECTION_DESCRIPTION_GEAR, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = currentGear.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    
                                    // Technical spec summary (Material, SKU, Category)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    GearSpecsCard(material = currentGear.material, sku = currentGear.sku, category = currentGear.category)
                                    
                                    /**
                                     * CAROUSEL: Similar Products
                                     * Horizontal list of related merchandise filtered by category.
                                     */
                                    if (similarGear.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(40.dp))
                                        Text(text = AppConstants.TITLE_SIMILAR_PRODUCTS, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        UniversalProductSlider(
                                            products = similarGear.map { it.toBook() },
                                            onProductClick = { selectedBook ->
                                                navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${selectedBook.id}")
                                            }
                                        )
                                    }

                                    // Social Section: User community feedback
                                    Spacer(modifier = Modifier.height(40.dp))
                                    ReviewSection(
                                        productId = gearId,
                                        reviews = allReviews,
                                        localUser = localUser,
                                        isLoggedIn = user != null,
                                        db = AppDatabase.getDatabase(LocalContext.current),
                                        isDarkTheme = isDarkTheme,
                                        onReviewPosted = { scope.launch { snackbarHostState.showSnackbar(AppConstants.MSG_THANKS_REVIEW) } },
                                        onLoginClick = onLoginRequired
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * STICKY FOOTER: Interaction Bar
         * Automatically appears at the bottom. Handles logic for:
         * 1. Guest users (Login required)
         * 2. Owned items (View pickup/invoice)
         * 3. Unowned items (Checkout flow)
         */
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
                    onFreePickup = { showAddConfirm = true }
                )
            }
        }

        // --- OVERLAY FLOWS ---

        // Workflow for multi-stage checkout and stock validation
        if (showOrderFlow && gear != null) {
            AppPopups.OrderPurchase(
                show = showOrderFlow,
                book = gear!!.toBook(),
                user = localUser,
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

        // Informational popup regarding physical collection at Student Hub
        if (showPickupPopup) {
            PickupInfoDialog(
                orderConfirmation = orderConfirmation,
                onDismiss = { showPickupPopup = false }
            )
        }

        // Workflow for free merchandise reservation
        AppPopups.AddToLibraryConfirmation(
            show = showAddConfirm,
            itemTitle = gear?.title ?: "",
            category = AppConstants.CAT_GEAR,
            onDismiss = { showAddConfirm = false },
            onConfirm = {
                showAddConfirm = false
                isProcessingAddition = true
                scope.launch {
                    delay(2000) // Simulated processing for DB transaction
                    viewModel.handleFreePickup(context) { msg ->
                        isProcessingAddition = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            }
        )

        // Global loading spinner for database-heavy interactions
        AppPopups.AddingToLibraryLoading(
            show = isProcessingAddition,
            category = AppConstants.CAT_GEAR
        )
    }
}
