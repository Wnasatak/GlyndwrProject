package assignment1.krzysztofoko.s16001089.ui.details.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseDao: CourseDao,
    private val userDao: UserDao,
    private val courseId: String,
    private val userId: String
) : ViewModel() {

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val localUser: StateFlow<UserLocal?> = if (userId.isNotEmpty()) {
        userDao.getUserFlow(userId)
    } else {
        flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOwned: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getPurchaseIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val inWishlist: StateFlow<Boolean> = if (userId.isNotEmpty()) {
        userDao.getWishlistIds(userId).map { it.contains(courseId) }
    } else {
        flowOf(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allReviews: StateFlow<List<ReviewLocal>> = userDao.getReviewsForProduct(courseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCourse()
    }

    private fun loadCourse() {
        viewModelScope.launch {
            _loading.value = true
            // Attempt to load from courseDao first
            val fetchedCourse = courseDao.getCourseById(courseId)
            _course.value = fetchedCourse
            
            if (userId.isNotEmpty() && fetchedCourse != null) {
                userDao.addToHistory(HistoryItem(userId, courseId))
            }
            _loading.value = false
        }
    }

    fun toggleWishlist(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            if (inWishlist.value) {
                userDao.removeFromWishlist(userId, courseId)
                onComplete("Removed from favorites")
            } else {
                userDao.addToHistory(HistoryItem(userId, courseId))
                userDao.addToWishlist(WishlistItem(userId, courseId))
                onComplete("Added to favorites!")
            }
        }
    }

    fun addFreePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.addPurchase(PurchaseItem(userId, courseId))
            onComplete("Successfully enrolled in the course!")
        }
    }

    fun removePurchase(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (userId.isEmpty()) return@launch
            userDao.deletePurchase(userId, courseId)
            onComplete("Course removed from your dashboard")
        }
    }
}
