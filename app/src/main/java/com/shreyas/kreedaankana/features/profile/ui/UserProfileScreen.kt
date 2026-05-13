package com.shreyas.kreedaankana.features.profile.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel
import com.shreyas.kreedaankana.features.profile.viewmodel.ProfileViewModel
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onManageGrounds: () -> Unit,
    onManageTeam: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val profileState by profileViewModel.state.collectAsState()
    val bookingState by bookingViewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    val context = LocalContext.current
    val userId = AuthManager.getUserId()

    val isGroundOwner = bookingState.grounds.any { it.ownerId == userId }
    // 🔹 FIX: Check if the list has any teams
    val hasTeam = teamState.myTeams.isNotEmpty()

    LaunchedEffect(Unit) {
        profileViewModel.loadStats(userId)
        // 🔹 FIX: Uses loadMyTeams
        teamViewModel.loadMyTeams(userId)
    }

    Scaffold(
        topBar = { AppTopBar(title = "My Profile", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = AuthManager.getUserName(), style = MaterialTheme.typography.headlineMedium)

            Card(
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("UserID", userId)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Player ID Copied!", Toast.LENGTH_SHORT).show()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("My Player ID (Tap to Copy)", style = MaterialTheme.typography.labelSmall)
                        Text(text = userId, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isGroundOwner) Color(0xFFE8F5E9) else Color(0xFFF5F5F5))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(text = if (isGroundOwner) "Ground Owner Account" else "Standard Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isGroundOwner) Color(0xFF2E7D32) else Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onManageGrounds,
                        enabled = isGroundOwner,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40), disabledContainerColor = Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.BusinessCenter, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isGroundOwner) "Manage My Grounds" else "You are not a ground owner")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (hasTeam) Color(0xFFE3F2FD) else Color(0xFFF5F5F5))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(text = if (hasTeam) "Team Admin Account" else "No Team Associated", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (hasTeam) Color(0xFF1976D2) else Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onManageTeam,
                        enabled = hasTeam,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40), disabledContainerColor = Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Group, null)
                        Spacer(Modifier.width(8.dp))
                        // 🔹 FIX: Uses the name of the first team, or "Teams" if multiple exist
                        val buttonText = if (hasTeam) "Manage ${teamState.myTeams.firstOrNull()?.teamName ?: "Teams"}" else "You haven't joined a team yet"
                        Text(buttonText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (profileState.isLoading) {
                CircularProgressIndicator()
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBox(label = "Matches", value = profileState.totalMatches.toString())
                    StatBox(label = "Wins", value = profileState.wins.toString())
                    StatBox(label = "Losses", value = profileState.losses.toString())
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}