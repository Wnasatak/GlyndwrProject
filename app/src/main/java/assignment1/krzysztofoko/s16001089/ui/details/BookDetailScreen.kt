package assignment1.krzysztofoko.s16001089.ui.details

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    initialBook: Book? = null,
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onPlayAudio: (Book) -> Unit,
    onReadBook: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var book by remember { mutableStateOf(initialBook) }
    var loading by remember { mutableStateOf(true) }
    var isOwned by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    
    var showOrderFlow by remember { mutableStateOf(false) }

    LaunchedEffect(bookId, user) {
        loading = true
        db.collection("books").document(bookId).get()
            .addOnSuccessListener { document ->
                val fetchedBook = document.toObject(Book::class.java)?.copy(id = document.id)
                book = fetchedBook
                
                if (fetchedBook != null) {
                    if (fetchedBook.price == 0.0) {
                        isOwned = true
                        loading = false
                    } else if (user != null) {
                        db.collection("users").document(user.uid)
                            .collection("purchases").document(bookId).get()
                            .addOnSuccessListener { purchaseDoc ->
                                isOwned = purchaseDoc.exists()
                                loading = false
                            }
                            .addOnFailureListener { loading = false }
                    } else {
                        loading = false
                    }
                } else {
                    loading = false
                }
            }
            .addOnFailureListener { loading = false }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DetailWavyBackground(isDarkTheme = isDarkTheme)
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { 
                        Text(
                            text = book?.title ?: "Item Details",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
            }
        ) { padding ->
            if (loading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (book == null) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("Item details not available.", color = Color.Gray)
                        TextButton(onClick = onBack) { Text("Go Back") }
                    }
                }
            } else {
                book?.let { currentBook ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shadowElevation = 8.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentBook.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = currentBook.title, 
                                    style = MaterialTheme.typography.headlineMedium, 
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "by ${currentBook.author}", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(
                                        onClick = {}, 
                                        label = { Text(currentBook.category) },
                                        leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                                    )
                                    AssistChip(
                                        onClick = {}, 
                                        label = { Text(if (currentBook.isAudioBook) "Audio Content" else "Reading Material") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(text = "About this item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentBook.description, 
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    val canAccessFree = currentBook.price == 0.0

                                    if (isOwned || canAccessFree) {
                                        ActionSectionOwned(
                                            book = currentBook, 
                                            isDownloading = isDownloading, 
                                            progress = downloadProgress, 
                                            onPlay = onPlayAudio, 
                                            onRead = { onReadBook(currentBook.id) },
                                            onDownload = {
                                                isDownloading = true
                                                scope.launch {
                                                    while (downloadProgress < 1f) {
                                                        delay(300)
                                                        downloadProgress += 0.1f
                                                    }
                                                    isDownloading = false
                                                    snackbarHostState.showSnackbar("Available offline!")
                                                }
                                            }
                                        )
                                    } else if (!isLoggedIn) {
                                        LoginPromptSection(onLoginRequired)
                                    } else {
                                        PurchaseSection(currentBook, { showOrderFlow = true })
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

        if (showOrderFlow && book != null) {
            OrderFlowDialog(
                book = book!!,
                userId = user?.uid ?: "",
                onDismiss = { showOrderFlow = false },
                onComplete = {
                    isOwned = true
                    showOrderFlow = false
                    scope.launch { snackbarHostState.showSnackbar("Order completed successfully!") }
                }
            )
        }
    }
}

@Composable
fun ActionSectionOwned(
    book: Book, 
    isDownloading: Boolean, 
    progress: Float, 
    onPlay: (Book) -> Unit, 
    onRead: () -> Unit, 
    onDownload: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (progress < 1f && !isDownloading) {
            OutlinedButton(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CloudDownload, null)
                Spacer(Modifier.width(8.dp))
                Text("Save for Offline Use")
            }
        } else if (isDownloading) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
        }

        Button(
            onClick = { if (book.isAudioBook) onPlay(book) else onRead() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(if (book.isAudioBook) Icons.Default.PlayCircleFilled else Icons.Default.AutoStories, null)
            Spacer(Modifier.width(8.dp))
            Text(if (book.isAudioBook) "Listen to Audio" else "Read Online")
        }
    }
}

@Composable
fun DetailWavyBackground(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "detailWave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val waveColor1 = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE) 
    val waveColor2 = if (isDarkTheme) Color(0xFF334155) else Color(0xFFBFDBFE) 

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height
        
        val path1 = Path().apply {
            moveTo(0f, height * 0.4f)
            for (x in 0..width.toInt() step 15) {
                val relX = x.toFloat() / width
                val y = height * 0.45f + Math.sin((relX * Math.PI + phase).toDouble()).toFloat() * 80f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path1, color = waveColor1)

        val path2 = Path().apply {
            moveTo(0f, height * 0.6f)
            for (x in 0..width.toInt() step 15) {
                val relX = x.toFloat() / width
                val y = height * 0.65f + Math.sin((relX * 1.5 * Math.PI - phase * 0.7f).toDouble()).toFloat() * 100f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.8f))
    }
}

@Composable
fun PurchaseSection(book: Book, onOrderClick: () -> Unit) {
    val discountedPrice = book.price * 0.9
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "£${String.format(Locale.US, "%.2f", book.price)}",
                style = MaterialTheme.typography.titleMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                color = Color.Gray
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "£${String.format(Locale.US, "%.2f", discountedPrice)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
            Text("STUDENT PRICE (-10%)", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOrderClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.ShoppingBag, null)
            Spacer(Modifier.width(12.dp))
            Text("Complete Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoginPromptSection(onLogin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text("Student Discount Available", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Sign in to unlock exclusive student pricing and access your digital library.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { 
                Text("Sign In to Purchase") 
            }
        }
    }
}

