package com.shreyas.kreedaankana.features.booking.viewmodel

import com.shreyas.kreedaankana.features.booking.data.Booking
import com.shreyas.kreedaankana.features.booking.data.Ground

data class GroundListState(
    val isLoading: Boolean = false,
    val grounds: List<Ground> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val error: String? = null
)