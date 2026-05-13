package com.shreyas.kreedaankana.features.challenge.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()

    // 🔹 Real-time listener for chat messages
    fun listenToMessages(challengeId: String, onResult: (List<Message>) -> Unit) {
        db.collection("challenges").document(challengeId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onResult(messages)
            }
    }

    // 🔹 Send a new message
    suspend fun sendMessage(challengeId: String, message: Message) {
        db.collection("challenges").document(challengeId).collection("messages").add(message).await()
    }
}