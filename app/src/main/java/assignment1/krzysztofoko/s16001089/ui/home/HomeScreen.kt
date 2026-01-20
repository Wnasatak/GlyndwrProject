package assignment1.krzysztofoko.s16001089.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.BookItemCard
import assignment1.krzysztofoko.s16001089.ui.components.CategoryChip
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
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
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedMainCategory by remember { mutableStateOf("All") }
    var selectedSubCategory by remember { mutableStateOf("All Genres") }
    var wishlistIds by remember { mutableStateOf<Set<String>>(setOf()) }

    val mainCategories = listOf("All", "University Courses", "University Gear", "Books", "Audio Books")
    val subCategoriesMap = mapOf(
        "Books" to listOf("All Genres", "Technology", "Cooking", "Fantasy", "Mystery", "Self-Help"),
        "Audio Books" to listOf("All Genres", "Self-Help", "Technology", "Cooking", "Mystery"),
        "University Courses" to listOf("All Departments", "Science", "Business", "Technology")
    )

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val user = auth.currentUser
            if (user != null) {
                db.collection("users").document(user.uid).collection("wishlist")
                    .addSnapshotListener { snapshot, _ ->
                        wishlistIds = snapshot?.documents?.map { it.id }?.toSet() ?: setOf()
                    }
            }
        } else {
            wishlistIds = setOf()
        }
    }

    val filteredBooks = remember(selectedMainCategory, selectedSubCategory, allBooks) {
        allBooks.filter { book ->
            val matchMain = if (selectedMainCategory == "All") true else book.mainCategory.equals(selectedMainCategory, ignoreCase = true)
            val matchSub = if (selectedSubCategory.contains("All", ignoreCase = true)) true else book.category.equals(selectedSubCategory, ignoreCase = true)
            matchMain && matchSub
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = "file:///android_asset/images/media/GlyndwrUniversity.jpg",
                                contentDescription = "Logo",
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Glyndŵr Store",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme") }
                        IconButton(onClick = onAboutClick) { Icon(Icons.Default.Info, "About") }
                        if (!isLoggedIn) {
                            IconButton(onClick = { navController.navigate("auth") }) { Icon(Icons.Default.Login, "Login") }
                        } else {
                            IconButton(onClick = { navController.navigate("dashboard") }) { Icon(Icons.Default.Dashboard, "Dashboard") }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                )
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item { if (!isLoggedIn) PromotionBanner { navController.navigate("auth") } else MemberWelcomeBanner() }
                item { CategoryFilterBar(categories = mainCategories, selectedCategory = selectedMainCategory) { selectedMainCategory = it; selectedSubCategory = if (it == "University Courses") "All Departments" else "All Genres" } }
                item { 
                    AnimatedVisibility(visible = subCategoriesMap.containsKey(selectedMainCategory)) {
                        CategoryFilterBar(categories = subCategoriesMap[selectedMainCategory] ?: emptyList(), selectedCategory = selectedSubCategory) { selectedSubCategory = it }
                    }
                }

                if (isLoading) {
                    item { Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(16.dp)); Text("Connecting...", style = MaterialTheme.typography.labelSmall) } } }
                } else if (error != null) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)); Spacer(Modifier.height(8.dp)); Text("Error", fontWeight = FontWeight.Bold); Button(onClick = onRefresh) { Text("Retry") } } } }
                } else {
                    items(filteredBooks) { book -> 
                        val isLiked = wishlistIds.contains(book.id)
                        BookItemCard(
                            book = book,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onClick = { navController.navigate("bookDetails/${book.id}") },
                            trailingContent = {
                                if (isLoggedIn) {
                                    IconButton(onClick = {
                                        val user = auth.currentUser ?: return@IconButton
                                        val wishlistRef = db.collection("users").document(user.uid).collection("wishlist").document(book.id)
                                        if (isLiked) {
                                            wishlistRef.delete().addOnSuccessListener { scope.launch { snackbarHostState.showSnackbar("Removed from favorites") } }
                                        } else {
                                            wishlistRef.set(mapOf("addedAt" to System.currentTimeMillis())).addOnSuccessListener { scope.launch { snackbarHostState.showSnackbar("Added to favorites!") } }
                                        }
                                    }, modifier = Modifier.size(24.dp)) {
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
                                    if (book.price == 0.0) {
                                        Text(text = "FREE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                                    } else if (isLoggedIn) {
                                        val discountPrice = String.format(Locale.US, "%.2f", book.price * 0.9)
                                        Text(
                                            text = "£${String.format(Locale.US, "%.2f", book.price)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.2f))
                                        ) {
                                            Text(
                                                text = "£$discountPrice", 
                                                style = MaterialTheme.typography.titleMedium, 
                                                color = Color(0xFF2E7D32), 
                                                fontWeight = FontWeight.Black, 
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Text(text = "£${String.format(Locale.US, "%.2f", book.price)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun PromotionBanner(onRegisterClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("University Store", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Exclusive 10% student discount on all courses, gear, and books.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onRegisterClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Get Started", fontWeight = FontWeight.Bold) }
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
            Column { Text("Student Status: Active", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge); Text("Your 10% discount is applied.", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun CategoryFilterBar(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            CategoryChip(category = category, isSelected = selectedCategory == category, onSelected = { onCategorySelected(category) })
        }
    }
}
