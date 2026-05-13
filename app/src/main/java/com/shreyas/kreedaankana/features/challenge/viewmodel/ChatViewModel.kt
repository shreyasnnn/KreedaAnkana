package com.shreyas.kreedaankana.features.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyas.kreedaankana.features.challenge.data.ChatRepository
import com.shreyas.kreedaankana.features.challenge.data.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state

    fun loadMessages(challengeId: String) {
        _state.value = _state.value.copy(isLoading = true)
        repo.listenToMessages(challengeId) { msgs ->
            _state.value = ChatState(messages = msgs, isLoading = false)
        }
    }

    fun sendMessage(
        challengeId: String,
        senderId: String,
        senderTeamId: String,
        senderTeamName: String,
        text: String,
        type: String = "TEXT"
    ) {
        viewModelScope.launch {
            val msg = Message(
                challengeId = challengeId,
                senderId = senderId,
                senderTeamId = senderTeamId,
                senderTeamName = senderTeamName,
                text = text,
                type = type
            )
            repo.sendMessage(challengeId, msg)
        }
    }
}