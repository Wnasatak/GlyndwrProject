package assignment1.krzysztofoko.s16001089.ui.tutor.components.Courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

@Composable
fun TutorCoursesTab(
    viewModel: TutorViewModel
) {
    val courses by viewModel.tutorCourses.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Courses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(courses) { course ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(course.title, fontWeight = FontWeight.Bold)
                        Text(course.department, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
