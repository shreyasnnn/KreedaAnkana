package com.shreyas.kreedaankana.core.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getUserId(): String = auth.currentUser?.uid ?: ""
    fun getUserName(): String = auth.currentUser?.displayName ?: "Guest"
    fun getUserEmail(): String = auth.currentUser?.email ?: ""
    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getGoogleClient(activity: Activity): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.shreyas.kreedaankana.R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, options)
    }

    fun handleSignInResult(
        data: Intent?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        // 🔹 This ensures existing users get a document if they don't have one
                        syncUserToFirestore(user, onSuccess, onError)
                    } else {
                        onSuccess()
                    }
                }
                .addOnFailureListener { onError(it.message ?: "Auth Failed") }
        } catch (e: Exception) {
            onError(e.message ?: "Google Sign-In Failed")
        }
    }

    private fun syncUserToFirestore(
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userData = mapOf(
            "name" to (user.displayName ?: "Athlete"),
            "email" to (user.email ?: ""),
            "uid" to user.uid
        )

        // 🔹 Merge = true ensures we don't delete existing user data if the doc already exists
        db.collection("users").document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("AuthManager", "User synced: ${user.uid}")
                onSuccess()
            }
            .addOnFailureListener { e -> onError("Sync failed: ${e.message}") }
    }

    fun logout(activity: Activity) {
        auth.signOut()
        GoogleSignIn.getClient(activity, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
    }
}