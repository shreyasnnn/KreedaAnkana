package com.shreyas.kreedaankana.features.team.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shreyas.kreedaankana.core.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class TeamChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TeamChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(TeamChatState())
    val state: StateFlow<TeamChatState> = _state

    fun listenToMessages(teamId: String) {
        _state.value = _state.value.copy(isLoading = true)
        db.collection("teams").document(teamId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                    return@addSnapshotListener
                }
                val msgs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _state.value = TeamChatState(messages = msgs, isLoading = false)
            }
    }

    fun sendMessage(teamId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val msg = ChatMessage(
                    senderId = AuthManager.getUserId(),
                    senderName = AuthManager.getUserName(),
                    text = text.trim()
                )
                db.collection("teams").document(teamId).collection("messages").add(msg).await()
            } catch (e: Exception) {
                // Handle error quietly
            }
        }
    }

    fun deleteMessage(teamId: String, messageId: String) {
        viewModelScope.launch {
            try {
                db.collection("teams").document(teamId).collection("messages").document(messageId).delete().await()
            } catch (e: Exception) {}
        }
    }
}