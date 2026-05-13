package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteForever
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
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroundScreen(
    groundId: String,
    onBack: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val ground = state.grounds.find { it.id == groundId }

    // Form States
    var name by remember { mutableStateOf(ground?.name ?: "") }
    var price by remember { mutableStateOf(ground?.price.toString()) }
    var location by remember { mutableStateOf(ground?.locationName ?: "") }
    var contact by remember { mutableStateOf(ground?.contactDetails ?: "") }

    // UI States
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Sync state if ground data loads late
    LaunchedEffect(ground) {
        ground?.let {
            name = it.name
            price = it.price.toString()
            location = it.locationName
            contact = it.contactDetails
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ground?") },
            text = { Text("This action cannot be undone. All associated data for ${ground?.name} will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    // 🔹 Add deleteGround to your ViewModel/Repo
                    viewModel.deleteGround(groundId)
                    showDeleteDialog = false
                    onBack()
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Ground Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.DeleteForever, "Delete", tint = Color.Red)
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Ground Info Section
            Text("BASIC DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            EditField(label = "Ground Name", value = name, onValueChange = { name = it })
            EditField(label = "Location", value = location, onValueChange = { location = it })
            EditField(label = "Price per Hour (₹)", value = price, onValueChange = { price = it })
            EditField(label = "Contact Details", value = contact, onValueChange = { contact = it })

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 2. Holiday / Closed Dates Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CLOSED DATES / HOLIDAYS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, "Add Holiday", tint = Color(0xFF004D40))
                }
            }

            // List existing holidays
            if (ground?.closedDates?.isEmpty() == true) {
                Text("No holidays set", fontSize = 14.sp, color = Color.LightGray)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ground?.closedDates?.forEach { date ->
                        InputChip(
                            selected = false,
                            onClick = { /* Option to remove date */ },
                            label = { Text(date) },
                            trailingIcon = { Icon(Icons.Default.DeleteForever, null, Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Save Button
            Button(
                onClick = {
                    if (ground != null) {
                        viewModel.updateGround(
                            ground.copy(
                                name = name,
                                price = price.toIntOrNull() ?: 0,
                                locationName = location,
                                contactDetails = contact
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40))
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Holiday Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = sdf.format(Date(millis))
                        if (ground != null && !ground.closedDates.contains(formattedDate)) {
                            viewModel.updateGround(ground.copy(closedDates = ground.closedDates + formattedDate))
                        }
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(18.dp), tint = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF004D40),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}