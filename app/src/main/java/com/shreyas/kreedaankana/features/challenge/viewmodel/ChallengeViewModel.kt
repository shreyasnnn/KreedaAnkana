package com.shreyas.kreedaankana.features.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shreyas.kreedaankana.features.challenge.data.Challenge
import com.shreyas.kreedaankana.features.challenge.data.ChallengeResponse
import com.shreyas.kreedaankana.features.challenge.data.NegotiationMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChallengeState(
    val incoming: List<Challenge> = emptyList(),
    val selectedChallenge: Challenge? = null,
    val chatMessages: List<NegotiationMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChallengeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(ChallengeState())
    val state: StateFlow<ChallengeState> = _state

    fun loadInbox(myTeamIds: List<String>) {
        if (myTeamIds.isEmpty()) {
            _state.value = _state.value.copy(incoming = emptyList())
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        db.collection("challenges")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                    return@addSnapshotListener
                }

                val allChallenges = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Challenge::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                val relevantChallenges = allChallenges.filter { challenge ->
                    if (challenge.status == "open") return@filter true

                    val isCreator = myTeamIds.contains(challenge.challengerTeamId)
                    val isTarget = challenge.targetTeamId != null && myTeamIds.contains(challenge.targetTeamId)
                    val isResponder = challenge.responses.any { myTeamIds.contains(it.responderTeamId) }

                    // 🔹 STRICT PRIVACY LOGIC:
                    // If the match is officially locked to a specific team, hide it from all other responders!
                    if (challenge.status in listOf("scheduled", "pending_verification", "completed")) {
                        isCreator || isTarget
                    } else {
                        isCreator || isTarget || isResponder
                    }
                }

                _state.value = _state.value.copy(incoming = relevantChallenges, isLoading = false)
            }
    }

    fun loadSingleChallenge(challengeId: String) {
        db.collection("challenges").document(challengeId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val challenge = snapshot.toObject(Challenge::class.java)?.copy(id = snapshot.id)
                _state.value = _state.value.copy(selectedChallenge = challenge)
            }
    }

    fun acceptOpenChallenge(
        challengeId: String, challengerTeamId: String, responderTeamId: String, responderTeamName: String, message: String, onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = ChallengeResponse(responderTeamId = responderTeamId, responderTeamName = responderTeamName, replyMessage = message, status = "pending")
                db.collection("challenges").document(challengeId).update("responses", FieldValue.arrayUnion(response)).await()

                val notification = mapOf("receiverId" to challengerTeamId, "title" to "Challenge Accepted!", "message" to "$responderTeamName wants to play your open challenge.", "type" to "CHALLENGE_RESPONSE", "relatedId" to challengeId, "isRead" to false, "timestamp" to FieldValue.serverTimestamp())
                db.collection("team_notifications").add(notification).await()
                onSuccess()
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    fun updateChallengeStatus(challengeId: String, isAccepted: Boolean) {
        viewModelScope.launch {
            try {
                val newStatus = if (isAccepted) "accepted" else "declined"
                db.collection("challenges").document(challengeId).update("status", newStatus).await()
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    fun loadNegotiationChat(challengeId: String) {
        db.collection("challenges").document(challengeId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val msgs = snapshot?.documents?.mapNotNull { doc -> doc.toObject(NegotiationMessage::class.java)?.copy(id = doc.id) } ?: emptyList()
                _state.value = _state.value.copy(chatMessages = msgs)
            }
    }

    fun sendNegotiationMessage(challengeId: String, senderTeamId: String, senderTeamName: String, text: String, type: String = "text", proposedDate: String = "") {
        if (text.isBlank() && type == "text") return
        viewModelScope.launch {
            val msg = NegotiationMessage(senderTeamId = senderTeamId, senderTeamName = senderTeamName, text = text, type = type, proposedDate = proposedDate)
            db.collection("challenges").document(challengeId).collection("messages").add(msg).await()
            db.collection("challenges").document(challengeId).update("status", "negotiating").await()
        }
    }

    fun respondToProposal(challengeId: String, messageId: String, accept: Boolean, proposedDate: String) {
        viewModelScope.launch {
            val status = if (accept) "accepted" else "declined"
            db.collection("challenges").document(challengeId).collection("messages").document(messageId).update("status", status).await()
            if (accept) { db.collection("challenges").document(challengeId).update("datePref", proposedDate).await() }
        }
    }

    fun confirmAndLockMatch(challengeId: String, targetTeamId: String) {
        viewModelScope.launch {
            db.collection("challenges").document(challengeId).update("status", "scheduled", "targetTeamId", targetTeamId).await()
        }
    }

    fun submitMatchResult(challengeId: String, submitterTeamId: String, scoreA: Int, scoreB: Int, winnerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("challenges").document(challengeId).update("status", "pending_verification", "scoreA", scoreA, "scoreB", scoreB, "winnerId", winnerId, "resultSubmittedByTeamId", submitterTeamId, "resultConfirmedByOpponent", false).await()
                onSuccess()
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    // 🔹 UPDATED: Verifies the match AND updates Win/Loss records for both teams!
    fun confirmMatchResult(challengeId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val doc = db.collection("challenges").document(challengeId).get().await()
                val match = doc.toObject(Challenge::class.java)

                if (match != null) {
                    // 1. Lock the match and put it on the Score Wall
                    db.collection("challenges").document(challengeId).update(
                        "status", "completed", "resultConfirmedByOpponent", true
                    ).await()

                    val teamAId = match.challengerTeamId
                    val teamBId = match.targetTeamId ?: match.responses.lastOrNull()?.responderTeamId ?: ""
                    val winnerId = match.winnerId

                    // 2. Increment Team A Stats
                    if (teamAId.isNotBlank()) {
                        val updateA = mutableMapOf<String, Any>("matchesPlayed" to FieldValue.increment(1))
                        if (winnerId == teamAId) updateA["matchesWon"] = FieldValue.increment(1) else updateA["matchesLost"] = FieldValue.increment(1)
                        db.collection("teams").document(teamAId).update(updateA)
                    }

                    // 3. Increment Team B Stats
                    if (teamBId.isNotBlank()) {
                        val updateB = mutableMapOf<String, Any>("matchesPlayed" to FieldValue.increment(1))
                        if (winnerId == teamBId) updateB["matchesWon"] = FieldValue.increment(1) else updateB["matchesLost"] = FieldValue.increment(1)
                        db.collection("teams").document(teamBId).update(updateB)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun loadCompletedMatches() {
        _state.value = _state.value.copy(isLoading = true)
        db.collection("challenges").whereEqualTo("status", "completed").orderBy("timestamp", Query.Direction.DESCENDING).limit(50)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { _state.value = _state.value.copy(isLoading = false); return@addSnapshotListener }
                val matches = snapshot?.documents?.mapNotNull { doc -> doc.toObject(Challenge::class.java)?.copy(id = doc.id) } ?: emptyList()
                _state.value = _state.value.copy(incoming = matches, isLoading = false)
            }
    }
}