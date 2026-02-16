package assignment1.krzysztofoko.s16001089.ui.classroom.components.Broadcasts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.classroom.LiveChatMessage
import assignment1.krzysztofoko.s16001089.ui.components.AdaptiveTypography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * LiveChatComponent encapsulates the interactive chat section of a live broadcast.
 * Designed for future expansion (reactions, attachments, moderation).
 */
@Composable
fun LiveChatComponent(
    chatMessages: List<LiveChatMessage>,
    onSendMessage: (String) -> Unit,
    listState: LazyListState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    var userMsg by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        val sectionHeaderStyle = AdaptiveTypography.sectionHeader()
        Text(
            text = "Live Chat", 
            style = sectionHeaderStyle, 
            fontWeight = FontWeight.Black, 
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { msg ->
                StudentChatBubble(msg)
            }
        }

        Spacer(Modifier.height(12.dp))

        // INPUT AREA
        val hintStyle = AdaptiveTypography.hint()
        val captionStyle = AdaptiveTypography.caption()
        
        OutlinedTextField(
            value = userMsg,
            onValueChange = { userMsg = it },
            placeholder = { @Suppress("DEPRECATION") Text("Ask a question...", style = hintStyle) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            trailingIcon = {
                IconButton(onClick = {
                    if (userMsg.isNotBlank()) {
                        onSendMessage(userMsg)
                        userMsg = ""
                        scope.launch { if (chatMessages.isNotEmpty()) listState.animateScrollToItem(chatMessages.size - 1) }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
                }
            },
            singleLine = true,
            textStyle = captionStyle,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (userMsg.isNotBlank()) {
                    onSendMessage(userMsg)
                    userMsg = ""
                    scope.launch { if (chatMessages.isNotEmpty()) listState.animateScrollToItem(chatMessages.size - 1) }
                }
            })
        )
    }
}

/**
 * Branded chat bubble for student-teacher interactions.
 */
@Composable
fun StudentChatBubble(msg: LiveChatMessage) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            text = "${msg.sender}: ",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (msg.isTeacher) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
        Text(
            text = msg.text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 16.sp
        )
    }
}
