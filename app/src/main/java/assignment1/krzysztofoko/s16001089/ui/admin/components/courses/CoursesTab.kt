package assignment1.krzysztofoko.s16001089.ui.admin.components.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.ui.admin.AdminViewModel
import assignment1.krzysztofoko.s16001089.ui.admin.components.catalog.*
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveScreenContainer
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveWidths
import java.util.UUID

/**
 * CoursesTab
 *
 * Displays the academic course catalog for administrators to manage.
 * Allows selecting a course to manage its modules and assignments.
 */
@Composable
fun CoursesTab(
    viewModel: AdminViewModel,
    isDarkTheme: Boolean,
    onCourseSelected: (Course) -> Unit,
    showAddCourseDialog: Boolean = false,
    onAddCourseDialogConsumed: () -> Unit = {}
) {
    val courses by viewModel.allCourses.collectAsState(initial = emptyList())
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var itemToDelete by remember { mutableStateOf<Course?>(null) }

    // Handle external trigger for adding a course (e.g. from the top bar)
    LaunchedEffect(showAddCourseDialog) {
        if (showAddCourseDialog) {
            courseToEdit = Course(
                id = UUID.randomUUID().toString(), 
                title = "", 
                department = "", 
                mainCategory = AppConstants.CAT_COURSES
            )
            onAddCourseDialogConsumed()
        }
    }

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
                    title = "Academic Catalog",
                    subtitle = "Manage university courses and syllabus.",
                    icon = Icons.Default.School,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            item(span = { GridItemSpan(this.maxLineSpan) }) { 
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CatalogSectionHeader("Active Courses", courses.size) 
                }
            }
            
            items(courses) { item ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CatalogItemCard(
                        title = item.title,
                        subtitle = item.department,
                        price = item.price,
                        imageUrl = item.imageUrl,
                        icon = Icons.Default.School,
                        onEdit = { courseToEdit = item },
                        onDelete = { itemToDelete = item },
                        isDarkTheme = isDarkTheme,
                        onClick = { onCourseSelected(item) }
                    )
                }
            }

            item(span = { GridItemSpan(this.maxLineSpan) }) { Spacer(Modifier.height(80.dp)) }
        }
    }

    // --- Edit Course Dialog ---
    if (courseToEdit != null) {
        CourseEditDialog(
            course = courseToEdit!!,
            onDismiss = { courseToEdit = null },
            onSave = { 
                viewModel.saveCourse(it)
                courseToEdit = null 
            }
        )
    }

    // --- Deletion Dialog ---
    if (itemToDelete != null) {
        CatalogDeleteDialog(
            itemName = "Course",
            onDismiss = { itemToDelete = null },
            onConfirm = {
                viewModel.deleteCourse(itemToDelete!!.id)
                itemToDelete = null
            }
        )
    }
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
