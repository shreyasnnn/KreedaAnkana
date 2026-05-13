package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel

@Composable
fun ChallengeInboxScreen(
    teamId: String,
    onBack: () -> Unit,
    viewModel: ChallengeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(teamId) {
        viewModel.loadInbox(listOf(teamId))
    }

    val directChallenges = state.incoming.filter { it.targetTeamId == teamId && it.status == "pending" }

    Scaffold(
        topBar = { AppTopBar(title = "Challenge Inbox", onBack = onBack) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (directChallenges.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active direct challenges.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                items(directChallenges) { challenge ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Challenge from ${challenge.challengerTeamName}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(text = "Sport: ${challenge.sportType}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = { viewModel.updateChallengeStatus(challenge.id, false) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                ) {
                                    Text("Decline")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.updateChallengeStatus(challenge.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                                ) {
                                    Text("Accept")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}