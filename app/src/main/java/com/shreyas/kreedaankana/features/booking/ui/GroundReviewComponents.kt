package com.shreyas.kreedaankana.features.booking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shreyas.kreedaankana.features.booking.data.Review

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = review.userName, style = MaterialTheme.typography.titleSmall)
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        // 🔹 FIXED: Use modifier for size
                        modifier = Modifier.size(16.dp),
                        tint = if (index < review.rating) Color(0xFFFFB400) else Color.LightGray
                    )
                }
            }
        }
        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Row {
        repeat(5) { index ->
            val currentRating = index + 1
            Icon(
                imageVector = if (currentRating <= rating) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (currentRating <= rating) Color(0xFFFFB400) else Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingSelected(currentRating) }
            )
        }
    }
}