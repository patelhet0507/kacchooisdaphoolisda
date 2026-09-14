package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.model.Card
import com.example.model.Suit
import com.example.model.Rank
import com.example.model.Player
import com.example.model.PlayedCard
import com.example.model.GameMode
import com.example.model.ScoringRule
import com.example.model.GamePhase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String,
    val scoringRule: String,
    val playerNames: String, // Comma separated
    val winnerName: String,
    val winnerScore: Int,
    val totalRounds: Int,
    val playedTricksJson: String // List<PlayedTrickHistory> serialized
)

data class PlayedTrickHistory(
    val roundIndex: Int,
    val trickIndex: Int,
    val trumpSuit: String,
    val leadSuit: String?,
    val playedCards: List<PlayedCardHistory>,
    val winnerName: String
)

data class PlayedCardHistory(
    val playerName: String,
    val cardId: String
)

class MatchTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromPlayedTrickHistoryList(value: List<PlayedTrickHistory>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPlayedTrickHistoryList(value: String): List<PlayedTrickHistory> {
        val listType = object : TypeToken<List<PlayedTrickHistory>>() {}.type
        return gson.fromJson(value, listType)
    }
}
