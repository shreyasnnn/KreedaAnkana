package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    onBack: () -> Unit,
    onSubmitResult: () -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    val currentUserId = AuthManager.getUserId()

    LaunchedEffect(matchId) {
        viewModel.loadSingleChallenge(matchId)
        teamViewModel.loadMyTeams(currentUserId)
    }

    val match = state.selectedChallenge

    if (match == null || teamState.isLoading) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF0F7F8)), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF00E676)) }
        return
    }

    val myTeam = teamState.myTeams.firstOrNull { it.id == match.challengerTeamId || match.responses.any { r -> r.responderTeamId == it.id } }
    val currentTeamId = myTeam?.id ?: ""
    val iAmAdmin = myTeam?.adminIds?.contains(currentUserId) == true

    val teamAName = match.challengerTeamName
    val teamBName = match.responses.lastOrNull()?.responderTeamName ?: "Opponent"

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7F8))) {
        Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(Color(0xFF004D40))) {
            Row(Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Surface(color = Color(0xFF00E676).copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
                    val statusText = when(match.status) {
                        "scheduled" -> "Confirmed"
                        "pending_verification" -> "Verifying"
                        "completed" -> "Completed"
                        else -> match.status.replaceFirstChar { it.uppercase() }
                    }
                    Text("• $statusText", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            Column(Modifier.fillMaxWidth().padding(top = 90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (match.status == "completed") "MATCH RESULT" else "UPCOMING MATCH", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(56.dp).background(Color(0xFF00E676), CircleShape), contentAlignment = Alignment.Center) {
                            Text(teamAName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF004D40), fontSize = 20.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(teamAName, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text(" VS ", color = Color(0xFFC6FF00), fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(56.dp).background(Color(0xFFC6FF00), CircleShape), contentAlignment = Alignment.Center) {
                            Text(teamBName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF004D40), fontSize = 20.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(teamBName, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 280.dp).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Text("MATCH DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                    Spacer(Modifier.height(16.dp))
                    DetailRow(icon = Icons.Default.CalendarMonth, title = "Date", subtitle = match.datePref)
                    DetailRow(icon = Icons.Default.Schedule, title = "Time", subtitle = match.timePref)
                    DetailRow(icon = Icons.Default.LocationOn, title = "Venue", subtitle = match.location.ifEmpty { "TBD" }, iconColor = Color(0xFFE91E63))
                    DetailRow(icon = Icons.Default.Sports, title = "Rules", subtitle = "${match.sportType} • ${match.matchFormat}", iconColor = Color(0xFFFF9800), hideDivider = true)
                }
            }

            if (iAmAdmin) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(color = Color(0xFF00E676), style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))
                        }
                        .background(Color(0xFFE8F5E9).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

                        when (match.status) {
                            "scheduled" -> {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Match Complete?", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                                Text("Submit the result once your match is over.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                                Button(
                                    onClick = onSubmitResult,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)), shape = RoundedCornerShape(12.dp)
                                ) { Text("📊 Submit Result →", fontWeight = FontWeight.Bold) }
                            }

                            "pending_verification" -> {
                                val iSubmitted = match.resultSubmittedByTeamId == currentTeamId

                                if (iSubmitted) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Result Submitted", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                                    Text("Waiting for the opponent to verify the score.", color = Color.Gray, fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Verify Result", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                                    Text("Opponent submitted: ${match.scoreA} - ${match.scoreB}", color = Color.DarkGray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = onSubmitResult, // 🔹 FIX 1: Dispute opens the Submit form for counter-submission!
                                            modifier = Modifier.weight(1f), border = BorderStroke(1.dp, Color.Red)
                                        ) { Text("Dispute", color = Color.Red) }
                                        Button(
                                            onClick = { viewModel.confirmMatchResult(match.id, onSuccess = onBack) },
                                            modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                                        ) { Text("Confirm") }
                                    }
                                }
                            }

                            "completed" -> {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Match Verified", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                                Text("Final Score: ${match.scoreA} - ${match.scoreB}", color = Color.DarkGray, fontSize = 14.sp)
                                Text("This match is now visible on the Score Wall.", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Match Status: ${match.status.replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text("Only Admins can submit or verify results.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.LightGray)) {
                Text("← Back to Calendar", color = Color(0xFF004D40), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, iconColor: Color = Color(0xFF1976D2), hideDivider: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(iconColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconColor) }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(subtitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF004D40))
        }
    }
    if (!hideDivider) HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(start = 56.dp))
}