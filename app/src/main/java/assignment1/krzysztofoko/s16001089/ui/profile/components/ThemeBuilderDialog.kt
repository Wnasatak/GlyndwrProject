package assignment1.krzysztofoko.s16001089.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A professional, client-friendly Theme Designer.
 * Optimized for mobile with an adaptive header and compact actions.
 */
@Composable
fun ThemeBuilderDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit, 
    isDark: Boolean,
    onIsDarkChange: (Boolean) -> Unit,
    // Primary
    primary: Long, onPrimary: (Long) -> Unit,
    onPrimaryVal: Long, onOnPrimary: (Long) -> Unit,
    primaryContainer: Long, onPrimaryContainer: (Long) -> Unit,
    onPrimaryContainerVal: Long, onOnPrimaryContainer: (Long) -> Unit,
    // Secondary
    secondary: Long, onSecondary: (Long) -> Unit,
    onSecondaryVal: Long, onOnSecondary: (Long) -> Unit,
    secondaryContainer: Long, onSecondaryContainer: (Long) -> Unit,
    onSecondaryContainerVal: Long, onOnSecondaryContainer: (Long) -> Unit,
    // Tertiary
    tertiary: Long, onTertiary: (Long) -> Unit,
    onTertiaryVal: Long, onOnTertiary: (Long) -> Unit,
    tertiaryContainer: Long, onTertiaryContainer: (Long) -> Unit,
    onTertiaryContainerVal: Long, onOnTertiaryContainer: (Long) -> Unit,
    // Background/Surface
    background: Long, onBackground: (Long) -> Unit,
    onBackgroundVal: Long, onOnBackground: (Long) -> Unit,
    surface: Long, onSurface: (Long) -> Unit,
    onSurfaceVal: Long, onOnSurface: (Long) -> Unit
) {
    if (!show) return

    val configuration = LocalConfiguration.current
    val isSmallPhone = configuration.screenWidthDp < 400

    val presets = listOf(
        0xFF6750A4, 0xFF625B71, 0xFF7D5260, // Material Pro
        0xFF38BDF8, 0xFF0EA5E9, 0xFF0369A1, // Azure Blue
        0xFF818CF8, 0xFF6366F1, 0xFF4338CA, // Indigo
        0xFF10B981, 0xFF059669, 0xFF047857, // Emerald Green
        0xFFF43F5E, 0xFFE11D48, 0xFFBE123C, // Rose Red
        0xFFF59E0B, 0xFFD97706, 0xFFB45309, // Amber
        0xFF020617, 0xFF0F172A, 0xFF1E293B, // Deep Slates
        0xFFF8FAFC, 0xFFF1F5F9, 0xFFE2E8F0  // Clean Whites
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Optimized Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.ColorLens, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Signature Designer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                                }
                                Text("Craft your perfect app experience", style = MaterialTheme.typography.bodySmall)
                            }
                            
                            // On tablets/large phones, keep Reset at top. On small phones, we handle it in the column flow.
                            if (!isSmallPhone) {
                                ResetButton(onReset)
                            }
                        }
                        
                        // Optimized: Reset button below title for phones
                        if (isSmallPhone) {
                            Spacer(Modifier.height(16.dp))
                            ResetButton(onReset, modifier = Modifier.align(Alignment.Start))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(20.dp))
                    
                    SectionTitle("Visual Atmosphere", "Toggle between a crisp day mode or a sleek night mode.")
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isDark) "Midnight Active" else "Bright Sky Active", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(4.dp)) {
                                IconButton(
                                    onClick = { onIsDarkChange(false) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (!isDark) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (!isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) { Icon(Icons.Rounded.WbSunny, null, modifier = Modifier.size(20.dp)) }
                                
                                IconButton(
                                    onClick = { onIsDarkChange(true) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (isDark) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) { Icon(Icons.Rounded.Nightlight, null, modifier = Modifier.size(20.dp)) }
                            }
                        }
                    }

                    SectionTitle("Brand Accents", "The main colors used for buttons, active icons, and highlights.")
                    ColorEditorRow("Primary Glow", "Main color for buttons and your brand identity.", primary, onPrimary, presets)
                    ColorEditorRow("Accent Text", "The color of text inside your primary buttons.", onPrimaryVal, onOnPrimary, presets)
                    ColorEditorRow("Hero Panels", "Background color for main headers and large top banners.", primaryContainer, onPrimaryContainer, presets)
                    ColorEditorRow("Hero Text", "The color of text used inside hero banners and headers.", onPrimaryContainerVal, onOnPrimaryContainer, presets)

                    SectionTitle("Subtle Details", "Colors used for secondary actions, chips, and small labels.")
                    ColorEditorRow("Secondary Accent", "Used for smaller highlights and less prominent buttons.", secondary, onSecondary, presets)
                    ColorEditorRow("Supporting Accent", "A third color choice for extra variety and status tags.", tertiary, onTertiary, presets)
                    ColorEditorRow("Supporting Panel", "Background color for small tags and informational boxes.", tertiaryContainer, onTertiaryContainer, presets)
                    ColorEditorRow("Supporting Text", "Text color inside informational boxes and tags.", onTertiaryContainerVal, onOnTertiaryContainer, presets)

                    SectionTitle("Core Environment", "The colors that define the backdrop of the entire application.")
                    ColorEditorRow("App Background", "The main background color behind all your content.", background, onBackground, presets)
                    ColorEditorRow("Reading Text", "The primary color for all standard reading text.", onBackgroundVal, onOnBackground, presets)
                    ColorEditorRow("Card Surface", "The color of content cards, menus, and popup dialogs.", surface, onSurface, presets)
                    ColorEditorRow("Surface Text", "The color of text specifically shown inside cards.", onSurfaceVal, onOnSurface, presets)
                    
                    Spacer(Modifier.height(40.dp))
                }

                // Optimized Footer for Phones
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { 
                            Text("Discard", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                        }
                        Spacer(Modifier.width(16.dp))
                        // Optimized: Compact button text for phones
                        Button(
                            onClick = onSave,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = if (isSmallPhone) 20.dp else 28.dp, vertical = 14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isSmallPhone) "Save Design" else "Apply Signature", 
                                fontWeight = FontWeight.Bold, 
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetButton(onReset: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onReset,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Icon(Icons.Rounded.RestartAlt, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Reset", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(title: String, description: String) {
    Column(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ColorEditorRow(
    label: String,
    description: String,
    currentColor: Long,
    onColorSelect: (Long) -> Unit,
    presets: List<Long>
) {
    var showPicker by remember { mutableStateOf(false) }

    Surface(
        onClick = { showPicker = true },
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(currentColor))
                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Tune, 
                    null, 
                    modifier = Modifier.size(18.dp), 
                    tint = if (Color(currentColor).isLight()) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }

    if (showPicker) {
        Dialog(onDismissRequest = { showPicker = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Select $label", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Pick a professional tone from the palette:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 20.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(260.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(presets) { color ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(
                                        width = if (currentColor == color) 4.dp else 1.dp,
                                        color = if (currentColor == color) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    )
                                    .clickable { 
                                        onColorSelect(color)
                                        showPicker = false
                                    }
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    TextButton(
                        onClick = { showPicker = false },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Close", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
