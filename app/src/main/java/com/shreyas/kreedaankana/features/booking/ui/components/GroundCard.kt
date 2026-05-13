package com.shreyas.kreedaankana.features.booking.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shreyas.kreedaankana.features.booking.data.Ground

@Composable
fun GroundCard(ground: Ground, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Image Placeholder (imageUrl is now in model)
            Box(
                Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏟️", fontSize = 40.sp)
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ground.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // Status Indicator
                Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                    Text(
                        "● Available",
                        color = Color(0xFF00C853),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp
                    )
                }
            }

            // Game Types Chips (Fixed: using gameTypes list)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ground.gameTypes.take(2).forEach { type ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(type) },
                        shape = CircleShape
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Location and Price (Fixed: using locationName and price)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍 ${ground.locationName}", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("₹${ground.price}/hr", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
            }

            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View →")
            }
        }
    }
}