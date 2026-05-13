package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel
import java.time.LocalDate

@Composable
fun GroundCalendarScreen(
    groundId: String,
    groundName: String,
    onBack: () -> Unit,
    onBookClick: (String, List<String>) -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    val state by bookingViewModel.state.collectAsState()
    val userId = AuthManager.getUserId()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val selectedSlots = remember { mutableStateListOf<String>() }

    val currentGround = state.grounds.find { it.id == groundId }
    val operatingSlots = currentGround?.operatingSlots ?: emptyList()

    // 🔹 HOLIDAY LOGIC: Check if selected date is in the closedDates list
    val selectedDateString = selectedDate.toString()
    val isHoliday = currentGround?.closedDates?.contains(selectedDateString) == true

    LaunchedEffect(Unit) {
        bookingViewModel.loadUserBookings(userId)
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF0F7F8))) {
        // Header
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text(groundName, Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {}, Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.Default.Person, null)
            }
        }

        // Horizontal Date Selector
        LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val dates = (0..14).map { LocalDate.now().plusDays(it.toLong()) }
            items(dates) { date ->
                val isSelected = selectedDate == date
                Card(
                    onClick = {
                        selectedDate = date
                        selectedSlots.clear()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF00C853) else Color.White),
                    modifier = Modifier.width(60.dp)
                ) {
                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(date.dayOfWeek.name.take(3), color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp)
                        Text(date.dayOfMonth.toString(), color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        // Status Legend (Only show if not a holiday)
        if (!isHoliday) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
                StatusIndicator("Available", Color(0xFF00C853))
                Spacer(Modifier.width(16.dp))
                StatusIndicator("Booked", Color(0xFF004D40))
                Spacer(Modifier.width(16.dp))
                StatusIndicator("My Booking", Color(0xFFC6FF00))
            }
        }

        // Slot List OR Holiday Message
        LazyColumn(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isHoliday) {
                // 🔹 HOLIDAY UI
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🚫", fontSize = 48.sp)
                                Text("CLOSED ON THIS DAY", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 18.sp)
                                Text("Holiday on $selectedDateString", color = Color.Gray, fontSize = 14.sp)
                                Text(
                                    text = "The owner has marked this day as unavailable for bookings.",
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(top = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else if (operatingSlots.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No operating slots configured for this ground.", color = Color.Gray)
                    }
                }
            } else {
                // Regular Slot Rendering
                items(operatingSlots) { slot ->
                    val isSelected = selectedSlots.contains(slot)
                    val bookingForSlot = state.bookings.find {
                        it.groundId == groundId && it.date == selectedDate.toString() && it.timeSlot == slot
                    }

                    val status = when {
                        bookingForSlot?.userId == userId -> "My Booking"
                        bookingForSlot != null -> "Booked"
                        else -> "Available"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable(enabled = status == "Available") {
                                if (isSelected) selectedSlots.remove(slot) else selectedSlots.add(slot)
                            },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00C853) else Color.Transparent),
                        colors = CardDefaults.cardColors(
                            containerColor = when (status) {
                                "My Booking" -> Color(0xFFF9FBE7)
                                "Booked" -> Color(0xFFF5F5F5)
                                else -> Color.White
                            }
                        )
                    ) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(slot, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(90.dp))
                            Text(
                                text = when (status) {
                                    "My Booking" -> "⭐ My Booking (${currentGround?.name})"
                                    "Booked" -> "Booked"
                                    else -> "Available — Tap to Book"
                                },
                                color = when (status) {
                                    "My Booking" -> Color(0xFF827717)
                                    "Booked" -> Color.DarkGray
                                    else -> Color(0xFF00C853)
                                },
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (status == "Available") {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    null,
                                    Modifier.size(16.dp).rotate(180f),
                                    tint = Color(0xFF00C853)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons (Only show if not a holiday)
        if (!isHoliday) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedSlots.isNotEmpty()) {
                    Button(
                        onClick = { onBookClick(selectedDate.toString(), selectedSlots.toList()) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("🗓 Book ${selectedSlots.size} Slot(s) →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = { /* Logic for viewing specific bookings */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("View My Bookings", color = Color.Black)
                }
            }
        }
    }
}

// 🔹 HELPER COMPONENT: StatusIndicator
@Composable
fun StatusIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}