package assignment1.krzysztofoko.s16001089.ui.details.gear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.utils.EmailUtils
import assignment1.krzysztofoko.s16001089.utils.OrderUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class GearViewModel(
    private val gearDao: GearDao,
    private val userDao: UserDao,
    private val gearId: String,
    private val userId: String
) : ViewModel() {

    private val _gear = MutableStateFlow<Gear?>(null)
    val gear: StateFlow<Gear?> = _gear.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _similarGear = MutableStateFlow<List<Gear>>(emptyList())
    val similarGear: StateFlow<List<Gear>> = _similarGear.asStateFlow()

    // UI States
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

    private fun loadGear() {
        viewModelScope.launch {
            _loading.value = true
            val fetchedGear = gearDao.getGearById(gearId)
            if (fetchedGear != null) {
                _gear.value = fetchedGear
                _selectedSize.value = fetchedGear.sizes.split(",").firstOrNull() ?: "M"
                _selectedColor.value = fetchedGear.colors.split(",").firstOrNull() ?: "Default"
                
                val allGear = gearDao.getAllGearOnce()
                _similarGear.value = allGear.filter { it.id != gearId && it.mainCategory == fetchedGear.mainCategory }.shuffled()
            }
            
            if (userId.isNotEmpty()) {
                userDao.addToHistory(HistoryItem(userId, gearId))
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

    private fun currentInvoiceTitle(gear: Gear): String {
        return gear.title + if (selectedSize.value.isNotEmpty() && selectedSize.value != "Default") " (${selectedSize.value})" else ""
    }

    fun handleFreePickup(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val orderConf = OrderUtils.generateOrderReference()
            val invoiceNum = OrderUtils.generateInvoiceNumber()
            val purchaseId = UUID.randomUUID().toString()
            val user = localUser.value
            val currentGear = _gear.value ?: return@launch
            val variant = "Size: ${selectedSize.value}, Color: ${selectedColor.value}"

            // 1. Create robust purchase record
            userDao.addPurchase(PurchaseItem(
                purchaseId = purchaseId,
                userId = userId, 
                productId = gearId, 
                mainCategory = currentGear.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = "Free Pickup",
                amountFromWallet = 0.0,
                amountPaidExternal = 0.0,
                totalPricePaid = 0.0,
                quantity = 1,
                orderConfirmation = orderConf
            ))

            // 2. Create official pickup invoice
            userDao.addInvoice(Invoice(
                invoiceNumber = invoiceNum,
                userId = userId,
                productId = gearId,
                itemTitle = currentInvoiceTitle(currentGear),
                itemCategory = currentGear.mainCategory,
                itemVariant = variant,
                pricePaid = 0.0,
                discountApplied = 0.0,
                quantity = 1,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = "Free Pickup",
                orderReference = orderConf,
                billingName = user?.name ?: "Guest Student",
                billingEmail = user?.email ?: "",
                billingAddress = user?.address
            ))

            // 3. Trigger notification
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = gearId,
                title = "Pick-up Ready",
                message = "Your ${currentGear.title} is ready for collection at Student Hub. Ref: $orderConf",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = "PICKUP"
            ))

            // 4. Send Email Confirmation
            if (user != null && user.email.isNotEmpty()) {
                viewModelScope.launch {
                    EmailUtils.sendPurchaseConfirmation(
                        context = null,
                        recipientEmail = user.email,
                        userName = user.name,
                        itemTitle = currentGear.title,
                        orderRef = orderConf,
                        price = "FREE"
                    )
                }
            }

            gearDao.reduceStock(gearId, 1)
            refreshGear()
            onComplete("Success! Please pick up your item at Student Hub. Ref: $orderConf")
        }
    }

    fun handlePurchaseComplete(qty: Int, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val orderConf = OrderUtils.generateOrderReference()
            val invoiceNum = OrderUtils.generateInvoiceNumber()
            val purchaseId = UUID.randomUUID().toString()
            val user = localUser.value
            val currentGear = _gear.value ?: return@launch
            val finalPrice = currentGear.price * qty * 0.9
            val variant = "Size: ${selectedSize.value}, Color: ${selectedColor.value}"

            // 1. Create robust purchase record
            userDao.addPurchase(PurchaseItem(
                purchaseId = purchaseId,
                userId = userId, 
                productId = gearId, 
                mainCategory = currentGear.mainCategory,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = "Student Balance",
                amountFromWallet = finalPrice,
                amountPaidExternal = 0.0,
                totalPricePaid = finalPrice,
                quantity = qty,
                orderConfirmation = orderConf
            ))

            // 2. Create official university invoice
            userDao.addInvoice(Invoice(
                invoiceNumber = invoiceNum,
                userId = userId,
                productId = gearId,
                itemTitle = currentInvoiceTitle(currentGear),
                itemCategory = currentGear.mainCategory,
                itemVariant = variant,
                pricePaid = finalPrice,
                discountApplied = (currentGear.price * qty) * 0.1,
                quantity = qty,
                purchasedAt = System.currentTimeMillis(),
                paymentMethod = "Student Balance",
                orderReference = orderConf,
                billingName = user?.name ?: "Guest Student",
                billingEmail = user?.email ?: "",
                billingAddress = user?.address
            ))

            // 3. Trigger notification
            userDao.addNotification(NotificationLocal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                productId = gearId,
                title = "Order Confirmed",
                message = "Your order for ${currentGear.title} was successful! Ref: $orderConf",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                type = "PURCHASE"
            ))

            // 4. Send Email Confirmation
            if (user != null && user.email.isNotEmpty()) {
                viewModelScope.launch {
                    val priceStr = "£" + String.format(Locale.US, "%.2f", finalPrice)
                    EmailUtils.sendPurchaseConfirmation(
                        context = null,
                        recipientEmail = user.email,
                        userName = user.name,
                        itemTitle = currentGear.title,
                        orderRef = orderConf,
                        price = priceStr
                    )
                }
            }

            gearDao.reduceStock(gearId, qty)
            refreshGear()
            onComplete("Order successful! Ref: $orderConf")
        }
    }
}
