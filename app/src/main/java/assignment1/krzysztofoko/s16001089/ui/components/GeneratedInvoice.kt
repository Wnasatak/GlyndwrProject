package assignment1.krzysztofoko.s16001089.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.PurchaseItem
import java.io.InputStream
import java.io.OutputStream
import java.util.*

fun generateAndSaveInvoicePdf(
    context: Context, 
    book: Book?, 
    userName: String, 
    invoiceId: String, 
    date: String,
    purchaseRecord: PurchaseItem? = null
) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()
    
    // Load Logo from Assets
    val logoBitmap: Bitmap? = try {
        val inputStream: InputStream = context.assets.open("images/media/Glyndwr_University_Logo.png")
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }

    // Modern Header Background
    val headerPaint = Paint()
    headerPaint.color = android.graphics.Color.parseColor("#F8F9FA")
    canvas.drawRect(0f, 0f, 595f, 150f, headerPaint)

    // Title
    paint.textSize = 32f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#1A1A1A")
    canvas.drawText("INVOICE", 40f, 70f, paint)

    // Draw Logo on the Right
    logoBitmap?.let {
        val scaledWidth = 100f
        val scaledHeight = (it.height.toFloat() / it.width.toFloat()) * scaledWidth
        val destRect = android.graphics.RectF(555f - scaledWidth, 40f, 555f, 40f + scaledHeight)
        canvas.drawBitmap(it, null, destRect, paint)
    }

    // Store Info
    paint.textSize = 14f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#2D3436")
    canvas.drawText(AppConstants.APP_NAME, 40f, 105f, paint)
    paint.textSize = 11f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Official University Store", 40f, 122f, paint)

    // Invoice Meta (ID and Date) - Right aligned and more compact
    paint.textAlign = Paint.Align.RIGHT
    paint.color = android.graphics.Color.parseColor("#2D3436")
    paint.isFakeBoldText = false
    paint.textSize = 9f
    canvas.drawText("INVOICE NO: $invoiceId", 555f, 110f, paint)
    paint.color = android.graphics.Color.GRAY
    canvas.drawText(date, 555f, 122f, paint)

    // Customer Info Section
    paint.textAlign = Paint.Align.LEFT
    paint.isFakeBoldText = true
    paint.textSize = 12f
    paint.color = android.graphics.Color.parseColor("#1A1A1A")
    canvas.drawText("BILL TO", 40f, 190f, paint)
    
    paint.isFakeBoldText = false
    paint.textSize = 14f
    canvas.drawText(userName, 40f, 210f, paint)
    paint.textSize = 11f
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Student ID: ${AppConstants.STUDENT_ID}", 40f, 225f, paint)

    // Table Header
    paint.color = android.graphics.Color.parseColor("#F1F2F6")
    canvas.drawRect(40f, 260f, 555f, 290f, paint)
    
    paint.color = android.graphics.Color.parseColor("#2D3436")
    paint.isFakeBoldText = true
    paint.textSize = 12f
    canvas.drawText("Description", 55f, 280f, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Amount", 540f, 280f, paint)

    // Item Row
    val actualTotal = purchaseRecord?.let { it.amountFromWallet + it.amountPaidExternal } ?: (book?.price ?: 0.0 * 0.9)
    val isFinance = book?.mainCategory == AppConstants.CAT_FINANCE || book?.id == AppConstants.ID_TOPUP || book == null
    val actualBase = if (isFinance) actualTotal else actualTotal / 0.9
    val calculatedDiscount = if (isFinance) 0.0 else actualBase * 0.1
    val actualBaseStr = String.format(Locale.US, "%.2f", actualBase)

    paint.textAlign = Paint.Align.LEFT
    paint.isFakeBoldText = true
    paint.textSize = 13f
    paint.color = android.graphics.Color.BLACK
    canvas.drawText(book?.title ?: "University Service", 55f, 320f, paint)
    
    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    val bookType = if (book?.isAudioBook == true) "Digital Audio" else "Service/Academic"
    canvas.drawText("${book?.category ?: "Finance"} • $bookType", 55f, 338f, paint)
    
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 13f
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("£$actualBaseStr", 540f, 320f, paint)

    // Divider Line
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(40f, 360f, 555f, 360f, paint)

    // Totals Section
    var currentY = 390f
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 11f
    
    // Subtotal
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Subtotal:", 460f, currentY, paint)
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("£$actualBaseStr", 540f, currentY, paint)
    currentY += 20f
    
    // Discount
    if (calculatedDiscount > 0) {
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Student Discount (10%):", 460f, currentY, paint)
        paint.color = android.graphics.Color.parseColor("#2E7D32")
        canvas.drawText("-£${String.format(Locale.US, "%.2f", calculatedDiscount)}", 540f, currentY, paint)
        currentY += 20f
    }

    // Total Box - Updated to include payment method info
    val boxStartY = currentY + 10f
    paint.color = android.graphics.Color.parseColor("#F8F9FA")
    canvas.drawRect(350f, boxStartY, 555f, boxStartY + 60f, paint)
    
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 13f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#1A1A1A")
    
    canvas.drawText("Total Paid:", 460f, boxStartY + 25f, paint)
    canvas.drawText("£${String.format(Locale.US, "%.2f", actualTotal)}", 540f, boxStartY + 25f, paint)
    
    // Payment Method
    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.parseColor("#6C5CE7") // Primary Color
    val methodText = "Paid via ${purchaseRecord?.paymentMethod ?: "University Account"}"
    canvas.drawText(methodText, 540f, boxStartY + 42f, paint)
    
    // Reference
    purchaseRecord?.orderConfirmation?.let { ref ->
        paint.textSize = 8f
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Ref: $ref", 540f, boxStartY + 54f, paint)
    }

    // Footer
    paint.textSize = 11f
    paint.isFakeBoldText = false
    paint.textAlign = Paint.Align.CENTER
    paint.color = android.graphics.Color.parseColor("#636E72")
    canvas.drawText("Thank you for your academic purchase!", 297f, 750f, paint)
    paint.textSize = 10f
    canvas.drawText("We appreciate your support of the Wrexham University community.", 297f, 768f, paint)
    
    paint.textSize = 9f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(150f, 785f, 445f, 785f, paint)
    canvas.drawText("This is an official computer-generated document.", 297f, 800f, paint)

    pdfDocument.finishPage(page)

    val fileName = "Invoice_$invoiceId.pdf"
    
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                outputStream?.use { os -> pdfDocument.writeTo(os) }
                Toast.makeText(context, "Invoice saved to Downloads", Toast.LENGTH_LONG).show()
            }
        } else {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(directory, fileName)
            pdfDocument.writeTo(java.io.FileOutputStream(file))
            Toast.makeText(context, "Invoice saved to Downloads", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}
