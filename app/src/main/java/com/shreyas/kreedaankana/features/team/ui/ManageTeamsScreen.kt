package com.shreyas.kreedaankana.features.team.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
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
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@Composable
fun ManageTeamsScreen(
    onBack: () -> Unit,
    onTeamClick: (String) -> Unit, // Passes the selected team ID
    viewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId = AuthManager.getUserId()

    LaunchedEffect(Unit) {
        viewModel.loadMyTeams(currentUserId)
    }

    Scaffold(
        topBar = { AppTopBar(title = "My Teams", onBack = onBack) },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF00E676))
            } else if (state.myTeams.isEmpty()) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, contentDescription = null, Modifier.size(64.dp), Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("You aren't in any teams yet.", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.myTeams) { team ->
                        val iAmAdmin = team.adminIds.contains(currentUserId)

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onTeamClick(team.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(team.teamName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF004D40))
                                        if (iAmAdmin) {
                                            Spacer(Modifier.width(8.dp))
                                            Icon(Icons.Default.Star, contentDescription = "Admin", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text("${team.sportType} • ${team.members.size} Members", color = Color.Gray, fontSize = 14.sp)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}