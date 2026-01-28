package assignment1.krzysztofoko.s16001089.ui.details.pdf

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.ui.components.PdfReadingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * ViewModel for the PDF Reader. Handles data loading, PDF rendering logic,
 * and maintains the UI state including reading modes and zoom levels.
 */
class PdfViewModel(application: Application) : AndroidViewModel(application) {
    // Repository for fetching book data
    private val repository = BookRepository(AppDatabase.getDatabase(application))
    
    // UI state flow to handle loading, success, and error states
    private val _uiState = MutableStateFlow<PdfUiState>(PdfUiState.Loading)
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    // Current reading mode (Normal, Night, Sepia)
    private val _readingMode = MutableStateFlow(PdfReadingMode.NORMAL)
    val readingMode: StateFlow<PdfReadingMode> = _readingMode.asStateFlow()

    // Screen brightness override (for the reader view only)
    private val _brightness = MutableStateFlow(1.0f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    // Baseline zoom/font scale set by the user
    private val _zoomScale = MutableStateFlow(1.0f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    // Native PDF rendering resources
    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    
    // In-memory cache for rendered pages to ensure smooth scrolling
    private val pageCache = mutableMapOf<Int, Bitmap>()

    /**
     * Entry point to load a book's PDF content.
     */
    fun loadBook(bookId: String, initialIsDark: Boolean) {
        viewModelScope.launch {
            _uiState.value = PdfUiState.Loading
            // Set initial reading mode based on global app theme
            _readingMode.value = if (initialIsDark) PdfReadingMode.INVERTED else PdfReadingMode.NORMAL
            
            val book = repository.getItemById(bookId)
            if (book == null || book.pdfUrl.isEmpty()) {
                _uiState.value = PdfUiState.Error("Book not found or PDF link missing")
                return@launch
            }

            try {
                // Prepare the native renderer in a background thread
                prepareRenderer(book.pdfUrl)
                val pageCount = renderer?.pageCount ?: 0
                _uiState.value = PdfUiState.Ready(book, pageCount)
            } catch (e: Exception) {
                _uiState.value = PdfUiState.Error(e.localizedMessage ?: "Failed to open PDF")
            }
        }
    }

    /**
     * Extracts the PDF from assets to a temporary file and initializes the PdfRenderer.
     */
    private suspend fun prepareRenderer(pdfUrl: String) = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        var assetPath = pdfUrl.trim()
        
        // Clean the asset path string
        assetPath = when {
            assetPath.startsWith("file:///android_asset/") -> assetPath.substring("file:///android_asset/".length)
            assetPath.startsWith("/") -> assetPath.substring(1)
            else -> assetPath
        }.replace("//", "/")

        // Copy asset to cache because PdfRenderer needs a real file on disk
        val file = File(context.cacheDir, "current_reader.pdf")
        context.assets.open(assetPath).use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }

        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(fileDescriptor!!)
        pageCache.clear() // Clear cache when a new book is loaded
    }

    /**
     * Fetches a rendered bitmap of a specific page.
     * Uses caching to prevent re-rendering the same page multiple times.
     */
    fun getPage(index: Int, mode: PdfReadingMode): Bitmap? {
        // Unique key for page index + reading mode combo
        val cacheKey = index * 10 + mode.ordinal
        pageCache[cacheKey]?.let { return it }

        return renderer?.let { r ->
            synchronized(r) {
                // Open and render the page at high resolution
                val page = r.openPage(index)
                val width = 1080
                val height = (width.toFloat() / page.width * page.height).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                
                // Apply color filters (Invert/Sepia) if needed
                val finalBitmap = if (mode != PdfReadingMode.NORMAL) applyColorFilter(bitmap, mode) else bitmap
                
                // Store in memory cache
                pageCache[cacheKey] = finalBitmap
                finalBitmap
            }
        }
    }

    // State update functions called from the UI settings sheet
    fun updateReadingMode(mode: PdfReadingMode) { _readingMode.value = mode }
    fun updateBrightness(value: Float) { _brightness.value = value }
    fun updateZoom(value: Float) { _zoomScale.value = value }

    /**
     * Applies visual filters to the rendered page bitmap using ColorMatrix.
     */
    private fun applyColorFilter(src: Bitmap, mode: PdfReadingMode): Bitmap {
        val bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val matrix = when(mode) {
            // Night Mode: Negative effect
            PdfReadingMode.INVERTED -> ColorMatrix(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            ))
            // Sepia Mode: Desaturate and warm tint
            PdfReadingMode.SEPIA -> ColorMatrix().apply {
                setSaturation(0f)
                postConcat(ColorMatrix(floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 0.8f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            else -> ColorMatrix()
        }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return bitmap
    }

    /**
     * Cleanup native resources when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        renderer?.close()
        fileDescriptor?.close()
        // Clear and recycle bitmaps to free up memory
        pageCache.values.forEach { it.recycle() }
        pageCache.clear()
    }
}

/**
 * Sealed class representing the various states of the Reader UI.
 */
sealed class PdfUiState {
    object Loading : PdfUiState()
    data class Ready(val book: Book, val pageCount: Int) : PdfUiState()
    data class Error(val message: String) : PdfUiState()
}
