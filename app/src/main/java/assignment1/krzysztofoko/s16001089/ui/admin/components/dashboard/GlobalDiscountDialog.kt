package assignment1.krzysztofoko.s16001089.ui.admin.components.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.RoleDiscount

/**
 * GlobalDiscountDialog.
 * A specialized administrative component that allows fine-grained control over role-based discounts.
 * Refactored to support batch updates and intuitive role switching.
 *
 * @param existingDiscounts The current list of discounts fetched from the system database.
 * @param onDismiss Callback invoked to close the dialog without saving.
 * @param onSave Callback invoked with a map of role identifiers to their new percentage values.
 */
@Composable
fun GlobalDiscountDialog(
    existingDiscounts: List<RoleDiscount>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Float>) -> Unit
) {
    // Define the set of system roles eligible for global discounts.
    val roles = listOf("student", "teacher", "user", "admin")
    
    // Tracks which role's discount is currently being edited.
    var selectedRole by remember { mutableStateOf("student") }
    
    // Uses a snapshot state map to buffer changes locally before they are committed.
    val pendingChanges = remember { mutableStateMapOf<String, Float>() }
    
    // Synchronize the local buffer with external data whenever the source list changes.
    LaunchedEffect(existingDiscounts) {
        existingDiscounts.forEach { pendingChanges[it.role] = it.discountPercent.toFloat() }
    }

    // Derived values for the current selection.
    val currentPercent = pendingChanges[selectedRole] ?: 0f
    var isEditingManually by remember { mutableStateOf(false) }
    
    // Local state for the manual text field, synced with the selected role.
    var manualText by remember(selectedRole, isEditingManually) { 
        mutableStateOf(currentPercent.toInt().toString()) 
    }

    val dialogShape = RoundedCornerShape(28.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), dialogShape),
        title = {
            // Header section with thematic icon and descriptive text.
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                    shape = CircleShape, 
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { 
                        Icon(
                            Icons.Default.Percent, 
                            null, 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(22.dp)
                        ) 
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Role Discounts", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                Text("Batch update group rates", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // --- Role Selection Section ---
                Text("Target Role:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(roles) { role ->
                        val isSelected = selectedRole == role
                        val rolePercent = pendingChanges[role] ?: 0f
                        
                        // Selectable card for each role.
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    selectedRole = role
                                    isEditingManually = false
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = role.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Text(
                                    text = "${rolePercent.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                // --- Discount Adjustment Section ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Discount Rate:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        
                        // Toggle between a slider-driven view and manual keyboard entry.
                        if (isEditingManually) {
                            OutlinedTextField(
                                value = manualText,
                                onValueChange = { input ->
                                    // Restrict input to numeric percentages between 0 and 100.
                                    if (input.length <= 3 && input.all { it.isDigit() }) {
                                        manualText = input
                                        val newVal = input.toFloatOrNull() ?: 0f
                                        pendingChanges[selectedRole] = newVal.coerceIn(0f, 100f)
                                    }
                                },
                                modifier = Modifier.width(80.dp),
                                textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 18.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                suffix = { Text("%") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        } else {
                            Text(
                                text = "${currentPercent.toInt()}%", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Black, 
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { isEditingManually = true }
                            )
                        }
                    }
                    
                    // Slider for quick, intuitive rate adjustments.
                    Slider(
                        value = currentPercent,
                        onValueChange = { 
                            pendingChanges[selectedRole] = it
                            isEditingManually = false 
                        },
                        valueRange = 0f..100f,
                        steps = 100, 
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
                
                // Visual disclaimer about state persistence during editing.
                Text(
                    "Note: Switches between roles above will preserve your temporary changes until you click save.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            // Commit all buffered changes via the onSave callback.
            Button(
                onClick = { onSave(pendingChanges.toMap()) }, 
                modifier = Modifier.fillMaxWidth().height(48.dp), 
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save All Changes", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            // Exit without committing any changes to the system.
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(32.dp)) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = dialogShape,
        containerColor = MaterialTheme.colorScheme.surface
    )
}
