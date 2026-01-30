package assignment1.krzysztofoko.s16001089.ui.admin.components.Catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Course
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

@Composable
fun CourseEditDialog(course: Course, onDismiss: () -> Unit, onSave: (Course) -> Unit) {
    var title by remember { mutableStateOf(course.title) }
    var price by remember { mutableStateOf(course.price.toString()) }
    var description by remember { mutableStateOf(course.description) }
    var imageUrl by remember { mutableStateOf(course.imageUrl) }
    var category by remember { mutableStateOf(course.category) }
    var department by remember { mutableStateOf(course.department) }
    var isInstallmentAvailable by remember { mutableStateOf(course.isInstallmentAvailable) }
    var modulePrice by remember { mutableStateOf(course.modulePrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Course Details", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                AsyncImage(model = formatAssetUrl(imageUrl), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Full Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isInstallmentAvailable, onCheckedChange = { isInstallmentAvailable = it })
                    Text("Installments Available")
                }
                if (isInstallmentAvailable) {
                    OutlinedTextField(value = modulePrice, onValueChange = { modulePrice = it }, label = { Text("Module Price") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(course.copy(
                    title = title, price = price.toDoubleOrNull() ?: course.price, 
                    description = description, imageUrl = imageUrl, category = category,
                    department = department, isInstallmentAvailable = isInstallmentAvailable,
                    modulePrice = modulePrice.toDoubleOrNull() ?: course.modulePrice
                )) 
            }) { Text("Save Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
