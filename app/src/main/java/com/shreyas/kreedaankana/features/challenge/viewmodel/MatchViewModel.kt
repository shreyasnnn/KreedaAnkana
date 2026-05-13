package com.shreyas.kreedaankana.features.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyas.kreedaankana.features.challenge.data.Match
import com.shreyas.kreedaankana.features.challenge.data.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 🔹 Added back the List state for the Calendar
data class MatchListState(
    val matches: List<Match> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MatchViewModel : ViewModel() {
    private val repo = MatchRepository()

    // State for a single match (Confirm Match Screen)
    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match

    // 🔹 State for the list of matches (Calendar Screen)
    private val _state = MutableStateFlow(MatchListState())
    val state: StateFlow<MatchListState> = _state

    // 🔹 Added back the fetch function for the Calendar
    fun listenToMatches() {
        _state.value = _state.value.copy(isLoading = true)
        repo.listenToAllMatches { matches ->
            _state.value = MatchListState(matches = matches, isLoading = false)
        }
    }

    fun loadDraftMatch(challengeId: String) {
        viewModelScope.launch {
            try {
                var existingMatch = repo.getMatchByChallengeId(challengeId)
                if (existingMatch == null) {
                    existingMatch = Match(
                        challengeId = challengeId,
                        teamAName = "Village Lions",
                        teamBName = "River Hawks",
                        date = "Saturday, 18 October 2025",
                        time = "4:00 PM - 6:00 PM",
                        venue = "Village Ground A, Nelmangala",
                        format = "Cricket · 11-a-side"
                    )
                }
                _match.value = existingMatch
            } catch (e: Exception) { }
        }
    }

    fun confirmAndLockFixture(onSuccess: () -> Unit) {
        val currentMatch = _match.value ?: return
        viewModelScope.launch {
            try {
                val lockedMatch = currentMatch.copy(status = "locked", teamAConfirmed = true, teamBConfirmed = true)
                repo.saveMatch(lockedMatch)
                repo.updateChallengeStatusToScheduled(lockedMatch.challengeId)
                onSuccess()
            } catch (e: Exception) { }
        }
    }
}