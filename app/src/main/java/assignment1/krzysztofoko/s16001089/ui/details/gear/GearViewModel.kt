package assignment1.krzysztofoko.s16001089.ui.details.gear

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * GearViewModel.kt
 *
 * This ViewModel acts as the data orchestrator for the University Gear (merchandise) details screen.
 * It manages the complex state of physical products, including variation selection (size/colour), 
 * quantity tracking, and stock level synchronization. It also coordinates both paid checkouts 
 * and free pickup claims while maintaining a detailed audit log of user interactions.
 */
class GearViewModel(
    private val gearDao: GearDao,           
    private val userDao: UserDao,           
    private val auditDao: AuditDao,         
    private val gearId: String,             
    private val userId: String              
) : ViewModel() {

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    // --- UI STATE FLOWS --- //

    // Holds the primary gear metadata once loaded from the database.
    private val _gear = MutableStateFlow<Gear?>(null)
    val gear: StateFlow<Gear?> = _gear.asStateFlow()

    // Indicates if an asynchronous data fetch is currently in progress.
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // A list of other merchandise in the same category for the "Similar Products" section.
    private val _similarGear = MutableStateFlow<List<Gear>>(emptyList())
    val similarGear: StateFlow<List<Gear>> = _similarGear.asStateFlow()

    // Tracks the user's currently selected size variation.
    private val _selectedSize = MutableStateFlow("")
    val selectedSize: StateFlow<String> = _selectedSize.asStateFlow()

    // Tracks the user's currently selected colour variation.
    private val _selectedColor = MutableStateFlow("")
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()

    // Tracks the quantity of items the user intends to purchase.
    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    // Tracks the active image in the product's gallery.
    private val _selectedImageIndex = MutableStateFlow(0)
    val selectedImageIndex: StateFlow<Int> = _selectedImageIndex.asStateFlow()

    // Streams current user profile data (balance, role, etc.).
    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Collects available role-based discounts for live pricing updates.
    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactively checks if the user already owns this specific item.
    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(gearId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Fetches the order confirmation reference if the item has been purchased.
    val orderConfirmation: StateFlow<String?> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { ids ->
            if (ids.contains(gearId)) {
                userDao.getPurchaseRecord(userId, gearId)?.orderConfirmation
            } else null
        }
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Streams all user reviews for this product.
    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(gearId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadGear() // Kick off initial data loading.
    }

    /**
     * Internal helper to record user actions in the system audit log.
     */
    private fun addLog(action: String, details: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            auditDao.insertLog(SystemLog(
                userId = userId,
                userName = user?.displayName ?: "Student",
                action = action,
                targetId = gearId,
                details = details,
                logType = "USER"
            ))
        }
    }

    /**
     * Fetches the product details and initialises variation defaults.
     * Also records the visit in the user's history and cross-references similar items.
     */
    private fun loadGear() {
        viewModelScope.launch {
            _loading.value = true // Start loading.
            val fetchedGear = gearDao.getGearById(gearId) // Fetch from Room.
            if (fetchedGear != null) {
                _gear.value = fetchedGear
                // Set default selections based on available variations.
                _selectedSize.value = fetchedGear.sizes.split(",").firstOrNull() ?: "M"
                _selectedColor.value = fetchedGear.colors.split(",").firstOrNull() ?: AppConstants.TEXT_DEFAULT
                
                // Fetch similar products for cross-selling.
                val allGear = gearDao.getAllGearOnce()
                _similarGear.value = allGear.filter { it.id != gearId && it.mainCategory == fetchedGear.mainCategory }.shuffled()
                
                // If authenticated, record the view event.
                if (userId.isNotEmpty()) {
                    userDao.addToHistory(HistoryItem(userId, gearId))
                    addLog("VIEW_ITEM", "User viewed gear: ${fetchedGear.title}")
                }
            }
            _loading.value = false // Stop loading.
        }
    }

    // --- UI ACTION HANDLERS --- //

    fun setSelectedSize(size: String) { _selectedSize.value = size }
    fun setSelectedColor(color: String) { _selectedColor.value = color }
    fun setQuantity(q: Int) { _quantity.value = q }
    fun setSelectedImageIndex(index: Int) { _selectedImageIndex.value = index }

    /**
     * Re-fetches the product from the database to ensure stock levels are up-to-date in the UI.
     */
    fun refreshGear() {
        viewModelScope.launch {
            val updated = gearDao.getGearById(gearId)
            if (updated != null) _gear.value = updated
        }
    }

    /**
     * Handles the acquisition of zero-cost merchandise.
     * Records the transaction, reduces physical stock, and notifies the user of the collection reference.
     */
    fun handleFreePickup(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val orderConf = OrderUtils.generateOrderReference() // Generate unique pickup ref.
            val currentGear = _gear.value ?: return@launch

            // 1. Record the claim in the user's library.
            userDao.addPurchase(PurchaseItem(
                purchaseId = UUID.randomUUID().toString(),
                userId = userId, 
                productId = gearId, 
                mainCategory = currentGear.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = AppConstants.METHOD_FREE_PICKUP,
                quantity = 1,
                orderConfirmation = orderConf
            ))

            // 2. Alert the user with a collection notification.
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = gearId,
                title = AppConstants.NOTIF_TITLE_GEAR_PICKUP,
                message = "Your ${currentGear.title} is ready for collection. Ref: $orderConf",
                timestamp = System.currentTimeMillis(),
                type = AppConstants.NOTIF_TYPE_PICKUP
            ))

            // 3. Update physical stock levels and log the event.
            gearDao.reduceStock(gearId, 1)
            addLog("PURCHASE_FREE", "Free pickup: ${currentGear.title}")
            refreshGear() // Sync UI.
            onComplete("${AppConstants.MSG_GEAR_PICKUP_SUCCESS} Ref: $orderConf")
        }
    }

    /**
     * Finalises a paid transaction for physical goods.
     * Adjusts inventory levels and records the financial event in the audit log.
     */
    fun handlePurchaseComplete(context: Context?, qty: Int, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val currentGear = _gear.value ?: return@launch
            // 1. Reduce the stock by the purchased quantity.
            gearDao.reduceStock(gearId, qty)
            // 2. Audit log the financial transaction.
            addLog("PURCHASE_PAID", "Purchased $qty units of ${currentGear.title} for £${String.format("%.2f", finalPrice)}")
            refreshGear() // Sync UI.
            onComplete(AppConstants.MSG_ORDER_SUCCESS)
        }
    }
}
