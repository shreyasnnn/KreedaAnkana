package com.shreyas.kreedaankana.features.team.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    Scaffold(
        topBar = { AppTopBar(title = "Create a Team", onBack = onBack) },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Start your journey. As the creator, you will be the primary Admin of this team.", color = Color.Gray, fontSize = 14.sp)

            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team Name") },
                placeholder = { Text("e.g., Royal Challengers") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = sportType,
                    onValueChange = { sportType = it },
                    label = { Text("Sport") },
                    placeholder = { Text("Cricket") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Home Base") },
                    placeholder = { Text("City/Town") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Team Bio") },
                placeholder = { Text("Tell everyone what your team is about...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

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

                                // 🔹 FIX: Make the team immediately visible in "Discover Teams"
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = teamName.isNotBlank() && sportType.isNotBlank() && location.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Create Team", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}