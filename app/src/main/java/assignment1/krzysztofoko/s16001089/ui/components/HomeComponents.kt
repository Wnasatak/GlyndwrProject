package assignment1.krzysztofoko.s16001089.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.flowOf
import java.util.Locale
import kotlin.math.abs

/**
 * HomeTopBar Composable: The primary navigation bar for the Home Screen.
 * Includes branding, search, theme switching, and user authentication/dashboard access.
 *
 * @param isSearchVisible State of the search bar visibility.
 * @param isLoggedIn Whether a user is currently authenticated.
 * @param currentTheme The active theme for the app.
 * @param userRole The role of the logged-in user (e.g., admin, tutor).
 * @param onSearchClick Toggle search visibility.
 * @param onThemeChange Update the app theme.
 * @param onAboutClick Navigate to the About screen.
 * @param onAuthClick Navigate to Login/Register.
 * @param onDashboardClick Navigate to the user's specific dashboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    isSearchVisible: Boolean,
    isLoggedIn: Boolean,
    currentTheme: Theme,
    userRole: String? = null,
    onSearchClick: () -> Unit,
    onThemeChange: (Theme) -> Unit,
    onAboutClick: () -> Unit,
    onAuthClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Branded university logo
                AdaptiveBrandedLogo(
                    model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                    contentDescription = "Logo",
                    logoSize = 32.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(text = AppConstants.APP_NAME, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        },
        actions = {
            // Contextual actions based on login state and role
            TopBarSearchAction(isSearchVisible = isSearchVisible) { onSearchClick() }

            if (!isLoggedIn) {
                ThemeToggleButton(currentTheme = currentTheme, onThemeChange = onThemeChange, isLoggedIn = false)
            }

            IconButton(onClick = onAboutClick) {
                Icon(Icons.Default.Info, "About")
            }

            if (!isLoggedIn) {
                IconButton(onClick = onAuthClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Login, contentDescription = "Sign In")
                }
            } else {
                IconButton(onClick = onDashboardClick) {
                    // Dynamic icon based on user role
                    val icon = when (userRole?.lowercase()) {
                        "admin" -> Icons.Default.AdminPanelSettings
                        "teacher", "tutor" -> Icons.Default.School
                        else -> Icons.Default.Dashboard
                    }
                    Icon(imageVector = icon, contentDescription = "Dashboard")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
    )
}

/**
 * HomeBookItem Composable: Represents a single item (Book, Course, or Gear) in the home list.
 * Handles dynamic discount calculations and item-specific states (purchased, liked, playing).
 *
 * @param book The data entity to display.
 * @param isLoggedIn Current authentication status.
 * @param isPendingReview If true, shows a 'Reviewing' label (Admin context).
 * @param userRole The role of the user (affects discount display).
 * @param isLiked If the item is in the user's wishlist.
 * @param isPurchased If the item has already been bought by the user.
 * @param isAudioPlaying If the audiobook is currently playing.
 * @param onItemClick Navigation callback.
 * @param onToggleWishlist Wishlist action.
 * @param onPlayAudio Audio player control.
 * @param onInvoiceClick View receipt action.
 * @param onRemoveClick Remove from library action.
 */
