package com.shreyas.kreedaankana.features.challenge.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class ChallengeRepository {
    private val db = FirebaseFirestore.getInstance()

    // Post a new challenge
    suspend fun postChallenge(challenge: Challenge) {
        db.collection("challenges").add(challenge).await()
    }

    // 🔹 NEW: Listen for challenges sent TO my team
    fun listenToIncomingChallenges(teamId: String, onResult: (List<Challenge>) -> Unit) {
        db.collection("challenges")
            .whereEqualTo("targetTeamId", teamId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Challenge::class.java)?.copy(id = it.id)
                } ?: emptyList()
                onResult(list)
            }
    }

    // 🔹 NEW: Fetch challenges that are open to any team
    fun listenToOpenChallenges(onResult: (List<Challenge>) -> Unit) {
        db.collection("challenges")
            .whereEqualTo("status", "open") // Only fetch open ones
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val challenges = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Challenge::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onResult(challenges)
            }
    }

    // Add inside ChallengeRepository.kt
    suspend fun addChallengeResponse(challengeId: String, response: ChallengeResponse) {
        // Adds the new response to the 'responses' array in Firestore
        db.collection("challenges").document(challengeId)
            .update("responses", com.google.firebase.firestore.FieldValue.arrayUnion(response))
            .await() // 🔹 Changed this line
    }

    // 🔹 NEW: Update challenge status (Accept/Decline)
    suspend fun updateChallengeStatus(challengeId: String, newStatus: String) {
        db.collection("challenges").document(challengeId)
            .update("status", newStatus)
            .await()
    }
}