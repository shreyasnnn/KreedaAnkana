package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGroundsScreen(
    onEditGround: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val userId = AuthManager.getUserId()
    val myGrounds = state.grounds.filter { it.ownerId == userId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Grounds", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (myGrounds.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("You haven't added any grounds yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(myGrounds) { ground ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(ground.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("📍 ${ground.locationName}", color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onEditGround(ground.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40))) {
                                    Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit Details")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}