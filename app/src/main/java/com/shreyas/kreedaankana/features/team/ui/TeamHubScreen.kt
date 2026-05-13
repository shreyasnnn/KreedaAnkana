package com.shreyas.kreedaankana.features.team.ui

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamHubScreen(
    teamId: String,
    onBack: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChat: (String, String) -> Unit, // 🔹 Added Chat Navigation
    viewModel: TeamViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var isEditingSettings by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) } // 🔹 Added Add Member Dialog State

    LaunchedEffect(teamId) { viewModel.loadTeamDetails(teamId) }

    // Auto-navigate back if the team is successfully deleted
    LaunchedEffect(state.teamDeleted) {
        if (state.teamDeleted) {
            Toast.makeText(context, "Team Deleted Successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onBack()
        }
    }

    val team = state.selectedTeam
    val iAmAdmin = team?.adminIds?.contains(AuthManager.getUserId()) == true
    val pendingCount = team?.pendingJoinRequests?.size ?: 0

    // 🔹 Invite Dialog Logic
    if (showAddDialog) {
        AddMemberDialog(onDismiss = { showAddDialog = false }) { userId ->
            if (team != null) {
                // Calls the viewmodel to send a personal notification invite
                viewModel.inviteUserToTeam(teamId, team.teamName, userId)
                Toast.makeText(context, "Invite Sent!", Toast.LENGTH_SHORT).show()
            }
            showAddDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(team?.teamName ?: "Loading...", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(badge = { if (pendingCount > 0) Badge { Text(pendingCount.toString()) } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Requests", tint = if (pendingCount > 0) Color(0xFFFFB300) else Color.Gray)
                        }
                    }
                    if (iAmAdmin) {
                        IconButton(onClick = { isEditingSettings = !isEditingSettings }) {
                            Icon(if (isEditingSettings) Icons.Default.CheckCircle else Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F7F8))
            )
        },
        containerColor = Color(0xFFF0F7F8),
        floatingActionButton = {
            // 🔹 Show FAB only to admins when not in settings
            if (iAmAdmin && !isEditingSettings) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF00E676),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Invite Member")
                }
            }
        },
        bottomBar = {
            if (!isEditingSettings && team != null) {
                Surface(
                    color = Color.White, shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            // 🔹 Triggers actual chat navigation
                            onClick = { onNavigateToChat(teamId, team.teamName) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, tint = Color(0xFFC6FF00))
                            Spacer(Modifier.width(8.dp))
                            Text("Enter Team Chat", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading && team == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (team != null) {
                if (isEditingSettings) {
                    SettingsView(team, viewModel) { isEditingSettings = false }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676))
                        ) {
                            Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(64.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                    Text(team.teamName.take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF004D40))
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(team.wins.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                                        Text("Wins", color = Color(0xFF004D40), fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(team.losses.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                                        Text("Losses", color = Color(0xFF004D40), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Text("TEAM ROSTER", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        RosterView(team, viewModel)
                    }
                }
            }
        }
    }
}

// 🔹 Added Add Member Dialog function
@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Player by User ID") },
        text = {
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("Paste User ID here") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { Button(onClick = { onConfirm(userId) }) { Text("Invite") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SettingsView(team: com.shreyas.kreedaankana.features.team.data.Team, viewModel: TeamViewModel, onDone: () -> Unit) {
    var desc by remember(team.id) { mutableStateOf(team.description) }
    var matchReady by remember(team.id, team.isLookingForMatches) { mutableStateOf(team.isLookingForMatches) }

    // Delete Team States
    var showDeleteDialog by remember { mutableStateOf(false) }
    var confirmTeamName by remember { mutableStateOf("") }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Team", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("This action cannot be undone. All team data, matches, and members will be removed.")
                    Spacer(Modifier.height(16.dp))
                    Text("Type '${team.teamName}' to confirm:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmTeamName,
                        onValueChange = { confirmTeamName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTeam(team.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = confirmTeamName == team.teamName // Exact match required
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; confirmTeamName = "" }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Open for Challenges", Modifier.weight(1f))
            Switch(checked = matchReady, onCheckedChange = { matchReady = it })
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Team Bio") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.updateTeamProfile(team.id, desc, matchReady); onDone() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Changes") }

        Spacer(Modifier.height(48.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Text("Danger Zone", color = Color.Red, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("Delete Team")
        }
    }
}

@Composable
fun RosterView(team: com.shreyas.kreedaankana.features.team.data.Team, viewModel: TeamViewModel) {
    val currentUserId = AuthManager.getUserId()
    val iAmAdmin = team.adminIds.contains(currentUserId)

    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<com.shreyas.kreedaankana.features.team.data.TeamMember?>(null) }
    var actionType by remember { mutableStateOf("") } // "promote", "demote", "remove"

    if (showConfirmDialog && selectedMember != null) {
        val title = when(actionType) {
            "promote" -> "Promote to Admin?"
            "demote" -> "Remove Admin Rights?"
            else -> "Remove Member?"
        }
        val text = if (actionType == "remove") "Are you sure you want to kick ${selectedMember?.name} from the team?"
        else "Change permissions for ${selectedMember?.name}?"

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                Button(
                    onClick = {
                        when (actionType) {
                            "promote" -> viewModel.promoteToCoAdmin(team.id, selectedMember!!.userId)
                            "demote" -> viewModel.demoteAdmin(team.id, selectedMember!!.userId)
                            "remove" -> viewModel.removeMember(team.id, selectedMember!!.userId)
                        }
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (actionType == "remove") Color.Red else MaterialTheme.colorScheme.primary)
                ) { Text(if (actionType == "remove") "Kick Player" else "Confirm") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(team.members) { member ->
            val isThisMemberAdmin = team.adminIds.contains(member.userId)

            ListItem(
                headlineContent = { Text(member.name, fontWeight = FontWeight.Bold) },
                supportingContent = { Text(if (isThisMemberAdmin) "Admin" else "Player", color = Color.Gray) },
                leadingContent = {
                    Icon(
                        imageVector = if (isThisMemberAdmin) Icons.Default.Star else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isThisMemberAdmin) Color(0xFFFFD700) else Color.Gray
                    )
                },
                trailingContent = {
                    if (iAmAdmin && member.userId != currentUserId) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isThisMemberAdmin) {
                                TextButton(onClick = { selectedMember = member; actionType = "promote"; showConfirmDialog = true }) {
                                    Text("Promote", color = Color(0xFF1976D2))
                                }
                            } else {
                                TextButton(onClick = { selectedMember = member; actionType = "demote"; showConfirmDialog = true }) {
                                    Text("Demote", color = Color(0xFFF57C00))
                                }
                            }

                            IconButton(onClick = { selectedMember = member; actionType = "remove"; showConfirmDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }
                }
            )
            HorizontalDivider(color = Color(0xFFF0F0F0))
        }
    }
}