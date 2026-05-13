package com.shreyas.kreedaankana.features.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Notifications
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
import com.shreyas.kreedaankana.features.notifications.viewmodel.NotificationViewModel

@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onNotificationClick: (String, String) -> Unit, // type, relatedId
    viewModel: NotificationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId = AuthManager.getUserId()
    val currentUserName = AuthManager.getUserName()

    LaunchedEffect(Unit) {
        viewModel.startListening(currentUserId)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Notifications", onBack = onBack) },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        if (state.notifications.isEmpty() && !state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("All caught up! 🏃‍♂️", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.notifications) { note ->
                    val isInvite = note.type == "TEAM_INVITE"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isInvite) {
                                    viewModel.markRead(note.id)
                                    onNotificationClick(note.type, note.relatedId)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (note.isRead) Color.White else Color(0xFFE8F5E9)
                        ),
                        elevation = CardDefaults.cardElevation(if (note.isRead) 1.dp else 4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(if (isInvite) Color(0xFFE3F2FD) else Color(0xFFF5F5F5), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when {
                                        isInvite -> Icons.Default.GroupAdd
                                        note.type == "score_verify" -> Icons.Default.Star
                                        else -> Icons.Default.Notifications
                                    }
                                    val iconTint = if (isInvite) Color(0xFF1976D2) else if (note.isRead) Color.Gray else Color(0xFF00E676)
                                    Icon(icon, contentDescription = null, tint = iconTint)
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF004D40))
                                    Text(note.message, color = Color.Gray, fontSize = 14.sp)
                                }
                            }

                            // 🔹 NEW: If it's a team invite, show Accept/Decline buttons!
                            if (isInvite) {
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    OutlinedButton(
                                        onClick = { viewModel.respondToTeamInvite(note.id, note.relatedId, false, currentUserId, currentUserName) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Decline")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.respondToTeamInvite(note.id, note.relatedId, true, currentUserId, currentUserName) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                        modifier = Modifier.height(36.dp)
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
}