@Composable
fun HomeBookItem(
    book: Book,
    isLoggedIn: Boolean,
    isPendingReview: Boolean = false,
    userRole: String?,
    isLiked: Boolean,
    isPurchased: Boolean,
    isAudioPlaying: Boolean,
    onItemClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onPlayAudio: () -> Unit,
    onInvoiceClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    
    // Fetch system-wide role discounts and local user data
    val roleDiscounts by db.userDao().getAllRoleDiscounts().collectAsState(initial = emptyList<RoleDiscount>())
    val userFlow = if (isLoggedIn && !FirebaseAuth.getInstance().currentUser?.uid.isNullOrEmpty()) {
        db.userDao().getUserFlow(FirebaseAuth.getInstance().currentUser!!.uid)
    } else {
        flowOf(null)
    }
    val localUser by userFlow.collectAsState(initial = null)

    // Calculate effective discount: The highest of role-based or individual discount
    val effectiveDiscount = remember(localUser, roleDiscounts) {
        val uRole = localUser?.role ?: "user"
        val roleRate = roleDiscounts.find { it.role == uRole }?.discountPercent ?: 0.0
        val individualRate = localUser?.discountPercent ?: 0.0
        maxOf(roleRate, individualRate)
    }

    BookItemCard(
        book = book,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onItemClick,
        imageOverlay = {
            // Show audio control for purchased audiobooks
            if (isLoggedIn && book.isAudioBook && isPurchased) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    SpinningAudioButton(isPlaying = isAudioPlaying, onToggle = onPlayAudio, size = 40)
                }
            }
        },
        trailingContent = {
            if (isLoggedIn) {
                IconButton(onClick = onToggleWishlist, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (isLiked) MaterialTheme.colorScheme.onSurface else Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        },
        bottomContent = {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPendingReview) {
                    // Badge for items awaiting moderator approval
                    Surface(color = Color(0xFFFBC02D).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFFBC02D).copy(alpha = 0.5f))) {
                        @Suppress("DEPRECATION")
                        Text(text = "REVIEWING", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, color = Color(0xFFFBC02D), fontWeight = FontWeight.ExtraBold)
                    }
                } else if (isPurchased) {
                    HomePurchasedLabel(book = book, onInvoiceClick = onInvoiceClick, onRemoveClick = onRemoveClick, isLoggedIn = isLoggedIn)
                } else {
                    HomePriceLabel(book = book, effectiveDiscount = effectiveDiscount, userRole = localUser?.role)
                }
            }
        }
    )
}

/**
 * Helper to display the status label and actions for a purchased item.
 */
@Composable
private fun HomePurchasedLabel(book: Book, onInvoiceClick: () -> Unit, onRemoveClick: () -> Unit, isLoggedIn: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val label = AppConstants.getItemStatusLabel(book)
        if (book.price > 0) {
            // Paid item label + Invoice shortcut
            Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (book.mainCategory == AppConstants.CAT_COURSES) Icons.Default.School else Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.6.dp))
                    @Suppress("DEPRECATION")
                    Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onInvoiceClick, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.ReceiptLong, "Invoice", tint = MaterialTheme.colorScheme.primary) }
        } else {
            // Free item label + Delete shortcut
            Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (book.mainCategory == AppConstants.CAT_COURSES) Icons.Default.School else Icons.Default.LibraryAddCheck, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.6.dp))
                    @Suppress("DEPRECATION")
                    Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
            }
            if (book.mainCategory != AppConstants.CAT_GEAR) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { if (isLoggedIn) onRemoveClick() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

/**
 * Handles the display of price, original price (if discounted), and the discount source badge.
 */
@Composable
private fun HomePriceLabel(book: Book, effectiveDiscount: Double, userRole: String?) {
    if (book.price == 0.0) {
        Text(text = AppConstants.LABEL_FREE, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
    } else if (effectiveDiscount > 0) {
        // Calculate and show discounted price alongside the struck-through original price
        val discountMultiplier = (100.0 - effectiveDiscount) / 100.0
        val discountPrice = "£" + String.format(Locale.US, "%.2f", book.price * discountMultiplier)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "£" + String.format(Locale.US, "%.2f", book.price), style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = discountPrice, style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Badge explaining why the user has this discount
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp), border = BorderStroke(0.5.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))) {
                val roleLabel = userRole?.uppercase() ?: "USER"
                Text(text = "$roleLabel DISCOUNT (-${effectiveDiscount.toInt()}% )", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), letterSpacing = 0.5.sp)
            }
        }
    } else {
        @Suppress("DEPRECATION")
        Text(text = "£" + String.format(Locale.US, "%.2f", book.price), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * UI State: Loading placeholder.
 */
@Composable
fun HomeLoadingState() {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            @Suppress("DEPRECATION")
            Text("Loading Data...", style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * UI State: Error message with retry action.
 */
@Composable
fun HomeErrorState(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("Error: $error", fontWeight = FontWeight.Bold)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

/**
 * UI State: Empty results display.
 */
@Composable
fun HomeEmptyState(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text("No results found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}

/**
 * Call-to-action banner for anonymous users to register.
 */
@Composable
fun PromotionBanner(theme: Theme, onRegisterClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.background(getBannerBrush(theme)).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(AppConstants.APP_NAME, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Enrol now to unlock exclusive group-wide discounts across our entire catalogue.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onRegisterClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Get Started", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/**
 * Personalized welcome banner for logged-in users.
 * Displays user info and their specific activated discounts.
 */
@Composable
fun MemberWelcomeBanner(user: UserLocal?, theme: Theme, onProfileClick: () -> Unit) {
    val role = user?.role ?: "user"
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val roleDiscounts by db.userDao().getAllRoleDiscounts().collectAsState(initial = emptyList<RoleDiscount>())
    
    // Effective discount calculation replicated here for the welcome message
    val effectiveDiscount = remember(user, roleDiscounts) {
        val userRole = user?.role ?: "user"
        val roleRate = roleDiscounts.find { it.role == userRole }?.discountPercent ?: 0.0
        val individualRate = user?.discountPercent ?: 0.0
        maxOf(roleRate, individualRate)
    }
    
    val displayRole = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    val displayName = buildString {
        if (!user?.title.isNullOrEmpty()) { append(user?.title); append(" ") }
        append(user?.name ?: displayRole)
    }
    
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
        Box(modifier = Modifier.background(getBannerBrush(theme)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(photoUrl = user?.photoUrl, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.width(12.dp))
                @Suppress("DEPRECATION")
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Welcome, $displayName", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    if (effectiveDiscount > 0) { Text(text = "${effectiveDiscount.toInt()}% $displayRole discount activated! Enjoy your perks ✨", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f)) }
                    else { Text(text = "Logged in as $displayRole. Access your management dashboard!", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f)) }
                }
                IconButton(onClick = onProfileClick) { Icon(Icons.Default.AccountBox, null, tint = Color.White) }
            }
        }
    }
}

/**
 * Returns a theme-aware gradient brush for banners.
 */
@Composable
private fun getBannerBrush(theme: Theme): Brush {
    val colors = when (theme) {
        Theme.SKY -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f))
        Theme.FOREST -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f))
        Theme.DARK_BLUE -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        Theme.DARK -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.2f))
        else -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f))
    }
    return Brush.linearGradient(colors = colors, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(1000f, 1000f))
}

