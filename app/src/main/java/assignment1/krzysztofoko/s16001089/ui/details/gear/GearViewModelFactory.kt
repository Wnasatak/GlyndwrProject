package assignment1.krzysztofoko.s16001089.ui.details.gear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.GearDao
import assignment1.krzysztofoko.s16001089.data.UserDao

class GearViewModelFactory(
    private val gearDao: GearDao,
    private val userDao: UserDao,
    private val gearId: String,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GearViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GearViewModel(gearDao, userDao, gearId, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
