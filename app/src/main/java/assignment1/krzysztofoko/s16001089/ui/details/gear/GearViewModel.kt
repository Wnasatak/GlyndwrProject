package assignment1.krzysztofoko.s16001089.ui.details.gear

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for the Gear Details screen.
 * 
 * Manages the state and logic for university merchandise (gear). This includes 
 * handling product variants (sizes, colors), managing inventory/stock counts, 
 * processing reservations for free items, and handling paid purchases with 
 * detailed email confirmations.
 */
class GearViewModel(
    private val gearDao: GearDao,           // DAO for accessing core gear product data
    private val userDao: UserDao,           // DAO for user session data (History, Purchases, Notifications)
    private val gearId: String,             // Unique identifier for the specific gear item
    private val userId: String              // Identifier for the current student session
) : ViewModel() {

    // --- Core Data Streams ---

    // Holds the resolved gear metadata from the local database
    private val _gear = MutableStateFlow<Gear?>(null)
    val gear: StateFlow<Gear?> = _gear.asStateFlow()

    // Flag to track the initial data loading state for the UI spinner
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // A randomized list of related items from the same category
    private val _similarGear = MutableStateFlow<List<Gear>>(emptyList())
    val similarGear: StateFlow<List<Gear>> = _similarGear.asStateFlow()

    // --- User Variant Selection States ---
    private val _selectedSize = MutableStateFlow("")
    val selectedSize: StateFlow<String> = _selectedSize.asStateFlow()

    private val _selectedColor = MutableStateFlow("")
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    // Tracks which image (primary vs secondary) is currently displayed in the gallery
    private val _selectedImageIndex = MutableStateFlow(0)
    val selectedImageIndex: StateFlow<Int> = _selectedImageIndex.asStateFlow()

    /**
     * Local User Profile Flow:
     * Provides real-time profile data (balance, email) used for purchase validations.
     */
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Ownership Check:
     * Reactively determines if the user has already reserved or bought this item.
     */
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(gearId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Order Reference Flow:
     * Resolves the official confirmation code if the item is already owned.
     */
    val orderConfirmation: StateFlow<String?> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { ids ->
            if (ids.contains(gearId)) {
                userDao.getPurchaseRecord(userId, gearId)?.orderConfirmation
            } else null
        }
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Social Data:
     * Collects all user reviews for this specific piece of gear.
     */
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(gearId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize the screen by fetching metadata and updating history
        loadGear()
    }

    /**
     * Data Resolution Logic:
     * 1. Fetches gear details and similar items.
     * 2. Sets initial variant defaults (Size/Color).
     * 3. Logs the visit in the student's local history.
     */
    private fun loadGear() {
        viewModelScope.launch {
            _loading.value = true
            val fetchedGear = gearDao.getGearById(gearId)
            if (fetchedGear != null) {
                _gear.value = fetchedGear
                // Default to first available size/color from comma-separated strings in DB
                _selectedSize.value = fetchedGear.sizes.split(",").firstOrNull() ?: "M"
                _selectedColor.value = fetchedGear.colors.split(",").firstOrNull() ?: AppConstants.TEXT_DEFAULT
                
                // Load similar products for the 'Similar Gear' carousel
                val allGear = gearDao.getAllGearOnce()
                _similarGear.value = allGear.filter { it.id != gearId && it.mainCategory == fetchedGear.mainCategory }.shuffled()
            }
            
            // Log interaction for personal recommendations
            if (userId.isNotEmpty()) {
                userDao.addToHistory(HistoryItem(userId, gearId))
            }
            _loading.value = false
        }
    }

    // --- State Updaters ---
    fun setSelectedSize(size: String) { _selectedSize.value = size }
    fun setSelectedColor(color: String) { _selectedColor.value = color }
    fun setQuantity(q: Int) { _quantity.value = q }
    fun setSelectedImageIndex(index: Int) { _selectedImageIndex.value = index }

    /**
     * Refetches the gear data from the DB to sync stock levels.
     */
    fun refreshGear() {
        viewModelScope.launch {
            val updated = gearDao.getGearById(gearId)
            if (updated != null) _gear.value = updated
        }
    }

    /**
     * Workflow for free item pick-up reservation.
     * 
     * Process:
     * 1. Creates a local purchase record.
     * 2. Triggers a "Ready for Pickup" system notification.
     * 3. Dispatches an item-specific HTML confirmation email with variant details.
     * 4. Deducts 1 item from local stock inventory.
     */
    fun handleFreePickup(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val orderConf = OrderUtils.generateOrderReference()
            val purchaseId = UUID.randomUUID().toString()
            val user = userDao.getUserById(userId)
            val currentGear = _gear.value ?: return@launch

            // Persist the reservation
            userDao.addPurchase(PurchaseItem(
                purchaseId = purchaseId,
                userId = userId, 
                productId = gearId, 
                mainCategory = currentGear.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = AppConstants.METHOD_FREE_PICKUP,
                amountFromWallet = 0.0,
                amountPaidExternal = 0.0,
                totalPricePaid = 0.0,
                quantity = 1,
                orderConfirmation = orderConf
            ))

            // Create notification alert
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = gearId,
                title = AppConstants.NOTIF_TITLE_GEAR_PICKUP,
                message = "Your ${currentGear.title} is ready for collection at Student Hub. Ref: $orderConf",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = AppConstants.NOTIF_TYPE_PICKUP
            ))

            // Dispatch external confirmation via SMTP
            if (user != null && user.email.isNotEmpty()) {
                val gearDetails = mapOf(
                    "Brand" to currentGear.brand,
                    "Size" to selectedSize.value,
                    "Color" to selectedColor.value,
                    "Material" to currentGear.material,
                    "SKU" to currentGear.sku
                )
                EmailUtils.sendPurchaseConfirmation(
                    context = context,
                    recipientEmail = user.email,
                    userName = user.name,
                    itemTitle = currentGear.title,
                    orderRef = orderConf,
                    price = AppConstants.LABEL_FREE,
                    category = currentGear.mainCategory,
                    details = gearDetails
                )
            }

            // Sync stock and UI
            gearDao.reduceStock(gearId, 1)
            refreshGear()
            onComplete("${AppConstants.MSG_GEAR_PICKUP_SUCCESS} Ref: $orderConf")
        }
    }

    /**
     * Finalizes a paid purchase of gear and dispatches detailed variant info via email.
     */
    fun handlePurchaseComplete(context: Context?, qty: Int, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserById(userId)
            val currentGear = _gear.value ?: return@launch

            // Dispatch external confirmation via SMTP
            if (user != null && user.email.isNotEmpty()) {
                val priceStr = "£" + String.format(Locale.US, "%.2f", finalPrice)
                val gearDetails = mapOf(
                    "Brand" to currentGear.brand,
                    "Quantity" to qty.toString(),
                    "Size" to selectedSize.value,
                    "Color" to selectedColor.value,
                    "Material" to currentGear.material,
                    "SKU" to currentGear.sku
                )
                EmailUtils.sendPurchaseConfirmation(
                    context = context,
                    recipientEmail = user.email,
                    userName = user.name,
                    itemTitle = currentGear.title,
                    orderRef = orderRef,
                    price = priceStr,
                    category = currentGear.mainCategory,
                    details = gearDetails
                )
            }

            // Update local stock based on purchased quantity
            gearDao.reduceStock(gearId, qty)
            refreshGear()
            onComplete(AppConstants.MSG_ORDER_SUCCESS)
        }
    }
}
