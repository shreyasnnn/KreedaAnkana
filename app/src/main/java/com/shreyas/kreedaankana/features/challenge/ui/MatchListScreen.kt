package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.challenge.data.Challenge
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@Composable
fun MatchListScreen(
    onBack: () -> Unit,
    onMatchClick: (String) -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    val myTeamIds = teamState.myTeams.map { it.id }

    LaunchedEffect(Unit) {
        teamViewModel.loadMyTeams(AuthManager.getUserId())
    }

    LaunchedEffect(myTeamIds) {
        viewModel.loadInbox(myTeamIds)
    }

    Scaffold(topBar = { AppTopBar(title = "My Matches", onBack = onBack) }) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                items(state.incoming) { match ->
                    MatchItem(match = match, onClick = { onMatchClick(match.id) })
                }
            }
        }
    }
}

@Composable
fun MatchItem(match: Challenge, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = match.sportType, style = MaterialTheme.typography.titleLarge)
            Text(text = "Against: Any Team", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${match.status}", color = MaterialTheme.colorScheme.primary)
        }
    }
}