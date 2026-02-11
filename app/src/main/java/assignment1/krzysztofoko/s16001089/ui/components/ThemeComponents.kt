package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import assignment1.krzysztofoko.s16001089.ui.theme.LocalAppTheme
import kotlinx.coroutines.delay

/**
 * A reusable, professional button to access theme customization.
 */
@Composable
fun AppAppearanceSelector(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onOpenDesigner: () -> Unit,
    isLoggedIn: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onOpenDesigner,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Palette, null, tint = Color.White)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("App Appearance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("Customize colors and themes", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

/**
 * A modern theme selector pill that expands to show the active theme name.
 */
@Composable
fun ThemeToggleButton(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onOpenCustomBuilder: () -> Unit = {},
    isLoggedIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showThemeMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var isFirstComposition by remember { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val targetColor = if (isExpanded) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val containerColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 800),
        label = "containerColor"
    )

    LaunchedEffect(currentTheme) {
        if (isFirstComposition) {
            isFirstComposition = false
        } else {
            isExpanded = true
            delay(2000)
            isExpanded = false
        }
    }

    // Adaptive container: Fixed width on phones (cover), Wrap content on tablets (push)
    Box(
        modifier = if (isTablet) {
            modifier.height(44.dp).wrapContentWidth()
        } else {
            modifier.size(44.dp)
        },
        contentAlignment = if (isTablet) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Surface(
            onClick = { showThemeMenu = true },
            shape = RoundedCornerShape(24.dp),
            color = containerColor,
            contentColor = if (isExpanded) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isExpanded) 0.5f else 0.2f)),
            modifier = Modifier.wrapContentWidth(
                align = if (isTablet) Alignment.Start else Alignment.End, 
                unbounded = !isTablet // Allows expansion over content ONLY on phones
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = if (isExpanded) 14.dp else 10.dp, vertical = 10.dp)
                    .animateContentSize(animationSpec = tween(500)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = getThemeIcon(currentTheme),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = getThemeName(currentTheme),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        ThemeSelectionDropdown(
            expanded = showThemeMenu,
            onDismissRequest = { showThemeMenu = false },
            onThemeChange = onThemeChange,
            onOpenCustomBuilder = onOpenCustomBuilder,
            isLoggedIn = isLoggedIn,
            offset = if (isTablet) DpOffset(x = 0.dp, y = 4.dp) else DpOffset(x = (-160).dp, y = 4.dp)
        )
    }
}

@Composable
fun ThemeSelectionDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onThemeChange: (Theme) -> Unit,
    onOpenCustomBuilder: () -> Unit = {},
    isLoggedIn: Boolean = false,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
    val currentTheme = LocalAppTheme.current

    DropdownMenu(
        expanded = expanded, 
        onDismissRequest = onDismissRequest,
        modifier = modifier.width(200.dp),
        offset = offset,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = "APPEARANCE SETTINGS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Theme.entries.forEach { theme ->
            if (theme == Theme.CUSTOM && !isLoggedIn) return@forEach

            val isSelected = theme == currentTheme
            DropdownMenuItem(
                text = { 
                    Text(
                        text = getThemeName(theme), 
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                onClick = { 
                    onThemeChange(theme)
                    if (theme == Theme.CUSTOM) {
                        onOpenCustomBuilder()
                    }
                    onDismissRequest() 
                },
                leadingIcon = { 
                    Icon(
                        imageVector = getThemeIcon(theme), 
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                trailingIcon = {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp), 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = if (isSelected) {
                    MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.primary,
                        leadingIconColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    MenuDefaults.itemColors()
                }
            )
        }
    }
}

private fun getThemeIcon(theme: Theme) = when (theme) {
    Theme.LIGHT -> Icons.Rounded.LightMode
    Theme.DARK -> Icons.Rounded.DarkMode
    Theme.GRAY -> Icons.Rounded.AutoAwesomeMotion
    Theme.SKY -> Icons.Rounded.CloudQueue
    Theme.FOREST -> Icons.Rounded.NaturePeople
    Theme.DARK_BLUE -> Icons.Rounded.NightsStay
    Theme.CUSTOM -> Icons.Rounded.AutoFixHigh
}

private fun getThemeName(theme: Theme) = when (theme) {
    Theme.LIGHT -> "Light Mode"
    Theme.DARK -> "Dark Mode"
    Theme.GRAY -> "Gray Street"
    Theme.SKY -> "Sky Blue"
    Theme.FOREST -> "Forest Green"
    Theme.DARK_BLUE -> "Deep Navy"
    Theme.CUSTOM -> "My Design"
}
