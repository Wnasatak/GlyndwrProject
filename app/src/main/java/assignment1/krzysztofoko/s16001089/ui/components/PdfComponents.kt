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
 * PdfComponents.kt
 *
 * This file provides a high-performance PDF reading engine for the application. 
 * It handles the complex task of loading PDF files from assets, rendering them into 
 * high-resolution bitmaps, and providing an interactive viewer with zoom, pan, and 
 * specialised reading modes (like night mode and sepia).
 */

/**
 * Enum defining available visual modes for reading PDFs.
 */
enum class PdfReadingMode {
    NORMAL,    // Standard white page
    INVERTED,  // Night mode (black background, white text)
    SEPIA      // Warm yellowish tint for eye comfort
}

/**
 * PdfViewer Composable
 *
 * A sophisticated PDF reader component that provides a smooth, scrollable reading experience.
 * It manages the entire lifecycle of a PDF document, from extraction and rendering to interactive display.
 *
 * Key features:
 * - **On-Demand Rendering:** Uses `PdfRenderer` to convert PDF pages into memory-efficient bitmaps.
 * - **Interactive Gestures:** Supports multi-touch pinch-to-zoom and panning.
 * - **Visual Accessibility:** Offers different reading modes (Inverted, Sepia) and adjustable brightness.
 * - **Responsive List:** Efficiently renders pages in a `LazyColumn` to handle large documents.
 *
 * @param pdfPath The asset-relative path to the PDF file.
 * @param modifier Custom styling for the viewer container.
 * @param readingMode The current visual filter applied to the pages.
 * @param brightness Float (0.0 to 1.0) controlling the simulated backlight.
 * @param scale The base magnification level.
 * @param listState State for the scrollable list of pages.
 * @param onPageCountReady Callback providing the total number of pages once loaded.
 * @param onCurrentPageChanged Callback notifying the parent of the currently visible page index.
 */
@Composable
fun PdfViewer(
    pdfPath: String,
    modifier: Modifier = Modifier,
    readingMode: PdfReadingMode = PdfReadingMode.NORMAL,
    brightness: Float = 1.0f,
    scale: Float = 1.0f,
    listState: LazyListState = rememberLazyListState(),
    onPageCountReady: (Int) -> Unit = {},
    onCurrentPageChanged: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Internal state for the collection of rendered pages.
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // State management for interactive transformations.
    var gestureScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Logic to handle multi-touch transformations (zoom and pan) simultaneously.
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        gestureScale = (gestureScale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    // Monitor scroll position to update the current page label in the parent UI.
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleIndex) {
        if (bitmaps.isNotEmpty()) {
            onCurrentPageChanged(firstVisibleIndex + 1)
        }
    }

    // Core IO Effect: Extracts the PDF and renders it on a background thread.
    LaunchedEffect(pdfPath) {
        isLoading = true
        error = null
        withContext(Dispatchers.IO) {
            try {
                // 1. Normalise the asset path for the Android AssetManager.
                var assetPath = pdfPath.trim()
                assetPath = when {
                    assetPath.startsWith("file:///android_asset/") -> assetPath.substring("file:///android_asset/".length)
                    assetPath.startsWith("/") -> assetPath.substring(1)
                    else -> assetPath
                }
                assetPath = assetPath.replace("//", "/")
                
                // 2. Prepare a temporary cache file. PdfRenderer requires a File Descriptor.
                val file = File(context.cacheDir, "temp_render.pdf")
                context.assets.open(assetPath).use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                // 3. Initialise the native PdfRenderer.
                val inputDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(inputDescriptor)
                val pageCount = renderer.pageCount
                withContext(Dispatchers.Main) { onPageCountReady(pageCount) }

                // 4. Render every page into a high-fidelity Bitmap.
                val loadedBitmaps = mutableListOf<Bitmap>()
                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    // Standardise width at 1080px for a sharp look across device types.
                    val width = 1080 
                    val height = (width.toFloat() / page.width * page.height).toInt()
                    
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    loadedBitmaps.add(bitmap)
                    page.close()
                }
                
                // 5. Clean up temporary files and native handles.
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

    // Root Container: Handles the layout and touch interactions.
    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator() // Show a spinner during initial render.
        } else if (error != null) {
            Text(text = "Error: $error", color = Color.Red) // Standard error feedback.
        } else {
            // Aggregate all scale factors (base + interactive).
            val totalScale = gestureScale * scale
            
            // Primary Render Surface: Displays the pages in a scrollable list.
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when(readingMode) {
                            PdfReadingMode.INVERTED -> Color.Black
                            PdfReadingMode.SEPIA -> Color(0xFFF4ECD8)
                            else -> Color(0xFF323639) // Branded dark grey reader background.
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
                    // Dynamically apply colour filters to each page bitmap based on the reading mode.
                    val displayBitmap = remember(bitmap, readingMode) {
                        applyColourFilter(bitmap, readingMode)
                    }
                    
                    // Display each page as a sophisticated Card.
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
        
        // Backlight Simulation: A semi-transparent black overlay to control apparent brightness.
        if (brightness < 1.0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 1.0f - brightness))
            )
        }
        
        // Navigation Shortcut: Quick-reset button if the user is zoomed in.
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
 * Helper function: Applies specialised colour matrices (Inversion/Sepia) to a source bitmap.
 * This is the core logic behind the reader's different visual modes.
 *
 * @param src The original rendered page bitmap.
 * @param mode The desired reading mode filter.
 * @return A new Bitmap with the requested visual transformation applied.
 */
private fun applyColourFilter(src: Bitmap, mode: PdfReadingMode): Bitmap {
    if (mode == PdfReadingMode.NORMAL) return src
    
    val bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    
    val matrix = when(mode) {
        // Night Mode Matrix: Inverts RGB values while preserving alpha.
        PdfReadingMode.INVERTED -> ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        // Sepia Matrix: Desaturates the image and applies a warm tint to the blue channel.
        PdfReadingMode.SEPIA -> ColorMatrix().apply {
            setSaturation(0f) // Start with grayscale.
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
