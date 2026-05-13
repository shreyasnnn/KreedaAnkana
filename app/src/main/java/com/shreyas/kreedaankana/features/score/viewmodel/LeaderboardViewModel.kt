package com.shreyas.kreedaankana.features.score.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shreyas.kreedaankana.features.team.data.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 🔹 BOTH the State and the ViewModel need to be here
data class LeaderboardState(
    val isLoading: Boolean = false,
    val topTeams: List<Team> = emptyList(),
    val error: String? = null
)

class LeaderboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state

    init {
        fetchLeaderboard()
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Fetching teams sorted by points
                val snapshot = db.collection("teams")
                    .orderBy("totalPoints", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .await()

                val teams = snapshot.documents.mapNotNull {
                    it.toObject(Team::class.java)?.copy(id = it.id)
                }
                _state.value = LeaderboardState(topTeams = teams, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}