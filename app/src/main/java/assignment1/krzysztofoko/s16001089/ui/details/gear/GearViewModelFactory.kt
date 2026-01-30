package assignment1.krzysztofoko.s16001089.ui.details.gear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import assignment1.krzysztofoko.s16001089.data.AuditDao
import assignment1.krzysztofoko.s16001089.data.GearDao
import assignment1.krzysztofoko.s16001089.data.UserDao

/**
 * Factory class for creating instances of [GearViewModel].
 * 
 * ViewModels with constructor parameters require a custom factory to be instantiated
 * by the Android Lifecycle system. This factory handles the injection of the Gear DAO,
 * User DAO, Audit DAO, and specific identifiers (gearId, userId) into the ViewModel.
 */
class GearViewModelFactory(
    private val gearDao: GearDao,   // Injected DAO for gear-specific data
    private val userDao: UserDao,   // Injected DAO for user-specific data
    private val auditDao: AuditDao, // Injected DAO for system logging
    private val gearId: String,     // Unique identifier for the gear item
    private val userId: String      // ID of the current authenticated user session
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the GearViewModel.
     * 
     * @param modelClass The type of ViewModel to instantiate.
     * @return A constructed instance of [GearViewModel] with all dependencies injected.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Validate that the requested ViewModel matches our target class
        if (modelClass.isAssignableFrom(GearViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Manually construct the ViewModel with the parameters provided to the factory
            return GearViewModel(gearDao, userDao, auditDao, gearId, userId) as T
        }
        // Exception thrown if the factory is used incorrectly for another ViewModel type
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
