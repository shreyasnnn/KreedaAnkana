package com.shreyas.kreedaankana.features.challenge.data

// 🔹 This data class must be accessible to both the Repo and the Profile Screen
data class UserStats(
    val total: Int = 0,
    val wins: Int = 0,
    val performanceHistory: List<Int> = emptyList()
)