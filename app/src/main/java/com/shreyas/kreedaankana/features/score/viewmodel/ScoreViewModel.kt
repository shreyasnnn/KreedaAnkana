package com.shreyas.kreedaankana.features.score.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyas.kreedaankana.features.challenge.data.Match
import com.shreyas.kreedaankana.features.challenge.data.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ScoreState(
    val isLoading: Boolean = false,
    val completedMatches: List<Match> = emptyList(), // 🔹 Swapped Challenge for Match
    val error: String? = null
)

class ScoreViewModel : ViewModel() {
    private val repo = MatchRepository()

    private val _state = MutableStateFlow(ScoreState())
    val state: StateFlow<ScoreState> = _state

    init {
        fetchScores()
    }

    private fun fetchScores() {
        _state.value = _state.value.copy(isLoading = true)
        repo.listenCompletedMatches { matches ->
            _state.value = ScoreState(completedMatches = matches, isLoading = false)
        }
    }
}