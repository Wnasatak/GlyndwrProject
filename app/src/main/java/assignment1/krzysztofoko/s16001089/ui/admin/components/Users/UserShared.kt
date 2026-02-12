package assignment1.krzysztofoko.s16001089.ui.admin.components.Users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.ui.components.formatAssetUrl
import coil.compose.AsyncImage

/**
 * Shared UI components for the User Management administrative views.
 */

@Composable
fun InfoSectionDetails(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(), 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Black, 
            color = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(20.dp), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { 
                content() 
            }
        }
    }
}

@Composable
fun InfoRowDetails(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActivitySectionDetails(title: String, items: List<Book>, onItemClick: (Book) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(), 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Black, 
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            Text("No activity recorded.", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(items) { book ->
                    Card(
                        modifier = Modifier
                            .width(115.dp)
                            .clickable { onItemClick(book) }, 
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                            AsyncImage(
                                model = formatAssetUrl(book.imageUrl), 
                                contentDescription = null, 
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), 
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = book.title, 
                                style = MaterialTheme.typography.labelSmall, 
                                maxLines = 1, 
                                overflow = TextOverflow.Ellipsis, 
                                fontWeight = FontWeight.ExtraBold, 
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeaderDetails(title: String) {
    Text(
        text = title.uppercase(), 
        style = MaterialTheme.typography.labelLarge, 
        fontWeight = FontWeight.Black, 
        color = MaterialTheme.colorScheme.primary, 
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowDetails(
    modifier: Modifier = Modifier, 
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start, 
    verticalArrangement: Arrangement.Vertical = Arrangement.Top, 
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier, 
        horizontalArrangement = horizontalArrangement, 
        verticalArrangement = verticalArrangement, 
        content = { content() }
    )
}
