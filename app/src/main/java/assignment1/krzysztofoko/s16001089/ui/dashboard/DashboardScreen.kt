package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.BookItemCard
import assignment1.krzysztofoko.s16001089.ui.components.VerticalWavyBackground
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    
    var ownedBooks by remember { mutableStateOf<List<Book>>(listOf()) }
    var wishlistBooks by remember { mutableStateOf<List<Book>>(listOf()) }
    var loadingLibrary by remember { mutableStateOf(true) }
    var loadingWishlist by remember { mutableStateOf(true) }
    
    var userBalance by remember { mutableDoubleStateOf(0.0) }
    var selectedPaymentMethod by remember { mutableStateOf("University Account") }
    var showPaymentPopup by remember { mutableStateOf(false) }

    DisposableEffect(user) {
        if (user == null) return@DisposableEffect onDispose {}
        
        val userRef = db.collection("users").document(user.uid)
        val userListener = userRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                userBalance = snapshot.getDouble("balance") ?: 0.0
                selectedPaymentMethod = snapshot.getString("selectedPaymentMethod") ?: "University Account"
            }
        }

        val purchasesRef = db.collection("users").document(user.uid).collection("purchases")
        val purchasesListener = purchasesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val denormalized = snapshot.documents.mapNotNull { doc ->
                    if (doc.contains("title")) {
                        Book(
                            id = doc.id,
                            title = doc.getString("title") ?: "Unknown",
                            author = doc.getString("author") ?: "Unknown",
                            category = doc.getString("category") ?: "General",
                            isAudioBook = doc.getBoolean("audioBook") ?: false
                        )
                    } else null
                }
                val legacyIds = snapshot.documents.filter { !it.contains("title") }.map { it.id }
                if (legacyIds.isEmpty()) { ownedBooks = denormalized; loadingLibrary = false } 
                else {
                    db.collection("books").whereIn(FieldPath.documentId(), legacyIds).get().addOnSuccessListener { res ->
                        val fetched = res.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
                        ownedBooks = (denormalized + fetched).distinctBy { it.id }
                        loadingLibrary = false
                    }.addOnFailureListener { loadingLibrary = false }
                }
            } else { loadingLibrary = false }
        }

        val wishlistRef = db.collection("users").document(user.uid).collection("wishlist")
        val wishlistListener = wishlistRef.addSnapshotListener { snapshot, _ ->
            val ids = snapshot?.documents?.map { it.id } ?: emptyList()
            if (ids.isNotEmpty()) {
                db.collection("books").whereIn(FieldPath.documentId(), ids).get().addOnSuccessListener { res ->
                    wishlistBooks = res.documents.mapNotNull { it.toObject(Book::class.java)?.copy(id = it.id) }
                    loadingWishlist = false
                }.addOnFailureListener { loadingWishlist = false }
            } else { wishlistBooks = emptyList(); loadingWishlist = false }
        }

        onDispose {
            userListener.remove()
            purchasesListener.remove()
            wishlistListener.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalWavyBackground(isDarkTheme = isDarkTheme)
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text("My Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    actions = {
                        IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) }
                        IconButton(onClick = { navController.navigate("profile") }) { Icon(Icons.Default.Settings, "Settings") }
                        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Log Out", tint = MaterialTheme.colorScheme.error) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                )
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), Color.Transparent))).padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = Color.Transparent) {
                                val photoUrl = user?.photoUrl?.toString()
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(if (!photoUrl.isNullOrEmpty()) photoUrl else "file:///android_asset/images/users/avatars/Avatar_defult.png").crossfade(true).build(),
                                    contentDescription = "Avatar", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop,
                                    error = { Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary) } }
                                )
                            }
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Hi, ${user?.displayName?.split(" ")?.firstOrNull() ?: "Student"}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(text = user?.email ?: "Guest Mode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Surface(
                                onClick = { showPaymentPopup = true },
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Funds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        Text("£${String.format(Locale.US, "%.2f", userBalance)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                item { Text("My Digital Library", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }

                if (loadingLibrary) {
                    item { Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                } else if (ownedBooks.isEmpty()) {
                    item { EmptyLibraryPlaceholder(onBrowse = onBack) }
                } else {
                    items(ownedBooks) { book ->
                        BookItemCard(
                            book = book,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onClick = { navController.navigate("bookDetails/${book.id}") },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }

                if (wishlistBooks.isNotEmpty() || loadingWishlist) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text("My Favorites", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        if (loadingWishlist) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(2.dp))
                        } else {
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(wishlistBooks) { book ->
                                    Card(
                                        onClick = { navController.navigate("bookDetails/${book.id}") },
                                        modifier = Modifier.width(150.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Box(modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                                Icon(imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text(book.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(book.author, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(48.dp)) }
            }

            if (showPaymentPopup) {
                Dialog(onDismissRequest = { showPaymentPopup = false }) {
                    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Payment Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { showPaymentPopup = false; navController.navigate("profile") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Manage in Profile") }
                            Spacer(modifier = Modifier.height(32.dp))
                            Text("Your active payment method", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = when { selectedPaymentMethod.contains("Google") -> Icons.Default.AccountBalanceWallet; selectedPaymentMethod.contains("PayPal") -> Icons.Default.Payment; selectedPaymentMethod.contains("Uni") -> Icons.Default.School; else -> Icons.Default.CreditCard }, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(16.dp))
                                    Text(selectedPaymentMethod, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            TextButton(onClick = { showPaymentPopup = false }) { Text("Close") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryPlaceholder(onBrowse: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your library is empty", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)
        TextButton(onClick = onBrowse) { Text("Browse the Store") }
    }
}
