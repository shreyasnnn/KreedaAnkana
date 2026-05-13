package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.features.booking.data.Ground
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel

@Composable
fun GroundListScreen(
    onGroundClick: (String, String, String) -> Unit,
    onAddGroundClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    // Hardcoded User Location for 30KM Check (In real app, get from GPS)
    val userLat = 12.9716
    val userLon = 77.5946

    // Inside GroundListScreen.kt
    val filteredGrounds = state.grounds.filter { ground ->
        val distance = calculateDistance(userLat, userLon, ground.latitude, ground.longitude)
        val matchesSearch = ground.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (selectedFilter == "All") true else ground.gameTypes.contains(selectedFilter)

        // 🔹 CHANGE THIS LINE: If lat/long are 0.0 (new ground), show it anyway
        val isNearby = distance <= 30.0 || (ground.latitude == 0.0 && ground.longitude == 0.0)

        isNearby && matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGroundClick, containerColor = Color(0xFF00C853)) {
                Icon(Icons.Default.Add, "Add", tint = Color.White)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(Color(0xFFF0F7F8))) {
            // Header
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, Modifier.background(Color.White, CircleShape)) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                Text("Find Grounds", Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = {}, Modifier.background(Color.White, CircleShape)) {
                    Icon(Icons.Default.Settings, null)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search grounds or village...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF00C853)) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            // Filter Chips
            LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Cricket", "Volleyball", "Football", "Badminton")) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF004D40), selectedLabelColor = Color.White)
                    )
                }
            }

            Text("NEARBY GROUNDS", Modifier.padding(horizontal = 16.dp), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredGrounds) { ground ->
                    GroundCard(ground, onClick = { onGroundClick(ground.id, ground.name, ground.gameTypes.firstOrNull() ?: "") })
                }
            }
        }
    }
}

@Composable
fun GroundCard(ground: Ground, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE8F5E9)), contentAlignment = Alignment.Center) {
                Text("🏟️", fontSize = 40.sp)
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ground.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                    Text("● Available", color = Color(0xFF00C853), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ground.gameTypes.forEach { type ->
                    SuggestionChip(onClick = {}, label = { Text(type) }, shape = CircleShape)
                }
            }
            Text("📍 ${ground.locationName}", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            Button(onClick = onClick, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))) {
                Text("View →")
            }
        }
    }
}

// Distance Utility
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0] / 1000.0 // Convert to KM
}