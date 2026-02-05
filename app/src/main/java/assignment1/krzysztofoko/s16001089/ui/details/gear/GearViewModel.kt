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
 * ViewModel for the Gear Details screen.
 */
class GearViewModel(
    private val gearDao: GearDao,           
    private val userDao: UserDao,           
    private val auditDao: AuditDao,         
    private val gearId: String,             
    private val userId: String              
) : ViewModel() {

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val _gear = MutableStateFlow<Gear?>(null)
    val gear: StateFlow<Gear?> = _gear.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _similarGear = MutableStateFlow<List<Gear>>(emptyList())
    val similarGear: StateFlow<List<Gear>> = _similarGear.asStateFlow()

    private val _selectedSize = MutableStateFlow("")
    val selectedSize: StateFlow<String> = _selectedSize.asStateFlow()

    private val _selectedColor = MutableStateFlow("")
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _selectedImageIndex = MutableStateFlow(0)
    val selectedImageIndex: StateFlow<Int> = _selectedImageIndex.asStateFlow()

    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val roleDiscounts: StateFlow<List<RoleDiscount>> = userDao.getAllRoleDiscounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(gearId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val orderConfirmation: StateFlow<String?> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { ids ->
            if (ids.contains(gearId)) {
                userDao.getPurchaseRecord(userId, gearId)?.orderConfirmation
            } else null
        }
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(gearId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadGear()
    }

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

    private fun loadGear() {
        viewModelScope.launch {
            _loading.value = true
            val fetchedGear = gearDao.getGearById(gearId)
            if (fetchedGear != null) {
                _gear.value = fetchedGear
                _selectedSize.value = fetchedGear.sizes.split(",").firstOrNull() ?: "M"
                _selectedColor.value = fetchedGear.colors.split(",").firstOrNull() ?: AppConstants.TEXT_DEFAULT
                val allGear = gearDao.getAllGearOnce()
                _similarGear.value = allGear.filter { it.id != gearId && it.mainCategory == fetchedGear.mainCategory }.shuffled()
                
                if (userId.isNotEmpty()) {
                    userDao.addToHistory(HistoryItem(userId, gearId))
                    addLog("VIEW_ITEM", "User viewed gear: ${fetchedGear.title}")
                }
            }
            _loading.value = false
        }
    }

    fun setSelectedSize(size: String) { _selectedSize.value = size }
    fun setSelectedColor(color: String) { _selectedColor.value = color }
    fun setQuantity(q: Int) { _quantity.value = q }
    fun setSelectedImageIndex(index: Int) { _selectedImageIndex.value = index }

    fun refreshGear() {
        viewModelScope.launch {
            val updated = gearDao.getGearById(gearId)
            if (updated != null) _gear.value = updated
        }
    }

    fun handleFreePickup(context: Context?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val orderConf = OrderUtils.generateOrderReference()
            val currentGear = _gear.value ?: return@launch

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

            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = gearId,
                title = AppConstants.NOTIF_TITLE_GEAR_PICKUP,
                message = "Your ${currentGear.title} is ready for collection. Ref: $orderConf",
                timestamp = System.currentTimeMillis(),
                type = AppConstants.NOTIF_TYPE_PICKUP
            ))

            gearDao.reduceStock(gearId, 1)
            addLog("PURCHASE_FREE", "Free pickup: ${currentGear.title}")
            refreshGear()
            onComplete("${AppConstants.MSG_GEAR_PICKUP_SUCCESS} Ref: $orderConf")
        }
    }

    fun handlePurchaseComplete(context: Context?, qty: Int, finalPrice: Double, orderRef: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val currentGear = _gear.value ?: return@launch
            gearDao.reduceStock(gearId, qty)
            addLog("PURCHASE_PAID", "Purchased $qty units of ${currentGear.title} for £${String.format("%.2f", finalPrice)}")
            refreshGear()
            onComplete(AppConstants.MSG_ORDER_SUCCESS)
        }
    }
}
