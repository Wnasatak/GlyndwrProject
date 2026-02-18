package assignment1.krzysztofoko.s16001089.ui.admin.components.catalog

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.components.*
import java.util.UUID

/**
 * CatalogTab.kt
 *
 * This component implements the primary inventory management hub for administrators.
 * it allows for comprehensive CRUD operations across various product categories including
 * academic books, audio learning, university courses, and official merchandise.
 *
 * FOLDER ANALYSIS & INTEGRATION:
 * 1. SHARED UI (CatalogShared.kt): Utilizes common UI elements like 'CatalogItemCard' for list consistency
 *    and 'CatalogDeleteDialog' to enforce a secure "type-to-confirm" deletion pattern.
 * 2. MODULAR EDITORS: Integrates category-specific dialogs (BookEditDialog, AudioBookEditDialog, 
 *    CourseEditDialog, GearEditDialog) for creating and updating inventory.
 * 3. REACTIVE DATA: Bridges the AdminViewModel's data streams (Books, Gear, etc.) to the UI.
 * 4. ADAPTIVE LAYOUT: Automatically switches between mobile-first carousel filters and tablet-optimized 
 *    grid systems.
 */

/**
 * CatalogTab Composable
 *
 * The main administrative dashboard for product and inventory control.
 *
 * @param viewModel The primary state holder for administrative data and operations.
 * @param isDarkTheme Flag to adjust visual elements for dark or light modes.
 * @param showAddProductDialog Trigger state for the 'Add New' selection menu.
 * @param onAddProductDialogConsumed Callback to reset the dialog visibility state.
 * @param onNavigateToDetails Navigation trigger to view high-level item specifics.
 */
