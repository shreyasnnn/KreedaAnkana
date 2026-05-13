package com.shreyas.kreedaankana.features.challenge.data

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val challengeId: String = "", // Links the chat to the specific challenge
    val senderId: String = "", // The specific user who sent it
    val senderTeamId: String = "", // Used to align bubbles left or right
    val senderTeamName: String = "",
    val text: String = "",
    val type: String = "TEXT", // "TEXT" for normal chat, "PROPOSAL" for match date suggestions
    val timestamp: Timestamp = Timestamp.now()
)