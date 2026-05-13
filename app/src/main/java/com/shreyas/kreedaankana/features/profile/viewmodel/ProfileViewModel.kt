package com.shreyas.kreedaankana.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyas.kreedaankana.features.challenge.data.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val isLoading: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val repo = MatchRepository()
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    fun loadStats(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val stats = repo.getUserMatchStats(userId)
                _state.value = ProfileState(
                    totalMatches = stats["total"] ?: 0,
                    wins = stats["wins"] ?: 0,
                    losses = stats["losses"] ?: 0,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}