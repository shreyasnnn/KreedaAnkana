package com.shreyas.kreedaankana.features.challenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCalendarScreen(
    onBack: () -> Unit,
    onMatchClick: (String) -> Unit,
    viewModel: ChallengeViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    val currentUserId = AuthManager.getUserId()

    val myTeamIds = teamState.myTeams.map { it.id }

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { teamViewModel.loadMyTeams(currentUserId) }
    LaunchedEffect(myTeamIds) { viewModel.loadInbox(myTeamIds) }

    val myFixtures = state.incoming.filter {
        (myTeamIds.contains(it.challengerTeamId) || myTeamIds.contains(it.targetTeamId)) &&
                (it.status == "scheduled" || it.status == "pending_verification" || it.status == "completed")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Team Calendar", fontWeight = FontWeight.Bold, color = Color(0xFF004D40)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF0F7F8))
            )
        },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // CALENDAR WIDGET
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color(0xFF004D40)) }
                        Text(text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF004D40))
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF004D40)) }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(day, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
                    val totalCells = daysInMonth + firstDayOfWeek
                    val rows = Math.ceil(totalCells / 7.0).toInt()
                    var currentDay = 1

                    for (i in 0 until rows) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceAround) {
                            for (j in 0..6) {
                                if (i == 0 && j < firstDayOfWeek || currentDay > daysInMonth) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val date = currentMonth.atDay(currentDay)
                                    val isToday = date == LocalDate.now()
                                    val isSelected = date == selectedDate
                                    val hasMatch = myFixtures.any { it.datePref.contains(date.dayOfMonth.toString()) }

                                    Column(
                                        modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Color(0xFF00E676) else Color.Transparent).clickable { selectedDate = date },
                                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = currentDay.toString(), color = if (isSelected) Color.White else if (isToday) Color(0xFF004D40) else Color.Black, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal)
                                        if (hasMatch) Box(modifier = Modifier.padding(top = 4.dp).size(4.dp).background(Color(0xFFC6FF00), CircleShape))
                                    }
                                    currentDay++
                                }
                            }
                        }
                    }
                }
            }

            // FIXTURES LIST
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("UPCOMING FIXTURES", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                Text("${myFixtures.size} matches", color = Color.Gray, fontSize = 12.sp)
            }

            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (myFixtures.isEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No fixtures scheduled yet.", color = Color.Gray) } }
                } else {
                    items(myFixtures) { match ->
                        FixtureCard(
                            match = match,
                            myTeamIds = myTeamIds,
                            onClick = { onMatchClick(match.id) },
                            onConfirm = {
                                viewModel.confirmMatchResult(match.id) {
                                    scope.launch { snackbarHostState.showSnackbar("Score Verified! Added to Score Wall.") }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FixtureCard(match: Challenge, myTeamIds: List<String>, onClick: () -> Unit, onConfirm: () -> Unit) {
    val amIChallenger = myTeamIds.contains(match.challengerTeamId)
    val opponentName = if (amIChallenger) match.responses.find { it.responderTeamId == match.targetTeamId }?.responderTeamName ?: "Opponent" else match.challengerTeamName

    // Determine Status Badge
    val iSubmitted = myTeamIds.contains(match.resultSubmittedByTeamId)

    val (statusText, statusColor, bgColor) = when(match.status) {
        "completed" -> Triple("Done", Color(0xFF00C853), Color(0xFFE8F5E9))
        "pending_verification" -> {
            if (iSubmitted) Triple("Verifying", Color(0xFFF57C00), Color(0xFFFFF3E0)) // Yellow/Orange
            else Triple("Action Req", Color(0xFFD32F2F), Color(0xFFFFEBEE)) // Red
        }
        else -> Triple("Set", Color(0xFF1976D2), Color(0xFFE3F2FD)) // Blue
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.width(80.dp).height(100.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).fillMaxHeight().background(statusColor))
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(match.datePref.split(" ").firstOrNull() ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        Text(match.datePref.split(" ").drop(1).joinToString(" "), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
                Box(Modifier.width(1.dp).height(60.dp).background(Color(0xFFF0F0F0)))
                Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("vs $opponentName", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF004D40))
                    Spacer(Modifier.height(4.dp))
                    Text("📍 ${match.location.ifEmpty { "TBD" }}", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    Text("⏰ ${match.timePref} • ${match.sportType}", color = Color.Gray, fontSize = 11.sp)
                }
                Surface(color = bgColor, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(end = 16.dp)) {
                    Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }

            // 🔹 INLINE VERIFICATION AREA (Only shows if Opponent submitted)
            if (match.status == "pending_verification" && !iSubmitted) {
                HorizontalDivider(color = Color(0xFFF5F5F5))
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF8E1)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Opponent submitted result:", color = Color(0xFFF57C00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${match.scoreA} - ${match.scoreB}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF004D40))
                    }
                    Button(
                        onClick = {
                            // Don't trigger the card click when clicking the button
                            onConfirm()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Verify Score", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}