package assignment1.krzysztofoko.s16001089.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    bookId: String,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    var book by remember { mutableStateOf<Book?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(bookId) {
        book = db.bookDao().getBookById(bookId)
        loading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(
            isDarkTheme = isDarkTheme,
            wave1HeightFactor = 0.45f,
            wave2HeightFactor = 0.65f,
            wave1Amplitude = 80f,
            wave2Amplitude = 100f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = book?.title ?: AppConstants.TEXT_READING,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (book != null) {
                                Text(
                                    text = "Page 1 of 42", // Simulation
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.BTN_CLOSE)
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme")
                        }
                        IconButton(onClick = { /* Settings / Font size */ }) {
                            Icon(Icons.Default.TextFields, "Text Settings")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* Previous Page */ }) {
                            Icon(Icons.Default.ChevronLeft, AppConstants.BTN_PREVIOUS)
                        }
                        
                        Slider(
                            value = 0.1f, 
                            onValueChange = {}, 
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                        )
                        
                        IconButton(onClick = { /* Next Page */ }) {
                            Icon(Icons.Default.ChevronRight, AppConstants.BTN_NEXT)
                        }
                    }
                }
            }
        ) { padding ->
            if (loading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                book?.let { currentBook ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Simulated PDF Page
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = currentBook.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Chapter 1: Introduction\n\n" + 
                                           currentBook.description + "\n\n" +
                                           "This is a professional reading view for your digital content. " +
                                           "The PDF rendering engine will display the high-quality pages here. " +
                                           "You can navigate through chapters, adjust brightness, and save your progress automatically.\n\n" +
                                           "The Glyndŵr Store ensures that your reading experience is seamless across all devices.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.DarkGray,
                                    lineHeight = 28.sp
                                )
                                Spacer(modifier = Modifier.height(300.dp)) // Simulation of a long page
                            }
                        }
                    }
                }
            }
        }
    }
}
