package assignment1.krzysztofoko.s16001089.ui.admin.components.Courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Assignment

@Composable
fun AssignmentEditDialog(
    assignment: Assignment,
    onDismiss: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    var title by remember { mutableStateOf(assignment.title) }
    var description by remember { mutableStateOf(assignment.description) }
    var status by remember { mutableStateOf(assignment.status) }
    var allowedFileTypes by remember { mutableStateOf(assignment.allowedFileTypes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (assignment.title.isBlank()) "Add New Task" else "Edit Task", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), maxLines = 4)
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status (PENDING, etc.)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = allowedFileTypes, onValueChange = { allowedFileTypes = it }, label = { Text("Allowed Files (PDF,DOCX,etc.)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(assignment.copy(title = title, description = description, status = status, allowedFileTypes = allowedFileTypes))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
