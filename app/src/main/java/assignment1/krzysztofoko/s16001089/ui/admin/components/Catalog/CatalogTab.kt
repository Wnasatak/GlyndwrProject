package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

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
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import java.util.UUID

@Composable
fun CatalogTab(
    viewModel: AdminViewModel,
    isDarkTheme: Boolean,
    showAddProductDialog: Boolean,
    onAddProductDialogConsumed: () -> Unit
) {
    val books by viewModel.allBooks.collectAsState(emptyList())
    val audioBooks by viewModel.allAudioBooks.collectAsState(emptyList())
    val courses by viewModel.allCourses.collectAsState(emptyList())
    val gear by viewModel.allGear.collectAsState(emptyList())

    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf(
        CatalogFilter("ALL", "All", Icons.Default.GridView),
        CatalogFilter("BOOKS", "Books", Icons.AutoMirrored.Filled.MenuBook),
        CatalogFilter("AUDIO", "Audio", Icons.Default.Headphones),
        CatalogFilter("COURSES", "Courses", Icons.Default.School),
        CatalogFilter("GEAR", "Gear", Icons.Default.Checkroom)
    )

    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var audioToEdit by remember { mutableStateOf<AudioBook?>(null) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var gearToEdit by remember { mutableStateOf<Gear?>(null) }
    
    val infiniteCount = Int.MAX_VALUE
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % filters.size)
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = startPosition)

    AdaptiveScreenContainer(maxWidth = AdaptiveWidths.Wide) { isTablet ->
        val columns = if (isTablet) 2 else 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(this.maxLineSpan) }) {
                HeaderSection(
                    title = "Global Catalog",
                    subtitle = "Manage all products and inventory.",
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            item(span = { GridItemSpan(this.maxLineSpan) }) {
                LazyRow(
                    state = tabListState,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(infiniteCount) { index ->
                        val filter = filters[index % filters.size]
                        val isSelected = selectedFilter == filter.id
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { selectedFilter = filter.id }
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
                }
            }

            if (selectedFilter == "ALL" || selectedFilter == "BOOKS") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("Academic Books", books.size) }
                items(books) { item ->
                    CatalogItemCard(title = item.title, subtitle = "By ${item.author}", price = item.price, imageUrl = item.imageUrl, icon = Icons.AutoMirrored.Filled.MenuBook, onEdit = { bookToEdit = item }, onDelete = { itemToDelete = item.id to "Book" }, isDarkTheme = isDarkTheme)
                }
            }

            if (selectedFilter == "ALL" || selectedFilter == "AUDIO") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("Audio Learning", audioBooks.size) }
                items(audioBooks) { item ->
                    CatalogItemCard(title = item.title, subtitle = "By ${item.author}", price = item.price, imageUrl = item.imageUrl, icon = Icons.Default.Headphones, onEdit = { audioToEdit = item }, onDelete = { itemToDelete = item.id to "Audiobook" }, isDarkTheme = isDarkTheme)
                }
            }

            if (selectedFilter == "ALL" || selectedFilter == "COURSES") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("University Courses", courses.size) }
                items(courses) { item ->
                    CatalogItemCard(title = item.title, subtitle = item.department, price = item.price, imageUrl = item.imageUrl, icon = Icons.Default.School, onEdit = { courseToEdit = item }, onDelete = { itemToDelete = item.id to "Course" }, isDarkTheme = isDarkTheme)
                }
            }

            if (selectedFilter == "ALL" || selectedFilter == "GEAR") {
                item(span = { GridItemSpan(this.maxLineSpan) }) { CatalogSectionHeader("Official Gear", gear.size) }
                items(gear) { item ->
                    CatalogItemCard(title = item.title, subtitle = "Stock: ${item.stockCount} left", price = item.price, imageUrl = item.imageUrl, icon = Icons.Default.Checkroom, onEdit = { gearToEdit = item }, onDelete = { itemToDelete = item.id to "Gear Item" }, isDarkTheme = isDarkTheme)
                }
            }

            item(span = { GridItemSpan(this.maxLineSpan) }) { Spacer(Modifier.height(80.dp)) }
        }
    }

    // --- Overlays ---
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { onAddProductDialogConsumed() },
            title = { Text("Select Product Category", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            dismissButton = { TextButton(onClick = { onAddProductDialogConsumed() }) { Text("Cancel") } }
        )
    }

    if (itemToDelete != null) {
        val (id, cat) = itemToDelete!!
        CatalogDeleteDialog(itemName = cat, onDismiss = { itemToDelete = null }, onConfirm = {
            when(cat) {
                "Book" -> viewModel.deleteBook(id)
                "Audiobook" -> viewModel.deleteAudioBook(id)
                "Course" -> viewModel.deleteCourse(id)
                "Gear Item" -> viewModel.deleteGear(id)
            }
            itemToDelete = null
        })
    }

    if (bookToEdit != null) BookEditDialog(book = bookToEdit!!, onDismiss = { bookToEdit = null }, onSave = { viewModel.saveBook(it); bookToEdit = null })
    if (audioToEdit != null) AudioBookEditDialog(audioBook = audioToEdit!!, onDismiss = { audioToEdit = null }, onSave = { viewModel.saveAudioBook(it); audioToEdit = null })
    if (courseToEdit != null) CourseEditDialog(course = courseToEdit!!, onDismiss = { courseToEdit = null }, onSave = { viewModel.saveCourse(it); courseToEdit = null })
    if (gearToEdit != null) GearEditDialog(gear = gearToEdit!!, onDismiss = { gearToEdit = null }, onSave = { viewModel.saveGear(it); gearToEdit = null })
}

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

@Composable
fun AddCategoryButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.Bold)
    }
}

data class CatalogFilter(val id: String, val title: String, val icon: ImageVector)
