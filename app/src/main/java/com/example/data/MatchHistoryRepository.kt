package com.example.data

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class MatchHistoryRepository(private val matchHistoryDao: MatchHistoryDao) {
    val lastFiveMatches: Flow<List<MatchHistoryEntity>> = matchHistoryDao.getLastFiveMatches()

    suspend fun saveMatch(match: MatchHistoryEntity) {
        matchHistoryDao.insertMatch(match)
        matchHistoryDao.trimHistory()
    }
}
