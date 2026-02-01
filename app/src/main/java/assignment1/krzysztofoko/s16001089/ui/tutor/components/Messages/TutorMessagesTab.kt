package assignment1.krzysztofoko.s16001089.ui.tutor.components.Messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.ui.tutor.TutorViewModel

@Composable
fun TutorMessagesTab(
    viewModel: TutorViewModel
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Messages", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.ChatBubbleOutline, 
                    null, 
                    modifier = Modifier.size(64.dp), 
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text("No recent conversations", color = Color.Gray)
                Text(
                    "Messages from students in your courses will appear here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}
