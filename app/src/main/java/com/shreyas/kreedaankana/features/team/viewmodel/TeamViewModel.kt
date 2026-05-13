package com.shreyas.kreedaankana.features.team.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyas.kreedaankana.features.notifications.data.AppNotification
import com.shreyas.kreedaankana.features.team.data.Team
import com.shreyas.kreedaankana.features.team.data.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeamState(
    val isLoading: Boolean = false,
    val myTeams: List<Team> = emptyList(),
    val selectedTeam: Team? = null,
    val pendingUsers: List<Map<String, String>> = emptyList(),
    val error: String? = null,
    val isSuccess: Boolean = false,
    val teamDeleted: Boolean = false, // 🔹 FIX: Added the missing comma here!
    val teamNotifications: List<AppNotification> = emptyList()
)

class TeamViewModel : ViewModel() {
    private val repo = TeamRepository()
    private val _state = MutableStateFlow(TeamState())
    val state: StateFlow<TeamState> = _state

    fun loadMyTeams(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val teams = repo.getMyTeams(userId)
                _state.value = _state.value.copy(isLoading = false, myTeams = teams)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadTeamDetails(teamId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                FirebaseFirestore.getInstance().collection("teams").document(teamId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) return@addSnapshotListener
                        val team = snapshot?.toObject(Team::class.java)?.copy(id = snapshot.id)
                        _state.value = _state.value.copy(selectedTeam = team, isLoading = false)

                        if (team != null && team.pendingJoinRequests.isNotEmpty()) {
                            loadPendingUsers(team.pendingJoinRequests)
                        } else {
                            _state.value = _state.value.copy(pendingUsers = emptyList())
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadPendingUsers(userIds: List<String>) {
        viewModelScope.launch {
            try {
                val users = repo.getPendingUsers(userIds)
                _state.value = _state.value.copy(pendingUsers = users)
            } catch (e: Exception) {}
        }
    }

    fun handleJoinRequest(teamId: String, userId: String, accept: Boolean) {
        viewModelScope.launch {
            try {
                if (accept) repo.acceptJoinRequest(teamId, userId)
                else repo.rejectJoinRequest(teamId, userId)
                _state.value = _state.value.copy(isSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun addRealMember(teamId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                repo.fetchRealUserAndAdd(teamId, targetUserId)
                _state.value = _state.value.copy(isSuccess = true)
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    fun promoteToCoAdmin(teamId: String, targetUserId: String) {
        viewModelScope.launch {
            try { repo.addCoAdmin(teamId, targetUserId); _state.value = _state.value.copy(isSuccess = true)
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    fun demoteAdmin(teamId: String, targetUserId: String) {
        viewModelScope.launch {
            try { repo.removeAdmin(teamId, targetUserId); _state.value = _state.value.copy(isSuccess = true)
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    fun removeMember(teamId: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                repo.removeMember(teamId, targetUserId)
                _state.value = _state.value.copy(isSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            try {
                repo.deleteTeam(teamId)
                _state.value = _state.value.copy(teamDeleted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateTeamProfile(teamId: String, description: String, isLookingForMatches: Boolean) {
        viewModelScope.launch {
            try {
                repo.updateTeamSettings(teamId, mapOf("description" to description, "isLookingForMatches" to isLookingForMatches))
                _state.value = _state.value.copy(isSuccess = true)
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    fun inviteUserToTeam(teamId: String, teamName: String, targetUserId: String) {
        viewModelScope.launch {
            try {
                repo.sendTeamInvite(teamId, teamName, targetUserId)
                _state.value = _state.value.copy(isSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun loadTeamNotifications(teamId: String) {
        repo.listenToTeamNotifications(teamId) { notifs ->
            _state.value = _state.value.copy(teamNotifications = notifs)
        }
    }

    fun markTeamNotificationRead(id: String) {
        viewModelScope.launch {
            try { repo.markTeamNotificationRead(id) } catch (e: Exception) {}
        }
    }

    fun resetState() { _state.value = _state.value.copy(error = null, isSuccess = false, teamDeleted = false) }
}