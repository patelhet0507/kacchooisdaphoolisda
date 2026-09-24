package com.example.data

import com.example.auth.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MatchHistoryRepository(private val matchHistoryDao: MatchHistoryDao) {
    val lastFiveMatches: Flow<List<MatchHistoryEntity>> = matchHistoryDao.getLastFiveMatches()
    private val firestoreManager = FirestoreManager.getInstance()
    private val authManager = AuthManager.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun saveMatch(match: MatchHistoryEntity) {
        matchHistoryDao.insertMatch(match)
        matchHistoryDao.trimHistory()

        val uid = authManager.getCurrentUserState().uid ?: authManager.getCurrentUser()?.uid
        if (!uid.isNullOrBlank()) {
            scope.launch {
                firestoreManager.saveMatchHistory(
                    uid = uid,
                    matchId = "match_${match.id}_${match.timestamp}",
                    gameMode = match.gameMode,
                    scoringRule = match.scoringRule,
                    playerNames = match.playerNames,
                    winnerName = match.winnerName,
                    winnerScore = match.winnerScore,
                    totalRounds = match.totalRounds,
                    timestamp = match.timestamp
                )
            }
        }
    }
}

