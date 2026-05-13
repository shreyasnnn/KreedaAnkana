package com.shreyas.kreedaankana.features.challenge.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.core.ui.components.AppTopBar
import com.shreyas.kreedaankana.features.challenge.data.Challenge
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostChallengeScreen(
    onBack: () -> Unit,
    teamViewModel: TeamViewModel = viewModel()
) {
    val teamState by teamViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // Form States
    var selectedSport by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedDateRange by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("") }
    var entryFee by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Calendar Dialog States
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    LaunchedEffect(Unit) {
        teamViewModel.loadMyTeams(AuthManager.getUserId())
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF00E676),
        unfocusedBorderColor = Color(0xFFE0E0E0),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFFAFAFA),
        focusedLeadingIconColor = Color(0xFF004D40),
        unfocusedLeadingIconColor = Color.Gray
    )

    Scaffold(
        topBar = { AppTopBar(title = "Post Open Challenge", onBack = onBack) },
        containerColor = Color(0xFFF0F7F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Set the rules, pick a time, and let opponents find you.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp) // 🔹 Chained separately!
            )

            // 🔹 MATCH DETAILS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("MATCH DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40), letterSpacing = 1.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = selectedSport, onValueChange = { selectedSport = it },
                            label = { Text("Sport") }, placeholder = { Text("Cricket") },
                            leadingIcon = { Icon(Icons.Default.Sports, null) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = selectedFormat, onValueChange = { selectedFormat = it },
                            label = { Text("Format") }, placeholder = { Text("11-a-side") },
                            leadingIcon = { Icon(Icons.Default.Groups, null) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                        )
                    }

                    OutlinedTextField(
                        value = selectedLocation, onValueChange = { selectedLocation = it },
                        label = { Text("Location Preference") }, placeholder = { Text("Any ground in XYZ area") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = selectedDateRange, onValueChange = { }, readOnly = true,
                            label = { Text("Date Range") }, placeholder = { Text("Select Dates") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                            modifier = Modifier.weight(1.5f).clickable { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp), colors = textFieldColors,
                            enabled = false // Using enabled false makes the whole area clickable easily
                        )
                        OutlinedTextField(
                            value = selectedTime, onValueChange = { selectedTime = it },
                            label = { Text("Time") }, placeholder = { Text("5 PM") },
                            leadingIcon = { Icon(Icons.Default.Schedule, null) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                        )
                    }
                }
            }

            // 🔹 EXTRA INFO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("ADDITIONAL INFO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40), letterSpacing = 1.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = prize, onValueChange = { prize = it },
                            label = { Text("Prize (Opt)") }, placeholder = { Text("e.g. ₹5k") },
                            leadingIcon = { Icon(Icons.Default.EmojiEvents, null) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = entryFee, onValueChange = { entryFee = it },
                            label = { Text("Entry Fee") }, placeholder = { Text("Optional") },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = textFieldColors
                        )
                    }

                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        label = { Text("Message to Opponents") }, placeholder = { Text("Bring your own ball...") },
                        leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, null) },
                        modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp), colors = textFieldColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            var myTeamId = teamState.myTeams.firstOrNull()?.id
                            var myTeamName = teamState.myTeams.firstOrNull()?.teamName

                            if (myTeamId == null || myTeamName == null) {
                                val userId = AuthManager.getUserId()
                                val teamSnapshot = FirebaseFirestore.getInstance().collection("teams")
                                    .whereArrayContains("adminIds", userId)
                                    .get().await()

                                val doc = teamSnapshot.documents.firstOrNull()
                                if (doc != null) {
                                    myTeamId = doc.id
                                    myTeamName = doc.getString("teamName") ?: "Unknown Team"
                                }
                            }

                            if (myTeamId != null && myTeamName != null) {
                                val newChallenge = Challenge(
                                    challengerTeamId = myTeamId,
                                    challengerTeamName = myTeamName,
                                    sportType = selectedSport,
                                    matchFormat = selectedFormat,
                                    datePref = selectedDateRange,
                                    timePref = selectedTime,
                                    location = selectedLocation,
                                    prize = prize,
                                    entryFee = entryFee,
                                    message = message,
                                    status = "open"
                                )
                                FirebaseFirestore.getInstance().collection("challenges").add(newChallenge).await()

                                Toast.makeText(context, "Challenge Posted Successfully!", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Error: You must create a team to post a challenge.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to post: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                enabled = selectedSport.isNotBlank() && selectedLocation.isNotBlank() && selectedDateRange.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), disabledContainerColor = Color(0xFFB9F6CA))
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                else {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFF004D40))
                    Spacer(Modifier.width(8.dp))
                    Text("Post Open Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF004D40))
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis
                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

                    if (startMillis != null && endMillis != null) {
                        selectedDateRange = "${sdf.format(Date(startMillis))} - ${sdf.format(Date(endMillis))}"
                    } else if (startMillis != null) {
                        selectedDateRange = sdf.format(Date(startMillis))
                    }
                    showDatePicker = false
                }) { Text("Confirm", color = Color(0xFF004D40), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Select Match Dates", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) },
                headline = { Text("When are you free to play?", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 14.sp) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}