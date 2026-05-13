package com.shreyas.kreedaankana.features.score.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.features.challenge.data.Challenge
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreWallScreen(
    onBack: () -> Unit,
    viewModel: ChallengeViewModel = viewModel() // 🔹 We use ChallengeViewModel directly now!
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCompletedMatches()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Score Wall", fontWeight = FontWeight.Bold, color = Color(0xFF004D40)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF004D40)) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF0F7F8))
            )
        },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            // Stats Hero Banner
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40))) {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.incoming.size}", color = Color(0xFFC6FF00), fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Text("Matches Played", color = Color.White, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Text("Villages", color = Color.White, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("All", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Text("Sports", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Text("RECENT RESULTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF00E676)) }
            } else if (state.incoming.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No verified matches yet.", color = Color.Gray) }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.incoming) { match ->
                        ScoreCard(match)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCard(match: Challenge) {
    val teamAName = match.challengerTeamName
    val teamAId = match.challengerTeamId
    val teamBName = match.responses.lastOrNull()?.responderTeamName ?: "Opponent"
    val teamBId = match.responses.lastOrNull()?.responderTeamId ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                    Text(match.sportType, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
                Text("${match.datePref} • ${match.location.ifEmpty { "Any Ground" }}", color = Color.Gray, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Team A
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(Modifier.size(48.dp).background(if(match.winnerId == teamAId) Color(0xFF004D40) else Color(0xFFE0E0E0), CircleShape), contentAlignment = Alignment.Center) {
                        Text(teamAName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = if(match.winnerId == teamAId) Color.White else Color.DarkGray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(teamAName, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Text("${match.scoreA}", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = if(match.winnerId == teamAId) Color(0xFF00E676) else Color.DarkGray)
                }

                Box(modifier = Modifier.background(Color(0xFF004D40), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("VS", color = Color(0xFFC6FF00), fontWeight = FontWeight.Bold)
                }

                // Team B
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(Modifier.size(48.dp).background(if(match.winnerId == teamBId) Color(0xFF004D40) else Color(0xFFE0E0E0), CircleShape), contentAlignment = Alignment.Center) {
                        Text(teamBName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = if(match.winnerId == teamBId) Color.White else Color.DarkGray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(teamBName, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Text("${match.scoreB}", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = if(match.winnerId == teamBId) Color(0xFF00E676) else Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                val winnerName = if (match.winnerId == teamAId) teamAName else teamBName
                Text("$winnerName won", color = Color(0xFF004D40), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}