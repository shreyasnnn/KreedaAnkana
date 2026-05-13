package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.shreyas.kreedaankana.features.booking.data.Booking
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    onBookAnother: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val userId = AuthManager.getUserId()
    var selectedTab by remember { mutableStateOf("Pending") }

    LaunchedEffect(Unit) {
        viewModel.loadUserBookings(userId)
    }

    val filteredBookings = state.bookings.filter {
        it.status.equals(selectedTab, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = onBookAnother, modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape)) {
                        Icon(Icons.Default.Add, null, tint = Color(0xFF00C853))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF0F7F8))) {

            // Tab Selection Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Pending", "Confirmed", "History").forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text("$tab (${state.bookings.count { it.status.equals(tab, ignoreCase = true) }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF004D40),
                            selectedLabelColor = Color.White
                        ),
                        shape = CircleShape
                    )
                }
            }

            if (filteredBookings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No $selectedTab bookings found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text(text = "${selectedTab.uppercase()} BOOKINGS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray) }

                    items(filteredBookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onDelete = { viewModel.cancelBooking(booking.id) }
                        )
                    }
                }
            }

            Button(
                onClick = onBookAnother,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🗓 Book Another Slot", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Cancel Booking") },
            text = { Text("Are you sure you want to delete this booking for ${booking.groundName}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Confirm Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Close") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(booking.groundName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    color = if (booking.status == "pending") Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                    shape = CircleShape
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.status == "pending") Color(0xFFE65100) else Color(0xFF00C853)
                    )
                }
            }

            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusInfoChip(booking.date, Color(0xFFE3F2FD), Color(0xFF1976D2))
                StatusInfoChip(booking.timeSlot, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
                StatusInfoChip(booking.sportType, Color(0xFFE8F5E9), Color(0xFF2E7D32))
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Note: ${booking.note.ifBlank { "None" }}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                }
            }
        }
    }
}

@Composable
fun StatusInfoChip(text: String, bgColor: Color, textColor: Color) {
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, color = textColor)
    }
}