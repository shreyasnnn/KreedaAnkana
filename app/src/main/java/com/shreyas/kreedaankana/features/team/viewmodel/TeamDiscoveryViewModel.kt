package com.shreyas.kreedaankana.features.team.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.team.data.Team
import com.shreyas.kreedaankana.features.team.data.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DiscoveryState(
    val isLoading: Boolean = false,
    val teams: List<Team> = emptyList(),
    val error: String? = null,
    val requestSent: Boolean = false // 🔹 New state for Join Requests
)

class TeamDiscoveryViewModel : ViewModel() {
    private val teamRepo = TeamRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(DiscoveryState())
    val state: StateFlow<DiscoveryState> = _state

    init {
        loadTeams()
    }

    fun loadTeams() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val userId = AuthManager.getUserId()
                // Fetches teams looking for matches, excluding ones you are already in
                val list = teamRepo.getDiscoverableTeams(userId)
                _state.value = _state.value.copy(isLoading = false, teams = list)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // 🔹 NEW: Sends a join request to the team's admin
    fun sendJoinRequest(teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val userId = AuthManager.getUserId()
                // Adds the current user to the team's pending requests list
                db.collection("teams").document(teamId)
                    .update("pendingJoinRequests", FieldValue.arrayUnion(userId)).await()

                _state.value = _state.value.copy(isLoading = false, requestSent = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun resetState() {
        _state.value = _state.value.copy(requestSent = false, error = null)
    }
}