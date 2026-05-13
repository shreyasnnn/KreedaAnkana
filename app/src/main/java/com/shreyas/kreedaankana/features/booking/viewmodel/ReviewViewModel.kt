package com.shreyas.kreedaankana.features.booking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.booking.data.BookingRepository
import com.shreyas.kreedaankana.features.booking.data.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReviewState(
    val isLoading: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val isSubmissionSuccess: Boolean = false,
    val error: String? = null
)

class ReviewViewModel : ViewModel() {
    private val repo = BookingRepository()

    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state

    fun loadReviews(groundId: String) {
        _state.value = _state.value.copy(isLoading = true)
        repo.listenToReviews(groundId) { list ->
            _state.value = _state.value.copy(isLoading = false, reviews = list)
        }
    }

    fun submitReview(groundId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val review = Review(
                    groundId = groundId,
                    userId = AuthManager.getUserId(),
                    userName = AuthManager.getUserName(),
                    rating = rating,
                    comment = comment
                )
                repo.postReview(groundId, review)
                _state.value = _state.value.copy(isLoading = false, isSubmissionSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun resetSubmissionState() {
        _state.value = _state.value.copy(isSubmissionSuccess = false, error = null)
    }
}