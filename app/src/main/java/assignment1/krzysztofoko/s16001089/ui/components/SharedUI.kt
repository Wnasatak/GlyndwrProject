package assignment1.krzysztofoko.s16001089.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.io.OutputStream
import java.util.*

@Composable
fun HorizontalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 12000,
    wave1HeightFactor: Float = 0.75f,
    wave2HeightFactor: Float = 0.82f,
    wave1Amplitude: Float = 60f,
    wave2Amplitude: Float = 40f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val waveColor1 = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE) 
    val waveColor2 = if (isDarkTheme) Color(0xFF334155) else Color(0xFFBFDBFE) 

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        
        val width = size.width
        val height = size.height
        
        // Horizontal Wave 1
        val path1 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave1HeightFactor + Math.sin((relativeX * 2 * Math.PI + phase).toDouble()).toFloat() * wave1Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path1, color = waveColor1)
        
        // Horizontal Wave 2
        val path2 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val relativeX = x.toFloat() / width
                val y = height * wave2HeightFactor + Math.sin((relativeX * 3 * Math.PI - phase * 0.7f).toDouble()).toFloat() * wave2Amplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.8f))
    }
}

@Composable
fun VerticalWavyBackground(
    isDarkTheme: Boolean,
    animationDuration: Int = 10000,
    wave1WidthFactor: Float = 0.6f,
    wave2WidthFactor: Float = 0.75f,
    wave1Amplitude: Float = 100f,
    wave2Amplitude: Float = 60f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val waveColor1 = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE) 
    val waveColor2 = if (isDarkTheme) Color(0xFF334155) else Color(0xFFBFDBFE) 

    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = bgColor)
        val width = size.width
        val height = size.height
        
        // Vertical Wave 1
        val path1 = Path().apply { 
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) { 
                val relativeY = y.toFloat() / height
                val x = width * wave1WidthFactor + Math.sin((relativeY * 1.5 * Math.PI + phase).toDouble()).toFloat() * wave1Amplitude
                lineTo(x, y.toFloat()) 
            }
            lineTo(width, height)
            close() 
        }
        drawPath(path1, color = waveColor1)
        
        // Vertical Wave 2
        val path2 = Path().apply { 
            moveTo(width, 0f)
            for (y in 0..height.toInt() step 10) { 
                val relativeY = y.toFloat() / height
                val x = width * wave2WidthFactor + Math.sin((relativeY * 2.5 * Math.PI - phase * 0.8f).toDouble()).toFloat() * wave2Amplitude
                lineTo(x, y.toFloat()) 
            }
            lineTo(width, height)
            close() 
        }
        drawPath(path2, color = waveColor2.copy(alpha = 0.7f))
    }
}

@Composable
fun InfoCard(
    icon: ImageVector, 
    title: String, 
    content: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
    contentStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    border: BorderStroke? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(text = content, style = contentStyle)
            }
        }
    }
}

@Composable
fun rememberGlowAnimation(): Pair<Float, Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    return scale to alpha
}

@Composable
fun BookItemCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (book.isAudioBook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    trailingContent?.invoke(this)
                }
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                bottomContent?.invoke(this)
                
                Spacer(modifier = Modifier.height(8.dp))
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        text = if (book.isAudioBook) "Audio" else book.category,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(category) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 1.0f)
        )
    )
}

@Composable
fun SelectionOption(
    title: String, 
    icon: ImageVector, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        shape = RoundedCornerShape(12.dp), 
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f), 
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
fun UserAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: Int = 24,
    isLarge: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null,
    overlay: @Composable (BoxScope.() -> Unit)? = null
) {
    val defaultAvatarPath = "file:///android_asset/images/users/avatars/Avatar_defult.png"
    val isUsingDefault = photoUrl.isNullOrEmpty() || photoUrl.contains("Avatar_defult")
    
    val avatarModifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (!photoUrl.isNullOrEmpty()) photoUrl else defaultAvatarPath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = avatarModifier,
                contentScale = if (isUsingDefault) ContentScale.Fit else contentScale,
                loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(iconSize.dp / 2)) } },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isLarge) Icons.Default.Person else Icons.Default.AccountCircle, 
                            contentDescription = null, 
                            modifier = Modifier.size(iconSize.dp), 
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            overlay?.invoke(this)
        }
    }
}

fun generateAndSaveInvoicePdf(context: Context, book: Book, userName: String, invoiceId: String, date: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()

    // Title
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("TAX INVOICE", 40f, 50f, paint)

    // Store Info
    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText(AppConstants.APP_NAME, 40f, 80f, paint)
    canvas.drawText("Official University Store", 40f, 95f, paint)

    // Invoice Meta
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Invoice ID: $invoiceId", 555f, 80f, paint)
    canvas.drawText("Date: $date", 555f, 95f, paint)

    // Customer Info
    paint.textAlign = Paint.Align.LEFT
    paint.isFakeBoldText = true
    canvas.drawText("ISSUED TO:", 40f, 140f, paint)
    paint.isFakeBoldText = false
    canvas.drawText(userName, 40f, 155f, paint)
    canvas.drawText("Student ID: ${AppConstants.STUDENT_ID}", 40f, 170f, paint)

    // Line
    canvas.drawLine(40f, 200f, 555f, 200f, paint)

    // Table Header
    paint.isFakeBoldText = true
    canvas.drawText("Description", 40f, 230f, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Amount", 555f, 230f, paint)

    // Item
    paint.isFakeBoldText = false
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText(book.title, 40f, 260f, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("£${String.format(Locale.US, "%.2f", book.price)}", 555f, 260f, paint)

    // Calculations
    val studentDiscount = book.price * 0.1
    val total = book.price - studentDiscount

    canvas.drawLine(40f, 300f, 555f, 300f, paint)
    canvas.drawText("Subtotal: £${String.format(Locale.US, "%.2f", book.price)}", 555f, 330f, paint)
    canvas.drawText("Student Discount (10%): -£${String.format(Locale.US, "%.2f", studentDiscount)}", 555f, 350f, paint)
    
    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText("Total Paid: £${String.format(Locale.US, "%.2f", total)}", 555f, 380f, paint)

    // Footer
    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("Thank you for your academic purchase!", 297f, 750f, paint)
    canvas.drawText("Glyndŵr Store Support - Wrexham University", 297f, 765f, paint)

    pdfDocument.finishPage(page)

    val fileName = "Invoice_${invoiceId}.pdf"
    
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
