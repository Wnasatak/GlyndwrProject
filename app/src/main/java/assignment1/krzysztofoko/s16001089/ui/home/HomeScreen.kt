package assignment1.krzysztofoko.s16001089.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.flowOf
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
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Animation for the logo to spin once on entry
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    var selectedMainCategory by remember { mutableStateOf("All") }
    var selectedSubCategory by remember { mutableStateOf("All Genres") }
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    val mainCategories = listOf("All", "Free", "University Courses", "University Gear", "Books", "Audio Books")
    val subCategoriesMap = mapOf(
        "Books" to listOf("All Genres", "Technology", "Cooking", "Fantasy", "Mystery", "Self-Help"),
        "Audio Books" to listOf("All Genres", "Self-Help", "Technology", "Cooking", "Mystery"),
        "University Courses" to listOf("All Departments", "Science", "Business", "Technology")
    )
    
    val userId = auth.currentUser?.uid ?: ""

    val wishlistIds by remember(isLoggedIn, userId) {
        if (isLoggedIn && userId.isNotEmpty()) {
            db.userDao().getWishlistIds(userId)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val purchasedIds by remember(isLoggedIn, userId) {
        if (isLoggedIn && userId.isNotEmpty()) {
            db.userDao().getPurchaseIds(userId)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val filteredBooks = remember(selectedMainCategory, selectedSubCategory, searchQuery, allBooks) {
        allBooks.filter { book ->
            val matchMain = when (selectedMainCategory) {
                "All" -> true
                "Free" -> book.price == 0.0
                else -> book.mainCategory.equals(selectedMainCategory, ignoreCase = true)
            }
            val matchSub = if (selectedSubCategory.contains("All", ignoreCase = true) || selectedMainCategory == "Free") true 
                           else book.category.equals(selectedSubCategory, ignoreCase = true)
            
            val matchQuery = if (searchQuery.isEmpty()) true 
                             else book.title.contains(searchQuery, ignoreCase = true) || 
                                  book.author.contains(searchQuery, ignoreCase = true) ||
                                  book.category.contains(searchQuery, ignoreCase = true)
            
            matchMain && matchSub && matchQuery
        }
    }

    // Suggestions based on search query
    val suggestions = remember(searchQuery, allBooks) {
        if (searchQuery.length < 2) emptyList()
        else allBooks.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.author.contains(searchQuery, ignoreCase = true)
        }.take(5)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { 
            if (isSearchVisible) {
                isSearchVisible = false
                searchQuery = ""
            }
        }
    ) {
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
                                modifier = Modifier
                                    .size(32.dp)
                                    .graphicsLayer { rotationZ = rotation.value }
                                    .clip(CircleShape),
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
                        TopBarSearchAction(isSearchVisible = isSearchVisible) {
                            isSearchVisible = true
                        }
                        IconButton(onClick = { 
                            isSearchVisible = false
                            onToggleTheme() 
                        }) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme") }
                        IconButton(onClick = { 
                            isSearchVisible = false
                            onAboutClick() 
                        }) { Icon(Icons.Default.Info, "About") }
                        if (!isLoggedIn) {
                            IconButton(onClick = { 
                                isSearchVisible = false
                                navController.navigate("auth") 
                            }) { Icon(Icons.AutoMirrored.Filled.Login, "Login") }
                        } else {
                            IconButton(onClick = { 
                                isSearchVisible = false
                                navController.navigate("dashboard") 
                            }) { Icon(Icons.Default.Dashboard, "Dashboard") }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    item { 
                        if (!isLoggedIn) {
                            PromotionBanner { 
                                isSearchVisible = false
                                navController.navigate("auth") 
                            }
                        } else {
                            MemberWelcomeBanner()
                        }
                    }
                    
                    item { 
                        MainCategoryFilterBar(
                            categories = mainCategories, 
                            selectedCategory = selectedMainCategory 
                        ) { 
                            isSearchVisible = false
                            selectedMainCategory = it
                            selectedSubCategory = if (it == "University Courses") "All Departments" else "All Genres" 
                        }
                    }
                    
                    item { 
                        AnimatedVisibility(visible = subCategoriesMap.containsKey(selectedMainCategory)) {
                            SubCategoryFilterBar(
                                categories = subCategoriesMap[selectedMainCategory] ?: emptyList(), 
                                selectedCategory = selectedSubCategory
                            ) { 
                                isSearchVisible = false
                                selectedSubCategory = it 
                            }
                        }
                    }

                    if (isLoading) {
                        item { 
                            Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) { 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                                    CircularProgressIndicator(); 
                                    Spacer(Modifier.height(16.dp)); 
                                    Text("Loading Local Data...", style = MaterialTheme.typography.labelSmall) 
                                } 
                            } 
                        }
                    } else if (error != null) {
                        item { 
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                                    Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)); 
                                    Spacer(Modifier.height(8.dp)); 
                                    Text("Error: $error", fontWeight = FontWeight.Bold); 
                                    Button(onClick = onRefresh) { Text("Retry") } 
                                } 
                            } 
                        }
                    } else {
                        val booksToShow = filteredBooks
                        if (booksToShow.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp).clickable { isSearchVisible = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No results found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                                }
                            }
                        } else {
                            items(booksToShow) { book -> 
                                val isLiked = wishlistIds.contains(book.id)
                                val isPurchased = purchasedIds.contains(book.id)
                                
                                BookItemCard(
                                    book = book,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    onClick = { 
                                        isSearchVisible = false
                                        navController.navigate("bookDetails/${book.id}") 
                                    },
                                    trailingContent = {
                                        if (isLoggedIn) {
                                            IconButton(onClick = {
                                                isSearchVisible = false
                                                scope.launch {
                                                    if (isLiked) {
                                                        db.userDao().removeFromWishlist(userId, book.id)
                                                        snackbarHostState.showSnackbar("Removed from favorites")
                                                    } else {
                                                        db.userDao().addToWishlist(WishlistItem(userId, book.id))
                                                        snackbarHostState.showSnackbar("Added to favorites!")
                                                    }
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
                                            if (isPurchased) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (book.price > 0) {
                                                        Surface(
                                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                        ) {
                                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                                Spacer(Modifier.width(6.dp))
                                                                Text(text = "Purchased", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                                                            }
                                                        }
                                                        Spacer(Modifier.width(8.dp))
                                                        IconButton(
                                                            onClick = { 
                                                                isSearchVisible = false
                                                                navController.navigate("invoiceCreating/${book.id}") 
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, "Invoice", tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                    } else {
                                                        Surface(
                                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                        ) {
                                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.LibraryAddCheck, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                                Spacer(Modifier.width(6.dp))
                                                                Text(text = "In Library", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                                                            }
                                                        }
                                                        Spacer(Modifier.width(8.dp))
                                                        IconButton(
                                                            onClick = {
                                                                if (isLoggedIn) {
                                                                    isSearchVisible = false
                                                                    scope.launch {
                                                                        db.userDao().deletePurchase(userId, book.id)
                                                                        snackbarHostState.showSnackbar("Removed from library")
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.DeleteOutline, "Remove", tint = MaterialTheme.colorScheme.error)
                                                        }
                                                    }
                                                }
                                            } else if (book.price == 0.0) {
                                                Text(text = "FREE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                                            } else if (isLoggedIn) {
                                                val discountPrice = String.format(Locale.US, "%.2f", book.price * 0.9)
                                                Text(
                                                    text = "£${String.format(Locale.US, "%.2f", book.price)}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                                                    color = Color.Gray
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
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
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }

                // Overlay Search - This will now float OVER the content without pushing it down
                HomeSearchSection(
                    isSearchVisible = isSearchVisible,
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onCloseClick = {
                        isSearchVisible = false
                        searchQuery = ""
                    },
                    suggestions = suggestions,
                    onSuggestionClick = { book ->
                        searchQuery = book.title
                        isSearchVisible = false
                        navController.navigate("bookDetails/${book.id}")
                    },
                    modifier = Modifier
                        .padding(top = padding.calculateTopPadding())
                        .zIndex(10f) // Ensure it's on top
                )
            }
        }
    }
}
