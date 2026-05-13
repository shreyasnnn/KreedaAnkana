package com.shreyas.kreedaankana.features.notifications.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()

    // Send a notification to a specific user
    suspend fun sendNotification(notification: AppNotification) {
        db.collection("notifications")
            .add(notification.copy(timestamp = com.google.firebase.Timestamp.now()))
            .await()
    }

    // Listen for real-time notifications for the current user
    fun listenToNotifications(userId: String, onResult: (List<AppNotification>) -> Unit) {
        db.collection("notifications")
            .whereEqualTo("receiverId", userId)
            // 🔹 FIX: Removed .orderBy() to bypass the Firestore Composite Index requirement!
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // If an error happens, it will log it here instead of failing silently
                    e.printStackTrace()
                    return@addSnapshotListener
                }

                // 🔹 FIX: Sort the notifications locally in Kotlin instead
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.timestamp } ?: emptyList()

                onResult(list)
            }
    }

    suspend fun markAsRead(notificationId: String) {
        db.collection("notifications").document(notificationId)
            .update("isRead", true).await()
    }
}