@Composable
fun OrderFlowDialog(
    book: Book,
    userId: String,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val db = FirebaseFirestore.getInstance()
    
    var fullName by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName ?: "") }
    var paymentMethod by remember { mutableStateOf("Credit Card") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = when(step) {
                            1 -> "Order Review"
                            2 -> "Billing Info"
                            3 -> "Payment"
                            else -> "Success"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Step $step of 3", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                
                LinearProgressIndicator(
                    progress = { step / 3f },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(6.dp).clip(CircleShape)
                )

                Box(modifier = Modifier.height(300.dp)) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        }, label = "orderStepTransition"
                    ) { currentStep ->
                        when(currentStep) {
                            1 -> OrderReviewStep(book)
                            2 -> BillingInfoStep(fullName) { fullName = it }
                            3 -> PaymentStep(paymentMethod) { paymentMethod = it }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 1) {
                        OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Back") }
                    } else {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    }

                    Button(
                        onClick = {
                            if (step < 3) {
                                step++
                            } else {
                                val purchase = mapOf(
                                    "bookId" to book.id,
                                    "timestamp" to System.currentTimeMillis(),
                                    "customerName" to fullName,
                                    "paymentMethod" to paymentMethod,
                                    "pricePaid" to (book.price * 0.9)
                                )
                                db.collection("users").document(userId).collection("purchases").document(book.id).set(purchase)
                                    .addOnSuccessListener { onComplete() }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (step == 3) "Pay Now" else "Continue")
                    }
                }
            }
        }
    }
}

@Composable
fun OrderReviewStep(book: Book) {
    val discountedPrice = book.price * 0.9
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("by ${book.author}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        DetailRow("Original Price", "£${String.format(Locale.US, "%.2f", book.price)}")
        DetailRow("Student Discount", "-£${String.format(Locale.US, "%.2f", book.price * 0.1)}")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        DetailRow("Total Amount", "£${String.format(Locale.US, "%.2f", discountedPrice)}", isTotal = true)
    }
}

@Composable
fun BillingInfoStep(name: String, onNameChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Confirmation Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Please confirm the name for your digital certificate.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, null) },
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = FirebaseAuth.getInstance().currentUser?.email ?: "",
            onValueChange = {},
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            leadingIcon = { Icon(Icons.Default.Email, null) },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun PaymentStep(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        PaymentOption("Credit / Debit Card", Icons.Default.CreditCard, selected == "Credit Card") { onSelect("Credit Card") }
        PaymentOption("Apple Pay / Google Pay", Icons.Default.Wallet, selected == "Mobile Pay") { onSelect("Mobile Pay") }
        PaymentOption("University Account", Icons.Default.School, selected == "Uni Account") { onSelect("University Account") }
    }
}

@Composable
fun PaymentOption(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            Spacer(Modifier.weight(1f))
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, color = if (isTotal) MaterialTheme.colorScheme.primary else Color.Unspecified)
    }
}
