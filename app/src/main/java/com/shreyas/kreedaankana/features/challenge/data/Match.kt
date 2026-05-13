package com.shreyas.kreedaankana.features.challenge.data

import com.google.firebase.Timestamp

data class Match(
    val id: String = "",
    val challengeId: String = "",
    val sportType: String = "", // 🔹 Added for the Score Wall
    val teamAId: String = "",
    val teamAName: String = "",
    val teamBId: String = "",
    val teamBName: String = "",
    val date: String = "",
    val time: String = "",
    val venue: String = "",
    val format: String = "",
    val status: String = "pending_confirmation", // pending_confirmation, locked, completed, cancelled
    val teamAConfirmed: Boolean = false,
    val teamBConfirmed: Boolean = false,

    // 🔹 Added post-match fields for the Score Wall
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val winnerId: String? = null,

    val timestamp: Timestamp = Timestamp.now()
)