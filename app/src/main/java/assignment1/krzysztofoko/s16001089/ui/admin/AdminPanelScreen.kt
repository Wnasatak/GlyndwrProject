package assignment1.krzysztofoko.s16001089.ui.admin

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.admin.components.Apps.ApplicationsTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Apps.ApplicationDetailScreen
import assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog.CatalogTab
import assignment1.krzysztofoko.s16001089.ui.admin.components.Users.*
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import java.util.*

enum class AdminSection { APPLICATIONS, USERS, CATALOG }

/**
 * The Central Dashboard for Administrators.
 * Orchestrates the Applications, Users, and Catalog management modules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onNavigateToUserDetails: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(
        db = AppDatabase.getDatabase(LocalContext.current)
    ))
) {
    var currentSection by remember { mutableStateOf(AdminSection.APPLICATIONS) }
    var selectedAppForReview by remember { mutableStateOf<AdminApplicationItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (selectedAppForReview == null) {
                    CenterAlignedTopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        title = { Text(text = "Admin Hub", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge) },
                        actions = { IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) } },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                }
            },
            bottomBar = {
                if (selectedAppForReview == null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        AdminNavButton(selected = currentSection == AdminSection.APPLICATIONS, onClick = { currentSection = AdminSection.APPLICATIONS }, icon = Icons.AutoMirrored.Filled.Assignment, label = "Apps")
                        AdminNavButton(selected = currentSection == AdminSection.USERS, onClick = { currentSection = AdminSection.USERS }, icon = Icons.Default.People, label = "Users")
                        AdminNavButton(selected = currentSection == AdminSection.CATALOG, onClick = { currentSection = AdminSection.CATALOG }, icon = Icons.AutoMirrored.Filled.LibraryBooks, label = "Catalog")
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = currentSection,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "AdminSectionTransition"
                ) { section ->
                    when (section) {
                        AdminSection.APPLICATIONS -> ApplicationsTab(viewModel = viewModel, onReviewApp = { selectedAppForReview = it })
                        AdminSection.USERS -> UsersTab(viewModel, onNavigateToUserDetails)
                        AdminSection.CATALOG -> CatalogTab(viewModel, isDarkTheme)
                    }
                }
            }
        }

        if (selectedAppForReview != null) {
            ApplicationDetailScreen(
                app = selectedAppForReview!!,
                onBack = { selectedAppForReview = null },
                onApprove = { viewModel.approveApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null },
                onReject = { viewModel.rejectApplication(selectedAppForReview!!.details.id, selectedAppForReview!!.details.userId, selectedAppForReview!!.course?.title ?: "Course"); selectedAppForReview = null },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
fun RowScope.AdminNavButton(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}

@Composable
fun UsersTab(viewModel: AdminViewModel, onNavigateToUserDetails: (String) -> Unit) {
    val users by viewModel.allUsers.collectAsState()
    var isCreatingNew by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserLocal?>(null) }
    
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Button(
                onClick = { isCreatingNew = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.PersonAdd, null); Spacer(Modifier.width(12.dp)); Text("Create New Account", fontWeight = FontWeight.Black)
            }
        }
        items(users) { user ->
            AdminUserCard(user = user, onClick = { onNavigateToUserDetails(user.id) }, onDelete = { userToDelete = user })
        }
    }

    if (isCreatingNew) {
        UserEditDialog(user = UserLocal(id = UUID.randomUUID().toString(), name = "", email = "", role = "student"), isNew = true, onDismiss = { isCreatingNew = false }, onSave = { newUser -> viewModel.saveUser(newUser); isCreatingNew = false })
    }

    if (userToDelete != null) {
        DeleteUserConfirmationDialog(userName = userToDelete!!.name, onDismiss = { userToDelete = null }, onConfirm = { viewModel.deleteUser(userToDelete!!.id); userToDelete = null })
    }
}
