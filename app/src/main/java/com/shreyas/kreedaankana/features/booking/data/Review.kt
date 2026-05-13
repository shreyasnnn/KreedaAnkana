package com.shreyas.kreedaankana.features.booking.data

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val groundId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5, // 1 to 5
    val comment: String = "",
    val timestamp: Timestamp? = null
)