/**
 * Scrollable bar for switching between high-level categories (All, Courses, Books, etc.).
 * Includes 'Infinite Scroll' logic for mobile to provide a smooth, loop-like selection experience.
 */
@Composable
fun MainCategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    if (isTablet) {
        // Standard static list for large screens
        val listState = rememberLazyListState()
        LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            items(categories) { category ->
                CategorySquareButton(label = category, icon = getMainCategoryIcon(category), isSelected = selectedCategory == category, scale = 1f, onClick = { onCategorySelected(category) })
            }
        }
    } else {
        // Pseudo-infinite scroll logic for mobile
        val infiniteCategories = Int.MAX_VALUE
        val startPosition = infiniteCategories / 2 - (infiniteCategories / 2 % categories.size)
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = startPosition)
        
        LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            items(infiniteCategories) { index ->
                val categoryIndex = index % categories.size
                val category = categories[categoryIndex]
                
                // Parallax scaling effect based on proximity to the screen center
                val scale by remember { derivedStateOf { val layoutInfo = listState.layoutInfo; val visibleItemsInfo = layoutInfo.visibleItemsInfo; val itemInfo = visibleItemsInfo.find { it.index == index }; if (itemInfo != null) { val center = layoutInfo.viewportEndOffset / 2; val itemCenter = itemInfo.offset + (itemInfo.size / 2); val distanceFromCenter = abs(center - itemCenter).toFloat(); val normalizedDistance = (distanceFromCenter / center).coerceIn(0f, 1f); 1.25f - (normalizedDistance * 0.4f) } else { 0.85f } } }
                
                CategorySquareButton(label = category, icon = getMainCategoryIcon(category), isSelected = selectedCategory == category, scale = scale, onClick = { onCategorySelected(category) })
            }
        }
    }
}

/**
 * Secondary scrollable bar for sub-category chips.
 */
@Composable
fun SubCategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            CategoryChip(category = category, isSelected = selectedCategory == category, onCategorySelected = onCategorySelected)
        }
    }
}

/**
 * Mapping helper to provide consistent iconography across the app.
 */
private fun getMainCategoryIcon(category: String): ImageVector {
    return when (category) {
        AppConstants.CAT_ALL -> Icons.Default.GridView
        AppConstants.CAT_FREE -> Icons.Default.Redeem
        AppConstants.CAT_COURSES -> Icons.Default.School
        AppConstants.CAT_GEAR -> Icons.Default.Checkroom
        AppConstants.CAT_BOOKS -> Icons.AutoMirrored.Filled.MenuBook
        AppConstants.CAT_AUDIOBOOKS -> Icons.Default.Headphones
        else -> Icons.Default.Category
    }
}
