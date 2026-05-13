package com.shreyas.kreedaankana.features.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyas.kreedaankana.features.notifications.data.AppNotification
import com.shreyas.kreedaankana.features.notifications.data.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0
)

class NotificationViewModel : ViewModel() {
    private val repo = NotificationRepository()
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state

    fun startListening(userId: String) {
        repo.listenToNotifications(userId) { list ->
            _state.value = NotificationState(
                isLoading = false,
                notifications = list,
                unreadCount = list.count { !it.isRead }
            )
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch { repo.markAsRead(id) }
    }

    // 🔹 NEW: Handle Team Invites directly from the Notification Screen
    fun respondToTeamInvite(notificationId: String, teamId: String, accept: Boolean, userId: String, userName: String) {
        viewModelScope.launch {
            try {
                if (accept) {
                    val newMember = mapOf("userId" to userId, "name" to userName, "role" to "Player")
                    db.collection("teams").document(teamId).update(
                        "members", FieldValue.arrayUnion(newMember),
                        "memberIds", FieldValue.arrayUnion(userId)
                    ).await()
                }
                // Remove the notification once it's handled
                db.collection("notifications").document(notificationId).delete().await()
            } catch (e: Exception) {
                // Silently handle error or expose to state if needed
            }
        }
    }
}