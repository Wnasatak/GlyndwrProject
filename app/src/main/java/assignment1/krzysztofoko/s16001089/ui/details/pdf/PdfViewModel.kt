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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * ViewModel for the PDF Reader. 
 * Redesigned for absolute stability by using localized resource lifecycle management.
 */
class PdfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(AppDatabase.getDatabase(application))
    
    private val _uiState = MutableStateFlow<PdfUiState>(PdfUiState.Loading)
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    private val _readingMode = MutableStateFlow(PdfReadingMode.NORMAL)
    val readingMode: StateFlow<PdfReadingMode> = _readingMode.asStateFlow()

    private val _brightness = MutableStateFlow(1.0f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _zoomScale = MutableStateFlow(1.0f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    // Resource trackers for this specific ViewModel instance
    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var tempFile: File? = null
    
    private val pageCache = LruCache<Int, Bitmap>(15)
    private var loadJob: Job? = null

    /**
     * Entry point to load a book's PDF content.
     */
    fun loadBook(bookId: String, initialIsDark: Boolean) {
        loadJob?.cancel()
        
        loadJob = viewModelScope.launch {
            _uiState.value = PdfUiState.Loading
            
            // Critical safety delay: Ensures previous composables detach from old bitmaps
            // before we destroy the native renderer.
            yield()
            delay(300)
            
            closeResources()
            
            _readingMode.value = if (initialIsDark) PdfReadingMode.INVERTED else PdfReadingMode.NORMAL
            
            val book = repository.getItemById(bookId)
            if (book == null || book.pdfUrl.isEmpty()) {
                _uiState.value = PdfUiState.Error("Invalid book data")
                return@launch
            }

            try {
                if (prepareRenderer(book.pdfUrl)) {
                    _uiState.value = PdfUiState.Ready(book, renderer?.pageCount ?: 0)
                } else {
                    _uiState.value = PdfUiState.Error("Engine failed to start")
                }
            } catch (e: Exception) {
                Log.e("PdfViewModel", "Critical load fail", e)
                _uiState.value = PdfUiState.Error("Renderer error: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun prepareRenderer(pdfUrl: String): Boolean = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        val assetPath = pdfUrl.trim()
            .replace("file:///android_asset/", "")
            .removePrefix("/")
            .replace("//", "/")

        val file = File(context.cacheDir, "book_${UUID.randomUUID()}.pdf")
        
        return@withContext try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }

            synchronized(this@PdfViewModel) {
                tempFile = file
                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(fileDescriptor!!)
            }
            true
        } catch (e: Exception) {
            file.delete()
            false
        }
    }

    /**
     * Fetches a rendered bitmap of a specific page.
     */
    fun getPage(index: Int, mode: PdfReadingMode): Bitmap? {
        val cacheKey = index * 10 + mode.ordinal
        pageCache.get(cacheKey)?.let { if (!it.isRecycled) return it }

        // Synchronization prevents race conditions with closeResources()
        synchronized(this) {
            val r = renderer ?: return null
            if (index < 0 || index >= r.pageCount) return null
            
            return try {
                val page = r.openPage(index)
                val width = 1080
                val height = (width.toFloat() / page.width * page.height).toInt()
                
                if (width <= 0 || height <= 0) {
                    page.close()
                    return null
                }

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                
                val finalBitmap = if (mode != PdfReadingMode.NORMAL) applyColorFilter(bitmap, mode) else bitmap
                pageCache.put(cacheKey, finalBitmap)
                finalBitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun closeResources() {
        synchronized(this) {
            try {
                renderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                // native pointers might already be invalid
            } finally {
                renderer = null
                fileDescriptor = null
                try {
                    tempFile?.delete()
                } catch (e: Exception) {}
                tempFile = null
            }
        }
        pageCache.evictAll()
    }

    fun updateReadingMode(mode: PdfReadingMode) { _readingMode.value = mode }
    fun updateBrightness(value: Float) { _brightness.value = value }
    fun updateZoom(value: Float) { _zoomScale.value = value }

    private fun applyColorFilter(src: Bitmap, mode: PdfReadingMode): Bitmap {
        val bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val matrix = when(mode) {
            PdfReadingMode.INVERTED -> ColorMatrix(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            ))
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

    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
        closeResources()
        
        // Sweep any left-over book files in background
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getApplication<Application>().cacheDir.listFiles()
                    ?.filter { it.name.startsWith("book_") }
                    ?.forEach { it.delete() }
            } catch (e: Exception) {}
        }
    }
}

sealed class PdfUiState {
    object Loading : PdfUiState()
    data class Ready(val book: Book, val pageCount: Int) : PdfUiState()
    data class Error(val message: String) : PdfUiState()
}
