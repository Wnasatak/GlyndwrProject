package assignment1.krzysztofoko.s16001089.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayAudio: (Book) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var book by remember { mutableStateOf<Book?>(null) }
    var loading by remember { mutableStateOf(true) }
    var isOwned by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(bookId, user) {
        db.collection("books").document(bookId).get()
            .addOnSuccessListener { document ->
                val fetchedBook = document.toObject(Book::class.java)?.copy(id = document.id)
                book = fetchedBook
                loading = false
                
                if (fetchedBook?.price == 0.0) {
                    isOwned = true
                } else if (user != null) {
                    db.collection("users").document(user.uid)
                        .collection("purchases").document(bookId).get()
                        .addOnSuccessListener { purchaseDoc ->
                            if (purchaseDoc.exists()) isOwned = true
                        }
                }
            }
            .addOnFailureListener { loading = false }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MediumTopAppBar(
                title = { Text(book?.title ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            book?.let { currentBook ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentBook.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = currentBook.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = "by ${currentBook.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(onClick = { }, label = { Text(currentBook.category) })
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(onClick = { }, label = { Text(if (currentBook.isAudioBook) "Audio" else "Item") })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = currentBook.description, style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(32.dp))

                    val canAccessFreeAudio = currentBook.isAudioBook && currentBook.price == 0.0

                    if (isOwned || canAccessFreeAudio) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (downloadProgress < 1f && !isDownloading) {
                                OutlinedButton(
                                    onClick = {
                                        isDownloading = true
                                        scope.launch {
                                            while (downloadProgress < 1f) {
                                                delay(300)
                                                downloadProgress += 0.1f
                                            }
                                            isDownloading = false
                                            snackbarHostState.showSnackbar("Downloaded to your device!")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Download Offline")
                                }
                            } else if (isDownloading) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                    )
                                    Text("Downloading... ${(downloadProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    enabled = false
                                ) {
                                    Icon(Icons.Default.Check, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Downloaded")
                                }
                            }

                            if (currentBook.isAudioBook) {
                                Button(
                                    onClick = { onPlayAudio(currentBook) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Listen Now")
                                }
                            } else {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Open PDF Content")
                                }
                            }
                        }
                    } else if (!isLoggedIn) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Student & Member Access", fontWeight = FontWeight.Bold)
                                Text("Please sign in to buy this content and unlock your student discount.")
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = onLoginRequired) { Text("Sign In to Buy") }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (user != null) {
                                    val purchase = mapOf("bookId" to bookId, "timestamp" to System.currentTimeMillis())
                                    db.collection("users").document(user.uid).collection("purchases").document(bookId).set(purchase)
                                        .addOnSuccessListener {
                                            isOwned = true
                                            scope.launch { snackbarHostState.showSnackbar("Purchase successful!") }
                                        }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val price = String.format(Locale.US, "%.2f", currentBook.price * 0.9)
                            Text("Buy Now for £$price")
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
