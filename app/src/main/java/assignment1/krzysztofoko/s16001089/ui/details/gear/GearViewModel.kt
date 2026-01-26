package assignment1.krzysztofoko.s16001089.ui.details.gear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun handleFreePickup(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            userDao.addPurchase(PurchaseItem(userId, gearId))
            gearDao.reduceStock(gearId, 1)
            refreshGear()
            onComplete("Success! Please pick up your item at Student Hub.")
        }
    }

    fun handlePurchaseComplete(qty: Int, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            gearDao.reduceStock(gearId, qty)
            refreshGear()
            onComplete("Order successful!")
        }
    }
}
