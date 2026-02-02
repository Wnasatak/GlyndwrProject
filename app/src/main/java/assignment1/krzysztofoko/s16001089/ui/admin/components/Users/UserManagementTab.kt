package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import java.util.*

@Composable
fun UserManagementTab(viewModel: AdminViewModel, onNavigateToUserDetails: (String) -> Unit) {
    val users by viewModel.allUsers.collectAsState()
    var isCreatingNew by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserLocal?>(null) }
    
    val categories = listOf("All", "Admins", "Teachers", "Students", "Users")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredUsers = remember(users, selectedCategory) {
        when (selectedCategory) {
            "Admins" -> users.filter { it.role == "admin" }
            "Teachers" -> users.filter { it.role == "teacher" }
            "Students" -> users.filter { it.role == "student" }
            "Users" -> users.filter { it.role == "user" }
            else -> users
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
            items(filteredUsers) { user ->
                AdminUserCard(user = user, onClick = { onNavigateToUserDetails(user.id) }, onDelete = { userToDelete = user })
            }
        }
    }

    if (isCreatingNew) {
        UserEditDialog(user = UserLocal(id = UUID.randomUUID().toString(), name = "", email = "", role = "student"), isNew = true, onDismiss = { isCreatingNew = false }, onSave = { newUser -> viewModel.saveUser(newUser); isCreatingNew = false })
    }

    if (userToDelete != null) {
        DeleteUserConfirmationDialog(userName = userToDelete!!.name, onDismiss = { userToDelete = null }, onConfirm = { viewModel.deleteUser(userToDelete!!.id); userToDelete = null })
    }
}
