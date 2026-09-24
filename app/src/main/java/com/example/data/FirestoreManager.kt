package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class FirestoreUserData(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val gamesPlayed: Int = 0,
    val winsCount: Int = 0,
    val highestScore: Int = 0,
    val selectedAvatar: String = "lion",
    val selectedTableTheme: String = "emerald",
    val unlockedAchievements: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

class FirestoreManager private constructor() {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance().apply {
                try {
                    firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                } catch (e: Exception) {
                    // Persistence already configured
                }
            }
        } catch (t: Throwable) {
            Log.w("FirestoreManager", "Firestore instance not ready: ${t.message}")
            null
        }

    /**
     * Upload / sync user profile data to Firestore document at `users/{uid}`
     */
    suspend fun saveUserProfile(
        uid: String,
        name: String,
        email: String,
        photoUrl: String,
        gamesPlayed: Int,
        winsCount: Int,
        highestScore: Int,
        selectedAvatar: String,
        selectedTableTheme: String,
        unlockedAchievements: Set<String>,
        friends: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext false
        try {
            val db = firestore ?: return@withContext false
            val data = hashMapOf(
                "uid" to uid,
                "displayName" to name,
                "email" to email,
                "photoUrl" to photoUrl,
                "gamesPlayed" to gamesPlayed,
                "winsCount" to winsCount,
                "highestScore" to highestScore,
                "selectedAvatar" to selectedAvatar,
                "selectedTableTheme" to selectedTableTheme,
                "unlockedAchievements" to unlockedAchievements.toList(),
                "friends" to friends,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()
            Log.d("FirestoreManager", "User profile synced successfully to Firestore for uid: $uid")
            true
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Failed to save profile to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Fetch user profile from Firestore at `users/{uid}`
     */
    suspend fun fetchUserProfile(uid: String): FirestoreUserData? = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext null
        try {
            val db = firestore ?: return@withContext null
            val snapshot = db.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: return@withContext null
                val achievementsList = (data["unlockedAchievements"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val friendsList = (data["friends"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                FirestoreUserData(
                    uid = uid,
                    displayName = data["displayName"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    photoUrl = data["photoUrl"] as? String ?: "",
                    gamesPlayed = (data["gamesPlayed"] as? Number)?.toInt() ?: 0,
                    winsCount = (data["winsCount"] as? Number)?.toInt() ?: 0,
                    highestScore = (data["highestScore"] as? Number)?.toInt() ?: 0,
                    selectedAvatar = data["selectedAvatar"] as? String ?: "lion",
                    selectedTableTheme = data["selectedTableTheme"] as? String ?: "emerald",
                    unlockedAchievements = achievementsList,
                    friends = friendsList,
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Failed to fetch profile from Firestore: ${e.message}")
            null
        }
    }

    /**
     * Sync a completed match record into Firestore `users/{uid}/match_history/{matchId}`
     */
    suspend fun saveMatchHistory(
        uid: String,
        matchId: String,
        gameMode: String,
        scoringRule: String,
        playerNames: String,
        winnerName: String,
        winnerScore: Int,
        totalRounds: Int,
        timestamp: Long
    ): Boolean = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext false
        try {
            val db = firestore ?: return@withContext false
            val matchDoc = hashMapOf(
                "matchId" to matchId,
                "gameMode" to gameMode,
                "scoringRule" to scoringRule,
                "playerNames" to playerNames,
                "winnerName" to winnerName,
                "winnerScore" to winnerScore,
                "totalRounds" to totalRounds,
                "timestamp" to timestamp,
                "syncedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid)
                .collection("match_history").document(matchId)
                .set(matchDoc, SetOptions.merge())
                .await()
            Log.d("FirestoreManager", "Match history $matchId synced to Firestore.")
            true
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Failed to sync match history to Firestore: ${e.message}")
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirestoreManager? = null

        fun getInstance(): FirestoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreManager().also { INSTANCE = it }
            }
        }
    }
}
