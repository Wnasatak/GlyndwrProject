package assignment1.krzysztofoko.s16001089.ui.details.pdf

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import assignment1.krzysztofoko.s16001089.ui.components.PdfReadingMode
import kotlinx.coroutines.launch

/**
 * Main Screen for the Enhanced PDF Book Reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    bookId: String,                   
    onBack: () -> Unit,               
    isDarkTheme: Boolean,             
    onToggleTheme: () -> Unit,        
    // Fix: Key the ViewModel to the bookId to ensure a fresh instance for every book
    viewModel: PdfViewModel = viewModel(key = "pdf_reader_$bookId")
) {
    val uiState by viewModel.uiState.collectAsState()
    val readingMode by viewModel.readingMode.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId, isDarkTheme)
    }

    var currentPage by remember { mutableIntStateOf(1) }
    LaunchedEffect(listState.firstVisibleItemIndex) {
        currentPage = listState.firstVisibleItemIndex + 1
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isFullScreen) {
            HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        } else {
            Box(modifier = Modifier.fillMaxSize().background(when(readingMode) {
                PdfReadingMode.INVERTED -> Color.Black
                PdfReadingMode.SEPIA -> Color(0xFFF4ECD8)
                else -> Color(0xFF323639)
            }))
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AnimatedVisibility(
                    visible = !isFullScreen,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.Start) {
                                val title = (uiState as? PdfUiState.Ready)?.book?.title ?: AppConstants.TEXT_READING
                                Text(
                                    text = title, 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (uiState is PdfUiState.Ready) {
                                    Text(
                                        text = "Page $currentPage of ${(uiState as PdfUiState.Ready).pageCount}", 
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
                            IconButton(onClick = { isFullScreen = true }) {
                                Icon(Icons.Default.Fullscreen, "Enter Full Screen")
                            }
                            IconButton(onClick = onToggleTheme) {
                                Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                            }
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = !isFullScreen && uiState is PdfUiState.Ready,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val readyState = uiState as PdfUiState.Ready
                    BottomAppBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { scope.launch { if (currentPage > 1) listState.animateScrollToItem(currentPage - 2) } }) {
                                Icon(Icons.Default.ChevronLeft, null)
                            }
                            Slider(
                                value = if (readyState.pageCount > 1) (currentPage - 1).toFloat() / (readyState.pageCount - 1) else 0f,
                                onValueChange = { scope.launch { listState.scrollToItem((it * (readyState.pageCount - 1)).toInt()) } },
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                            )
                            IconButton(onClick = { scope.launch { if (currentPage < readyState.pageCount) listState.animateScrollToItem(currentPage) } }) {
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            val contentPadding = if (isFullScreen) PaddingValues(0.dp) else padding
            
            when (uiState) {
                is PdfUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is PdfUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text((uiState as PdfUiState.Error).message, color = Color.Red) }
                is PdfUiState.Ready -> {
                    val readyState = uiState as PdfUiState.Ready
                    Box(modifier = Modifier.fillMaxSize()) {
                        PdfContent(
                            viewModel = viewModel,
                            pageCount = readyState.pageCount,
                            readingMode = readingMode,
                            zoomScale = zoomScale,
                            brightness = brightness,
                            listState = listState,
                            modifier = Modifier.padding(contentPadding)
                        )
                        
                        if (isFullScreen) {
                            SmallFloatingActionButton(
                                onClick = { isFullScreen = false },
                                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            ) {
                                Icon(Icons.Default.FullscreenExit, "Exit Full Screen")
                            }
                        }
                    }
                }
            }
        }

        if (showSettings) {
            ReaderSettingsSheet(
                readingMode = readingMode,
                brightness = brightness,
                zoomScale = zoomScale,
                onModeChange = { viewModel.updateReadingMode(it) },
                onBrightnessChange = { viewModel.updateBrightness(it) },
                onZoomChange = { viewModel.updateZoom(it) },
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
fun PdfContent(
    viewModel: PdfViewModel,
    pageCount: Int,
    readingMode: PdfReadingMode,
    zoomScale: Float,
    brightness: Float,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    var gestureScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        gestureScale = (gestureScale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Box(modifier = modifier.fillMaxSize().transformable(transformState)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
                .background(when(readingMode) {
                    PdfReadingMode.INVERTED -> Color.Black
                    PdfReadingMode.SEPIA -> Color(0xFFF4ECD8)
                    else -> Color(0xFF323639)
                })
                .graphicsLayer(
                    scaleX = gestureScale * zoomScale, 
                    scaleY = gestureScale * zoomScale, 
                    translationX = offset.x, 
                    translationY = offset.y
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pageCount) { index ->
                val bitmap = remember(index, readingMode) { viewModel.getPage(index, readingMode) }
                bitmap?.let {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = when(readingMode) {
                            PdfReadingMode.INVERTED -> Color.DarkGray
                            PdfReadingMode.SEPIA -> Color(0xFFFDF6E3)
                            else -> Color.White
                        }),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
        }
        
        if (brightness < 1.0f) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 1.0f - brightness)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    readingMode: PdfReadingMode,
    brightness: Float,
    zoomScale: Float,
    onModeChange: (PdfReadingMode) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onZoomChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Reader Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            Text("Reading Mode", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(selected = readingMode == PdfReadingMode.NORMAL, onClick = { onModeChange(PdfReadingMode.NORMAL) }, label = { Text("Normal") })
                FilterChip(selected = readingMode == PdfReadingMode.INVERTED, onClick = { onModeChange(PdfReadingMode.INVERTED) }, label = { Text("Night") })
                FilterChip(selected = readingMode == PdfReadingMode.SEPIA, onClick = { onModeChange(PdfReadingMode.SEPIA) }, label = { Text("Sepia") })
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BrightnessLow, null, Modifier.size(20.dp))
                Slider(value = brightness, onValueChange = onBrightnessChange, valueRange = 0.2f..1.0f, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                Icon(Icons.Default.BrightnessHigh, null, Modifier.size(20.dp))
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TextFields, null, Modifier.size(16.dp))
                Slider(value = zoomScale, onValueChange = onZoomChange, valueRange = 1.0f..2.0f, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                Icon(Icons.Default.TextFields, null, Modifier.size(24.dp))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
