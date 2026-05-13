package com.shreyas.kreedaankana.features.booking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyas.kreedaankana.features.booking.data.Booking
import com.shreyas.kreedaankana.features.booking.data.BookingRepository
import com.shreyas.kreedaankana.features.booking.data.Ground
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Note: GroundListState should be defined in a separate state file or the 'data' package
// to avoid "Redeclaration" errors if it exists elsewhere.

data class BookingFormState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class BookingViewModel : ViewModel() {

    private val repo = BookingRepository()

    private val _state = MutableStateFlow(GroundListState(isLoading = true))
    val state: StateFlow<GroundListState> = _state

    private val _formState = MutableStateFlow(BookingFormState())
    val formState: StateFlow<BookingFormState> = _formState

    init {
        observeGrounds()
    }

    private fun observeGrounds() {
        repo.listenGrounds { grounds ->
            _state.value = _state.value.copy(
                grounds = grounds,
                isLoading = false,
                error = null
            )
        }
    }

    fun loadUserBookings(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repo.listenBookings(userId) { list ->
                _state.value = _state.value.copy(
                    bookings = list,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    // 🔹 NEW: Update ground details (Price, Location, Holidays/Closed Dates)
    fun updateGround(ground: Ground) {
        viewModelScope.launch {
            try {
                repo.updateGround(ground)
                // The real-time listener (observeGrounds) will update the UI automatically
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Update failed: ${e.message}")
            }
        }
    }

    fun submitBooking(
        groundId: String, groundName: String, date: String,
        timeSlot: String, userId: String, sportType: String, note: String
    ) {
        viewModelScope.launch {
            _formState.value = BookingFormState(isLoading = true)
            try {
                val newBooking = Booking(
                    groundId = groundId, groundName = groundName,
                    date = date, timeSlot = timeSlot, userId = userId,
                    sportType = sportType, status = "pending", note = note
                )
                repo.createBooking(newBooking)
                _formState.value = BookingFormState(isSuccess = true)
            } catch (e: Exception) {
                _formState.value = BookingFormState(error = e.message ?: "Booking failed")
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                repo.deleteBooking(bookingId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Delete failed: ${e.message}")
            }
        }
    }
    fun deleteGround(groundId: String) {
        viewModelScope.launch {
            repo.deleteGround(groundId)
        }
    }

    fun resetFormState() {
        _formState.value = BookingFormState()
    }
}