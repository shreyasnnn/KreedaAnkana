package com.shreyas.kreedaankana.features.booking.data

// features/booking/data/Ground.kt
data class Ground(
    val id: String = "",
    val name: String = "",
    val contactDetails: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val gameTypes: List<String> = emptyList(),
    val ownerId: String = "", // 🔹 To identify who can manage this ground
    val operatingSlots: List<String> = emptyList(),
    val status: String = "Available",
    val price: Int = 0,
    val imageUrl: String = "",
    val closedDates: List<String> = emptyList() // 🔹 Store dates as "YYYY-MM-DD"
)