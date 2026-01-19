package assignment1.krzysztofoko.s16001089.ui.home

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.AsyncImage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    allBooks: List<Book>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onAboutClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedMainCategory by remember { mutableStateOf("All") }
    var selectedSubCategory by remember { mutableStateOf("All Genres") }

    val mainCategories = listOf("All", "University Courses", "University Gear", "Books", "Audio Books")
    
    val subCategoriesMap = mapOf(
        "Books" to listOf("All Genres", "Technology", "Cooking", "Fantasy", "Mystery", "Self-Help"),
        "Audio Books" to listOf("All Genres", "Self-Help", "Technology", "Cooking", "Mystery"),
        "University Courses" to listOf("All Departments", "Science", "Business", "Technology")
    )

    val filteredBooks = remember(selectedMainCategory, selectedSubCategory, allBooks) {
        allBooks.filter { book ->
            val matchMain = when (selectedMainCategory) {
                "All" -> true
                else -> book.mainCategory.equals(selectedMainCategory, ignoreCase = true)
            }
            val matchSub = if (selectedSubCategory.contains("All", ignoreCase = true)) true else book.category.equals(selectedSubCategory, ignoreCase = true)
            matchMain && matchSub
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AsyncImage(
                                model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                contentDescription = "Logo",
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Glyndŵr Store",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Theme")
                        }
                        IconButton(onClick = onAboutClick) {
                            Icon(Icons.Default.Info, contentDescription = "About")
                        }
                        if (!isLoggedIn) {
                            Button(
                                onClick = { navController.navigate("auth") },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Sign In", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            IconButton(
                                onClick = { navController.navigate("dashboard") },
                                modifier = Modifier.padding(end = 8.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(Icons.Default.Dashboard, "Dashboard")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    if (!isLoggedIn) PromotionBanner(onRegisterClick = { navController.navigate("auth") }) else MemberWelcomeBanner()
                }

                item {
                    CategoryFilterBar(categories = mainCategories, selectedCategory = selectedMainCategory) {
                        selectedMainCategory = it
                        selectedSubCategory = if (it == "University Courses") "All Departments" else "All Genres"
                    }
                }

                item {
                    AnimatedVisibility(visible = subCategoriesMap.containsKey(selectedMainCategory)) {
                        CategoryFilterBar(
                            categories = subCategoriesMap[selectedMainCategory] ?: emptyList(),
                            selectedCategory = selectedSubCategory
                        ) { selectedSubCategory = it }
                    }
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("Connecting to store...", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                } else if (error != null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Connection Error", fontWeight = FontWeight.Bold)
                                Text(error, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = onRefresh) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Retry Connection")
                                }
                            }
                        }
                    }
                } else if (filteredBooks.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No items found.", style = MaterialTheme.typography.bodyLarge)
                                Text("Try changing your filters or refresh.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(onClick = onRefresh) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Refresh Store")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredBooks) { book ->
                        BookDisplayCard(book = book, isLoggedIn = isLoggedIn) {
                            navController.navigate("bookDetails/${book.id}")
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun WavyBackground(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Improved visibility for Light Mode
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val waveColor1 = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE) 
    val waveColor2 = if (isDarkTheme) Color(0xFF334155) else Color(0xFFBFDBFE) 

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        
        val width = size.width
        val height = size.height
        
        // Vertical Wave 1 (flowing top to bottom on the RIGHT side)
        val path1 = Path().apply {
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) {
                val relativeY = y.toFloat() / height
                val x = width * 0.6f + Math.sin((relativeY * 1.5 * Math.PI + phase).toDouble()).toFloat() * 100f
                lineTo(x, y.toFloat())
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)
        
        // Vertical Wave 2 (opposite phase, slightly different curve, RIGHT side)
        val path2 = Path().apply {
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) {
                val relativeY = y.toFloat() / height
                val x = width * 0.75f + Math.sin((relativeY * 2.5 * Math.PI - phase * 0.8f).toDouble()).toFloat() * 60f
                lineTo(x, y.toFloat())
            }
            lineTo(width, height)
            close()
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.7f))
    }
}

@Composable
fun CategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@Composable
fun BookDisplayCard(book: Book, isLoggedIn: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp), 
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(width = 90.dp, height = 120.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "by ${book.author}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val priceText = if (book.price == 0.0) "FREE" else "£${String.format(Locale.US, "%.2f", book.price)}"
                    Text(text = priceText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = if (book.price == 0.0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)
                    if (isLoggedIn && book.price > 0.0) {
                        val discountPrice = String.format(Locale.US, "%.2f", book.price * 0.9)
                        Text(text = " £$discountPrice", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(text = if (book.isAudioBook) "Audio" else book.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun PromotionBanner(onRegisterClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))).padding(24.dp)) {
            Column {
                Text("University Store", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Get courses and books with 10% student discount.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRegisterClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary)) { Text("Get Started") }
            }
        }
    }
}

@Composable
fun MemberWelcomeBanner() {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Student Status: Active", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text("Your 10% discount is applied automatically.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
