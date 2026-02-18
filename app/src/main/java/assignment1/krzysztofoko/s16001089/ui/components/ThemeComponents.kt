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
 * ThemeComponents.kt
 *
 * This file houses all the user-facing components for selecting and customising
 * the application's visual theme. It provides a consistent and branded entry point
 * for users to change their appearance settings.
 */

/**
 * AppAppearanceSelector Composable
 *
 * A prominent, card-style button used on the main settings or dashboard screen.
 * It serves as the primary entry point to the theme selection and customisation section.
 *
 * @param currentTheme The currently active `Theme` enum.
 * @param onThemeChange Callback to change the global theme.
 * @param onOpenDesigner Callback to navigate to the advanced theme customisation screen.
 * @param isLoggedIn Flag indicating the user's authentication status.
 * @param modifier Custom styling for the component.
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
                Text("Customise colours and themes", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

/**
 * ThemeToggleButton Composable
 *
 * A compact, animated theme picker designed for use in a TopAppBar.
 * When a new theme is selected, it temporarily expands to show the name of the new theme,
 * providing clear visual feedback before shrinking back to an icon-only state.
 *
 * @param currentTheme The currently selected theme.
 * @param onThemeChange Callback to apply a new theme.
 * @param onOpenCustomBuilder Callback to open the advanced theme designer.
 * @param isLoggedIn Authentication status, used to enable/disable the 'Custom' theme option.
 */
@Composable
fun ThemeToggleButton(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    onOpenCustomBuilder: () -> Unit = {},
    isLoggedIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    // --- STATE MANAGEMENT --- //
    var showThemeMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) } // Controls the text visibility.
    var isFirstComposition by remember { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Animate the background colour change when the pill expands.
    val targetColor = if (isExpanded) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val containerColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(durationMillis = 800), label = "containerColor")

    // This effect triggers the expand/collapse animation upon a theme change.
    LaunchedEffect(currentTheme) {
        if (isFirstComposition) {
            isFirstComposition = false
        } else {
            isExpanded = true
            delay(2000) // Keep the name visible for 2 seconds.
            isExpanded = false
        }
    }

    // The main container that handles the dropdown menu.
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
                unbounded = !isTablet // Allows the pill to expand over other content on phones.
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = if (isExpanded) 14.dp else 10.dp, vertical = 10.dp)
                    .animateContentSize(animationSpec = tween(500)), // Animate the width change smoothly.
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = getThemeIcon(currentTheme),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // The theme name text appears and disappears with an animation.
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

/**
 * ThemeSelectionDropdown Composable
 *
 * A styled dropdown menu that lists all available themes, indicating the currently 
 * selected one with a checkmark icon.
 */
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
        // Menu Header
        Text(
            text = "APPEARANCE SETTINGS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Dynamically create a menu item for each entry in the Theme enum.
        Theme.entries.forEach { theme ->
            // The 'Custom' theme is only available to authenticated users.
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
                    // If the user selects 'Custom', also trigger the builder.
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
                // Show a checkmark for the currently active theme.
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

/**
 * A private helper function that maps a `Theme` enum to its corresponding icon.
 */
private fun getThemeIcon(theme: Theme) = when (theme) {
    Theme.LIGHT -> Icons.Rounded.LightMode
    Theme.DARK -> Icons.Rounded.DarkMode
    Theme.GRAY -> Icons.Rounded.AutoAwesomeMotion
    Theme.SKY -> Icons.Rounded.CloudQueue
    Theme.FOREST -> Icons.Rounded.NaturePeople
    Theme.DARK_BLUE -> Icons.Rounded.NightsStay
    Theme.CUSTOM -> Icons.Rounded.AutoFixHigh
}

/**
 * A private helper function that maps a `Theme` enum to its user-friendly display name.
 */
private fun getThemeName(theme: Theme) = when (theme) {
    Theme.LIGHT -> "Light Mode"
    Theme.DARK -> "Dark Mode"
    Theme.GRAY -> "Grey Street"
    Theme.SKY -> "Sky Blue"
    Theme.FOREST -> "Forest Green"
    Theme.DARK_BLUE -> "Deep Navy"
    Theme.CUSTOM -> "My Design"
}
