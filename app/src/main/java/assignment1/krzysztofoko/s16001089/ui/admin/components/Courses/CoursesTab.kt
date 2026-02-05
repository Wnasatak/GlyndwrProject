package assignment1.krzysztofoko.s16001089.ui.admin.components.Courses

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsSuggest
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
import assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog.*
import java.util.UUID

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

    // Handle external trigger for adding a course
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderSection(
                    title = "Academic Catalog",
                    subtitle = "Manage university courses and syllabus.",
                    icon = Icons.Default.School,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            item { 
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

            item { Spacer(Modifier.height(80.dp)) }
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
