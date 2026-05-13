package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.challenge.data.ChallengeResponse
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challengeId: String,
    myTeamId: String = "",
    myTeamName: String = "",
    onBack: () -> Unit,
    onStartChat: (String, String) -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    var replyMessage by remember { mutableStateOf("") }

    val currentUserId = AuthManager.getUserId()

    LaunchedEffect(challengeId) {
        viewModel.loadSingleChallenge(challengeId)
        teamViewModel.loadMyTeams(currentUserId)
    }

    val challenge = state.selectedChallenge

    if (challenge == null || teamState.isLoading) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF0F7F8)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00E676))
        }
        return
    }

    val myTeams = teamState.myTeams

    // 🔹 Find the relevant team and check Admin status
    val myTeam = myTeams.firstOrNull { it.id == challenge.challengerTeamId || challenge.responses.any { r -> r.responderTeamId == it.id } }
    val activeAcceptingTeamId = myTeam?.id ?: myTeams.firstOrNull()?.id ?: ""
    val activeAcceptingTeamName = myTeam?.teamName ?: myTeams.firstOrNull()?.teamName ?: ""

    val isCreator = myTeams.any { it.id == challenge.challengerTeamId }
    val myResponse = challenge.responses.find { response -> myTeams.any { it.id == response.responderTeamId } }
    val hasResponded = myResponse != null

    // 🔹 SECURITY CHECK: Is the current user an admin of the active team?
    val iAmAdmin = myTeams.find { it.id == activeAcceptingTeamId }?.adminIds?.contains(currentUserId) == true

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7F8))) {
        // HERO HEADER
        Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(Color(0xFF004D40))) {
            Row(Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Surface(color = Color(0xFF00E676).copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
                    Text("• ${challenge.status.replaceFirstChar { it.uppercase() }}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            Column(Modifier.fillMaxWidth().padding(top = 90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CHALLENGE POSTED BY", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(48.dp).background(Color(0xFFC6FF00), CircleShape), contentAlignment = Alignment.Center) {
                            Text(challenge.challengerTeamName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF004D40), fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(challenge.challengerTeamName, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(challenge.location, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                    Text(" VS ", color = Color(0xFFC6FF00), fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(48.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("?", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(if (challenge.targetTeamId == null) "Open" else "Specific Team", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SCROLLABLE CONTENT
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 280.dp).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (challenge.message.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("CHALLENGE MESSAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Box(Modifier.width(3.dp).height(40.dp).background(Color(0xFF00E676)))
                            Spacer(Modifier.width(12.dp))
                            Text("\"${challenge.message}\"", fontStyle = FontStyle.Italic, color = Color.DarkGray, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (isCreator) {
                Text("RESPONSES (${challenge.responses.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                if (challenge.responses.isEmpty()) {
                    Text("No responses yet.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    challenge.responses.forEach { response ->
                        ResponseCard(response = response, onOpenChat = { onStartChat(challenge.id, response.responderTeamName) })
                    }
                }
            } else {
                if (hasResponded) {
                    Text("YOUR RESPONSE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    ResponseCard(response = myResponse!!, onOpenChat = { onStartChat(challenge.id, challenge.challengerTeamName) })
                } else {
                    // 🔹 SECURITY: Show form only to Admins
                    if (iAmAdmin) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("ACCEPT THIS CHALLENGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = replyMessage, onValueChange = { replyMessage = it },
                                    placeholder = { Text("e.g. We're in!") }, modifier = Modifier.fillMaxWidth().height(100.dp),
                                    shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = Color(0xFF00E676))
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (replyMessage.isNotBlank() && activeAcceptingTeamId.isNotBlank()) {
                                            viewModel.acceptOpenChallenge(
                                                challengeId = challenge.id,
                                                challengerTeamId = challenge.challengerTeamId,
                                                responderTeamId = activeAcceptingTeamId,
                                                responderTeamName = activeAcceptingTeamName,
                                                message = replyMessage,
                                                onSuccess = { onStartChat(challenge.id, challenge.challengerTeamName) }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp), enabled = replyMessage.isNotBlank() && activeAcceptingTeamId.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)), shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Accept & Start Chat →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    } else {
                        // 🔹 READ-ONLY VIEW FOR PLAYERS
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                            Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Text("Waiting for Admin", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("Only team admins can accept open challenges.", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResponseCard(response: ChallengeResponse, onOpenChat: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp)) {
            Text(response.responderTeamName, fontWeight = FontWeight.Bold)
            Text("\"${response.replyMessage}\"", fontStyle = FontStyle.Italic, color = Color.DarkGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onOpenChat, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)), shape = RoundedCornerShape(12.dp)) {
                Text("Open Chat →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}