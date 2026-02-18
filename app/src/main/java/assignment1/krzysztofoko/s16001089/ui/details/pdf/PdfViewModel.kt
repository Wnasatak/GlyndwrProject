package assignment1.krzysztofoko.s16001089.ui.details.pdf

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import assignment1.krzysztofoko.s16001089.data.AppDatabase
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.BookRepository
import assignment1.krzysztofoko.s16001089.ui.components.PdfReadingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * PdfViewModel.kt
 *
 * This ViewModel acts as the engine room for the PDF reader. It handles the heavy lifting
 * of extracting PDF files from the assets, rendering pages into bitmaps on a background thread,
 * and managing memory-efficient caching. It also coordinates reading modes, brightness, 
 * and zoom states for the UI.
 */
class PdfViewModel(application: Application) : AndroidViewModel(application) {
    
    // Injected repository for fetching book metadata.
    private val repository = BookRepository(AppDatabase.getDatabase(application))

    // --- UI STATE EXPOSURE --- //
    
    // Main state machine for the reader (Loading, Ready, Error).
    private val _uiState = MutableStateFlow<PdfUiState>(PdfUiState.Loading)
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    // Current visual filter (Normal, Inverted, Sepia).
    private val _readingMode = MutableStateFlow(PdfReadingMode.NORMAL)
    val readingMode: StateFlow<PdfReadingMode> = _readingMode.asStateFlow()

    // Software-simulated brightness level (0.2 to 1.0).
    private val _brightness = MutableStateFlow(1.0f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    // Global zoom multiplier for all pages.
    private val _zoomScale = MutableStateFlow(1.0f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    // --- NATIVE RESOURCE HANDLES --- //
    private var renderer: PdfRenderer? = null // The native Android PDF engine.
    private var fileDescriptor: ParcelFileDescriptor? = null // Low-level file handle.
    private var tempFile: File? = null // Cached file in the app's internal storage.

    // A memory-sensitive cache to store rendered page bitmaps, preventing jank during scrolls.
    private val pageCache = LruCache<Int, Bitmap>(10)
    private var loadJob: Job? = null // Coroutine job for managing book loading.

    /**
     * Initialises the loading process for a specific book.
     * Cancels any existing load jobs to prevent state conflicts.
     */
    fun loadBook(bookId: String, initialIsDark: Boolean) {
        loadJob?.cancel() // Stop any previous loading.
        loadJob = viewModelScope.launch {
            _uiState.value = PdfUiState.Loading // Notify UI to show spinner.

            // Clean up old resources before starting a new session.
            closeResourcesInternal()

            // Auto-select night mode if the app is currently in a dark theme.
            _readingMode.value = if (initialIsDark) PdfReadingMode.INVERTED else PdfReadingMode.NORMAL

            val book = repository.getItemById(bookId) // Fetch metadata.
            if (book == null || book.pdfUrl.isEmpty()) {
                _uiState.value = PdfUiState.Error("Invalid book data")
                return@launch
            }

            try {
                // Prepare the native renderer on a background thread.
                if (prepareRenderer(book.pdfUrl)) {
                    _uiState.value = PdfUiState.Ready(book, renderer?.pageCount ?: 0) // Engine started successfully.
                } else {
                    _uiState.value = PdfUiState.Error("Engine failed to start")
                }
            } catch (e: Exception) {
                Log.e("PdfViewModel", "Load failed", e)
                _uiState.value = PdfUiState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Extracts the PDF from assets to a temporary file and initialises the PdfRenderer.
     */
    private suspend fun prepareRenderer(pdfUrl: String): Boolean = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        // Normalise the path by removing the asset prefix.
        val assetPath = pdfUrl.trim()
            .replace("file:///android_asset/", "")
            .removePrefix("/")
            .replace("//", "/")

        // Generate a unique filename to avoid collisions.
        val file = File(context.cacheDir, "book_${UUID.randomUUID()}.pdf")

        return@withContext try {
            // Copy binary data from assets to the cache directory.
            context.assets.open(assetPath).use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }

            synchronized(this@PdfViewModel) {
                tempFile = file
                // Open the file in read-only mode for the native renderer.
                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(fileDescriptor!!)
            }
            true
        } catch (e: Exception) {
            file.delete() // Clean up on failure.
            false
        }
    }

    /**
     * Fetches a specific page bitmap, checking the memory cache first.
     * If not cached, it renders the page and applies the current colour filter.
     */
    fun getPage(index: Int, mode: PdfReadingMode): Bitmap? {
        val cacheKey = index * 10 + mode.ordinal // Composite key for page and mode.
        pageCache.get(cacheKey)?.let { if (!it.isRecycled) return it } // Use cached version if available.

        synchronized(this) {
            val r = renderer ?: return null
            if (index < 0 || index >= r.pageCount) return null

            return try {
                val page = r.openPage(index)
                // Render at a crisp 1080p base width.
                val width = 1080
                val height = (width.toFloat() / page.width * page.height).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                
                // Native render operation.
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Post-process the bitmap with colour filters if needed.
                val finalBitmap = if (mode != PdfReadingMode.NORMAL) applyColourFilter(bitmap, mode) else bitmap
                pageCache.put(cacheKey, finalBitmap) // Store in cache.
                finalBitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Safely closes native handles and deletes temporary files.
     */
    private fun closeResourcesInternal() {
        synchronized(this) {
            try {
                renderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                // Silently ignore errors during cleanup.
            } finally {
                renderer = null
                fileDescriptor = null
                try { tempFile?.delete() } catch (e: Exception) {}
                tempFile = null
            }
        }
        pageCache.evictAll() // Clear all cached bitmaps to free memory.
    }

    /**
     * Publicly accessible reset to clear state and resources.
     */
    fun reset() {
        loadJob?.cancel() // Stop any ongoing loads.
        closeResourcesInternal() // Wipe engine and files.
        _uiState.value = PdfUiState.Loading // Revert to initial state.
    }

    // --- UI CONFIGURATION UPDATES --- //
    fun updateReadingMode(mode: PdfReadingMode) { _readingMode.value = mode }
    fun updateBrightness(value: Float) { _brightness.value = value }
    fun updateZoom(value: Float) { _zoomScale.value = value }

    /**
     * Applies specialised colour matrices (Inversion/Sepia) to a source bitmap.
     */
    private fun applyColourFilter(src: Bitmap, mode: PdfReadingMode): Bitmap {
        val bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val matrix = when(mode) {
            // Night mode: Inverts RGB channels.
            PdfReadingMode.INVERTED -> ColorMatrix(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            ))
            // Sepia mode: Desaturates and warms the colour profile.
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
        canvas.drawBitmap(src, 0f, 0f, paint) // Redraw with the filter applied.
        return bitmap
    }

    /**
     * Cleans up resources when the ViewModel is destroyed.
     * Includes a scorched-earth cleanup of the cache directory.
     */
    override fun onCleared() {
        super.onCleared()
        reset()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete all temporary PDF files left in the cache.
                getApplication<Application>().cacheDir.listFiles()
                    ?.filter { it.name.startsWith("book_") }
                    ?.forEach { it.delete() }
            } catch (e: Exception) {
                // Ignore cleanup errors.
            }
        }
    }
}

/**
 * Sealed class hierarchy for the PDF Reader UI state.
 */
sealed class PdfUiState {
    data object Loading : PdfUiState() // Initial fetching state.
    data class Ready(val book: Book, val pageCount: Int) : PdfUiState() // Success state.
    data class Error(val message: String) : PdfUiState() // Failure state.
}
