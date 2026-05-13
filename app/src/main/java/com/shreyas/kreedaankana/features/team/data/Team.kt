package com.shreyas.kreedaankana.features.team.data

import com.google.firebase.firestore.PropertyName

data class Team(
    var id: String = "",
    var teamName: String = "",
    var sportType: String = "",
    var location: String = "",
    var description: String = "",

    // Core Permissions
    var adminIds: List<String> = emptyList(),
    var memberIds: List<String> = emptyList(),
    var members: List<TeamMember> = emptyList(),

    // 🔹 FIX: Force Firestore to keep the exact "isLookingForMatches" spelling
    @get:PropertyName("isLookingForMatches")
    @set:PropertyName("isLookingForMatches")
    var isLookingForMatches: Boolean = false,

    var wins: Int = 0,
    var losses: Int = 0,
    var totalMatches: Int = 0,

    // Join Requests
    var pendingJoinRequests: List<String> = emptyList()
)

data class TeamMember(
    var userId: String = "",
    var name: String = "",
    var role: String = "Player"
)