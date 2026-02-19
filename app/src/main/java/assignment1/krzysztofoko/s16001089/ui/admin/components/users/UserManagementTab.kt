package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.admin.components.catalog.CatalogDeleteDialog
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import java.util.*

/**
 * UserManagementTab.kt
 *
 * This component provides the primary administrative interface for managing the university's 
 * user directory. It allows administrators to browse, filter, create, and delete student, 
 * teacher, and staff accounts.
 *
 * UI Architecture:
 * - Reactive Filtering: Provides role-based tabs (Admins, Students, etc.) to quickly narrow down the directory.
 * - Adaptive Layout: Automatically switches between a 1-column list (mobile) and a 2-column grid (tablet).
 * - Modal Workflows: Manages state for complex operations like user creation and account removal.
 */

/**
 * Main management view for the user directory.
 *
 * @param viewModel The shared [AdminViewModel] providing access to the user database.
 * @param onNavigateToUserDetails Callback to navigate to a specific user's deep-dive details page.
 * @param showAddUserDialog External trigger (usually from TopAppBar) to open the creation dialog.
 * @param onAddUserDialogConsumed Callback to reset the external trigger after it has been handled.
 */
@Composable
fun UserManagementTab(
    viewModel: AdminViewModel, 
    onNavigateToUserDetails: (String) -> Unit,
    showAddUserDialog: Boolean = false,
    onAddUserDialogConsumed: () -> Unit = {}
) {
    // --- DATA OBSERVATION ---
    // Subscribes to the live list of all users from the system database.
    val users by viewModel.allUsers.collectAsState()
    
    // --- UI STATE ---
    var isCreatingNew by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserLocal?>(null) }
    
    // Category mapping for the filter chips.
    val categories = listOf("All", "Admins", "Teachers", "Students", "Users")
    var selectedCategory by remember { mutableStateOf("All") }

    // --- EXTERNAL TRIGGER HANDLER ---
    // Responds to "Add" button clicks in the top navigation bar of the Admin Panel.
    LaunchedEffect(showAddUserDialog) {
        if (showAddUserDialog) {
            isCreatingNew = true
            onAddUserDialogConsumed()
        }
    }

    // --- REACTIVE FILTERING LOGIC ---
    // Dynamically refines the user list based on the selected role category.
    val filteredUsers = remember(users, selectedCategory) {
        when (selectedCategory) {
            "Admins" -> users.filter { it.role == "admin" }
            "Teachers" -> users.filter { it.role == "teacher" }
            "Students" -> users.filter { it.role == "student" }
            "Users" -> users.filter { it.role == "user" }
            else -> users
        }
    }

    // Adaptive container ensures optimal readability by capping width on ultra-wide screens.
    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        // Dynamically adjust grid density based on device form factor.
        val columns = if (isTablet) 2 else 1

        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- ROLE FILTER TABS ---
            // Horizontal scrollable row of chips for quick segment switching.
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            enabled = true,
                            selected = selectedCategory == category
                        )
                    )
                }
            }

            // --- DIRECTORY GRID ---
            // Displays user account summaries in high-impact adaptive cards.
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    AdminUserCard(
                        user = user, 
                        onClick = { onNavigateToUserDetails(user.id) }, 
                        onDelete = { userToDelete = user }
                    )
                }
            }
        }
    }

    // --- OVERLAY: CREATE NEW USER ---
    if (isCreatingNew) {
        UserEditDialog(
            // Provides a fresh template with a unique ID for the new account.
            user = UserLocal(id = UUID.randomUUID().toString(), name = "", email = "", role = "student"), 
            isNew = true, 
            onDismiss = { isCreatingNew = false }, 
            onSave = { newUser -> 
                viewModel.saveUser(newUser)
                isCreatingNew = false 
            }
        )
    }

    // --- OVERLAY: DELETE CONFIRMATION ---
    if (userToDelete != null) {
        CatalogDeleteDialog(
            itemName = "User Account: ${userToDelete!!.name}", 
            onDismiss = { userToDelete = null }, 
            onConfirm = { 
                // Permanently remove the user record and associated data.
                viewModel.deleteUser(userToDelete!!.id)
                userToDelete = null
            }
        )
    }
}
