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

/**
 * GeneratedInvoice.kt
 *
 * This file contains the logic for programmatically creating a PDF invoice using the native
 * Android `PdfDocument` API. It draws each element manually onto a canvas, including text,
 * shapes, and images, to construct a professional-looking invoice document.
 */

/**
 * Generates a PDF invoice from purchase data and saves it to the device's Downloads folder.
 * This function uses the `android.graphics` canvas to manually draw each element, from the
 * header and logo to the itemised list and totals.
 *
 * @param context The application context, required for asset loading and file system access.
 * @param book The core product details (can be null for generic services like top-ups).
 * @param userName The name of the user receiving the invoice.
 * @param invoiceId A unique identifier for the invoice.
 * @param date The date the invoice was generated.
 * @param purchaseRecord The detailed purchase record, containing amounts and payment methods.
 */
fun generateAndSaveInvoicePdf(
    context: Context, 
    book: Book?, 
    userName: String, 
    invoiceId: String, 
    date: String,
    purchaseRecord: PurchaseItem? = null
) {
    // Initialise a new PDF document with a standard A4 page size.
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint() // Reusable paint object for drawing text and shapes.
    
    // Attempt to load the university logo from the assets folder.
    val logoBitmap: Bitmap? = try {
        val inputStream: InputStream = context.assets.open("images/media/Glyndwr_University_Logo.png")
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null // If the logo can't be loaded, we proceed without it.
    }

    // --- RENDER HEADER --- //
    val headerPaint = Paint() // Create a separate paint for the header background.
    headerPaint.color = android.graphics.Color.parseColor("#F8F9FA") // A light grey for the header area.
    canvas.drawRect(0f, 0f, 595f, 150f, headerPaint) // Draw the header rectangle.

    // Draw the main "INVOICE" title.
    paint.textSize = 32f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#1A1A1A")
    canvas.drawText("INVOICE", 40f, 70f, paint)

    // Draw the university logo on the right-hand side of the header.
    logoBitmap?.let {
        val scaledWidth = 100f
        val scaledHeight = (it.height.toFloat() / it.width.toFloat()) * scaledWidth
        val destRect = android.graphics.RectF(555f - scaledWidth, 40f, 555f, 40f + scaledHeight)
        canvas.drawBitmap(it, null, destRect, paint)
    }

    // Draw the store information below the main title.
    paint.textSize = 14f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#2D3436")
    canvas.drawText(AppConstants.APP_NAME, 40f, 105f, paint)
    paint.textSize = 11f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Official University Store", 40f, 122f, paint)

    // Draw invoice metadata (ID and Date) on the right.
    paint.textAlign = Paint.Align.RIGHT // Align text to the right for this section.
    paint.color = android.graphics.Color.parseColor("#2D3436")
    paint.isFakeBoldText = false
    paint.textSize = 9f
    canvas.drawText("INVOICE NO: $invoiceId", 555f, 110f, paint)
    paint.color = android.graphics.Color.GRAY
    canvas.drawText(date, 555f, 122f, paint)

    // --- RENDER CUSTOMER & ITEM DETAILS --- //
    paint.textAlign = Paint.Align.LEFT // Reset text alignment.
    paint.isFakeBoldText = true
    paint.textSize = 12f
    paint.color = android.graphics.Color.parseColor("#1A1A1A")
    canvas.drawText("BILL TO", 40f, 190f, paint)
    
    paint.isFakeBoldText = false
    paint.textSize = 14f
    canvas.drawText(userName, 40f, 210f, paint) // Draw the user's name.
    paint.textSize = 11f
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Student ID: ${AppConstants.STUDENT_ID}", 40f, 225f, paint)

    // Draw the header for the itemised table.
    paint.color = android.graphics.Color.parseColor("#F1F2F6")
    canvas.drawRect(40f, 260f, 555f, 290f, paint)
    
    paint.color = android.graphics.Color.parseColor("#2D3436")
    paint.isFakeBoldText = true
    paint.textSize = 12f
    canvas.drawText("Description", 55f, 280f, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Amount", 540f, 280f, paint)

    // Calculate the actual amounts based on the purchase record or book data.
    val actualTotal = purchaseRecord?.let { it.amountFromWallet + it.amountPaidExternal } ?: (book?.price ?: 0.0 * 0.9)
    val isFinance = book?.mainCategory == AppConstants.CAT_FINANCE || book?.id == AppConstants.ID_TOPUP || book == null
    val actualBase = if (isFinance) actualTotal else actualTotal / 0.9
    val calculatedDiscount = if (isFinance) 0.0 else actualBase * 0.1
    val actualBaseStr = String.format(Locale.US, "%.2f", actualBase)

    // Draw the main line item for the product or service.
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

    // Draw a divider line below the items.
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(40f, 360f, 555f, 360f, paint)

    // --- RENDER TOTALS & FOOTER --- //
    var currentY = 390f // Use a variable to manage the vertical position of the totals.
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 11f
    
    // Draw Subtotal row.
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Subtotal:", 460f, currentY, paint)
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("£$actualBaseStr", 540f, currentY, paint)
    currentY += 20f
    
    // Draw Student Discount row if applicable.
    if (calculatedDiscount > 0) {
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Student Discount (10%):", 460f, currentY, paint)
        paint.color = android.graphics.Color.parseColor("#2E7D32") // A green colour for the discount.
        canvas.drawText("-£${String.format(Locale.US, "%.2f", calculatedDiscount)}", 540f, currentY, paint)
        currentY += 20f
    }

    // Draw a highlighted box for the final total and payment method.
    val boxStartY = currentY + 10f
    paint.color = android.graphics.Color.parseColor("#F8F9FA")
    canvas.drawRect(350f, boxStartY, 555f, boxStartY + 60f, paint)
    
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 13f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#1A1A1A")
    canvas.drawText("Total Paid:", 460f, boxStartY + 25f, paint)
    canvas.drawText("£${String.format(Locale.US, "%.2f", actualTotal)}", 540f, boxStartY + 25f, paint)
    
    // Draw the method of payment inside the totals box.
    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.parseColor("#6C5CE7") // Use primary theme colour.
    val methodText = "Paid via ${purchaseRecord?.paymentMethod ?: "University Account"}"
    canvas.drawText(methodText, 540f, boxStartY + 42f, paint)
    
    // Draw the unique order reference number.
    purchaseRecord?.orderConfirmation?.let { ref ->
        paint.textSize = 8f
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Ref: $ref", 540f, boxStartY + 54f, paint)
    }

    // Draw the footer section with a thank-you message.
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

    // Finalise the page and prepare for saving.
    pdfDocument.finishPage(page)

    val fileName = "Invoice_$invoiceId.pdf"
    
    // Save the PDF to the device's public "Downloads" directory.
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android Q (API 29) and above for modern file saving.
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
            // Use the older, direct file path method for pre-Android Q devices.
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(directory, fileName)
            pdfDocument.writeTo(java.io.FileOutputStream(file))
            Toast.makeText(context, "Invoice saved to Downloads", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        // Display an error message if the file saving fails.
        Toast.makeText(context, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    } finally {
        // Always close the PdfDocument to release its resources.
        pdfDocument.close()
    }
}
