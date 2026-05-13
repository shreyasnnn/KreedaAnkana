package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shreyas.kreedaankana.features.booking.data.Ground

@Composable
fun AddGroundScreen(
    onSave: (Ground) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var mapLink by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }

    // 🔹 NEW: Sport Type Selection
    var selectedType by remember { mutableStateOf("") }
    val sportTypes = listOf("Cricket", "Volleyball", "Badminton", "Football")

    val allDaySlots = (0..23).map { "${it.toString().padStart(2, '0')}:00-${(it + 1).toString().padStart(2, '0')}:00" }
    val selectedOperatingSlots = remember { mutableStateListOf<String>() }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Add New Ground", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ground Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = locationName, onValueChange = { locationName = it }, label = { Text("Village/City Name") }, modifier = Modifier.fillMaxWidth())

        // 🔹 NEW: Side-wise List for Types
        Spacer(Modifier.height(12.dp))
        Text("Select Ground Type", style = MaterialTheme.typography.titleSmall)
        LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sportTypes) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type) }
                )
            }
        }

        OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = mapLink, onValueChange = { mapLink = it }, label = { Text("Google Maps Link") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per Hour (₹)") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        Text("Select Available Operating Slots", style = MaterialTheme.typography.titleMedium)

        Box(modifier = Modifier.height(200.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allDaySlots) { slot ->
                    val isSelected = selectedOperatingSlots.contains(slot)
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (isSelected) selectedOperatingSlots.remove(slot) else selectedOperatingSlots.add(slot) },
                        label = { Text(slot, fontSize = 10.sp) }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (name.isNotEmpty() && selectedType.isNotEmpty()) {
                    val newGround = Ground(
                        name = name,
                        contactDetails = contact,
                        locationName = locationName,
                        price = price.toIntOrNull() ?: 0,
                        gameTypes = listOf(selectedType), // 🔹 Save as a list for the UI
                        operatingSlots = selectedOperatingSlots.toList(),
                        status = "Available"
                    )
                    onSave(newGround)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
        ) {
            Text("Save Ground")
        }
    }
}