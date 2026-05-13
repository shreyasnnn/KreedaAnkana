package com.shreyas.kreedaankana.features.challenge.data

import com.google.firebase.Timestamp

data class Challenge(
    val id: String = "",
    val challengerTeamId: String = "",
    val challengerTeamName: String = "",
    val targetTeamId: String? = null,
    val sportType: String = "",
    val matchFormat: String = "",
    val location: String = "",
    val datePref: String = "",
    val timePref: String = "",
    val prize: String = "",
    val entryFee: String = "",
    // 🔹 NEW STATUS FLOW: open -> negotiating -> scheduled -> pending_verification -> completed
    val status: String = "open",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val responses: List<ChallengeResponse> = emptyList(),

    // 🔹 RESULT VERIFICATION DATA
    val scoreA: Int = 0, // Score of Challenger Team
    val scoreB: Int = 0, // Score of Target/Responding Team
    val winnerId: String? = null,
    val resultSubmittedByTeamId: String? = null, // Who submitted it first?
    val resultConfirmedByOpponent: Boolean = false
)

data class ChallengeResponse(
    val responderTeamId: String = "",
    val responderTeamName: String = "",
    val replyMessage: String = "",
    val status: String = "pending",
    val timestamp: Timestamp = Timestamp.now()
)

data class NegotiationMessage(
    val id: String = "",
    val senderTeamId: String = "",
    val senderTeamName: String = "",
    val text: String = "",
    val type: String = "text",
    val proposedDate: String = "",
    val status: String = "pending",
    val timestamp: Timestamp = Timestamp.now()
)