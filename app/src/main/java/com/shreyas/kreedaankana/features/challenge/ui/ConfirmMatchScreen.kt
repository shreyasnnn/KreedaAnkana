package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmMatchScreen(
    challengeId: String,
    onBack: () -> Unit,
    onConfirmLocked: () -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(challengeId) {
        viewModel.loadSingleChallenge(challengeId)
        // 🔹 CRITICAL FIX: Load Teams!
        teamViewModel.loadMyTeams(AuthManager.getUserId())
    }

    val challenge = state.selectedChallenge

    val myTeam = teamState.myTeams.firstOrNull { it.id == challenge?.challengerTeamId || challenge?.responses?.any { r -> r.responderTeamId == it.id } == true }
    val currentTeamId = myTeam?.id ?: ""
    val currentTeamName = myTeam?.teamName ?: ""

    val isHostAdmin = challenge?.challengerTeamId == currentTeamId

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Match Status", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF004D40))
            )
        },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            if (challenge != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("CURRENT FIXTURE DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))

                        Text("Teams", color = Color.Gray, fontSize = 12.sp)
                        Text("${challenge.challengerTeamName} vs ${challenge.responses.lastOrNull()?.responderTeamName ?: "Opponent"}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))

                        Text("Date & Time", color = Color.Gray, fontSize = 12.sp)
                        Text("${challenge.datePref} • ${challenge.timePref}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.weight(1f))

                if (isHostAdmin) {
                    // 🔹 FIX: Extract the opponent ID and pass it to the ViewModel
                    val opponentId = challenge.responses.lastOrNull()?.responderTeamId ?: ""

                    Button(
                        onClick = {
                            viewModel.confirmAndLockMatch(challenge.id, opponentId)
                            onConfirmLocked()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Confirm & Lock Fixture", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Text("Waiting for the Host Team to officially lock the fixture.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp))
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF00E676))
                    Spacer(Modifier.width(8.dp))
                    Text("Propose a New Date", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val selectedDate = sdf.format(Date(millis))

                        viewModel.sendNegotiationMessage(
                            challengeId = challengeId,
                            senderTeamId = currentTeamId,
                            senderTeamName = currentTeamName,
                            text = "Proposed a new date.",
                            type = "proposal",
                            proposedDate = selectedDate
                        )
                        showDatePicker = false
                        onBack()
                    }
                }) { Text("Send Proposal") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}