package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel

@Composable
fun CatalogTab(
    viewModel: AdminViewModel,
    isDarkTheme: Boolean
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

    // Selection States for Dialogs
    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var audioToEdit by remember { mutableStateOf<AudioBook?>(null) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var gearToEdit by remember { mutableStateOf<Gear?>(null) }

    val infiniteCount = Int.MAX_VALUE
    val startPosition = infiniteCount / 2 - (infiniteCount / 2 % filters.size)
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = startPosition)

    Column(modifier = Modifier.fillMaxSize()) {
        // Looping Category Filter
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(
                    onClick = { /* TODO: Global Add Logic */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Add New Product", fontWeight = FontWeight.Black)
                }
            }

            // Filtered Content
            if (selectedFilter == "ALL" || selectedFilter == "BOOKS") {
                item { CatalogSectionHeader("Academic Books", books.size) }
                items(books) { item ->
                    CatalogItemCard(
                        title = item.title,
                        subtitle = "By ${item.author}",
                        price = item.price,
                        imageUrl = item.imageUrl,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        onEdit = { bookToEdit = item },
                        onDelete = { itemToDelete = item.id to "Book" },
                        isDarkTheme = isDarkTheme
                    )
                }
            }

            if (selectedFilter == "ALL" || selectedFilter == "AUDIO") {
                item { CatalogSectionHeader("Audio Learning", audioBooks.size) }
                items(audioBooks) { item ->
                    CatalogItemCard(
                        title = item.title,
                        subtitle = "Narrated by ${item.author}",
                        price = item.price,
                        imageUrl = item.imageUrl,
                        icon = Icons.Default.Headphones,
                        onEdit = { audioToEdit = item },
                        onDelete = { itemToDelete = item.id to "Audiobook" },
                        isDarkTheme = isDarkTheme
                    )
                }
            }

            if (selectedFilter == "ALL" || selectedFilter == "COURSES") {
                item { CatalogSectionHeader("University Courses", courses.size) }
                items(courses) { item ->
                    CatalogItemCard(
                        title = item.title,
                        subtitle = item.department,
                        price = item.price,
                        imageUrl = item.imageUrl,
                        icon = Icons.Default.School,
                        onEdit = { courseToEdit = item },
                        onDelete = { itemToDelete = item.id to "Course" },
                        isDarkTheme = isDarkTheme
                    )
                }
            }

            if (selectedFilter == "ALL" || selectedFilter == "GEAR") {
                item { CatalogSectionHeader("Official Gear", gear.size) }
                items(gear) { item ->
                    CatalogItemCard(
                        title = item.title,
                        subtitle = "Stock: ${item.stockCount} left",
                        price = item.price,
                        imageUrl = item.imageUrl,
                        icon = Icons.Default.Checkroom,
                        onEdit = { gearToEdit = item },
                        onDelete = { itemToDelete = item.id to "Gear Item" },
                        isDarkTheme = isDarkTheme
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // --- Deletion Dialog ---
    if (itemToDelete != null) {
        val (id, cat) = itemToDelete!!
        CatalogDeleteDialog(
            itemName = cat,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                when(cat) {
                    "Book" -> viewModel.deleteBook(id)
                    "Audiobook" -> viewModel.deleteAudioBook(id)
                    "Course" -> viewModel.deleteCourse(id)
                    "Gear Item" -> viewModel.deleteGear(id)
                }
                itemToDelete = null
            }
        )
    }

    // --- Product Edit Dialogs ---
    if (bookToEdit != null) {
        BookEditDialog(
            book = bookToEdit!!,
            onDismiss = { bookToEdit = null },
            onSave = { viewModel.saveBook(it); bookToEdit = null }
        )
    }
    if (audioToEdit != null) {
        AudioBookEditDialog(
            audioBook = audioToEdit!!,
            onDismiss = { audioToEdit = null },
            onSave = { viewModel.saveAudioBook(it); audioToEdit = null }
        )
    }
    if (courseToEdit != null) {
        CourseEditDialog(
            course = courseToEdit!!,
            onDismiss = { courseToEdit = null },
            onSave = { viewModel.saveCourse(it); courseToEdit = null }
        )
    }
    if (gearToEdit != null) {
        GearEditDialog(
            gear = gearToEdit!!,
            onDismiss = { gearToEdit = null },
            onSave = { viewModel.saveGear(it); gearToEdit = null }
        )
    }
}

data class CatalogFilter(val id: String, val title: String, val icon: ImageVector)
