package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    groundId: String,
    groundName: String,
    date: String,
    slots: List<String>,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentGround = state.grounds.find { it.id == groundId }
    val groundSport = currentGround?.gameTypes?.firstOrNull() ?: "General"

    var teamName by remember { mutableStateOf(AuthManager.getUserName() + " Team") }
    var opponentTeam by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val formState by viewModel.formState.collectAsState()
    val userId = AuthManager.getUserId()

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) {
            onSuccess()
            viewModel.resetFormState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Book a Slot", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF00C853))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("SELECTED SLOT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(groundName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Sport: $groundSport", fontSize = 14.sp, color = Color(0xFF00C853), fontWeight = FontWeight.Medium)

                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text(date) }, colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF00C853), labelColor = Color.White))
                        SuggestionChip(onClick = {}, label = { Text(slots.joinToString(", ")) }, colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFC6FF00)))
                    }
                }
            }

            Text("YOUR TEAM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            OutlinedTextField(value = teamName, onValueChange = { teamName = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Text("OPPONENT TEAM (OPTIONAL)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            OutlinedTextField(value = opponentTeam, onValueChange = { opponentTeam = it }, placeholder = { Text("e.g. River Hawks") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Text("NOTE TO GROUND OWNER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Any special requirements...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)) }
            )

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("BOOKING SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    SummaryRow("Ground", groundName)
                    SummaryRow("Sport", groundSport)
                    SummaryRow("Date", date)
                    SummaryRow("Time", slots.joinToString(", "))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Status after submit", fontSize = 14.sp)
                        Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp)) {
                            Text("Pending Approval", color = Color(0xFFE65100), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    slots.forEach { slot ->
                        viewModel.submitBooking(groundId, groundName, date, slot, userId, groundSport, note)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(16.dp),
                enabled = !formState.isLoading
            ) {
                if (formState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Submit Booking Request →", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancel", color = Color.Black)
            }
        }
    }
}

// 🔹 THE MISSING FUNCTION
@Composable
fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}