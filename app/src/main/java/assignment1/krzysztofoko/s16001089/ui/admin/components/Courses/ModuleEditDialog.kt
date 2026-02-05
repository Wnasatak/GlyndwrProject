package assignment1.krzysztofoko.s16001089.ui.admin.components.Courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.ModuleContent

@Composable
fun ModuleEditDialog(
    module: ModuleContent,
    onDismiss: () -> Unit,
    onSave: (ModuleContent) -> Unit
) {
    var title by remember { mutableStateOf(module.title) }
    var description by remember { mutableStateOf(module.description) }
    var contentType by remember { mutableStateOf(module.contentType) }
    var contentUrl by remember { mutableStateOf(module.contentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (module.title.isBlank()) "Add New Module" else "Edit Module", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Module Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), maxLines = 4)
                OutlinedTextField(value = contentType, onValueChange = { contentType = it }, label = { Text("Content Type (e.g., VIDEO, PDF)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = contentUrl, onValueChange = { contentUrl = it }, label = { Text("Content URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedModule = module.copy(
                    title = title,
                    description = description,
                    contentType = contentType,
                    contentUrl = contentUrl
                )
                onSave(updatedModule)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
