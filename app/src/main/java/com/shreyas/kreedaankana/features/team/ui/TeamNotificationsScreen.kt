package com.shreyas.kreedaankana.features.team.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamNotificationsScreen(
    teamId: String,
    onBack: () -> Unit,
    viewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val team = state.selectedTeam
    val iAmAdmin = team?.adminIds?.contains(AuthManager.getUserId()) == true

    LaunchedEffect(teamId) {
        if (state.selectedTeam == null || state.selectedTeam?.id != teamId) {
            viewModel.loadTeamDetails(teamId)
        }
        // 🔹 Make sure notifications are loaded
        viewModel.loadTeamNotifications(teamId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Team Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF0F7F8))
            )
        },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.pendingUsers.isEmpty() && state.teamNotifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No new notifications.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // 🔹 1. CHALLENGE RESPONSES
                    if (state.teamNotifications.isNotEmpty()) {
                        item { Text("CHALLENGE UPDATES", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp)) }
                        items(state.teamNotifications) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (!note.isRead) viewModel.markTeamNotificationRead(note.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (note.isRead) Color.White else Color(0xFFE8F5E9)),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).background(if (note.isRead) Color(0xFFF5F5F5) else Color(0xFF00E676).copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, tint = if (note.isRead) Color.Gray else Color(0xFF004D40))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(note.message, color = Color.DarkGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 🔹 2. JOIN REQUESTS
                    if (state.pendingUsers.isNotEmpty()) {
                        item { Text("JOIN REQUESTS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) }
                        items(state.pendingUsers) { userMap ->
                            val uId = userMap["userId"] ?: ""
                            val uName = userMap["name"] ?: "Unknown"

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1976D2))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(uName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("wants to join your team.", color = Color.Gray, fontSize = 12.sp)
                                    }

                                    if (iAmAdmin) {
                                        Row {
                                            IconButton(
                                                onClick = { viewModel.handleJoinRequest(teamId, uId, false) },
                                                modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(36.dp)
                                            ) { Icon(Icons.Default.Close, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp)) }
                                            Spacer(Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { viewModel.handleJoinRequest(teamId, uId, true) },
                                                modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape).size(36.dp)
                                            ) { Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp)) }
                                        }
                                    } else {
                                        Text("Pending Admin", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}