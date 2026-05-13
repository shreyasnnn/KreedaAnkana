package com.shreyas.kreedaankana.features.team.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.team.viewmodel.TeamDiscoveryViewModel

@Composable
fun TeamDiscoveryScreen(
    onBack: () -> Unit,
    onChallengeSuccess: () -> Unit, // Keeping this to prevent MainActivity NavHost errors
    viewModel: TeamDiscoveryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.requestSent, state.error) {
        if (state.requestSent) {
            Toast.makeText(context, "Join Request Sent to Admin!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onBack() // Go back to Home Screen
        }
        state.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Discover Teams", onBack = onBack) },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF00E676))
            } else if (state.teams.isEmpty()) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, contentDescription = null, Modifier.size(64.dp), Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No new teams are looking for members right now.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.teams) { team ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = team.teamName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF004D40)
                                )
                                Text(
                                    text = "${team.sportType} • ${team.location.ifEmpty { "Any Location" }}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (team.description.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "\"${team.description}\"",
                                        color = Color.DarkGray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                Button(
                                    onClick = { viewModel.sendJoinRequest(team.id) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Request to Join", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}