@Composable
fun CatalogTab(
    viewModel: AdminViewModel,
    isDarkTheme: Boolean,
    showAddProductDialog: Boolean,
    onAddProductDialogConsumed: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    // --- DATA COLLECTION --- //
    // Observe live streams of different inventory categories from the database.
    val books by viewModel.allBooks.collectAsState(emptyList())
    val audioBooks by viewModel.allAudioBooks.collectAsState(emptyList())
    val courses by viewModel.allCourses.collectAsState(emptyList())
    val gear by viewModel.allGear.collectAsState(emptyList())

    // --- FILTERING LOGIC --- //
    // State for the active category filter (All, Books, Audio, etc.).
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf(
        CatalogFilter("ALL", "All", Icons.Default.GridView),
        CatalogFilter("BOOKS", "Books", Icons.AutoMirrored.Filled.MenuBook),
        CatalogFilter("AUDIO", "Audio", Icons.Default.Headphones),
        CatalogFilter("COURSES", "Courses", Icons.Default.School),
        CatalogFilter("GEAR", "Gear", Icons.Default.Checkroom)
    )

    // --- OVERLAY & EDIT STATE --- //
    // Tracks which specific item is being targeted for deletion or modification.
    // These states are passed to the specialized *EditDialogs found in this folder.
    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var audioToEdit by remember { mutableStateOf<AudioBook?>(null) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var gearToEdit by remember { mutableStateOf<Gear?>(null) }
    
    // --- RESPONSIVE CONFIGURATION --- //
    val isTablet = isTablet()
    val infiniteCount = Int.MAX_VALUE // For the infinite carousel effect on mobile filter bars.
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % filters.size)
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = if (isTablet) 0 else startPosition)

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { screenIsTablet ->
        val columns = if (screenIsTablet) 2 else 1 // Multi-column layout for tablets.
        val horizontalPadding = 16.dp 

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION: HEADER --- //
            item(span = { GridItemSpan(this.maxLineSpan) }) {
                HeaderSection(
                    title = "Global Catalog",
                    subtitle = "Manage all products and inventory.",
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 12.dp)
                )
            }

            // --- SECTION: CATEGORY FILTER BAR --- //
            item(span = { GridItemSpan(this.maxLineSpan) }) {
                LazyRow(
                    state = tabListState,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = if (screenIsTablet) Arrangement.Center else Arrangement.spacedBy(8.dp)
                ) {
                    if (screenIsTablet) {
                        // Standard layout for wide screens.
                        items(filters) { filter ->
                            CatalogFilterItem(
                                filter = filter,
                                isSelected = selectedFilter == filter.id,
                                onClick = { selectedFilter = filter.id }
                            )
                        }
                    } else {
                        // Infinite scrolling carousel for smaller mobile screens.
                        items(infiniteCount) { index ->
                            val filter = filters[index % filters.size]
                            CatalogFilterItem(
                                filter = filter,
                                isSelected = selectedFilter == filter.id,
                                onClick = { selectedFilter = filter.id }
                            )
                        }
                    }
                }
            }

            // --- SECTION: BOOKS --- //
            // Renders items using 'CatalogItemCard' from CatalogShared.kt
            if (selectedFilter == "ALL" || selectedFilter == "BOOKS") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("Academic Books", books.size, modifier = Modifier.padding(horizontal = horizontalPadding)) }
                items(books) { item ->
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        CatalogItemCard(
                            title = item.title, 
                            subtitle = "By ${item.author}", 
                            price = item.price, 
                            imageUrl = item.imageUrl, 
                            icon = Icons.AutoMirrored.Filled.MenuBook, 
                            onEdit = { bookToEdit = item }, 
                            onDelete = { itemToDelete = item.id to "Book" }, 
                            isDarkTheme = isDarkTheme,
                            onClick = { onNavigateToDetails(item.id) }
                        )
                    }
                }
            }

            // --- SECTION: AUDIOBOOKS --- //
            if (selectedFilter == "ALL" || selectedFilter == "AUDIO") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("Audio Learning", audioBooks.size, modifier = Modifier.padding(horizontal = horizontalPadding)) }
                items(audioBooks) { item ->
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        CatalogItemCard(
                            title = item.title, 
                            subtitle = "By ${item.author}", 
                            price = item.price, 
                            imageUrl = item.imageUrl, 
                            icon = Icons.Default.Headphones, 
                            onEdit = { audioToEdit = item }, 
                            onDelete = { itemToDelete = item.id to "Audiobook" }, 
                            isDarkTheme = isDarkTheme,
                            onClick = { onNavigateToDetails(item.id) }
                        )
                    }
                }
            }

            // --- SECTION: COURSES --- //
            if (selectedFilter == "ALL" || selectedFilter == "COURSES") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("University Courses", courses.size, modifier = Modifier.padding(horizontal = horizontalPadding)) }
                items(courses) { item ->
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        CatalogItemCard(
                            title = item.title, 
                            subtitle = item.department, 
                            price = item.price, 
                            imageUrl = item.imageUrl, 
                            icon = Icons.Default.School, 
                            onEdit = { courseToEdit = item }, 
                            onDelete = { itemToDelete = item.id to "Course" }, 
                            isDarkTheme = isDarkTheme,
                            onClick = { onNavigateToDetails(item.id) }
                        )
                    }
                }
            }

            // --- SECTION: GEAR --- //
            if (selectedFilter == "ALL" || selectedFilter == "GEAR") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("Official Gear", gear.size, modifier = Modifier.padding(horizontal = horizontalPadding)) }
                items(gear) { item ->
                    Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                        CatalogItemCard(
                            title = item.title, 
                            subtitle = "Stock: ${item.stockCount} left", 
                            price = item.price, 
                            imageUrl = item.imageUrl, 
                            icon = Icons.Default.Checkroom, 
                            onEdit = { gearToEdit = item }, 
                            onDelete = { itemToDelete = item.id to "Gear Item" }, 
                            isDarkTheme = isDarkTheme,
                            onClick = { onNavigateToDetails(item.id) }
                        )
                    }
                }
            }

            // Gutter spacing at the end of the grid.
            item(span = { GridItemSpan(this.maxLineSpan) }) { Spacer(Modifier.height(80.dp)) }
        }
    }

    // --- OVERLAYS: Global Add Product Dialog --- //
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { onAddProductDialogConsumed() },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Add New Product", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    @Suppress("DEPRECATION")
                    Text("Select a category to begin adding to the inventory.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    // Logic for initialising new template objects for each category.
                    // Clicking these initiates a specific EditDialog flow.
                    AddCategoryButton("Academic Book", Icons.AutoMirrored.Filled.MenuBook) {
                        bookToEdit = Book(id = UUID.randomUUID().toString(), title = "", author = "", mainCategory = AppConstants.CAT_BOOKS)
                        onAddProductDialogConsumed()
                    }
                    AddCategoryButton("Audiobook", Icons.Default.Headphones) {
                        audioToEdit = AudioBook(id = UUID.randomUUID().toString(), title = "", author = "", mainCategory = AppConstants.CAT_AUDIOBOOKS)
                        onAddProductDialogConsumed()
                    }
                    AddCategoryButton("University Course", Icons.Default.School) {
                        courseToEdit = Course(id = UUID.randomUUID().toString(), title = "", department = "", mainCategory = AppConstants.CAT_COURSES)
                        onAddProductDialogConsumed()
                    }
                    AddCategoryButton("Official Gear", Icons.Default.Checkroom) {
                        gearToEdit = Gear(id = UUID.randomUUID().toString(), title = "", brand = "Wrexham University", mainCategory = AppConstants.CAT_GEAR)
                        onAddProductDialogConsumed()
                    }
                }
            },
            confirmButton = {},
            dismissButton = { 
                TextButton(onClick = { onAddProductDialogConsumed() }) { 
                    Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) 
                } 
            }
        )
    }

    // --- OVERLAYS: Confirmation & Editing --- //
    
    // Destructive confirmation dialog (from CatalogShared.kt)
    if (itemToDelete != null) {
        val (id, cat) = itemToDelete!!
        CatalogDeleteDialog(itemName = cat, onDismiss = { itemToDelete = null }, onConfirm = {
            // Determine which repository deletion method to call based on the category.
            when(cat) {
                "Book" -> viewModel.deleteBook(id)
                "Audiobook" -> viewModel.deleteAudioBook(id)
                "Course" -> viewModel.deleteCourse(id)
                "Gear Item" -> viewModel.deleteGear(id)
            }
            itemToDelete = null
        })
    }

    // Integration of Specialized Editor Dialogs:
    // Each dialog handles its own complex form state and native file picking logic.
    if (bookToEdit != null) BookEditDialog(book = bookToEdit!!, onDismiss = { bookToEdit = null }, onSave = { viewModel.saveBook(it); bookToEdit = null })
    if (audioToEdit != null) AudioBookEditDialog(audioBook = audioToEdit!!, onDismiss = { audioToEdit = null }, onSave = { viewModel.saveAudioBook(it); audioToEdit = null })
    if (courseToEdit != null) CourseEditDialog(course = courseToEdit!!, onDismiss = { courseToEdit = null }, onSave = { viewModel.saveCourse(it); courseToEdit = null })
    if (gearToEdit != null) GearEditDialog(gear = gearToEdit!!, onDismiss = { gearToEdit = null }, onSave = { viewModel.saveGear(it); gearToEdit = null })
}

/**
 * CatalogFilterItem Composable
 *
 * An interactive filter pill for switching between product categories.
 */
@Composable
fun CatalogFilterItem(
    filter: CatalogFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = filter.icon, 
                contentDescription = null, 
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = filter.title,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * HeaderSection Composable
 *
 * Top-level branding and title section for the catalog tab.
 */
@Composable
private fun HeaderSection(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * AddCategoryButton Composable
 *
 * A stylized action item used inside the 'Add New' dialog to select a product type.
 */
@Composable
fun AddCategoryButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color.Gray.copy(alpha = 0.5f))
        }
    }
}

/**
 * Configuration data class for the catalog filter bar.
 */
data class CatalogFilter(val id: String, val title: String, val icon: ImageVector)
