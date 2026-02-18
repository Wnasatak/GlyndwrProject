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
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import kotlinx.coroutines.launch

/**
 * PdfReaderScreen.kt
 *
 * This file implements the primary user interface for the enhanced PDF reading experience.
 * It provides a highly stable, feature-rich viewer that supports custom reading modes, 
 * brightness control, and interactive zoom/pan gestures. 
 */

/**
 * PdfReaderScreen Composable
 *
 * The main container for the PDF reader. It orchestrates the top navigation bar, 
 * the bottom pagination controls, and the central document viewing area.
 *
 * Key features:
 * - **Extreme Stability:** Uses the `key(bookId)` pattern to force a clean UI reset when switching books.
 * - **Immersive Modes:** Supports a toggleable full-screen state that hides the UI for focused reading.
 * - **Reactive State:** Syncs directly with the `PdfViewModel` for reading modes and zoom levels.
 * - **Lifecycle Management:** Automatically loads book data on entry and performs clean-up on disposal.
 *
 * @param bookId Unique identifier for the PDF book to be loaded.
 * @param onBack Callback to return to the previous screen.
 * @param currentTheme The active application theme.
 * @param onThemeChange Callback to update the application theme.
 * @param viewModel The state holder for PDF rendering logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    bookId: String,                   
    onBack: () -> Unit,               
    currentTheme: Theme,             
    onThemeChange: (Theme) -> Unit,        
    viewModel: PdfViewModel = viewModel()
) {
    // --- STATE OBSERVATION --- //
    val uiState by viewModel.uiState.collectAsState()
    val readingMode by viewModel.readingMode.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()
    val isDarkTheme = currentTheme == Theme.DARK || currentTheme == Theme.DARK_BLUE
    
    // UI Visibility flags.
    var showSettings by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    
    // Persistent scroll state for the document list.
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // --- LIFECYCLE HOOKS --- //
    // Triggers loading when the bookId changes and ensures state reset when leaving.
    DisposableEffect(bookId) {
        viewModel.loadBook(bookId, isDarkTheme)
        onDispose {
            viewModel.reset() // Prevent memory leaks by clearing cached bitmaps.
        }
    }

    // Tracks the current page index based on the scroll position.
    var currentPage by remember { mutableIntStateOf(1) }
    LaunchedEffect(listState.firstVisibleItemIndex) {
        currentPage = listState.firstVisibleItemIndex + 1
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- BACKGROUND LAYER --- //
        if (!isFullScreen) {
            // Show decorative wavy background in normal mode.
            HorizontalWavyBackground(isDarkTheme = isDarkTheme)
        } else {
            // Solid, eye-friendly background in full-screen mode based on reading mode.
            Box(modifier = Modifier.fillMaxSize().background(when(readingMode) {
                PdfReadingMode.INVERTED -> Color.Black
                PdfReadingMode.SEPIA -> Color(0xFFF4ECD8)
                else -> Color(0xFF323639)
            }))
        }

        Scaffold(
            containerColor = Color.Transparent, // Let the background layers show through.
            topBar = {
                // Top Bar slides out of view during full-screen reading.
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
                                // Displays live progress indicator (e.g., "Page 5 of 120").
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
                            // Full-screen toggle.
                            IconButton(onClick = { isFullScreen = true }) {
                                Icon(Icons.Default.Fullscreen, "Enter Full Screen")
                            }
                            
                            ThemeToggleButton(
                                currentTheme = currentTheme,
                                onThemeChange = onThemeChange
                            )

                            // Open advanced reader settings (Reading mode, Brightness).
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    )
                }
            },
            bottomBar = {
                // Bottom bar provides rapid navigation via a slider.
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
                            // Proportional slider for document scrubbing.
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
            
            // --- MAIN CONTENT KEYING --- //
            // Forcing a fresh composition when bookId changes prevents rendering stale pages.
            key(bookId) {
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
                            
                            // Exit full-screen floating action button.
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
        }

        // Modal sheet for accessibility and comfort settings.
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

/**
 * PdfContent Composable
 *
 * Handles the actual rendering of PDF page bitmaps in a highly interactive scrollable list.
 * It integrates gesture-based zooming and panning for a natural reading feel.
 */
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
    // --- GESTURE STATE --- //
    var gestureScale by remember { mutableStateOf(1f) } // Dynamic pinch-to-zoom scale.
    var offset by remember { mutableStateOf(Offset.Zero) } // Current pan coordinates.
    
    // Logic to handle multi-touch interaction.
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        gestureScale = (gestureScale * zoomChange).coerceIn(1f, 5f) // Limit zoom range.
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
                    scaleX = gestureScale * zoomScale, // Multiply base zoom by gesture zoom.
                    scaleY = gestureScale * zoomScale, 
                    translationX = offset.x, 
                    translationY = offset.y
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pageCount) { index ->
                // Fetch the correctly filtered bitmap for the current reading mode.
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
        
        // --- BACKLIGHT SIMULATION --- //
        // A semi-transparent black layer that darkens the entire screen to simulate brightness control.
        if (brightness < 1.0f) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 1.0f - brightness)))
        }
    }
}

/**
 * ReaderSettingsSheet Composable
 *
 * A modal bottom sheet providing granular controls for the reading environment.
 */
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
            
            // --- MODE SELECTOR --- //
            Text("Reading Mode", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(selected = readingMode == PdfReadingMode.NORMAL, onClick = { onModeChange(PdfReadingMode.NORMAL) }, label = { Text("Normal") })
                FilterChip(selected = readingMode == PdfReadingMode.INVERTED, onClick = { onModeChange(PdfReadingMode.INVERTED) }, label = { Text("Night") })
                FilterChip(selected = readingMode == PdfReadingMode.SEPIA, onClick = { onModeChange(PdfReadingMode.SEPIA) }, label = { Text("Sepia") })
            }
            
            Spacer(Modifier.height(16.dp))
            
            // --- BRIGHTNESS SLIDER --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BrightnessLow, null, Modifier.size(20.dp))
                Slider(value = brightness, onValueChange = onBrightnessChange, valueRange = 0.2f..1.0f, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                Icon(Icons.Default.BrightnessHigh, null, Modifier.size(20.dp))
            }
            
            Spacer(Modifier.height(16.dp))
            
            // --- ZOOM SCALE SLIDER --- //
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TextFields, null, Modifier.size(16.dp))
                Slider(value = zoomScale, onValueChange = onZoomChange, valueRange = 1.0f..2.0f, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                Icon(Icons.Default.TextFields, null, Modifier.size(24.dp))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
