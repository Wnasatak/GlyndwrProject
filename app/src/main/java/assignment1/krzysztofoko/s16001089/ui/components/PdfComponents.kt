package assignment1.krzysztofoko.s16001089.ui.components

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Enum defining available visual modes for reading PDFs.
 */
enum class PdfReadingMode {
    NORMAL,    // Standard white page
    INVERTED,  // Night mode (black background, white text)
    SEPIA      // Warm yellowish tint for eye comfort
}

@Composable
fun PdfViewer(
    pdfPath: String,                          // Path to the PDF file (e.g., file:///android_asset/...)
    modifier: Modifier = Modifier,
    readingMode: PdfReadingMode = PdfReadingMode.NORMAL, // Current color mode
    brightness: Float = 1.0f,                 // Brightness level (0.2 to 1.0)
    scale: Float = 1.0f,                      // Base zoom level from settings
    listState: LazyListState = rememberLazyListState(), // State for scrolling list
    onPageCountReady: (Int) -> Unit = {},     // Callback when total pages are known
    onCurrentPageChanged: (Int) -> Unit = {}  // Callback when user scrolls to a new page
) {
    val context = LocalContext.current
    
    // State to hold rendered page bitmaps
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Internal state for pinch-to-zoom gestures
    var gestureScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Logic to handle multi-touch transformations (zoom and pan)
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        gestureScale = (gestureScale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    // Monitor scroll position to calculate the current page index
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleIndex) {
        if (bitmaps.isNotEmpty()) {
            onCurrentPageChanged(firstVisibleIndex + 1)
        }
    }

    // Core logic to load, copy, and render the PDF pages from Assets
    LaunchedEffect(pdfPath) {
        isLoading = true
        error = null
        withContext(Dispatchers.IO) {
            try {
                // Sanitize the asset path provided by the database
                var assetPath = pdfPath.trim()
                assetPath = when {
                    assetPath.startsWith("file:///android_asset/") -> assetPath.substring("file:///android_asset/".length)
                    assetPath.startsWith("/") -> assetPath.substring(1)
                    else -> assetPath
                }
                assetPath = assetPath.replace("//", "/")
                
                // Copy the PDF from Assets to a temporary file (PdfRenderer needs a file descriptor)
                val file = File(context.cacheDir, "temp_render.pdf")
                context.assets.open(assetPath).use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                // Open the PDF for rendering
                val inputDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(inputDescriptor)
                val pageCount = renderer.pageCount
                withContext(Dispatchers.Main) { onPageCountReady(pageCount) }

                // Render each page into a Bitmap
                val loadedBitmaps = mutableListOf<Bitmap>()
                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    // Scale the bitmap resolution (1080p width base)
                    val width = 1080 
                    val height = (width.toFloat() / page.width * page.height).toInt()
                    
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    loadedBitmaps.add(bitmap)
                    page.close()
                }
                
                // Clean up native resources
                renderer.close()
                inputDescriptor.close()
                file.delete() 
                
                withContext(Dispatchers.Main) {
                    bitmaps = loadedBitmaps
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("PdfViewer", "Error loading PDF", e)
                withContext(Dispatchers.Main) {
                    error = e.toString() 
                    isLoading = false
                }
            }
        }
    }

    // Root container that handles pinch-to-zoom gestures
    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator() // Show spinner while processing
        } else if (error != null) {
            Text(text = "Error: $error", color = Color.Red) // Show error message if loading fails
        } else {
            // Combined scale: Base zoom + Pinch gesture zoom
            val totalScale = gestureScale * scale
            
            // Vertically scrollable list of pages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when(readingMode) {
                            PdfReadingMode.INVERTED -> Color.Black
                            PdfReadingMode.SEPIA -> Color(0xFFF4ECD8)
                            else -> Color(0xFF323639) // Classic dark gray reader bg
                        }
                    )
                    .graphicsLayer(
                        scaleX = totalScale,
                        scaleY = totalScale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(bitmaps) { index, bitmap ->
                    // Apply visual mode filter (Inverted or Sepia) to the page bitmap
                    val displayBitmap = remember(bitmap, readingMode) {
                        applyColorFilter(bitmap, readingMode)
                    }
                    
                    // Display each page as a Card
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when(readingMode) {
                                PdfReadingMode.INVERTED -> Color.DarkGray
                                PdfReadingMode.SEPIA -> Color(0xFFFDF6E3)
                                else -> Color.White
                            }
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Image(
                            bitmap = displayBitmap.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
        }
        
        // Transparent black layer to simulate dimming for the brightness setting
        if (brightness < 1.0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 1.0f - brightness))
            )
        }
        
        // FAB to quickly reset the zoom level if the user has zoomed in
        if (gestureScale > 1.1f || scale > 1.1f) {
            SmallFloatingActionButton(
                onClick = { gestureScale = 1f; offset = Offset.Zero },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).padding(bottom = 80.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text("Fit")
            }
        }
    }
}

/**
 * Helper function to apply color matrices (Inversion/Sepia) to a source bitmap.
 */
private fun applyColorFilter(src: Bitmap, mode: PdfReadingMode): Bitmap {
    if (mode == PdfReadingMode.NORMAL) return src
    
    val bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    
    val matrix = when(mode) {
        // Inverts RGB values: NewValue = 255 - OldValue
        PdfReadingMode.INVERTED -> ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        // Desaturates and shifts blue channel for sepia effect
        PdfReadingMode.SEPIA -> ColorMatrix().apply {
            setSaturation(0f)
            val sepiaMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            postConcat(sepiaMatrix)
        }
        else -> ColorMatrix()
    }
    
    paint.colorFilter = ColorMatrixColorFilter(matrix)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return bitmap
}
