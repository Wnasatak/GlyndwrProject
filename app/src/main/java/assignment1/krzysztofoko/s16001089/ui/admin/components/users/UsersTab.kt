package assignment1.krzysztofoko.s16001089.ui.admin.components.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveContent
import java.util.*

/**
 * UsersTab.kt
 *
 * This tab provides administrators with an overview of all system users.
 * It allows for the management of user accounts, including creation, deletion,
 * and navigation to detailed user profiles.
 */

/**
 * Renders the main User Management tab.
 *
 * @param viewModel The shared [AdminViewModel] for state management and user operations.
 * @param onNavigateToUserDetails Callback to navigate to a specific user's detail screen.
 */
@Composable
fun UsersTab(viewModel: AdminViewModel, onNavigateToUserDetails: (String) -> Unit) {
    // --- DATA OBSERVATION ---
    // Reactively observe the list of all system users.
    val users by viewModel.allUsers.collectAsState()
    
    // --- UI STATE MANAGEMENT ---
    // Tracks the visibility of the creation dialog.
    var isCreatingNew by remember { mutableStateOf(false) }
    // Tracks the user currently targeted for deletion.
    var userToDelete by remember { mutableStateOf<UserLocal?>(null) }

    AdaptiveContent {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // --- HEADER ACTION ---
            // Prominent button to initiate the creation of a new user account.
            item {
                Button(
                    onClick = { isCreatingNew = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Create New Account", fontWeight = FontWeight.Black)
                }
            }
            
            // --- USER LIST ---
            // Efficiently renders individual user cards for every system account.
            items(users) { user ->
                AdminUserCard(
                    user = user, 
                    onClick = { onNavigateToUserDetails(user.id) }, 
                    onDelete = { userToDelete = user }
                )
            }
        }
    }

    // --- OVERLAY: USER CREATION DIALOG ---
    // Opens when isCreatingNew is true, providing a form for new account data.
    if (isCreatingNew) {
        UserEditDialog(
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
    // Opens when userToDelete is not null, requesting verification before permanent removal.
    if (userToDelete != null) {
        DeleteUserConfirmationDialog(
            userName = userToDelete!!.name, 
            onDismiss = { userToDelete = null }, 
            onConfirm = { 
                viewModel.deleteUser(userToDelete!!.id)
                userToDelete = null 
            }
        )
    }
}
