package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiationChatScreen(
    challengeId: String,
    matchTitle: String,
    onBack: () -> Unit,
    onProposeMatch: () -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val sdf = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()) }

    val currentUserId = AuthManager.getUserId()

    LaunchedEffect(challengeId) {
        viewModel.loadSingleChallenge(challengeId)
        viewModel.loadNegotiationChat(challengeId)
        teamViewModel.loadMyTeams(currentUserId)
    }

    val challenge = state.selectedChallenge
    val myTeam = teamState.myTeams.firstOrNull { it.id == challenge?.challengerTeamId || challenge?.responses?.any { r -> r.responderTeamId == it.id } == true }
    val currentTeamId = myTeam?.id ?: ""
    val currentTeamName = myTeam?.teamName ?: ""
    val iAmAdmin = myTeam?.adminIds?.contains(currentUserId) == true

    LaunchedEffect(state.chatMessages.size) {
        if (state.chatMessages.isNotEmpty()) listState.animateScrollToItem(state.chatMessages.size - 1)
    }

    Scaffold(
        topBar = { AppTopBar(title = matchTitle, onBack = onBack) },
        containerColor = Color(0xFFF0F7F8),
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {

                val isLocked = challenge?.status in listOf("scheduled", "pending_verification", "completed")

                if (iAmAdmin) {
                    Column(Modifier.fillMaxWidth().padding(8.dp).navigationBarsPadding()) {

                        // 🔹 FIX: Only hide the Propose Button, NOT the Chat Input!
                        if (!isLocked) {
                            Button(
                                onClick = onProposeMatch,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF004D40)), shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Propose / Confirm Match →", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // 🔹 Chat Input is ALWAYS visible for Admins
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = messageText, onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f), placeholder = { Text("Message...") },
                                shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF00E676), unfocusedContainerColor = Color(0xFFF5F5F5), focusedContainerColor = Color.White)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (currentTeamId.isNotBlank()) {
                                        viewModel.sendNegotiationMessage(challengeId, currentTeamId, currentTeamName, messageText)
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier.background(Color(0xFF00E676), CircleShape)
                            ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White) }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Read-only. Only team admins can chat.", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            items(state.chatMessages) { msg ->
                val isMe = msg.senderTeamId == currentTeamId
                val timeString = sdf.format(msg.timestamp.toDate())

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                    Text(msg.senderTeamName.uppercase() + " ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color(0xFF00E676) else Color.Gray, modifier = Modifier.padding(bottom = 4.dp))

                    if (msg.type == "proposal") {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFFF57C00))
                                    Spacer(Modifier.width(8.dp))
                                    Text("New Date Proposed", fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(msg.proposedDate, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                                if (msg.status == "pending") {
                                    if (!isMe) {
                                        Spacer(Modifier.height(12.dp))
                                        if (iAmAdmin) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedButton(onClick = { viewModel.respondToProposal(challengeId, msg.id, false, msg.proposedDate) }, border = BorderStroke(1.dp, Color.Red)) { Text("Decline", color = Color.Red) }
                                                Button(onClick = { viewModel.respondToProposal(challengeId, msg.id, true, msg.proposedDate) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))) { Text("Accept") }
                                            }
                                        } else { Text("Waiting for your Admin to respond...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                                    } else { Text("Waiting for opponent response...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                                } else {
                                    Spacer(Modifier.height(8.dp))
                                    Surface(color = if (msg.status == "accepted") Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                                        Text(msg.status.uppercase(), fontWeight = FontWeight.Bold, color = if (msg.status == "accepted") Color(0xFF2E7D32) else Color(0xFFD32F2F), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.background(color = if (isMe) Color(0xFF00E676) else Color.White, shape = RoundedCornerShape(16.dp)).padding(12.dp)) {
                            Text(msg.text, color = if (isMe) Color(0xFF004D40) else Color.Black, fontSize = 16.sp)
                        }
                    }
                    Text(timeString, color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}