package com.shreyas.kreedaankana.features.team.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.team.viewmodel.TeamChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamChatScreen(
    teamId: String,
    teamName: String,
    onBack: () -> Unit,
    viewModel: TeamChatViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val currentUserId = AuthManager.getUserId()
    val listState = rememberLazyListState()

    LaunchedEffect(teamId) { viewModel.listenToMessages(teamId) }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    var messageToDelete by remember { mutableStateOf<String?>(null) }

    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Delete Message?") },
            text = { Text("This message will be permanently removed for everyone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMessage(teamId, messageToDelete!!)
                        messageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { messageToDelete = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "$teamName Chat", onBack = onBack) },
        containerColor = Color(0xFFF0F7F8),
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(8.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message team...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF00E676), unfocusedContainerColor = Color(0xFFF5F5F5), focusedContainerColor = Color.White)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(teamId, messageText)
                            messageText = ""
                        },
                        modifier = Modifier.background(Color(0xFF00E676), CircleShape)
                    ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White) }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(state.messages) { msg ->
                val isMe = msg.senderId == currentUserId
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeString = sdf.format(msg.timestamp.toDate())

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    if (!isMe) {
                        Text(msg.senderName, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isMe) Color(0xFF004D40) else Color.White,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            )
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { if (isMe) messageToDelete = msg.id } // 🔹 Long press to delete own msg
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(msg.text, color = if (isMe) Color.White else Color.Black, fontSize = 16.sp)
                            Text(timeString, color = if (isMe) Color.LightGray else Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}