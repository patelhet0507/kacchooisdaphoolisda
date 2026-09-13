package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scorecard_games")
data class ScorecardGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val playerNamesRaw: String, // Comma-separated names
    val roundsConfigRaw: String, // Comma-separated card counts
    val currentRoundIndex: Int = 0,
    val scoringRuleName: String = "STANDARD"
) {
    fun getPlayerNames(): List<String> = playerNamesRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun getRoundsConfig(): List<Int> = roundsConfigRaw.split(",").mapNotNull { it.trim().toIntOrNull() }
}

@Entity(tableName = "scorecard_rounds")
data class ScorecardRoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val roundIndex: Int,
    val cardCount: Int,
    val trumpSuitName: String,
    val dealerIndex: Int,
    val bidsRaw: String = "", // Comma-separated: "player0Bid,player1Bid,..."
    val tricksWonRaw: String = "", // Comma-separated: "player0Won,player1Won,..."
    val roundScoresRaw: String = "" // Comma-separated: "player0Score,player1Score,..."
) {
    fun getBids(): List<Int?> {
        if (bidsRaw.isEmpty()) return emptyList()
        return bidsRaw.split(",").map { it.trim().toIntOrNull() }
    }

    fun getTricksWon(): List<Int?> {
        if (tricksWonRaw.isEmpty()) return emptyList()
        return tricksWonRaw.split(",").map { it.trim().toIntOrNull() }
    }

    fun getRoundScores(): List<Int> {
        if (roundScoresRaw.isEmpty()) return emptyList()
        return roundScoresRaw.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
