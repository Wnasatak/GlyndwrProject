package assignment1.krzysztofoko.s16001089.ui.details.gear

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * GearDetailScreen.kt
 *
 * This file contains the primary UI implementation for the University Merchandise (Gear) details screen.
 * It manages a comprehensive product view, allowing users to select variations like size and colour,
 * track stock levels, and coordinate both paid and free acquisition workflows.
 */

/**
 * GearDetailScreen Composable
 *
 * An immersive, data-driven screen that showcases university-branded physical products.
 *
 * Key features:
 * - **Variation Selection:** Integrates state-aware selectors for product sizes and colours.
 * - **Stock Management:** Displays real-time stock levels with colour-coded urgency indicators.
 * - **Dynamic Action Bar:** A specialised bottom bar that adapts based on ownership, stock, and price.
 * - **Responsive Design:** Ensures that product content is properly constrained and centred on tablets.
 * - **Acquisition Flow:** Orchestrates both the paid checkout wizard and free claim process.
 *
 * @param navController For navigating to similar products.
 * @param gearId Unique identifier for the merchandise item.
 * @param user The current Firebase user session.
 * @param onLoginRequired Callback to trigger auth flow for guest users.
 * @param onBack Callback for navigation.
 * @param currentTheme Active visual theme for background rendering.
 * @param onNavigateToProfile Callback to jump to profile settings.
 * @param onViewInvoice Callback to open the digital receipt.
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
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
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
    // Determine if the background should use dark mode variants.
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE || currentTheme == Theme.CUSTOM
    
    val isTablet = isTablet()

    // --- VIEWMODEL STATE OBSERVATION --- //
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

    // Dynamic price calculation based on user role and specific discounts.
    val effectiveDiscount = remember(localUser, roleDiscounts) {
        val uRole = localUser?.role ?: "user"
        val roleRate = roleDiscounts.find { it.role == uRole }?.discountPercent ?: 0.0
        val individualRate = localUser?.discountPercent ?: 0.0
        maxOf(roleRate, individualRate)
    }

    // --- UI INTERACTION FLAGS --- //
    var showPickupPopup by remember { mutableStateOf(false) }
    var showOrderFlow by remember { mutableStateOf(false) }
    var showAddConfirm by remember { mutableStateOf(false) }
    var isProcessingAddition by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Branded wavy background layer.
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent, // Let the background show.
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0), // Fix for unwanted top spacing.
                    title = { Text(gear?.title ?: "Details", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                )
            },
            bottomBar = {
                // Specialized action bar for merchandise.
                if (gear != null && !loading) {
                    val unitPrice = gear!!.price * ((100.0 - effectiveDiscount) / 100.0)
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)).padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.widthIn(max = AdaptiveWidths.Medium)) {
                            GearBottomActionBar(
                                isOwned = isOwned,
                                price = unitPrice * quantity, // Reactive price calculation.
                                stockCount = gear!!.stockCount,
                                quantity = quantity,
                                isLoggedIn = user != null,
                                onViewInvoice = { onViewInvoice(gear!!.id) },
                                onPickupInfo = { showPickupPopup = true }, // Show physical collection details.
                                onLoginRequired = onLoginRequired,
                                onCheckout = { showOrderFlow = true },
                                onFreePickup = { showAddConfirm = true }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            // --- MAIN CONTENT AREA --- //
            if (loading && gear == null) {
                // Initial data fetch loading state.
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (gear == null) {
                // Handle missing product case.
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Item not found") }
            } else {
                gear?.let { item ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight().widthIn(max = AdaptiveWidths.Medium),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Section 1: Immersive Header.
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    ProductHeaderImage(book = item.toBook(), isOwned = isOwned, isDarkTheme = isDarkTheme, primaryColor = MaterialTheme.colorScheme.primary)
                                }
                            }
                            // Section 2: Core Details and Selectors.
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().offset(y = (-24).dp), // Slight overlap for depth.
                                    shape = RoundedCornerShape(32.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        // Header Row: Title & Price.
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(Modifier.weight(1f)) {
                                                @Suppress("DEPRECATION")
                                                Text("Wrexham University", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                val discountPrice = item.price * ((100.0 - effectiveDiscount) / 100.0)
                                                if (item.price > 0) {
                                                    // Show discounted price if applicable.
                                                    if (effectiveDiscount > 0) {
                                                        @Suppress("DEPRECATION")
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
                                        
                                        // Discount Label.
                                        if (effectiveDiscount > 0 && item.price > 0) {
                                            Spacer(Modifier.height(8.dp))
                                            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp), border = BorderStroke(0.5.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))) {
                                                val roleLabel = localUser?.role?.uppercase() ?: "USER"
                                                @Suppress("DEPRECATION")
                                                Text(
                                                    text = "$roleLabel DISCOUNT (-${effectiveDiscount.toInt()}% )",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF2E7D32),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(16.dp))
                                        GearTagsSection(item.productTags) // Render tags carousel.
                                        Spacer(Modifier.height(24.dp))
                                        
                                        // Interactive selection for Size and Colour.
                                        GearOptionSelectors(
                                            sizes = item.sizes, selectedSize = selectedSize, onSizeSelected = { viewModel.setSelectedSize(it) },
                                            colors = item.colors, selectedColor = selectedColor, onColorSelected = { viewModel.setSelectedColor(it) }, onColorClick = {}
                                        )
                                        
                                        // Display stock levels and quantity selector.
                                        GearStockIndicator(item.stockCount, quantity, isOwned, item.price <= 0) { viewModel.setQuantity(it) }
                                        
                                        Spacer(Modifier.height(32.dp))
                                        Text("Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(item.description, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                                        
                                        Spacer(Modifier.height(32.dp))
                                        GearSpecsCard(item.material, item.sku, item.category) // Technical specs card.
                                        
                                        // Cross-selling slider.
                                        if (similarGear.isNotEmpty()) {
                                            Spacer(Modifier.height(40.dp))
                                            Text("Similar Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            UniversalProductSlider(similarGear.map { it.toBook() }) { navController.navigate("${AppConstants.ROUTE_BOOK_DETAILS}/${it.id}") }
                                        }
                                        
                                        Spacer(Modifier.height(40.dp))
                                        // Social Feedback Section.
                                        ReviewSection(productId = gearId, reviews = allReviews, localUser = localUser, isLoggedIn = user != null, db = AppDatabase.getDatabase(context), isDarkTheme = isDarkTheme, onReviewPosted = {}, onLoginClick = onLoginRequired)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECONDARY DIALOGS AND OVERLAYS --- //

        // Workflow for selecting payment plans and confirming purchase.
        if (showOrderFlow && gear != null) {
            AppPopups.OrderPurchase(show = showOrderFlow, book = gear!!.toBook(), user = localUser, roleDiscounts = roleDiscounts, onDismiss = { showOrderFlow = false }, onEditProfile = { showOrderFlow = false; onNavigateToProfile() }, onComplete = { price, ref -> viewModel.handlePurchaseComplete(context, quantity, price, ref) { showOrderFlow = false; scope.launch { snackbarHostState.showSnackbar(it) } } })
        }
        
        // Show physical collection details.
        if (showPickupPopup) { PickupInfoDialog(orderConfirmation, onDismiss = { showPickupPopup = false }) }
        
        // Workflow for claiming zero-cost merchandise.
        AppPopups.AddToLibraryConfirmation(
            show = showAddConfirm, 
            itemTitle = gear?.title ?: "", 
            category = AppConstants.CAT_GEAR, 
            onDismiss = { showAddConfirm = false }, 
            onConfirm = { 
                showAddConfirm = false; 
                isProcessingAddition = true; 
                scope.launch { 
                    delay(2000); // Simulated verification latency.
                    viewModel.handleFreePickup(context) { 
                        isProcessingAddition = false; 
                        scope.launch { snackbarHostState.showSnackbar(it) } 
                    } 
                } 
            }
        )
        
        // Branded verification dialog.
        AppPopups.AddingToLibraryLoading(isProcessingAddition, AppConstants.CAT_GEAR)
    }
}
