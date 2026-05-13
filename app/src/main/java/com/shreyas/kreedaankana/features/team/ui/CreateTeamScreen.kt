package com.shreyas.kreedaankana.features.team.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.team.data.Team
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun CreateTeamScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var teamName by remember { mutableStateOf("") }
    var sportType by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF00E676),
        unfocusedBorderColor = Color(0xFFE0E0E0),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFFAFAFA),
        focusedLeadingIconColor = Color(0xFF004D40),
        unfocusedLeadingIconColor = Color.Gray
    )

    Scaffold(
        topBar = { AppTopBar(title = "Create a Team", onBack = onBack) },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 TOP HERO ICON
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(72.dp)
                    .background(Color(0xFF004D40), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFC6FF00), modifier = Modifier.size(36.dp))
            }

            Text(
                "Build Your Squad",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF004D40)
            )

            Text(
                "Start your journey. As the creator, you will be the primary Admin of this team.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            )

            // 🔹 FORM CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Text("TEAM IDENTITY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40), letterSpacing = 1.sp)

                    OutlinedTextField(
                        value = teamName, onValueChange = { teamName = it },
                        label = { Text("Team Name") }, placeholder = { Text("e.g., Royal Challengers") },
                        leadingIcon = { Icon(Icons.Default.Groups, null) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = sportType, onValueChange = { sportType = it },
                            label = { Text("Sport") }, placeholder = { Text("Cricket") },
                            leadingIcon = { Icon(Icons.Default.Sports, null) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = location, onValueChange = { location = it },
                            label = { Text("Home Base") }, placeholder = { Text("City/Town") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                        )
                    }

                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Team Bio") }, placeholder = { Text("Tell everyone what your team is about...") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp), colors = textFieldColors
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val userId = AuthManager.getUserId()
                    val userName = AuthManager.getUserName()
                    if (userId.isEmpty()) return@Button

                    scope.launch {
                        isLoading = true
                        try {
                            val newTeam = Team(
                                teamName = teamName,
                                sportType = sportType,
                                location = location,
                                description = description,
                                adminIds = listOf(userId),
                                memberIds = listOf(userId),
                                members = listOf(com.shreyas.kreedaankana.features.team.data.TeamMember(userId = userId, name = userName, role = "Captain")),
                                isLookingForMatches = true
                            )

                            val ref = FirebaseFirestore.getInstance().collection("teams").document()
                            FirebaseFirestore.getInstance().collection("teams").document(ref.id).set(newTeam.copy(id = ref.id)).await()

                            Toast.makeText(context, "Team Created Successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                enabled = teamName.isNotBlank() && sportType.isNotBlank() && location.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), disabledContainerColor = Color(0xFFB9F6CA))
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                else {
                    Icon(Icons.Default.AddTask, contentDescription = null, tint = Color(0xFF004D40))
                    Spacer(Modifier.width(8.dp))
                    Text("Create Team", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF004D40))
                }
            }
        }
    }
}