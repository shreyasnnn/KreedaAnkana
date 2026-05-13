package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.challenge.data.Challenge
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeListScreen(
    myTeamIds: List<String>,
    onBack: () -> Unit,
    onChallengeClick: (String) -> Unit,
    onPostChallenge: () -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    val currentUserId = AuthManager.getUserId()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Open", "My Challenges", "Accepted")

    val isAnyTeamAdmin = teamState.myTeams.any { it.adminIds.contains(currentUserId) }
    val cleanTeamIds = myTeamIds.filter { it.isNotBlank() }

    LaunchedEffect(cleanTeamIds) {
        viewModel.loadInbox(cleanTeamIds)
        teamViewModel.loadMyTeams(currentUserId)
    }

    val filteredChallenges = state.incoming.filter { challenge ->
        val isMine = challenge.challengerTeamId.isNotBlank() && cleanTeamIds.contains(challenge.challengerTeamId)
        val isLocked = challenge.status in listOf("scheduled", "pending_verification", "completed")

        when (selectedTab) {
            0 -> !isLocked // All: Shows open and negotiating matches
            1 -> challenge.status == "open" && !isMine // Open: From others
            2 -> isMine && !isLocked // My Challenges: Created by me, still open/negotiating
            3 -> isLocked || challenge.status == "negotiating" // 🔹 Accepted: Shows negotiations + matches strictly locked to YOU
            else -> true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Challenges", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    if (isAnyTeamAdmin) {
                        IconButton(onClick = onPostChallenge, modifier = Modifier.padding(end = 8.dp).background(Color(0xFF00E676), RoundedCornerShape(12.dp))) {
                            Icon(Icons.Default.Add, contentDescription = "Post Challenge", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF0F7F8))
            )
        },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab, containerColor = Color.Transparent, indicator = { }, divider = { }, edgePadding = 16.dp, modifier = Modifier.padding(bottom = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected, onClick = { selectedTab = index },
                        modifier = Modifier.padding(end = 8.dp).background(if (isSelected) Color(0xFF004D40) else Color.White, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp)
                    ) { Text(title, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold) }
                }
            }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredChallenges) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        isMine = challenge.challengerTeamId.isNotBlank() && cleanTeamIds.contains(challenge.challengerTeamId),
                        onClick = { onChallengeClick(challenge.id) },
                        onAccept = { },
                        onDecline = { }
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    isMine: Boolean,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(color = Color(0xFF00E676), shape = RoundedCornerShape(12.dp)) { Text(challenge.sportType, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
                Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)) { Text("• ${challenge.status.uppercase()}", color = Color(0xFF1976D2), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                // TEAM A
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(56.dp).background(if(isMine) Color(0xFF004D40) else Color(0xFFC6FF00), CircleShape), contentAlignment = Alignment.Center) {
                        Text(challenge.challengerTeamName.take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if(isMine) Color.White else Color(0xFF004D40))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(challenge.challengerTeamName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    if (isMine) Text("(Created by you)", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.background(Color(0xFF004D40), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Text("VS", color = Color(0xFFC6FF00), fontWeight = FontWeight.Bold) }

                // 🔹 TEAM B (Dynamically populates the opponent if targetTeamId is locked)
                val hasTarget = challenge.targetTeamId != null && challenge.targetTeamId.isNotBlank()
                val targetResponse = challenge.responses.find { it.responderTeamId == challenge.targetTeamId }
                val teamBName = if (hasTarget && targetResponse != null) targetResponse.responderTeamName else (if (challenge.targetTeamId == null) "Open" else "Specific Team")
                val showQuestionMark = !hasTarget || targetResponse == null

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(56.dp).background(if(showQuestionMark) Color(0xFFF5F5F5) else Color(0xFF004D40), CircleShape), contentAlignment = Alignment.Center) {
                        if (showQuestionMark) {
                            Text("?", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Gray)
                        } else {
                            Text(teamBName.take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(teamBName, fontWeight = FontWeight.Bold)
                    Text(challenge.location.ifEmpty { "Any Location" }, color = Color.Gray, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailPill(icon = Icons.Default.Schedule, text = challenge.timePref)
                DetailPill(icon = Icons.Default.CalendarToday, text = challenge.datePref)
                DetailPill(icon = Icons.Default.Group, text = challenge.matchFormat)
            }

            if (challenge.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.width(4.dp).height(40.dp).background(Color(0xFF00E676), RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text("\"${challenge.message}\"", fontStyle = FontStyle.Italic, color = Color(0xFF004D40), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isMine) {
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF004D40))) { Text("View Responses →", fontWeight = FontWeight.Bold) }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f).height(50.dp), border = BorderStroke(1.dp, Color(0xFFB2DFDB))) { Text("Decline", color = Color(0xFF004D40), fontWeight = FontWeight.Bold) }
                    Button(onClick = onClick, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))) { Text("Accept →", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun DetailPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    if (text.isNotEmpty()) {
        Row(modifier = Modifier.background(Color(0xFFF0F7F8), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF004D40))
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, color = Color(0xFF004D40), fontWeight = FontWeight.Bold)
        }
    }
}