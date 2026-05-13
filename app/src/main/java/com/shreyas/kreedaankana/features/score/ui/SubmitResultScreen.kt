package com.shreyas.kreedaankana.features.score.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitResultScreen(
    matchId: String,
    onBack: () -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()

    var scoreAInput by remember { mutableStateOf("") }
    var scoreBInput by remember { mutableStateOf("") }
    var selectedWinnerId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(matchId) {
        viewModel.loadSingleChallenge(matchId)
        teamViewModel.loadMyTeams(AuthManager.getUserId())
    }

    val challenge = state.selectedChallenge

    if (challenge == null) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF0F7F8)), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF00E676)) }
        return
    }

    val teamAName = challenge.challengerTeamName
    val teamAId = challenge.challengerTeamId
    val teamBName = challenge.responses.lastOrNull()?.responderTeamName ?: "Opponent"
    val teamBId = challenge.responses.lastOrNull()?.responderTeamId ?: ""

    // Figure out which team the submitter belongs to
    val submitterTeamId = teamState.myTeams.firstOrNull { it.id == teamAId || it.id == teamBId }?.id ?: ""

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Submit Result", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF004D40))
            )
        },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            Text("ENTER FINAL SCORE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            // Score Inputs
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = scoreAInput, onValueChange = { scoreAInput = it },
                    label = { Text(teamAName) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676))
                )
                Text("-", fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterVertically))
                OutlinedTextField(
                    value = scoreBInput, onValueChange = { scoreBInput = it },
                    label = { Text(teamBName) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E676))
                )
            }

            Spacer(Modifier.height(32.dp))
            Text("SELECT WINNER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            // Winner Selection Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SelectableTeamCard(
                    name = teamAName,
                    isSelected = selectedWinnerId == teamAId,
                    onClick = { selectedWinnerId = teamAId },
                    modifier = Modifier.weight(1f)
                )
                SelectableTeamCard(
                    name = teamBName,
                    isSelected = selectedWinnerId == teamBId,
                    onClick = { selectedWinnerId = teamBId },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val aScore = scoreAInput.toIntOrNull() ?: 0
                    val bScore = scoreBInput.toIntOrNull() ?: 0
                    if (selectedWinnerId != null) {
                        viewModel.submitMatchResult(
                            challengeId = challenge.id,
                            submitterTeamId = submitterTeamId,
                            scoreA = aScore,
                            scoreB = bScore,
                            winnerId = selectedWinnerId!!,
                            onSuccess = onBack
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = scoreAInput.isNotBlank() && scoreBInput.isNotBlank() && selectedWinnerId != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, null)
                Spacer(Modifier.width(8.dp))
                Text("Submit for Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SelectableTeamCard(name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF004D40) else Color.White),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(name, color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}