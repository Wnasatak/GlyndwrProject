package assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.data.Assignment
import assignment1.krzysztofoko.s16001089.data.LiveSession
import assignment1.krzysztofoko.s16001089.data.ModuleContent
import assignment1.krzysztofoko.s16001089.ui.components.isTablet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClassroomBroadcastsTab(
    sharedBroadcasts: List<LiveSession>,
    modules: List<ModuleContent>,
    assignments: List<Assignment>,
    onBroadcastClick: (LiveSession) -> Unit
) {
    if (sharedBroadcasts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Podcasts, 
                    null, 
                    modifier = Modifier.size(64.dp), 
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text("No shared broadcasts yet.", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(sharedBroadcasts) { session ->
                val moduleName = modules.find { it.id == session.moduleId }?.title ?: "General"
                val assignmentName = assignments.find { it.id == session.assignmentId }?.title
                
                StudentBroadcastCard(
                    session = session,
                    moduleName = moduleName,
                    assignmentName = assignmentName,
                    onClick = { onBroadcastClick(session) }
                )
            }
        }
    }
}

@Composable
fun StudentBroadcastCard(
    session: LiveSession,
    moduleName: String,
    assignmentName: String?,
    onClick: () -> Unit
) {
    val isTablet = isTablet()
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(session.startTime))

    val btnHeight = if (isTablet) 44.dp else 34.dp
    val fontSize = if (isTablet) 13.sp else 11.sp
    val iconSize = if (isTablet) 18.dp else 14.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(if (isTablet) 48.dp else 40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VideoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (isTablet) 24.dp else 20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title, 
                        fontWeight = FontWeight.Black, 
                        style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    @Suppress("DEPRECATION")
                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    ArchiveBadge(icon = Icons.Default.ViewModule, label = moduleName)
                }
                if (assignmentName != null) {
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        ArchiveBadge(icon = Icons.Default.Assignment, label = assignmentName, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(btnHeight),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, 
                    null, 
                    modifier = Modifier.size(iconSize)
                )
                Spacer(Modifier.width(6.dp))
                Text("Watch Replay", fontSize = fontSize, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ArchiveBadge(icon: ImageVector, label: String, color: Color = MaterialTheme.colorScheme.primary) {
    val isTablet = isTablet()
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(if (isTablet) 14.dp else 12.dp), tint = color)
            Spacer(Modifier.width(4.dp))
            @Suppress("DEPRECATION")
            Text(
                text = label, 
                style = if (isTablet) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), 
                color = color, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
