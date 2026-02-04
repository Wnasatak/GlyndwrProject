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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.UserLocal
import coil.compose.AsyncImage
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    isSearchVisible: Boolean,
    isLoggedIn: Boolean,
    isDarkTheme: Boolean,
    onSearchClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onAboutClick: () -> Unit,
    onAuthClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = formatAssetUrl("images/media/GlyndwrUniversity.jpg"),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = rotation.value }
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(8.dp))
                Text(text = AppConstants.APP_NAME, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        },
        actions = {
            TopBarSearchAction(isSearchVisible = isSearchVisible) { onSearchClick() }
            IconButton(onClick = onToggleTheme) { 
                Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme") 
            }
            IconButton(onClick = onAboutClick) { 
                Icon(Icons.Default.Info, "About") 
            }
            if (!isLoggedIn) {
                IconButton(onClick = onAuthClick) { 
                    Icon(Icons.AutoMirrored.Filled.Login, "Login") 
                }
            } else {
                IconButton(onClick = onDashboardClick) { 
                    Icon(Icons.Default.Dashboard, "Dashboard") 
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
    )
}

@Composable
fun EnrolledCourseHeader(
    course: Book,
    isLive: Boolean = false,
    onEnterClassroom: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Live indicator in top right corner
            if (isLive) {
                val infiniteTransition = rememberInfiniteTransition(label = "live")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "alpha"
                )
                Surface(
                    color = Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 0.dp),
                    modifier = Modifier.align(Alignment.TopEnd),
                    border = BorderStroke(0.5.dp, Color.Red.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .graphicsLayer { this.alpha = alpha }
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            color = Color.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = formatAssetUrl(course.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "MY COURSE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF00BFA5))
                    Text(text = course.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onEnterClassroom(course.id) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                    ) {
                        Icon(Icons.Default.School, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enter Classroom", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FreeCourseHeader(
    course: Book,
    isLive: Boolean = false,
    onEnterClassroom: (String) -> Unit
) {
    EnrolledCourseHeader(course = course, isLive = isLive, onEnterClassroom = onEnterClassroom)
}

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
    BookItemCard(
        book = book,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onItemClick,
        imageOverlay = {
            if (isLoggedIn && book.isAudioBook && isPurchased) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    SpinningAudioButton(isPlaying = isAudioPlaying, onToggle = onPlayAudio, size = 40)
                }
            }
        },
        trailingContent = {
            if (isLoggedIn) {
                IconButton(onClick = onToggleWishlist, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        bottomContent = {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPendingReview) {
                    Surface(
                        color = Color(0xFFFBC02D).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFBC02D).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "REVIEWING",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFBC02D),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else if (isPurchased) {
                    HomePurchasedLabel(
                        book = book,
                        onInvoiceClick = onInvoiceClick,
                        onRemoveClick = onRemoveClick,
                        isLoggedIn = isLoggedIn
                    )
                } else {
                    HomePriceLabel(book = book, isLoggedIn = isLoggedIn, userRole = userRole)
                }
            }
        }
    )
}

@Composable
private fun HomePurchasedLabel(
    book: Book,
    onInvoiceClick: () -> Unit,
    onRemoveClick: () -> Unit,
    isLoggedIn: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val label = AppConstants.getItemStatusLabel(book)
        
        if (book.price > 0) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (book.mainCategory == AppConstants.CAT_COURSES) Icons.Default.School else Icons.AutoMirrored.Filled.ReceiptLong, 
                        null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onInvoiceClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, "Invoice", tint = MaterialTheme.colorScheme.primary)
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (book.mainCategory == AppConstants.CAT_COURSES) Icons.Default.School else Icons.Default.LibraryAddCheck, 
                        null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
            }
            if (book.mainCategory != AppConstants.CAT_GEAR) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { if (isLoggedIn) onRemoveClick() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun HomePriceLabel(book: Book, isLoggedIn: Boolean, userRole: String?) {
    val isStudent = userRole?.equals("student", ignoreCase = true) == true
    
    if (book.price == 0.0) {
        Text(text = AppConstants.LABEL_FREE, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
    } else if (isLoggedIn && isStudent) {
        val discountPrice = "£" + String.format(Locale.US, "%.2f", book.price * 0.9)
        Text(
            text = "£" + String.format(Locale.US, "%.2f", book.price),
            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
            Text(
                text = discountPrice,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    } else {
        Text(
            text = "£" + String.format(Locale.US, "%.2f", book.price),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HomeLoadingState() {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Loading Data...", style = MaterialTheme.typography.labelSmall)
        }
    }
}

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

@Composable
fun HomeEmptyState(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text("No results found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}

@Composable
fun PromotionBanner(onRegisterClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(AppConstants.APP_NAME, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Exclusive 10% student discount applied for enrolled students.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onRegisterClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Get Started", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun MemberWelcomeBanner(user: UserLocal?) {
    val role = user?.role ?: "user"
    val isStudent = role.equals("student", ignoreCase = true)
    val displayRole = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    
    val displayName = buildString {
        if (!user?.title.isNullOrEmpty()) {
            append(user?.title)
            append(" ")
        }
        append(user?.name ?: displayRole)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Welcome, $displayName", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (isStudent) {
                    Text(text = "10% discount activated! Enjoy your perks ✨", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                } else {
                    Text(text = "Logged in as $displayRole. Access your management dashboard!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun MainCategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val infiniteCategories = Int.MAX_VALUE
    val startPosition = infiniteCategories / 2 - (infiniteCategories / 2 % categories.size)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startPosition)
    
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(infiniteCategories) { index ->
            val categoryIndex = index % categories.size
            val category = categories[categoryIndex]
            
            val scale by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo
                    val itemInfo = visibleItemsInfo.find { it.index == index }
                    
                    if (itemInfo != null) {
                        val center = layoutInfo.viewportEndOffset / 2
                        val itemCenter = itemInfo.offset + (itemInfo.size / 2)
                        val distanceFromCenter = abs(center - itemCenter).toFloat()
                        val normalizedDistance = (distanceFromCenter / center).coerceIn(0f, 1f)
                        1.25f - (normalizedDistance * 0.4f)
                    } else {
                        0.85f
                    }
                }
            }

            CategorySquareButton(
                label = category,
                icon = getMainCategoryIcon(category),
                isSelected = selectedCategory == category,
                scale = scale,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun SubCategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            CategoryChip(category = category, isSelected = selectedCategory == category, onCategorySelected = onCategorySelected)
        }
    }
}

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
