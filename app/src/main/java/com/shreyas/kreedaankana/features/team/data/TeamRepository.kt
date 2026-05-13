package com.shreyas.kreedaankana.features.team.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shreyas.kreedaankana.features.notifications.data.AppNotification
import kotlinx.coroutines.tasks.await

class TeamRepository {
    private val db = FirebaseFirestore.getInstance()

    fun listenToMyTeam(userId: String, onResult: (Team?) -> Unit) {
        db.collection("teams")
            .whereArrayContains("adminIds", userId)
            .addSnapshotListener { snapshot, _ ->
                val team = snapshot?.documents?.firstOrNull()?.let {
                    it.toObject(Team::class.java)?.copy(id = it.id)
                }
                onResult(team)
            }
    }

    suspend fun fetchRealUserAndAdd(teamId: String, targetUserId: String) {
        val userDoc = db.collection("users").document(targetUserId).get().await()
        if (userDoc.exists()) {
            val realName = userDoc.getString("name") ?: "Unknown User"
            val newMember = mapOf("userId" to targetUserId, "name" to realName, "role" to "Player")
            db.collection("teams").document(teamId).update(
                "members", FieldValue.arrayUnion(newMember),
                "memberIds", FieldValue.arrayUnion(targetUserId)
            ).await()
        } else {
            throw Exception("User ID not found.")
        }
    }

    suspend fun getPendingUsers(userIds: List<String>): List<Map<String, String>> {
        if (userIds.isEmpty()) return emptyList()
        val result = mutableListOf<Map<String, String>>()
        for (id in userIds) {
            val doc = db.collection("users").document(id).get().await()
            if (doc.exists()) {
                result.add(mapOf("userId" to id, "name" to (doc.getString("name") ?: "Unknown")))
            }
        }
        return result
    }

    suspend fun acceptJoinRequest(teamId: String, targetUserId: String) {
        val userDoc = db.collection("users").document(targetUserId).get().await()
        val realName = userDoc.getString("name") ?: "Unknown User"
        val newMember = mapOf("userId" to targetUserId, "name" to realName, "role" to "Player")

        db.collection("teams").document(teamId).update(
            "pendingJoinRequests", FieldValue.arrayRemove(targetUserId),
            "memberIds", FieldValue.arrayUnion(targetUserId),
            "members", FieldValue.arrayUnion(newMember)
        ).await()
    }

    suspend fun rejectJoinRequest(teamId: String, targetUserId: String) {
        db.collection("teams").document(teamId).update(
            "pendingJoinRequests", FieldValue.arrayRemove(targetUserId)
        ).await()
    }

    suspend fun removeMember(teamId: String, targetUserId: String) {
        val teamDoc = db.collection("teams").document(teamId).get().await()
        val team = teamDoc.toObject(Team::class.java)
        if (team != null) {
            val updatedMembers = team.members.filter { it.userId != targetUserId }
            db.collection("teams").document(teamId).update(
                "memberIds", FieldValue.arrayRemove(targetUserId),
                "adminIds", FieldValue.arrayRemove(targetUserId),
                "members", updatedMembers
            ).await()
        }
    }

    suspend fun deleteTeam(teamId: String) {
        db.collection("teams").document(teamId).delete().await()
    }

    suspend fun updateTeamSettings(teamId: String, updates: Map<String, Any>) {
        db.collection("teams").document(teamId).update(updates).await()
    }

    suspend fun addCoAdmin(teamId: String, newAdminId: String) {
        db.collection("teams").document(teamId).update("adminIds", FieldValue.arrayUnion(newAdminId)).await()
    }

    suspend fun removeAdmin(teamId: String, targetAdminId: String) {
        db.collection("teams").document(teamId).update("adminIds", FieldValue.arrayRemove(targetAdminId)).await()
    }

    suspend fun getDiscoverableTeams(currentUserId: String): List<Team> {
        val snapshot = db.collection("teams").whereEqualTo("isLookingForMatches", true).get().await()
        return snapshot.documents.mapNotNull { doc ->
            val team = doc.toObject(Team::class.java)?.copy(id = doc.id)
            if (team?.memberIds?.contains(currentUserId) == true) null else team
        }
    }

    suspend fun getMyTeams(userId: String): List<Team> {
        val snapshot = db.collection("teams").whereArrayContains("memberIds", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Team::class.java) }
    }

    suspend fun sendTeamInvite(teamId: String, teamName: String, targetUserId: String) {
        val notification = mapOf(
            "receiverId" to targetUserId,
            "title" to "Team Invitation",
            "message" to "You have been invited to join $teamName.",
            "type" to "TEAM_INVITE",
            "relatedId" to teamId,
            "isRead" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("notifications").add(notification).await()
    }

    // 🔹 MISSING FUNCTION 1: Listen for Challenge Acceptances
    fun listenToTeamNotifications(teamId: String, onResult: (List<AppNotification>) -> Unit) {
        db.collection("team_notifications")
            .whereEqualTo("receiverId", teamId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                onResult(list)
            }
    }

    // 🔹 MISSING FUNCTION 2: Mark them as read
    suspend fun markTeamNotificationRead(id: String) {
        db.collection("team_notifications").document(id).update("isRead", true).await()
    }
}