package com.shreyas.kreedaankana.features.challenge.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MatchRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveMatch(match: Match) {
        if (match.id.isEmpty()) {
            val ref = db.collection("matches").document()
            db.collection("matches").document(ref.id).set(match.copy(id = ref.id)).await()
        } else {
            db.collection("matches").document(match.id).set(match).await()
        }
    }

    suspend fun getMatchByChallengeId(challengeId: String): Match? {
        val snapshot = db.collection("matches").whereEqualTo("challengeId", challengeId).get().await()
        return snapshot.documents.firstOrNull()?.toObject(Match::class.java)
    }

    suspend fun updateChallengeStatusToScheduled(challengeId: String) {
        db.collection("challenges").document(challengeId).update("status", "scheduled").await()
    }

    // 🔹 For the Profile stats
    suspend fun getUserMatchStats(userId: String): Map<String, Int> {
        return mapOf("total" to 0, "wins" to 0, "losses" to 0)
    }

    // 🔹 For the Score Wall
    fun listenCompletedMatches(onResult: (List<Match>) -> Unit) {
        db.collection("matches").whereEqualTo("status", "completed").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val matches = snapshot?.documents?.mapNotNull { it.toObject(Match::class.java)?.copy(id = it.id) } ?: emptyList()
            onResult(matches)
        }
    }

    // 🔹 NEW: For the Team Calendar
    fun listenToAllMatches(onResult: (List<Match>) -> Unit) {
        db.collection("matches").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val matches = snapshot?.documents?.mapNotNull { it.toObject(Match::class.java)?.copy(id = it.id) } ?: emptyList()
            onResult(matches)
        }
    }
}