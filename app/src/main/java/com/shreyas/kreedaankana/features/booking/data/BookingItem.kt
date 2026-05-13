package com.shreyas.kreedaankana.features.booking.data

data class Booking(
    val id: String = "",
    val groundId: String = "", // Use ID instead of name for relationships
    val groundName: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val userId: String = "",
    val sportType: String = "",
    val status: String = "Pending", // States: Pending, Confirmed, Rejected
    val note: String = ""
)