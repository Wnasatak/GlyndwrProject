package assignment1.krzysztofoko.s16001089.ui.tutor.components.Students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorStudentsTab(
    viewModel: TutorViewModel
) {
    val students by viewModel.allStudents.collectAsState()
    var searchTxt by remember { mutableStateOf("") }

    val filteredStudents = remember(students, searchTxt) {
        if (searchTxt.isEmpty()) students
        else students.filter { it.name.contains(searchTxt, ignoreCase = true) || it.email.contains(searchTxt, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Student Directory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        
        OutlinedTextField(
            value = searchTxt,
            onValueChange = { searchTxt = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            placeholder = { Text("Search students...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = MaterialTheme.shapes.medium
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredStudents) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = student.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name, fontWeight = FontWeight.Bold)
                            Text(student.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { /* TODO: Navigate to chat */ }) {
                            Icon(Icons.Default.Chat, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
